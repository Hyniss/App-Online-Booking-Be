package com.fpt.h2s.services.commands.user;

import ananta.utility.StringEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.AuthResponse;
import com.fpt.h2s.utilities.MoreRequests;
import com.fpt.h2s.utilities.Tokens;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class GetNewTokenCommand implements BaseCommand<Void, AuthResponse> {

    private final UserRepository userRepository;

    @Override
    public ApiResponse<AuthResponse> execute(Void request) {
        String currentToken = Tokens.getTokenFrom(MoreRequests.getCurrentHttpRequest());
        String tokenKey = StringEx.format("{}-alt-token", currentToken);

        String alternativeToken = RedisRepository.get(tokenKey).orElseThrow();

        Integer loginUser = User.getCurrentId();
        User user = userRepository.getById(loginUser);
        final AuthResponse response = AuthResponse.from(user, alternativeToken);
        RedisRepository.remove(currentToken);
        return ApiResponse.success(response);
    }

}
