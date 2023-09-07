package com.fpt.h2s.models.exceptions;

import com.fpt.h2s.utilities.MoreStrings;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    
    private final HttpStatus status;
    
    public ApiException(@NonNull final HttpStatus status, @NonNull final String message, @Nullable final Object... args) {
        super(MoreStrings.format(message, args));
        this.status = status;
    }
    
    public ApiException(
        @NonNull final Throwable cause,
        @NonNull final HttpStatus status,
        @NonNull final String message,
        @Nullable final Object... args
    ) {
        super(MoreStrings.format(message, args), cause);
        this.status = status;
    }
    
    public HttpStatus getStatus() {
        return this.status;
    }
    
    public static ApiException badRequest(@NonNull final String message, @Nullable final Object... args) {
        return new ApiException(HttpStatus.BAD_REQUEST, message, args);
    }
    
    public static ApiException badRequest(
        @NonNull final Exception exception,
        @NonNull final String message,
        @Nullable final Object... args
    ) {
        return new ApiException(exception, HttpStatus.BAD_REQUEST, message, args);
    }
    
    public static ApiException notFound(@NonNull final String message, @Nullable final Object... args) {
        return new ApiException(HttpStatus.NOT_FOUND, message, args);
    }
    
    public static ApiException unauthorized(@NonNull final String message, @Nullable final Object... args) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message, args);
    }
    
    public static ApiException unauthorized() {
        return ApiException.notFound("Xin hãy đăng nhập để tiếp tục");
    }
    
    public static ApiException forbidden(@NonNull final String message, @Nullable final Object... args) {
        return new ApiException(HttpStatus.FORBIDDEN, message, args);
    }
    
    public static ApiException forbidden() {
        return ApiException.forbidden("Bạn không có quyền truy cập.");
    }
    
    public static ApiException requiredVerification(@NonNull final String message, @Nullable final Object... args) {
        return new ApiException(HttpStatus.LOCKED, message, args);
    }
    
    public static ApiException failed(@NonNull final String message, @Nullable final Object... args) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, message, args);
    }
    
    public static ApiException failed() {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Có lỗi xảy ra, xin hãy thử lại.");
    }
    
    public static ApiException failed(
        @NonNull final Exception exception,
        @NonNull final String message,
        @Nullable final Object... args
    ) {
        return new ApiException(exception, HttpStatus.INTERNAL_SERVER_ERROR, message, args);
    }
    
    public static void badRequestIf(
        final boolean isTrue,
        @NonNull final String message,
        @Nullable final Object... args
    ) {
        if (isTrue) {
            throw ApiException.badRequest(message, args);
        }
    }
    
    public static void badRequestIfNot(
        final boolean isTrue,
        @NonNull final String message,
        @Nullable final Object... args
    ) {
        if (!isTrue) {
            throw ApiException.badRequest(message, args);
        }
    }
    
    public static void unauthorizedIf(
        final boolean isTrue,
        @NonNull final String message,
        @Nullable final Object... args
    ) {
        if (isTrue) {
            throw ApiException.unauthorized(message, args);
        }
    }
    
    public static void unauthorizedIfNot(
        final boolean isTrue,
        @NonNull final String message,
        @Nullable final Object... args
    ) {
        if (!isTrue) {
            throw ApiException.unauthorized(message, args);
        }
    }
    
    public static void unauthorizedIf(final boolean isTrue) {
        if (isTrue) {
            throw ApiException.unauthorized();
        }
    }
    
    public static void unauthorizedIfNot(final boolean isTrue) {
        if (!isTrue) {
            throw ApiException.unauthorized();
        }
    }
    
    public static void forbiddenIf(
        final boolean isTrue,
        @NonNull final String message,
        @Nullable final Object... args
    ) {
        if (isTrue) {
            throw ApiException.forbidden(message, args);
        }
    }
    
    public static void forbiddenIfNot(
        final boolean isTrue,
        @NonNull final String message,
        @Nullable final Object... args
    ) {
        if (!isTrue) {
            throw ApiException.forbidden(message, args);
        }
    }
    
    public static void forbiddenIf(final boolean isTrue) {
        if (isTrue) {
            throw ApiException.forbidden();
        }
    }
    
    public static void forbiddenIfNot(final boolean isTrue) {
        if (!isTrue) {
            throw ApiException.forbidden();
        }
    }
    
    public static void notFoundIf(final boolean isTrue, @NonNull final String message, @Nullable final Object... args) {
        if (isTrue) {
            throw ApiException.notFound(message, args);
        }
    }
    
    public static void notFoundIfNot(
        final boolean isTrue,
        @NonNull final String message,
        @Nullable final Object... args
    ) {
        if (!isTrue) {
            throw ApiException.notFound(message, args);
        }
    }
    
    public static void failedIf(final boolean isTrue, @NonNull final String message, @Nullable final Object... args) {
        if (isTrue) {
            throw ApiException.unauthorized(message, args);
        }
    }
    
    public static void failedIfNot(
        final boolean isTrue,
        @NonNull final String message,
        @Nullable final Object... args
    ) {
        if (!isTrue) {
            throw ApiException.unauthorized(message, args);
        }
    }
}
