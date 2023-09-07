package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.BookingRequestRepository;
import com.fpt.h2s.repositories.TravelStatementRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.CategoryResponse;
import com.fpt.h2s.services.commands.responses.UserResponse;
import com.fpt.h2s.utilities.Mappers;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.fpt.h2s.models.entities.User.Role.ADMIN;
import static com.fpt.h2s.models.entities.User.Role.HOUSE_OWNER;

@Service
@RequiredArgsConstructor
public class ViewDetailAccommodationCommand implements BaseCommand<ViewDetailAccommodationCommand.Request, ViewDetailAccommodationCommand.Response> {
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final TravelStatementRepository travelStatementRepository;

    @Override
    public ApiResponse<Response> execute(final ViewDetailAccommodationCommand.Request request) {
        User currentUser = User.currentUserId().flatMap(userRepository::findById).orElse(User.DUMMY);
        final Accommodation accommodation = this.accommodationRepository.getById(request.getId(), "Không tìm thấy nhà.");
        checkIfUserCanView(currentUser, accommodation);

        final Response response = Mappers.convertTo(Response.class, accommodation)
            .withImages(accommodation.getImages().stream().toList())
            .withAmenities(accommodation.getCategories().stream().filter(category -> category.getType() == Category.Type.AMENITY).sorted(Comparator.comparing(Category::getId)).map(CategoryResponse::of).toList())
            .withViews(accommodation.getCategories().stream().filter(category -> category.getType() == Category.Type.LOCATION).sorted(Comparator.comparing(Category::getId)).map(CategoryResponse::of).toList())
            .withCanBook(canUserBook(request, accommodation, currentUser))
            .withOwner(UserResponse.of(accommodation.getOwner()));
        return ApiResponse.success(response);
    }

    private static void checkIfUserCanView(User currentUser, Accommodation accommodation) {
        if (accommodation.getStatus() == Accommodation.Status.OPENING) {
            return;
        }
        if (currentUser.is(ADMIN)) {
            return;
        }
        if (currentUser.is(HOUSE_OWNER) && accommodation.isCreatedBy(currentUser)) {
            return;
        }
        throw ApiException.badRequest("Chỗ ở này hiện tại chưa được mở cho thuê phòng.");
    }

    private boolean canUserBook(Request request, Accommodation accommodation, User currentUser) {
        if (currentUser.isOneOf(User.Role.BUSINESS_OWNER, User.Role.BUSINESS_MEMBER, ADMIN)) {
            return false;
        }
        if (accommodation.isCreatedBy(currentUser)) {
            return false;
        }
        if (currentUser.is(User.Role.BUSINESS_ADMIN)) {
            boolean adminIsBookingForStatement = request.statementId != null;
            if (!adminIsBookingForStatement) {
                return false;
            }
            Optional<TravelStatement> statement = travelStatementRepository.findById(request.statementId);
            if (statement.isEmpty()) {
                return false;
            }

            User statementOwner = statement.get().getUser();
            boolean isSameCompany = Objects.equals(statementOwner.getCompanyId(), currentUser.getCompanyId());
            return isSameCompany;
        }
        return true;
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static final class Request extends BaseRequest {
        private final Integer id;
        private final Integer statementId;
    }

    @With
    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static final class Response extends BaseRequest {
        private Integer id;

        private String name;

        private String thumbnail;

        private String shortDescription;

        private String description;

        private String address;

        private Double latitude;

        private Double longitude;

        private Long minPrice;

        private Long maxPrice;

        private Accommodation.Type type;

        private Integer totalViews;

        private Integer totalLikes;

        private Integer totalReviews;

        private Integer totalBookings;

        private Float reviewRate;

        private UserResponse owner;

        private Integer ownerId;

        private Boolean likedByUser;

        private List<AccommodationImage> images;

        private List<CategoryResponse> amenities;
        private List<CategoryResponse> views;

        private boolean canBook;

    }

}
