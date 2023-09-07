package com.fpt.h2s.services.commands.user;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Objects;

public class CheckToHouseOwnerCommand implements BaseCommand<CheckToHouseOwnerCommand.Request, Void> {

    @Override
    public ApiResponse<Void> execute(Request request) {
        return ApiResponse.success();
    }

    @Getter
    @Builder
    @FieldNameConstants
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        @NonNull
        private Integer userId;

        @JsonCreator
        public Request(@JsonProperty("userId") @NotNull Integer userId) {
            this.userId = userId;
        }

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {

            private final UserRepository userRepository;
            private final ContractRepository contractRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(Request.Fields.userId, this::validateUserId);
            }

            private String validateUserId() {
                Integer currentUserId = User.currentUserId()
                        .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));

                if (!Objects.equals(currentUserId, this.request.userId)) {
                    return "Có lỗi xảy ra. Vui lòng tải lại trang.";
                }

                final User user = this.userRepository.findById(this.request.userId)
                        .orElseThrow(() -> ApiException.badRequest("Không thể người dùng với id = {}.", this.request.userId));

                if (user.is(User.Role.BUSINESS_OWNER)
                        || user.is(User.Role.BUSINESS_ADMIN)
                        || user.is(User.Role.BUSINESS_MEMBER)) {
                    return "Người dùng thuộc tài khoản công ty không được phép đăng ký cho thuê nhà.";
                }

                if (user.is(User.Status.BANNED)
                        || user.is(User.Status.PENDING)) {
                    return "Xin vui lòng kiểm tra lại trạng thái tài khoản.";
                }

                if(user.is(User.Role.HOUSE_OWNER)) {
                    return "Người dùng đã đăng kí làm người cho thuê nhà.";
                }

                Contract contract = contractRepository.findContractByCreatorId(currentUserId);

                if(contract.is(Contract.Status.APPROVED)) {
                    return "Bạn đã có hợp đồng trên hệ thống. Vui lòng không tạo thêm.";
                }

                if(contract.is(Contract.Status.TERMINATED) || contract.is(Contract.Status.REJECTED)) {
                    return "Hợp đồng của bạn đã bị cấm do bạn vi phạm điều khoản của chúng tôi.";
                }

                if(user.getPhone() == null) {
                    return "Xin vui lòng thêm số điện thoại của bạn vào phần thông tin cá nhân trước khi đăng kí làm người cho thuê nhà.";
                }

                if(user.getEmail() == null) {
                    return "Xin vui lòng thêm email của bạn vào phần thông tin cá nhân trước khi đăng kí làm người cho thuê nhà.";
                }

                return null;
            }
        }
    }
}
