package com.fpt.h2s.services.commands.user.utils;

import ananta.utility.type.Couple;
import com.fpt.h2s.configurations.ConsulConfiguration;
import com.fpt.h2s.models.domains.OTP;
import com.fpt.h2s.models.domains.TokenUser;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.utilities.ImmutableCollectors;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.SpringBeans;
import com.fpt.h2s.utilities.Tokens;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@UtilityClass
public class AuthUtils {

    public static String getGeneratedTokenOf(User user) {
        final TokenUser tokenUser = TokenUser.of(user);
        ConsulConfiguration consul = SpringBeans.getBean(ConsulConfiguration.class);
        final String token = Tokens.generateToken(Mappers.mapOf(tokenUser), consul.get("secret-key.AUTH_TOKEN"));
        RedisRepository.set(token, tokenUser);
        return token;
    }

    public static String generateVerifyCode(final User user, final String redisKey, final Duration duration, final String secretKey) {
        assert user.getId() != null;
        final long time = System.currentTimeMillis();

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

    public static <T> void validate(final T retryKey, final OTP requestOTP, final OTP storedOTP, final Map<T, Integer> retryMap, final int maxTry, final Map<String, String> messages) {
        final Integer totalTried = retryMap.getOrDefault(retryKey, 0);
        if (totalTried >= maxTry) {
            throw ApiException.badRequest(messages.get("DISABLED"));
        }

        if (!requestOTP.equals(storedOTP)) {
            final int newTotalTried = totalTried + 1;
            retryMap.put(retryKey, newTotalTried);
            final int tryLeft = maxTry - newTotalTried;

            if (tryLeft == 0) {
                throw ApiException.badRequest(messages.get("DISABLED"));
            }

            throw ApiException.badRequest(messages.get("WRONG"), tryLeft);
        }
    }
}
