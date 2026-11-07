package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.config.KycProperties;
import com.jasonwj.snailpay.dto.KycDocumentResponse;
import com.jasonwj.snailpay.dto.KycStatusResponse;
import com.jasonwj.snailpay.dto.KycSubmissionRequest;
import com.jasonwj.snailpay.enums.AddressType;
import com.jasonwj.snailpay.enums.ConsentType;
import com.jasonwj.snailpay.enums.IdDocumentType;
import com.jasonwj.snailpay.enums.KycDocumentStatus;
import com.jasonwj.snailpay.enums.KycDocumentType;
import com.jasonwj.snailpay.enums.KycStatus;
import com.jasonwj.snailpay.enums.ResidencyStatus;
import com.jasonwj.snailpay.enums.TinType;
import com.jasonwj.snailpay.models.Customer;
import com.jasonwj.snailpay.models.CustomerAddress;
import com.jasonwj.snailpay.models.FieldEncryptor;
import com.jasonwj.snailpay.models.KycDocument;
import com.jasonwj.snailpay.models.KycProfile;
import com.jasonwj.snailpay.repository.CustomerAddressRepository;
import com.jasonwj.snailpay.repository.CustomerRepository;
import com.jasonwj.snailpay.repository.KycDocumentRepository;
import com.jasonwj.snailpay.repository.KycProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class KycService {

    private static final int MIN_AGE_YEARS = 18;

    // No leading 000, 666 or 9xx area; no 00 group; no 0000 serial.
    private static final Pattern SSN = Pattern.compile("^(?!000|666|9\\d\\d)\\d{3}(?!00)\\d{2}(?!0000)\\d{4}$");
    // ITINs start with 9 and use a restricted range for the 4th and 5th digits.
    private static final Pattern ITIN = Pattern.compile("^9\\d{2}(5\\d|6[0-5]|7\\d|8[0-8]|9[0-2]|9[4-9])\\d{4}$");
    private static final Pattern ZIP = Pattern.compile("^\\d{5}(-\\d{4})?$");
    // CIP requires a street address for the residence; PO boxes don't qualify.
    private static final Pattern PO_BOX = Pattern.compile("(?i)\\b(p\\.?\\s*o\\.?\\s*box|post\\s+office\\s+box)\\b");

    private static final Set<String> US_STATES = Set.of((
            "AL AK AZ AR CA CO CT DE DC FL GA HI ID IL IN IA KS KY LA ME MD MA MI MN MS MO MT NE NV NH NJ NM NY NC ND "
                    + "OH OK OR PA RI SC SD TN TX UT VT VA WA WV WI WY AS GU MP PR VI").split(" "));

    private final CustomerRepository customerRepository;
    private final KycProfileRepository profileRepository;
    private final KycDocumentRepository documentRepository;
    private final CustomerAddressRepository addressRepository;
    private final FieldEncryptor fieldEncryptor;
    private final ConsentRecorder consentRecorder;
    private final KycProperties props;

    public Mono<KycStatusResponse> getStatus(UUID customerId) {
        Mono<Customer> customer = customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")));

        Mono<List<KycDocumentResponse>> documents = documentRepository.findByCustomerId(customerId)
                .filter(d -> d.getStatus() == KycDocumentStatus.UPLOADED
                        || d.getStatus() == KycDocumentStatus.PENDING_UPLOAD)
                .map(KycDocumentResponse::from)
                .collectList();

        return Mono.zip(customer, documents,
                (c, docs) -> new KycStatusResponse(c.getKycStatus(), docs));
    }

    @Transactional
    public Mono<KycStatusResponse> submit(UUID customerId, KycSubmissionRequest request,
                                          String forwardedFor, String userAgent) {
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")))
                .flatMap(customer -> {
                    // Exceptions thrown here become error signals.
                    requirePreconditions(customer);
                    validateRequest(customer, request);

                    return documentRepository.findByCustomerId(customerId).collectList().flatMap(documents -> {
                        requireDocuments(documents, request);
                        return persist(customer, request, forwardedFor, userAgent);
                    });
                });
    }

    private Mono<KycStatusResponse> persist(Customer customer, KycSubmissionRequest request,
                                            String forwardedFor, String userAgent) {
        UUID customerId = customer.getId();
        String tin = request.tinType() == TinType.NONE ? null : request.tin();

        Mono<Optional<byte[]>> tinCipher = tin == null
                ? Mono.just(Optional.<byte[]>empty())
                : fieldEncryptor.encrypt(tin, context(customerId, "TIN")).map(Optional::of);
        Mono<byte[]> idCipher = fieldEncryptor.encrypt(request.idNumber().trim(), context(customerId, "ID_NUMBER"));

        return Mono.zip(tinCipher, idCipher).flatMap(ciphers -> {
            String blindIndex = tin == null ? null : blindIndex(tin);

            KycProfile profile = new KycProfile();
            profile.setCustomerId(customerId);
            profile.setResidencyStatus(request.residencyStatus());
            profile.setCitizenshipCountry(request.citizenshipCountry());
            profile.setTinType(request.tinType());
            profile.setTinCiphertext(ciphers.getT1().orElse(null));
            profile.setTinLast4(tin == null ? null : tin.substring(5));
            profile.setTinBlindIndex(blindIndex);
            profile.setIdDocumentType(request.idDocumentType());
            profile.setIdNumberCiphertext(ciphers.getT2());
            profile.setIdIssuingCountry(request.idIssuingCountry());
            profile.setIdIssuingState(request.idIssuingState());
            profile.setIdExpirationDate(request.idExpirationDate());
            profile.setSubmittedAt(Instant.now());

            KycSubmissionRequest.Address a = request.address();
            CustomerAddress address = new CustomerAddress();
            address.setCustomerId(customerId);
            address.setAddressType(AddressType.RESIDENTIAL);
            address.setLine1(a.line1().trim());
            address.setLine2(a.line2() == null || a.line2().isBlank() ? null : a.line2().trim());
            address.setCity(a.city().trim());
            address.setStateProvince(a.stateProvince().trim());
            address.setPostalCode(a.postalCode().trim());
            address.setCountryCode(a.countryCode());

            Mono<Boolean> duplicateTin = blindIndex == null
                    ? Mono.just(false)
                    : profileRepository.existsByTinBlindIndexAndCustomerIdNot(blindIndex, customerId);

            Mono<Void> consents = Flux.just(
                            ConsentType.CIP_NOTICE_ACKNOWLEDGED, ConsentType.ESIGN_CONSENT, ConsentType.KYC_CERTIFICATION)
                    .concatMap(type -> consentRecorder.record(
                            customerId, type, props.disclosureVersion(), forwardedFor, userAgent))
                    .then();

            return duplicateTin.flatMap(isDuplicate -> profileRepository.save(profile)
                    .then(addressRepository.save(address))
                    .then(consents)
                    .then(Mono.defer(() -> {
                        // Same TIN on another account: hold for manual review instead
                        // of rejecting, and don't tell the applicant why.
                        customer.setKycStatus(isDuplicate ? KycStatus.REQUIRES_REVIEW : KycStatus.PENDING);
                        return customerRepository.save(customer);
                    }))
                    .then(getStatus(customerId)));
        });
    }

    private void requirePreconditions(Customer customer) {
        if (customer.getEmailVerifiedAt() == null || customer.getPhoneVerifiedAt() == null) {
            throw status(HttpStatus.CONFLICT, "Verify your email and phone before continuing");
        }
        if (customer.getKycStatus() != KycStatus.NOT_STARTED) {
            throw status(HttpStatus.CONFLICT, "Your application was already submitted");
        }
    }

    private void validateRequest(Customer customer, KycSubmissionRequest request) {
        // Minimum age: CIP covers adults opening accounts in their own name.
        LocalDate dob = customer.getDateOfBirth();
        if (dob == null || Period.between(dob, LocalDate.now(ZoneOffset.UTC)).getYears() < MIN_AGE_YEARS) {
            throw status(HttpStatus.UNPROCESSABLE_ENTITY, "You must be at least 18 years old to open an account");
        }

        // Taxpayer identification number.
        boolean usPerson = request.residencyStatus() != ResidencyStatus.NON_US_PERSON;
        if (usPerson && request.tinType() != TinType.SSN) {
            throw status(HttpStatus.UNPROCESSABLE_ENTITY, "A Social Security number is required");
        }
        switch (request.tinType()) {
            case SSN -> {
                if (request.tin() == null || !SSN.matcher(request.tin()).matches()) {
                    throw status(HttpStatus.UNPROCESSABLE_ENTITY, "Enter a valid Social Security number");
                }
            }
            case ITIN -> {
                if (request.tin() == null || !ITIN.matcher(request.tin()).matches()) {
                    throw status(HttpStatus.UNPROCESSABLE_ENTITY, "Enter a valid ITIN");
                }
            }
            case NONE -> {
                if (request.tin() != null) {
                    throw status(HttpStatus.UNPROCESSABLE_ENTITY, "Remove the tax ID or choose its type");
                }
                // Without a U.S. TIN, a non-U.S. person identifies with a passport or
                // alien identification card number (31 CFR 1020.220(a)(2)(i)(A)(4)).
                if (request.idDocumentType() != IdDocumentType.PASSPORT
                        && request.idDocumentType() != IdDocumentType.PERMANENT_RESIDENT_CARD) {
                    throw status(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Without a U.S. tax ID, identify with a passport or permanent resident card");
                }
            }
        }

        // Residential street address.
        KycSubmissionRequest.Address a = request.address();
        if (PO_BOX.matcher(a.line1()).find() || (a.line2() != null && PO_BOX.matcher(a.line2()).find())) {
            throw status(HttpStatus.UNPROCESSABLE_ENTITY, "Enter a street address. PO boxes are not accepted");
        }
        if ("US".equals(a.countryCode())) {
            if (!US_STATES.contains(a.stateProvince())) {
                throw status(HttpStatus.UNPROCESSABLE_ENTITY, "Enter a valid U.S. state");
            }
            if (!ZIP.matcher(a.postalCode()).matches()) {
                throw status(HttpStatus.UNPROCESSABLE_ENTITY, "Enter a valid ZIP code");
            }
        }

        // Identity document.
        boolean stateIssued = request.idDocumentType() == IdDocumentType.DRIVERS_LICENSE
                || request.idDocumentType() == IdDocumentType.STATE_ID;
        if (stateIssued && (!"US".equals(request.idIssuingCountry())
                || request.idIssuingState() == null || !US_STATES.contains(request.idIssuingState()))) {
            throw status(HttpStatus.UNPROCESSABLE_ENTITY, "Enter the U.S. state that issued your ID");
        }
    }

    private void requireDocuments(List<KycDocument> documents, KycSubmissionRequest request) {
        Set<KycDocumentType> uploaded = documents.stream()
                .filter(d -> d.getStatus() == KycDocumentStatus.UPLOADED)
                .map(KycDocument::getDocumentType)
                .collect(java.util.stream.Collectors.toSet());

        if (!uploaded.contains(KycDocumentType.GOVERNMENT_ID_FRONT)) {
            throw status(HttpStatus.UNPROCESSABLE_ENTITY, "Upload the front of your identity document");
        }
        if (request.idDocumentType() != IdDocumentType.PASSPORT
                && !uploaded.contains(KycDocumentType.GOVERNMENT_ID_BACK)) {
            throw status(HttpStatus.UNPROCESSABLE_ENTITY, "Upload the back of your identity document");
        }
    }

    private Map<String, String> context(UUID customerId, String purpose) {
        return Map.of("customer_id", customerId.toString(), "purpose", purpose);
    }

    // HMAC-SHA256 of the TIN with its own secret. It reveals nothing about the
    // number without the secret, but equal TINs give equal values.
    private String blindIndex(String tin) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(props.tinIndexSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(tin.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC unavailable", e);
        }
    }

    private static ResponseStatusException status(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }
}
