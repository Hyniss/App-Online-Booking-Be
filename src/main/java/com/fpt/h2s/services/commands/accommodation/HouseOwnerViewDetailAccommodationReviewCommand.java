package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.HouseOwnerViewDetailAccommodationRequest;
import com.fpt.h2s.services.commands.responses.ReviewResponse;
import com.fpt.h2s.services.commands.responses.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseOwnerViewDetailAccommodationReviewCommand implements
        BaseCommand<HouseOwnerViewDetailAccommodationRequest, Set<ReviewResponse>> {

    private final AccommodationRepository accommodationRepository;
    @Override
    public ApiResponse<Set<ReviewResponse>> execute
            (final HouseOwnerViewDetailAccommodationRequest request) {
        final Accommodation accommodation = this.accommodationRepository.findById(request.getId()).orElseThrow();
        final var reviews = accommodation.getReviews();
        final Set<ReviewResponse> responses = reviews
                .stream()
                .map(review -> ReviewResponse
                    .builder()
                    .id(review.getId())
                    .owner(UserResponse.of(review.getOwner()))
                    .content(review.getContent())
                    .rate(review.getRate())
                    .createdAt(review.getCreatedAt())
                    .build())
                .collect(Collectors.toSet());
        return ApiResponse.success(responses);
    }
}
