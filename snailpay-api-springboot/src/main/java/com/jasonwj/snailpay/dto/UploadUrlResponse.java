package com.jasonwj.snailpay.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * The browser PUTs the file straight to uploadUrl with exactly these headers.
 * The file never passes through the API server.
 */
public record UploadUrlResponse(
        UUID documentId,
        String uploadUrl,
        Map<String, String> headers,
        Instant expiresAt
) {
}
