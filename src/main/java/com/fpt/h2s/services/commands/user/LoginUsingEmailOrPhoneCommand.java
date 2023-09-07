    package com.fpt.h2s.services.commands.user;

    import ananta.utility.StringEx;
    import ananta.utility.type.Couple;
    import com.fpt.h2s.configurations.ConsulConfiguration;
    import com.fpt.h2s.configurations.requests.DataContext;
    import com.fpt.h2s.models.domains.ApiResponse;
    import com.fpt.h2s.models.domains.BaseRequest;
    import com.fpt.h2s.models.domains.BaseValidator;
    import com.fpt.h2s.models.entities.EmailTemplate;
    import com.fpt.h2s.models.entities.User;
    import com.fpt.h2s.models.exceptions.ApiException;
    import com.fpt.h2s.repositories.RedisRepository;
    import com.fpt.h2s.repositories.UserRepository;
    import com.fpt.h2s.services.commands.BaseCommand;
    import com.fpt.h2s.services.commands.responses.AuthResponse;
    import com.fpt.h2s.services.commands.user.utils.AuthUtils;
    import com.fpt.h2s.services.commands.user.utils.Urls;
    import com.fpt.h2s.utilities.ImmutableCollectors;
    import com.fpt.h2s.workers.MailWorker;
    import com.github.benmanes.caffeine.cache.Cache;
    import com.github.benmanes.caffeine.cache.Caffeine;
    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.SignatureAlgorithm;
    import jakarta.validation.constraints.NotBlank;
    import lombok.Builder;
    import lombok.Getter;
    import lombok.NonNull;
    import lombok.RequiredArgsConstructor;
    import lombok.experimental.FieldNameConstants;
    import lombok.extern.jackson.Jacksonized;
    import lombok.extern.log4j.Log4j2;
    import org.jetbrains.annotations.NotNull;
    import org.jetbrains.annotations.Nullable;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Component;
    import org.springframework.stereotype.Service;

    import java.time.Duration;
    import java.util.Date;
    import java.util.Map;
    import java.util.UUID;
    import java.util.concurrent.TimeUnit;
    import java.util.stream.Stream;

    import static com.fpt.h2s.models.entities.User.Status.BANNED;
    import static com.fpt.h2s.models.entities.User.Status.PENDING;

@Log4j2
@Service
@RequiredArgsConstructor
public class LoginUsingEmailOrPhoneCommand implements BaseCommand<LoginUsingEmailOrPhoneCommand.LoginRequest, AuthResponse> {
    
    private final MailWorker mailWorker;
    private final ConsulConfiguration consul;
    
    /**
     * Allow to send 1 email every 10 minutes.
     */
    private final Cache<String, Object> limitEmailCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(10000).build();
    
    @Override
    public ApiResponse<AuthResponse> execute(final LoginRequest request) {
        final User user = DataContext.get(User.class);
        if (user.is(PENDING)) {
            this.requireVerificationFor(user, request);
        }

        final String token = AuthUtils.getGeneratedTokenOf(user);
        final AuthResponse response = AuthResponse.from(user, token);
    
        LoginUsingEmailOrPhoneCommand.log.info("Login succeed for user with id {}", user.getId());
        return ApiResponse.success("Đăng nhập thành công.", response);
    }

    private void requireVerificationFor(final User user, final LoginRequest request) {
        this.sendVerificationToEmailOf(user);
        throw ApiException.forbidden("Xin hãy kiểm tra email chúng tôi vừa gửi đến bạn để xác thực tài khoản");
    }
    
