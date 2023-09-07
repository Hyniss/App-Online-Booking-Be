package com.fpt.h2s.services.commands.room;

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
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateRoomImageRequest;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateRoomPropertyRequest;
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
public class HouseOwnerValidateCreateRoomCommand implements
        BaseCommand<HouseOwnerValidateCreateRoomCommand.Request, Void> {

    @Override
    public ApiResponse<Void> execute(final HouseOwnerValidateCreateRoomCommand.Request request) {
        return ApiResponse.success("Thông tin hợp lệ.");
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {
        private Integer accommodationId;

        @NotBlank(message = "Xin vui lòng nhập tên phòng.")
        @Length(max = 512, message = "Xin vui lòng nhập tên phòng trong khoảng 512 ký tự.")
        private String name;

        @NotBlank(message = "Xin vui lòng chọn trạng thái của phòng.")
        private String status;

        @NotBlank(message = "Xin vui lòng nhập giá của phòng.")
        private String price;

        @NotBlank(message = "Xin vui lòng không để trống phần trăm giá giảm. Nếu không giảm giá phòng xin vui lòng để bằng 0.")
        private String weekdayDiscountPercent;

        @NotBlank(message = "Xin vui lòng không để trống phần trăm giá giảm. Nếu không giảm giá phòng xin vui lòng để bằng 0.")
        private String weekendDiscountPercent;

        @NotBlank(message = "Xin vui lòng không để trống phần trăm giá giảm. Nếu không muốn giảm giá phòng xin vui lòng để bằng 0.")
        private String specialDayDiscountPercent;

        @NotBlank(message = "Xin vui lòng không để trống % giá trong ngày. Nếu bạn không muốn thay đổi thì có thể để bằng 100.")
        private String weekdayPricePercent;

        @NotBlank(message = "Xin vui lòng không để trống % giá trong ngày cuối tuần. Nếu bạn không muốn thay đổi thì có thể để bằng 100.")
        private String weekendPricePercent;

        @NotBlank(message = "Xin vui lòng không để trống % giá trong các ngày lễ tết. Nếu bạn không muốn thay đổi thì có thể để bằng 100.")
        private String specialDayPricePercent;

        @NotBlank(message = "Xin vui lòng nhập số lượng phòng. Với chỗ ở thuộc loại hình là nhà (HOUSE) hoặc căn hộ (APARTMENT) thì chỉ có duy nhất 1 phòng.")
        private String count;

        @NotEmpty(message = "Xin vui lòng để ảnh của phòng.")
        private Set<HouseOwnerCreateUpdateRoomImageRequest> roomImageRequests;

        @NotEmpty(message = "Xin vui lòng điền 6 thuộc tính của phòng.")
        private Set<HouseOwnerCreateUpdateRoomPropertyRequest> roomPropertyRequests;

        @NotEmpty(message = "Xin vui lòng lựa chọn các tiện ích của phòng.")
        private Set<Integer> amenities;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {

            private final AccommodationRepository accommodationRepository;
            private final ContractRepository contractRepository;
            private final CategoryRepository categoryRepository;

            @Override
            protected void validate() {
                if (this.request.accommodationId == null) {
                    this.rejectIfEmpty(Fields.accommodationId, this::validateId);
                    return;
                }
                this.rejectIfEmpty(Fields.accommodationId, this::validateId);
                this.rejectIfEmpty(Fields.status, this::validateStatus);
                this.rejectIfEmpty(Fields.price, this::validatePrice);
                this.rejectIfEmpty(Fields.weekdayDiscountPercent, this::validateWeekdayDiscount);
                this.rejectIfEmpty(Fields.weekendDiscountPercent, this::validateWeekendDiscount);
                this.rejectIfEmpty(Fields.specialDayDiscountPercent, this::validateSpecialDayDiscount);
                this.rejectIfEmpty(Fields.weekdayPricePercent, this::validateWeekdayPrice);
                this.rejectIfEmpty(Fields.weekendPricePercent, this::validateWeekendPrice);
                this.rejectIfEmpty(Fields.specialDayPricePercent, this::validateSpecialDayPrice);
                this.rejectIfEmpty(Fields.count, this::validateCount);
                this.rejectIfEmpty(Fields.roomImageRequests, this::validateSetImages);
                this.rejectIfEmpty(Fields.roomPropertyRequests, this::validateSetProperties);
                this.rejectIfEmpty(Fields.amenities, this::validateAmenities);
            }

            private String validateId() {
                if (this.request.accommodationId == null) {
                    return "Xin vui lòng không để trống ID của chỗ ở.";
                }

                Integer userId = User.currentUserId()
                        .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));

                final Contract contract = this.contractRepository.findContractByCreatorId(userId);
                if (contract == null) {
                    return "Xin vui lòng đăng kí làm người cho thuê chỗ ở trước khi quản lí chỗ ở. Người dùng thuộc tài khoản công ty không được phép quản lý chỗ ở.";
                }

                if (contract.is(Contract.Status.PENDING) || contract.is(Contract.Status.REJECTED)) {
                    return "Người dùng không thể quản lí chỗ ở do hợp đồng bị huỷ hoặc hợp đồng chưa được duyêt.";
                }

                final Accommodation accommodation = this.accommodationRepository
                        .findById(this.request.accommodationId)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", this.request.accommodationId));

                if (!Objects.equals(accommodation.getOwnerId(), userId)) {
                    return "Chỗ ở này không thuộc quyền quản lí của bạn.";
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

            private String validatePrice() {
                if(!this.request.price.matches("^[0-9]+$")) {
                    return "Xin vui lòng chỉ nhập số nguyên dương!";
                }
                long price = Long.parseLong(this.request.price);
                if(price < 100000 || price > 100000000) {
                    return "Xin vui lòng nhập giá của phòng trong khoảng từ 100.000 đến 100.000.000 vnđ.";
                }
                return null;
            }

            private String validateWeekdayDiscount() {
                if(!this.request.weekdayDiscountPercent.matches("^[0-9]+$")) {
                    return "Xin vui lòng chỉ nhập số nguyên dương!";
                }

                int discount = Integer.parseInt(this.request.weekdayDiscountPercent);
                if(discount < 0 || discount > 100) {
                    return "Phần trăm giá giảm phải trong khoảng từ 0% đến 100%.";
                }
                return null;
            }

            private String validateWeekendDiscount() {
                if(!this.request.weekendDiscountPercent.matches("^[0-9]+$")) {
                    return "Xin vui lòng chỉ nhập số nguyên dương!";
                }

                int discount = Integer.parseInt(this.request.weekendDiscountPercent);
                if(discount < 0 || discount > 100) {
                    return "Phần trăm giá giảm phải trong khoảng từ 0% đến 100%.";
                }
                return null;
            }

            private String validateSpecialDayDiscount() {
                if(!this.request.specialDayDiscountPercent.matches("^[0-9]+$")) {
                    return "Xin vui lòng chỉ nhập số nguyên dương!";
                }

                int discount = Integer.parseInt(this.request.specialDayDiscountPercent);
                if(discount < 0 || discount > 100) {
                    return "Phần trăm giá giảm phải trong khoảng từ 0% đến 100%.";
                }
                return null;
            }

            private String validateWeekdayPrice() {
                if(!this.request.weekdayPricePercent.matches("^[0-9]+$")) {
                    return "Xin vui lòng chỉ nhập số nguyên dương!";
                }

                int price = Integer.parseInt(this.request.weekdayPricePercent);
                if(price < 100 || price > 1000) {
                    return "Xin hãy để % giá trong ngày trong khoảng từ 100 đến 1000.";
                }
                return null;
            }

            private String validateWeekendPrice() {
                if(!this.request.weekendPricePercent.matches("^[0-9]+$")) {
                    return "Xin vui lòng chỉ nhập số nguyên dương!";
                }

                int price = Integer.parseInt(this.request.weekendPricePercent);
                if(price < 100 || price > 1000) {
                    return "Xin hãy để % giá trong ngày cuối tuần trong khoảng từ 100 đến 1000.";
                }
                return null;
            }

            private String validateSpecialDayPrice() {
                if(!this.request.specialDayPricePercent.matches("^[0-9]+$")) {
                    return "Xin vui lòng chỉ nhập số nguyên dương!";
                }

                int price = Integer.parseInt(this.request.specialDayPricePercent);
                if(price < 100 || price > 1000) {
                    return "Xin hãy để % giá trong các ngày lễ tết trong khoảng từ 100 đến 1000.";
                }
                return null;
            }

            private String validateCount() {
                final Accommodation accommodation = this.accommodationRepository
                        .findById(this.request.accommodationId)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", this.request.accommodationId));

                if(!this.request.count.matches("^[0-9]+$")) {
                    return "Xin vui lòng chỉ nhập số nguyên dương cho số phòng!";
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

            private String validateSetImages() {
                return request.roomImageRequests.stream()
                        .flatMap(request1 -> Stream.of(
                                validateImageName(request1.getName()),
                                validateImage(request1.getUrl())
                        ))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
            }

            private String validateSetProperties() {
                String message = this.request.roomPropertyRequests.stream()
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

                if (message != null) {
                    return message;
                }

                if (this.request.roomPropertyRequests.size() != 6) {
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
                if (type != Category.Type.AMENITY) {
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

