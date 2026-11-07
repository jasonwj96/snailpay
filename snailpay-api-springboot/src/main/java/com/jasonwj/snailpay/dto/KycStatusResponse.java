package com.jasonwj.snailpay.dto;

import com.jasonwj.snailpay.enums.KycStatus;

import java.util.List;

public record KycStatusResponse(
        KycStatus kycStatus,
        List<KycDocumentResponse> documents
) {
}
