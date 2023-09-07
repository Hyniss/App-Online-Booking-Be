package com.fpt.h2s.controllers;

import com.fpt.h2s.models.annotations.RequireStatus;
import com.fpt.h2s.models.annotations.RequiredRoles;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.Category;
import com.fpt.h2s.models.entities.Property;
import com.fpt.h2s.models.entities.Room;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.RoomRepository;
import com.fpt.h2s.services.commands.accommodation.*;
import com.fpt.h2s.services.commands.requests.ChangeStatusAccommodationRequest;
import com.fpt.h2s.services.commands.requests.HouseOwnerViewDetailAccommodationRequest;
import com.fpt.h2s.services.commands.responses.*;
import com.fpt.h2s.services.commands.transactions.CancelBookingRequestCommand;
import com.fpt.h2s.services.commands.transactions.CreateBookingTransactionRequestCommand;
import com.fpt.h2s.services.commands.transactions.SaveBookingTransactionResult;
import com.fpt.h2s.services.commands.user.HouseOwnerAccommodationsCommand;
import com.fpt.h2s.services.commands.user.HouseOwnerReviewsCommand;
import com.fpt.h2s.utilities.SpringBeans;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.StaleObjectStateException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accommodation")
@SuppressWarnings("unused")
public class AccommodationController {
    @PostMapping("/admin")
    @Operation(summary = "View accommodations for admin")
    public static ApiResponse<ListResult<AdminAccommodationResponse>> viewAccommodationsForAdmin(@Valid @RequestBody final AdminSearchAccommodationCommand.AdminSearchAccommodationCommandRequest request) {
        return SpringBeans.getBean(AdminSearchAccommodationCommand.class).execute(request);
    }

    @GetMapping("")
    @Operation(summary = "View detail accommodation")
    public ApiResponse<ViewDetailAccommodationCommand.Response> detail(final ViewDetailAccommodationCommand.Request request) {
        return SpringBeans.getBean(ViewDetailAccommodationCommand.class).execute(request);
    }

    @PostMapping("/rooms/preview")
    @Operation(summary = "Search preview rooms for accommodation")
    public ApiResponse<List<ShowAccommodationRoomsCommand.RoomResponse>> detail(@Valid @RequestBody final ShowAccommodationRoomsCommand.Request request) {
        return SpringBeans.getBean(ShowAccommodationRoomsCommand.class).execute(request);
    }

    @PostMapping("/rooms/available")
    @Operation(summary = "Search available rooms for accommodation")
    public ApiResponse<List<SearchAccommodationAvailableRoomsCommand.RoomResponse>> detail(@Valid @RequestBody final SearchAccommodationAvailableRoomsCommand.Request request) {
        return SpringBeans.getBean(SearchAccommodationAvailableRoomsCommand.class).execute(request);
    }

    @GetMapping("/properties")
    @Operation(summary = "Get all searchable properties")
    public ApiResponse<GetSearchablePropertiesCommand.Response> getSearchableProperties() {
        return SpringBeans.getBean(GetSearchablePropertiesCommand.class).execute(null);
    }

    @PostMapping("/book")
    @Operation(summary = "Create payment")
    public ApiResponse<CreateBookingTransactionRequestCommand.Response> vnPay(@Valid @RequestBody final CreateBookingTransactionRequestCommand.Request request) {
        try {
            return SpringBeans.getBean(CreateBookingTransactionRequestCommand.class).execute(request);
        } catch (final Exception e) {
            if (ExceptionUtils.getRootCause(e) instanceof StaleObjectStateException transactionException) {
                transactionException.printStackTrace();
                final Integer id = Integer.parseInt(transactionException.getIdentifier().toString());
                final RoomRepository roomRepository = SpringBeans.getBean(RoomRepository.class);
                final Room room = roomRepository.findById(id).orElseThrow();
                throw ApiException.badRequest("Phòng '{}' hiện không đủ phòng để đặt.", room.getName());
            }
            throw e;
        }
    }

    @PutMapping("/book/{id}/cancel")
    @RequiredRoles
    @RequireStatus({User.Status.ACTIVE})
    @Operation(summary = "Save transaction result")
    public ApiResponse<Void> saveTransaction(@PathVariable final Integer id) {
        return SpringBeans.getBean(CancelBookingRequestCommand.class).execute(id);
    }

    @PostMapping("/{id}/success")
    @Operation(summary = "Cancel booking request")
    public ApiResponse<Void> saveTransaction(@PathVariable final String id, final SaveBookingTransactionResult.Request request) {
        return SpringBeans.getBean(SaveBookingTransactionResult.class).execute(request.withId(id));
    }

