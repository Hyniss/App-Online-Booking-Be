package com.fpt.h2s.services.commands.company.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@UtilityClass
public class MultipartFileUtils {

    public static MultipartFile convertToMultipartFile(byte[] bytes, String filename, String contentType) {
        ByteArrayResource resource = new ByteArrayResource(bytes);
        return new CustomMultipartFile(resource, filename, contentType);
    }

    private static class CustomMultipartFile implements MultipartFile {

        private final ByteArrayResource resource;
        private final String filename;
        private final String contentType;

        public CustomMultipartFile(ByteArrayResource resource, String filename, String contentType) {
            this.resource = resource;
            this.filename = filename;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getOriginalFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return resource.contentLength() == 0;
        }

        @Override
        public long getSize() {
            return resource.contentLength();
        }

        @Override
        public byte @NotNull [] getBytes() {
            return resource.getByteArray();
        }

        @Override
        public @NotNull InputStream getInputStream() throws IOException {
            return resource.getInputStream();
        }

        @Override
        public void transferTo(@NotNull File dest) throws IOException, IllegalStateException {
            try (OutputStream outputStream = new FileOutputStream(dest)) {
                resource.getInputStream().transferTo(outputStream);
            }
        }
    }

}
