package com.fpt.h2s.services.commands.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.BaseRequest;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.sql.Timestamp;


@Getter
@Builder
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class DetailAccommodationFilterRequest extends BaseRequest {
    
    @NonNull
    private Integer id;

    private Timestamp checkIn;

    private Timestamp checkOut;

    @NonNull
    private Integer totalRoomBook;

    @NonNull
    private Integer numberOfBeds;

    @NonNull
    private Integer numberOfAdults;

    @NonNull
    private Integer numberOfChildren;

    @NonNull
    private Integer numberOfPets;

}

