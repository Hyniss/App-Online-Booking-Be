package com.fpt.h2s.controllers;

import com.fpt.h2s.models.annotations.RequiredRoles;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.services.commands.boookingrequest.*;
import com.fpt.h2s.services.commands.responses.*;
import com.fpt.h2s.utilities.SpringBeans;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/booking-request")
@SuppressWarnings("unused")
public class BookingRequestController {
    @PostMapping("/house-owner")
    @Operation(summary = "View booking requests for house owner")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    public static ApiResponse<ListResult<HouseOwnerBookingRequestResponse>> viewBookingRequestsForHouseOwner(@Valid @RequestBody final HouseOwnerSearchBookingRequestCommand.HouseOwnerSearchBookingRequestCommandRequest request) {
        return SpringBeans.getBean(HouseOwnerSearchBookingRequestCommand.class).execute(request);
    }

    @PostMapping("/list")
    @RequiredRoles({User.Role.CUSTOMER})
    @Operation(summary = "View booking requests for guest")
    public static ApiResponse<ListResult<BookingRequestResponse>> viewBookingRequestsForCustomer(@Valid @RequestBody final SearchBookingRequestCommand.SearchBookingRequestCommandRequest request) {
        return SpringBeans.getBean(SearchBookingRequestCommand.class).execute(request);
    }

    @RequiredRoles({User.Role.CUSTOMER, User.Role.BUSINESS_ADMIN})
    @PutMapping("/change-status")
    @Operation(summary = "customer cancel booking request")
    public static ApiResponse<BookingRequestResponse> changeStatusBookingRequestForCustomer(@Valid @RequestBody final ChangeStatusBookingRequestCommand.ChangeStatusBookingRequestRequest request) {
        return SpringBeans.getBean(ChangeStatusBookingRequestCommand.class).execute(request);
    }

    @GetMapping("")
    @RequiredRoles({User.Role.CUSTOMER, User.Role.HOUSE_OWNER})
    @Operation(summary = "View detailed booking request for customer and house owner")
    public static ApiResponse<DetailBookingRequestResponse> viewDetailedBookingRequestForCustomer(@Valid @ModelAttribute final ViewDetailedBookingRequestCommand.ViewDetailedBookingRequestRequest request) {
        return SpringBeans.getBean(ViewDetailedBookingRequestCommand.class).execute(request);
    }

    @PostMapping("/business")
    @Operation(summary = "View booking requests for business owner and business admin")
    @RequiredRoles({User.Role.BUSINESS_OWNER, User.Role.BUSINESS_ADMIN})
    public static ApiResponse<ListResult<BusinessOwnerBookingRequestResponse>> viewBookingRequestsForBusinessOwner(@Valid @RequestBody final BusinessOwnerSearchBookingRequestCommand.BusinessOwnerSearchBookingRequestCommandRequest request) {
        return SpringBeans.getBean(BusinessOwnerSearchBookingRequestCommand.class).execute(request);
    }

    @GetMapping("/business")
    @RequiredRoles({User.Role.BUSINESS_OWNER, User.Role.BUSINESS_ADMIN, User.Role.BUSINESS_MEMBER})
    @Operation(summary = "View detailed booking request for business customer")
    public static ApiResponse<BusinessOwnerDetailBookingRequestResponse> viewDetailedBookingRequestForBusinessOwner(@Valid @ModelAttribute final BusinessOwnerViewDetailedBookingRequestCommand.BusinessOwnerViewDetailedBookingRequestRequest request) {
        return SpringBeans.getBean(BusinessOwnerViewDetailedBookingRequestCommand.class).execute(request);
    }
}
