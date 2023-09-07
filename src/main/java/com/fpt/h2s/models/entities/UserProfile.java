package com.fpt.h2s.models.entities;

import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

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
@Table(name = "user_profiles")
@EntityListeners({CreatorIdListener.class})
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userId;
    private String avatar;
    private String address;
    private String bio;
    private String identification;
    private String card;
    @Convert(converter = UserProfile.BankCode.Converter.class)
    private BankCode bankCode;
    private String accountNo;
    private String accountName;
    @Convert(converter = UserProfile.AccountType.Converter.class)
    private AccountType accountType;
    private Integer totalReview;
    private Double totalRate;
    private Integer creatorId;
    @CreationTimestamp
    private Timestamp createdAt;
    
    @UpdateTimestamp
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public enum AccountType {
        Account, Card;

        @Component
        public static class Converter extends JPAEnumConverter<AccountType> {
        }
    }

    public enum BankCode {
        VCB, TECHCOMBANK, TPBANK, VIETINBANK, DAB, VIB, MB, HDBANK, MARITIMEBANK, VIETABANK,
        EXIMBANK, SHB, ABBANK, VPBANK, NAMABANK, SACOMBANK, BIDV, OCEANBANK,
        BACA, SEABANK, AGRIBANK, PVBANK, NCB, LPB, OCB, SAIGONBANK, ACB, BVBANK,
        VRB, PGBANK, KIENLONGBANK, SCB, PBVN, VIETCAPITALBANK, GPBANK, WOORIBANK,
        UOB, SHINHAN, COOPBANK, HONGLEONG, IVB, IBK, VIETBANK
        ;

        @Component
        public static class Converter extends JPAEnumConverter<BankCode> {
        }
    }
}
