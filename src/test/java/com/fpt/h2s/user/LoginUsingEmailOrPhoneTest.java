package com.fpt.h2s.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.BaseTest;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.services.commands.user.LoginUsingEmailOrPhoneCommand;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.MoreStrings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginUsingEmailOrPhoneTest extends BaseTest {

    public static final String MAIL_INVALID = "home2stayvn@";
    public static final String MAIL_NOT_FOUND = "aemailthatnotexisted@gmail.com";
    public static final String EMAIL_CORRECT = "home2stayvn@gmail.com      ";
    public static final String PHONE_INVALID = "084840081";
    public static final String PHONE_9_CHARS = "08484008";
    public static final String PHONE_11_CHARS = "08484008100";
    public static final String PHONE_NOT_FOUND = "0848400850";
    public static final String PHONE_CORRECT = "0848400810";
    public static final String PASSWORD_INCORRECT = "H2sisthebestXY";
    public static final String PASSWORD_CORRECT = "H2sisthebest   ";
    public static final String BLANK = "        ";
    public static final String EMPTY = "";
    public static final String ERROR_MAIL_BLANK = "Xin hãy nhập email hoặc số điện thoại";
    public static final String ERROR_MAIL_INVALID = "Email không hợp lệ";
    public static final String ERROR_EMAIL_NOT_FOUND = "Không tìm thấy email.";
    public static final String ERROR_PHONE_INVALID = "Số điện thoại không hợp lệ.";
    public static final String ERROR_PHONE_NOT_FOUND = "Không tìm thấy số điện thoại";
    public static final String ERROR_PASS_BLANK = "Xin hãy nhập mật khẩu";
    public static final String ERROR_PASS_WRONG = "Mật khẩu sai, xin hãy nhập lại";
    public static final String ERROR_USER_BANNED = "Người dùng đã bị khóa";
    public static final String ERROR_USER_PENDING = "Xin hãy kiểm tra email chúng tôi vừa gửi đến bạn để xác thực tài khoản";
    public static final String SUCCESS = "Đăng nhập thành công.";

    @Order(1)
    @Test
    void should_failed_when_email_is_null() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(null)
            .password(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.emailOrPhone);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_MAIL_BLANK);
    }

    @Order(2)
    @Test
    void should_failed_when_email_is_empty() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(EMPTY)
            .password(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.emailOrPhone);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_MAIL_BLANK);
    }

    @Order(3)
    @Test
    void should_failed_when_email_is_blank() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(BLANK)
            .password(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.emailOrPhone);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_MAIL_BLANK);
    }

    @Order(4)
    @Test
    void should_failed_when_email_is_invalid() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(MAIL_INVALID)
            .password(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.emailOrPhone);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_MAIL_INVALID);
    }

    @Order(5)
    @Test
    void should_failed_when_email_is_not_found() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(MAIL_NOT_FOUND)
            .password(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.emailOrPhone);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_EMAIL_NOT_FOUND);
    }

    @Order(6)
    @Test
    void should_failed_when_phone_is_invalid() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(PHONE_INVALID)
            .password(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.emailOrPhone);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PHONE_INVALID);
    }

    @Order(7)
    @Test
    void should_failed_when_phone_is_having_more_than_10_chars() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(PHONE_11_CHARS)
            .password(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.emailOrPhone);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PHONE_INVALID);
    }

    @Order(8)
    @Test
    void should_failed_when_phone_is_having_less_than_9_chars() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(PHONE_9_CHARS)
            .password(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.emailOrPhone);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PHONE_INVALID);
    }

    @Order(9)
    @Test
    void should_failed_when_phone_is_not_found() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(PHONE_NOT_FOUND)
            .password(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.emailOrPhone);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PHONE_NOT_FOUND);
    }

    @Order(10)
    @Test
    void should_failed_when_password_is_null() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(EMAIL_CORRECT)
            .password(null)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_BLANK);
    }

    @Order(11)
    @Test
    void should_failed_when_password_is_empty() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(EMAIL_CORRECT)
            .password(EMPTY)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_BLANK);
    }

    @Order(12)
    @Test
    void should_failed_when_password_is_blank() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(EMAIL_CORRECT)
            .password(BLANK)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_BLANK);
    }

    @Order(13)
    @Test
    void should_failed_when_password_is_wrong() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(EMAIL_CORRECT)
            .password(PASSWORD_INCORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_WRONG);
    }

    @Order(14)
    @Test
    void should_failed_when_user_is_banned() throws Exception {
        final User user = this.createUser(u -> u.password(PASSWORD_CORRECT).status(User.Status.BANNED));

        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(user.getEmail())
            .password(PASSWORD_CORRECT)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(LoginUsingEmailOrPhoneCommand.LoginRequest.Fields.emailOrPhone);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_USER_BANNED);
    }

    @Order(15)
    @Test
    void should_failed_when_user_is_pending() throws Exception {
        userRepository.findByPhoneEndingWith("0321392106").ifPresent((u) -> userRepository.delete(u));
        final User user = this.createUser(u -> u.phone("0321392106").password(PASSWORD_CORRECT).status(User.Status.PENDING));

        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(user.getPhone())
            .password(PASSWORD_CORRECT)
            .build();

        final MvcResult result = this.mvc
            .perform(
                post("/user/login")
                    .content(Mappers.jsonOf(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-LOCALE", "en")
                    .header("X-FROM", "http://localhost:3000/")
            )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        final String error = BaseTest.messageOf(result);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_USER_PENDING);
    }

    private HashMap<String, String> executeFailRequest(final LoginUsingEmailOrPhoneCommand.LoginRequest request) throws Exception {
        final MvcResult result = this.mvc
            .perform(
                post("/user/login")
                    .content(Mappers.jsonOf(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-LOCALE", "en")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        return BaseTest.responseOf(result, new TypeReference<>() {
        });
    }

    @Order(16)
    @Test
    void should_success_login_using_email() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(EMAIL_CORRECT)
            .password(PASSWORD_CORRECT)
            .build();
        final MvcResult result = this.mvc
            .perform(
                post("/user/login")
                    .content(Mappers.jsonOf(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-LOCALE", "en")
                    .header("X-FROM", "http://localhost:3000/")
            )
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final String message = BaseTest.messageOf(result);
        Assertions.assertThat(MoreStrings.utf8Of(message)).isEqualTo(SUCCESS);
    }

    @Order(17)
    @Test
    void should_success_login_using_phone() throws Exception {
        final LoginUsingEmailOrPhoneCommand.LoginRequest request = LoginUsingEmailOrPhoneCommand.LoginRequest
            .builder()
            .emailOrPhone(PHONE_CORRECT)
            .password(PASSWORD_CORRECT)
            .build();
        final MvcResult result = this.mvc
            .perform(
                post("/user/login")
                    .content(Mappers.jsonOf(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-LOCALE", "en")
                    .header("X-FROM", "http://localhost:3000/")
            )
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final String message = BaseTest.messageOf(result);
        Assertions.assertThat(MoreStrings.utf8Of(message)).isEqualTo(SUCCESS);
    }

}
