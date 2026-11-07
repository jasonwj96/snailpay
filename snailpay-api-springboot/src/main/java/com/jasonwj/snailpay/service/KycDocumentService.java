package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.config.KycProperties;
import com.jasonwj.snailpay.dto.KycDocumentResponse;
import com.jasonwj.snailpay.dto.UploadUrlRequest;
import com.jasonwj.snailpay.dto.UploadUrlResponse;
import com.jasonwj.snailpay.enums.KycDocumentStatus;
import com.jasonwj.snailpay.enums.KycStatus;
import com.jasonwj.snailpay.models.KycDocument;
import com.jasonwj.snailpay.repository.CustomerRepository;
import com.jasonwj.snailpay.repository.KycDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Upload flow:
 *   1. createUploadUrl: validates the request, records a PENDING_UPLOAD row and
 *      returns a short-lived presigned PUT URL.
 *   2. The browser uploads straight to S3.
 *   3. completeUpload: the server checks the object really exists, matches the
 *      declared size and type, and starts with the right file signature. Only
 *      then is the document marked UPLOADED.
 */
@Service
@RequiredArgsConstructor
public class KycDocumentService {

    // Declared content type -> first bytes a genuine file of that type starts with.
    private static final Map<String, byte[]> SIGNATURES = Map.of(
            "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}
    );

    private final KycDocumentRepository documentRepository;
    private final CustomerRepository customerRepository;
    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final KycProperties props;

    @Transactional
    public Mono<UploadUrlResponse> createUploadUrl(UUID customerId, UploadUrlRequest request) {
        if (!SIGNATURES.containsKey(request.contentType())) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only JPEG, PNG or PDF files are accepted"));
        }
        if (request.sizeBytes() > props.maxUploadBytes()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE, "Files can be at most 10 MB"));
        }

        return requireNotSubmitted(customerId)
                // A new upload replaces the previous one of the same type. The old
                // row and object are kept (CIP record retention), just marked.
                .then(documentRepository.supersedeActive(customerId, request.documentType().name()))
                .then(Mono.defer(() -> {
                    // The S3 key holds no filename and no personal data.
                    String key = "kyc/%s/%s".formatted(customerId, UUID.randomUUID());

                    KycDocument document = new KycDocument();
                    document.setCustomerId(customerId);
                    document.setDocumentType(request.documentType());
                    document.setStatus(KycDocumentStatus.PENDING_UPLOAD);
                    document.setS3Key(key);
                    document.setContentType(request.contentType());
                    document.setSizeBytes(request.sizeBytes());

                    return documentRepository.save(document).map(this::presign);
                }));
    }

    // noRollbackFor: when a file is rejected we save REJECTED and then signal an
    // error. Without this, the error would roll back the REJECTED status too.
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public Mono<KycDocumentResponse> completeUpload(UUID customerId, UUID documentId) {
        return documentRepository.findByIdAndCustomerId(documentId, customerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found")))
                .flatMap(document -> {
                    if (document.getStatus() == KycDocumentStatus.UPLOADED) {
                        return Mono.just(document); // idempotent
                    }
                    if (document.getStatus() != KycDocumentStatus.PENDING_UPLOAD) {
                        return Mono.<KycDocument>error(new ResponseStatusException(
                                HttpStatus.CONFLICT, "This upload is no longer valid. Please upload the file again."));
                    }

                    return Mono.fromCallable(() -> inspectObject(document))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(valid -> {
                                if (!valid) {
                                    document.setStatus(KycDocumentStatus.REJECTED);
                                    return documentRepository.save(document)
                                            .then(Mono.<KycDocument>error(new ResponseStatusException(
                                                    HttpStatus.UNPROCESSABLE_ENTITY,
                                                    "The file could not be accepted. Please upload a clear JPEG, PNG or PDF.")));
                                }
                                document.setStatus(KycDocumentStatus.UPLOADED);
                                document.setUploadedAt(Instant.now());
                                return documentRepository.save(document);
                            });
                })
                .map(KycDocumentResponse::from);
    }

    private Mono<Void> requireNotSubmitted(UUID customerId) {
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")))
                .flatMap(customer -> customer.getKycStatus() == KycStatus.NOT_STARTED
                        ? Mono.<Void>empty()
                        : Mono.<Void>error(new ResponseStatusException(
                        HttpStatus.CONFLICT, "Your application was already submitted")));
    }

    private UploadUrlResponse presign(KycDocument document) {
        // contentLength and contentType become signed headers, so S3 rejects an
        // upload whose size or type differs from what was declared. The size is
        // checked again in completeUpload as a backstop.
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(document.getS3Key())
                .contentType(document.getContentType())
                .contentLength(document.getSizeBytes())
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(r -> r
                .signatureDuration(props.uploadUrlTtl())
                .putObjectRequest(put));

        // Browsers set Host and Content-Length themselves and refuse to let
        // scripts override them, so only the remaining signed headers go back.
        Map<String, String> headers = new LinkedHashMap<>();
        presigned.signedHeaders().forEach((name, values) -> {
            String lower = name.toLowerCase();
            if (!lower.equals("host") && !lower.equals("content-length")) {
                headers.put(name, String.join(",", values));
            }
        });

        return new UploadUrlResponse(document.getId(), presigned.url().toString(), headers, presigned.expiration());
    }

    // Runs on a blocking thread. Returns false (after deleting the object) when
    // the uploaded file doesn't match what was declared.
    private boolean inspectObject(KycDocument document) {
        HeadObjectResponse head;
        try {
            head = s3Client.headObject(r -> r.bucket(props.bucket()).key(document.getS3Key()));
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "File not found. Please upload it again.");
            }
            throw e;
        }

        boolean sizeMatches = head.contentLength() != null && head.contentLength() == document.getSizeBytes();
        boolean typeMatches = document.getContentType().equals(head.contentType());

        boolean signatureMatches = false;
        if (sizeMatches && typeMatches) {
            byte[] expected = SIGNATURES.get(document.getContentType());
            byte[] firstBytes = s3Client.getObjectAsBytes(r -> r
                    .bucket(props.bucket())
                    .key(document.getS3Key())
                    .range("bytes=0-7")).asByteArray();
            signatureMatches = firstBytes.length >= expected.length
                    && Arrays.equals(Arrays.copyOf(firstBytes, expected.length), expected);
        }

        if (!signatureMatches) {
            s3Client.deleteObject(r -> r.bucket(props.bucket()).key(document.getS3Key()));
            return false;
        }
        return true;
    }
}
