package com.fpt.h2s.interceptors.proccessors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

@Component
class StringProcessor implements RequestProcessor<String> {
    @Override
    public JsonDeserializer<String> getJsonDeserializer() {
        return new JsonDeserializer<>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) {
                try {
                    final String value = p.getValueAsString();
                    if (value == null) {
                        return null;
                    }
                    return value.trim();
                } catch (IOException e) {
                    return null;
                }
            }
        };
    }

    @Override
    public JsonSerializer<String> getJsonSerializer() {
        return new JsonSerializer<>() {
            @SneakyThrows
            @Override
            public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(value);
                }
            }
        };
    }

    @Override
    public Formatter<String> getTypedFieldFormatter() {
        return new Formatter<>() {
            @Override
            public @NotNull String parse(@NotNull String text, @NotNull Locale locale) {
                return text.trim();
            }

            @Override
            public @NotNull String print(@NotNull String object, @NotNull Locale locale) {
                return object;
            }
        };
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}