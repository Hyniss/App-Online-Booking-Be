package com.fpt.h2s.models.entities;

import com.fpt.h2s.utilities.MoreStrings;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.sql.Timestamp;
import java.time.Instant;

@With
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ToString
/* Jpa */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "otps")
public class OtpDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String otpKey;
    private String value;
    private Integer totalTried;
    private String ipAddress;
    private Timestamp expiredAt;
    private Timestamp verifiedAt;

    @CreationTimestamp
    private Timestamp createdAt;
    public boolean isVerified() {
        return this.getVerifiedAt() != null;
    }

    public OtpDetail markVerified() {
        return this.withVerifiedAt(Timestamp.from(Instant.now()));
    }

    public boolean isExpired() {
        return this.expiredAt.before(Timestamp.from(Instant.now()));
    }

    public static String generate() {
        return MoreStrings.randomOTP(6);
    }

}
