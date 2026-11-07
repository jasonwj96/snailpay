package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.enums.ConsentType;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Appends rows to consent_records. Uses SQL directly because ip_address is an
 * INET column: R2DBC would send a String as VARCHAR and Postgres would reject
 * it, so the value is cast in the statement instead.
 */
@Component
@RequiredArgsConstructor
public class ConsentRecorder {

    private static final Pattern IP_CHARS = Pattern.compile("^[0-9a-fA-F:.]{2,45}$");
    private static final int MAX_USER_AGENT = 512;

    private final DatabaseClient databaseClient;

    public Mono<Void> record(UUID customerId, ConsentType type, String documentVersion,
                             String forwardedFor, String userAgent) {
        return databaseClient.sql("""
                        INSERT INTO consent_records
                            (customer_id, consent_type, document_version, accepted_at, ip_address, user_agent)
                        VALUES
                            (:customerId, :type, :version, now(),
                             CAST(NULLIF(:ip, '') AS inet), NULLIF(:userAgent, ''))
                        """)
                .bind("customerId", customerId)
                .bind("type", type.name())
                .bind("version", documentVersion)
                .bind("ip", sanitizeIp(forwardedFor))
                .bind("userAgent", truncate(userAgent))
                .then();
    }

    // X-Forwarded-For is client-supplied unless your gateway overwrites it, so
    // treat it as a hint: keep only the first address and only if it looks like one.
    private static String sanitizeIp(String forwardedFor) {
        if (forwardedFor == null) {
            return "";
        }
        String first = forwardedFor.split(",")[0].trim();
        return IP_CHARS.matcher(first).matches() ? first : "";
    }

    private static String truncate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > MAX_USER_AGENT ? value.substring(0, MAX_USER_AGENT) : value;
    }
}
