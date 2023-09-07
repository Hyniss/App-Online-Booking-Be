package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.utilities.Mappers;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
public class BusinessOwnerDetailBookingRequestResponse {
    private Integer id;

    private Integer totalRooms;

    private String contact;

    private String contactName;

    private String note;

    private BookingRequest.Status status;

    private Timestamp checkinAt;

    private Timestamp checkoutAt;

    private Timestamp createdAt;

    private BusinessOwnerDetailBookingRequestResponse.AccommodationResponse accommodation;

    private BusinessOwnerDetailBookingRequestResponse.DetailBookingRequestUserResponse user;

    private BusinessOwnerDetailBookingRequestResponse.DetailBookingRequestTransactionResponse transaction;

    private BusinessOwnerDetailBookingRequestResponse.DetailBookingRequestTravelStatementResponse travelStatement;

    private Set<BusinessOwnerDetailBookingRequestResponse.BookingRequestDetailResponse> bookingRequestDetails;

    private Long originalPrice;

    private Long total;

    public static BusinessOwnerDetailBookingRequestResponse of(final BookingRequest bookingRequest) {
        return BusinessOwnerDetailBookingRequestResponse
            .builder()
            .id(bookingRequest.getId())
            .totalRooms(bookingRequest.getTotalRooms())
            .contact(bookingRequest.getContact())
            .contactName(bookingRequest.getContactName())
            .note(bookingRequest.getNote())
            .status(bookingRequest.getStatus())
            .checkinAt(bookingRequest.getCheckinAt())
            .checkoutAt(bookingRequest.getCheckoutAt())
            .createdAt(bookingRequest.getCreatedAt())
            .accommodation(BusinessOwnerDetailBookingRequestResponse.AccommodationResponse.of(bookingRequest))
            .user(BusinessOwnerDetailBookingRequestResponse.DetailBookingRequestUserResponse.of(bookingRequest.getUser()))
            .transaction(BusinessOwnerDetailBookingRequestResponse.DetailBookingRequestTransactionResponse.of(bookingRequest.getTransaction()))
            .travelStatement(BusinessOwnerDetailBookingRequestResponse.DetailBookingRequestTravelStatementResponse.of(bookingRequest.getTravelStatement()))
            .bookingRequestDetails(turnToDetailBookingRequestDetailResponse(bookingRequest.getBookingRequestDetails()))
            .originalPrice(getOriginalPrice(bookingRequest.getBookingRequestDetails()))
            .total(getTotalPrice(bookingRequest.getBookingRequestDetails()))
            .build();
    }

    @Builder
    @Getter
    public static class BookingRequestDetailResponse {
        private Long price;

        private Long originalPrice;

        private Integer totalRooms;

        private BusinessOwnerDetailBookingRequestResponse.RoomResponse room;

        public static BusinessOwnerDetailBookingRequestResponse.BookingRequestDetailResponse of(final BookingRequestDetail bookingRequestDetail) {
            return BusinessOwnerDetailBookingRequestResponse.BookingRequestDetailResponse
                .builder()
                .price(bookingRequestDetail.getPrice())
                .originalPrice(bookingRequestDetail.getOriginalPrice())
                .totalRooms(bookingRequestDetail.getTotalRooms())
                .room(BusinessOwnerDetailBookingRequestResponse.RoomResponse.of(bookingRequestDetail.getRoom()))
                .build();
        }
    }

    @With
    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static final class RoomResponse {
        private Integer id;
        private String name;
        private Long price;
        private Integer discount;
        private Integer totalRoomsLeft;
        private List<RoomImage> images;
        private List<Category> amenities;
        private List<DetailedRoomProperty> properties;

        public static RoomResponse of(final Room room) {
            return Mappers.convertTo(RoomResponse.class, room)
                    .withImages(room.getImages().stream().toList())
                    .withAmenities(room.getAmenities().stream().toList())
                    .withProperties((room.getProperties()).stream().map(x -> DetailedRoomProperty.of(x)).filter(Objects::nonNull).toList());
        }

    }

