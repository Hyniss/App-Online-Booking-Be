package com.fpt.h2s.controllers;

import com.fpt.h2s.models.annotations.RequiredRoles;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.services.commands.company.*;
import com.fpt.h2s.services.commands.responses.BusinessAdminSearchBusinessMemberResponse;
import com.fpt.h2s.services.commands.responses.BusinessAdminUploadBusinessUserSucceedResponse;
import com.fpt.h2s.services.commands.responses.CompanyResponse;
import com.fpt.h2s.services.commands.responses.UserResponse;
import com.fpt.h2s.utilities.SpringBeans;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/company")
@SuppressWarnings("unused")
public class CompanyController {
    @GetMapping("/business/detail")
    @RequiredRoles({User.Role.BUSINESS_OWNER, User.Role.BUSINESS_ADMIN, User.Role.BUSINESS_MEMBER})
    @Operation(summary = "View current company of user.")
    public static ApiResponse<CompanyResponse> viewCurrentCompanyOfUser(@Valid @ModelAttribute final ViewDetailedCompanyForBusinessUserCommand.Request request) {
        return SpringBeans.getBean(ViewDetailedCompanyForBusinessUserCommand.class).execute(request);
    }

    @PutMapping("/business/send-cooperate-request")
    @RequiredRoles({User.Role.BUSINESS_OWNER})
    @Operation(summary = "Send cooperate request to admin")
    public static ApiResponse<Void> sendCooperateRequestToAdmin(@Valid @ModelAttribute final SendCooperateRequestToAdminCommand.Request request) {
        return SpringBeans.getBean(SendCooperateRequestToAdminCommand.class).execute(request);
    }

    @PutMapping("/business/detail")
    @RequiredRoles({User.Role.BUSINESS_OWNER})
    @Operation(summary = "Update current company information.")
    public static ApiResponse<Void> updateCurrentCompanyInformation(@Valid @RequestBody final UpdateCompanyCommand.Request request) {
        return SpringBeans.getBean(UpdateCompanyCommand.class).execute(request);
    }

    @PostMapping("/admin/list")
    @RequiredRoles({User.Role.ADMIN})
    @Operation(summary = "Find all companies for admin.")
    public static ApiResponse<ListResult<CompanyResponse>> findAllCompaniesForAdmin(@Valid @RequestBody final AdminSearchCompanyCommand.Request request) {
        return SpringBeans.getBean(AdminSearchCompanyCommand.class).execute(request);
    }


    @GetMapping("/admin/detail")
    @RequiredRoles({User.Role.ADMIN})
    @Operation(summary = "View company for admin.")
    public static ApiResponse<CompanyResponse> viewCompanyForAdmin(@Valid @ModelAttribute final AdminViewDetailCompanyCommand.Request request) {
        return SpringBeans.getBean(AdminViewDetailCompanyCommand.class).execute(request);
    }

    @PutMapping("/admin/status")
    @RequiredRoles({User.Role.ADMIN})
    @Operation(summary = "Update status company for admin.")
    public static ApiResponse<CompanyResponse> viewCompanyForAdmin(@Valid @RequestBody final AdminUpdateCompanyStatusCommand.Request request) {
        return SpringBeans.getBean(AdminUpdateCompanyStatusCommand.class).execute(request);
    }

    @PostMapping("/business-owner/add")
    @RequiredRoles({User.Role.BUSINESS_OWNER})
    @Operation(summary = "Add business member and admin by business owner.")
    public static ApiResponse<Void> addBusinessUserByBusinessOwner
        (@Valid @RequestBody final BusinessOwnerAddBusinessUserCommand.Request request) {
        return SpringBeans.getBean(BusinessOwnerAddBusinessUserCommand.class).execute(request);
    }

    @PostMapping("/business-admin/add")
    @RequiredRoles({User.Role.BUSINESS_ADMIN})
    @Operation(summary = "Add business member by business admin.")
    public static ApiResponse<Void> addBusinessUserByBusinessAdmin
        (@Valid @RequestBody final BusinessAdminAddBusinessUserCommand.Request request) {
        return SpringBeans.getBean(BusinessAdminAddBusinessUserCommand.class).execute(request);
    }

    @PostMapping("/business-admin/list")
    @RequiredRoles({User.Role.BUSINESS_ADMIN, User.Role.BUSINESS_OWNER, User.Role.BUSINESS_MEMBER})
    @Operation(summary = "View list business members")
    public ApiResponse<ListResult<BusinessAdminSearchBusinessMemberResponse>> listBusinessUsersByAdmin(@RequestBody final BusinessAdminSearchBusinessUserCommand.Request request) {
        return SpringBeans.getBean(BusinessAdminSearchBusinessUserCommand.class).execute(request);
    }

    @PutMapping("/business-admin/disable")
    @RequiredRoles({User.Role.BUSINESS_ADMIN, User.Role.BUSINESS_OWNER})
    @Operation(summary = "Active/Inactive user")
    public static ApiResponse<UserResponse> changeBusinessUsersStatus(@Valid @ModelAttribute final BusinessAdminChangeStatusBusinessUserCommand.Request request) {
        return SpringBeans.getBean(BusinessAdminChangeStatusBusinessUserCommand.class).execute(request);
    }


    @PostMapping("/business-admin/upload")
    @RequiredRoles({User.Role.BUSINESS_ADMIN, User.Role.BUSINESS_OWNER})
    @Operation(summary = "Upload business member by business admin")
    public ApiResponse<BusinessAdminUploadBusinessUserSucceedResponse> upload(@RequestPart final MultipartFile file) {
        return SpringBeans.getBean(BusinessAdminUploadBusinessUserCommand.class).execute(file);
    }

    @PostMapping("/business-admin/send/mail")
    @RequiredRoles({User.Role.BUSINESS_ADMIN, User.Role.BUSINESS_OWNER})
    @Operation(summary = "Send mail to business member after upload")
    public ApiResponse<Void> sendMail(@RequestBody final BusinessAdminSendMailBusinessUserCommand.Request request) {
        return SpringBeans.getBean(BusinessAdminSendMailBusinessUserCommand.class).execute(request);
    }
}
