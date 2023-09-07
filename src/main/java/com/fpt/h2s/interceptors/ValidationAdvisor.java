package com.fpt.h2s.interceptors;

import com.fpt.h2s.models.domains.BaseValidator;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.List;

@ControllerAdvice
@AllArgsConstructor
public class ValidationAdvisor {
    
    private final List<BaseValidator<?>> validators;
    
    @InitBinder
    public void initBinder(@NonNull final WebDataBinder binder) {
        final Object request= binder.getTarget();
        if (request== null) {
            return;
        }
        this.validators.stream()
            .filter(validator -> validator.supports(request.getClass()))
            .forEach(binder::addValidators);
    }
    
}