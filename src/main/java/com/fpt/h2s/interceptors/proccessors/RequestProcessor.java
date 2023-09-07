package com.fpt.h2s.interceptors.proccessors;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import org.springframework.format.Formatter;

public interface RequestProcessor<T> {
    JsonDeserializer<T> getJsonDeserializer();
    JsonSerializer<T> getJsonSerializer();
    Formatter<T> getTypedFieldFormatter();
    Class<T> getType();
}