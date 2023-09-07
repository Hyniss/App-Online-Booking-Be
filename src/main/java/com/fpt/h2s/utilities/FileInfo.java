package com.fpt.h2s.utilities;

import ananta.utility.SetEx;
import ananta.utility.StringEx;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;


public class FileInfo {

    @Getter
    @AllArgsConstructor
    public enum Type {
        IMAGE("jpeg", "jpg", "png", "heif", "tiff", "tif", "gif", "dib", "heic", "bmp", "eps", "raw", "nef", "cr2", "orf"),
        VIDEO("mpg", "avi", "mov", "mp4", "webm", "hevc", "m4v"),
        AUDIO("m4a", "mp3", "wav", "mp4", "aac", "adts"),
        EXCEL("xlsx", "xls");

        private final Set<String> extensions;

        Type(final String... extensions) {
            this.extensions = SetEx.linkedSetOf(extensions);
        }

        public boolean isTypeOf(@Nullable final String file) {
            if (file == null) {
                return false;
            }
            final String extension = extensionOf(file).toLowerCase();
            return this.extensions.contains(extension);
        }

        @Contract("null -> false")
        public boolean isTypeOf(@Nullable final MultipartFile file) {
            if (file == null) {
                return false;
            }
            final String extension = extensionOf(file.getOriginalFilename()).toLowerCase();
            return this.extensions.contains(extension);
        }

        public static String extensionOf(final @Nullable String fileName) {
            return StringEx.afterLastOf(".", fileName);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Size {
        B(1L),
        KB(1024L),
        MB(1024L * 1024L),
        GB(1024L * 1024L * 1024L);
        private final Long size;

        public Long of(Long value) {
            return value * this.size;
        }

        public Long of(Integer value) {
            return value * this.size;
        }

        public Long ofBytes(Long value) {
            return value / this.size;
        }

        public static Long bytesOf(Long value, Size size) {
            return size.of(value);
        }

        public static Long bytesOf(Integer value, Size size) {
            return size.of(value);
        }
    }

}