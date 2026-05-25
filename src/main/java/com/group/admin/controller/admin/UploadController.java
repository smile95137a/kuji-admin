package com.group.admin.controller.admin;

import com.group.admin.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 圖片上傳控制器。
 *
 * <p>負責後台圖片上傳至 S3 與刪除的管理 API。</p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/upload")
@RequiredArgsConstructor
@Tag(name = "圖片上傳管理", description = "圖片上傳至 S3 的後台管理 API")
public class UploadController {

    private final S3Service s3Service;

    /**
     * 上傳 News 圖片。
     */
    @PostMapping("/news")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "上傳 News 圖片", description = "上傳 News 或公告相關圖片至 S3")
    public ResponseEntity<Map<String, String>> uploadNewsImage(
            @RequestParam("file")
            @Parameter(description = "圖片檔案，大小上限 5MB，支援 jpg/png/gif/webp")
            MultipartFile file) {

        log.info("上傳 News 圖片: filename={}", file.getOriginalFilename());

        String imageUrl = s3Service.uploadImage(file, "news");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * 上傳 Banner 圖片。
     */
    @PostMapping("/banner")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "上傳 Banner 圖片", description = "上傳 Banner 輪播圖片至 S3")
    public ResponseEntity<Map<String, String>> uploadBannerImage(
            @RequestParam("file")
            @Parameter(description = "圖片檔案，大小上限 5MB，支援 jpg/png/gif/webp")
            MultipartFile file) {

        log.info("上傳 Banner 圖片: filename={}", file.getOriginalFilename());

        String imageUrl = s3Service.uploadImage(file, "banner");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * 上傳 Lottery 商品圖片。
     */
    @PostMapping("/lottery")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER','STORE_EDITOR')")
    @Operation(summary = "上傳 Lottery 商品圖片", description = "上傳 Lottery 商品圖片至 S3")
    public ResponseEntity<Map<String, String>> uploadLotteryImage(
            @RequestParam("file")
            @Parameter(description = "圖片檔案，大小上限 5MB，支援 jpg/png/gif/webp")
            MultipartFile file) {

        log.info("上傳 Lottery 圖片: filename={}", file.getOriginalFilename());

        String imageUrl = s3Service.uploadImage(file, "lottery");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * 上傳 Prize 商品圖片。
     */
    @PostMapping("/prize")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER','STORE_EDITOR')")
    @Operation(summary = "上傳 Prize 商品圖片", description = "上傳 Prize 商品圖片至 S3")
    public ResponseEntity<Map<String, String>> uploadPrizeImage(
            @RequestParam("file")
            @Parameter(description = "圖片檔案，大小上限 5MB，支援 jpg/png/gif/webp")
            MultipartFile file) {

        log.info("上傳 Prize 圖片: filename={}", file.getOriginalFilename());

        String imageUrl = s3Service.uploadImage(file, "prize");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * 上傳 Store 圖片。
     */
    @PostMapping("/store")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "上傳 Store 圖片", description = "上傳店家相關圖片")
    public ResponseEntity<Map<String, String>> uploadStoreImage(
            @RequestParam("file")
            @Parameter(description = "圖片檔案，大小上限 5MB，支援 jpg/png/gif/webp")
            MultipartFile file) {

        log.info("上傳 Store 圖片: filename={}", file.getOriginalFilename());

        String imageUrl = s3Service.uploadImage(file, "store");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * 上傳 Store Logo。
     */
    @PostMapping("/store-logo")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")
    @Operation(summary = "上傳 Store Logo", description = "上傳店家 Logo 圖片")
    public ResponseEntity<Map<String, String>> uploadStoreLogo(
            @RequestParam("file")
            @Parameter(description = "圖片檔案，大小上限 5MB，支援 jpg/png/gif/webp")
            MultipartFile file) {

        log.info("上傳 Store Logo: filename={}", file.getOriginalFilename());

        String imageUrl = s3Service.uploadImage(file, "store-logo");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * 上傳 Store Cover。
     */
    @PostMapping("/store-cover")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")
    @Operation(summary = "上傳 Store Cover", description = "上傳店家封面圖片")
    public ResponseEntity<Map<String, String>> uploadStoreCover(
            @RequestParam("file")
            @Parameter(description = "圖片檔案，大小上限 5MB，支援 jpg/png/gif/webp")
            MultipartFile file) {

        log.info("上傳 Store Cover: filename={}", file.getOriginalFilename());

        String imageUrl = s3Service.uploadImage(file, "store-cover");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * 刪除圖片。
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "刪除圖片", description = "從 S3 刪除圖片")
    public ResponseEntity<Void> deleteImage(
            @RequestParam
            @Parameter(description = "圖片 URL", example = "https://xxx.s3.amazonaws.com/news/xxx.jpg")
            String imageUrl) {

        log.info("刪除圖片: imageUrl={}", imageUrl);

        s3Service.deleteImage(imageUrl);

        return ResponseEntity.ok().build();
    }
}