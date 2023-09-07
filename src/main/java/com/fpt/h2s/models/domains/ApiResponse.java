package com.fpt.h2s.models.domains;

import lombok.*;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private String message;
    private int status;
    private T data;

    private static final String SUCCESS = "Success";
    private static final String FAILED = "Failed";
    private static final String NOT_FOUND = "Not found";

    public static <T> ApiResponse<T> of(
        @Nullable final String message,
        @NonNull final HttpStatus status,
        @Nullable final T data
    ) {
        return new ApiResponse<>(message, status.value(), data);
    }

    public static <T> ApiResponse<T> of(@Nullable final String message, @NonNull final HttpStatus status) {
        return of(message, status, null);
    }

    public static ApiResponse<Void> success() {
        return of(SUCCESS, HttpStatus.OK);
    }

    public static ApiResponse<Void> success(@Nullable final String message) {
        return of(message, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> success(@Nullable final T data) {
        return of(SUCCESS, HttpStatus.OK, data);
    }

    public static <T> ApiResponse<T> success(@Nullable final String message, @Nullable final T data) {
        return of(message, HttpStatus.OK, data);
    }

    public static <T> ApiResponse<T> created(@Nullable final String message, @Nullable final T data) {
        return of(message, HttpStatus.CREATED, data);
    }

    public static <T> ApiResponse<T> badRequest(@Nullable final String message, @Nullable final T data) {
        return of(message, HttpStatus.BAD_REQUEST, data);
    }

    public static <T> ApiResponse<T> badRequest(@Nullable final String message) {
        return of(message, HttpStatus.BAD_REQUEST, null);
    }

    public static <T> ApiResponse<T> notFound(@Nullable final String message, @Nullable final T data) {
        return of(message, HttpStatus.NOT_FOUND, data);
    }

    public static <T> ApiResponse<T> failed(@Nullable final String message, @Nullable final T data) {
        return of(message, HttpStatus.INTERNAL_SERVER_ERROR, data);
    }
}