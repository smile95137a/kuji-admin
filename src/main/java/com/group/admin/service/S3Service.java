package com.group.admin.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * S3 檔案上傳 Service
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface S3Service {
    
    /**
     * 上傳圖片到 S3
     * 
     * @param file 檔案
     * @param folder 資料夾（news / banner / lottery 等）
     * @return 圖片 URL
     */
    String uploadImage(MultipartFile file, String folder);
    
    /**
     * 刪除 S3 上的圖片
     * 
     * @param imageUrl 圖片 URL
     */
    void deleteImage(String imageUrl);
    
    /**
     * 取得預簽名 URL（用於臨時存取）
     * 
     * @param key S3 物件 key
     * @param expirationMinutes 有效期限（分鐘）
     * @return 預簽名 URL
     */
    String getPresignedUrl(String key, int expirationMinutes);
}
