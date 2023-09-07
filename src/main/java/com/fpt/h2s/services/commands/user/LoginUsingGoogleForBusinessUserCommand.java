package com.fpt.h2s.services.commands.user;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.configurations.ConsulConfiguration;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.TokenUser;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserProfileRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.AuthResponse;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.Tokens;
import com.fpt.h2s.workers.MailWorker;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

import static com.fpt.h2s.models.entities.User.Role.*;
import static com.fpt.h2s.models.entities.User.Status.*;
import static com.fpt.h2s.models.entities.User.Status.ACTIVE;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class LoginUsingGoogleForBusinessUserCommand implements BaseCommand<LoginUsingGoogleForBusinessUserCommand.LoginUsingGoogleRequest, AuthResponse> {
    
    public static final String GOOGLE_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=%s";
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final MailWorker mailWorker;
    private final ConsulConfiguration consulConfiguration;
    
    @Override
    public ApiResponse<AuthResponse> execute(final LoginUsingGoogleRequest request) {
        final GoogleOAuth2User oauthUser = LoginUsingGoogleForBusinessUserCommand.getGoogleUserUsingGoogleToken(request);
        final User user = this.userRepository
            .findByEmail(oauthUser.getEmail())
            .map(this::verifyUserIfPending)
            .orElseGet(() -> this.createUserFrom(oauthUser));
        
        final String token = Tokens.generateToken(Mappers.mapOf(TokenUser.of(user)), this.consulConfiguration.get("secret-key.AUTH_TOKEN"));
        RedisRepository.set(token, user);
        final AuthResponse response = AuthResponse.from(user, token);
        
        return ApiResponse.success(response);
    }
    
    private static GoogleOAuth2User getGoogleUserUsingGoogleToken(final LoginUsingGoogleRequest request) {
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpPost httpPost = new HttpPost(LoginUsingGoogleForBusinessUserCommand.GOOGLE_INFO_URL.formatted(request.getGoogleToken()));
            
            final HttpResponse response = httpClient.execute(httpPost);
            final String responseJson = EntityUtils.toString(response.getEntity());
            
            if (HttpStatusCode.valueOf(response.getStatusLine().getStatusCode()).isError()) {
                throw ApiException.badRequest("Oauth token invalid.");
            }
            
            if (responseJson == null) {
                throw ApiException.badRequest("Oauth token invalid.");
            }
            
            final HashMap<String, Object> content = Mappers.mapToObjectFrom(responseJson, new TypeReference<>() {
            });
            return new GoogleOAuth2User(content);
        } catch (final IOException e) {
            throw ApiException.badRequest("Validate Oauth token failed.");
        }
    }
    
    private User verifyUserIfPending(final User user) {
        if (user.is(BANNED)) {
            throw ApiException.badRequest("User is banned.");
        }
    
        if (!user.isOneOf(BUSINESS_OWNER, BUSINESS_ADMIN, BUSINESS_MEMBER)) {
            throw ApiException.badRequest("User is not belong to any business company.");
        }
        
        if (user.is(PENDING)) {
            final User userToActive = user.withStatus(ACTIVE);
            final User updatedUser = this.userRepository.save(userToActive);
            log.info("Verify user after login using Google.");
            return updatedUser;
        }
        return user;
    }
    
    private User createUserFrom(final GoogleOAuth2User googleOAuth2User) {
        final User userToInsert = User
            .builder()
            .username(googleOAuth2User.getUsername())
            .email(googleOAuth2User.getEmail())
            .status(ACTIVE)
            .roles(String.valueOf(User.Role.CUSTOMER))
            .build();
        
        final User newUser = this.userRepository.save(userToInsert);
        
        final UserProfile profile = UserProfile.builder()
            .userId(newUser.getId())
            .avatar(googleOAuth2User.avatar)
            .build();
        this.userProfileRepository.save(profile);
        
        if (newUser.getEmail() != null) {
            this.mailWorker.sendMail(
                mail -> mail
                    .sendTo(newUser.getEmail())
                    .withTemplate(EmailTemplate.Key.WELCOME_MAIL)
                    .withProperty("username", newUser.getUsername())
                    .withSuccessMessage("Sent welcome message to {}", newUser.getEmail())
            );
        }
        log.info("Create user using Google successfully.");
        return newUser;
    }
    
    @Getter
    @NoArgsConstructor
    @Setter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class LoginUsingGoogleRequest extends BaseRequest {
        private String googleToken;
    }
    
    @Getter
    static class GoogleOAuth2User {
        
        private final String username;
        
        private final String email;
        
        private final String avatar;
        
        public GoogleOAuth2User(final HashMap<String, Object> googleResponse) {
            this.username = (String) googleResponse.get("name");
            this.email = (String) googleResponse.get("email");
            this.avatar = (String) googleResponse.get("picture");
        }
    }
}
