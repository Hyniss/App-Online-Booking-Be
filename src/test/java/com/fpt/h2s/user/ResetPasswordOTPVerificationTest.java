//package com.fpt.h2s.user;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fpt.h2s.BaseTest;
//import com.fpt.h2s.models.domains.OTP;
//import com.fpt.h2s.models.entities.User;
//import com.fpt.h2s.repositories.RedisRepository;
//import com.fpt.h2s.services.commands.user.ResetPasswordOTPVerificationCommand.ConfirmResetOTPRequest;
//import com.fpt.h2s.services.commands.user.ResetPasswordSendOTPCommand;
//import com.fpt.h2s.utilities.Mappers;
//import com.fpt.h2s.utilities.MoreStrings;
//import org.assertj.core.api.Assertions;
//import org.jetbrains.annotations.NotNull;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.ResultActions;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//import java.util.HashMap;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class ResetPasswordOTPVerificationTest extends BaseTest {
//
//    public static final String WRONG_OTP = "1234567";
//
//    @Test
//    void should_success() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final String otp = getOtpFor(user);
//        final ConfirmResetOTPRequest request = createOtpRequest(user, otp);
//        this.executeRequest(request).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
//    }
//
//    private static String getOtpFor(final User user) {
//        return RedisRepository.get(user.getEmail() + "-resetPasswordOTP", OTP.class).orElseThrow().getValue();
//    }
//
//    @Test
//    void should_failed_if_email_is_blank() throws Exception {
//        final ConfirmResetOTPRequest request = ConfirmResetOTPRequest
//            .builder()
//            .email("")
//            .otp("otp")
//            .build();
//        final HashMap<String, String> response = this.executeRequestWithResponse(request);
//        final String error = response.get(ConfirmResetOTPRequest.Fields.email);
//        final String message = this.messageResolver.get("EMAIL_BLANK");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_email_is_invalid() throws Exception {
//        final ConfirmResetOTPRequest request = ConfirmResetOTPRequest
//            .builder()
//            .email("asdashdhjashjdahj@")
//            .otp("otp")
//            .build();
//        final HashMap<String, String> response = this.executeRequestWithResponse(request);
//        final String error = response.get(ConfirmResetOTPRequest.Fields.email);
//        final String message = this.messageResolver.get("EMAIL_INVALID");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_email_is_not_found() throws Exception {
//        final ConfirmResetOTPRequest request = ConfirmResetOTPRequest
//            .builder()
//            .email(MoreStrings.randomStringWithLength(12) + "@gmail.com")
//            .otp("otp")
//            .build();
//        final HashMap<String, String> response = this.executeRequestWithResponse(request);
//        final String error = response.get(ConfirmResetOTPRequest.Fields.email);
//        final String message = this.messageResolver.get("EMAIL_NOT_FOUND");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_email_is_banned() throws Exception {
//        final User user = this.createUser(u -> u.status(User.Status.BANNED));
//        final ConfirmResetOTPRequest request = createOtpRequest(user, "otp");
//        final HashMap<String, String> response = this.executeRequestWithResponse(request);
//        final String error = response.get(ConfirmResetOTPRequest.Fields.email);
//        final String message = this.messageResolver.get("USER_IS_BANNED");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_otp_is_blank() throws Exception {
//        final User user = this.createUser();
//        final ConfirmResetOTPRequest request = createOtpRequest(user, "");
//        final HashMap<String, String> response = this.executeRequestWithResponse(request);
//        final String error = response.get(ConfirmResetOTPRequest.Fields.otp);
//        final String message = this.messageResolver.get("OTP_BLANK");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_otp_is_wrong_at_first_try() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final ConfirmResetOTPRequest request = createOtpRequest(user, WRONG_OTP);
////        for (int i = 0; i < 1; i++) {
////            executeRequest(request);
////        }
//        final String error = BaseTest.messageOf(this.executeRequest(request).andReturn());
//        final int TRY_LEFT = 4;
//        final String message = this.messageResolver.get("OTP_WRONG", TRY_LEFT);
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_otp_is_wrong_at_2nd_try() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final ConfirmResetOTPRequest request = createOtpRequest(user, WRONG_OTP);
//        this.tryVerifyFail(request, 1);
//        final String error = BaseTest.messageOf(this.executeRequest(request).andReturn());
//        final int TRY_LEFT = 3;
//        final String message = this.messageResolver.get("OTP_WRONG", TRY_LEFT);
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_otp_is_wrong_at_3rd_try() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final ConfirmResetOTPRequest request = createOtpRequest(user, WRONG_OTP);
//        this.tryVerifyFail(request, 2);
//        final String error = BaseTest.messageOf(this.executeRequest(request).andReturn());
//        final int TRY_LEFT = 2;
//        final String message = this.messageResolver.get("OTP_WRONG", TRY_LEFT);
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_otp_is_wrong_at_4th_try() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final ConfirmResetOTPRequest request = createOtpRequest(user, WRONG_OTP);
//        this.tryVerifyFail(request, 3);
//        final String error = BaseTest.messageOf(this.executeRequest(request).andReturn());
//        final int TRY_LEFT = 1;
//        final String message = this.messageResolver.get("OTP_WRONG", TRY_LEFT);
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_otp_is_wrong_at_5th_try() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final ConfirmResetOTPRequest request = createOtpRequest(user, WRONG_OTP);
//        this.tryVerifyFail(request, 4);
//        final String error = BaseTest.messageOf(this.executeRequest(request).andReturn());
//        final String message = this.messageResolver.get("RESET_PASSWORD_DISABLED");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    private static ConfirmResetOTPRequest createOtpRequest(final User user, final String otp) {
//        return ConfirmResetOTPRequest
//            .builder()
//            .email(user.getEmail())
//            .otp(otp)
//            .build();
//    }
//
//    @Test
//    void should_failed_if_out_of_try() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final ConfirmResetOTPRequest failRequest = createOtpRequest(user, WRONG_OTP);
//        this.tryVerifyFail(failRequest, 5);
//
//        final String error = BaseTest.messageOf(this.executeRequest(failRequest).andReturn());
//        final String message = this.messageResolver.get("RESET_PASSWORD_DISABLED");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_success_after_wrong_1_time() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final ConfirmResetOTPRequest failedRequest = createOtpRequest(user, WRONG_OTP);
//        this.tryVerifyFail(failedRequest, 1);
//        final ConfirmResetOTPRequest successRequest = createOtpRequest(user, getOtpFor(user));
//        this.executeRequest(successRequest).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
//    }
//
//    @Test
//    void should_success_after_wrong_2_time() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final ConfirmResetOTPRequest failedRequest = createOtpRequest(user, WRONG_OTP);
//        this.tryVerifyFail(failedRequest, 2);
//        final ConfirmResetOTPRequest successRequest = createOtpRequest(user, getOtpFor(user));
//        this.executeRequest(successRequest).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
//    }
//
//    @Test
//    void should_success_after_wrong_3_time() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final ConfirmResetOTPRequest failedRequest = createOtpRequest(user, WRONG_OTP);
//        this.tryVerifyFail(failedRequest, 3);
//        final ConfirmResetOTPRequest successRequest = createOtpRequest(user, getOtpFor(user));
//        this.executeRequest(successRequest).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
//    }
//
//    @Test
//    void should_success_after_wrong_4_time() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final ConfirmResetOTPRequest failedRequest = createOtpRequest(user, WRONG_OTP);
//        this.tryVerifyFail(failedRequest, 4);
//        final ConfirmResetOTPRequest successRequest = createOtpRequest(user, getOtpFor(user));
//        this.executeRequest(successRequest).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
//    }
//
//    private void tryVerifyFail(final ConfirmResetOTPRequest request, final int time) throws Exception {
//        for (int i = 0; i < time; i++) {
//            this.executeRequest(request);
//        }
//    }
//
//    private HashMap<String, String> executeRequestWithResponse(final ConfirmResetOTPRequest request) throws Exception {
//        final MvcResult result = this.executeRequest(request).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
//        return BaseTest.responseOf(result, new TypeReference<>() {
//        });
//    }
//
//    @NotNull
//    private ResultActions executeRequest(final ConfirmResetOTPRequest request) throws Exception {
//        return this.mvc
//            .perform(
//                post("/user/reset-password/verify")
//                    .content(Mappers.jsonOf(request))
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .header("X-LOCALE", "en")
//            );
//    }
//
//    void sendOTP(final User user) throws Exception {
//        final ResetPasswordSendOTPCommand.SendOTPResetPasswordRequest request = ResetPasswordSendOTPCommand.SendOTPResetPasswordRequest.builder().email(user.getEmail()).build();
//        this.mvc
//            .perform(
//                post("/user/reset-password/otp")
//                    .content(Mappers.jsonOf(request))
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .header("X-LOCALE", "en")
//            )
//            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
//    }
//
//}
