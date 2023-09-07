package com.fpt.h2s.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.BaseTest;
import com.fpt.h2s.interceptors.models.MessageResolver;
import com.fpt.h2s.services.commands.user.RegisterToHouseOwnerCommand;
import com.fpt.h2s.utilities.Mappers;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class RegisterToHouseOwnerTest extends BaseTest {

    public static final String URL = "/user/register/house-owner";
    public static final String authToken = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MTIwLCJlbWFpbCI6IkFCQ0QiLCJ1c2VybmFtZSI6IkFCQ0QiLCJwaG9uZSI6bnVsbCwic2NvcGVzIjpbIkNVU1RPTUVSIl0sImlhdCI6MTY5MDIwODMzNywiZXhwIjoxNjkxNTA0MzM3fQ.QpTsXIPSD1b_ZDYrwGjuj1eIXpP7B3Mq0IE0s9N9r3I";
    public static final String authTokenForHouseOwner = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwiZW1haWwiOiJBZG1pbjEyMyIsInVzZXJuYW1lIjoiQWRtaW4xMjMiLCJwaG9uZSI6bnVsbCwic2NvcGVzIjpbIkFETUlOIiwiSE9VU0VfT1dORVIiXSwiaWF0IjoxNjkwMjA3NTU1LCJleHAiOjE2OTE1MDM1NTV9.cReJT4TogN3srF2GyVUaLf4p4LaWGRYZqR8tMIA3stU";
    public static final String authTokenForBusinessOwner = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MTEwLCJlbWFpbCI6IlNvbmRkIGJ1c2luZXNzIG93bmVyIiwidXNlcm5hbWUiOiJTb25kZCBidXNpbmVzcyBvd25lciIsInBob25lIjpudWxsLCJzY29wZXMiOlsiQlVTSU5FU1NfT1dORVIiXSwiaWF0IjoxNjkwMjA5NTk5LCJleHAiOjE2OTE1MDU1OTl9.3wC6fhsSpGhdfl87wUmjd8uTbv3EQ0_S4MpXBzYogic";
    public static final String authTokenForBusinessAdmin = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MTI2LCJlbWFpbCI6Im5hbW5hbTAyIiwidXNlcm5hbWUiOiJuYW1uYW0wMiIsInBob25lIjpudWxsLCJzY29wZXMiOlsiQlVTSU5FU1NfQURNSU4iXSwiaWF0IjoxNjkwMjA5NjkzLCJleHAiOjE2OTE1MDU2OTN9.9D8w4JUU9c1h8HsmBwV7WLg_r5HPsAhTGAHKbHa6HqQ";
    public static final String authTokenForBusinessMember = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MTUyLCJlbWFpbCI6Im5hbTIiLCJ1c2VybmFtZSI6Im5hbTIiLCJwaG9uZSI6Iis4NDkxOTgyMDMxMiIsInNjb3BlcyI6WyJCVVNJTkVTU19NRU1CRVIiXSwiaWF0IjoxNjkwMjA5OTAyLCJleHAiOjE2OTE1MDU5MDJ9.HaTi5-OtGgZnoNqzCPNuw-37m7uRqowMVLp8XLBpCac";

    @Test
    void should_success_when_use_english() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(120)
                .contractName("Contract")
                .content("<p>Contract</p>")
                .profit(15)
                .build();
        final MvcResult result = this.mvc
                .perform(
                        post(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final String message = BaseTest.messageOf(result);
        Assertions.assertThat(message).isEqualTo(this.messageResolver.getMessageIn(MessageResolver.SupportLocale.ENGLISH, "REGISTER_TO_HOUSE_OWNER_SUCCEED"));
    }

    @Test
    void should_success_admin_when_use_english() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(120)
                .contractName("Contract")
                .content("<p>Contract</p>")
                .profit(15)
                .build();
        final MvcResult result = this.mvc
                .perform(
                        post(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final String message = BaseTest.messageOf(result);
        Assertions.assertThat(message).isEqualTo(this.messageResolver.getMessageIn(MessageResolver.SupportLocale.ENGLISH, "REGISTER_TO_HOUSE_OWNER_SUCCEED"));
    }
    @Test
    void should_failed_if_is_house_owner() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(1)
                .contractName("Contract")
                .content("Contract")
                .profit(15)
                .build();
        final MvcResult result = this.mvc
                .perform(
                        post(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + authTokenForHouseOwner)
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final HashMap<String, String> response = BaseTest.responseOf(result, new TypeReference<>() {});
        final String error = response.get(RegisterToHouseOwnerCommand.Request.Fields.userId);
        final String message = this.messageResolver.get("USER_CAN_NOT_BE_HOUSE_OWNER");
        Assertions.assertThat(message).isEqualTo(error);
    }

    @Test
    void should_failed_if_is_business_owner() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(110)
                .contractName("Contract")
                .content("Contract")
                .profit(15)
                .build();
        final MvcResult result = this.mvc
                .perform(
                        post(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + authTokenForBusinessOwner)
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final HashMap<String, String> response = BaseTest.responseOf(result, new TypeReference<>() {});
        final String error = response.get(RegisterToHouseOwnerCommand.Request.Fields.userId);
        final String message = this.messageResolver.get("USER_CAN_NOT_BE_HOUSE_OWNER");
        Assertions.assertThat(message).isEqualTo(error);
    }

    @Test
    void should_failed_if_is_business_admin() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(126)
                .contractName("Contract")
                .content("Contract")
                .profit(15)
                .build();
        final MvcResult result = this.mvc
                .perform(
                        post(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + authTokenForBusinessAdmin)
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final HashMap<String, String> response = BaseTest.responseOf(result, new TypeReference<>() {});
        final String error = response.get(RegisterToHouseOwnerCommand.Request.Fields.userId);
        final String message = this.messageResolver.get("USER_CAN_NOT_BE_HOUSE_OWNER");
        Assertions.assertThat(message).isEqualTo(error);
    }

    @Test
    void should_failed_if_is_business_member() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(152)
                .contractName("Contract")
                .content("Contract")
                .profit(15)
                .build();
        final MvcResult result = this.mvc
                .perform(
                        post(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + authTokenForBusinessMember)
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final HashMap<String, String> response = BaseTest.responseOf(result, new TypeReference<>() {});
        final String error = response.get(RegisterToHouseOwnerCommand.Request.Fields.userId);
        final String message = this.messageResolver.get("USER_CAN_NOT_BE_HOUSE_OWNER");
        Assertions.assertThat(message).isEqualTo(error);
    }

    @Test
    void should_failed_if_status_is_pending() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(124)
                .contractName("Contract")
                .content("Contract")
                .profit(15)
                .build();
        final MvcResult result = this.mvc
                .perform(
                        post(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final String message = BaseTest.messageOf(result);
        Assertions.assertThat(message).isEqualTo(this.messageResolver.getMessageIn(MessageResolver.SupportLocale.ENGLISH, "USER_CAN_NOT_BE_HOUSE_OWNER"));
    }

    @Test
    void should_failed_if_status_is_banned() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(125)
                .contractName("Contract")
                .content("Contract")
                .profit(15)
                .build();
        final MvcResult result = this.mvc
                .perform(
                        post(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final String message = BaseTest.messageOf(result);
        Assertions.assertThat(message).isEqualTo(this.messageResolver.getMessageIn(MessageResolver.SupportLocale.ENGLISH, "USER_CAN_NOT_BE_HOUSE_OWNER"));
    }

    @Test
    void should_failed_if_contract_name_is_null() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(120)
                .contractName(null)
                .content("Contract")
                .profit(15)
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(RegisterToHouseOwnerCommand.Request.Fields.contractName);
        final String message = this.messageResolver.get("CONTRACT_NAME_BLANK");
        Assertions.assertThat(error).isEqualTo(message);
    }

    @Test
    void should_failed_if_contract_name_is_over_required_length() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(120)
                .contractName(gen256Character())
                .content("Contract")
                .profit(15)
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(RegisterToHouseOwnerCommand.Request.Fields.contractName);
        final String message = this.messageResolver.get("CONTRACT_NAME_INVALID");
        Assertions.assertThat(error).isEqualTo(message);
    }

    @Test
    void should_failed_if_content_is_null() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(120)
                .contractName("Contract")
                .content(null)
                .profit(15)
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(RegisterToHouseOwnerCommand.Request.Fields.content);
        final String message = this.messageResolver.get("CONTENT_BLANK");
        Assertions.assertThat(error).isEqualTo(message);
    }

    @Test
    void should_failed_if_content_is_over_required_length() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(120)
                .contractName("Contract")
                .content(genCharacter())
                .profit(15)
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(RegisterToHouseOwnerCommand.Request.Fields.content);
        final String message = this.messageResolver.get("CONTENT_INVALID");
        Assertions.assertThat(error).isEqualTo(message);
    }

    @Test
    void should_failed_if_profit_is_null() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(120)
                .contractName("Contract")
                .content("Contract")
                .profit(null)
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(RegisterToHouseOwnerCommand.Request.Fields.profit);
        final String message = this.messageResolver.get("PROFIT_NULL");
        Assertions.assertThat(error).isEqualTo(message);
    }

    @Test
    void should_failed_if_profit_is_over_required() throws Exception {
        final RegisterToHouseOwnerCommand.Request request = RegisterToHouseOwnerCommand.Request
                .builder()
                .userId(120)
                .contractName("Contract")
                .content("Contract")
                .profit(14)
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(RegisterToHouseOwnerCommand.Request.Fields.profit);
        final String message = this.messageResolver.get("PROFIT_INVALID");
        Assertions.assertThat(error).isEqualTo(message);
    }



    private HashMap<String, String> executeRequest(final RegisterToHouseOwnerCommand.Request request) throws Exception {
        final MvcResult result = this.mvc
                .perform(
                        post(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        return BaseTest.responseOf(result, new TypeReference<>() {
        });
    }


    private String gen256Character() {
        return IntStream.range(0, 256)
                .mapToObj(i -> 'A')
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    private String genCharacter() {
        return IntStream.range(0, 65536)
                .mapToObj(i -> 'A')
                .map(Object::toString)
                .collect(Collectors.joining());
    }

}
