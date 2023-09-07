package com.fpt.h2s.utilities;

import ananta.utility.ListEx;
import ananta.utility.StreamEx;
import lombok.experimental.UtilityClass;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class QueryValues {
    public static final Timestamp UNREACHABLE_PAST = Timestamp.valueOf(LocalDateTime.of(1970, 1, 1, 0, 0));

    public static <T> List<String> enumListOf(final List<T> list) {
        return ListEx.listOf(list).stream().map(Object::toString).toList();
    }

    public static Set<Integer> integerList(final Collection<Integer> collection) {
        if (collection == null) {
            return Set.of(-1);
        }
        return StreamEx.from(collection).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static Set<Long> longList(final Collection<Long> collection) {
        if (collection == null) {
            return Set.of(-1L);
        }
        return StreamEx.from(collection).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static Long longOf(final Long value) {
        return Optional.ofNullable(value).orElse(0L);
    }

    public static String like(final String value) {
        return Optional.ofNullable(value).map(String::trim).map(v -> "%" + v + "%").orElse("%%");
    }

    public static Timestamp unreachablePastOr(final Timestamp value) {
        return Optional.ofNullable(value).orElse(UNREACHABLE_PAST);
    }

    public static Timestamp unreachableFutureOr(final Timestamp value) {
        return Optional.ofNullable(value).orElse(Timestamp.valueOf(LocalDateTime.of(3000, 1, 1, 0, 0)));
    }
}
