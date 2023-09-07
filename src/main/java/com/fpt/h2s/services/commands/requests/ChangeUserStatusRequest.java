package com.fpt.h2s.services.commands.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.UserRepository;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;

@Getter
@Builder
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class ChangeUserStatusRequest extends BaseRequest {

    @NonNull
    private final Integer id;
    @NonNull
    private final User.Status status;

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<ChangeUserStatusRequest> {
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
                return "Không thể cấm người dùng này do tài khoản của người dùng chưa được kích hoạt.";
            }


            Integer adminId =  User.currentUserId()
                    .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));

            if(adminId.equals(this.request.id)) {
                return "Admin can not ban himself.";
            }
            return null;
        }

        private String validateStatus() {
            if(this.request.status == User.Status.PENDING) {
                return "Không thể đổi trạng thái của người dùng sang đang được xử lý (PENDING).";
            }
            return null;
        }
    }

}
