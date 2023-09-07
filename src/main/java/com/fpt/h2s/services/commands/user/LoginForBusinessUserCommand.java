package com.fpt.h2s.services.commands.user;

import ananta.utility.type.Couple;
import com.fpt.h2s.configurations.ConsulConfiguration;
import com.fpt.h2s.configurations.requests.DataContext;
import com.fpt.h2s.interceptors.models.MessageResolver;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.domains.TokenUser;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.AuthResponse;
import com.fpt.h2s.services.commands.user.utils.Urls;
import com.fpt.h2s.utilities.ImmutableCollectors;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.Tokens;
import com.fpt.h2s.workers.MailWorker;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
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

import static com.fpt.h2s.models.entities.User.Role.*;
import static com.fpt.h2s.models.entities.User.Status.BANNED;
import static com.fpt.h2s.models.entities.User.Status.PENDING;

@Log4j2
@Service
@RequiredArgsConstructor
public class LoginForBusinessUserCommand implements BaseCommand<LoginForBusinessUserCommand.LoginRequest, AuthResponse> {

    private final MessageResolver messageResolver;
    private final ConsulConfiguration consulConfiguration;
    private final MailWorker mailWorker;
    private final ConsulConfiguration consul;

    /**
     * Allow to send 1 email every 10 minutes.
     */
    private final Cache<String, Object> limitEmailCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(10000).build();

    @Override
    public ApiResponse<com.fpt.h2s.services.commands.responses.AuthResponse> execute(final LoginRequest request) {
        final User user = DataContext.get(User.class);
        if (user.is(PENDING)) {
            this.requireVerificationFor(user, request);
        }

        final TokenUser tokenUser = TokenUser.of(user);
        final String token = Tokens.generateToken(Mappers.mapOf(tokenUser), this.consulConfiguration.get("secret-key.AUTH_TOKEN"));
        RedisRepository.set(token, tokenUser);
        final AuthResponse response = AuthResponse.from(user, token);

        LoginForBusinessUserCommand.log.info("Login succeed for user with id {}", user.getId());
        return ApiResponse.success(this.messageResolver.get("LOGIN_SUCCESS"), response);
    }

    private void requireVerificationFor(final User user, final LoginRequest request) {
        this.sendVerificationToEmailOf(user);
        throw ApiException.forbidden(this.messageResolver.get("REQUIRE_LOGIN_EMAIL_VERIFICATION"));
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

        @NotBlank(message = "{EMAIL_BLANK}")
        @Email(message = "{EMAIL_INVALID})")
        private final String email;

        @NotBlank(message = "{PASSWORD_BLANK}")
        private final String password;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<LoginRequest> {

            private final UserRepository userRepository;
            private final MessageResolver messageResolver;
            private final PasswordEncoder passwordEncoder;

            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.email, this::validateEmail);
                this.rejectIfEmpty(Fields.password, this::validatePassword);
            }

            private String validatePassword() {
                final String message = this.messageResolver.get("PASSWORD_WRONG");
                final User user = this.userRepository.findByEmail(this.request.email).orElseThrow(() -> ApiException.notFound(message));
                final boolean isRightPassword = this.passwordEncoder.matches(this.request.getPassword(), user.getPassword());
                if (!isRightPassword) {
                    return message;
                }
                return null;
            }

            @Nullable
            private String validateEmail() {
                final User user = this.userRepository.findByEmail(this.request.getEmail()).orElseThrow(() -> ApiException.badRequest(this.messageResolver.get("EMAIL_NOT_FOUND")));
                if (user.is(BANNED)) {
                    return this.messageResolver.get("USER_IS_BANNED");
                }
                if (!user.isOneOf(BUSINESS_OWNER, BUSINESS_ADMIN, BUSINESS_MEMBER)) {
                    return "User is not belong to any business company.";
                }
                DataContext.store(user);
                return null;
            }

        }
    }
}