    @GetMapping("/house-owner/accommodations")
    @Operation(summary = "Find all accommodations of house owner")
    public ApiResponse<ListResult<HouseOwnerAccommodationsCommand.Response>> saveTransaction(@Valid final HouseOwnerAccommodationsCommand.Request request) {
        return SpringBeans.getBean(HouseOwnerAccommodationsCommand.class).execute(request);
    }

    @GetMapping("/house-owner/reviews")
    @Operation(summary = "Find all reviews of house owner")
    public ApiResponse<ListResult<HouseOwnerReviewsCommand.Response>> saveTransaction(@Valid final HouseOwnerReviewsCommand.Request request) {
        return SpringBeans.getBean(HouseOwnerReviewsCommand.class).execute(request);
    }

    @GetMapping("/detail/reviews")
    @Operation(summary = "View list reviews in detail accommodation")
    public ApiResponse<ListResult<ReviewResponse>> viewDetailAccommodationReviews
        (@Valid @ModelAttribute final ViewDetailAccommodationReviewCommand.Request request) {
        return SpringBeans.getBean(ViewDetailAccommodationReviewCommand.class).execute(request);
    }

    @GetMapping("/detail/house-owner")
    @Operation(summary = "View detail house owner in detail accommodation")
    public ApiResponse<DetailAccommodationHouseOwnerResponse> viewDetailAccommodationHouseOwner
        (@Valid @ModelAttribute final ViewDetailAccommodationHouseOwnerCommand.DetailAccommodationHouseOwnerRequest request) {
        return SpringBeans.getBean(ViewDetailAccommodationHouseOwnerCommand.class).execute(request);
    }

    @GetMapping("/category")
    @Operation(summary = "Find all locations in home page")
    public ApiResponse<List<CategoryResponse>> getAllLocationCategories() {
        return SpringBeans.getBean(GetAllLocationCategoryCommand.class).execute(null);
    }

    @GetMapping("/amenity")
    @Operation(summary = "Find all amenities")
    public ApiResponse<ListResult<CategoryResponse>> getAllAmenities(@Valid @ModelAttribute final SearchAmenityCommand.Request request) {
        return SpringBeans.getBean(SearchAmenityCommand.class).execute(request);
    }

    @PostMapping("/search")
    @Operation(summary = "Search accommodations for guest.")
    public ApiResponse<ListResult<AccommodationResponse>> searchAccommodationsToBook(@Valid @RequestBody final SearchAccommodationCommand.Request request) {
        return SpringBeans.getBean(SearchAccommodationCommand.class).execute(request);
    }

    @GetMapping("/search/filter-items")
    @Operation(summary = "Get all essential items to search accommodations")
    public ApiResponse<AccommodationFilterEssentialCommand.Response> getFilterItemsForAccommodationsToBook() {
        return SpringBeans.getBean(AccommodationFilterEssentialCommand.class).execute(null);
    }

//    @PostMapping("/booking")
//    @Operation(summary = "Booking a accommodation")
//    public ApiResponse<Void> getFilterItemsForAccommodationsToBook(@Valid @RequestBody final BookingAccommodationCommand.Request request) {
//        return SpringBeans.getBean(BookingAccommodationCommand.class).execute(request);
//    }

    @PutMapping("/change-status")
    @RequiredRoles({User.Role.ADMIN, User.Role.HOUSE_OWNER})
    @Operation(summary = "Change status of accommodation")
    public static ApiResponse<Void> changeStatusAccommodation(@Valid @ModelAttribute final ChangeStatusAccommodationRequest request) {
        return SpringBeans.getBean(ChangeStatusAccommodationCommand.class).execute(request);
    }

