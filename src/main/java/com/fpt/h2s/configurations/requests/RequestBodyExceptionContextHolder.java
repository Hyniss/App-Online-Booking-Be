package com.fpt.h2s.configurations.requests;

import com.fasterxml.jackson.core.JsonParser;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class RequestBodyExceptionContextHolder {

    public static final Object DEFAULT_VALUE_WHEN_DESERIALIZATION_FAILED = null;

    /**
     * Thread local helps us to store exceptions separately for each request.
     */
    private static final ThreadLocal<Map<String, Throwable>> context = new ThreadLocal<>();

    private static Map<String, Throwable> getExceptionsFromContext() {
        Map<String, Throwable> data = context.get();
        if (data == null) {
            data = new LinkedHashMap<>();
            context.set(data);
        }
        return data;
    }

    public static Map<String, Throwable> getStoredExceptions() {
        return Collections.unmodifiableMap(getExceptionsFromContext());
    }

    public static void clearExceptions() {
        context.remove();
    }

    public static void collectFailedFieldWhileDeserialization(final JsonParser parser, final Throwable exception) throws IOException {
        final Map<String, Throwable> exceptions = getExceptionsFromContext();
        final String fieldName = Optional.ofNullable(parser.getCurrentName()).orElseGet(() -> parser.getParsingContext().getParent().getCurrentName());
        exceptions.put(fieldName, exception);
    }

}
