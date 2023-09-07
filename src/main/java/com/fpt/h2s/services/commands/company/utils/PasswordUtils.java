package com.fpt.h2s.services.commands.company.utils;

import lombok.experimental.UtilityClass;
import java.util.Random;
import java.util.stream.Collectors;

@UtilityClass
public class PasswordUtils {
    public static String generatePassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();

        return random.ints(8, 0, characters.length())
                .mapToObj(characters::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
