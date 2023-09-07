package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Room;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.HouseOwnerViewDetailAccommodationRequest;
import com.fpt.h2s.services.commands.responses.DetailAccommodationSetImageResponse;
import com.fpt.h2s.services.commands.responses.HouseOwnerDetailAccommodationResponse;
import com.fpt.h2s.services.commands.responses.HouseOwnerDetailAccommodationRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseOwnerViewDetailAccommodationCommand implements
        BaseCommand<HouseOwnerViewDetailAccommodationRequest, HouseOwnerDetailAccommodationResponse> {

    private final AccommodationRepository accommodationRepository;

    @Override
    public ApiResponse<HouseOwnerDetailAccommodationResponse> execute(final HouseOwnerViewDetailAccommodationRequest request) {
        final HouseOwnerDetailAccommodationResponse response = this.getAccommodationDetail(request.getId());
        return ApiResponse.success(response);
    }

    private HouseOwnerDetailAccommodationResponse getAccommodationDetail(final Integer id) {
        final var accommodation = this.accommodationRepository.findById(id).orElseThrow();
        return HouseOwnerDetailAccommodationResponse.of(
                accommodation,
                getImages(accommodation),
                accommodation.getCategories(),
                getRooms(accommodation)
        );
    }

    private Set<DetailAccommodationSetImageResponse> getImages(final Accommodation accommodation) {
        return accommodation.getImages()
                .stream()
                .map(image -> new DetailAccommodationSetImageResponse(
                        image.getId(),
                        image.getAccommodationId(),
                        image.getName(),
                        image.getImage()
                ))
                .collect(Collectors.toSet());
    }

    private Set<HouseOwnerDetailAccommodationRoomResponse> getRooms(final Accommodation accommodation) {
        return accommodation.getRooms()
                .stream()
                .map(room -> new HouseOwnerDetailAccommodationRoomResponse(
                        room.getId(),
                        room.getName(),
                        room.getStatus(),
                        room.getPrice(),
                        room.getTotalRooms(),
                        getImages(room),
                        null,
                        null,
                        null
                ))
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
