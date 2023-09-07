//package com.fpt.h2s.user;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fpt.h2s.BaseTest;
//import com.fpt.h2s.models.domains.OTP;
//import com.fpt.h2s.models.entities.User;
//import com.fpt.h2s.repositories.RedisRepository;
//import com.fpt.h2s.services.commands.user.ResetPasswordCommand.ResetPasswordRequest;
//import com.fpt.h2s.services.commands.user.ResetPasswordOTPVerificationCommand;
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
//import static com.fpt.h2s.services.commands.user.ResetPasswordOTPVerificationCommand.MAX_TRY;
//import static com.fpt.h2s.services.commands.user.ResetPasswordOTPVerificationCommand.otpRetryMapToUserId;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class ResetPasswordTest extends BaseTest {
//    @Test
//    void should_success() throws Exception {
//        final User user = this.createUser();
//        this.sendOTP(user);
//        final String token = this.getVerifiedToken(user);
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .password("Hi111111111")
//            .confirmPassword("Hi111111111")
//            .email(user.getEmail())
//            .verificationToken(token).build();
//        this.executeApi(request).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
//    }
//
//    @NotNull
//    private ResultActions executeApi(final ResetPasswordRequest request) throws Exception {
//        return this.mvc
//            .perform(
//                post("/user/reset-password")
//                    .content(Mappers.jsonOf(request))
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .header("X-LOCALE", "en")
//            );
//    }
//
//    private String getVerifiedToken(final User user) throws Exception {
//        final String otp = RedisRepository.get(user.getEmail() + "-resetPasswordOTP", OTP.class).orElseThrow().getValue();
//        final ResetPasswordOTPVerificationCommand.ConfirmResetOTPRequest request = ResetPasswordOTPVerificationCommand.ConfirmResetOTPRequest
//            .builder()
//            .email(user.getEmail())
//            .otp(otp)
//            .build();
//
//        final MvcResult verifyResult = this.mvc
//            .perform(
//                post("/user/reset-password/verify")
//                    .content(Mappers.jsonOf(request))
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .header("X-LOCALE", "en")
//            )
//            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
//        final HashMap<String, String> verifyOtpResult = BaseTest.responseOf(verifyResult, new TypeReference<>() {
//        });
//        return verifyOtpResult.get(ResetPasswordRequest.Fields.verificationToken);
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
//    @Test
//    void should_failed_when_email_is_blank() throws Exception {
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .email("")
//            .verificationToken("")
//            .password("Hi111111111")
//            .confirmPassword("Hi111111111")
//            .build();
//        final HashMap<String, String> response = BaseTest.responseOf(this.executeApi(request).andReturn(), new TypeReference<>() {
//        });
//        final String error = response.get(ResetPasswordRequest.Fields.email);
//        final String message = this.messageResolver.get("EMAIL_BLANK");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_when_email_is_invalid() throws Exception {
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .email("asdamskdbahushu@")
//            .verificationToken("")
//            .password("Hi111111111")
//            .confirmPassword("Hi111111111")
//            .build();
//        final HashMap<String, String> response = BaseTest.responseOf(this.executeApi(request).andReturn(), new TypeReference<>() {
//        });
//        final String error = response.get(ResetPasswordRequest.Fields.email);
//        final String message = this.messageResolver.get("EMAIL_INVALID");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_when_email_is_not_found() throws Exception {
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .email(MoreStrings.randomStringWithLength(12) + "@gmail.com")
//            .verificationToken("")
//            .password("Hi111111111")
//            .confirmPassword("Hi111111111")
//            .build();
//        final HashMap<String, String> response = BaseTest.responseOf(this.executeApi(request).andReturn(), new TypeReference<>() {
//        });
//        final String error = response.get(ResetPasswordRequest.Fields.email);
//        final String message = this.messageResolver.get("EMAIL_NOT_FOUND");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_when_email_is_banned() throws Exception {
//        final User user = this.createUser(u -> u.status(User.Status.BANNED));
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .email(user.getEmail())
//            .verificationToken("")
//            .password("Hi111111111")
//            .confirmPassword("Hi111111111")
//            .build();
//        final HashMap<String, String> response = BaseTest.responseOf(this.executeApi(request).andReturn(), new TypeReference<>() {
//        });
//        final String error = response.get(ResetPasswordRequest.Fields.email);
//        final String message = this.messageResolver.get("USER_IS_BANNED");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_when_email_is_out_of_try() throws Exception {
//        final User user = this.createUser();
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .email(user.getEmail())
//            .verificationToken("")
//            .password("Hi111111111")
//            .confirmPassword("Hi111111111")
//            .build();
//        otpRetryMapToUserId.put(user.getId(), MAX_TRY);
//        final HashMap<String, String> response = BaseTest.responseOf(this.executeApi(request).andReturn(), new TypeReference<>() {
//        });
//        final String error = response.get(ResetPasswordRequest.Fields.email);
//        final String message = this.messageResolver.get("RESET_PASSWORD_DISABLED");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_when_verification_token_is_blank() throws Exception {
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .email("")
//            .verificationToken("")
//            .password("Hi111111111")
//            .confirmPassword("Hi111111111")
//            .build();
//        final HashMap<String, String> response = BaseTest.responseOf(this.executeApi(request).andReturn(), new TypeReference<>() {
//        });
//        final String error = response.get(ResetPasswordRequest.Fields.verificationToken);
//        final String message = "Please fill verification token";
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_when_verification_token_is_invalid() throws Exception {
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .email("")
//            .verificationToken("1321312")
//            .password("Hi111111111")
//            .confirmPassword("Hi111111111")
//            .build();
//        final HashMap<String, String> response = BaseTest.responseOf(this.executeApi(request).andReturn(), new TypeReference<>() {
//        });
//        final String error = response.get(ResetPasswordRequest.Fields.verificationToken);
//        final String message = "Token is invalid";
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_when_password_is_blank() throws Exception {
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .email("")
//            .verificationToken("1321312")
//            .password("")
//            .confirmPassword("Hi111111111")
//            .build();
//        final HashMap<String, String> response = BaseTest.responseOf(this.executeApi(request).andReturn(), new TypeReference<>() {
//        });
//        final String error = response.get(ResetPasswordRequest.Fields.password);
//        final String message = messageResolver.get("PASSWORD_BLANK");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_when_password_is_invalid() throws Exception {
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .email("")
//            .verificationToken("1321312")
//            .password("12311111111111111111")
//            .confirmPassword("Hi111111111")
//            .build();
//        final HashMap<String, String> response = BaseTest.responseOf(this.executeApi(request).andReturn(), new TypeReference<>() {
//        });
//        final String error = response.get(ResetPasswordRequest.Fields.password);
//        final String message = messageResolver.get("PASSWORD_FORMAT_INVALID");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_when_confirm_password_is_invalid() throws Exception {
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .email("")
//            .verificationToken("1321312")
//            .password("Hi1111111111111111")
//            .confirmPassword("")
//            .build();
//        final HashMap<String, String> response = BaseTest.responseOf(this.executeApi(request).andReturn(), new TypeReference<>() {
//        });
//        final String error = response.get(ResetPasswordRequest.Fields.confirmPassword);
//        final String message = messageResolver.get("CONFIRM_PASSWORD_BLANK");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_when_confirm_password_is_mismatch() throws Exception {
//        final ResetPasswordRequest request = ResetPasswordRequest.builder()
//            .email("")
//            .verificationToken("1321312")
//            .password("Hi1111111111111111")
//            .confirmPassword("Hi111111111111")
//            .build();
//        final HashMap<String, String> response = BaseTest.responseOf(this.executeApi(request).andReturn(), new TypeReference<>() {
//        });
//        final String error = response.get(ResetPasswordRequest.Fields.confirmPassword);
//        final String message = messageResolver.get("CONFIRM_PASSWORD_MISMATCH");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//}
