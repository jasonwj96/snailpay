package com.jasonwj.snailpay.dto;

import com.jasonwj.snailpay.enums.KycDocumentStatus;
import com.jasonwj.snailpay.enums.KycDocumentType;
import com.jasonwj.snailpay.models.KycDocument;

import java.util.UUID;

public record KycDocumentResponse(
        UUID documentId,
        KycDocumentType documentType,
        KycDocumentStatus status
) {

    public static KycDocumentResponse from(KycDocument document) {
        return new KycDocumentResponse(document.getId(), document.getDocumentType(), document.getStatus());
    }
}
