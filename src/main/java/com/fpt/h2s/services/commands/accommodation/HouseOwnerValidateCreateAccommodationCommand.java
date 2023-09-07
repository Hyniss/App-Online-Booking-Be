package com.fpt.h2s.services.commands.accommodation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Category;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.CategoryRepository;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateAccommodationImageRequest;
import com.fpt.h2s.utilities.MoreStrings;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class HouseOwnerValidateCreateAccommodationCommand  implements
        BaseCommand<HouseOwnerValidateCreateAccommodationCommand.Request, Void> {

    @Override
    public ApiResponse<Void> execute(final Request request) {
        return ApiResponse.success("Thông tin hợp lệ.");
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        @NotBlank(message = "Xin vui lòng nhập tên chỗ ở.")
        @Length(max = 1024, message = "Xin vui lòng nhập tên chỗ ở trong khoảng 1024 ký tự.")
        private String name;

        @NotBlank(message = "Xin vui lòng nhập đường dẫn hình ảnh đại diện của chỗ ở.")
        @Length(max = 1024, message = "Xin vui lòng nhập đường dẫn hình ảnh hợp lệ.")
        private String thumbnail;

        @NotBlank(message = "Xin vui lòng nhập mô tả ngắn gọn cho chỗ ở này.")
        @Length(max = 400, message = "Xin vui lòng nhập mô tả ngắn trong khoảng 400 ký tự.")
        private String shortDescription;

        @NotBlank(message = "Xin vui lòng nhập mô tả chi tiết cho chỗ ở này.")
        @Length(max = 2048, message = "Xin vui lòng nhập mô tả chi tiết trong khoảng 2048 ký tự.")
        private String description;

        @NotBlank(message = "Xin vui lòng nhập địa chỉ hiện tại của chỗ ở.")
        @Length(max = 2048, message = "Xin vui lòng nhập địa chỉ hiện tại của chỗ ở trong khoảng 2048 ký tự.")
        private String address;

        @NotNull(message = "Xin vui lòng chọn vị trí hiện tại của chỗ ở.")
        @DecimalMin(value = "-90", message = "Kinh độ trong khoảng từ -90 đến 90.")
        @DecimalMax(value = "90", message = "Kinh độ trong khoảng từ -90 đến 90.")
        private Double latitude;

        @NotNull(message = "Xin vui lòng chọn vị trí hiện tại của chỗ ở.")
        @DecimalMin(value = "-180", message = "Vĩ độ trong khoảng từ -180 đến 180.")
        @DecimalMax(value = "180", message = "Vĩ độ trong khoảng từ -180 đến 180.")
        private Double longitude;

        @NotBlank(message = "Xin vui lòng lựa chọn loại hình của chỗ ở.")
        private String type;

        @NotEmpty(message = "Xin vui lòng chọn 5 bức ảnh hoặc nhiều hơn cho chỗ ở này.")
        private Set<HouseOwnerCreateUpdateAccommodationImageRequest> image;

        @NotEmpty(message = "Xin vui lòng lựa chọn các tiện ích xung quanh chỗ ở.")
        private Set<Integer> location;


        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {

            private final ContractRepository contractRepository;
            private final CategoryRepository categoryRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.type, this::validateTypes);
                this.rejectIfEmpty(Fields.thumbnail, this::validateThumbnail);
                this.rejectIfEmpty(Fields.location, this::validateLocation);
                this.rejectIfEmpty(Fields.image, this::validateSetImages);
            }

            private String validateTypes() {

                Integer userId = User.currentUserId()
                        .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));

                final Contract contract = this.contractRepository.findContractByCreatorId(userId);
                if(contract == null) {
                    return "Xin vui lòng đăng kí làm người cho thuê chỗ ở trước khi quản lí chỗ ở. Người dùng thuộc tài khoản công ty không được phép quản lý chỗ ở.";
                }

                if(contract.is(Contract.Status.PENDING) || contract.is(Contract.Status.REJECTED)) {
                    return "Người dùng không thể quản lí chỗ ở do hợp đồng bị huỷ hoặc hợp đồng chưa được duyêt.";
                }

                final Set<String> allowedTypes = new HashSet<>(Set.of("HOUSE", "APARTMENT", "HOTEL"));
                if (!allowedTypes.contains(this.request.type)) {
                    return MoreStrings.format("Loại hình của chỗ ở thuộc một trong các loại sau đây: {}.", allowedTypes);
                }
                return null;
            }

            private String validateThumbnail() {
                if (isValidURL(this.request.thumbnail)) {
                    return "Xin vui lòng nhập đường dẫn hình ảnh hợp lệ.";
                }
                return null;
            }

            private String validateLocation() {
                List<Category> categories = this.categoryRepository.findAllById(this.request.location);
                return categories.stream()
                        .flatMap(category -> {
                            if (categories.size() != this.request.location.size()) {
                                return Stream.of("Xin vui lòng chỉ chọn danh mục với thể loại là vị trí (location).");
                            } else {
                                return Stream.of(validateLocation(category.getType()));
                            }
                        })
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
            }

            private String validateLocation(Category.Type type) {
                if(type != Category.Type.LOCATION) {
                    return "Xin vui lòng chỉ chọn danh mục với thể loại là vị trí (location).";
                }
                return null;
            }

            private String validateSetImages() {
                String message = this.request.image.stream()
                        .flatMap(request -> Stream.of(
                                validateImageName(request.getName()),
                                validateImage(request.getUrl())
                        ))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);

                if (message != null) {
                    return message;
                }

                if (request.image.size() < 5) {
                    return "Xin vui lòng chọn 5 bức ảnh hoặc nhiều hơn cho chỗ ở này.";
                }
                return null;
            }

            private String validateImageName(String name) {
                if (StringUtils.isEmpty(name)) {
                    return "Xin vui lòng để tên ảnh cho bức ảnh này.";
                }
                if (name.length() > 512) {
                    return "Xin vui lòng để tên hình ảnh trong khoảng 512 ký tự.";
                }
                return null;
            }

            private String validateImage(String image) {
                if (StringUtils.isEmpty(image)) {
                    return "Xin vui lòng không để trống ảnh.";
                }
                if (image.length() > 1024 || isValidURL(image)) {
                    return "Xin vui lòng để đường dẫn hình ảnh hợp lệ.";
                }
                return null;
            }

            public boolean isValidURL(String url) {
                try {
                    new URL(url);
                    return false;
                } catch (MalformedURLException e) {
                    return true;
                }
            }
        }
    }
}
