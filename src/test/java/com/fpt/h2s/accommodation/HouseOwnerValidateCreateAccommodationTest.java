package com.fpt.h2s.accommodation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.BaseTest;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.services.commands.accommodation.HouseOwnerValidateCreateAccommodationCommand;
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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class HouseOwnerValidateCreateAccommodationTest extends BaseTest {

    public static final String URL = "/accommodation/house-owner/validate";
    public static final String IMAGE = "https://h2s-s3.s3.ap-northeast-1.amazonaws.com/5a10d4ffb22444cd_20230809044715675%2B0000.jpg";
    public static final String token = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MTgwLCJlbWFpbCI6Ik5ndXllbiBOYW0iLCJ1c2VybmFtZSI6Ik5ndXllbiBOYW0iLCJwaG9uZSI6Iis4NDkxOTgyMDMxMSIsInNjb3BlcyI6WyJDVVNUT01FUiIsIkhPVVNFX09XTkVSIl0sImlhdCI6MTY5MjE5MDYyNywiZXhwIjoxNjkzNDg2NjI3fQ.w1TwHCx_ckNWef2P6fVGyQRp2d_URJ7yl6M-UpEJDes";

    @Test
    void should_success_when_use_english() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request request = HouseOwnerValidateCreateAccommodationCommand.Request
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
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
    void should_failed_if_name_is_null() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request request = HouseOwnerValidateCreateAccommodationCommand.Request
                .builder()
                .name(null)
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request.Fields.name);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập tên chỗ ở.");
    }

    @Test
    void should_failed_if_name_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request request = HouseOwnerValidateCreateAccommodationCommand.Request
                .builder()
                .name(gen1025Character())
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.name);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập tên chỗ ở trong khoảng 1024 ký tự.");
    }

    @Test
    void should_failed_if_thumbnail_is_null() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(null)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.thumbnail);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập đường dẫn hình ảnh đại diện của chỗ ở.");
    }

    @Test
    void should_failed_if_thumbnail_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail("Aaaa")
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.thumbnail);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập đường dẫn hình ảnh hợp lệ.");
    }

    @Test
    void should_failed_if_short_description_is_null() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription(null)
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.shortDescription);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập mô tả ngắn gọn cho chỗ ở này.");
    }

    @Test
    void should_failed_if_short_description_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription(gen1025Character())
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.shortDescription);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập mô tả ngắn trong khoảng 400 ký tự.");
    }

    @Test
    void should_failed_if_description_is_null() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .description(null)
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.description);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập mô tả chi tiết cho chỗ ở này.");
    }

    @Test
    void should_failed_if_description_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .description(gen2049Character())
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.description);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập mô tả chi tiết trong khoảng 2048 ký tự.");
    }

    @Test
    void should_failed_if_address_is_null() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address(null)
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.address);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập địa chỉ hiện tại của chỗ ở.");
    }

    @Test
    void should_failed_if_address_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address(gen2049Character())
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.address);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng nhập địa chỉ hiện tại của chỗ ở trong khoảng 2048 ký tự.");
    }

    @Test
    void should_failed_if_latitude_is_null() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(null)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.latitude);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn vị trí hiện tại của chỗ ở.");
    }

    @Test
    void should_failed_if_latitude_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(91d)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.latitude);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Kinh độ trong khoảng từ -90 đến 90.");
    }

    @Test
    void should_failed_if_longitude_is_null() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(null)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.longitude);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn vị trí hiện tại của chỗ ở.");
    }

    @Test
    void should_failed_if_longitude_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(181d)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.longitude);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Vĩ độ trong khoảng từ -180 đến 180.");
    }

    @Test
    void should_failed_if_type_is_null() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(null)
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.type);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng lựa chọn loại hình của chỗ ở.");
    }

    @Test
    void should_failed_if_type_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type("Tower")
                .image(getSetImages())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.type);
        Assertions.assertThat(MoreStrings.utf8Of(error)).startsWith("Loại hình của chỗ ở thuộc một trong các loại sau đây: [APARTMENT, HOUSE, HOTEL].");
    }

    @Test
    void should_failed_if_image_name_is_null() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImagesWithNameNull(true, false))
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.image);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để tên ảnh cho bức ảnh này.");
    }

    @Test
    void should_failed_if_image_name_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImagesWithNameNull(false, true))
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.image);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để tên hình ảnh trong khoảng 512 ký tự.");
    }

    @Test
    void should_failed_if_image_is_null() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImagesWithUrlNull(true, false))
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.image);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng không để trống ảnh.");
    }

    @Test
    void should_failed_if_image_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImagesWithUrlNull(false, true))
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.image);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng để đường dẫn hình ảnh hợp lệ.");
    }
    @Test
    void should_failed_if_image_size_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImagesUnder5Images())
                .location(getSetCategories())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.image);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chọn 5 bức ảnh hoặc nhiều hơn cho chỗ ở này.");
    }

    @Test
    void should_failed_if_location_is_null() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request 
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(null)
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.location);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng lựa chọn các tiện ích xung quanh chỗ ở.");
    }

    @Test
    void should_failed_if_location_is_invalid() throws Exception {
        final HouseOwnerValidateCreateAccommodationCommand.Request  request = HouseOwnerValidateCreateAccommodationCommand.Request
                .builder()
                .name("Sheraton Hanoi Hotel")
                .thumbnail(IMAGE)
                .shortDescription("Tận hưởng dịch vụ đỉnh cao, đẳng cấp thế giới tại Sheraton Hanoi Hotel")
                .name("Nằm cách trung tâm thành phố Hà Nội và Hồ Hoàn Kiếm 10 phút lái xe, khách sạn Sheraton Hanoi Hotel sang trọng này có tầm nhìn tuyệt đẹp ra quang cảnh Hồ Tây. Nơi đây cung cấp chỗ nghỉ hiện đại với hồ bơi ngoài trời, trung tâm thể dục và chỗ đỗ xe miễn phí trong khuôn viên.Phòng nghỉ rộng rãi tại đây được trang bị phòng tắm và buồng tắm vòi sen riêng biệt, két an toàn cá nhân, Internet WiFi, TV truyền hình cáp màn hình phẳng cũng như trà/cà phê miễn phí. Khách sạn có trung tâm thể dục 24 giờ với thiết bị tập thể dục, các phòng xông hơi khô, bể jacuzzi và spa. Oven D’or - Nhà hàng phục vụ ăn uống cả ngày - cung cấp tiệc tự chọn quốc tế. Hemispheres - Nhà hàng ăn ngon - phục vụ bít tết và hải sản. Lobby Lounge cung cấp các bữa ăn nhẹ, cà phê, trà chiều và đồ uống suốt cả ngày. Quán Bar Déjà Vu mang đến cho du khách một không gian sôi động để thưởng thức đồ uống có cồn và đồ ăn nhẹ vào buổi tối.")
                .address("Hanoi")
                .latitude(21.028511)
                .longitude(105.804817)
                .type(Accommodation.Type.HOTEL.name())
                .image(getSetImages())
                .location(getSetCategoriesInvalid())
                .build();
        final HashMap<String, String> response = this.executeRequest(request);
        final String error = response.get(HouseOwnerValidateCreateAccommodationCommand.Request .Fields.location);
        Assertions.assertThat(MoreStrings.utf8Of(error)).isEqualTo("Xin vui lòng chỉ chọn danh mục với thể loại là vị trí (location).");
    }


    private HashMap<String, String> executeRequest(final HouseOwnerValidateCreateAccommodationCommand.Request  request) throws Exception {
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

    private Set<HouseOwnerCreateUpdateAccommodationImageRequest> getSetImagesWithNameNull(Boolean isNull, Boolean isInvalid) {
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

    private Set<HouseOwnerCreateUpdateAccommodationImageRequest> getSetImagesWithUrlNull(Boolean isNull, Boolean isInvalid) {
        if (isNull) {
            return IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> HouseOwnerCreateUpdateAccommodationImageRequest.builder()
                            .name("Sheraton Hanoi Hotel")
                            .url(null)
                            .build())
                    .collect(Collectors.toSet());
        } else if (isInvalid) {
            return IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> HouseOwnerCreateUpdateAccommodationImageRequest.builder()
                            .name("Sheraton Hanoi Hotel")
                            .url("Aaaa")
                            .build())
                    .collect(Collectors.toSet());
        }
        return null;
    }
}
