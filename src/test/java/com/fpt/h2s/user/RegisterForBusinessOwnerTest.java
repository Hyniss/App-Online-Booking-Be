package com.fpt.h2s.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.BaseTest;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.services.commands.user.RegisterForBusinessOwnerCommand;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.MoreStrings;
import com.fpt.h2s.utilities.SpringBeans;
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
public class RegisterForBusinessOwnerTest extends BaseTest {

    private final String EMPTY = "";
    private final String BLANK = "   ";

    private final String EMAIL_INVALID = "businessOwner@";
    private final String EMAIL_EXISTED = "existedBusinessOwner@gmail.com";
    private final String EMAIL_CORRECT = "businessOwner@gmail.com";

    private final String USERNAME_WRONG_2 = "Ha";
    private final String USERNAME_WRONG_3 = "Nam";
    private final String USERNAME_CORRECT_4 = "Linh   ";
    private final String USERNAME_CORRECT_32 = "Phạm Thị Long Lanh Kim Ánh Dương   " ;
    private final String USERNAME_INCORRECT_33 = "Hoàng Thị Long Lanh Kim Ánh Dương" ;
    private final String USERNAME_INCORRECT_34 = "Nguyễn Thị Long Lanh Kim Ánh Dương" ;

    private final String USERNAME_INCORRECT_FORMAT_1 = "Nguyễn Thị Ánh Dương 1" ;

    private final String USERNAME_INCORRECT_FORMAT_2 = "Nguyễn Thị Ánh Dương @" ;

    private final String PASSWORD_WRONG_5 = "Hello" ;
    private final String PASSWORD_WRONG_7 = "Hello12" ;
    private final String PASSWORD_CORRECT_8 = "Hello123   " ;

    private final String PASSWORD_CORRECT_32 = "Hello123".repeat(10).substring(0, 32) ;
    private final String PASSWORD_INCORRECT_33 = "Hello123".repeat(10).substring(0, 33) ;
    private final String PASSWORD_INCORRECT_40 = "Hello123".repeat(10).substring(0, 40) ;

    private final String CONF_PASS_DIFF = "Hello123X";
    private final String CONF_PASS_CORRECT = "Hello123   ";
    private final Company.Size COMPANY_SIZE = Company.Size.MEDIUM;

    private final String COMPANY_NAME_WRONG_1 = "F";

    private final String COMPANY_NAME_WRONG_2 = "FS";
    private final String COMPANY_NAME_CORRECT_3 = "FPT";
    private final String COMPANY_NAME_CORRECT_255 = "Rikkeisoft".repeat(50).substring(0, 255);
    private final String COMPANY_NAME_INCORRECT_256 = "Rikkeisoft".repeat(50).substring(0, 256);
    private final String COMPANY_NAME_INCORRECT_300 = "Rikkeisoft".repeat(50).substring(0, 300);
    private final String COMPANY_NAME_EXISTED = "Toprate";

    private final String ADDRESS_WRONG_NAME_2 = "HN";
    private final String ADDRESS_WRONG_NAME_4 = "TPHN";
    private final String ADDRESS_CORRECT_NAME_5 = "TPHCM    ";
    private final String ADDRESS_CORRECT_NAME_255 = "Xã Thạch Hoà, huyện Thạch Thất".repeat(50).substring(0, 255);
    private final String ADDRESS_INCORRECT_NAME_256 = "Xã Thạch Hoà, huyện Thạch Thất".repeat(50).substring(0, 256);
    private final String ADDRESS_INCORRECT_NAME_300 = "Xã Thạch Hoà, huyện Thạch Thất".repeat(50).substring(0, 300);

    private final String TAX_WRONG_8 = "12345678";
    private final String TAX_WRONG_9 = "123456789";
    private final String TAX_CORRECT = "0123456789  ";
    private final String TAX_WRONG_11 = "01234567890";
    private final String TAX_WRONG_15 = "0123456789012345";
    private final String TAX_WRONG_FORMAT = "D123@AS011A";