    @PostMapping("/house-owner/list")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "View accommodations for house owner")
    public static ApiResponse<ListResult<HouseOwnerAccommodationResponse>> viewAccommodationsForHouseOwner
        (@Valid @RequestBody final HouseOwnerSearchAccommodationCommand.HouseOwnerSearchAccommodationCommandRequest request) {
        return SpringBeans.getBean(HouseOwnerSearchAccommodationCommand.class).execute(request);
    }

    @GetMapping("/house-owner/detail")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "View detail accommodation for house owner")
    public ApiResponse<HouseOwnerDetailAccommodationResponse> viewDetailAccommodationForHouseOwner
        (@Valid @ModelAttribute final HouseOwnerViewDetailAccommodationRequest request) {
        return SpringBeans.getBean(HouseOwnerViewDetailAccommodationCommand.class).execute(request);
    }

    @GetMapping("/house-owner/detail/reviews")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "View list reviews in detail accommodation for house owner")
    public ApiResponse<Set<ReviewResponse>> viewDetailAccommodationReviewsForHouseOwner
        (@Valid @ModelAttribute final HouseOwnerViewDetailAccommodationRequest request) {
        return SpringBeans.getBean(HouseOwnerViewDetailAccommodationReviewCommand.class).execute(request);
    }

    @GetMapping("/house-owner/detail/price")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "View detail accommodation for house owner")
    public ApiResponse<Map<Integer, List<HouseOwnerViewDetailAccommodationAmountResponse>>> viewDetailAccommodationAmountForHouseOwner
        (@Valid @ModelAttribute final HouseOwnerViewDetailAccommodationAmountCommand.Request request) {
        return SpringBeans.getBean(HouseOwnerViewDetailAccommodationAmountCommand.class).execute(request);
    }

    @PostMapping("/house-owner/validate")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Validate accommodation request for house owner")
    public ApiResponse<Void> validateAccommodationForHouseOwner
        (@Valid @RequestBody final HouseOwnerValidateCreateAccommodationCommand.Request request) {
        return SpringBeans.getBean(HouseOwnerValidateCreateAccommodationCommand.class).execute(request);
    }

    @PostMapping("/house-owner/create")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Create a new accommodation for house owner")
    public ApiResponse<HouseOwnerCreateAccommodationResponse> createAccommodationForHouseOwner
        (@Valid @RequestBody final HouseOwnerCreateAccommodationCommand.Request request) {
        return SpringBeans.getBean(HouseOwnerCreateAccommodationCommand.class).execute(request);
    }

    @PutMapping("/house-owner/update")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Update accommodation for house owner")
    public ApiResponse<Void> updateAccommodationForHouseOwner
        (@Valid @RequestBody final HouseOwnerUpdateAccommodationCommand.Request request) {
        return SpringBeans.getBean(HouseOwnerUpdateAccommodationCommand.class).execute(request);
    }

    @PutMapping("/house-owner/update/status")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Change status of accommodation")
    public static ApiResponse<Void> updateAccommodationStatusForHouseOwner
        (@Valid @ModelAttribute final ChangeStatusAccommodationRequest request) {
        return SpringBeans.getBean(ChangeStatusAccommodationCommand.class).execute(request);
    }

    @PutMapping("/house-owner/update/images")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Change image of accommodation")
    public static ApiResponse<Void> updateAccommodationImagesForHouseOwner
        (@Valid @RequestBody final HouseOwnerUpdateAccommodationImageCommand.Request request) {
        return SpringBeans.getBean(HouseOwnerUpdateAccommodationImageCommand.class).execute(request);
    }

    @GetMapping("/house-owner/categories")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Send list categories to house owner for create accommodation")
    public ApiResponse<Map<Category.Type, List<Category>>> viewCategoriesForHouseOwner() {
        return SpringBeans.getBean(HouseOwnerViewListCategories.class).execute(null);
    }

    @GetMapping("/house-owner/properties")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Send list properties to house owner for create accommodation")
    public ApiResponse<List<Property>> viewPropertiesForHouseOwner() {
        return SpringBeans.getBean(HouseOwnerViewListProperties.class).execute(null);
    }

    @PostMapping("/house-owner/upload")
    @Operation(summary = "Upload lists images for house owner")
    public ApiResponse<List<String>> upload(@RequestPart final List<MultipartFile> images) {
        return SpringBeans.getBean(HouseOwnerCreateAccommodationUploadCommand.class).execute(images);
    }

    @PostMapping("/review")
    @Operation(summary = "Add review to accommodation")
    @RequiredRoles({User.Role.BUSINESS_MEMBER, User.Role.CUSTOMER})
    public ApiResponse<CreateReviewCommand.Response> reviewAccommodation(@Valid @RequestBody final CreateReviewCommand.Request request) {
        return SpringBeans.getBean(CreateReviewCommand.class).execute(request);
    }

    @PutMapping("/review")
    @Operation(summary = "Edit review")
    @RequiredRoles({User.Role.BUSINESS_MEMBER, User.Role.CUSTOMER})
    public ApiResponse<UpdateReviewCommand.Response> editReview(@Valid @RequestBody final UpdateReviewCommand.Request request) {
        return SpringBeans.getBean(UpdateReviewCommand.class).execute(request);
    }

    @GetMapping("/review/check")
    @Operation(summary = "Check if user can review accommodation.")
    @RequiredRoles({User.Role.BUSINESS_MEMBER, User.Role.CUSTOMER})
    public ApiResponse<Boolean> checkReviewAccommodation(@Valid final CheckAccommodationReviewableCommand.Request request) {
        return SpringBeans.getBean(CheckAccommodationReviewableCommand.class).execute(request);
    }

}
