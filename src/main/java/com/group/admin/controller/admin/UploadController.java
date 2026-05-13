package com.group.admin.controller.admin;

import com.group.admin.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 圖片上傳 Controller
 * 
 * <p>提供圖片上傳到 S3 功能（需 Admin 權限）</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/upload")
@RequiredArgsConstructor
@Tag(name = "後台-圖片上傳", description = "圖片上傳到 S3（需 Admin 權限）")
public class UploadController {

    private final S3Service s3Service;

    /**
     * 上傳 News 圖片
     */
    @PostMapping("/news")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "上傳 News 圖片", description = "上傳最新消息的封面圖片到 S3")
    public ResponseEntity<Map<String, String>> uploadNewsImage(
            @RequestParam("file") 
            @Parameter(description = "圖片檔案（限制 5MB，支援 jpg/png/gif/webp）")
            MultipartFile file) {
        
        log.info("📤 上傳 News 圖片，檔案名稱：{}", file.getOriginalFilename());
        
        String imageUrl = s3Service.uploadImage(file, "news");
        
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 上傳 Banner 圖片
     */
    @PostMapping("/banner")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "上傳 Banner 圖片", description = "上傳 Banner 的圖片到 S3")
    public ResponseEntity<Map<String, String>> uploadBannerImage(
            @RequestParam("file") 
            @Parameter(description = "圖片檔案（限制 5MB，支援 jpg/png/gif/webp）")
            MultipartFile file) {
        
        log.info("📤 上傳 Banner 圖片，檔案名稱：{}", file.getOriginalFilename());
        
        String imageUrl = s3Service.uploadImage(file, "banner");
        
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 上傳 Lottery 商品圖片
     */
    @PostMapping("/lottery")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "上傳 Lottery 商品圖片", description = "上傳商品的圖片到 S3")
    public ResponseEntity<Map<String, String>> uploadLotteryImage(
            @RequestParam("file") 
            @Parameter(description = "圖片檔案（限制 5MB，支援 jpg/png/gif/webp）")
            MultipartFile file) {
        
        log.info("📤 上傳 Lottery 圖片，檔案名稱：{}", file.getOriginalFilename());
        
        String imageUrl = s3Service.uploadImage(file, "lottery");
        
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 上傳 Prize 獎品圖片
     */
    @PostMapping("/prize")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "上傳 Prize 獎品圖片", description = "上傳獎品的圖片到 S3")
    public ResponseEntity<Map<String, String>> uploadPrizeImage(
            @RequestParam("file") 
            @Parameter(description = "圖片檔案（限制 5MB，支援 jpg/png/gif/webp）")
            MultipartFile file) {
        
        log.info("📤 上傳 Prize 圖片，檔案名稱：{}", file.getOriginalFilename());
        
        String imageUrl = s3Service.uploadImage(file, "prize");
        
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 刪除圖片
     */
    @PostMapping("/store")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "銝 Store ??", description = "銝摨振??")
    public ResponseEntity<Map<String, String>> uploadStoreImage(
            @RequestParam("file")
            @Parameter(description = "??瑼?嚗???5MB嚗??jpg/png/gif/webp嚗?")
            MultipartFile file) {

        log.info("? 銝 Store ??嚗?獢?蝔梧?{}", file.getOriginalFilename());

        String imageUrl = s3Service.uploadImage(file, "store");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/store-logo")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")
    @Operation(summary = "銝 Store Logo", description = "銝摨振 Logo ??")
    public ResponseEntity<Map<String, String>> uploadStoreLogo(
            @RequestParam("file")
            @Parameter(description = "??瑼?嚗???5MB嚗??jpg/png/gif/webp嚗?")
            MultipartFile file) {

        log.info("? 銝 Store Logo 嚗?獢?蝔梧?{}", file.getOriginalFilename());

        String imageUrl = s3Service.uploadImage(file, "store-logo");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/store-cover")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")
    @Operation(summary = "銝 Store Cover", description = "銝摨振撠??")
    public ResponseEntity<Map<String, String>> uploadStoreCover(
            @RequestParam("file")
            @Parameter(description = "??瑼?嚗???5MB嚗??jpg/png/gif/webp嚗?")
            MultipartFile file) {

        log.info("? 銝 Store Cover 嚗?獢?蝔梧?{}", file.getOriginalFilename());

        String imageUrl = s3Service.uploadImage(file, "store-cover");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "刪除圖片", description = "從 S3 刪除圖片")
    public ResponseEntity<Void> deleteImage(
            @RequestParam 
            @Parameter(description = "圖片 URL", example = "https://xxx.s3.amazonaws.com/news/xxx.jpg")
            String imageUrl) {
        
        log.info("🗑️ 刪除圖片，URL：{}", imageUrl);
        
        s3Service.deleteImage(imageUrl);
        
        return ResponseEntity.ok().build();
    }
}