    private final String CONTACT_NAME_WRONG_2 = "Ha";
    private final String CONTACT_NAME_WRONG_3 = "Nam";
    private final String CONTACT_NAME_CORRECT_4 = "Linh   ";
    private final String CONTACT_NAME_CORRECT_32 = "Phạm Thị Long Lanh Kim Ánh Dương   " ;
    private final String CONTACT_NAME_INCORRECT_33 = "Hoàng Thị Long Lanh Kim Ánh Dương" ;
    private final String CONTACT_NAME_INCORRECT_34 = "Nguyễn Thị Long Lanh Kim Ánh Dương" ;

    private final String CONTACT_NAME_INVALID_FORMAT_1 = "Nguyễn Thị Ánh Dương 1" ;
    private final String CONTACT_NAME_INVALID_FORMAT_2 = "Nguyễn Thị Ánh Dương @" ;

    private final String CONTACT_NUMBER_INCORRECT_8 = "84840081" ;
    private final String CONTACT_NUMBER_INCORRECT_11 = "08484008100" ;
    private final String CONTACT_NUMBER_CORRECT_9 = "848400810" ;
    private final String CONTACT_NUMBER_CORRECT_10 = "0848400810" ;
    private final String CONTACT_NUMBER_INCORRECT_FORMAT = "08484AA810" ;

