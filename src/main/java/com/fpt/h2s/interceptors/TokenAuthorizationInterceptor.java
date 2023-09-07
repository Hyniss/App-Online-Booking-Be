package com.fpt.h2s.interceptors;

import ananta.utility.CollectionEx;
import ananta.utility.SetEx;
import com.fpt.h2s.models.annotations.RequireStatus;
import com.fpt.h2s.models.annotations.RequiredRoles;
import com.fpt.h2s.models.domains.TokenUser;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.models.exceptions.NoAnnotationPresentException;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.utilities.MoreRequests;
import com.fpt.h2s.utilities.Tokens;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Component
@AllArgsConstructor
public class TokenAuthorizationInterceptor implements ApiInterceptor {

    private static final RequestAnnotationResolver<RequiredRoles> AUTHORIZATION_RESOLVER = RequestAnnotationResolver.of(RequiredRoles.class);
    private static final RequestAnnotationResolver<RequireStatus> STATUS_RESOLVER = RequestAnnotationResolver.of(RequireStatus.class);

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final Object handler
    ) throws Exception {
        final String token = Tokens.findTokenFrom(request).orElse(null);

        try {
            final HandlerMethod apiMethod = MoreRequests.getCallingMethod(request);
            final TokenUser user = Optional.ofNullable(token).map(this::getTokenUserFrom).orElse(null);
            try {
                this.checkIfUserCanGo(user, AUTHORIZATION_RESOLVER.resolve(apiMethod).unbox());
            } catch (final NoAnnotationPresentException | ClassCastException ignored) {}
            try {
                this.checkIfUserCanGo(user, STATUS_RESOLVER.resolve(apiMethod).unbox());
            } catch (final NoAnnotationPresentException | ClassCastException ignored) {}
        } catch (final NoAnnotationPresentException | ClassCastException ignored) {
        }

        return ApiInterceptor.super.preHandle(request, response, handler);
    }

    private TokenUser getTokenUserFrom(final String token) {
        return RedisRepository.get(token, TokenUser.class).orElseGet(() -> TokenUser.builder().build());
    }

    private void checkIfUserCanGo(@Nullable final TokenUser user, @Nullable final RequiredRoles roles) {
        final boolean everyOneCanAccess = roles == null || roles.value().length == 0;
        if (everyOneCanAccess) {
            return;
        }
        final Set<User.Role> requiredRoles = SetEx.setOf(roles.value());
        final List<User.Role> userRoles = Optional.ofNullable(user)
            .map(TokenUser::id)
            .flatMap(id -> userRepository.findById(id).map(User::roleList))
            .orElse(Collections.emptyList());
        final boolean canAccess = CollectionEx.hasAnyOf(requiredRoles, userRoles);
        if (canAccess) {
            return;
        }
        if (user == null) {
            throw ApiException.unauthorized("Xin hãy đăng nhập để tiếp tục.");
        }
        throw ApiException.forbidden("User must have one of following roles: {}", requiredRoles);
    }

    private void checkIfUserCanGo(@Nullable final TokenUser user, @Nullable final RequireStatus roles) {
        if (user == null) {
            throw ApiException.unauthorized("Please login to continue");
        }

        Boolean canAccess = Optional.of(user)
            .map(TokenUser::id)
            .flatMap(userRepository::findById)
            .map(u -> u.isOneOf(roles.value()))
            .orElse(false);
        if (canAccess) {
            return;
        }

        throw ApiException.forbidden("User must have one of following status: {}", SetEx.setOf(roles.value()));
    }
}
