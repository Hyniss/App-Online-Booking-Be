package com.fpt.h2s.interceptors;

import ananta.utility.MapEx;
import ananta.utility.ReflectionEx;
import ananta.utility.StringEx;
import com.bugsnag.Bugsnag;
import com.bugsnag.Severity;
import com.fasterxml.jackson.core.JsonParseException;
import com.fpt.h2s.H2sApplication;
import com.fpt.h2s.configurations.ConsulConfiguration;
import com.fpt.h2s.configurations.requests.RequestBodyExceptionContextHolder;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.utilities.ExceptionPrinter;
import com.fpt.h2s.utilities.MoreRequests;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.MethodInvocationException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.ContentCachingRequestWrapper;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionAdvisor {
    
    private final DispatcherServlet servlet;

    private final ConsulConfiguration consulConfiguration;
    private Bugsnag exceptionLogger;
    
    @PostConstruct
    private void configSpringDefaultExceptionBehaviours() {
        this.enableThrowExceptionPageNotFound404Exception();
        ExceptionPrinter.registerPackage(H2sApplication.class.getPackageName());
        exceptionLogger = new Bugsnag(consulConfiguration.get("service.bugsnag.KEY"));
    }
    
    private void enableThrowExceptionPageNotFound404Exception() {
        this.servlet.setThrowExceptionIfNoHandlerFound(true);
    }
    
    //===============================================================================================================//
    //===============================================================================================================//
    //===========================================// Service Unavailable //===========================================//
    //===============================================================================================================//
    //===============================================================================================================//
    
    @ExceptionHandler(JedisConnectionException.class)
    public ResponseEntity<ApiResponse<Void>> handleJedisConnectionException(
        @NonNull final JedisConnectionException exception, final ContentCachingRequestWrapper requestWrapper
    ) {
        ExceptionPrinter.print(exception);
        this.logExceptionToWebsite(exception, requestWrapper, Severity.ERROR);
        return ResponseEntities.create("Redis setup failed.", HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    private void logExceptionToWebsite(@NotNull final Exception exception, final ContentCachingRequestWrapper request, final Severity severity) {
        String environment = System.getenv("ENVIRONMENT");
        final boolean isLocal = StringEx.isBlank(environment) || environment.equals("LOCAL");
        if (isLocal) {
            return;
        }
        ExceptionPrinter.print(exception);
        this.exceptionLogger.notify(
            exception,
            severity,
            (report) -> report
                .addToTab("Details", "Exception", StringEx.join("\n", ExceptionPrinter.getLinesToSprint(exception)))
                .addToTab("Details", "Request", getCurlOf(request))
                .addToTab("Details", "User id", User.currentUserId().orElse(null))
                .addToTab("Details", "Message", exception.getMessage())
        );
    }
    
    private static String getCurlOf(final ContentCachingRequestWrapper request) {
        try {
            return MoreRequests.getCurlOf(request);
        } catch (Exception e) {
            return null;
        }
    }
    
    //===============================================================================================================//
    //===============================================================================================================//
    //===============================================// Bad Request //===============================================//
    //===============================================================================================================//
    //===============================================================================================================//
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleHttpMessageNotReadableException(
        @NonNull final HttpMessageNotReadableException exception, final ContentCachingRequestWrapper requestWrapper
    ) {
        ExceptionPrinter.print(exception);
        final Map<String, Throwable> exceptions = RequestBodyExceptionContextHolder.getStoredExceptions();
        this.logExceptionToWebsite(exception, requestWrapper, Severity.WARNING);
        if (!exceptions.isEmpty()) {
            this.exceptionLogger.notify(new RequestBodyDeserializationException(exceptions));
            final Map<String, String> errors = ExceptionAdvisor.findExceptionDetails(exceptions);
            return ResponseEntities.badRequest(errors);
        }
        
        final String message = getRootCause(exception).getMessage();
        this.logExceptionToWebsite(exception, requestWrapper, Severity.WARNING);
        return ResponseEntities.badRequest(message, Collections.emptyMap());
    }
    
    @ExceptionHandler(RequestBodyDeserializationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleRequestBodyDeserializationException(
        @NonNull final RequestBodyDeserializationException exception, final ContentCachingRequestWrapper requestWrapper
    ) {
        ExceptionPrinter.print(exception);
        this.logExceptionToWebsite(exception, requestWrapper, Severity.WARNING);
        final Map<String, String> errors = ExceptionAdvisor.findExceptionDetails(exception.getErrors());
        return ResponseEntities.badRequest(errors);
    }
    
    @NotNull
    private static Map<String, String> findExceptionDetails(final Map<String, Throwable> errors) {
        return errors
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                final Throwable rootCause = getRootCause(entry.getValue());
                if (rootCause instanceof JsonParseException e) {
                    return e.getOriginalMessage();
                }
                return rootCause.getMessage();
            }));
    }
    
    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleConversionFailedException(
        @NonNull final ConversionFailedException exception, final ContentCachingRequestWrapper requestWrapper
    ) {
        this.logExceptionToWebsite(exception, requestWrapper, Severity.WARNING);
        ExceptionPrinter.print(exception);
        return ResponseEntities.badRequest(exception.getLocalizedMessage());
    }
    
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(
        @NonNull final BindException exception, final ContentCachingRequestWrapper requestWrapper
    ) {
        final List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        final Map<String, String> errors = MapEx.nonNullMapOf(
            fieldErrors,
            FieldError::getField,
            ExceptionAdvisor::getErrorMessage
        );
        logExceptionToWebsite(exception, requestWrapper, Severity.WARNING);
        return ResponseEntities.badRequest(errors);
    }
    
    private static String getErrorMessage(@NonNull final FieldError error) {
        try {
            return ReflectionEx
                .findFieldValue("source", error)
                .map(MethodInvocationException.class::cast)
                .map(NestedRuntimeException::getRootCause)
                .map(Throwable::getLocalizedMessage)
                .orElseThrow();
        } catch (final Exception e) {
            return Optional.ofNullable(error.getDefaultMessage()).orElseGet(error::getCode);
        }
    }
    
    //===============================================================================================================//
    //===============================================================================================================//
    //================================================// Not found //================================================//
    //===============================================================================================================//
    //===============================================================================================================//
    @ExceptionHandler(NoHandlerFoundException.class)
    public static ResponseEntity<ApiResponse<Void>> handleNotFoundException() {
        return ResponseEntities.notFound("Endpoint not found.");
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(
        @NonNull final EntityNotFoundException exception, final ContentCachingRequestWrapper requestWrapper
    ) {
        ExceptionPrinter.print(exception);
        this.logExceptionToWebsite(exception, requestWrapper, Severity.WARNING);
        return ResponseEntities.badRequest(exception.getLocalizedMessage());
    }
    
    //===============================================================================================================//
    //===============================================================================================================//
    //=================================================// Failed //==================================================//
    //===============================================================================================================//
    //===============================================================================================================//
    @ExceptionHandler({ConstraintViolationException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleDatabaseException(
        @NonNull final ConstraintViolationException exception, final ContentCachingRequestWrapper requestWrapper
    ) {
        ExceptionPrinter.print(exception);
        this.logExceptionToWebsite(exception, requestWrapper, Severity.ERROR);
        return ResponseEntities.failed(exception.getMessage());
    }
    
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(@NonNull final ApiException exception, final ContentCachingRequestWrapper requestWrapper) {
        ExceptionPrinter.print(exception);
        if (exception.getStatus().is5xxServerError()) {
            this.logExceptionToWebsite(exception, requestWrapper, Severity.ERROR);
            return ResponseEntities.create(exception.getMessage(), exception.getStatus());
        }
        this.logExceptionToWebsite(exception, requestWrapper, Severity.WARNING);
        return ResponseEntities.create(exception.getLocalizedMessage(), exception.getStatus());
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(@NonNull final Exception exception, final ContentCachingRequestWrapper requestWrapper) {
        ExceptionPrinter.print(exception);
        this.logExceptionToWebsite(exception, requestWrapper, Severity.ERROR);
        return ResponseEntities.failed(exception.getMessage());
    }
    
    @UtilityClass
    static class ResponseEntities<T> {
        
        public static <T> ResponseEntity<ApiResponse<T>> from(@NonNull final ApiResponse<T> response) {
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        
        public static ResponseEntity<ApiResponse<Void>> create(@NonNull final String message, @NonNull final HttpStatus status) {
            return ResponseEntity.status(status).body(ApiResponse.of(message, status));
        }
        
        public static <T> ResponseEntity<ApiResponse<T>> badRequest(@NonNull final T data) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(null, HttpStatus.BAD_REQUEST, data));
        }
        
        public static ResponseEntity<ApiResponse<Void>> badRequest(@NonNull final String message) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(message, HttpStatus.BAD_REQUEST));
        }
        
        public static <T> ResponseEntity<ApiResponse<T>> badRequest(@NonNull final String message, @NonNull final T data) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(message, HttpStatus.BAD_REQUEST, data));
        }
        
        public static <T> ResponseEntity<ApiResponse<T>> notFound(@NonNull final T data) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.of(null, HttpStatus.NOT_FOUND, data));
        }
        
        public static ResponseEntity<ApiResponse<Void>> notFound(@NonNull final String message) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.of(message, HttpStatus.NOT_FOUND));
        }
        
        public static <T> ResponseEntity<ApiResponse<T>> failed(@NonNull final T data) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(null, HttpStatus.INTERNAL_SERVER_ERROR, data));
        }
        
        public static ResponseEntity<ApiResponse<Void>> failed(@NonNull final String message) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(message, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
    
}
