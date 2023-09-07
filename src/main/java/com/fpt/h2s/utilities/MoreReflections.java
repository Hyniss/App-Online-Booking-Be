package com.fpt.h2s.utilities;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

@UtilityClass
public class MoreReflections {
    /**
     * Find generic type for superclass.
     * @param type can be null.
     * @param index the index of the generic type you want.
     * @return Optional of generic type. Return empty if any exception caught or input is null.
     */
    @NotNull
    public static Optional<Class<?>> genericTypeOf(@Nullable final Type type, final int index) {
        if (type == null) {
            return Optional.empty();
        }
        try {
            final Type genericType = ((ParameterizedType) type).getActualTypeArguments()[index];
            return Optional.of((Class<?>) genericType);
        } catch (final ClassCastException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Find generic type for superclass.
     * @param type can be null.
     * @return Optional of generic type. Return empty if any exception caught or input is null.
     */
    @NotNull
    public static Optional<Class<?>> genericTypeOf(@Nullable final Type type) {
        return MoreReflections.genericTypeOf(type, 0);
    }
    
    /**
     * Find generic type for superclass.
     * @param clazz can be null. NOTE: This class must contains only one generic type.
     * @param index the index of the generic type you want.
     * @return Optional of generic type. Return empty if any exception caught or input is null.
     */
    @NotNull
    public static Optional<Class<?>> genericTypeOf(@Nullable final Class<?> clazz, final int index) {
        if (clazz == null) {
            return Optional.empty();
        }
        return genericTypeOf(clazz.getGenericSuperclass(), index);
    }
    
    /**
     * Find generic type for superclass.
     * @param clazz can be null. NOTE: This class must contains only one generic type.
     * @return Optional of generic type. Return empty if any exception caught or input is null.
     */
    @NotNull
    public static Optional<Class<?>> genericTypeOf(@Nullable final Class<?> clazz) {
        return genericTypeOf(clazz, 0);
    }
}
