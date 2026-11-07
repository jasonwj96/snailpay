package com.jasonwj.snailpay.models;

import com.jasonwj.snailpay.enums.IdDocumentType;
import com.jasonwj.snailpay.enums.ResidencyStatus;
import com.jasonwj.snailpay.enums.TinType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// No @ToString/@Data on purpose: nothing here should ever end up in a log line.
@Table("kyc_profiles")
@Getter
@Setter
@NoArgsConstructor
public class KycProfile {

    @Id
    private UUID id;

    @Column("customer_id")
    private UUID customerId;

    @Column("residency_status")
    private ResidencyStatus residencyStatus;

    @Column("citizenship_country")
    private String citizenshipCountry;

    @Column("tin_type")
    private TinType tinType;

    @Column("tin_ciphertext")
    private byte[] tinCiphertext;

    @Column("tin_last4")
    private String tinLast4;

    @Column("tin_blind_index")
    private String tinBlindIndex;

    @Column("id_document_type")
    private IdDocumentType idDocumentType;

    @Column("id_number_ciphertext")
    private byte[] idNumberCiphertext;

    @Column("id_issuing_country")
    private String idIssuingCountry;

    @Column("id_issuing_state")
    private String idIssuingState;

    @Column("id_expiration_date")
    private LocalDate idExpirationDate;

    @Column("submitted_at")
    private Instant submittedAt;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
}
