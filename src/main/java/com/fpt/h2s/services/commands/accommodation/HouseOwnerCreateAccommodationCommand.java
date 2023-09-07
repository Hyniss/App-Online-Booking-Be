package com.fpt.h2s.services.commands.accommodation;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.AccommodationImage;
import com.fpt.h2s.models.entities.Category;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationImageRepository;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateAccommodationImageRequest;
import com.fpt.h2s.services.commands.responses.HouseOwnerCreateAccommodationResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class HouseOwnerCreateAccommodationCommand
        implements BaseCommand<HouseOwnerCreateAccommodationCommand.Request, HouseOwnerCreateAccommodationResponse> {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationImageRepository accommodationImageRepository;

    @Override
    public ApiResponse<HouseOwnerCreateAccommodationResponse> execute
            (final Request request) {
        Integer ownerId =  User.currentUserId()
                .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));
        final Accommodation accommodationToSave = request.toAccommodation(ownerId);
        final Accommodation savedAccommodation = this.accommodationRepository.save(accommodationToSave);
        final Set<AccommodationImage> accommodationImagesToSave = request.toImage(savedAccommodation);
        this.accommodationImageRepository.saveAll(accommodationImagesToSave);
        return ApiResponse.success(HouseOwnerCreateAccommodationResponse.of(savedAccommodation));
    }


    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        @NonNull
        private String name;

        @NonNull
        private String thumbnail;

        @NonNull
        private String shortDescription;

        @NonNull
        private String description;

        @NonNull
        private String address;

        @NonNull
        private Double latitude;

        @NonNull
        private Double longitude;

        @NonNull
        private Accommodation.Type type;

        @NotEmpty
        private Set<HouseOwnerCreateUpdateAccommodationImageRequest> image;

        @NotEmpty
        private Set<Integer> location;

        public Accommodation toAccommodation(final Integer ownerId) {
            return Accommodation.builder()
                    .name(this.name)
                    .ownerId(ownerId)
                    .thumbnail(this.thumbnail)
                    .shortDescription(this.shortDescription)
                    .description(this.description)
                    .address(this.address)
                    .latitude(this.latitude)
                    .longitude(this.longitude)
                    .minPrice(0L)
                    .maxPrice(0L)
                    .type(this.type)
                    .status(Accommodation.Status.PENDING)
                    .totalRoom(0)
                    .categories(location.stream()
                            .map(location -> Category.builder()
                                    .id(location)
                                    .build())
                            .collect(Collectors.toSet()))
                    .build();
        }

        public Set<AccommodationImage> toImage(final Accommodation savedAccommodation) {
            return this.image
                    .stream()
                    .map(accommodationImageRequest -> AccommodationImage
                            .builder()
                            .accommodationId(savedAccommodation.getId())
                            .name(accommodationImageRequest.getName())
                            .image(accommodationImageRequest.getUrl())
                            .build())
                    .collect(Collectors.toSet());
        }
    }
}