    public static Set<BusinessOwnerDetailBookingRequestResponse.BookingRequestDetailResponse> turnToDetailBookingRequestDetailResponse(final Set<BookingRequestDetail> details) {
        return details.stream()
            .map(BusinessOwnerDetailBookingRequestResponse.BookingRequestDetailResponse::of)
            .collect(Collectors.toSet());
    }

    @Builder
    @Getter
    public static class DetailBookingRequestTransactionResponse {
        private Integer id;
        private Long amount;
        private String paymentMethod;
        private String transactionRequestNo;
        private String bankTransactionNo;
        private String payDate;
        private Timestamp createdAt;

        public static BusinessOwnerDetailBookingRequestResponse.DetailBookingRequestTransactionResponse of(final Transaction transaction) {
            return Mappers.convertTo(BusinessOwnerDetailBookingRequestResponse.DetailBookingRequestTransactionResponse.class, transaction);
        }
    }

    @Builder
    @Getter
    public static class DetailBookingRequestUserResponse {
        private Integer id;

        private String email;

        private String username;

        private String phone;

        public static BusinessOwnerDetailBookingRequestResponse.DetailBookingRequestUserResponse of(final User user) {
            return Mappers.convertTo(BusinessOwnerDetailBookingRequestResponse.DetailBookingRequestUserResponse.class, user);
        }
    }

    @Builder
    @Getter
    public static class DetailBookingRequestTravelStatementResponse {
        private Integer id;

        private String name;

        private TravelStatement.Status status;

        private String note;

        private Timestamp fromDate;

        private Timestamp toDate;

        private Integer creatorId;

        private String creatorName;

        private Timestamp approvedAt;

        private Timestamp createdAt;


        public static BusinessOwnerDetailBookingRequestResponse.DetailBookingRequestTravelStatementResponse of(final TravelStatement travelStatement) {
            return DetailBookingRequestTravelStatementResponse
                .builder()
                .id(travelStatement.getId())
                .name(travelStatement.getName())
                .status(travelStatement.getStatus())
                .note(travelStatement.getNote())
                .fromDate(travelStatement.getFromDate())
                .toDate(travelStatement.getToDate())
                .creatorId(travelStatement.getCreatorId())
                .creatorName((travelStatement.getUser() == null) ? "" : travelStatement.getUser().getUsername())
                .approvedAt(travelStatement.getApprovedAt())
                .createdAt(travelStatement.getCreatedAt())
                .build();
        }
    }

    @With
    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class AccommodationResponse {
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

        private Float reviewRate;

        private UserResponse owner;

        private Integer ownerId;

        private Boolean likedByUser;

        private List<AccommodationImage> images;

        private List<CategoryResponse> amenities;
        private List<CategoryResponse> views;

        private boolean canBook;

        public static AccommodationResponse of(final BookingRequest bookingRequest) {
            return Mappers.convertTo(AccommodationResponse.class, bookingRequest.getAccommodation())
                    .withImages(bookingRequest.getAccommodation().getImages().stream().toList())
                    .withAmenities(bookingRequest.getAccommodation().getCategories().stream().filter(category -> category.getType() == Category.Type.AMENITY).sorted(Comparator.comparing(Category::getId)).map(CategoryResponse::of).toList())
                    .withViews(bookingRequest.getAccommodation().getCategories().stream().filter(category -> category.getType() == Category.Type.LOCATION).sorted(Comparator.comparing(Category::getId)).map(CategoryResponse::of).toList())
                    .withOwner(UserResponse.of(bookingRequest.getAccommodation().getOwner()));
        }

    }

    public static Long getOriginalPrice(Set<BookingRequestDetail> bookingRequestDetail) {
        return bookingRequestDetail.stream().mapToLong(BookingRequestDetail::getOriginalPrice).sum();
    }

    public static Long getTotalPrice(Set<BookingRequestDetail> bookingRequestDetail) {
        return bookingRequestDetail.stream().mapToLong(BookingRequestDetail::getPrice).sum();
    }
}
