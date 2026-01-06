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
@PreAuthorize("hasRole('ADMIN')")
public class UploadController {

    private final S3Service s3Service;

    /**
     * 上傳 News 圖片
     */
    @PostMapping("/news")
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
    @DeleteMapping
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
