package com.fpt.h2s.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.BaseTest;
import com.fpt.h2s.TestUserInfo;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.services.commands.user.RegisterUsingEmailCommand;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegisterUsingEmailTest extends BaseTest {

    public static final String URL = "/user/register/email";
    public static final String EMPTY = "";
    public static final String BLANK = "";
    public static final String EMAIL_INVALID = "home2stayvn@";
    public static final String EMAIL_EXISTED = "home2stayvn@gmail.com";
    public static final String EMAIL_CORRECT = "sonddhe151113@fpt.edu.vn    ";
    public static final String USERNAME_WRONG_2_CHAR = "Ha";
    public static final String USERNAME_WRONG_3_CHAR = "Nam";
    public static final String USERNAME_WRONG_BOUNDARY_MAX = "Hoàng Thị Long Lanh Kim Ánh Dương";
    public static final String USERNAME_WRONG_NORMAL = "Nguyễn Thị Long Lanh Kim Ánh Dương";
    public static final String USERNAME_WRONG_WITH_SPECIAL = "Nguyễn Thị Ánh Dương @";
    public static final String USERNAME_EXISTED = "Admin";
    public static final String USERNAME_CORRECT_MIN = "Linh";
    public static final String USERNAME_CORRECT_MAX = "Phạm Thị Long Lanh Kim Ánh Dương    ";
    public static final String USERNAME_CORRECT_WITH_NUMBER = "Nguyễn Thị Ánh Dương 1";
    public static final String PASSWORD_WRONG_5_CHAR = "Hello";
    public static final String PASSWORD_WRONG_MIN_BOUNDARY = "Hello12";
    public static final String PASSWORD_CORRECT = "Hello123";
    public static final String PASSWORD_WRONG_MAX_NORMAL = PASSWORD_CORRECT.repeat(10).substring(0, 40);
    public static final String PASSWORD_WRONG_MAX_BOUNDARY = PASSWORD_CORRECT.repeat(10).substring(0, 33);
    public static final String PASSWORD_CORRECT_MAX = PASSWORD_CORRECT.repeat(10).substring(0, 32);
    public static final String PASSWORD_WRONG_NO_DIGIT = "HelloHello";
    public static final String PASSWORD_WRONG_NO_UPPER = "hello123";
    public static final String PASSWORD_WRONG_NO_LOWER = "HELLO123";
    public static final String ERROR_EMAIL_BLANK = "Xin hãy nhập email";
    public static final String ERROR_EMAIL_INVALID = "Email không hợp lệ";
    public static final String ERROR_EMAIL_USED = "Email đã được sử dụng, xin hãy sử dụng email khác";
    public static final String ERROR_USERNAME_BLANK = "Xin hãy nhập tên người dùng";
    public static final String ERROR_USERNAME_INVALID = "Tên người dùng chỉ chứa a-z, A-Z, khoảng trắng và có độ dài từ 4 đến 32 kí tự";
    public static final String ERROR_PASS_BLANK = "Xin hãy nhập mật khẩu.";
    public static final String ERROR_PASS_INVALID = "Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự";
    public static final String ERROR_CONFIRM_PASSWORD_BLANK = "Xin hãy nhập mật khẩu xác nhận";
    public static final String ERROR_PASS_DIFF = "Mật khẩu xác nhận phải khớp với mật khẩu";

    @Test
    @Order(1)
    void should_failed_if_email_is_null() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(null)
            .username(info.name())
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.email);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_EMAIL_BLANK);
    }

    @Test
    @Order(2)
    void should_failed_if_email_is_empty() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMPTY)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.email);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_EMAIL_BLANK);
    }

    @Test
    @Order(3)
    void should_failed_if_email_is_blank() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(" ".repeat(5))
            .username(info.name())
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.email);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_EMAIL_BLANK);
    }

    @Test
    @Order(4)
    void should_failed_if_email_is_invalid() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(info.name())
            .username(info.name())
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.email);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_EMAIL_INVALID);
    }

    @Test
    @Order(5)
    void should_failed_if_email_is_existed() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final String existedEmail = this.userRepository.findAll().stream().findAny().map(User::getEmail).orElse(null);
        if (existedEmail == null) {
            return;
        }
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(existedEmail)
            .username(info.name())
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.email);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_EMAIL_USED);
    }

    @Test
    @Order(6)
    void should_failed_if_username_is_null() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .username(null)
            .email(info.email())
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_USERNAME_BLANK);
    }

    @Test
    @Order(7)
    void should_failed_if_username_is_empty() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .username("")
            .email(info.email())
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_USERNAME_BLANK);
    }

    @Test
    @Order(8)
    void should_failed_if_username_is_blank() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .username(" ".repeat(5))
            .email(info.email())
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_USERNAME_BLANK);
    }

    @Test
    @Order(9)
    void should_failed_if_username_have_length_less_than_min_length() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(info.email())
            .username(USERNAME_WRONG_2_CHAR)
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_USERNAME_INVALID);
    }

    @Test
    @Order(10)
    void should_failed_if_username_have_length_less_than_min_length_at_boundary() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(info.email())
            .username(USERNAME_WRONG_3_CHAR)
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();

         final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_USERNAME_INVALID);
    }

    @Test
    @Order(11)
    void should_ok_if_username_have_length_is_min_length() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(info.email())
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();

        deleteUserIfFound(request);
        this.executeSuccessRequest(request);

        User createdUser = userRepository.findByEmail(info.email()).orElseThrow(() -> ApiException.notFound("User not found"));
        Assertions.assertThat(createdUser.getEmail()).isEqualTo(request.getEmail().trim());
        Assertions.assertThat(createdUser.getUsername()).isEqualTo(request.getUsername().trim());
        Assertions.assertThat(createdUser.roleList()).isEqualTo(List.of(User.Role.CUSTOMER));
        Assertions.assertThat(passwordEncoder.matches(request.getPassword(), createdUser.getPassword())).isTrue();
    }

    @Test
    @Order(12)
    void should_ok_if_username_have_length_is_max_length() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(info.email())
            .username(USERNAME_CORRECT_MAX)
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();

        deleteUserIfFound(request);
        this.executeSuccessRequest(request);

        User createdUser = userRepository.findByEmail(info.email()).orElseThrow(() -> ApiException.notFound("User not found"));
        Assertions.assertThat(createdUser.getEmail()).isEqualTo(request.getEmail().trim());
        Assertions.assertThat(createdUser.getUsername()).isEqualTo(request.getUsername().trim());
        Assertions.assertThat(createdUser.roleList()).isEqualTo(List.of(User.Role.CUSTOMER));
        Assertions.assertThat(passwordEncoder.matches(request.getPassword(), createdUser.getPassword())).isTrue();
    }

    private void deleteUserIfFound(RegisterUsingEmailCommand.Request request) {
        userRepository.findByUsername(request.getUsername().trim()).ifPresent(u -> userRepository.save(u.withUsername(MoreStrings.randomStringWithLength(32))));
        userRepository.findByEmail(request.getEmail().trim()).ifPresent(u -> userRepository.save(u.withEmail(MoreStrings.randomStringWithLength(32) + "@gmail.com")));
    }

    @Test
    @Order(13)
    void should_failed_if_username_have_length_more_than_max_length_at_boundary() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(info.email())
            .username(USERNAME_WRONG_BOUNDARY_MAX)
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_USERNAME_INVALID);
    }

    @Test
    @Order(14)
    void should_failed_if_username_have_length_more_than_length() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(info.email())
            .username(USERNAME_WRONG_NORMAL)
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_USERNAME_INVALID);
    }

    @Test
    @Order(15)
    void should_failed_if_username_have_digit() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(info.email())
            .username(USERNAME_CORRECT_WITH_NUMBER)
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();

        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_USERNAME_INVALID);
    }
    @Test
    @Order(16)
    void should_failed_if_username_have_special_char() throws Exception {
        final TestUserInfo info = TestUserInfo.generate();

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(info.email())
            .username(USERNAME_WRONG_WITH_SPECIAL)
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        deleteUserIfFound(request);

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_USERNAME_INVALID);
    }

    @Test
    @Order(17)
    void should_failed_if_password_is_null() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(null)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_BLANK);
    }

    @Test
    @Order(18)
    void should_failed_if_password_is_empty() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(EMPTY)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_BLANK);
    }

    @Test
    @Order(19)
    void should_failed_if_password_is_blank() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(BLANK)
            .confirmPassword(PASSWORD_CORRECT)
            .build();

        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_BLANK);
    }



    @Test
    @Order(20)
    void should_failed_if_password_have_less_than_min_length() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_WRONG_5_CHAR)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_INVALID);
    }

    @Test
    @Order(21)
    void should_failed_if_password_have_less_than_min_length_boundary() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_WRONG_MIN_BOUNDARY)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_INVALID);
    }

    @Test
    @Order(22)
    void should_ok_if_password_have_min_length() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();

        deleteUserIfFound(request);
        this.executeSuccessRequest(request);

        User createdUser = userRepository.findByEmail(EMAIL_CORRECT.trim()).orElseThrow(() -> ApiException.notFound("User not found"));
        Assertions.assertThat(createdUser.getEmail()).isEqualTo(request.getEmail().trim());
        Assertions.assertThat(createdUser.getUsername()).isEqualTo(request.getUsername().trim());
        Assertions.assertThat(createdUser.roleList()).isEqualTo(List.of(User.Role.CUSTOMER));
        Assertions.assertThat(passwordEncoder.matches(request.getPassword(), createdUser.getPassword())).isTrue();
    }

    @Test
    @Order(23)
    void should_ok_if_password_have_max_length() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_CORRECT_MAX)
            .confirmPassword(PASSWORD_CORRECT_MAX)
            .build();
        deleteUserIfFound(request);
        this.executeSuccessRequest(request);
    }

    @Test
    @Order(24)
    void should_failed_if_password_have_more_than_max_length_boundary() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_WRONG_MAX_BOUNDARY)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_INVALID);
    }

    @Test
    @Order(25)
    void should_failed_if_password_have_more_than_max_length() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_WRONG_MAX_NORMAL)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_INVALID);
    }

    @Test
    @Order(26)
    void should_failed_if_password_have_no_digits() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_WRONG_NO_DIGIT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_INVALID);
    }

    @Test
    @Order(27)
    void should_failed_if_password_have_no_uppercase_letter() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_WRONG_NO_UPPER)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_INVALID);
    }

    @Test
    @Order(28)
    void should_failed_if_password_have_no_normalcase_letter() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_WRONG_NO_LOWER)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_INVALID);
    }

    @Test
    @Order(29)
    void should_failed_if_confirm_password_is_null() throws Exception {

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_CORRECT)
            .confirmPassword(null)
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.confirmPassword);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_CONFIRM_PASSWORD_BLANK);
    }

    @Test
    @Order(30)
    void should_failed_if_confirm_password_is_empty() throws Exception {

        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_CORRECT)
            .confirmPassword("")
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.confirmPassword);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_CONFIRM_PASSWORD_BLANK);
    }

    @Test
    @Order(31)
    void should_failed_if_confirm_password_is_blank() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_CORRECT)
            .confirmPassword(" ".repeat(5))
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.confirmPassword);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_CONFIRM_PASSWORD_BLANK);
    }

    @Test
    @Order(32)
    void should_failed_if_confirm_password_is_not_match() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_CORRECT)
            .confirmPassword("Hello123X")
            .build();
        deleteUserIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterUsingEmailCommand.Request.Fields.confirmPassword);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo(ERROR_PASS_DIFF);
    }

    @Test
    @Order(33)
    void should_success() throws Exception {
        final RegisterUsingEmailCommand.Request request = RegisterUsingEmailCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_MIN)
            .password(PASSWORD_CORRECT)
            .confirmPassword(PASSWORD_CORRECT)
            .build();
        final MvcResult result = this.mvc
            .perform(
                post(URL)
                    .content(Mappers.jsonOf(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-FROM", "http://localhost:3000/")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final String message = MoreStrings.utf8Of(BaseTest.messageOf(result));
        Assertions.assertThat(message).isEqualTo("Bạn đã đăng ký thành công. Xin hãy kiểm tra email của bạn để xác thực tài khoản");

        User createdUser = userRepository.findByEmail(request.getEmail().trim()).orElseThrow(() -> ApiException.notFound("User not found"));

        Assertions.assertThat(createdUser.getEmail()).isEqualTo(request.getEmail().trim());
        Assertions.assertThat(createdUser.getUsername()).isEqualTo(request.getUsername().trim());
        Assertions.assertThat(createdUser.roleList()).isEqualTo(List.of(User.Role.CUSTOMER));
        Assertions.assertThat(passwordEncoder.matches(request.getPassword(), createdUser.getPassword())).isTrue();
    }

    private HashMap<String, String> executeFailRequest(final RegisterUsingEmailCommand.Request request) throws Exception {
        final MvcResult result = this.mvc
            .perform(
                post(URL)
                    .content(Mappers.jsonOf(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-LOCALE", "en")
                    .header("X-FROM", "http://localhost:3000/")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
        return BaseTest.responseOf(result, new TypeReference<>() {
        });
    }

    private void executeSuccessRequest(final RegisterUsingEmailCommand.Request request) throws Exception {
        this.mvc
            .perform(
                post(URL)
                    .content(Mappers.jsonOf(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-LOCALE", "en")
                    .header("X-FROM", "http://localhost:3000/")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
    }
}
