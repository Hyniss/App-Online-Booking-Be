package com.fpt.h2s.accommodation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.BaseTest;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.services.commands.accommodation.HouseOwnerUpdateAccommodationCommand;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
public class HouseOwnerUpdateAccommodationTest extends BaseTest {

    private final Integer ID = 56;
    public static final String URL = "/accommodation/house-owner/update";
    public static final String IMAGE = "https://h2s-s3.s3.ap-northeast-1.amazonaws.com/74e3923a365341ea_20230810114241007%2B0000.jpg";
    public static final String token = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MTg5LCJlbWFpbCI6IlRow6FpIFRy4bqnbiIsInVzZXJuYW1lIjoiVGjDoWkgVHLhuqduIiwicGhvbmUiOiIrODQ5NDE4MTUyMzYiLCJzY29wZXMiOlsiQ1VTVE9NRVIiLCJIT1VTRV9PV05FUiJdLCJpYXQiOjE2OTIxOTU0MTUsImV4cCI6MTY5MzQ5MTQxNX0.vaj7ssYPAeIZE4A-KOgMTPeVpDy0VXCaNF0ylU6Elq8";
    @Test
    void should_success_when_use_english() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
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
        Assertions.assertThat(MoreStrings.utf8Of(message)).isEqualTo("Cập nhật chỗ ở thành công.");
    }


    @Test
    void should_failed_if_id_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(null)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.id);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng không để trống ID của chỗ ở.");
    }


    @Test
    void should_failed_if_id_is_not_found() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(0)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.id);
                Assertions.assertThat(MoreStrings.utf8Of(error)).startsWith("Không thể tìm thấy chỗ ở với id =");
    }

    @Test
    void should_failed_if_id_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(63)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.id);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Chỗ ở này không thuộc quyền quản lí của bạn.");
    }

    @Test
    void should_failed_if_name_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name(null)
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.name);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập tên chỗ ở.");
    }

    @Test
    void should_failed_if_name_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name(gen1025Character())
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.name);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập tên chỗ ở trong khoảng 1024 ký tự.");
    }

    @Test
    void should_failed_if_thumbnail_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(null)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.thumbnail);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập đường dẫn hình ảnh đại diện của chỗ ở.");
    }

    @Test
    void should_failed_if_thumbnail_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail("Aaaaa")
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.thumbnail);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập đường dẫn hình ảnh hợp lệ.");
    }

    @Test
    void should_failed_if_short_description_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription(null)
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.shortDescription);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập mô tả ngắn gọn cho chỗ ở này.");
    }

    @Test
    void should_failed_if_short_description_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription(gen1025Character())
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.shortDescription);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập mô tả ngắn gọn trong khoảng 400 ký tự.");
    }

    @Test
    void should_failed_if_description_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description(null)
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.description);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập mô tả chi tiết cho chỗ ở này.");
    }

    @Test
    void should_failed_if_description_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description(gen2049Character())
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.description);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập mô tả chi tiết trong khoảng 2048 ký tự.");
    }

    @Test
    void should_failed_if_address_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address(null)
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.address);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập địa chỉ hiện tại của chỗ ở.");
    }

    @Test
    void should_failed_if_address_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address(gen2049Character())
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.address);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập địa chỉ hiện tại của chỗ ở trong khoảng 2048 ký tự.");
    }

    @Test
    void should_failed_if_latitude_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(null)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.latitude);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn vị trí hiện tại của chỗ ở.");
    }

    @Test
    void should_failed_if_latitude_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(-91d)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.latitude);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Kinh độ trong khoảng từ -90 đến 90.");
    }

    @Test
    void should_failed_if_longitude_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(null)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.longitude);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn vị trí hiện tại của chỗ ở.");
    }

    @Test
    void should_failed_if_longitude_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(181d)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.longitude);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Vĩ độ trong khoảng từ -180 đến 180.");
    }

    @Test
    void should_failed_if_type_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(null)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.type);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng lựa chọn loại hình của chỗ ở.");
    }

    @Test
    void should_failed_if_type_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.APARTMENT)
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.type);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn lại loại hình của chỗ ở này. Chỗ ở thuộc loại CĂN HỘ hoặc NHÀ chỉ có duy nhất 1 phòng.");
    }

    @Test
    void should_failed_if_location_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(null)
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.location);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng lựa chọn các tiện ích xung quanh chỗ ở.");
    }

    @Test
    void should_failed_if_location_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationCommand.Request request = HouseOwnerUpdateAccommodationCommand.Request
                .builder()
                .id(ID)
                .name("Nhà unit test")
                .thumbnail(IMAGE)
                .shortDescription("Đây là nhà unit test, mời bạn ghé chơi")
                .description("<p>Nhà unit test - Nơi Thấu Hiểu Tâm Hồn và Kết Nối Yêu Thương</p><p>Nhà của unit test chào đón bạn đến một không gian đậm chất ấm áp và tràn đầy yêu thương. Được xây dựng với tâm huyết và ý nghĩa, ngôi nhà này không chỉ là một mái ấm mà còn là một góc kỷ niệm đáng trân quý.</p>")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL)
                .location(getSetCategoriesInvalid())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationCommand.Request.Fields.location);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chỉ chọn danh mục với thể loại là vị trí (location).");
    }

    private HashMap<String, String> executeRequest(final HouseOwnerUpdateAccommodationCommand.Request request) throws Exception {
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
    private Set<Integer> getSetCategories() {
        Set<Integer> categoriesRequestSet = new HashSet<>();
        categoriesRequestSet.add(3);
        categoriesRequestSet.add(4);
        categoriesRequestSet.add(5);
        categoriesRequestSet.add(6);
        return categoriesRequestSet;
    }

    private Set<Integer> getSetCategoriesInvalid() {
        Set<Integer> categoriesRequestSet = new HashSet<>();
        categoriesRequestSet.add(3);
        categoriesRequestSet.add(4);
        categoriesRequestSet.add(21);
        categoriesRequestSet.add(22);
        return categoriesRequestSet;
    }

    private String gen1025Character() {
        return IntStream.range(0, 1025)
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
