package com.fpt.h2s.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@With
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ToString
@Builder(toBuilder = true)
/* Jpa */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "travel_statements")
@EntityListeners({CreatorIdListener.class})
public class TravelStatement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Enumerated(EnumType.STRING)
    private TravelStatement.Status status;

    private  Integer numberOfPeople;

    private  String location;

    private String note;

    private Integer bookingRequestId;

    private Timestamp fromDate;

    private Timestamp toDate;

    private Integer creatorId;

    private Timestamp approvedAt;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    private Timestamp deletedAt;

    private String rejectMessage;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = Fields.bookingRequestId, insertable = false, updatable = false)
    private BookingRequest bookingRequest;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Fields.creatorId, insertable = false, updatable = false)
    private User user;

    public boolean is(final TravelStatement.Status status) {
        return this.status == status;
    }

    public enum Status {
        PENDING, REJECTED, APPROVED, CANCELED;

        @Component
        public static class Converter extends JPAEnumConverter<TravelStatement.Status> {
        }
    }
}
