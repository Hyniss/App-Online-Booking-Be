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
public class AdminViewDetailedUserRequest extends BaseRequest {
    @NonNull
    private final Integer id;

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<AdminViewDetailedUserRequest> {
        private final UserRepository userRepository;

        @Override
        protected void validate() {
            this.rejectIfEmpty(Fields.id, this::validateUserId);
        }

        private String validateUserId() {
            Integer adminId =  User.currentUserId()
                    .orElseThrow();

            User admin = this.userRepository
                    .findById(adminId)
                    .orElseThrow(() -> ApiException.badRequest("Không tìm thấy người dùng."));

            if(!admin.is(User.Role.ADMIN)) {
                return "Vai trò của người dùng không phải là quản trị viên.";
            }

            return null;
        }
    }
}
