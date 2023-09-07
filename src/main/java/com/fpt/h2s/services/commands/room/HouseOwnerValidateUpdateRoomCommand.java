package com.fpt.h2s.services.commands.room;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.CategoryRepository;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.repositories.RoomRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateRoomImageRequest;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateRoomPropertyRequest;
import com.fpt.h2s.utilities.MoreStrings;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class HouseOwnerValidateUpdateRoomCommand implements
        BaseCommand<HouseOwnerValidateUpdateRoomCommand.Request, Void> {
    @Override
    public ApiResponse<Void> execute(final HouseOwnerValidateUpdateRoomCommand.Request request) {
        return ApiResponse.success("Thông tin hợp lệ.");
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        @NotNull(message = "Xin vui lòng không để trống ID của phòng.")
        private Integer id;

        private Integer accommodationId;

        @NotBlank(message = "Xin vui lòng nhập tên phòng.")
        @Length(max = 512, message = "Xin vui lòng nhập tên phòng trong khoảng 512 ký tự.")
        private String name;

        @NotBlank(message = "Xin vui lòng chọn trạng thái của phòng.")
        private String status;

        @NotBlank(message = "Xin vui lòng nhập số lượng phòng. Với chỗ ở thuộc loại hình là nhà (HOUSE) hoặc căn hộ (APARTMENT) thì chỉ có duy nhất 1 phòng.")
        private String count;

        @NotNull(message = "Xin hãy chọn TRUE để cập nhật hình ảnh hoặc FALSE để không cập nhật hình ảnh.")
        private Boolean isUpdateImage;

        @NotEmpty(message = "Xin vui lòng để ảnh của phòng.")
        private Set<HouseOwnerCreateUpdateRoomImageRequest> images;

        @NotNull(message = "Xin hãy chọn TRUE để cập nhật thuộc tính của phòng hoặc FALSE để không cập nhật thuộc tính của phòng.")
        private Boolean isUpdateProperty;

        @NotEmpty(message = "Xin vui lòng điền 6 thuộc tính của phòng.")
        private Set<HouseOwnerCreateUpdateRoomPropertyRequest> properties;

        @NotEmpty(message = "Xin vui lòng lựa chọn các tiện ích của phòng.")
        private Set<Integer> amenities;


        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {

            private final AccommodationRepository accommodationRepository;
            private final RoomRepository roomRepository;
            private final ContractRepository contractRepository;
            private final CategoryRepository categoryRepository;

            @Override
            protected void validate() {
                if(this.request.accommodationId == null) {
                    this.rejectIfEmpty(Fields.accommodationId, this::validateAccommodationId);
                    return;
                }
                this.rejectIfEmpty(Fields.accommodationId, this::validateAccommodationId);
                this.rejectIfEmpty(Fields.id, this::validateRoomId);
                this.rejectIfEmpty(Fields.count, this::validateCount);
                this.rejectIfEmpty(Fields.status, this::validateStatus);
                this.rejectIfEmpty(Fields.images, this ::validateSetImages);
                this.rejectIfEmpty(Fields.properties, this::validateSetProperties);
                this.rejectIfEmpty(Fields.amenities, this::validateAmenities);
            }

            private String validateAccommodationId() {

                if(this.request.accommodationId == null) {
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

                final Accommodation accommodation = this.accommodationRepository
                        .findById(this.request.accommodationId)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", this.request.accommodationId));

                if (!Objects.equals(accommodation.getOwnerId(), houseOwnerId)) {
                    return "Chỗ ở này không thuộc quyền quản lí của bạn.";
                }
                return null;
            }

            private String validateRoomId() {

                final Room room = this.roomRepository.findById(this.request.id)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy phòng với id = {}.", this.request.id));

                if (!Objects.equals(this.request.accommodationId, room.getAccommodationId())) {
                    return "Phòng này không thuộc về chỗ ở hiện tại.";
                }

                return null;
            }

            private String validateCount() {
                final Accommodation accommodation = this.accommodationRepository
                        .findById(this.request.accommodationId)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", this.request.accommodationId));

                if(!this.request.count.matches("^[0-9]+$")) {
                    return "Xin vui lòng chỉ nhập số nguyên dương!";
                }

                int totalRooms = Integer.parseInt(this.request.count);
                if(totalRooms < 1 || totalRooms > 1000) {
                    return "Xin vui lòng để số lượng phòng trong khoảng từ 1 đến 1000.";
                }
                if(accommodation.is(Accommodation.Type.APARTMENT) || accommodation.is(Accommodation.Type.HOUSE)) {
                    if(totalRooms > 1) {
                        return "Xin vui lòng chọn lại thể loại của chỗ ở này. Chỗ ở thuộc loại CĂN HỘ hoặc NHÀ chỉ có duy nhất 1 phòng.";
                    }
                }
                return null;
            }


            private String validateStatus() {
                final Set<String> allowedStatus = new HashSet<>(Set.of("OPENING", "CLOSED"));
                if (!allowedStatus.contains(this.request.status)) {
                    return MoreStrings.format("Trạng thái của phòng thuộc một trong các loại sau đây: {}.", allowedStatus);
                }
                return null;
            }

            private String validateSetImages() {
                return request.images.stream()
                        .flatMap(request1 -> Stream.of(
                                validateImageName(request1.getName()),
                                validateImage(request1.getUrl())
                        ))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
            }

            private String validateSetProperties() {
                String message = this.request.properties.stream()
                        .flatMap(request -> {
                            if (request.getValue() == null) {
                                return Stream.of("Xin vui lòng không để trống thuộc tính của phòng.");
                            } else {
                                return Stream.of(validateValue(request.getValue(), request.getKeyId()));
                            }
                        })
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);

                if(message != null)  {
                    return message;
                }

                if(this.request.properties.size() != 6) {
                    return "Xin vui lòng chỉ điền 6 thuộc tính của phòng.";
                }

                return null;
            }

            private String validateAmenities() {
                List<Category> categories = this.categoryRepository.findAllById(this.request.amenities);
                return categories.stream()
                        .flatMap(category -> {
                            if (categories.size() != this.request.amenities.size()) {
                                return Stream.of("Xin vui lòng chỉ chọn danh mục với thể loại là tiện ích (amenities).");
                            } else {
                                return Stream.of(validateAmenity(category.getType()));
                            }
                        })
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
            }

            private String validateAmenity(Category.Type type) {
                if(type != Category.Type.AMENITY) {
                    return "Xin vui lòng chỉ chọn danh mục với thể loại là tiện ích (amenities).";
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

            private String validateValue(Integer value, Integer keyId) {
                if(keyId == 2 || keyId == 3 || keyId == 4 || keyId == 7) {
                    if(value < 1 || value > 10) {
                        return "Số giường, số phòng tắm, số phòng ngủ và số người lớn phải lớn hơn 0.";
                    }
                }
                if (value < 0 || value > 10) {
                    return "Xin vui lòng chọn từng thuộc tính của phòng trong khoảng cho phép từ 0 đến 10.";
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

