package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.BookingRequest;
import com.fpt.h2s.models.entities.BookingRequestDetail;
import com.fpt.h2s.models.entities.Room;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
public class HouseOwnerBookingRequestResponse {
    private Integer id;

    private Integer totalRooms;

    private Integer userId;

    private String userUsername;

    private String contact;

    private String contactName;

    private String note;

    private BookingRequest.Status status;

    private Integer accommodationId;

    private String accommodationName;

    private Integer accommodationOwnerId;

    private String accommodationOwnerUsername;

    private Timestamp checkinAt;

    private Timestamp checkoutAt;

    private Timestamp createdAt;

    private Integer transactionId;

    private Long transactionAmount;

    private Set<HouseOwnerBookingRequestResponse.BookingRequestDetailResponse> bookingRequestDetails;

    public static HouseOwnerBookingRequestResponse of(final BookingRequest bookingRequest) {
        return HouseOwnerBookingRequestResponse
            .builder()
            .id(bookingRequest.getId())
            .totalRooms(bookingRequest.getTotalRooms())
            .userId(bookingRequest.getUserId())
            .userUsername((bookingRequest.getUser()) == null ? "" : bookingRequest.getUser().getUsername())
            .contact(bookingRequest.getContact())
            .contactName(bookingRequest.getContactName())
            .note(bookingRequest.getNote())
            .status(bookingRequest.getStatus())
            .accommodationId(bookingRequest.getAccommodationId())
            .accommodationName((bookingRequest.getAccommodation()) == null ? "" : bookingRequest.getAccommodation().getName())
            .accommodationOwnerId((bookingRequest.getAccommodation()) == null ? 0 : bookingRequest.getAccommodation().getOwnerId())
            .accommodationOwnerUsername((bookingRequest.getAccommodation()) == null ? "" : bookingRequest.getAccommodation().getOwner().getUsername())
            .checkinAt(bookingRequest.getCheckinAt())
            .checkoutAt(bookingRequest.getCheckoutAt())
            .createdAt(bookingRequest.getCreatedAt())
            .transactionId(bookingRequest.getTransactionId())
            .transactionAmount((bookingRequest.getTransaction()) == null ? 0 : (bookingRequest.getTransaction().getAmount() / 100))
            .bookingRequestDetails(turnToDetailBookingRequestDetailResponse(bookingRequest.getBookingRequestDetails()))
            .build();
    }

    @Builder
    @Getter
    public static class BookingRequestDetailResponse {
        private Integer id;

        private Long price;

        private Integer totalRooms;

        private HouseOwnerBookingRequestResponse.RoomResponse room;

        public static HouseOwnerBookingRequestResponse.BookingRequestDetailResponse of(final BookingRequestDetail bookingRequestDetail) {
            return HouseOwnerBookingRequestResponse.BookingRequestDetailResponse
                .builder()
                .id(bookingRequestDetail.getId())
                .price(bookingRequestDetail.getPrice())
                .totalRooms(bookingRequestDetail.getTotalRooms())
                .room(HouseOwnerBookingRequestResponse.RoomResponse.of(bookingRequestDetail.getRoom()))
                .build();
        }
    }

    @Builder
    @Getter
    public static class RoomResponse {
        private Integer id;

        private String name;

        private Room.Status status;

        private Long price;


        public static HouseOwnerBookingRequestResponse.RoomResponse of(final Room room) {
            return HouseOwnerBookingRequestResponse.RoomResponse
                .builder()
                .id(room.getId())
                .name(room.getName())
                .status(room.getStatus())
                .price(room.getPrice())
                .build();
        }
    }

    public static Set<HouseOwnerBookingRequestResponse.BookingRequestDetailResponse> turnToDetailBookingRequestDetailResponse(final Set<BookingRequestDetail> details) {
        return details.stream()
            .map(HouseOwnerBookingRequestResponse.BookingRequestDetailResponse::of)
            .collect(Collectors.toSet());
    }
}
