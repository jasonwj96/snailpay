package com.jasonwj.snailpay.controller;

import com.jasonwj.snailpay.dto.KycDocumentResponse;
import com.jasonwj.snailpay.dto.KycStatusResponse;
import com.jasonwj.snailpay.dto.KycSubmissionRequest;
import com.jasonwj.snailpay.dto.UploadUrlRequest;
import com.jasonwj.snailpay.dto.UploadUrlResponse;
import com.jasonwj.snailpay.models.CurrentCustomerProvider;
import com.jasonwj.snailpay.service.KycDocumentService;
import com.jasonwj.snailpay.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

// Every method derives the customer from the authenticated request. There is no
// customer id or externalId in any path or body here, on purpose.
@RestController
@RequestMapping("/onboarding/kyc")
@RequiredArgsConstructor
public class KycController {

    private final CurrentCustomerProvider currentCustomer;
    private final KycService kycService;
    private final KycDocumentService documentService;

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<KycStatusResponse> status(@RequestHeader HttpHeaders headers) {
        return currentCustomer.currentCustomerId(headers).flatMap(kycService::getStatus);
    }

    @PostMapping(value = "/documents/upload-url",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UploadUrlResponse> uploadUrl(@RequestHeader HttpHeaders headers,
                                             @RequestBody @Valid UploadUrlRequest request) {
        return currentCustomer.currentCustomerId(headers)
                .flatMap(customerId -> documentService.createUploadUrl(customerId, request));
    }

    @PostMapping(value = "/documents/{documentId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<KycDocumentResponse> completeUpload(@RequestHeader HttpHeaders headers,
                                                    @PathVariable UUID documentId) {
        return currentCustomer.currentCustomerId(headers)
                .flatMap(customerId -> documentService.completeUpload(customerId, documentId));
    }

    @PostMapping(value = "/submit",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<KycStatusResponse> submit(@RequestHeader HttpHeaders headers,
                                          @RequestBody @Valid KycSubmissionRequest request) {
        String forwardedFor = headers.getFirst("X-Forwarded-For");
        String userAgent = headers.getFirst(HttpHeaders.USER_AGENT);
        return currentCustomer.currentCustomerId(headers)
                .flatMap(customerId -> kycService.submit(customerId, request, forwardedFor, userAgent));
    }
}
