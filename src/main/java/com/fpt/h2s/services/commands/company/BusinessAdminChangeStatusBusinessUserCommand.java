package com.fpt.h2s.services.commands.company;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.UserResponse;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessAdminChangeStatusBusinessUserCommand implements BaseCommand<BusinessAdminChangeStatusBusinessUserCommand.Request, UserResponse> {

    private final UserRepository userRepository;
    @Override
    public ApiResponse<UserResponse> execute(Request request) {
        final User user = this.userRepository
                .findById(request.getId())
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", request.getId()));
        final User userToDisable = user.toBuilder().status(request.getStatus()).build();
        this.userRepository.save(userToDisable);
        return ApiResponse.success("Thay đổi trạng thái thành viên thành công", UserResponse.of(userToDisable));
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        @NonNull
        private final Integer id;
        @NonNull
        private final User.Status status;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {
            private final UserRepository userRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.status, this::validateStatus);
                this.rejectIfEmpty(Fields.id, this::validateUserId);
            }

            private String validateUserId() {

                User userToChangeStatus = this.userRepository
                        .findById(this.request.id)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", this.request.id));

                if(userToChangeStatus.getStatus() == User.Status.PENDING) {
                    return "Chỉ có thể thay đổi trạng thái của những người dùng đã được kích hoạt tài khoản.";
                }

                Integer businessAdminId =  User.currentUserId()
                        .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));

                if(businessAdminId.equals(this.request.id)) {
                    return "Người dùng không được phép tự thay đổi trạng thái của chính mình.";
                }

                User businessAdmin = this.userRepository
                        .findById(businessAdminId)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", businessAdminId));

                if(!businessAdmin.is(User.Role.BUSINESS_ADMIN) && !businessAdmin.is(User.Role.BUSINESS_OWNER)) {
                    return "Chỉ những người dùng có quyền là BUSINESS ADMIN hoặc BUSINESS OWNER mới được phép thực hiện.";
                }

                if(businessAdmin.is(User.Role.BUSINESS_OWNER) && userToChangeStatus.is(User.Role.BUSINESS_OWNER)) {
                    return "Bạn không có quyền thay đổi trạng thái tài khoản này.";
                }

                if(businessAdmin.is(User.Role.BUSINESS_ADMIN) && !userToChangeStatus.is(User.Role.BUSINESS_MEMBER)) {
                    return "Bạn không có quyền thay đổi trạng thái tài khoản này.";
                }

                return null;
            }

            private String validateStatus() {
                if(this.request.status == User.Status.PENDING) {
                    return "Có gì đó không ổn. Vui lòng kiểm tra lại trạng thái cần áp dụng cho thành viên này.";
                }
                return null;
            }
        }

    }
}