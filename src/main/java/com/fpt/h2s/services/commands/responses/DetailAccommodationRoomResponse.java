package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;


@Builder
@Getter
@AllArgsConstructor
public class DetailAccommodationRoomResponse {

    private Integer id;

    private String name;

    private Room.Status status;

    private Long price;

    private Integer totalRooms;

    private Set<DetailAccommodationSetImageResponse> roomImages;

    private Set<DetailAccommodationSetPropertiesResponse> properties;

    private DetailAccommodationRoomRemainingResponse roomRemaining;

    private String alternativeBookingDay;


    public static DetailAccommodationRoomResponse of(Room room,
                                                     Set<DetailAccommodationSetImageResponse> roomImages,
                                                     Set<DetailAccommodationSetPropertiesResponse> properties
    ) {
        return DetailAccommodationRoomResponse
                .builder()
                .id(room.getId())
                .name(room.getName())
                .status(room.getStatus())
                .price(room.getPrice())
                .totalRooms(room.getTotalRooms())
                .roomImages(roomImages)
                .properties(properties)
                .build();
    }
}
