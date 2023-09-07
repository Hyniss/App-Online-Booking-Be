package com.fpt.h2s.services.commands.accommodation;

import ananta.utility.ListEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.*;
import com.fpt.h2s.services.NotificationService;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.ReviewResponse;
import com.fpt.h2s.services.commands.responses.UserResponse;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.fpt.h2s.services.commands.accommodation.CheckAccommodationReviewableCommand.*;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class CreateReviewCommand implements BaseCommand<CreateReviewCommand.Request, CreateReviewCommand.Response> {

    private final AccommodationReviewRepository accommodationReviewRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final AccommodationRepository accommodationRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final NotificationService notificationService;

    @Override
    public ApiResponse<Response> execute(final Request request) {
        BookingRequest bookingRequest = bookingRequestRepository.getById(request.requestId, "Không thể tìm thấy thông tin đặt phòng với id = {}.", request.requestId);
        Timestamp monthAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
        boolean isBookedMoreThanMonthAgo = bookingRequest.getUpdatedAt().before(monthAgo);
        if (isBookedMoreThanMonthAgo) {
            throw ApiException.badRequest("Bạn không thể đánh giá chỗ ở này bây giờ. Vui lòng tải lại trang để tải lại.");
        }

        Accommodation accommodation = accommodationRepository.getById(request.accommodationId, "Không thể tìm thấy chỗ ở với id = {}.", request.accommodationId);
        AccommodationReview review = accommodationReviewRepository.save(request.toReview());
        List<ReviewImage> images = ListEx.nonNullListOf(request.getImages()).stream().map(img -> ReviewImage.builder().image(img).reviewId(review.getId()).build()).toList();
        reviewImageRepository.saveAll(images);

        Accommodation updatedAccommodation = updateReviewCountFor(accommodation);
        updateAchievementOfOwnerOf(updatedAccommodation);

        notificationService.send(notification -> notification
            .toUser(updatedAccommodation.getOwnerId())
            .withContent("Nhà của bạn vừa được nhận đánh giá {} sao từ khách hàng", review.getRate())
        );

        Response response = Response
            .builder()
            .review(
                responseOf(review, bookingRequest)
                .withImages(images.stream().map(ReviewImage::getImage).toList())
                .withCanDelete(true)
                .withCanEdit(true)
            )
            .newRate(updatedAccommodation.getReviewRate())
            .newTotalReviews(updatedAccommodation.getTotalReviews())
            .build();

        return ApiResponse.success("Bình luận thành công.", response);
    }

    private void updateAchievementOfOwnerOf(Accommodation updatedAccommodation) {
        Integer creatorId = updatedAccommodation.getCreatorId();
        User accommodationOwner = userRepository.findById(creatorId).orElseThrow();

        List<Integer> rates = accommodationReviewRepository.findAllRatesOfUser(creatorId);
        double rate = round(rates.stream().mapToInt(x -> x).average().orElse(0));

        UserProfile profile = accommodationOwner.getUserProfile();
        if (profile != null) {
            UserProfile profileToUpdate = profile.withTotalRate(rate).withTotalReview(rates.size());
            userProfileRepository.save(profileToUpdate);
            return;
        }

        UserProfile newProfile = UserProfile
            .builder()
            .totalRate(rate)
            .totalReview(rates.size())
            .userId(accommodationOwner.getId())
            .build();

        userProfileRepository.save(newProfile);
    }

    @NotNull
    private static Float round(double newRate) {
        DecimalFormat df = new DecimalFormat("#.#");
        return Float.valueOf(df.format(newRate));
    }

    private Accommodation updateReviewCountFor(Accommodation accommodation) {
        Set<AccommodationReview> accommodationReviews = accommodation.getReviews();
        Float reviewRate = reviewRateOf(accommodationReviews);
        Accommodation accommodationToUpdate = accommodation.withReviewRate(reviewRate).withTotalReviews(accommodationReviews.size());
        return accommodationRepository.save(accommodationToUpdate);
    }

    @NotNull
    private static Float reviewRateOf(Set<AccommodationReview> accommodationReviews) {
        double newRate = accommodationReviews.stream().mapToDouble(AccommodationReview::getRate).average().getAsDouble();
        DecimalFormat df = new DecimalFormat("#.#");
        return Float.valueOf(df.format(newRate));
    }

    private ReviewResponse responseOf(AccommodationReview review, BookingRequest bookingRequest) {
        User owner = userRepository.getById(User.currentUserId().orElseThrow());
        List<String> roomNames = bookingRequest.getBookingRequestDetails().stream().map(x -> x.getRoom().getName()).toList();
        return ReviewResponse
            .builder()
            .id(review.getId())
            .detail(BookingDetailResponse.builder().roomNames(roomNames).build())
            .owner(UserResponse.of(owner))
            .content(review.getContent())
            .rate(review.getRate())
            .createdAt(review.getCreatedAt())
            .build();
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        @NonNull
        private Integer rate;

        @NonNull
        private Integer accommodationId;

        @NonNull
        private Integer requestId;

        @NonNull
        @Length(min = 20, max = 2048, message="Xin vui lòng để nội dung bình luận trong khoảng 20 đến 2048 ký tự.")
        private String content;

        private List<String> images;


        public AccommodationReview toReview() {
            Integer userId = User.currentUserId().orElseThrow();
            return AccommodationReview.builder()
                .userId(userId)
                .accommodationId(accommodationId)
                .requestId(requestId)
                .content(content.trim())
                .rate(rate)
                .build();
        }

    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Response {

        @NonNull
        private ReviewResponse review;

        private Float newRate;
        private Integer newTotalReviews;

    }

}
