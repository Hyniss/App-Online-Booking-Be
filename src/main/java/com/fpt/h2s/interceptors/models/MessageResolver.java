package com.fpt.h2s.interceptors.models;

import ananta.utility.StringEx;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.utilities.MoreRequests;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@AllArgsConstructor
public class MessageResolver {
    private final MessageSource messageSource;

    public String getMessageIn(final SupportLocale locale, final String key, final Object... args) {
        final String message = this.messageSource.getMessage(key, null, null, locale.getLocale());
        if (message == null) {
            throw ApiException.failed("No message found for {}", key);
        }
        return StringEx.format(message, args);
    }

    public String get(final String key, final Object... args) {
        final String message = this.messageSource.getMessage(key, null, null, getLocale());
        if (message == null) {
            throw ApiException.failed("No message found for {}", key);
        }
        return StringEx.format(message, args);
    }

    private static Locale getLocale() {
        try {
            final HttpServletRequest httpRequest = MoreRequests.getCurrentHttpRequest();
            return Locale.forLanguageTag(httpRequest.getHeader("X-LOCALE"));
        } catch (final Exception e) {
            return Locale.forLanguageTag("en");
        }
    }

    @Getter
    @AllArgsConstructor
    public enum SupportLocale {
        VIETNAMESE(Locale.forLanguageTag("vi")),
        ENGLISH(Locale.forLanguageTag("en"));
        private final Locale locale;
    }
}
