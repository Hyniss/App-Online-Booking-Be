package com.fpt.h2s;

import com.fpt.h2s.utilities.MoreStrings;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

public record TestUserInfo(String email, String name) {
    private static final Faker FAKER = new Faker(Locale.forLanguageTag("vi"));
    public static TestUserInfo generate() {
        final String name = FAKER.name().lastName() + " " + FAKER.name().firstName();
        final LocalDateTime date = LocalDateTime.ofInstant(FAKER.date().birthday().toInstant(), ZoneId.systemDefault());

        final String month = StringUtils.leftPad(String.valueOf(date.getMonth().getValue() + 1), 2, '0');
        final String year = String.valueOf(date.getYear()).substring(2);

        final String email = MoreStrings.unaccent(name).replace(" ", "") + month + year + "@gmail.com";
        return new TestUserInfo(email, name);
    }
}
