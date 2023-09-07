package com.fpt.h2s.repositories;

import ananta.utility.StringEx;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.SpringBeans;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Log4j2
public class RedisRepository {

    private static StringRedisTemplate redis;

    public static final int DEFAULT_DURATION_IN_HOURS = 24;

    private static final Duration DEFAULT_DURATION = Duration.ofHours(RedisRepository.DEFAULT_DURATION_IN_HOURS);

    private RedisRepository(final StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static StringRedisTemplate getRedis() {
        if (RedisRepository.redis == null) {
            RedisRepository.redis = SpringBeans.getBean(StringRedisTemplate.class);
        }
        return RedisRepository.redis;
    }

    /**
     * Allow to set a value to Redis. The value will be available in a certain duration.
     * After that, Redis will remove the key and value from its memory.
     *
     * @param key               unique key of the value. Must be not blank.
     * @param value             Must be not null.
     * @param durationInSeconds After this span of seconds, the key will be expired.
     * @throws ApiException if key is blank or value is null.
     */
    public static void set(@NonNull final String key, @NonNull final String value, final long durationInSeconds) {
        RedisRepository.getRedis()
            .opsForValue()
            .set(Objects.requireNonNull(key), Objects.requireNonNull(value), durationInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Allow to set a value to Redis. The value will be available in a certain duration.
     * After that, Redis will remove the key and value from its memory.
     *
     * @param key      unique key of the value. Must be not blank.
     * @param value    Must be not null.
     * @param duration After this duration, the key will be expired.
     * @throws ApiException if key is blank or value is null.
     */
    public static void set(@NonNull final String key, @NonNull final String value, @NonNull final Duration duration) {
        RedisRepository.getRedis().opsForValue().set(Objects.requireNonNull(key), Objects.requireNonNull(value), duration);
    }

    /**
     * Allow to set a value to Redis. The value will be available in {@value DEFAULT_DURATION_IN_HOURS} hours.
     * After that, Redis will remove the key and value from its memory.
     *
     * @param key   unique key of the value. Must be not blank.
     * @param value Must be not null.
     * @throws ApiException if key is blank or value is null.
     */
    public static void set(@NonNull final String key, @NonNull final String value) {
        RedisRepository.getRedis().opsForValue().set(Objects.requireNonNull(key), Objects.requireNonNull(value), RedisRepository.DEFAULT_DURATION);
    }

    /**
     * Allow to set a value to Redis. The value will be available in a certain duration.
     * After that, Redis will remove the key and value from its memory.
     *
     * @param key               unique key of the value. Must be not blank.
     * @param value             Must be not null.
     * @param durationInSeconds After this span of seconds, the key will be expired.
     * @throws ApiException if key is blank or value is null.
     */
    public static void set(@NonNull final String key, @NonNull final Object value, final long durationInSeconds) {
        final String json = Mappers.jsonOf(value);
        RedisRepository.getRedis().opsForValue().set(Objects.requireNonNull(key), json, durationInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Allow to set a value to Redis. The value will be available in a certain duration.
     * After that, Redis will remove the key and value from its memory.
     *
     * @param key      unique key of the value. Must be not blank.
     * @param value    Must be not null.
     * @param duration After this duration, the key will be expired.
     * @throws ApiException if key is blank or value is null.
     */
    public static void set(@NonNull final String key, @NonNull final Object value, @NonNull final Duration duration) {
        final String json = Mappers.jsonOf(value);
        RedisRepository.getRedis().opsForValue().set(Objects.requireNonNull(key), json, duration);
    }

    /**
     * Allow to set a value to Redis. The value will be available in {@value DEFAULT_DURATION_IN_HOURS} hours.
     * After that, Redis will remove the key and value from its memory.
     *
     * @param key   unique key of the value. Must be not blank.
     * @param value Must be not null.
     * @throws ApiException if key is blank or value is null.
     */
    public static void set(@NonNull final String key, @NonNull final Object value) {
        final String json = Mappers.jsonOf(value);
        RedisRepository.getRedis().opsForValue().set(Objects.requireNonNull(key), json, RedisRepository.DEFAULT_DURATION);
    }

    /**
     * Get a key from Redis as String.
     *
     * @param key unique key of the value. Must be not blank.
     * @return Redis object.
     * @throws ApiException if key is blank or value is null.
     */
    public static Optional<String> get(@NonNull final String key) {
        if (StringEx.isBlank(key)) {
            return Optional.empty();
        }
        return Optional.ofNullable(RedisRepository.getRedis().opsForValue().get(key));
    }

    /**
     * Get a key from Redis as object.
     *
     * @param key   unique key of the value. Must be not blank.
     * @param clazz type that you want to return. Must be not null.
     * @return empty if key is blank or get key failed or failed while extract object. Otherwise, return value object.
     */
    public static <T> Optional<T> get(@NonNull final String key, @NonNull final Class<T> clazz) {
        if (StringEx.isBlank(key)) {
            return Optional.empty();
        }
        final String json = RedisRepository.getRedis().opsForValue().get(key);
        return Optional.ofNullable(Mappers.mapToObjectFrom(json, clazz));
    }

    /**
     * Get a key from Redis as object.
     *
     * @param key  unique key of the value. Must be not blank.
     * @param type type that you want to return. Must be not null.
     * @return empty if key is blank or get key failed or failed while extract object. Otherwise, return value object.
     */
    public static <T> Optional<T> get(@NonNull final String key, @NonNull final TypeReference<T> type) {
        if (StringEx.isBlank(key)) {
            return Optional.empty();
        }
        final String json = RedisRepository.getRedis().opsForValue().get(key);
        return Optional.ofNullable(Mappers.mapToObjectFrom(json, type));
    }

    /**
     * Get the expired time of a key.
     *
     * @param key should not be blank.
     * @return 0 if key is blank. Otherwise, return the key's expired time.
     */
    public static long getExpiredTimeOf(@NonNull final String key) {
        if (StringEx.isBlank(key)) {
            return 0L;
        }
        return Optional.ofNullable(RedisRepository.getRedis().getExpire(key, TimeUnit.SECONDS)).orElse(0L);
    }

    /**
     * Check if Redis contains certain key. This method won't check if the key is blank.
     *
     * @param key should not be blank.
     * @return false if the key is blank. Otherwise, return the check result.
     */
    public static boolean hasKey(@NonNull final String key) {
        if (StringEx.isBlank(key)) {
            return false;
        }
        try {
            final Boolean value = RedisRepository.getRedis().hasKey(key);
            return Optional.ofNullable(value).orElse(false);
        } catch (final NullPointerException e) {
            return false;
        }
    }

    /**
     * Remove a key from Redis.
     *
     * @param key should not be null.
     * @return true if key is successfully removed. Otherwise, return false.
     * @throws ApiException if key is blank.
     */
    public static boolean remove(final String key) {
        ApiException.failedIf(StringEx.isBlank(key), "Key must not be blank.");
        return Optional.ofNullable(RedisRepository.getRedis().delete(key)).orElse(false);
    }
}