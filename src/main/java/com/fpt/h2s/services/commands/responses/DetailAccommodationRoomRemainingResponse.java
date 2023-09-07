package com.fpt.h2s.services.commands.responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
@AllArgsConstructor
public class DetailAccommodationRoomRemainingResponse {
    private Timestamp fromDate;
    private Timestamp toDate;
    private Integer totalRoomsRemaining;

    public static DetailAccommodationRoomRemainingResponse of(Object totalRoomsRemaining,
                                                              Object fromDate,
                                                              Object toDate
                                                              ) {
        return DetailAccommodationRoomRemainingResponse.builder()
                .fromDate(Timestamp.valueOf(fromDate.toString()))
                .toDate(Timestamp.valueOf(toDate.toString()))
                .totalRoomsRemaining(Integer.parseInt(totalRoomsRemaining.toString()))
                .build();
    }


}
