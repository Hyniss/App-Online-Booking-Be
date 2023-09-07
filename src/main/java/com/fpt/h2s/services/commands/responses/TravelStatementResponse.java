package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.utilities.Mappers;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.sql.Timestamp;
import java.util.List;

@Builder
@Getter
public class TravelStatementResponse {
    private Integer id;

    private String name;

    private TravelStatement.Status status;

    private  Integer numberOfPeople;

    private  String location;

    private String note;

    private Timestamp fromDate;

    private Timestamp toDate;

    private Timestamp approvedAt;

    private Timestamp createdAt;

    private String rejectMessage;

    private TravelStatementResponse.BookingRequestResponse bookingRequest;

    private TravelStatementResponse.UserResponse user;

    public static TravelStatementResponse of(TravelStatement travelStatement) {
        return TravelStatementResponse
                .builder()
                .id(travelStatement.getId())
                .name(travelStatement.getName())
                .status(travelStatement.getStatus())
                .numberOfPeople(travelStatement.getNumberOfPeople())
                .location(travelStatement.getLocation())
                .note(travelStatement.getNote())
                .fromDate(travelStatement.getFromDate())
                .toDate(travelStatement.getToDate())
                .approvedAt(travelStatement.getApprovedAt())
                .createdAt(travelStatement.getCreatedAt())
                .rejectMessage(travelStatement.getRejectMessage())
                .bookingRequest(TravelStatementResponse.BookingRequestResponse.of(travelStatement.getBookingRequest()))
                .user(TravelStatementResponse.UserResponse.of(travelStatement.getUser()))
                .build();
    }

    @Builder
    @Getter
    @With
    public static class UserResponse {
        private Integer id;

        private String email;

        private String username;

        private List<User.Role> roles;

        public static TravelStatementResponse.UserResponse of(final User user) {
            return Mappers.convertTo(TravelStatementResponse.UserResponse.class, user).withRoles(user.roleList());
        }

    }

    @Builder
    @Getter
    @With
    public static class BookingRequestResponse {
        private Integer id;

        private BookingRequest.Status status;

        public static TravelStatementResponse.BookingRequestResponse of(final BookingRequest bookingRequest) {
            return Mappers.convertTo(TravelStatementResponse.BookingRequestResponse.class, bookingRequest);
        }

    }
}
