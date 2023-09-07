package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.Accommodation;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HouseOwnerCreateAccommodationResponse {
    private Integer accommodationId;
    public static HouseOwnerCreateAccommodationResponse of(Accommodation accommodation) {
        return HouseOwnerCreateAccommodationResponse
                .builder()
                .accommodationId(accommodation.getId())
                .build();
    }
}
