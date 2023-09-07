package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.user.utils.Urls;
import com.fpt.h2s.utilities.Tokens;
import com.fpt.h2s.workers.MailWorker;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class UpdateUserEmailCommand implements BaseCommand<UpdateUserEmailCommand.Request, Void> {

    public static final String KEY = "sdh8132871insuah9hd8y9312h21h01ja9dshasd9g71279982e98as";
    private final UserRepository userRepository;
    private final MailWorker mailWorker;

    @Override
    public ApiResponse<Void> execute(Request request) {
        User user = userRepository.getById(User.getCurrentId());

        Map<String, Object> payload = Map.ofEntries(
            Map.entry("userId", user.getId()),
            Map.entry("email", request.getEmail()),
            Map.entry("date", LocalDateTime.now().toString()),
            Map.entry("uuid", UUID.randomUUID())
        );

        String token = Tokens.generateToken(payload, KEY);
        this.mailWorker.sendMail(
            mail -> mail
                .sendTo(request.getEmail())
                .withTemplate(EmailTemplate.Key.UPDATE_EMAIL_VERIFICATION)
                .withProperty("username", user.getUsername())
                .withProperty("url", Urls.verifyUpdateEmailUrlOf(token))
                .withSuccessMessage("Sent verification email to user with id {}", user.getId())
        );

        RedisRepository.set("update-email-%s".formatted(user.getId()), request.getEmail(), Duration.ofMinutes(15));
        RedisRepository.set(token, "", Duration.ofMinutes(15));

        log.info("Sent update email to {}", request.getEmail());
        return ApiResponse.success("Chúng tôi vừa gửi mail xác thực đến email của bạn. Xin hãy xác thực để hoàn tất thay đổi");
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        @NotBlank(message = "Xin hãy nhập email")
        @Email(message = "Email không hợp lệ.")
        private final String email;
    }

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<Request> {

        private final UserRepository userRepository;

        @Override
        protected void validate() {
            this.rejectIfEmpty(Request.Fields.email, this::validateEmail);
        }

        private String validateEmail() {
            User user = userRepository.getById(User.currentUserId().orElseThrow(), "Không tìm thấy người dùng.");
            if (request.email.equalsIgnoreCase(user.getEmail())) {
                return "Xin hãy nhập email mới";
            }
            if (userRepository.existsByEmail(request.email)) {
                return "Email đã được sử dụng. Xin hãy sử dụng email khác.";
            }

            return null;
        }
    }

}