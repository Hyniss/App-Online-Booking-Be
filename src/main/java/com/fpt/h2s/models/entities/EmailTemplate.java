package com.fpt.h2s.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ToString
/* Jpa */
@Entity
@Table(name = "email_templates")
public class EmailTemplate {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Key key;

    @Column(nullable = false, unique = true)
    private String subject;

    @Column(nullable = false, unique = true)
    private String content;

    public enum Key {
        EMAIL_VERIFICATION,
        UPDATE_EMAIL_VERIFICATION,
        WELCOME_MAIL,
        RESET_PASSWORD,
        CHANGE_CONTRACT_STATUS,
        APPROVE_OR_REJECT_BOOKING_REQUEST,
        REGISTER_TO_HOUSE_OWNER,
        BUSINESS_EMAIL_VERIFICATION,
        ADMIN_ACCEPT_COMPANY_REQUEST,
        ADMIN_SUSPEND_COMPANY_REQUEST,
        ADMIN_REJECT_COMPANY_REQUEST,
        ADMIN_BAN_COMPANY_REQUEST,
        ADMIN_UNBAN_COMPANY_REQUEST,
        CHANGE_TRAVEL_STATEMENT_STATUS,
        BUSINESS_CANCEL_BOOKING_REQUEST,
    }

}
