package com.group.admin.service.impl;

import com.group.admin.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Profile({"prod", "dev"})
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.base-url}")
    private String baseUrl;

    @Value("${aws.s3.key-prefix:}")
    private String keyPrefix;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        log.info("開始上傳圖片至 S3: bucket={}, folder={}, filename={}", bucketName, folder, file.getOriginalFilename());

        try {
            validateFile(file);

            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String objectPath = buildObjectPath(folder, fileName);
            String s3Key = buildS3Key(objectPath);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            String imageUrl = buildPublicUrl(objectPath);
            log.info("圖片上傳成功: imageUrl={}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("圖片上傳失敗: {}", e.getMessage(), e);
            throw new RuntimeException("圖片上傳失敗: " + e.getMessage());
        } catch (S3Exception e) {
            log.error("S3 上傳失敗: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("S3 上傳失敗: " + e.awsErrorDetails().errorMessage());
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        log.info("準備刪除 S3 圖片: imageUrl={}", imageUrl);

        try {
            List<String> s3Keys = extractS3KeysFromUrl(imageUrl);
            if (s3Keys.isEmpty()) {
                log.warn("無法從 URL 解析 S3 key: {}", imageUrl);
                return;
            }

            for (String s3Key : s3Keys) {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
                log.info("圖片刪除完成: s3Key={}", s3Key);
            }
        } catch (S3Exception e) {
            log.error("S3 刪除失敗: {}", e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public String getPresignedUrl(String key, int expirationMinutes) {
        log.info("開始產生預簽名 URL: key={}, expirationMinutes={}", key, expirationMinutes);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();

            log.info("預簽名 URL 產生成功");
            return url;
        } catch (S3Exception e) {
            log.error("預簽名 URL 產生失敗: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("預簽名 URL 產生失敗: " + e.awsErrorDetails().errorMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("圖片檔案不得為空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("圖片檔案大小不得超過 10MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("圖片檔名格式不正確");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支援的圖片格式，僅允許: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID() + extension;
    }

    private String buildObjectPath(String folder, String fileName) {
        return normalizePath(folder) + "/" + fileName;
    }

    private String buildS3Key(String objectPath) {
        String normalizedPrefix = normalizePath(keyPrefix);
        if (normalizedPrefix.isEmpty()) {
            return objectPath;
        }
        return normalizedPrefix + "/" + objectPath;
    }

    private String buildPublicUrl(String objectPath) {
        return trimTrailingSlash(baseUrl) + "/" + buildS3Key(objectPath);
    }

    private List<String> extractS3KeysFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return List.of();
        }

        String normalizedBaseUrl = trimTrailingSlash(baseUrl);
        String normalizedPrefix = normalizePath(keyPrefix);
        Set<String> candidateKeys = new LinkedHashSet<>();

        if (imageUrl.startsWith(normalizedBaseUrl + "/")) {
            addCandidateKeys(candidateKeys, imageUrl.substring(normalizedBaseUrl.length() + 1), normalizedPrefix);
        }

        if (imageUrl.contains(".s3.") && imageUrl.contains(".amazonaws.com/")) {
            int index = imageUrl.indexOf(".amazonaws.com/");
            addCandidateKeys(candidateKeys, imageUrl.substring(index + ".amazonaws.com/".length()), normalizedPrefix);
        }

        addCandidateKeys(candidateKeys, imageUrl, normalizedPrefix);
        return new ArrayList<>(candidateKeys);
    }

    private void addCandidateKeys(Set<String> candidateKeys, String rawPath, String normalizedPrefix) {
        String normalizedPath = normalizePath(rawPath);
        if (normalizedPath.isEmpty()) {
            return;
        }

        candidateKeys.add(normalizedPath);
        if (!normalizedPrefix.isEmpty() && normalizedPath.startsWith(normalizedPrefix + "/")) {
            candidateKeys.add(normalizedPath.substring(normalizedPrefix.length() + 1));
        }
    }

    private String normalizePath(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
