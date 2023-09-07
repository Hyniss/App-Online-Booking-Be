package com.fpt.h2s.controllers;

import com.fpt.h2s.models.annotations.RequireStatus;
import com.fpt.h2s.models.annotations.RequiredRoles;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.services.commands.requests.AdminViewDetailedUserRequest;
import com.fpt.h2s.services.commands.requests.ChangeUserStatusRequest;
import com.fpt.h2s.services.commands.responses.AuthResponse;
import com.fpt.h2s.services.commands.responses.UserDetailResponse;
import com.fpt.h2s.services.commands.responses.UserResponse;
import com.fpt.h2s.services.commands.user.*;
import com.fpt.h2s.utilities.SpringBeans;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@SuppressWarnings("unused")
public class UserController {

    @PostMapping("/register/email")
    @Operation(summary = "Register using email")
    public static ApiResponse<Void> registerUsingEmail(@Valid @RequestBody final RegisterUsingEmailCommand.Request request) {
        return SpringBeans.getBean(RegisterUsingEmailCommand.class).execute(request);
    }

    @PostMapping("/register/phone")
    @Operation(summary = "Register using phone")
    public static ApiResponse<Void> registerUsingPhone(@Valid @RequestBody final RegisterUsingPhoneCommand.Request request) {
        return SpringBeans.getBean(RegisterUsingPhoneCommand.class).execute(request);
    }

    @PostMapping("/register/business-owner")
    @Operation(summary = "Register for business owner")
    public static ApiResponse<Void> registerForBusinessOwner(@Valid @RequestBody final RegisterForBusinessOwnerCommand.Request request) {
        return SpringBeans.getBean(RegisterForBusinessOwnerCommand.class).execute(request);
    }

    @GetMapping("/register/business-owner/essential")
    @Operation(summary = "Register for business owner")
    public static ApiResponse<List<HashMap<Object, Object>>> registerForBusinessOwnerEssentials() {
        final List<HashMap<Object, Object>> data = Arrays.stream(Company.Size.values()).map(size -> {
            final HashMap<Object, Object> obj = new HashMap<>();
            obj.put("name", size.name());
            obj.put("from", size.getFrom());
            obj.put("to", size.getTo());
            return obj;
        }).toList();
        return ApiResponse.success(data);
    }

