//package com.fpt.h2s.user;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fpt.h2s.BaseTest;
//import com.fpt.h2s.models.entities.User;
//import com.fpt.h2s.services.commands.user.RegisterUsingEmailCommand;
//import com.fpt.h2s.services.commands.user.ResetPasswordOTPVerificationCommand.ConfirmResetOTPRequest;
//import com.fpt.h2s.services.commands.user.ResetPasswordSendOTPCommand.SendOTPResetPasswordRequest;
//import com.fpt.h2s.utilities.Mappers;
//import com.fpt.h2s.utilities.MoreStrings;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//import java.util.HashMap;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class SendResetPasswordOTPTest extends BaseTest {
//    @Test
//    void should_success() throws Exception {
//        final User user = this.createUser();
//        final SendOTPResetPasswordRequest request = SendOTPResetPasswordRequest.builder().email(user.getEmail()).build();
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
//    void should_failed_if_email_is_blank() throws Exception {
//        final User user = this.createUser();
//        final SendOTPResetPasswordRequest request = SendOTPResetPasswordRequest.builder().email("").build();
//        final HashMap<String, String> response = this.executeRequest(request);
//        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.email);
//        final String message = this.messageResolver.get("EMAIL_BLANK");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_email_is_invalid() throws Exception {
//        final User user = this.createUser();
//        final SendOTPResetPasswordRequest request = SendOTPResetPasswordRequest.builder().email("adsad2das@").build();
//        final HashMap<String, String> response = this.executeRequest(request);
//        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.email);
//        final String message = this.messageResolver.get("EMAIL_INVALID");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_email_is_not_found() throws Exception {
//        final User user = this.createUser();
//        final SendOTPResetPasswordRequest request = SendOTPResetPasswordRequest.builder().email(MoreStrings.randomStringWithLength(12) + "@gmail.com").build();
//        final HashMap<String, String> response = this.executeRequest(request);
//        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.email);
//        final String message = this.messageResolver.get("EMAIL_NOT_FOUND");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_email_is_banned() throws Exception {
//        final User user = this.createUser(u -> u.status(User.Status.BANNED));
//        final SendOTPResetPasswordRequest request = SendOTPResetPasswordRequest.builder().email(user.getEmail()).build();
//        final HashMap<String, String> response = this.executeRequest(request);
//        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.email);
//        final String message = this.messageResolver.get("USER_IS_BANNED");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    @Test
//    void should_failed_if_email_is_out_of_try() throws Exception {
//        final User user = this.createUser(u -> u.status(User.Status.BANNED));
//        final SendOTPResetPasswordRequest request = SendOTPResetPasswordRequest.builder().email(user.getEmail()).build();
//        final ConfirmResetOTPRequest verifyRequest = ConfirmResetOTPRequest.builder().email(user.getEmail()).otp("1231y312631").build();
//
//        for (int i = 0; i < 6; i++) {
//            this.mvc
//                .perform(
//                    post("/user/reset-password/verify")
//                        .content(Mappers.jsonOf(verifyRequest))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("X-LOCALE", "en")
//                );
//        }
//
//        final HashMap<String, String> response = this.executeRequest(request);
//        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.email);
//        final String message = this.messageResolver.get("USER_IS_BANNED");
//        Assertions.assertThat(error).isEqualTo(message);
//    }
//
//    private HashMap<String, String> executeRequest(final SendOTPResetPasswordRequest request) throws Exception {
//        final MvcResult result = this.mvc
//            .perform(
//                post("/user/reset-password/otp")
//                    .content(Mappers.jsonOf(request))
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .header("X-LOCALE", "en")
//            )
//            .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
//        return BaseTest.responseOf(result, new TypeReference<>() {
//        });
//    }
//}
