package com.fpt.h2s.interceptors;

import com.fpt.h2s.models.exceptions.NoAnnotationPresentException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;

class RequestAnnotationResolver<T extends Annotation> {

    private final Class<T> annotation;

    private RequestAnnotationResolver(final Class<T> annotation) {
        this.annotation = annotation;
    }

    static <T extends Annotation> RequestAnnotationResolver<T> of(@NonNull final Class<T> annotation) {
        return new RequestAnnotationResolver<>(annotation);
    }

    /**
     * Resolve annotation on some api. It will find annotation on api first.
     * If not found, find annotation in controller which contains that api.
     */
    Result<T> resolve(@NonNull final HandlerMethod apiMethod) {
        if (apiMethod.hasMethodAnnotation(this.annotation)) {
            return new Result<>(this.annotation, apiMethod, null, true);
        }

        final Class<?> controllerClass = apiMethod.getBeanType();
        if (controllerClass.isAnnotationPresent(this.annotation)) {
            return new Result<>(this.annotation, apiMethod, controllerClass, true);
        }
        return new Result<>(this.annotation, apiMethod, null, false);
    }

    static class Result<T extends Annotation> {

        private final Class<T> annotation;

        private final HandlerMethod method;

        private final Class<?> controller;

        private final boolean hasAnnotation;

        private Result(@NonNull final Class<T> annotation, @NonNull final HandlerMethod method, @Nullable final Class<?> controller, final boolean hasAnnotation) {
            this.annotation = annotation;
            this.method = method;
            this.controller = controller;
            this.hasAnnotation = hasAnnotation;
        }

        boolean hasAnnotation() {
            return this.hasAnnotation;
        }

        /**
         * Get annotation of current api.
         *
         * @throws NoAnnotationPresentException if no annotation presented.
         */
        T unbox() {
            if (!this.hasAnnotation) {
                throw new NoAnnotationPresentException("No annotation {} found on {}.", this.annotation, this.method);
            }
            if (this.controller != null) {
                return this.controller.getAnnotation(this.annotation);
            }
            return this.method.getMethodAnnotation(this.annotation);
        }

    }

}
