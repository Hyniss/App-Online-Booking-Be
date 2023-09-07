package com.fpt.h2s.services.commands.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Date;

@Builder
@Getter
@AllArgsConstructor
public class DetailAccommodationSetBookingTimesResponse {
    private Date bookingDate;
    private Integer totalRoomsRemaining;
}
