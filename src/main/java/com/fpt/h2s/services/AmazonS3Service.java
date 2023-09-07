package com.fpt.h2s.services;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface AmazonS3Service {
    /**
     * Upload file to Amazon S3 service.
     * @param file contains name, bytes of the file.
     * @return accessible url of the file in S3.
     */
    Optional<String> uploadFile(@Nullable final MultipartFile file);
    
    /**
     * Download a file from S3 using its url.
     * @param url url of the file that you want to download.
     * @throws AmazonStorageFileNotFoundException if no file found for the url.
     */
    MultipartFile downloadFile(@NonNull final String url);
    
    /**
     * Delete a file from S3 using its url.
     * @param url url of the file that you want to delete.
     */
    void deleteFile(@NonNull final String url);
    
    class AmazonStorageFileNotFoundException extends RuntimeException {
    
    }
    
}
