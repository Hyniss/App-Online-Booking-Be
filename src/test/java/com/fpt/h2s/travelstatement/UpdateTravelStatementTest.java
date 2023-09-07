package com.fpt.h2s.travelstatement;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.BaseTest;
import com.fpt.h2s.services.commands.travelstatement.UpdateTravelStatementCommand;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.MoreStrings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
public class UpdateTravelStatementTest extends BaseTest {

    private final Integer ID = 12;
    public static final String URL = "/travel-statement/business-member/update";
    public static final String token = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MTg0LCJlbWFpbCI6IlRoYWkgQnVzaW5lc3MgTWVtYmVyIiwidXNlcm5hbWUiOiJUaGFpIEJ1c2luZXNzIE1lbWJlciIsInBob25lIjoiKzg0OTQxODE1MDY1Iiwic2NvcGVzIjpbIkJVU0lORVNTX01FTUJFUiJdLCJpYXQiOjE2OTMwNjkxODEsImV4cCI6MTY5NDM2NTE4MX0.-DhS9jYMZfb0YYJgKNBldpH3nJsC03LJP9pC0OSmVCw";

    @Test
    void should_success() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("Travel Statement for test 1")
                .numberOfPeople("3")
                .location("Ha noi")
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final MvcResult result = this.mvc
                .perform(
                        put(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + token)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final String message = BaseTest.messageOf(result);
        Assertions.assertThat(MoreStrings.utf8Of(message)).isEqualTo("Cập nhật tờ trình thành công.");
    }

    //Validate id

    @Test
    void should_failed_if_id_is_null() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(null)
                .name("Travel Statement for test 1")
                .numberOfPeople("3")
                .location("Ha noi")
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.id);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng không để trống ID của tờ trình.");
    }

    @Test
    void should_failed_if_name_is_null() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name(null)
                .numberOfPeople("3")
                .location("Ha noi")
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.name);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng điền tên của tờ trình.");
    }

    @Test
    void should_failed_if_name_is_blank() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("")
                .numberOfPeople("3")
                .location("Ha noi")
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.name);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng điền tên của tờ trình.");
    }

    @Test
    void should_failed_if_name_is_invalid_256_characters() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name(gen256Character())
                .numberOfPeople("3")
                .location("Ha noi")
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.name);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để tên của tờ trình trong khoảng 255 kí tự.");
    }


    @Test
    void should_failed_if_note_is_invalid_256_characters() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("Travel Statement for test 1")
                .numberOfPeople("3")
                .location("Ha noi")
                .note(gen256Character())
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.note);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để ghi chú của tờ trình trong khoảng 255 kí tự.");
    }

    //Validate number of people
    @Test
    void should_failed_if_number_of_people_is_null() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("Travel Statement for test 1")
                .numberOfPeople(null)
                .location("Ha noi")
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.numberOfPeople);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng điền số lượng người cần đặt phòng.");
    }

    @Test
    void should_failed_if_number_of_people_is_not_greater_than_0() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("Travel Statement for test 1")
                .numberOfPeople("0")
                .location("Ha noi")
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.numberOfPeople);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập số lượng người trong khoảng từ 0 đến 5000.");
    }

    //Validate location
    @Test
    void should_failed_if_location_is_blank() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("Travel Statement for test 1")
                .numberOfPeople("3")
                .location("")
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.location);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập khu vực mà người dùng có dự định ở vào tờ trình.");
    }

    @Test
    void should_failed_if_location_invalid_2049_characters() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("Travel Statement for test 1")
                .numberOfPeople("3")
                .location(gen2049Character())
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.location);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để khu vực mà người dùng có dự định ở vào tờ trình trong khoảng 2048 kí tự.");
    }

    @Test
    void should_failed_if_location_is_null() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("Travel Statement for test 1")
                .numberOfPeople("3")
                .location(null)
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.location);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập khu vực mà người dùng có dự định ở vào tờ trình.");
    }

    //Validate fromDate/toDate

    @Test
    void should_failed_if_fromDate_is_null() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("Travel Statement for test 1")
                .numberOfPeople("3")
                .location("Ha noi")
                .note("Booking for work of Department D")
                .fromDate(null)
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.fromDate);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn ngày nhận phòng.");
    }

    @Test
    void should_failed_if_fromDate_not_after_current_date() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("Travel Statement for test 1")
                .numberOfPeople("3")
                .location("Ha noi")
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-06-08 00:00:00"))
                .toDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.fromDate);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn ngày nhận phòng sau ngày hiện tại.");
    }

    @Test
    void should_failed_if_toDate_is_null() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("Travel Statement for test 1")
                .numberOfPeople("3")
                .location("Ha noi")
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-04 00:00:00"))
                .toDate(null)
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.toDate);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn ngày trả phòng.");
    }

    @Test
    void should_failed_if_toDate_not_after_fromDate() throws Exception {
        final UpdateTravelStatementCommand.UpdateTravelStatementRequest request = UpdateTravelStatementCommand.UpdateTravelStatementRequest
                .builder()
                .id(ID)
                .name("Travel Statement for test 1")
                .numberOfPeople("3")
                .location("Ha noi")
                .note("Booking for work of Department D")
                .fromDate(Timestamp.valueOf("2023-12-08 00:00:00"))
                .toDate(Timestamp.valueOf("2023-06-08 00:00:00"))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(UpdateTravelStatementCommand.UpdateTravelStatementRequest.Fields.toDate);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn ngày trả phòng sau ngày nhận phòng.");
    }
    

    private HashMap<String, String> executeRequest(final UpdateTravelStatementCommand.UpdateTravelStatementRequest request) throws Exception {
        final MvcResult result = this.mvc
                .perform(
                        put(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + token)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        return BaseTest.responseOf(result, new TypeReference<>() {
        });
    }

    private String gen256Character() {
        return IntStream.range(0, 256)
                .mapToObj(i -> 'A')
                .map(Object::toString)
                .collect(Collectors.joining());
    }
    private String gen2049Character() {
        return IntStream.range(0, 2049)
                .mapToObj(i -> 'A')
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
