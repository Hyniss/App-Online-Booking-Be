package com.fpt.h2s.services;

import ananta.utility.StringEx;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.fpt.h2s.configurations.ConsulConfiguration;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.utilities.ExceptionPrinter;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AmazonS3ServiceImpl implements AmazonS3Service {
    
    private static final Duration DEFAULT_DURATION = Duration.ofHours(1);
    
    private static final String USER_METADATA_KEY_PREFIX = "x-amz-meta-";
    private static final Map<String, String> DEFAULT_USER_METADATA = Map.ofEntries(Map.entry("owner", "h2s"));
    private static final DateTimeFormatter PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSZ");
    
    private final ConsulConfiguration consulConfiguration;
    private final AmazonS3 s3Client;
    
    private String bucket;
    
    @PostConstruct
    private void postInit() {
        this.bucket = this.consulConfiguration.get("service.amazon.s3.AMAZON_BUCKET_NAME");
    }
    
    @Override
    public Optional<String> uploadFile(@Nullable final MultipartFile file) {
        if (file == null) {
            return Optional.empty();
        }
        try {
            final String fileName = AmazonS3ServiceImpl.generateFileName(Objects.requireNonNull(file.getOriginalFilename()));
            final byte[] content = IOUtils.toByteArray(file.getInputStream());
            final ObjectMetadata metadata = AmazonS3ServiceImpl.getMetaData(file);
    
            this.uploadFileTos3bucket(fileName, content, metadata);
            String url = ((AmazonS3Client) s3Client).getResourceUrl(bucket, fileName);
    
            AmazonS3ServiceImpl.log.info("Upload successfully to S3 for file {}.", file.getName());
            return Optional.of(url);
        } catch (final Exception e) {
            AmazonS3ServiceImpl.log.warn("Upload failed to S3 for file {}.", file.getName());
            ExceptionPrinter.print(e);
            return Optional.empty();
        }
    }
    
    private static ObjectMetadata getMetaData(final MultipartFile file) {
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        AmazonS3ServiceImpl.DEFAULT_USER_METADATA.forEach((key, value) -> metadata.addUserMetadata(AmazonS3ServiceImpl.USER_METADATA_KEY_PREFIX + key, value));
        return metadata;
    }
    
    private void uploadFileTos3bucket(final String fileName, final byte[] content, final ObjectMetadata metadata) {
        this.s3Client.putObject(
            new PutObjectRequest(this.bucket, fileName, new ByteArrayInputStream(content), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead)
        );
    }
    
    private static String generateFileName(@NonNull final String originalFilename) {
        final String uniqueId = Long.toHexString(UUID.randomUUID().getMostSignificantBits());
        
        final OffsetDateTime now = LocalDateTime.now().atOffset(ZoneOffset.UTC);
        final String uploadTime = AmazonS3ServiceImpl.PATTERN.format(now);
        final String fileExtension = StringEx.afterLastOf(".", originalFilename);
        
        return StringEx.format("{}_{}.{}", uniqueId, uploadTime, fileExtension);
    }
    
    @Override
    public MultipartFile downloadFile(@NonNull final String url) {
        final String key = this.extractObjectKeyFrom(url);
        final S3Object s3object = this.getS3ObjectOf(key);
        final byte[] bytes = AmazonS3ServiceImpl.getBytesOf(s3object);
        return new MultipartFile() {
            
            @Override
            public @NotNull String getName() {
                return s3object.getKey();
            }
            
            @Override
            public String getOriginalFilename() {
                return this.getName();
            }
            
            @Override
            public String getContentType() {
                return s3object.getObjectMetadata().getContentType();
            }
            
            @Override
            public boolean isEmpty() {
                return bytes == null || bytes.length == 0;
            }
            
            @Override
            public long getSize() {
                return bytes.length;
            }
            
            @Override
            public byte @NotNull [] getBytes() {
                return bytes;
            }
            
            @Override
            public @NotNull InputStream getInputStream() {
                return new ByteArrayInputStream(bytes);
            }
            
            @Override
            public void transferTo(final @NotNull File dest) throws IOException, IllegalStateException {
                try (final FileOutputStream output = new FileOutputStream(dest)) {
                    output.write(bytes);
                }
            }
            
        };
    }
    
    private S3Object getS3ObjectOf(final String key) {
        try {
            return this.s3Client.getObject(new GetObjectRequest(this.bucket, key));
        } catch (final AmazonS3Exception exception) {
            throw AmazonS3ServiceImpl.resultExceptionOf(exception);
        }
    }
    
    private static RuntimeException resultExceptionOf(final AmazonS3Exception exception) {
        if (exception.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
            return new AmazonStorageFileNotFoundException();
        }
        return exception;
    }
    
    private static byte[] getBytesOf(final S3Object s3object) {
        try {
            return s3object.getObjectContent().readAllBytes();
        } catch (final IOException e) {
            throw ApiException.failed(e, "No bytes found for s3 object.");
        }
    }
    
    @Override
    public void deleteFile(@NonNull final String url) {
        String fileName = this.extractObjectKeyFrom(url);
        this.s3Client.deleteObject(this.bucket, fileName);
    }
    
    private String extractObjectKeyFrom(@NonNull final String url) {
        String objectKey = url;
        if (url.contains("amazonaws.com/")) {
            final String[] parts = url.split("amazonaws\\.com/");
            if (parts.length > 1) {
                objectKey = parts[1];
            }
        }
        if (objectKey.startsWith(this.bucket + "/")) {
            objectKey = objectKey.replaceFirst("^" + this.bucket + "/", "");
        }
        return objectKey;
    }
}
