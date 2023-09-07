package com.fpt.h2s.services.commands.accommodation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Category;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.CategoryRepository;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Transactional
@RequiredArgsConstructor
public class HouseOwnerUpdateAccommodationCommand
        implements BaseCommand<HouseOwnerUpdateAccommodationCommand.Request, Void> {

    private final AccommodationRepository accommodationRepository;

    @Override
    public ApiResponse<Void> execute(HouseOwnerUpdateAccommodationCommand.Request request) {

        final Accommodation originAccommodation = accommodationRepository.findById(request.getId())
                .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));

        final Accommodation updatedAccommodation = request.toAccommodation(originAccommodation);
        this.accommodationRepository.save(updatedAccommodation);
        return ApiResponse.success("Cập nhật chỗ ở thành công.");

    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        private Integer id;

        @NotBlank(message = "Xin vui lòng nhập tên chỗ ở.")
        @Length(max = 1024, message = "Xin vui lòng nhập tên chỗ ở trong khoảng 1024 ký tự.")
        private String name;

        @NotBlank(message = "Xin vui lòng nhập đường dẫn hình ảnh đại diện của chỗ ở.")
        @Length(max = 1024, message = "Xin vui lòng nhập đường dẫn hình ảnh hợp lệ.")
        private String thumbnail;

        @NotBlank(message = "Xin vui lòng nhập mô tả ngắn gọn cho chỗ ở này.")
        @Length(max = 400, message = "Xin vui lòng nhập mô tả ngắn gọn trong khoảng 400 ký tự.")
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

        @NotNull(message = "Xin vui lòng lựa chọn loại hình của chỗ ở.")
        private Accommodation.Type type;

        @NotEmpty(message = "Xin vui lòng lựa chọn các tiện ích xung quanh chỗ ở.")
        private Set<Integer> location;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {

            private final AccommodationRepository accommodationRepository;
            private final ContractRepository contractRepository;
            private final CategoryRepository categoryRepository;


            @Override
            protected void validate() {
                if (this.request.id == null) {
                    this.rejectIfEmpty(Fields.id, this::validateAccommodationId);
                    return;
                }
                this.rejectIfEmpty(Fields.id, this::validateAccommodationId);
                this.rejectIfEmpty(Fields.type, this::validateType);
                this.rejectIfEmpty(Fields.thumbnail, this::validateThumbnail);
                this.rejectIfEmpty(Fields.location, this::validateLocation);
            }

            private String validateType() {
                final Accommodation accommodation = this.accommodationRepository
                        .findById(this.request.id)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", this.request.id));

                if(accommodation.getTotalRoom() > 1) {
                    if(Accommodation.Type.HOUSE == this.request.type
                            || Accommodation.Type.APARTMENT == this.request.type) {
                        return "Xin vui lòng chọn lại loại hình của chỗ ở này. Chỗ ở thuộc loại CĂN HỘ hoặc NHÀ chỉ có duy nhất 1 phòng.";
                    }
                }
                return null;
            }


            private String validateAccommodationId() {

                if(this.request.id == null) {
                    return "Xin vui lòng không để trống ID của chỗ ở.";
                }
                Integer houseOwnerId =  User.currentUserId()
                        .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));

                final Contract contract = this.contractRepository.findContractByCreatorId(houseOwnerId);
                if(contract == null) {
                    return "Xin vui lòng đăng kí làm người cho thuê chỗ ở trước khi quản lí chỗ ở. Người dùng thuộc tài khoản công ty không được phép quản lý chỗ ở.";
                }

                if(contract.is(Contract.Status.PENDING) || contract.is(Contract.Status.REJECTED)) {
                    return "Người dùng không thể quản lí chỗ ở do hợp đồng bị huỷ hoặc hợp đồng chưa được duyêt.";
                }

                final Accommodation accommodation = this.accommodationRepository.findById(this.request.id)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", this.request.id));

                if(!Objects.equals(accommodation.getOwnerId(), houseOwnerId)) {
                    return "Chỗ ở này không thuộc quyền quản lí của bạn.";
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

            public boolean isValidURL(String url) {
                try {
                    new URL(url);
                    return false;
                } catch (MalformedURLException e) {
                    return true;
                }
            }
        }

        public Accommodation toAccommodation(Accommodation existingRoom) {
            existingRoom.getCategories().clear();
            return existingRoom.toBuilder()
                    .name(this.name)
                    .thumbnail(this.thumbnail)
                    .shortDescription(this.shortDescription)
                    .description(this.description)
                    .address(this.address)
                    .latitude(this.latitude)
                    .longitude(this.longitude)
                    .type(this.type)
                    .categories(this.location
                            .stream()
                            .map(location -> Category
                                    .builder()
                                    .id(location)
                                    .build())
                            .collect(Collectors.toSet()))
                    .build();
        }
    }


}
