package com.fpt.h2s.services.commands.accommodation;

import ananta.utility.ListEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.*;
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

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateReviewCommand implements BaseCommand<UpdateReviewCommand.Request, UpdateReviewCommand.Response> {

    private final AccommodationReviewRepository accommodationReviewRepository;
    private final UserRepository userRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserProfileRepository userProfileRepository;

    private final BookingRequestRepository bookingRequestRepository;
    @Override
    public ApiResponse<Response> execute(final Request request) {
        AccommodationReview review = accommodationReviewRepository.getById(request.id, "Không thể tìm thấy đánh giá với id = {}.", request.id);

        Timestamp weekAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(7));
        boolean isReviewedMoreThanAWeek = review.getCreatedAt().before(weekAgo);

        if (isReviewedMoreThanAWeek) {
            throw ApiException.badRequest("Bạn không thể đánh giá chỗ ở này bây giờ. Vui lòng tải lại trang để tải lại đánh giá.");
        }

        AccommodationReview updatedReview = accommodationReviewRepository.save(review.withContent(request.content).withRate(request.rate));
        List<ReviewImage> images = updateImages(request, review);
        Accommodation updatedAccommodation = updateReviewCountFor(review.getAccommodation());
        updateAchievementOfOwnerOf(updatedAccommodation);

        Integer requestId = review.getRequestId();
        BookingRequest bookingRequest = bookingRequestRepository.getById(requestId);
        List<String> roomNames = bookingRequest.getBookingRequestDetails().stream().map(x -> x.getRoom().getName()).toList();

        Response response = Response
            .builder()
            .review(
                responseOf(updatedReview)
                .withImages(images.stream().map(ReviewImage::getImage).toList())
                .withCanDelete(true)
                .withCanEdit(true)
                .withDetail(CheckAccommodationReviewableCommand.BookingDetailResponse.builder().roomNames(roomNames).build())
            )
            .newRate(updatedAccommodation.getReviewRate())
            .build();

        return ApiResponse.success(response);
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

    private List<ReviewImage> updateImages(Request request, AccommodationReview review) {
        List<ReviewImage> oldImages = reviewImageRepository.findAllByReviewId(review.getId());
        reviewImageRepository.deleteAll(oldImages);

        List<ReviewImage> images = ListEx.nonNullListOf(request.getImages()).stream().map(img -> ReviewImage.builder().image(img).reviewId(review.getId()).build()).toList();
        reviewImageRepository.saveAll(images);
        return images;
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
        return round(newRate);
    }

    @NotNull
    private static Float round(double newRate) {
        DecimalFormat df = new DecimalFormat("#.#");
        return Float.valueOf(df.format(newRate));
    }

    private ReviewResponse responseOf(AccommodationReview review) {
        User owner = userRepository.getById(User.currentUserId().orElseThrow());
        return ReviewResponse
            .builder()
            .id(review.getId())
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

        @NotNull
        private Integer id;

        @NotNull
        private Integer rate;

        @NotNull
        @Length(min = 20, max = 2048, message="Xin vui lòng để nội dung bình luận trong khoảng 20 đến 2048 ký tự.")
        private String content;

        private List<String> images;

    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Response {

        @NonNull
        private ReviewResponse review;

        private Float newRate;

    }

}
