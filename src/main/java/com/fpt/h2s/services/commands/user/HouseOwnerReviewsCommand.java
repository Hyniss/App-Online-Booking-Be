package com.fpt.h2s.services.commands.user;

import ananta.utility.SetEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.domains.SearchRequest;
import com.fpt.h2s.models.entities.AccommodationReview;
import com.fpt.h2s.repositories.AccommodationReviewRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.UserResponse;
import com.fpt.h2s.utilities.Mappers;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class HouseOwnerReviewsCommand implements BaseCommand<HouseOwnerReviewsCommand.Request, ListResult<HouseOwnerReviewsCommand.Response>> {

    private final AccommodationReviewRepository accommodationReviewRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<ListResult<Response>> execute(Request request) {
        Page<AccommodationReview> page = accommodationReviewRepository.findAllByOwnerId(request.userId, request.toPageRequest());

        Set<Integer> ownerIds = SetEx.setOf(page.getContent(), AccommodationReview::getUserId);
        Map<Integer, UserResponse> owners = userRepository.findAllByIdsIn(ownerIds, id -> id, UserResponse::of);

        return ApiResponse.success(ListResult.of(page).map(accommodation -> Mappers.convertTo(Response.class, accommodation).withOwner(owners.get(accommodation.getUserId()))));
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

        private Integer accommodationId;
        private String content;
        private Integer rate;
        private Timestamp createdAt;
        private UserResponse owner;
    }

}
