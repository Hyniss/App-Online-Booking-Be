package com.fpt.h2s.services.commands.room;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Property;
import com.fpt.h2s.models.entities.Room;
import com.fpt.h2s.repositories.RoomPropertyRepository;
import com.fpt.h2s.repositories.RoomRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.HouseOwnerViewDetailRoomRequest;
import com.fpt.h2s.services.commands.responses.DetailAccommodationSetImageResponse;
import com.fpt.h2s.services.commands.responses.DetailAccommodationSetPropertiesResponse;
import com.fpt.h2s.services.commands.responses.HouseOwnerDetailAccommodationRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseOwnerViewDetailRoomCommand implements
        BaseCommand<HouseOwnerViewDetailRoomRequest, HouseOwnerDetailAccommodationRoomResponse> {

    private final RoomRepository roomRepository;
    private final RoomPropertyRepository roomPropertyRepository;

    @Override
    public ApiResponse<HouseOwnerDetailAccommodationRoomResponse> execute(final HouseOwnerViewDetailRoomRequest request) {
        final HouseOwnerDetailAccommodationRoomResponse response = this.getRoomDetail(request.getId(),
                request.getAccommodationId());
        return ApiResponse.success(response);
    }

    private HouseOwnerDetailAccommodationRoomResponse getRoomDetail(final Integer id, final Integer accommodationId) {
        final Room room = this.roomRepository.findByIdAndAccommodationId(id,accommodationId).orElseThrow();
        return HouseOwnerDetailAccommodationRoomResponse.of(
                room,
                getImages(room),
                getProperties(room),
                null,
                room.getAmenities()
        );
    }

    private Set<DetailAccommodationSetPropertiesResponse> getProperties(final Room room) {
        final Set<Object[]> results = this.roomPropertyRepository.findJoinedDataByRoomId(room.getId());
        return results.stream()
                .map(result -> {
                    Integer keyId = Integer.parseInt(result[0].toString());
                    String key  = result[1].toString();
                    Property.Type type =  Property.Type.valueOf(result[2].toString());
                    String value = result[3].toString();
                    return new DetailAccommodationSetPropertiesResponse(keyId, key, type, value);
                })
                .collect(Collectors.toSet());
    }

    private Set<DetailAccommodationSetImageResponse> getImages(final Room room) {
        return room.getImages()
                .stream()
                .map(image -> new DetailAccommodationSetImageResponse(
                        image.getId(),
                        image.getRoomId(),
                        image.getName(),
                        image.getImage()
                ))
                .collect(Collectors.toSet());
    }
}
