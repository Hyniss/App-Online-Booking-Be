package com.fpt.h2s.utilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.repositories.RedisRepository;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.function.Supplier;

@UtilityClass
public class Caches {

    public static  <T> T storeIfNotFound(String key, Supplier<T> supplier, Duration duration) {
        if (RedisRepository.hasKey(key)) {
            return RedisRepository.get(key, new TypeReference<T>() {}).orElseThrow();
        }

        final T cacheItem = supplier.get();
        RedisRepository.set(key, cacheItem, duration);
        return cacheItem;
    }

    public static  <T> T storeIfNotFound(String key, Supplier<T> supplier) {
        return storeIfNotFound(key, supplier, Duration.ofHours(1));
    }
}
