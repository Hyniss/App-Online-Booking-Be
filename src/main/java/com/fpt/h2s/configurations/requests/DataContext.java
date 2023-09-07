package com.fpt.h2s.configurations.requests;

import ananta.utility.StringEx;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class DataContext {

    public static class DataExistedInStorageException extends RuntimeException {
        public DataExistedInStorageException(@NonNull final String message, @Nullable final Object... args) {
            super(StringEx.format(message, args));
        }
    }

    private static final ThreadLocal<Map<String, Object>> context = new ThreadLocal<>();

    /**
     * Allow you to store values that takes a lot of time to calculate (by reading file or from database queries)
     * which can be used later in deeper layer. Example: Pass value from validators to services...
     *
     * @param key   can not be null.
     * @param value can not be null.
     * @throws DataExistedInStorageException if key already existed.
     */
    public static void store(@NonNull final String key, @NonNull final Object value) {
        final Map<String, Object> data = getDataFromContext();
        if (data.containsKey(key)) {
            throw new DataExistedInStorageException("There is already value with key {} in storage. Please use another name.", key);
        }
        data.put(key, value);
    }

    /**
     * Allow you to store values that takes a lot of time to calculate (by reading file or from database queries)
     * which can be used later in deeper layer. Example: Pass value from validators to services...
     * This method will save value with key as the value's class name.
     *
     * @param value can not be null. Its class name will be used as the key.
     * @throws DataExistedInStorageException if key already existed.
     */
    public static void store(@NonNull final Object value) {
        store(value.getClass().getSimpleName(), value);
    }

    /**
     * This method allows you to get value from context with specific key.
     */
    public static Object get(final String key) {
        return getDataFromContext().get(key);
    }

    /**
     * This method allows you to get value from context with specific key.
     *
     * @param clazz key of the value and also the return type.
     * @throws ClassCastException       if cast value failed
     * @throws IllegalArgumentException if key not found.
     */
    @NonNull
    public static <T> T get(final Class<T> clazz) {
        return get(clazz.getSimpleName(), clazz);
    }

    /**
     * This method allows you to get value from context with specific key.
     *
     * @param type type of the returned value.
     * @return Return the cast value.
     * @throws ClassCastException       if cast value failed
     * @throws IllegalArgumentException if key not found.
     */
    public static <T> T get(final String key, final TypeReference<T> type) {
        final Object value = getDataFromContext().get(key);
        return classOf(type).cast(value);
    }

    /**
     * This method allows you to get value from context with specific key.
     *
     * @param clazz class of the returned value.
     * @return Return the cast value.
     * @throws ClassCastException       if cast value failed
     * @throws IllegalArgumentException if key not found.
     */
    @NonNull
    public static <T> T get(final String key, final Class<T> clazz) {
        final Object value = getDataFromContext().get(key);
        if (value == null) {
            throw new IllegalArgumentException("Key %s is not existed in context.".formatted(key));
        }
        return clazz.cast(value);
    }

    /**
     * Check if exists any value with desired key or not.
     */
    public static boolean has(final String key) {
        return getDataFromContext().get(key) != null;
    }

    /**
     * Check if exists any value with desired key or not.
     */
    public static boolean has(final Class<?> key) {
        return has(key.getSimpleName());
    }

    /**
     * Clear all data inside context. Please use this function after request is finished.
     */
    public static void clear() {
        context.remove();
    }

    private static <T> Class<T> classOf(final TypeReference<T> ref) {
        return (Class<T>) ((ParameterizedType) ref.getType()).getRawType();
    }

    @NotNull
    private static Map<String, Object> getDataFromContext() {
        Map<String, Object> data = context.get();
        if (data == null) {
            data = new HashMap<>();
            context.set(data);
        }
        return data;
    }

}