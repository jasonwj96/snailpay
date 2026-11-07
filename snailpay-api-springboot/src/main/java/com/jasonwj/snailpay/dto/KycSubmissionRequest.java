package com.jasonwj.snailpay.dto;

import com.jasonwj.snailpay.enums.IdDocumentType;
import com.jasonwj.snailpay.enums.ResidencyStatus;
import com.jasonwj.snailpay.enums.TinType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record KycSubmissionRequest(

        @NotNull
        ResidencyStatus residencyStatus,

        @NotBlank
        @Pattern(regexp = "[A-Z]{2}")
        String citizenshipCountry,

        @NotNull
        @Valid
        Address address,

        @NotNull
        TinType tinType,

        // Digits only, no dashes. Format rules (SSN vs ITIN) are checked in the service.
        @Pattern(regexp = "\\d{9}")
        String tin,

        @NotNull
        IdDocumentType idDocumentType,

        @NotBlank
        @Pattern(regexp = "[A-Za-z0-9 \\-]{4,32}")
        String idNumber,

        @NotBlank
        @Pattern(regexp = "[A-Z]{2}")
        String idIssuingCountry,

        @Pattern(regexp = "[A-Z]{2}")
        String idIssuingState,

        @NotNull
        @Future
        LocalDate idExpirationDate,

        @AssertTrue(message = "The identity verification notice must be acknowledged")
        boolean cipNoticeAcknowledged,

        @AssertTrue(message = "Consent to electronic disclosures is required")
        boolean esignConsent,

        @AssertTrue(message = "The accuracy certification must be accepted")
        boolean certificationAccepted
) {

    public record Address(

            @NotBlank @Size(max = 100)
            String line1,

            @Size(max = 100)
            String line2,

            @NotBlank @Size(max = 100)
            String city,

            @NotBlank @Size(max = 50)
            String stateProvince,

            @NotBlank @Size(max = 20)
            String postalCode,

            @NotBlank @Pattern(regexp = "[A-Z]{2}")
            String countryCode
    ) {
    }

    // Records print every field in toString(). Mask the sensitive ones so a
    // logged request or exception message can never contain them.
    @Override
    public String toString() {
        return "KycSubmissionRequest[residencyStatus=" + residencyStatus
                + ", tinType=" + tinType + ", tin=*********, idNumber=*****]";
    }
}
