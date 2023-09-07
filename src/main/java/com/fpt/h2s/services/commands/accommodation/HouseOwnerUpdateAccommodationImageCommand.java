package com.fpt.h2s.services.commands.accommodation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.AccommodationImage;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationImageRepository;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateUpdateAccommodationImageRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class HouseOwnerUpdateAccommodationImageCommand
        implements BaseCommand<HouseOwnerUpdateAccommodationImageCommand.Request, Void> {

    private final AccommodationImageRepository accommodationImageRepository;

    @Override
    public ApiResponse<Void> execute(HouseOwnerUpdateAccommodationImageCommand.Request request) {

        final Set<AccommodationImage> originAccommodationImages
                = this.accommodationImageRepository.findAllByAccommodationId(request.getAccommodationId());
        final Set<AccommodationImage> updatedImages = request.getImages().stream()
                .map(image -> AccommodationImage
                        .builder()
                        .accommodationId(request.getAccommodationId())
                        .name(image.getName())
                        .image(image.getUrl())
                        .build())
                .collect(Collectors.toSet());
        this.accommodationImageRepository.saveAll(updatedImages);
        this.accommodationImageRepository.deleteAll(originAccommodationImages);

        return ApiResponse.success("Cập nhật chỗ ở thành công.");
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        @NotNull(message = "Xin vui lòng không để trống ID của chỗ ở.")
        private Integer accommodationId;

        @NotEmpty(message = "Xin vui lòng chọn 5 bức ảnh hoặc nhiều hơn cho chỗ ở này.")
        private Set<HouseOwnerCreateUpdateAccommodationImageRequest> images;
        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {

            private final AccommodationRepository accommodationRepository;
            private final ContractRepository contractRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.accommodationId, this::validateAccommodationId);
                this.rejectIfEmpty(Fields.images, this::validateSetImages);

            }
            private String validateAccommodationId() {
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

                if(!Objects.equals(accommodation.getOwnerId(), houseOwnerId)) {
                    return "Chỗ ở này không thuộc quyền quản lí của bạn.";
                }
                return null;
            }

            private String validateSetImages() {
                String message = request.images.stream()
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

                if (request.images.size() < 5) {
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
