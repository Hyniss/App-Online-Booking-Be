package com.fpt.h2s.services.commands.accommodation;

import ananta.utility.ListEx;
import ananta.utility.SetEx;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.domains.SearchRequest;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.AccommodationReview;
import com.fpt.h2s.models.entities.ReviewImage;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.*;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.accommodation.CheckAccommodationReviewableCommand.BookingDetailResponse;
import com.fpt.h2s.services.commands.accommodation.CheckAccommodationReviewableCommand.BookingRoomProjection;
import com.fpt.h2s.services.commands.responses.ReviewResponse;
import com.fpt.h2s.services.commands.responses.UserResponse;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewDetailAccommodationReviewCommand implements BaseCommand<ViewDetailAccommodationReviewCommand.Request, ListResult<ReviewResponse>> {

    private final AccommodationRepository accommodationRepository;
    private final ReviewImageRepository imageRepository;
    private final AccommodationReviewRepository accommodationReviewRepository;
    private final BookingRequestDetailRepository bookingRequestDetailRepository;

    @Override
    public ApiResponse<ListResult<ReviewResponse>> execute(final Request request) {
        final Accommodation accommodation = this.accommodationRepository
            .findById(request.id)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", request.id));

        if (accommodation.getStatus() != Accommodation.Status.OPENING) {
            throw ApiException.badRequest("Chỗ ở này hiện tại chưa được mở cho thuê phòng.");
        }

        Page<AccommodationReview> page = accommodationReviewRepository.findAllByAccommodationId(accommodation.getId(), request.toPageRequest());

        Set<Integer> reviewIds = SetEx.setOf(page.getContent(), AccommodationReview::getId);

        Map<Integer, List<BookingRoomProjection>> roomsMapToReviewId = bookingRequestDetailRepository
            .findAllByReviews(reviewIds)
            .stream()
            .collect(Collectors.groupingBy(BookingRoomProjection::getId));

        Map<Integer, List<ReviewImage>> imagesMapToReviewId = imageRepository.findAllByReviewIdIn(reviewIds).stream().collect(Collectors.groupingBy(ReviewImage::getReviewId));

        Integer loginUserId = User.currentUserId().orElse(null);

        final List<ReviewResponse> content = page.getContent()
            .stream()
            .map(review -> {
                List<BookingRoomProjection> rooms = roomsMapToReviewId.getOrDefault(review.getId(), Collections.emptyList());
                BookingDetailResponse detail = BookingDetailResponse.builder().roomNames(rooms.stream().map(BookingRoomProjection::getName).toList()).build();
                return ReviewResponse
                        .builder()
                        .id(review.getId())
                        .owner(UserResponse.of(review.getOwner()))
                        .content(review.getContent())
                        .rate(review.getRate())
                        .createdAt(review.getCreatedAt())
                        .images(ListEx.listOf(imagesMapToReviewId.get(review.getId()), ReviewImage::getImage))
                        .canEdit(review.getOwner().getId().equals(loginUserId))
                        .canDelete(review.getOwner().getId().equals(loginUserId))
                        .detail(detail)
                        .build();
                }
            )
            .collect(Collectors.toList());

        return ApiResponse.success(ListResult.of(page).withContent(content));
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends SearchRequest {

        @NotNull
        private Integer id;

        private String orderBy;
        private Boolean isDescending;
        private Integer size;
        private Integer page;
    }
}
