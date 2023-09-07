package com.fpt.h2s.utilities;

import ananta.utility.MapEx;
import ananta.utility.ReflectionEx;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Mappers {
    
    public static final Set<? extends Class<? extends Annotation>> IGNORE_ANNOTATIONS = Set.of(ManyToOne.class, ManyToMany.class, OneToMany.class, OneToOne.class);
    
    /**
     * Create an object with specific class from a hashmap
     * @param map A map that contains values of the new object. <br/>
     * - Key: field name that exists in input class.
     * - Value: field value of new object.
     * @param clazz Class that you want to create from map.
     * @return null if input map is null. Otherwise, return new object with values extracted from map.
     */
    @Nullable
    @Contract("null, _ -> null")
    public static <T> T fromMap(@Nullable final Map<String, Object> map, @NonNull final Class<T> clazz) {
        if (map == null) {
            return null;
        }
        return Mappers.getMapper().convertValue(map, clazz);
    }
    
    @NotNull
    private static ObjectMapper getMapper() {
        return SpringBeans.getBean(ObjectMapper.class);
    }
    
    /**
     * Create an object with specific class from a hashmap
     * @param map A map that contains values of the new object. <br/>
     * - Key: field name that exists in input class.
     * - Value: field value of new object.
     * @param clazz Class that you want to create from map.
     * @return null if input map is null. Otherwise, return new object with values extracted from map.
     */
    @Nullable
    @Contract("null, _ -> null")
    public static <T> T fromMap(@Nullable final Map<String, Object> map, @NonNull final TypeReference<T> clazz) {
        if (map == null) {
            return null;
        }
        return Mappers.getMapper().convertValue(map, clazz);
    }
    
    /**
     * Convert an object into a hashmap with key is field names of object and value as field value.
     * @param object object that you want to convert to hashmap.
     * @return empty map if object is null.
     * Otherwise, return a hashmap contains field name as key and field value as value.
     */
    public static <T> Map<String, Object> mapOf(@Nullable final T object) {
        if (object == null) {
            return MapEx.emptyMap();
        }
        return Mappers.getMapper().convertValue(object, Map.class);
    }
    
    /**
     * Convert an object to json string.
     * @param object object that you want to serialize.
     * @return null if object is null. Otherwise, return its serialized json.
     */
    @SneakyThrows
    public static String jsonOf(final @Nullable Object object) {
        if (object == null) {
            return null;
        }
        return Mappers.getMapper().writeValueAsString(object);
    }
    
    /**
     * Convert from json to object.
     * @return null if json is null. Otherwise, return deserialized object.
     */
    @Contract("null, _ -> null")
    @SneakyThrows
    public static <T> T mapToObjectFrom(@Nullable final String json, @NonNull final Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return Mappers.getMapper().readValue(json, clazz);
        } catch (final Exception e) {
            return null;
        }
    }
    
    /**
     * Convert from json to object.
     * @return null if json is null. Otherwise, return deserialized object.
     */
    @Contract("null, _ -> null")
    @SneakyThrows
    public static <T> T mapToObjectFrom(@Nullable final String json, @NonNull final TypeReference<T> typeReference) {
        if (json == null) {
            return null;
        }
        return Mappers.getMapper().readValue(json, typeReference);
    }
    
    /**
     * Convert from object to another object.
     * @return null if object is null. Otherwise, return deserialized object.
     */
    @SneakyThrows
    @Contract("_, null -> null" )
    public static <T> T convertTo(@NonNull final Class<T> clazz, @Nullable final Object src) {
        if (src == null) {
            return null;
        }
        final T emptyInstance = Mappers.mapToObjectFrom("{}", clazz);
        Mappers.populateFieldValuesFor(emptyInstance, src);
        return emptyInstance;
    }
    
    /**
     * Convert from object to another object.
     * @return null if object is null. Otherwise, return deserialized object.
     */
    @SneakyThrows
    public static <T> T convertTo(@NonNull final TypeReference<T> typeReference, @Nullable final Object src) {
        if (src == null) {
            return null;
        }
        final T instance = Mappers.mapToObjectFrom("{}", typeReference);
        Mappers.populateFieldValuesFor(instance, src);
        return instance;
    }
    
    private static <T> void populateFieldValuesFor(final T emptyInstance, final @NotNull Object src) {
        final Object realSrc = Hibernate.unproxy(src);
        final List<Field> srcFields = ReflectionEx
            .fieldsOf(realSrc.getClass())
            .stream()
            .filter(field -> ReflectionEx.annotationsOf(field).stream().map(Annotation::annotationType).noneMatch(Mappers.IGNORE_ANNOTATIONS::contains))
            .toList();
        
        srcFields.forEach(field -> {
            try {
                ReflectionEx
                    .findFieldValue(field.getName(), realSrc)
                    .flatMap(value -> Optional.of(Mappers.mapToObjectFrom(Mappers.jsonOf(value), field.getType())))
                    .ifPresent(value -> ReflectionEx.setFieldValue(emptyInstance, field.getName(), value));
            } catch (final Exception e) {
            }
        });
    }

    private static final ModelMapper MODEL_MAPPER = new ModelMapper();

    static {
        MODEL_MAPPER.getConfiguration().setSourceNameTokenizer(NameTokenizers.UNDERSCORE);
    }

    public static <T> T fromTuple(Map<String, Object> tuple, Class<T> clazz) {
        return MODEL_MAPPER.map(tuple, clazz);
    }
}
