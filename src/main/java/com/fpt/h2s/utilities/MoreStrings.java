package com.fpt.h2s.utilities;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class MoreStrings {

    private static final String SPACE = " ";
    private static final String UNDERSCORE = "_";
    private static final String EMPTY = "";
    private static final String COMMA = ", ";
    private static final String DOT = ", ";

    private static final int ASCII_START_VALUE = 33;
    private static final int ASCII_END_VALUE = 122;
    private static final boolean ALLOW_LETTERS = true;
    private static final boolean ALLOW_NUMBER = true;

    /**
     * Append non-null variables into a string.
     *
     * @param messagePattern a format text. can be null.
     *                       This messagePattern will contain '{}' which will then replace by following arguments.
     *                       Example: My name is {}.
     *                       For more information, please visit this page: https://stackoverflow.com/a/43262120
     * @param args           values which will be used to fill in messagePattern.
     * @return empty if messagePattern is null. Otherwise, return a string which contains the arguments.
     */
    public static String format(final String messagePattern, final Object... args) {
        if (Pattern.matches(".*\\{\\d+,.+,.+}.*", messagePattern)) {
            return MessageFormat.format(messagePattern, args);
        }
        final String r = "\\{\\d}";

        if (!messagePattern.matches(".*" + r + ".*")) {
            return MessageFormatter.arrayFormat(messagePattern, args).getMessage();
        }

        final Pattern pattern = Pattern.compile(r);
        final Matcher matcher = pattern.matcher(messagePattern);
        final StringBuilder stringBuilder = new StringBuilder();
        int parsingValue = 0;

        while (matcher.find()) {
            matcher.appendReplacement(stringBuilder, args[parsingValue].toString());
            parsingValue++;
        }
        matcher.appendTail(stringBuilder);
        return stringBuilder.toString();
    }


    public static String Sha256(final String message) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder(2 * hash.length);
            for (final byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException ex) {
            return "";
        }
    }

    public static String base64url(final String input) {
        final byte[] b64data = Base64.encodeBase64URLSafe(input.getBytes(StandardCharsets.UTF_8));
        return new String(b64data);
    }

    public static String utf8Of(final String input) {
        if (input == null) {
            return EMPTY;
        }
        return new String(input.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static String hmacWithJava(final String algorithm, final String data, final String key) {
        final SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
        final Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);
        return Hex.encodeHexString(mac.doFinal(data.getBytes()));
    }

    @SneakyThrows
    public static String HMacSha256(final String data, final String key) {
        return MoreStrings.hmacWithJava("HmacSHA256", data, key);
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            final byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            final byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            final byte[] result = hmac512.doFinal(dataBytes);
            final StringBuilder sb = new StringBuilder(2 * result.length);
            for (final byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (final Exception ex) {
            return "";
        }
    }

    public static String md5(final String message) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder(2 * hash.length);
            for (final byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException ex) {
            return "";
        }
    }

    public static String randomNumber(final int len) {
        final Random rnd = new Random();
        final String chars = "0123456789";
        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String randomStringWithLength(final int length) {
        return RandomStringUtils.random(
            length,
            MoreStrings.ASCII_START_VALUE,
            MoreStrings.ASCII_END_VALUE,
            MoreStrings.ALLOW_LETTERS,
            MoreStrings.ALLOW_NUMBER,
            null,
            new SecureRandom()
        );
    }

    @SneakyThrows
    public static String randomOTP(final int size) {
        final StringBuilder generatedToken = new StringBuilder();
        final SecureRandom number = SecureRandom.getInstance("SHA1PRNG");
        for (int i = 0; i < size; i++) {
            generatedToken.append(number.nextInt(9));
        }
        return generatedToken.toString();
    }

    public static Optional<String> optionalOf(final String input) {
        if (Strings.isBlank(input)) {
            return Optional.empty();
        }
        return Optional.of(input);
    }

    /**
     * @param text         Can be null.
     * @param emptyIfBlank When text is blank, return empty string if true. Otherwise, return null.
     * @author Ananta0810
     * Trim all spaces in a string.
     */
    public static String removeAllSpaces(@Nullable final String text, final boolean emptyIfBlank) {
        if (Strings.isBlank(text)) {
            return emptyIfBlank ? MoreStrings.EMPTY : null;
        }
        return Stream.of(text.split(MoreStrings.SPACE)).filter(Strings::isNotBlank).collect(Collectors.joining(MoreStrings.EMPTY));
    }

    /**
     * @author Ananta0810
     * Clean the name of a place or a human by trimming redundant space
     * and uppercase only first character of each word (proper case).
     */
    public static String capitalOf(@Nullable final String name) {
        if (name == null) {
            return MoreStrings.EMPTY;
        }

        final List<String> words = Arrays
            .stream(name.toLowerCase().split(MoreStrings.SPACE))
            .filter(StringUtils::isNotBlank)
            .map(StringUtils::capitalize)
            .toList();

        return String.join(MoreStrings.SPACE, words);
    }

    /**
     * @param text String that you want to remove space.
     * @return Empty string if null or blank. Otherwise, return string with no space.
     * @author Ananta0810
     * Remove all space of a text
     */
    public static String removeAllSpaces(@Nullable final String text) {
        return MoreStrings.removeAllSpaces(text, true);
    }

    public static String snakeCaseOf(@Nullable final String input) {
        if (input == null) {
            return MoreStrings.EMPTY;
        }
        final String regex = "([a-z])([A-Z]+)";
        final String replacement = "$1_$2";
        return input.replaceAll(regex, replacement).toLowerCase();
    }

    public static String screamSnakeCaseOf(@Nullable final String input) {
        if (Strings.isEmpty(input)) {
            return MoreStrings.EMPTY;
        }
        final String regex = "([a-z])([A-Z]+)";
        final String replacement = "$1_$2";
        return input.replaceAll(regex, replacement).toUpperCase();
    }

    public static String snakeCaseToCamelCase(String input) {
        if (input == null) {
            return "";
        }
        if (!input.contains("_")) {
            return input.toLowerCase();
        }
        input = input.toLowerCase();
        return input.substring(0, input.indexOf("_")) + Arrays.stream(input.substring(input.indexOf("_") + 1).split("_")).map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1)).collect(Collectors.joining());

    }

    public static String camelCaseToPascalCase(final String input) {
        if (Strings.isEmpty(input)) {
            return MoreStrings.EMPTY;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);

    }

    public static String asciiOf(final String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    public static String secondsOf(final long milliseconds) {
        final double MILLISECONDS_PER_SECOND = 1000.0;
        return String.format("%.2f", (float) milliseconds / MILLISECONDS_PER_SECOND);
    }

    public static String unaccent(final String input) {
        if (Strings.isBlank(input)) {
            return EMPTY;
        }
        final String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace("đ", "d").replace("Đ", "D");
    }
    
    public static String removeLines(final @Nullable String inputWithLines) {
        if (StringUtils.isEmpty(inputWithLines)) {
            return "";
        }
        String[] lines = inputWithLines.split("\\\\n");
        return String.join("", lines);
    }

}
