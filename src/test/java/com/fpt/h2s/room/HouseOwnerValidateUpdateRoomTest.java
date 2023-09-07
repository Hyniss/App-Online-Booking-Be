package com.fpt.h2s.room;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.BaseTest;
import com.fpt.h2s.models.entities.Room;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateRoomImageRequest;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateRoomPropertyRequest;
import com.fpt.h2s.services.commands.room.HouseOwnerValidateUpdateRoomCommand;
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
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class HouseOwnerValidateUpdateRoomTest extends BaseTest {
    
    private final Integer ID = 111;
    private final Integer AccommID = 111;

    public static final String URL = "/room/house-owner/update/validate";
    public static final String IMAGE = "https://a0.muscache.com/im/pictures/miso/Hosting-64AccommID15294944482016/original/70267b18-d1e3-41f1-9489-33243471e8ad.jpeg?im_w=720";
    public static final String token = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MTg5LCJlbWFpbCI6IlRow6FpIFRy4bqnbiIsInVzZXJuYW1lIjoiVGjDoWkgVHLhuqduIiwicGhvbmUiOiIrODQ5NDE4MTUyMzYiLCJzY29wZXMiOlsiQ1VTVE9NRVIiLCJIT1VTRV9PV05FUiJdLCJpYXQiOjE2OTI4MDcwMTksImV4cCI6MTY5NDEwMzAxOX0.A1Qfa2oaGezMAVb9pt_lRV0WtdqYAr5A29Ohs6Wkgio";

    @Test
    void should_success_when_use_english() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final MvcResult result = this.mvc
                .perform(
                        post(URL)
                                .content(Mappers.jsonOf(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-LOCALE", "en")
                                .header("Authorization", "Bearer " + token)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final String message = BaseTest.messageOf(result);
        Assertions.assertThat(MoreStrings.utf8Of(message)).isEqualTo("Thông tin hợp lệ.");

    }

    @Test
    void should_failed_if_room_id_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(null)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.id);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng không để trống ID của phòng.");
    }


    @Test
    void should_failed_if_id_room_is_not_found() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(0)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.id);
        Assertions.assertThat(MoreStrings.utf8Of(error)).startsWith("Không thể tìm thấy phòng với id = ");
    }

    @Test
    void should_failed_if_room_id_is_invalid() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(110)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.id);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Phòng này không thuộc về chỗ ở hiện tại.");
    }

    @Test
    void should_failed_if_id_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(null)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.accommodationId);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng không để trống ID của chỗ ở.");
    }


    @Test
    void should_failed_if_id_is_not_found() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(0)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.accommodationId);
        Assertions.assertThat(MoreStrings.utf8Of(error)).startsWith("Không thể tìm thấy chỗ ở với id = ");
    }

    @Test
    void should_failed_if_id_is_invalid() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(63)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.accommodationId);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Chỗ ở này không thuộc quyền quản lí của bạn.");
    }

    @Test
    void should_failed_if_name_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name(null)
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.name);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập tên phòng.");
    }

    @Test
    void should_failed_if_name_is_invalid() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name(gen513Character())
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.name);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập tên phòng trong khoảng 512 ký tự.");
    }

    @Test
    void should_failed_if_status_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(null)
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.status);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn trạng thái của phòng.");
    }

    @Test
    void should_failed_if_status_is_invalid() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status("PENDING")
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.status);
        Assertions.assertThat(MoreStrings.utf8Of(error)).startsWith("Trạng thái của phòng thuộc một trong các loại sau đây:");
    }

    @Test
    void should_failed_if_count_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count(null)
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.count);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập số lượng phòng. Với chỗ ở thuộc loại hình là nhà (HOUSE) hoặc căn hộ (APARTMENT) thì chỉ có duy nhất 1 phòng.");
    }

    @Test
    void should_failed_if_count_is_over() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1001")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.count);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để số lượng phòng trong khoảng từ 1 đến 1000.");
    }

    @Test
    void should_failed_if_count_is_invalid() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("10")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.count);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn lại thể loại của chỗ ở này. Chỗ ở thuộc loại CĂN HỘ hoặc NHÀ chỉ có duy nhất 1 phòng.");
    }

    @Test
    void should_failed_if_count_is_float() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("10.1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.count);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chỉ nhập số nguyên.");
    }

    @Test
    void should_failed_if_is_update_image_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(null)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.isUpdateImage);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy chọn TRUE để cập nhật hình ảnh hoặc FALSE để không cập nhật hình ảnh.");
    }

    @Test
    void should_failed_if_image_name_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImagesWithNameNull(true, false))
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.images);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để tên ảnh cho bức ảnh này.");
    }

    @Test
    void should_failed_if_image_name_is_invalid() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImagesWithNameNull(false, true))
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.images);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để tên hình ảnh trong khoảng 512 ký tự.");
    }

    @Test
    void should_failed_if_image_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImagesWithUrlNull(true, false))
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.images);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng không để trống ảnh.");
    }

    @Test
    void should_failed_if_image_is_invalid() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImagesWithUrlNull(false, true))
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.images);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để đường dẫn hình ảnh hợp lệ.");
    }

    @Test
    void should_failed_if_image_size_is_invalid() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(null)
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.images);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để ảnh của phòng.");
    }

    @Test
    void should_failed_if_is_update_properties_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(null)
                .properties(getSetRoomProperties())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.isUpdateProperty);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin hãy chọn TRUE để cập nhật thuộc tính của phòng hoặc FALSE để không cập nhật thuộc tính của phòng.");
    }

    @Test
    void should_failed_if_properties_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
            .builder()
            .id(ID)
            .accommodationId(AccommID)
            .name("Suite")
            .status(Room.Status.OPENING.name())
            .count("1")
            .isUpdateImage(true)
            .images(getSetImages())
            .isUpdateProperty(true)
            .properties(null)
            .amenities(getSetCategories())
            .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.properties);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng điền 6 thuộc tính của phòng.");
    }

    @Test
    void should_failed_if_properties_size_is_invalid() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomPropertiesBelow5())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.properties);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chỉ điền 6 thuộc tính của phòng.");
    }

    @Test
    void should_failed_if_value_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomPropertiesNull())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.properties);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng không để trống thuộc tính của phòng.");
    }
    @Test
    void should_failed_if_value_is_invalid() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomPropertiesInvalid())
                .amenities(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.properties);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn từng thuộc tính của phòng trong khoảng cho phép từ 0 đến 10.");
    }

    @Test
    void should_failed_if_amenities_is_null() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(getSetRoomProperties())
                .amenities(null)
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.amenities);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng lựa chọn các tiện ích của phòng.");
    }

    @Test
    void should_failed_if_amenities_is_invalid() throws Exception {
        final HouseOwnerValidateUpdateRoomCommand.Request request = HouseOwnerValidateUpdateRoomCommand.Request
                .builder()
                .id(ID)
                .accommodationId(AccommID)
                .name("Suite")
                .status(Room.Status.OPENING.name())
                .count("1")
                .isUpdateImage(true)
                .images(getSetImages())
                .isUpdateProperty(true)
                .properties(null)
                .amenities(getSetCategoriesInvalid())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateUpdateRoomCommand.Request.Fields.amenities);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chỉ chọn danh mục với thể loại là tiện ích (amenities).");
    }

    private HashMap<String, String> executeRequest(final HouseOwnerValidateUpdateRoomCommand.Request request) throws Exception {
        final MvcResult result = this.mvc
                .perform(
                        post(URL)
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

    private Set<HouseOwnerCreateUpdateRoomImageRequest> getSetImages() {
        return IntStream.rangeClosed(1, 2)
                .mapToObj(i -> HouseOwnerCreateUpdateRoomImageRequest.builder()
                        .name("Aaaa " + i)
                        .url(IMAGE)
                        .build())
                .collect(Collectors.toSet());
    }

    private Set<HouseOwnerCreateUpdateRoomImageRequest> getSetImagesWithNameNull(final Boolean isNull,
                                                                                 final Boolean isInvalid) {
        if (isNull) {
            return IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> HouseOwnerCreateUpdateRoomImageRequest.builder()
                            .name(null)
                            .url(IMAGE)
                            .build())
                    .collect(Collectors.toSet());
        } else if (isInvalid) {
            String character = IntStream.range(0, 513)
                    .mapToObj(i -> 'A')
                    .map(Object::toString)
                    .collect(Collectors.joining());

            return IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> HouseOwnerCreateUpdateRoomImageRequest.builder()
                            .name(character)
                            .url(IMAGE)
                            .build())
                    .collect(Collectors.toSet());
        }
        return null;
    }

    private Set<HouseOwnerCreateUpdateRoomImageRequest> getSetImagesWithUrlNull(final Boolean isNull,
                                                                                final Boolean isInvalid) {
        if (isNull) {
            return IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> HouseOwnerCreateUpdateRoomImageRequest.builder()
                            .name("Aaaa")
                            .url(null)
                            .build())
                    .collect(Collectors.toSet());
        } else if (isInvalid) {
            return IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> HouseOwnerCreateUpdateRoomImageRequest.builder()
                            .name("Aaaa")
                            .url("Aaaa")
                            .build())
                    .collect(Collectors.toSet());
        }
        return null;
    }

    private Set<HouseOwnerCreateUpdateRoomPropertyRequest> getSetRoomProperties() {
        Random random = new Random();
        return IntStream.rangeClosed(1, 6)
                .mapToObj(i -> HouseOwnerCreateUpdateRoomPropertyRequest.builder()
                        .keyId(i)
                        .value(random.nextInt(6))
                        .build())
                .collect(Collectors.toSet());
    }

    private Set<HouseOwnerCreateUpdateRoomPropertyRequest> getSetRoomPropertiesBelow5() {
        Random random = new Random();
        return IntStream.rangeClosed(1, 5)
                .mapToObj(i -> HouseOwnerCreateUpdateRoomPropertyRequest.builder()
                        .keyId(i)
                        .value(random.nextInt(6))
                        .build())
                .collect(Collectors.toSet());
    }

    private Set<HouseOwnerCreateUpdateRoomPropertyRequest> getSetRoomPropertiesInvalid() {
        return IntStream.rangeClosed(1, 5)
                .mapToObj(i -> HouseOwnerCreateUpdateRoomPropertyRequest.builder()
                        .keyId(i)
                        .value(12)
                        .build())
                .collect(Collectors.toSet());
    }

    private Set<HouseOwnerCreateUpdateRoomPropertyRequest> getSetRoomPropertiesNull() {
        return IntStream.rangeClosed(1, 5)
                .mapToObj(i -> HouseOwnerCreateUpdateRoomPropertyRequest.builder()
                        .keyId(i)
                        .value(null)
                        .build())
                .collect(Collectors.toSet());
    }

    private String gen513Character() {
        return IntStream.range(0, 513)
                .mapToObj(i -> 'A')
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    private Set<Integer> getSetCategories() {
        Set<Integer> categoriesRequestSet = new HashSet<>();
        categoriesRequestSet.add(23);
        categoriesRequestSet.add(24);
        categoriesRequestSet.add(21);
        categoriesRequestSet.add(22);
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

}
