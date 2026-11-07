package com.jasonwj.snailpay.dto;

import com.jasonwj.snailpay.enums.KycDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UploadUrlRequest(

        @NotNull
        KycDocumentType documentType,

        @NotBlank
        String contentType,

        @Positive
        long sizeBytes
) {
}
