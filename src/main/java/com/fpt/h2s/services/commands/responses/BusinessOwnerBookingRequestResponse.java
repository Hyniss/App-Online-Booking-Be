package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.BookingRequest;
import com.fpt.h2s.models.entities.BookingRequestDetail;
import com.fpt.h2s.models.entities.Room;
import com.fpt.h2s.models.entities.TravelStatement;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
public class BusinessOwnerBookingRequestResponse {
    private Integer id;

    private Integer totalRooms;

    private Integer userId;

    private String customerName;

    private String customerEmail;

    private String contact;

    private String contactName;

    private String note;

    private BookingRequest.Status status;

    private Integer accommodationId;

    private String accommodationName;

    private Integer accommodationOwnerId;

    private String accommodationOwnerUsername;

    private Integer transactionId;

    private Long transactionAmount;

    private Timestamp transactionCreatedAt;

    private Timestamp checkinAt;

    private Timestamp checkoutAt;

    private Timestamp createdAt;

    private TravelStatementResponse travelStatement;

    private Set<BusinessOwnerBookingRequestResponse.BookingRequestDetailResponse> bookingRequestDetails;

    public static BusinessOwnerBookingRequestResponse of(final BookingRequest bookingRequest) {
        return BusinessOwnerBookingRequestResponse
            .builder()
            .id(bookingRequest.getId())
            .totalRooms(bookingRequest.getTotalRooms())
            .userId(bookingRequest.getUserId())
            .customerName((bookingRequest.getUser() == null) ? "" : bookingRequest.getUser().getUsername())
            .customerEmail((bookingRequest.getUser() == null) ? "" : bookingRequest.getUser().getEmail())
            .contact(bookingRequest.getContact())
            .contactName(bookingRequest.getContactName())
            .note(bookingRequest.getNote())
            .status(bookingRequest.getStatus())
            .accommodationId(bookingRequest.getAccommodationId())
            .accommodationName((bookingRequest.getAccommodation()) == null ? "" : bookingRequest.getAccommodation().getName())
            .accommodationOwnerId((bookingRequest.getAccommodation()) == null ? 0 : bookingRequest.getAccommodation().getOwnerId())
            .accommodationOwnerUsername((bookingRequest.getAccommodation()) == null ? "" : bookingRequest.getAccommodation().getOwner().getUsername())
            .transactionId(bookingRequest.getTransactionId())
            .transactionAmount((bookingRequest.getTransaction() == null) ? 0 : (bookingRequest.getTransaction().getAmount() / 100))
            .transactionCreatedAt((bookingRequest.getTransaction() == null) ? new Timestamp(System.currentTimeMillis()) : bookingRequest.getTransaction().getCreatedAt())
            .checkinAt(bookingRequest.getCheckinAt())
            .checkoutAt(bookingRequest.getCheckoutAt())
            .createdAt(bookingRequest.getCreatedAt())
            .travelStatement(bookingRequest.getTravelStatement() == null ? null : TravelStatementResponse.of(bookingRequest.getTravelStatement()))
            .bookingRequestDetails(turnToDetailBookingRequestDetailResponse(bookingRequest.getBookingRequestDetails()))
            .build();
    }

    @Builder
    @Getter
    public static class BookingRequestDetailResponse {
        private Integer id;

        private Long price;

        private Integer totalRooms;

        private BusinessOwnerBookingRequestResponse.RoomResponse room;

        public static BusinessOwnerBookingRequestResponse.BookingRequestDetailResponse of(final BookingRequestDetail bookingRequestDetail) {
            return BusinessOwnerBookingRequestResponse.BookingRequestDetailResponse
                .builder()
                .id(bookingRequestDetail.getId())
                .price(bookingRequestDetail.getPrice())
                .totalRooms(bookingRequestDetail.getTotalRooms())
                .room(BusinessOwnerBookingRequestResponse.RoomResponse.of(bookingRequestDetail.getRoom()))
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


        public static BusinessOwnerBookingRequestResponse.RoomResponse of(final Room room) {
            return BusinessOwnerBookingRequestResponse.RoomResponse
                .builder()
                .id(room.getId())
                .name(room.getName())
                .status(room.getStatus())
                .price(room.getPrice())
                .build();
        }
    }

    @Builder
    @Getter
    public static class TravelStatementResponse {
        private Integer id;

        private String name;

        private  Integer numberOfPeople;

        private  String location;

        private String note;

        private Timestamp fromDate;

        private Timestamp toDate;

        private String creator;


        public static BusinessOwnerBookingRequestResponse.TravelStatementResponse of(final TravelStatement travelStatement) {
            return TravelStatementResponse
                    .builder()
                    .id(travelStatement.getId())
                    .name(travelStatement.getName())
                    .numberOfPeople(travelStatement.getNumberOfPeople())
                    .location(travelStatement.getLocation())
                    .note(travelStatement.getRejectMessage())
                    .fromDate(travelStatement.getFromDate())
                    .toDate(travelStatement.getToDate())
                    .creator(travelStatement.getUser() == null ? "" : travelStatement.getUser().getUsername())
                    .build();
        }
    }

    public static Set<BusinessOwnerBookingRequestResponse.BookingRequestDetailResponse> turnToDetailBookingRequestDetailResponse(final Set<BookingRequestDetail> details) {
        return details.stream()
            .map(BusinessOwnerBookingRequestResponse.BookingRequestDetailResponse::of)
            .collect(Collectors.toSet());
    }
}
