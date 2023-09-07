package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.Category;
import com.fpt.h2s.models.entities.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
@AllArgsConstructor
public class HouseOwnerDetailAccommodationRoomResponse {

    private Integer id;

    private String name;

    private Room.Status status;

    private Long price;

    private Integer totalRoomType;

    private Set<DetailAccommodationSetImageResponse> roomImages;

    private Set<DetailAccommodationSetPropertiesResponse> properties;

    private Set<DetailAccommodationSetBookingTimesResponse> bookings;

    private Set<Category> amenities;

    public static HouseOwnerDetailAccommodationRoomResponse of(Room room,
                                                               Set<DetailAccommodationSetImageResponse> roomImages,
                                                               Set<DetailAccommodationSetPropertiesResponse> properties,
                                                               Set<DetailAccommodationSetBookingTimesResponse> bookings,
                                                               Set<Category> amenities) {
        return HouseOwnerDetailAccommodationRoomResponse
                .builder()
                .id(room.getId())
                .name(room.getName())
                .status(room.getStatus())
                .price(room.getPrice())
                .totalRoomType(room.getTotalRooms())
                .roomImages(roomImages)
                .properties(properties)
                .bookings(bookings)
                .amenities(amenities)
                .build();
    }
}
