package com.fpt.h2s.models.entities;

import ananta.utility.StreamEx;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.List;

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
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Long amount;

    private String paymentMethod;

    private String transactionRequestNo;
    private String bankTransactionNo;

    private String payDate;

    private Integer creatorId;
    private Integer receiverId;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = BookingRequest.Fields.transactionId, insertable = false, updatable = false)
    private List<BookingRequest> bookingRequests;

    public BookingRequest getBookingRequest() {
        return StreamEx.from(this.getBookingRequests()).findFirst().orElse(null);
    }

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false, name = Fields.creatorId)
    private User creator;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false, name = Fields.receiverId)
    private User receiver;

}
