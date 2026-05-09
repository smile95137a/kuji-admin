package com.group.admin.controller.api;

import com.group.admin.res.emergencyannouncement.AdminEmergencyAnnouncementRes;
import com.group.admin.service.AdminEmergencyAnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public Emergency Announcement API — returns ACTIVE announcements
 * when current time is within displayStartTime and displayEndTime.
 * No authentication required.
 */
@Slf4j
@RestController
@RequestMapping({
        "/emergency-announcement",
        "/emergency-announcements"
})
@RequiredArgsConstructor
public class ApiEmergencyAnnouncementController {

    private final AdminEmergencyAnnouncementService adminEmergencyAnnouncementService;

    @GetMapping({
            "",
            "/list",
            "/active"
    })
    public ResponseEntity<List<AdminEmergencyAnnouncementRes>> getActiveAnnouncements() {
        log.info("🚨 前台查詢緊急公告");

        return ResponseEntity.ok(
                adminEmergencyAnnouncementService.getActiveAnnouncements()
        );
    }
}