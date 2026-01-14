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
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * AWS S3 檔案上傳 Service 實作
 * 
 * <p>在 prod 和 dev 環境都啟用（當有 AWS 配置時）</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
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

    // 支援的圖片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 上傳圖片到 S3
     */
    @Override
    public String uploadImage(MultipartFile file, String folder) {
        log.info("📤 上傳圖片到 S3，Bucket：{}，資料夾：{}，檔案名稱：{}", bucketName, folder, file.getOriginalFilename());
        
        try {
            // 驗證檔案
            validateFile(file);
            
            // 生成唯一檔名
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            
            // S3 Key（路徑）
            String s3Key = folder + "/" + fileName;
            
            // 上傳到 S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)  // 公開讀取
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            
            // 返回完整 URL
            String imageUrl = baseUrl + "/" + s3Key;
            log.info("✅ 圖片上傳成功：{}", imageUrl);
            
            return imageUrl;
            
        } catch (IOException e) {
            log.error("❌ 圖片上傳失敗：{}", e.getMessage(), e);
            throw new RuntimeException("圖片上傳失敗：" + e.getMessage());
        } catch (S3Exception e) {
            log.error("❌ S3 錯誤：{}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("S3 上傳失敗：" + e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * 刪除 S3 上的圖片
     */
    @Override
    public void deleteImage(String imageUrl) {
        log.info("🗑️ 刪除 S3 圖片：{}", imageUrl);
        
        try {
            // 從 URL 提取 S3 Key
            String s3Key = extractS3KeyFromUrl(imageUrl);
            
            if (s3Key == null || s3Key.isEmpty()) {
                log.warn("⚠️ 無法從 URL 提取 S3 Key：{}", imageUrl);
                return;
            }
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            log.info("✅ 圖片刪除成功：{}", s3Key);
            
        } catch (S3Exception e) {
            log.error("❌ S3 刪除失敗：{}", e.awsErrorDetails().errorMessage(), e);
            // 不拋出例外，避免影響主流程
        }
    }

    /**
     * 取得預簽名 URL（用於臨時存取私有檔案）
     */
    @Override
    public String getPresignedUrl(String key, int expirationMinutes) {
        log.info("🔗 生成預簽名 URL，Key：{}，有效期：{} 分鐘", key, expirationMinutes);
        
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
            
            log.info("✅ 預簽名 URL 生成成功");
            return url;
            
        } catch (S3Exception e) {
            log.error("❌ 預簽名 URL 生成失敗：{}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("預簽名 URL 生成失敗：" + e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * 驗證檔案
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("檔案不可為空");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("檔案大小不可超過 10MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("檔案格式不正確");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支援的檔案格式，僅支援：" + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    /**
     * 生成唯一檔名
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 從 URL 提取 S3 Key
     * 
     * 範例：
     * - https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/news/abc.jpg → news/abc.jpg
     */
    private String extractS3KeyFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        // 移除 base URL
        if (imageUrl.startsWith(baseUrl + "/")) {
            return imageUrl.substring(baseUrl.length() + 1);
        }
        
        // 如果是完整 S3 URL
        if (imageUrl.contains(".s3.") && imageUrl.contains(".amazonaws.com/")) {
            int index = imageUrl.indexOf(".amazonaws.com/");
            return imageUrl.substring(index + ".amazonaws.com/".length());
        }
        
        return imageUrl;
    }
}
