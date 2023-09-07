package com.fpt.h2s.services.commands.user;

import ananta.utility.StringEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.UserProfileRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class UpdateUserInformationCommand implements BaseCommand<UpdateUserInformationCommand.Request, Void> {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public ApiResponse<Void> execute(Request request) {
        User user = userRepository.getById(User.getCurrentId());
        updateProfileOf(user, request);

        log.info("Update user address for {} using with value {}", user.getId(), request.getAddress());
        return ApiResponse.success("Cập nhật thông tin thành công.");
    }

    private void updateProfileOf(User user, Request request) {
        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            UserProfile newProfile = UserProfile
                .builder()
                .userId(user.getId())
                .bio(request.bio)
                .address(request.address)
                .build();
            userProfileRepository.save(newProfile);
        }

        UserProfile userProfile = profile.withAddress(request.address).withBio(request.bio);
        userProfileRepository.save(userProfile);
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        private final String address;

        private final String bio;

    }

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<Request> {

        @Override
        protected void validate() {
            rejectIfEmpty(Request.Fields.address, () -> {
                if (request.address != null) {
                    if (StringEx.lengthOf(request.address) < 5 || StringEx.lengthOf(request.address) > 255) {
                        throw ApiException.badRequest("Địa chỉ phải có độ dài từ 5 đến 255 kí tự");
                    }
                }
                return null;
            });
            rejectIfEmpty(Request.Fields.bio, () -> {
                if (request.bio != null) {
                    if (StringEx.lengthOf(request.bio) > 1024) {
                        throw ApiException.badRequest("Mô tả phải có độ dài nhỏ hơn 1024 kí tự");
                    }
                }
                return null;
            });

        }

    }

}