    @PostMapping("/register/phone/send-otp")
    @Operation(summary = "Send otp to phone to register.")
    public static ApiResponse<String> resentOtpToVerifyRegisterUsingPhone(@Valid @RequestBody final SendOtpToRegisterUsingPhoneCommand.Request request) {
        return SpringBeans.getBean(SendOtpToRegisterUsingPhoneCommand.class).execute(request);
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify register using phone")
    public static ApiResponse<String> verifyRegisterUsingPhone(@Valid @RequestBody final OTPVerificationCommand.Request request) {
        return SpringBeans.getBean(OTPVerificationCommand.class).execute(request);
    }

    @GetMapping("/register/verify")
    @Operation(summary = "Verify register using email")
    public static ApiResponse<Void> verifyRegister(@Valid @ModelAttribute final RegisterVerificationCommand.RegisterRequest request) {
        return SpringBeans.getBean(RegisterVerificationCommand.class).execute(request);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh token for HO")
    public static ApiResponse<AuthResponse> refresh() {
        return SpringBeans.getBean(GetNewTokenCommand.class).execute(null);
    }

    @GetMapping("/profile/{id}")
    @Operation(summary = "View detailed user profile.")
    public static ApiResponse<ViewUserProfileCommand.Response> updateProfile(@Validated @NotNull @PathVariable final Integer id) {
        return SpringBeans.getBean(ViewUserProfileCommand.class).execute(ViewUserProfileCommand.Request.builder().id(id).build());
    }

    @RequiredRoles
    @PutMapping("/profile/address")
    @RequireStatus(User.Status.ACTIVE)
    @Operation(summary = "Update address for user.")
    public static ApiResponse<Void> updateProfile(@Valid @RequestBody final UpdateUserInformationCommand.Request request) {
        return SpringBeans.getBean(UpdateUserInformationCommand.class).execute(request);
    }

    @PutMapping("/profile/password")
    @RequireStatus(User.Status.ACTIVE)
    @Operation(summary = "Update password for user.")
    @RequiredRoles()
    public static ApiResponse<Void> updateProfile(@Valid @RequestBody final UpdateUserPasswordCommand.Request request) {
        return SpringBeans.getBean(UpdateUserPasswordCommand.class).execute(request);
    }

    @RequiredRoles()
    @RequireStatus(User.Status.ACTIVE)
    @PutMapping("/profile/avatar")
    @Operation(summary = "Update address for user.")
    public static ApiResponse<AuthResponse> updateProfile(@Valid @RequestBody final UpdateUserAvatarCommand.Request request) {
        return SpringBeans.getBean(UpdateUserAvatarCommand.class).execute(request);
    }

    @PostMapping("/profile/phone/send-otp")
    @Operation(summary = "Send otp to update phone.")
    public static ApiResponse<String> sentOTPToPhoneToUpdate(@Valid @RequestBody final SendOtpToUpdatePhoneCommand.Request request) {
        return SpringBeans.getBean(SendOtpToUpdatePhoneCommand.class).execute(request);
    }

    @PutMapping("/profile/phone")
    @RequireStatus(User.Status.ACTIVE)
    @Operation(summary = "Update phone number for user.")
    @RequiredRoles
    public static ApiResponse<AuthResponse> updateProfile(@Valid @RequestBody final UpdateUserPhoneNumberCommand.Request request) {
        return SpringBeans.getBean(UpdateUserPhoneNumberCommand.class).execute(request);
    }

    @RequiredRoles()
    @RequireStatus(User.Status.ACTIVE)
    @PutMapping("/profile/email")
    @Operation(summary = "Update email for user.")
    public static ApiResponse<Void> updateProfile(@Valid @RequestBody final UpdateUserEmailCommand.Request request) {
        return SpringBeans.getBean(UpdateUserEmailCommand.class).execute(request);
    }

    @PutMapping("/profile/email/verify")
    @Operation(summary = "Verify update email for user.")
    public static ApiResponse<AuthResponse> updateProfile(@Valid final VerifyUpdateUserEmailCommand.Request request) {
        return SpringBeans.getBean(VerifyUpdateUserEmailCommand.class).execute(request);
    }

    @RequiredRoles(User.Role.ADMIN)
    @RequireStatus(User.Status.ACTIVE)
    @GetMapping("/admin/dashboard")
    @Operation(summary = "View dashboard for admin.")
    public static ApiResponse<AdminDashboardCommand.DashboardResponse> updateProfile() {
        return SpringBeans.getBean(AdminDashboardCommand.class).execute(null);
    }

    @GetMapping("/book-info")
    @RequireStatus(User.Status.ACTIVE)
    @RequiredRoles({User.Role.BUSINESS_ADMIN, User.Role.CUSTOMER})
    @Operation(summary = "Get creator name of statement")
    public static ApiResponse<UserResponse> updateTravelStatement(GetInformationOfBookingUserCommand.Request request) {
        return SpringBeans.getBean(GetInformationOfBookingUserCommand.class).execute(request);
    }

    @PostMapping("/admin")
    @RequiredRoles(User.Role.ADMIN)
    @Operation(summary = "View users for admin")
    public static ApiResponse<ListResult<UserResponse>> viewUsersForAdmin(@Valid @RequestBody final AdminSearchUserCommand.Request request) {
        return SpringBeans.getBean(AdminSearchUserCommand.class).execute(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login using email or phone number")
    public static ApiResponse<AuthResponse> login(@Valid @RequestBody final LoginUsingEmailOrPhoneCommand.LoginRequest request) {
        return SpringBeans.getBean(LoginUsingEmailOrPhoneCommand.class).execute(request);
    }

    @PostMapping("/login/google")
    @Operation(summary = "Login using google")
    public static ApiResponse<AuthResponse> loginWithGoogle(@Valid @RequestBody final LoginUsingGoogleCommand.LoginUsingGoogleRequest request) {
        return SpringBeans.getBean(LoginUsingGoogleCommand.class).execute(request);
    }

    @PostMapping("/login/business")
    @Operation(summary = "Login for business users")
    public static ApiResponse<AuthResponse> loginForBusinessUsers(@Valid @RequestBody final LoginForBusinessUserCommand.LoginRequest request) {
        return SpringBeans.getBean(LoginForBusinessUserCommand.class).execute(request);
    }

    @PostMapping("/login/business/google")
    @Operation(summary = "Login using google for business users")
    public static ApiResponse<AuthResponse> loginWithGoogleForBusinessUsers(@Valid @RequestBody final LoginUsingGoogleForBusinessUserCommand.LoginUsingGoogleRequest request) {
        return SpringBeans.getBean(LoginUsingGoogleForBusinessUserCommand.class).execute(request);
    }

    @PostMapping("/reset-password/otp")
    @Operation(summary = "Send OTP to reset password")
    public static ApiResponse<String> sendOTpToResetPassword(@Valid @RequestBody final SendOtpToResetPasswordCommand.SendOTPResetPasswordRequest request) {
        return SpringBeans.getBean(SendOtpToResetPasswordCommand.class).execute(request);
    }

    @PostMapping("/reset-password/verify")
    @Operation(summary = "Verify reset password OTP")
    public static ApiResponse<String> verifyResetPasswordOtp(@Valid @RequestBody final ResetPasswordOTPVerificationCommand.ConfirmResetOTPRequest request) {
        return SpringBeans.getBean(ResetPasswordOTPVerificationCommand.class).execute(request);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password")
    public static ApiResponse<Void> resetPassword(@Valid @RequestBody final ResetPasswordCommand.ResetPasswordRequest request) {
        return SpringBeans.getBean(ResetPasswordCommand.class).execute(request);
    }


    @PutMapping("/admin/disable")
    @RequiredRoles({User.Role.ADMIN})
    @Operation(summary = "Active/Inactive user")
    public static ApiResponse<Void> userStatus(@Valid @ModelAttribute final ChangeUserStatusRequest request) {
        return SpringBeans.getBean(ChangeUserStatusCommand.class).execute(request);
    }

    @GetMapping("/admin")
    @Operation(summary = "View detailed user for admin")
    public static ApiResponse<UserDetailResponse> viewDetailedUserForAdmin(@Valid @ModelAttribute final AdminViewDetailedUserRequest request) {
        return SpringBeans.getBean(AdminViewDetailedUserCommand.class).execute(request);
    }

    @PostMapping("/register/house-owner/check")
    @Operation(summary = "Check to become house owner")
    public static ApiResponse<Void> checkToHouseOwner(@Valid @RequestBody final CheckToHouseOwnerCommand.Request request) {
        return SpringBeans.getBean(CheckToHouseOwnerCommand.class).execute(request);
    }

    @PostMapping("/register/house-owner")
    @Operation(summary = "Register to become house owner")
    public static ApiResponse<Void> registerToHouseOwner(@Valid @RequestBody final RegisterToHouseOwnerCommand.Request request) {
        return SpringBeans.getBean(RegisterToHouseOwnerCommand.class).execute(request);
    }
}
