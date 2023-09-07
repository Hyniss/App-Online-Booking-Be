package com.fpt.h2s.controllers;

import com.fpt.h2s.models.annotations.RequiredRoles;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.TravelStatement;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.services.commands.requests.BusinessMemberCreateTravelStatementRequest;
import com.fpt.h2s.services.commands.responses.TravelStatementResponse;
import com.fpt.h2s.services.commands.travelstatement.*;
import com.fpt.h2s.utilities.SpringBeans;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/travel-statement")
@SuppressWarnings("unused")
public class TravelStatementController {
    @PostMapping("/business-admin")
    @Operation(summary = "View travel statements for business owner")
    @RequiredRoles({User.Role.BUSINESS_ADMIN})
    public static ApiResponse<ListResult<TravelStatementResponse>> viewTravelStatementsForBusinessAdmin(@Valid @RequestBody final BusinessAdminSearchTravelStatementCommand.BusinessAdminSearchTravelStatementCommandRequest request) {
        return SpringBeans.getBean(BusinessAdminSearchTravelStatementCommand.class).execute(request);
    }

    @PutMapping("/change-status")
    @RequiredRoles({User.Role.BUSINESS_ADMIN, User.Role.BUSINESS_MEMBER})
    @Operation(summary = "business admin/member change status travel statement")
    public static ApiResponse<TravelStatementResponse> changeStatusTravelStatement(@Valid @RequestBody final ChangeStatusTravelStatementCommand.ChangeStatusTravelStatementRequest request) {
        return SpringBeans.getBean(ChangeStatusTravelStatementCommand.class).execute(request);
    }

    @PostMapping("/business-member")
    @RequiredRoles({User.Role.BUSINESS_MEMBER})
    @Operation(summary = "View travel statements for business member")
    public static ApiResponse<ListResult<TravelStatementResponse>> viewTravelStatementsForBusinessMember(@Valid @RequestBody final BusinessMemberSearchTravelStatementCommand.BusinessMemberSearchTravelStatementCommandRequest request) {
        return SpringBeans.getBean(BusinessMemberSearchTravelStatementCommand.class).execute(request);
    }

    @PostMapping("/business-member/create")
    @RequiredRoles({User.Role.BUSINESS_MEMBER})
    @Operation(summary = "Business member create travel statement")
    public static ApiResponse<TravelStatement> createTravelStatement(@Valid @RequestBody final BusinessMemberCreateTravelStatementRequest request) {
        return SpringBeans.getBean(BusinessMemberCreateTravelStatementCommand.class).execute(request);
    }

    @PutMapping("/business-member/update")
    @RequiredRoles({User.Role.BUSINESS_MEMBER})
    @Operation(summary = "Update travel statement for business member")
    public static ApiResponse<TravelStatementResponse> updateTravelStatement(@Valid @RequestBody final UpdateTravelStatementCommand.UpdateTravelStatementRequest request) {
        return SpringBeans.getBean(UpdateTravelStatementCommand.class).execute(request);
    }

}
