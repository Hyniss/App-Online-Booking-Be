package com.fpt.h2s.models.domains;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Range<T> {

    private final T start;
    private final T end;

    public Range(T start, T end) {
        this.start = start;
        this.end = end;
    }

    public T getStartOr(T value) {
        if (this.start == null) {
            return value;
        }
        return start;
    }
    public T getEndOr(T value) {
        if (this.end == null) {
            return value;
        }
        return end;
    }

    public static <T> Range<T> of(T start, T end) {
        return new Range<>(start, end);
    }
    public static <T> Range<T> blank() {
        return new Range<>(null, null);
    }
}
