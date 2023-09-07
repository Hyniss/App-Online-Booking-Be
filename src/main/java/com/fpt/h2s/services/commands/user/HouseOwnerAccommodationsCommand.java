package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.domains.SearchRequest;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.utilities.Mappers;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class HouseOwnerAccommodationsCommand implements BaseCommand<HouseOwnerAccommodationsCommand.Request, ListResult<HouseOwnerAccommodationsCommand.Response>> {

    private final AccommodationRepository accommodationRepository;

    @Override
    public ApiResponse<ListResult<Response>> execute(Request request) {
        Page<Accommodation> page = accommodationRepository.findAllByOwnerIdAndStatus(request.userId, Accommodation.Status.OPENING, request.toPageRequest());
        return ApiResponse.success(ListResult.of(page).map(accommodation -> Mappers.convertTo(Response.class, accommodation)));
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @Jacksonized
    public static class Request extends SearchRequest {
        private final Integer size;

        private final Integer page;

        private final String orderBy;

        private final Boolean isDescending;

        @NotNull
        private final Integer userId;

    }

    @Getter
    @Builder
    @With
    @FieldNameConstants
    @Jacksonized
    public static class Response {

        private Integer id;

        private String name;

        private String thumbnail;

        private Integer totalReviews;

        private Float reviewRate;

        private Long minPrice;

        private Long maxPrice;

        private Double latitude;

        private Double longitude;

        private String address;
    }

}