    private void sendVerificationToEmailOf(@NonNull final User savedUser) {
        final Duration tokenDuration = Duration.ofDays(3);
        final String redisKey = "user-id-%s-verification-token".formatted(savedUser.getId());
        if (this.limitEmailCache.getIfPresent(redisKey) != null) {
            return;
        }
        final String token = RedisRepository.get(redisKey).orElseGet(() -> this.generateVerifyCode(savedUser, redisKey, tokenDuration));
        
        this.mailWorker.sendMail(
            mail -> mail
                .sendTo(savedUser.getEmail())
                .withTemplate(EmailTemplate.Key.EMAIL_VERIFICATION)
                .withProperty("username", savedUser.getUsername())
                .withProperty("url", Urls.verifyEmailUrlOf(token))
                .withSuccessMessage("Sent verification email to user with id {}", savedUser.getId())
        );
        this.limitEmailCache.put(redisKey, token);
        RedisRepository.set(redisKey, token, tokenDuration);
    }
    
    private String generateVerifyCode(final User user, final String redisKey, final Duration duration) {
        assert user.getId() != null;
        final long time = System.currentTimeMillis();
        final String secretKey = this.consul.get("secret-key.VERIFY_EMAIL_ADDRESS");
        
        final Map<String, Object> payload = Stream.of(
            Couple.of("u_id", user.getId()),
            Couple.of("u_iid", UUID.randomUUID()),
            Couple.of("u_time", time),
            Couple.of("u_expired", time + duration.toMillis()),
            Couple.of("r_k", redisKey)
        ).collect(ImmutableCollectors.toLinkedMap(Couple::getLeft, Couple::getRight));
        
        final Claims claims = Jwts.claims().setId(user.getId().toString());
        claims.putAll(payload);
        
        return Jwts
            .builder()
            .setClaims(claims)
            .setIssuedAt(new Date(time))
            .setExpiration(new Date(time + duration.toMillis()))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class LoginRequest extends BaseRequest {
        
        @NotBlank(message = "Xin hãy nhập email hoặc số điện thoại")
        private final String emailOrPhone;
        
        @NotBlank(message = "Xin hãy nhập mật khẩu")
        private final String password;
        
        @NotNull
        private String toPhone() {
            if (StringEx.lengthOf(emailOrPhone) == 10) {
                return this.emailOrPhone.substring(1);
            }
            return this.emailOrPhone;
        }
        
        public boolean isUsingPhone() {
            return StringEx.hasDigitOnly(this.emailOrPhone);
        }
        
        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<LoginRequest> {
            
            private final UserRepository userRepository;
            private final PasswordEncoder passwordEncoder;
            
            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.emailOrPhone, this::validateEmailOrPhone);
                this.rejectIfEmpty(Fields.password, this::validatePassword);
            }
            
            private String validatePassword() {
                final User user = this.userRepository.findByEmail(this.request.emailOrPhone).orElseThrow(() -> ApiException.notFound("Mật khẩu sai, xin hãy nhập lại"));
                final boolean isRightPassword = this.passwordEncoder.matches(this.request.getPassword(), user.getPassword());
                if (!isRightPassword) {
                    return "Mật khẩu sai, xin hãy nhập lại";
                }
                return null;
            }
            
            private String validateEmailOrPhone() {
                if (this.request.isUsingPhone()) {
                    return this.validatePhone(this.request);
                }
                return this.validateEmail(this.request);
            }
            
            @Nullable
            private String validateEmail(final LoginRequest request) {
                final String email = request.emailOrPhone;
                if (!email.matches("^(.+)@(\\S+)$")) {
                    throw ApiException.badRequest("Email không hợp lệ");
                }
                final User user = this.userRepository.findByEmail(email).orElseThrow(() -> ApiException.badRequest("Không tìm thấy email."));
                if (user.is(BANNED)) {
                    return "Người dùng đã bị khóa";
                }
                DataContext.store(user);
                return null;
            }
            
            @Nullable
            private String validatePhone(final LoginRequest request) {
                final String phone = request.toPhone();
                if (!phone.matches("^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$")) {
                    return "Số điện thoại không hợp lệ.";
                }
                final User user = this.userRepository
                    .findByPhoneEndingWith("%" + phone)
                    .orElseThrow(() -> ApiException.badRequest("Không tìm thấy số điện thoại"));

                if (user.is(BANNED)) {
                    return "Người dùng đã bị khóa";
                }
                DataContext.store(user);
                return null;
            }
        }
    }
}
