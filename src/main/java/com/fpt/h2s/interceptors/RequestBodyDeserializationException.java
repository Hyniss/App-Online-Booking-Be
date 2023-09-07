package com.fpt.h2s.interceptors;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class RequestBodyDeserializationException extends RuntimeException {

    private final Map<String, Throwable> errors;

}
