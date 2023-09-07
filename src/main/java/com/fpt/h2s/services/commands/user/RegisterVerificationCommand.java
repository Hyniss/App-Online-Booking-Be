package com.fpt.h2s.services.commands.user;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.configurations.ConsulConfiguration;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.utilities.ExceptionPrinter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterVerificationCommand implements BaseCommand<RegisterVerificationCommand.RegisterRequest, Void> {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ConsulConfiguration consul;

    @Override
    public ApiResponse<Void> execute(final RegisterRequest request) {
        final Claims claims = this.getClaimOf(request.getCode());
        final String tokenKey = claims.get("r_k").toString();

        final String storedToken = RedisRepository.get(tokenKey).orElseThrow(() -> ApiException.badRequest("Token is invalid"));
        if (!storedToken.equals(request.getCode())) {
            throw ApiException.badRequest("Token is invalid");
        }

        final int userId = Integer.parseInt(claims.get("u_id").toString());
        final User user = this.userRepository
            .findById(userId)
            .orElseThrow(() -> ApiException.badRequest("User not found."));

        final User userToActive = user.toBuilder().status(User.Status.ACTIVE).build();
        this.userRepository.save(userToActive);

        RedisRepository.remove(tokenKey);

        RegisterVerificationCommand.log.info("Active user with id {} succeed.", userId);
        return ApiResponse.success("Verify succeed");
    }

    private Claims getClaimOf(@NonNull final String token) {
        try {
            return Jwts
                .parser()
                .setSigningKey(this.consul.get("secret-key.VERIFY_EMAIL_ADDRESS"))
                .parseClaimsJws(token)
                .getBody();
        } catch (final ExpiredJwtException e) {
            throw ApiException.badRequest("Token is expired.");
        } catch (final Throwable e) {
            throw ApiException.badRequest("Can't recognize token.");
        }
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class RegisterRequest extends BaseRequest {

        private final String code;

    }
}
