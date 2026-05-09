package com.group.admin.controller.admin;

import com.group.admin.req.emergencyannouncement.AdminEmergencyAnnouncementFilterCondition;
import com.group.admin.req.emergencyannouncement.CreateAdminEmergencyAnnouncementReq;
import com.group.admin.req.emergencyannouncement.UpdateAdminEmergencyAnnouncementReq;
import com.group.admin.req.emergencyannouncement.UpdateAdminEmergencyAnnouncementStatusReq;
import com.group.admin.res.emergencyannouncement.AdminEmergencyAnnouncementRes;
import com.group.admin.service.AdminEmergencyAnnouncementService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/emergency-announcements")
@Tag(name = "緊急公告管理", description = "Admin 管理緊急公告 API")
public class AdminEmergencyAnnouncementController {

    private final AdminEmergencyAnnouncementService adminEmergencyAnnouncementService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminEmergencyAnnouncementRes> createAnnouncement(
            @Valid @RequestBody CreateAdminEmergencyAnnouncementReq req
    ) {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();

        log.info(
                "新增緊急公告: title={}, announcementType={}, status={}",
                req.getTitle(),
                req.getAnnouncementType(),
                req.getStatus()
        );

        AdminEmergencyAnnouncementRes res =
                adminEmergencyAnnouncementService.createAnnouncement(req, adminUserId);

        return ResponseEntity.status(201).body(res);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String announcementType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String displayStartTime,
            @RequestParam(required = false) String displayEndTime,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        log.info(
                "查詢緊急公告列表: page={}, size={}, status={}, announcementType={}",
                page,
                size,
                status,
                announcementType
        );

        AdminEmergencyAnnouncementFilterCondition filters =
                new AdminEmergencyAnnouncementFilterCondition();

        filters.setStatus(status);
        filters.setAnnouncementType(announcementType);
        filters.setKeyword(keyword);
        filters.setDisplayStartTime(displayStartTime);
        filters.setDisplayEndTime(displayEndTime);
        filters.setSortBy(sortBy);
        filters.setSortDir(sortDir);

        return ResponseEntity.ok(
                adminEmergencyAnnouncementService.listAnnouncements(filters, page, size)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> listActiveAnnouncements() {
        log.info("查詢目前有效緊急公告");

        return ResponseEntity.ok(
                adminEmergencyAnnouncementService.listActiveAnnouncements()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminEmergencyAnnouncementRes> getAnnouncement(
            @PathVariable String id
    ) {
        log.info("查詢緊急公告明細: id={}", id);

        AdminEmergencyAnnouncementRes res =
                adminEmergencyAnnouncementService.getAnnouncement(id);

        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminEmergencyAnnouncementRes> updateAnnouncement(
            @PathVariable String id,
            @Valid @RequestBody UpdateAdminEmergencyAnnouncementReq req
    ) {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();

        log.info(
                "編輯緊急公告: id={}, title={}, status={}",
                id,
                req.getTitle(),
                req.getStatus()
        );

        AdminEmergencyAnnouncementRes res =
                adminEmergencyAnnouncementService.updateAnnouncement(id, req, adminUserId);

        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminEmergencyAnnouncementRes> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateAdminEmergencyAnnouncementStatusReq req
    ) {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();

        log.info("更新緊急公告狀態: id={}, status={}", id, req.getStatus());

        AdminEmergencyAnnouncementRes res =
                adminEmergencyAnnouncementService.updateStatus(id, req, adminUserId);

        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable String id) {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();

        log.info("刪除緊急公告: id={}", id);

        adminEmergencyAnnouncementService.deleteAnnouncement(id, adminUserId);

        return ResponseEntity.noContent().build();
    }
}