package com.fpt.h2s.models.entities;

import ananta.utility.StreamEx;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.*;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@With
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ToString
/* Jpa */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "booking_requests")
@EqualsAndHashCode(of = "id")
@EntityListeners({CreatorIdListener.class})
@BatchSize(size = 50)
public class BookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private Integer totalRooms;

    private String contact;

    private String contactName;

    private String note;

    private Integer accommodationId;

    @Enumerated(EnumType.STRING)
    private BookingRequest.Status status;

    private Integer transactionId;

    private Timestamp checkinAt;

    private Timestamp checkoutAt;

    private Integer creatorId;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    private Timestamp deletedAt;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false, name = Fields.userId)
    private User user;

    //    @JsonIgnore
//    @ToString.Exclude
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(insertable = false, updatable = false, name = Fields.roomId)
    @Transient
    private Room room;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false, name = Fields.accommodationId)
    private Accommodation accommodation;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(insertable = false, updatable = false, name = Fields.transactionId)
    private Transaction transaction;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = BookingRequestService.Fields.bookingRequestId, insertable = false, updatable = false)
    private Set<BookingRequestService> details;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = BookingRequestDetail.Fields.bookingRequestId, insertable = false, updatable = false)
    private Set<BookingRequestDetail> bookingRequestDetails;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = TravelStatement.Fields.bookingRequestId, insertable = false, updatable = false)
    private List<TravelStatement> travelStatements;
    public boolean isBookedBy(Integer userId) {
        return Objects.equals(getUserId(), userId);
    }

    public boolean isCreatedBy(Integer userId) {
        return getCreatorId().equals(userId);
    }

    public TravelStatement getTravelStatement() {
        return StreamEx.from(this.getTravelStatements()).findFirst().orElse(null);
    }

    public boolean is(final BookingRequest.Status status) {
        return this.status == status;
    }

    public enum Status {
        PENDING, PURCHASED, UN_PURCHASED, CANCELED, SUCCEED;

        @Component
        public static class Converter extends JPAEnumConverter<BookingRequest.Status> {
        }
    }
}
