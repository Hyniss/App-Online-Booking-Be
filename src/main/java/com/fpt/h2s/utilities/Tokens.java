package com.fpt.h2s.utilities;

import ananta.utility.StringEx;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.RedisRepository;
import com.machinezoo.noexception.Exceptions;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Log4j2
@UtilityClass
public class Tokens {

    private static final Duration DEFAULT_DURATION = Duration.ofDays(15);
    private static final String BEARER = "Bearer ";

    private static final String TOKEN_REQUEST_KEY = "Authorization";

    /**
     * Get token from client request.
     *
     * @param request client request. Must be not null.
     * @return the extracted token.
     * @throws ApiException if no token found.
     */
    public static String getTokenFrom(@NonNull final HttpServletRequest request) {
        final String bearerToken = request.getHeader(TOKEN_REQUEST_KEY);
        if (StringEx.isNotBlank(bearerToken) && bearerToken.startsWith(BEARER)) {
            return StringEx.afterOf(BEARER, bearerToken);
        }
        throw ApiException.badRequest(
            "No token found in request. Please add token to header using key {}",
            TOKEN_REQUEST_KEY
        );
    }

    /**
     * Find token that attached in request header.
     *
     * @param request current request that you want to get token from.
     * @return Token if found in request. Otherwise, return empty value.
     */
    public static Optional<String> findTokenFrom(@NonNull final HttpServletRequest request) {
        return Exceptions.silence().get(() -> getTokenFrom(request));
    }

    public static Optional<Integer> findUserIdFromToken(@NonNull final String token, final String key) {
        try {
            final Claims claims = Jwts
                .parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();

            return Optional.of(Integer.parseInt(claims.getId()));
        } catch (final Exception e) {
            log.warn(e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Generate a token. The token will be expired in 15 days as default.
     *
     * @param payload information that want to save to token.
     * @return generated token that can be used in 15 days.
     */
    public static String generateToken(@NonNull final Map<String, Object> payload, final String key) {
        return generateToken(payload, DEFAULT_DURATION, key);
    }

    /**
     * Generate a token. The token will be expired in 15 days as default.
     *
     * @param payload  information that want to save to token.
     * @param duration Token will be available in this duration.
     * @return generated token that can be used in input duration.
     */
    public static String generateToken(@NonNull final Map<String, Object> payload, @NonNull final Duration duration, final String key) {
        return generateToken(payload, key, duration.getSeconds() * 1000);
    }

    /**
     * Generate a token. The token will be expired in 15 days as default.
     *
     * @param payload               information that want to save to token.
     * @param timeOutInMilliSeconds Token will be available in this span of milliseconds.
     * @return generated token that can be used in input timeOutInMilliSeconds.
     */
    public static String generateToken(@NonNull final Map<String, Object> payload, final String key, final Long timeOutInMilliSeconds) {
        return generateToken(payload, timeOutInMilliSeconds, key);
    }

    /**
     * Check if a token is valid.
     *
     * @param token token that you want to validate. Should not be null.
     * @throws ApiException if validate token failed.
     */
    public static void validateToken(final String token, final String key) {
        ApiException.unauthorizedIf(StringEx.isBlank(token), "No token found. Please login to get one.");
        ApiException.unauthorizedIf(Tokens.isTokenExpired(token, key), "Your token is expired.");
        ApiException.unauthorizedIfNot(Tokens.isTokenValid(token), "Your token is invalid.");
    }

    /**
     * Check if a token is expired.
     *
     * @param token token that you want to check.
     * @return true if token is expired. Otherwise, return false.
     */
    public static boolean isTokenExpired(@NonNull final String token, final String key) {
        final Date expiration = getClaimOf(token, key).getExpiration();
        final Date now = Date.from(Instant.now());
        return now.after(expiration);
    }

    private static boolean isTokenValid(@NonNull final String token) {
        return RedisRepository.hasKey(token);
    }

    public static Claims getClaimOf(@NonNull final String token, final String key) {
        try {
            return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch (final Exception e) {
            throw ApiException.badRequest("Can't recognize token.");
        }
    }

    private String generateToken(
        @NonNull final Map<String, Object> map,
        final long timeOutInMilliseconds,
        final String key
    ) {
        final Claims claims = Jwts.claims();
        claims.putAll(map);

        return Jwts
            .builder()
            .setClaims(claims)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + timeOutInMilliseconds))
            .signWith(SignatureAlgorithm.HS256, key)
            .compact();
    }

}
