package com.fpt.h2s.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
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
@EntityListeners({CreatorIdListener.class})
@BatchSize(size = 50)
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer ownerId;
    private String name;
    private String shortName;
    private String quotaCode;

    @Convert(converter = Size.Converter.class)
    private Size size;

    @Convert(converter = Status.Converter.class)
    private Status status;

    private String address;
    private String contact;
    private String contactName;
    private int discount;

    private int totalRejected;
    private String rejectMessage;

    private Integer creatorId;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Fields.ownerId, insertable = false, updatable = false)
    private User owner;

    @Getter
    @AllArgsConstructor
    public enum Size {
        MICRO(1, 10),
        SMALL(10, 49),
        MEDIUM(50, 249),
        BIG(250, null);

        private final Integer from;
        private final Integer to;

        @Component
        public static class Converter extends JPAEnumConverter<Size> {
        }
    }

    public enum Status {
        ACTIVE,
        PENDING_CHANGE,
        PENDING,
        REJECTED,
        INACTIVE;

        @Component
        public static class Converter extends JPAEnumConverter<Status> {
        }
    }
}
