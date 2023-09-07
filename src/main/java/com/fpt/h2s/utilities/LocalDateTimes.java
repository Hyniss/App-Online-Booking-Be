package com.fpt.h2s.utilities;

import com.fpt.h2s.models.exceptions.ApiException;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class LocalDateTimes {
    private final List<String> patterns = List.of(
        "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss.SS", "yyyy-MM-dd HH:mm:ss.S", "yyyy-MM-dd HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SS", "yyyy-MM-dd'T'HH:mm:ss.S", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH", "yyyy-MM-dd",
        "dd-MM-YYYY HH:mm:ss", "dd-MM-YYYY HH:mm", "dd-MM-YYYY HH", "dd-MM-YYYY"
    );
    public static final Timestamp UNREACHABLE_PAST = Timestamp.valueOf(LocalDateTime.of(0, 1, 1, 0, 0));
    public static final Timestamp UNREACHABLE_FUTURE = Timestamp.valueOf(LocalDateTime.of(3000, 12, 31, 23, 59));

    public static LocalDateTime startDayOf(final @Nullable Timestamp time) {
        if (time == null) {
            return null;
        }
        return startDayOf(time.toLocalDateTime());
    }

    public static LocalDateTime startDayOf(final LocalDateTime time) {
        return time.withHour(0).withMinute(0).withSecond(0);
    }

    public static LocalDateTime startDayOf(final ZonedDateTime time) {
        final ZonedDateTime zonedDateTime = time.toLocalDate().atStartOfDay(time.getZone());
        return LocalDateTime.ofInstant(zonedDateTime.toInstant(), time.getZone());
    }

    public static LocalDateTime endDayOf(final LocalDateTime time) {
        return time.withHour(23).withMinute(59).withSecond(59);
    }

    public static LocalDateTime endDayOf(final ZonedDateTime time) {
        return time.toLocalDate().atStartOfDay(time.getZone()).plusDays(1).toLocalDateTime();
    }

    public static ArrayList<LocalDateTime> getDaysBetween(final @Nullable Timestamp start, final @Nullable Timestamp end) {
        if (start == null || end == null) {
            return Lists.newArrayList();
        }

        LocalDateTime currentDate = startDayOf(start);
        final LocalDateTime endDate = startDayOf(end);

        final ArrayList<LocalDateTime> days = Lists.newArrayList();

        while (currentDate.isBefore(endDate)) {
            days.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        days.add(endDate);
        return days;
    }

    @Nullable
    public static LocalDateTime localDateTimeOf(final String time) {
        if (Strings.isBlank(time)) {
            return null;
        }
        for (final String pattern : patterns) {
            try {
                return LocalDateTime.parse(time, DateTimeFormatter.ofPattern(pattern));
            } catch (final Exception ignored) {
            }
        }
        throw ApiException.badRequest(String.format("Time format of %s must be one of %s", time, patterns));
    }

    @Nullable
    public static LocalDateTime localDateTimeOf(final org.joda.time.LocalDateTime joda) {
        if (joda == null) {
            return null;
        }
        return LocalDateTime.of(joda.getYear(), joda.getMonthOfYear(), joda.getDayOfMonth(), joda.getHourOfDay(), joda.getMinuteOfHour(), joda.getSecondOfMinute(), joda.getMillisOfSecond());
    }

    @Nullable
    public static LocalDateTime localDateTimeOf(final Timestamp time) {
        if (time == null) {
            return null;
        }
        return time.toLocalDateTime();
    }

    @Nullable
    public static org.joda.time.LocalDateTime jodaLocalDateTimeOf(final String time) {
        if (Strings.isBlank(time)) {
            return null;
        }
        final java.time.LocalDateTime java8LDT = localDateTimeOf(time);
        return jodaLocalDateTimeOf(java8LDT);
    }

    private static org.joda.time.LocalDateTime jodaLocalDateTimeOf(final LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return new org.joda.time.LocalDateTime(time.toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    @Nullable
    public static Timestamp timestampOf(final String time) {
        final LocalDateTime localDateTime = localDateTimeOf(time);
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }

    @Nullable
    public static Timestamp timestampOf(@Nullable final DateTime time) {
        if (time == null) {
            return null;
        }
        return new Timestamp(time.getMillis());
    }

    @Nullable
    public static Timestamp timestampOf(@Nullable final LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return Timestamp.valueOf(time);
    }

    public static String toDateString(final LocalDateTime time, final String pattern) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return time.format(formatter);
    }

    public static String toDateString(final ZonedDateTime time, final String pattern) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return time.format(formatter);
    }

    public static Timestamp unreachableFutureOrTimestampOf(final String time) {
        return Optional
            .ofNullable(localDateTimeOf(time))
            .map(Timestamp::valueOf)
            .orElse(UNREACHABLE_FUTURE);
    }

    public static Timestamp unreachablePastOrTimestampOf(final String time) {
        return Optional
            .ofNullable(localDateTimeOf(time))
            .map(Timestamp::valueOf)
            .orElse(UNREACHABLE_PAST);
    }

    public static List<ZonedDateTime> getNextNthDays(final int totalDays, final ZoneId zoneId) {
        final List<ZonedDateTime> dates = Lists.newArrayList();
        final ZonedDateTime today = ZonedDateTime.now(zoneId);
        ZonedDateTime date = today;
        dates.add(today);
        for (int i = 1; i < totalDays; i++) {
            date = date.plusDays(1);
            dates.add(date);
        }
        return dates;
    }

    public static boolean isOvertime(final Timestamp time) {
        if (time == null) {
            return false;
        }
        final LocalDateTime now = LocalDateTime.now();
        return time.toLocalDateTime().isBefore(now);
    }

    public static Timestamp plusHourTo(@Nullable final Timestamp time, @Nullable final Long hour) {
        if (hour == null) {
            return time;
        }
        if (time == null) {
            return null;
        }
        return timestampOf(time.toLocalDateTime().plus(hour, ChronoUnit.HOURS));
    }
}
