package com.group.admin.service.impl;

import com.group.admin.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 本地檔案上傳 Service 實作（暫時替代 S3）
 * 
 * <p>將檔案儲存在本地 static/img 目錄，供前端透過 /img/** 存取</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
public class LocalFileServiceImpl implements S3Service {

    @Value("${file.upload.base-path:src/main/resources/static/img}")
    private String basePath;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${file.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    // 支援的圖片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 上傳圖片到本地
     */
    @Override
    public String uploadImage(MultipartFile file, String folder) {
        log.info("📤 上傳圖片到本地，資料夾：{}，檔案名稱：{}", folder, file.getOriginalFilename());
        
        try {
            // 驗證檔案
            validateFile(file);
            
            // 生成唯一檔名
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            
            // 建立目標路徑
            String folderPath = basePath + File.separator + folder;
            File targetFolder = new File(folderPath);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
                log.info("📁 建立資料夾：{}", folderPath);
            }
            
            // 儲存檔案
            String filePath = folderPath + File.separator + fileName;
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());
            
            // 返回 URL（格式：/img/folder/filename.jpg）
            String imageUrl = "/img/" + folder + "/" + fileName;
            log.info("✅ 圖片上傳成功：{}", imageUrl);
            
            return imageUrl;
            
        } catch (IOException e) {
            log.error("❌ 圖片上傳失敗：{}", e.getMessage(), e);
            throw new RuntimeException("圖片上傳失敗：" + e.getMessage());
        }
    }

    /**
     * 刪除圖片
     */
    @Override
    public void deleteImage(String imageUrl) {
        log.info("🗑️ 刪除圖片：{}", imageUrl);
        
        try {
            // 從 URL 提取檔案路徑（去除 /img/ 前綴）
            String relativePath = imageUrl.replace("/img/", "");
            String filePath = basePath + File.separator + relativePath.replace("/", File.separator);
            
            File file = new File(filePath);
            if (file.exists()) {
                if (file.delete()) {
                    log.info("✅ 圖片刪除成功：{}", filePath);
                } else {
                    log.warn("⚠️ 圖片刪除失敗：{}", filePath);
                }
            } else {
                log.warn("⚠️ 檔案不存在：{}", filePath);
            }
            
        } catch (Exception e) {
            log.error("❌ 圖片刪除失敗：{}", e.getMessage(), e);
            throw new RuntimeException("圖片刪除失敗：" + e.getMessage());
        }
    }

    /**
     * 取得預簽名 URL（本地版本不需要，直接返回原 URL）
     */
    @Override
    public String getPresignedUrl(String key, int expirationMinutes) {
        // 本地檔案不需要預簽名，直接返回 URL
        return "/img/" + key;
    }

    /**
     * 驗證檔案
     */
    private void validateFile(MultipartFile file) {
        // 檢查檔案是否為空
        if (file.isEmpty()) {
            throw new RuntimeException("檔案不能為空");
        }

        // 檢查檔案大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("檔案大小不能超過 5MB");
        }

        // 檢查內容類型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只支援圖片檔案");
        }

        // 檢查副檔名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            throw new RuntimeException("不支援的圖片格式，請使用 jpg/jpeg/png/gif/webp");
        }

        log.info("✅ 檔案驗證通過：{}, 大小：{} bytes, 類型：{}", 
                originalFilename, file.getSize(), contentType);
    }

    /**
     * 檢查副檔名是否有效
     */
    private boolean hasValidExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * 取得副檔名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * 生成唯一檔名（UUID + 原始副檔名）
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + extension;
    }
}
