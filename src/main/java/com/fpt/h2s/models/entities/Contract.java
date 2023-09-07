package com.fpt.h2s.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Setter
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
@Table(name = "contracts")
@EntityListeners({CreatorIdListener.class})
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Integer accommodationId;

    private String content;

    private Integer profit;
    @Enumerated(EnumType.STRING)
    private Status status;

    private Integer acceptedBy;

    private Timestamp expiredAt;

    private Integer creatorId;
    
    @CreationTimestamp
    private Timestamp createdAt;
    
    @UpdateTimestamp
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = Fields.acceptedBy, insertable = false, updatable = false)
    private User user;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = Fields.creatorId, insertable = false, updatable = false)
    private User houseOwner;

    public boolean is(final Contract.Status status) {
        return this.status == status;
    }

    public enum Status {
        PENDING, REJECTED, APPROVED, TERMINATED;

        @Component
        public static class Converter extends JPAEnumConverter<Contract.Status> {
        }
    }
}
