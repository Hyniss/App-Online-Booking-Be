package com.fpt.h2s.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
@Table(name = "booking_requests_details")
@EqualsAndHashCode(of = "id")
public class BookingRequestDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer bookingRequestId;

    private Integer roomId;

    private Long price;

    private Long originalPrice;

    private Integer totalRooms;

    @JsonIgnore
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Fields.roomId, insertable = false, updatable = false)
    private Room room;
}
