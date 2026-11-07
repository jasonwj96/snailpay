package com.jasonwj.snailpay.models;

import com.jasonwj.snailpay.enums.AddressType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

// R2DBC version. The CHAR(2) country code maps to a plain String here.
// If your AddressType enum is still in another package, fix the import above.
@Table("customer_addresses")
@Getter
@Setter
@NoArgsConstructor
public class CustomerAddress {

    @Id
    private UUID id;

    @Column("customer_id")
    private UUID customerId;

    @Column("address_type")
    private AddressType addressType;

    private String line1;

    private String line2;

    private String city;

    @Column("state_province")
    private String stateProvince;

    @Column("postal_code")
    private String postalCode;

    @Column("country_code")
    private String countryCode = "US";

    // Named "current", not "isCurrent": Spring Data would otherwise derive a
    // confusing property name from the Lombok accessors.
    @Column("is_current")
    private boolean current = true;

    @Column("superseded_at")
    private Instant supersededAt;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;
}
