package com.fpt.h2s.services.commands.user.utils;

import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.utilities.MoreRequests;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Urls {
    @SneakyThrows
    public static String verifyEmailUrlOf(final String token) {
        return getClientSidePath() + "register/verify?code=" + token;
    }

    public static String getClientSidePath() {
        String path = MoreRequests.getCurrentHttpRequest().getHeader("X-FROM");
        if (path == null) {
            throw ApiException.badRequest("Invalid header.");
        }
        return path;
    }

    @SneakyThrows
    public static String verifyUpdateEmailUrlOf(final String token) {
        return getClientSidePath() + "personal-info/password/verify?code=" + token;
    }
}
