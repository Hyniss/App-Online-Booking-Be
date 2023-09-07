package com.fpt.h2s.models.domains;

import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.utilities.Generalizable;
import com.fpt.h2s.utilities.MoreStrings;
import jakarta.validation.GroupSequence;
import lombok.NonNull;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.function.Supplier;

public abstract class BaseValidator<T extends BaseRequest> extends Generalizable<T> implements Validator {

    private Errors errors;
    protected T request;

    @Override
    public boolean supports(@NonNull final Class<?> clazz) {
        return clazz.isAssignableFrom(this.getGenericClass());
    }

    @Override
    public void validate(@NonNull final Object target, @NonNull final Errors errors) {
        this.errors = errors;
        this.request = (T) target;
        this.validate();
    }

    protected abstract void validate();

    protected void rejectIfEmpty(@NonNull final String key, @NonNull final Supplier<String> supplier) {
        if (this.errors.hasFieldErrors(key)) {
            return;
        }
        try {
            final String error = supplier.get();
            if (Strings.isNotBlank(error)) {
                this.errors.rejectValue(key, error);
            }
        } catch (final IllegalArgumentException e) {
            this.errors.rejectValue(key, e.getMessage());
        } catch (final ApiException e) {
            if (e.getStatus() == HttpStatus.BAD_REQUEST) {
                this.errors.rejectValue(key, e.getMessage());
            }
        }
    }

    @Contract("_, _ -> fail")
    protected static void reject(@NonNull final String message, @Nullable final Object... args) {
        throw new IllegalArgumentException(MoreStrings.format(message, args));
    }

    public interface FirstOrder{}
    public interface SecondOrder{}
    public interface ThirdOrder{}

    @GroupSequence({FirstOrder.class, SecondOrder.class, ThirdOrder.class})
    public interface ValidationGroup{}
}