package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.AccommodationImage;
import com.fpt.h2s.models.entities.Like;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.repositories.AccommodationImageRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.responses.AccommodationResponse;
import com.fpt.h2s.services.commands.responses.UserResponse;
import com.fpt.h2s.utilities.ImmutableCollectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class AccommodationAdapter {

    private final AccommodationImageRepository accommodationImageRepository;
    private final UserRepository userRepository;

    @NotNull
    public List<AccommodationResponse> responsesOf(final List<Accommodation> accommodations) {
        final Set<Integer> currentUserLikedAccommodations = this.getLikedAccommodationsOfCurrentUser();

        final Map<Integer, User> ownerMapToId = this.userRepository.findAllByIdsIn(accommodations, Accommodation::getOwnerId);
        final Map<Integer, List<String>> imagesMapToAccId = this.accommodationImageRepository
            .findAllByAccommodationIdIn(accommodations.stream().map(Accommodation::getId).toList())
            .stream()
            .collect(groupingBy(
                AccommodationImage::getAccommodationId,
                ImmutableCollectors.toList(AccommodationImage::getImage))
            );

        return accommodations.stream().map(accommodation -> {
            final AccommodationResponse response = AccommodationResponse.of(accommodation);
            return response
                .withThumbnails(imagesMapToAccId.get(accommodation.getId()))
                .withLiked(currentUserLikedAccommodations.contains(accommodation.getId()))
                .withOwner(UserResponse.of(ownerMapToId.get(accommodation.getOwnerId())));
        }).toList();
    }

    @NotNull
    private Set<Integer> getLikedAccommodationsOfCurrentUser() {
        return User.currentUserId()
            .map(this.userRepository::getById)
            .map(User::getLikedAccommodations)
            .orElseGet(Set::of)
            .stream()
            .map(Like::getId)
            .map(Like.PK::getAccommodationId).collect(Collectors.toSet());
    }
}
