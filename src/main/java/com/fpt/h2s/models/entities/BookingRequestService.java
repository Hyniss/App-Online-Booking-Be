package com.fpt.h2s.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
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
@Table(name = "booking_requests_services")
@EqualsAndHashCode(of = "id")
@EntityListeners({CreatorIdListener.class})
public class BookingRequestService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer bookingRequestId;

    private Integer keyId;

    private String value;

    @JsonIgnore
    @ToString.Exclude
    //@EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Fields.keyId, insertable = false, updatable = false)
    private Property property;

}