    @Order(1)
    @Test
    void should_failed_when_email_is_null() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(null)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.email);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập email");
    }

    @Order(2)
    @Test
    void should_failed_when_email_is_empty() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMPTY)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.email);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập email");
    }

    @Order(3)
    @Test
    void should_failed_when_email_is_blank() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(BLANK)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.email);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập email");
    }

    @Order(4)
    @Test
    void should_failed_when_email_is_invalid() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_INVALID)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        deleteUserAndCompanyIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.email);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Email không hợp lệ");
    }

    @Order(5)
    @Test
    void should_failed_when_email_is_existed() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_EXISTED)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        deleteUserAndCompanyIfFound(request);
        this.executeSuccessRequest(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.email);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Email đã được sử dụng, xin hãy sử dụng email khác");
    }

    @Order(6)
    @Test
    void should_failed_when_username_is_null() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(null)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        deleteUserAndCompanyIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập tên người dùng");
    }

    private void deleteUserAndCompanyIfFound(RegisterForBusinessOwnerCommand.Request request, boolean deleteCompany) {
        try {
            userRepository.findByEmail(request.getEmail().trim()).ifPresent(
                u -> userRepository.save(u.withUsername(MoreStrings.randomStringWithLength(32)).withEmail(MoreStrings.randomStringWithLength(32) +"@gmail.com"))
            );
            userRepository.findByUsername(request.getUsername().trim()).ifPresent(
                u -> userRepository.save(u.withUsername(MoreStrings.randomStringWithLength(32)).withEmail(MoreStrings.randomStringWithLength(32) +"@gmail.com"))
            );

        if (deleteCompany) {
            CompanyRepository companyRepository = SpringBeans.getBean(CompanyRepository.class);
            companyRepository.findByName(request.getCompanyName().trim())
                .ifPresent(company -> companyRepository.save(
                    company
                        .withCreatorId(userRepository.findAll().stream().findFirst().orElseThrow().getId())
                        .withName(MoreStrings.randomStringWithLength(32))
                ));
        }
        } catch (Exception ignored) {

        }
    }

    private void deleteUserAndCompanyIfFound(RegisterForBusinessOwnerCommand.Request request) {
        deleteUserAndCompanyIfFound(request, true);
    }

    @Order(7)
    @Test
    void should_failed_when_username_is_empty() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(EMPTY)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập tên người dùng");
    }

    @Order(8)
    @Test
    void should_failed_when_username_is_blank() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(BLANK)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập tên người dùng");
    }


    @Order(9)
    @Test
    void should_failed_when_username_is_2_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_WRONG_2)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên người dùng phải có độ dài từ 4 đến 32 kí tự");
    }

    @Order(10)
    @Test
    void should_failed_when_username_is_3_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_WRONG_3)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên người dùng phải có độ dài từ 4 đến 32 kí tự");
    }

    @Order(11)
    @Test
    void should_success_when_username_is_4_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        deleteUserAndCompanyIfFound(request);
        this.executeSuccessRequest(request);
    }

    @Order(12)
    @Test
    void should_failed_when_username_is_33_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_INCORRECT_33)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên người dùng phải có độ dài từ 4 đến 32 kí tự");
    }

    @Order(13)
    @Test
    void should_failed_when_username_is_34_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_INCORRECT_34)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên người dùng phải có độ dài từ 4 đến 32 kí tự");
    }

    @Order(14)
    @Test
    void should_failed_when_username_have_number() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_INCORRECT_FORMAT_1)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên người dùng chỉ chứa a-z, A-Z và khoảng trắng");
    }
    @Order(15)
    @Test
    void should_failed_when_username_have_specical() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_INCORRECT_FORMAT_2)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.username);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên người dùng chỉ chứa a-z, A-Z và khoảng trắng");
    }

    @Order(16)
    @Test
    void should_failed_when_pass_is_null() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(null)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập mật khẩu.");
    }

    @Order(17)
    @Test
    void should_failed_when_pass_is_empty() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMPTY)
            .username(USERNAME_CORRECT_32)
            .password(EMPTY)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập mật khẩu.");
    }

    @Order(18)
    @Test
    void should_failed_when_pass_is_blank() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(BLANK)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập mật khẩu.");
    }

    @Order(19)
    @Test
    void should_failed_when_pass_is_5_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_WRONG_5)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự");
    }

    @Order(20)
    @Test
    void should_failed_when_pass_is_7_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_WRONG_7)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự");
    }

    @Order(21)
    @Test
    void should_ok_when_pass_is_32_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_32)
            .confirmPassword(PASSWORD_CORRECT_32)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        deleteUserAndCompanyIfFound(request);
        this.executeSuccessRequest(request);
    }

    @Order(22)
    @Test
    void should_failed_when_pass_is_33_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_INCORRECT_33)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự");
    }
    @Order(23)
    @Test
    void should_failed_when_pass_is_40_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_INCORRECT_40)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.password);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự");
    }

    @Order(24)
    @Test
    void should_failed_when_confirm_pass_is_null() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(null)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.confirmPassword);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập mật khẩu xác nhận");
    }
    @Order(25)
    @Test
    void should_failed_when_confirm_pass_is_empty() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(EMPTY)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.confirmPassword);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập mật khẩu xác nhận");
    }

    @Order(26)
    @Test
    void should_failed_when_confirm_pass_is_blank() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(BLANK)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.confirmPassword);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập mật khẩu xác nhận");
    }

    @Order(27)
    @Test
    void should_failed_when_confirm_pass_is_different_with_pass() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(CONF_PASS_DIFF)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.confirmPassword);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Mật khẩu xác nhận phải khớp với mật khẩu");
    }

    @Order(28)
    @Test
    void should_failed_when_size_is_null() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(null)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.size);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy chọn quy mô công ty");
    }

    @Order(29)
    @Test
    void should_failed_when_company_name_is_null() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(null)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập tên công ty");
    }
    @Order(30)
    @Test
    void should_failed_when_company_name_is_empty() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(EMPTY)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập tên công ty");
    }
    @Order(31)
    @Test
    void should_failed_when_company_name_is_blank() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(BLANK)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập tên công ty");
    }

    @Order(32)
    @Test
    void should_failed_when_company_name_have_1_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_WRONG_1)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên công ty phải có độ dài từ 3-255 kí tự");
    }
    @Order(33)
    @Test
    void should_failed_when_company_name_have_2_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_WRONG_2)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên công ty phải có độ dài từ 3-255 kí tự");
    }

    @Order(34)
    @Test
    void should_success_when_company_name_have_255_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_255)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        deleteUserAndCompanyIfFound(request);
        this.executeSuccessRequest(request);
    }

    @Order(35)
    @Test
    void should_failed_when_company_name_have_256_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_INCORRECT_256)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên công ty phải có độ dài từ 3-255 kí tự");
    }
    @Order(36)
    @Test
    void should_failed_when_company_name_have_300_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_INCORRECT_300)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên công ty phải có độ dài từ 3-255 kí tự");
    }

    @Order(37)
    @Test
    void should_failed_when_company_name_existed() throws Exception {
        try {
            SpringBeans.getBean(CompanyRepository.class).findAll().stream().findAny().ifPresent(c -> SpringBeans.getBean(CompanyRepository.class).save(c.withName(COMPANY_NAME_EXISTED)));
        } catch (Exception ignored) {}
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        deleteUserAndCompanyIfFound(request, false);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo( "Tên công ty đã được sử dụng.");
    }

    @Order(38)
    @Test
    void should_failed_when_company_address_is_null() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(null)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyAddress);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập địa chỉ công ty");
    }
    @Order(39)
    @Test
    void should_failed_when_company_address_is_empty() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(EMPTY)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyAddress);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập địa chỉ công ty");
    }

    @Order(40)
    @Test
    void should_failed_when_company_address_is_blank() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(BLANK)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyAddress);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập địa chỉ công ty");
    }

    @Order(41)
    @Test
    void should_failed_when_company_address_have_length_2() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_WRONG_NAME_2)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyAddress);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Địa chỉ công ty phải có độ dài từ 5-255 kí tự");
    }

    @Order(42)
    @Test
    void should_failed_when_company_address_have_length_4() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_WRONG_NAME_4)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyAddress);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Địa chỉ công ty phải có độ dài từ 5-255 kí tự");
    }

    @Order(43)
    @Test
    void should_ok_when_company_address_have_length_255() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_255)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        deleteUserAndCompanyIfFound(request);
        this.executeSuccessRequest(request);

    }

    @Order(44)
    @Test
    void should_failed_when_company_address_have_length_256() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_INCORRECT_NAME_256)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyAddress);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Địa chỉ công ty phải có độ dài từ 5-255 kí tự");
    }
    @Order(45)
    @Test
    void should_failed_when_company_address_have_length_300() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_INCORRECT_NAME_300)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.companyAddress);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Địa chỉ công ty phải có độ dài từ 5-255 kí tự");
    }

    @Order(46)
    @Test
    void should_failed_when_tax_is_null() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(null)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.taxCode);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập mã số thuế");
    }
    @Order(47)
    @Test
    void should_failed_when_tax_is_empty() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(EMPTY)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.taxCode);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập mã số thuế");
    }
    @Order(48)
    @Test
    void should_failed_when_tax_is_blank() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(BLANK)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.taxCode);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập mã số thuế");
    }

    @Order(49)
    @Test
    void should_failed_when_tax_have_length_8() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_WRONG_8)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.taxCode);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Mã số thuế không hợp lệ");
    }
    @Order(50)
    @Test
    void should_failed_when_tax_have_length_9() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_WRONG_9)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.taxCode);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Mã số thuế không hợp lệ");
    }

    @Order(51)
    @Test
    void should_failed_when_tax_have_length_11() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_WRONG_11)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.taxCode);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Mã số thuế không hợp lệ");
    }

    @Order(52)
    @Test
    void should_failed_when_tax_have_length_15() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_WRONG_15)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.taxCode);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Mã số thuế không hợp lệ");
    }

    @Order(53)
    @Test
    void should_failed_when_tax_is_not_digit_only() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_32)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_WRONG_FORMAT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();

        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.taxCode);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Mã số thuế không hợp lệ");
    }

    @Order(54)
    @Test
    void should_failed_when_contact_name_is_null() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(null)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập tên liên hệ");
    }

    @Order(55)
    @Test
    void should_failed_when_contact_name_is_empty() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(EMPTY)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập tên liên hệ");
    }

    @Order(56)
    @Test
    void should_failed_when_contact_name_is_blank() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(BLANK)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập tên liên hệ");
    }


    @Order(57)
    @Test
    void should_failed_when_contact_name_is_2_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_WRONG_2)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên liên hệ phải có độ dài từ 4 đến 32 kí tự");
    }

    @Order(58)
    @Test
    void should_failed_when_contact_name_is_3_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_WRONG_3)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên liên hệ phải có độ dài từ 4 đến 32 kí tự");
    }

    @Order(59)
    @Test
    void should_success_when_contact_name_is_4_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_4)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        deleteUserAndCompanyIfFound(request);
        this.executeSuccessRequest(request);
    }

    @Order(60)
    @Test
    void should_failed_when_contact_name_is_33_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_INCORRECT_33)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên liên hệ phải có độ dài từ 4 đến 32 kí tự");
    }

    @Order(61)
    @Test
    void should_failed_when_contact_name_is_34_chars() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_INCORRECT_34)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên liên hệ phải có độ dài từ 4 đến 32 kí tự");
    }

    @Order(62)
    @Test
    void should_failed_when_contact_name_have_number() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_INVALID_FORMAT_1)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        deleteUserAndCompanyIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên liên hệ chỉ chứa a-z, A-Z và khoảng trắng");
    }
    @Order(63)
    @Test
    void should_failed_when_contact_name_have_specical() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_INVALID_FORMAT_2)
            .contactNumber(CONTACT_NUMBER_CORRECT_10)
            .build();
        deleteUserAndCompanyIfFound(request);
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactName);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Tên liên hệ chỉ chứa a-z, A-Z và khoảng trắng");
    }

    @Order(64)
    @Test
    void should_failed_when_contact_number_null() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(null)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactNumber);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập số điện thoại liên hệ");
    }

    @Order(65)
    @Test
    void should_failed_when_contact_number_empty() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(EMPTY)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactNumber);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập số điện thoại liên hệ");
    }

    @Order(66)
    @Test
    void should_failed_when_contact_number_blank() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(BLANK)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactNumber);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy nhập số điện thoại liên hệ");
    }

    @Order(67)
    @Test
    void should_failed_when_contact_number_length_is_8() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_INCORRECT_8)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactNumber);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Số điện thoại không hợp lệ");
    }
    @Order(68)
    @Test
    void should_failed_when_contact_number_length_is_11() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_INCORRECT_11)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactNumber);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Số điện thoại không hợp lệ");
    }

    @Order(69)
    @Test
    void should_ok_when_contact_number_length_is_9() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_CORRECT_9)
            .build();
        deleteUserAndCompanyIfFound(request);
        this.executeSuccessRequest(request);
    }

    @Order(70)
    @Test
    void should_failed_when_contact_number_length_is_invalid() throws Exception {
        final RegisterForBusinessOwnerCommand.Request request = RegisterForBusinessOwnerCommand.Request
            .builder()
            .email(EMAIL_CORRECT)
            .username(USERNAME_CORRECT_4)
            .password(PASSWORD_CORRECT_8)
            .confirmPassword(CONF_PASS_CORRECT)
            .size(COMPANY_SIZE)
            .companyName(COMPANY_NAME_CORRECT_3)
            .companyAddress(ADDRESS_CORRECT_NAME_5)
            .taxCode(TAX_CORRECT)
            .contactName(CONTACT_NAME_CORRECT_32)
            .contactNumber(CONTACT_NUMBER_INCORRECT_FORMAT)
            .build();
        final HashMap<String, String> response = this.executeFailRequest(request);
        final String error = response.get(RegisterForBusinessOwnerCommand.Request.Fields.contactNumber);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Số điện thoại không hợp lệ");
    }


    private HashMap<String, String> executeFailRequest(final RegisterForBusinessOwnerCommand.Request request) throws Exception {
        final MvcResult result = this.mvc
            .perform(
                post("/user/register/business-owner")
                    .content(Mappers.jsonOf(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-FROM", "http://localhost:3000/")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        return BaseTest.responseOf(result, new TypeReference<>() {
        });
    }

    private void executeSuccessRequest(final RegisterForBusinessOwnerCommand.Request request) throws Exception {
         this.mvc
            .perform(
                post("/user/register/business-owner")
                    .content(Mappers.jsonOf(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-FROM", "http://localhost:3000/")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }



}
