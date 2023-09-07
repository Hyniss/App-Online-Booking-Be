package com.fpt.h2s.services.commands.room;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.RoomImageRepository;
import com.fpt.h2s.repositories.RoomPropertyRepository;
import com.fpt.h2s.repositories.RoomRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateRoomImageRequest;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateRoomPropertyRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class HouseOwnerUpdateRoomCommand
        implements BaseCommand<HouseOwnerUpdateRoomCommand.Request, Void> {

    private final RoomRepository roomRepository;
    private final AccommodationRepository accommodationRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomPropertyRepository roomPropertyRepository;

    @Override
    public ApiResponse<Void> execute(final HouseOwnerUpdateRoomCommand.Request request) {

        final Room originRoom = this.roomRepository.findById(request.getId())
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy phòng với id = {}.", request.getId()));

        final Room updatedRoom = originRoom.toBuilder()
                .name(request.getName())
                .status(request.getStatus())
                .totalRooms(request.getCount())
                .amenities(request.amenities.stream()
                .map(amenity -> Category.builder()
                        .id(amenity)
                        .build())
                .collect(Collectors.toSet()))
                .build();
        this.saveUpdatedAccommodation(request, originRoom);
        this.roomRepository.save(updatedRoom);
        this.updatedRoomImageAndProperty(request, originRoom);
        return ApiResponse.success("Cập nhật thông tin phòng thành công.");
    }

    private void updatedRoomImageAndProperty(final HouseOwnerUpdateRoomCommand.Request request,
                                             final Room originRoom) {
        if(request.getIsUpdateImage()) {
            final Set<RoomImage> updatedImages = request.toImages();
            this.roomImageRepository.deleteAll(originRoom.getImages());
            this.roomImageRepository.saveAll(updatedImages);
        }
        if(request.getIsUpdateProperty()) {
            final Set<RoomProperty> updatedProperties = request.toProperties();
            this.roomPropertyRepository.saveAll(updatedProperties);
        }}

    private void saveUpdatedAccommodation(final Request request, Room originRoom) {
        final Accommodation originAccommodation = this.accommodationRepository
                .findById(request.getAccommodationId())
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", request.getAccommodationId()));

        final Accommodation accommodation = request.toAccommodation(originAccommodation, originRoom)
                .orElseThrow(() -> ApiException.badRequest("Có gì đó không ổn. Xin vui lòng kiểm tra lại thông tin."));
        this.accommodationRepository.save(accommodation);
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        @NonNull
        private Integer id;

        @NonNull
        private Integer accommodationId;

        @NonNull
        private String name;

        @NonNull
        private Room.Status status;

        @NonNull
        private Integer count;

        @NonNull
        private Boolean isUpdateImage;

        @NotEmpty
        private Set<HouseOwnerCreateUpdateRoomImageRequest> images;

        @NonNull
        private Boolean isUpdateProperty;

        @NotEmpty
        private Set<HouseOwnerCreateUpdateRoomPropertyRequest> properties;

        @NotEmpty
        private Set<Integer> amenities;

        public Set<RoomImage> toImages() {
            return this.images.stream()
                    .map(image -> RoomImage
                            .builder()
                            .roomId(this.getId())
                            .name(image.getName())
                            .image(image.getUrl())
                            .build())
                    .collect(Collectors.toSet());
        }

        public Set<RoomProperty> toProperties() {
            return this.properties.stream()
                    .map(property -> RoomProperty
                            .builder()
                            .id(new RoomProperty
                                    .PK(this.getId(),
                                    property.getKeyId()))
                            .value(property.getValue().toString())
                            .build())
                    .collect(Collectors.toSet());
        }

        public Optional<Accommodation> toAccommodation(final Accommodation originAccommodation, Room originRoom) {
            Integer updatedTotalRooms;
            if(this.count >= originRoom.getTotalRooms()) {
                updatedTotalRooms = this.count - originRoom.getTotalRooms();
            } else {
                return Optional.ofNullable(originAccommodation
                        .toBuilder()
                        .totalRoom(originAccommodation.getTotalRoom() - (originRoom.getTotalRooms() - this.count))
                        .build());
            }
            return Optional.ofNullable(originAccommodation
                    .toBuilder()
                    .totalRoom(originAccommodation.getTotalRoom() + updatedTotalRooms)
                    .build());
        }

    }
}
