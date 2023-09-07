package com.fpt.h2s.accommodation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.BaseTest;
import com.fpt.h2s.services.commands.accommodation.HouseOwnerUpdateAccommodationImageCommand;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateAccommodationImageRequest;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
public class HouseOwnerUpdateAccommodationImageTest extends BaseTest {

    private final Integer ID = 56;
    public static final String URL = "/accommodation/house-owner/update/images";
    public static final String IMAGE = "https://h2s-s3.s3.ap-northeast-1.amazonaws.com/74e3923a365341ea_20230810114241007%2B0000.jpg";
    public static final String token = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MTg5LCJlbWFpbCI6IlRow6FpIFRy4bqnbiIsInVzZXJuYW1lIjoiVGjDoWkgVHLhuqduIiwicGhvbmUiOiIrODQ5NDE4MTUyMzYiLCJzY29wZXMiOlsiQ1VTVE9NRVIiLCJIT1VTRV9PV05FUiJdLCJpYXQiOjE2OTIxOTI0MTksImV4cCI6MTY5MzQ4ODQxOX0.PQHCgUnXPoyjawgJHYkegOW8DcfcTIy6ETEZELX0mdQ";


    @Test
    void should_success_when_use_english() throws Exception {
        final HouseOwnerUpdateAccommodationImageCommand.Request request = HouseOwnerUpdateAccommodationImageCommand.Request
                .builder()
                .accommodationId(ID)
                .images(getSetImages())
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
        final HouseOwnerUpdateAccommodationImageCommand.Request request = HouseOwnerUpdateAccommodationImageCommand.Request
                .builder()
                .accommodationId(null)
                .images(getSetImages())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationImageCommand.Request.Fields.accommodationId);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng không để trống ID của chỗ ở.");
    }

    @Test
    void should_failed_if_id_not_found() throws Exception {
        final HouseOwnerUpdateAccommodationImageCommand.Request request = HouseOwnerUpdateAccommodationImageCommand.Request
                .builder()
                .accommodationId(0)
                .images(getSetImages())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationImageCommand.Request.Fields.accommodationId);
        Assertions.assertThat(MoreStrings.utf8Of(error)).startsWith("Không thể tìm thấy chỗ ở với id =");
    }

    @Test
    void should_failed_if_id_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationImageCommand.Request request = HouseOwnerUpdateAccommodationImageCommand.Request
                .builder()
                .accommodationId(52)
                .images(getSetImages())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationImageCommand.Request.Fields.accommodationId);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Chỗ ở này không thuộc quyền quản lí của bạn.");
    }



    @Test
    void should_failed_if_image_name_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationImageCommand.Request request = HouseOwnerUpdateAccommodationImageCommand.Request
                .builder()
                .accommodationId(ID)
                .images(getSetImagesWithNameNull(true, false ))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationImageCommand.Request.Fields.images);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để tên ảnh cho bức ảnh này.");
    }

    @Test
    void should_failed_if_image_name_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationImageCommand.Request request = HouseOwnerUpdateAccommodationImageCommand.Request
                .builder()
                .accommodationId(ID)
                .images(getSetImagesWithNameNull(false, true))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationImageCommand.Request.Fields.images);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để tên hình ảnh trong khoảng 512 ký tự.");

    }

    @Test
    void should_failed_if_image_is_null() throws Exception {
        final HouseOwnerUpdateAccommodationImageCommand.Request request = HouseOwnerUpdateAccommodationImageCommand.Request
                .builder()
                .accommodationId(ID)
                .images(getSetImagesWithUrlNull(true, false))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationImageCommand.Request.Fields.images);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng không để trống ảnh.");
    }

    @Test
    void should_failed_if_image_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationImageCommand.Request request = HouseOwnerUpdateAccommodationImageCommand.Request
                .builder()
                .accommodationId(ID)
                .images(getSetImagesWithUrlNull(false, true))
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationImageCommand.Request.Fields.images);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để đường dẫn hình ảnh hợp lệ.");
    }

    @Test
    void should_failed_if_image_size_is_invalid() throws Exception {
        final HouseOwnerUpdateAccommodationImageCommand.Request request = HouseOwnerUpdateAccommodationImageCommand.Request
                .builder()
                .accommodationId(ID)
                .images(getSetImagesUnder5Images())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerUpdateAccommodationImageCommand.Request.Fields.images);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn 5 bức ảnh hoặc nhiều hơn cho chỗ ở này.");
    }

    private HashMap<String, String> executeRequest(final HouseOwnerUpdateAccommodationImageCommand.Request request) throws Exception {
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

    private Set<HouseOwnerCreateUpdateAccommodationImageRequest> getSetImages() {
        return IntStream.rangeClosed(1, 5)
                .mapToObj(i -> HouseOwnerCreateUpdateAccommodationImageRequest.builder()
                        .name("Aaaa " + i)
                        .url(IMAGE)
                        .build())
                .collect(Collectors.toSet());
    }

    private Set<HouseOwnerCreateUpdateAccommodationImageRequest> getSetImagesUnder5Images() {
        return IntStream.rangeClosed(1, 2)
                .mapToObj(i -> HouseOwnerCreateUpdateAccommodationImageRequest.builder()
                        .name("Aaaa " + i)
                        .url(IMAGE)
                        .build())
                .collect(Collectors.toSet());
    }

    private Set<HouseOwnerCreateUpdateAccommodationImageRequest> getSetImagesWithNameNull(final Boolean isNull,
                                                                                          final Boolean isInvalid) {
        if (isNull) {
            return IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> HouseOwnerCreateUpdateAccommodationImageRequest.builder()
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
                    .mapToObj(i -> HouseOwnerCreateUpdateAccommodationImageRequest.builder()
                            .name(character)
                            .url(IMAGE)
                            .build())
                    .collect(Collectors.toSet());
        }
        return null;
    }

    private Set<HouseOwnerCreateUpdateAccommodationImageRequest> getSetImagesWithUrlNull(final Boolean isNull,
                                                                                         final Boolean isInvalid) {
        if (isNull) {
            return IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> HouseOwnerCreateUpdateAccommodationImageRequest.builder()
                            .name("Aaaa")
                            .url(null)
                            .build())
                    .collect(Collectors.toSet());
        } else if (isInvalid) {
            return IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> HouseOwnerCreateUpdateAccommodationImageRequest.builder()
                            .name("Aaaa")
                            .url("Aaaa")
                            .build())
                    .collect(Collectors.toSet());
        }
        return null;
    }
}
