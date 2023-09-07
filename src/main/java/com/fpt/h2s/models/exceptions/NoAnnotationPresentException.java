package com.fpt.h2s.models.exceptions;

import ananta.utility.StringEx;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class NoAnnotationPresentException extends RuntimeException {

    public NoAnnotationPresentException(@NonNull final String message, @Nullable final Object... args) {
        super(StringEx.format(message, args));
    }

}
