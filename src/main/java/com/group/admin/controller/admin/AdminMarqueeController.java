package com.group.admin.controller.admin;

import com.group.admin.dto.req.MarqueeCreateReq;
import com.group.admin.dto.req.MarqueeUpdateReq;
import com.group.admin.entity.Marquee;
import com.group.admin.service.MarqueeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 跑馬燈管理 API（後台）
 */
@Slf4j
@RestController
@RequestMapping("/admin/marquee")
@RequiredArgsConstructor
public class AdminMarqueeController {
    
    private final MarqueeService marqueeService;
    
    /**
     * 取得所有跑馬燈
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<List<Marquee>> getAllMarquees() {
        return ResponseEntity.ok(marqueeService.getAllMarquees());
    }
    
    /**
     * 取得單一跑馬燈
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<Marquee> getMarquee(@PathVariable String id) {
        return ResponseEntity.ok(marqueeService.getMarqueeById(id));
    }
    
    /**
     * 新增跑馬燈
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<Marquee> createMarquee(@RequestBody MarqueeCreateReq req) {
        Marquee marquee = new Marquee();
        marquee.setContent(req.getContent());
        marquee.setLinkUrl(req.getLinkUrl());
        marquee.setLinkType(req.getLinkType());
        marquee.setPriority(req.getPriority());
        marquee.setBgColor(req.getBgColor());
        marquee.setTextColor(req.getTextColor());
        marquee.setStartTime(req.getStartTime());
        marquee.setEndTime(req.getEndTime());
        marquee.setIsActive(req.getIsActive() != null && req.getIsActive() ? (byte) 1 : (byte) 0);
        
        return ResponseEntity.ok(marqueeService.createMarquee(marquee));
    }
    
    /**
     * 更新跑馬燈
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<Marquee> updateMarquee(@PathVariable String id, @RequestBody MarqueeUpdateReq req) {
        Marquee marquee = marqueeService.getMarqueeById(id);
        if (marquee == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (req.getContent() != null) marquee.setContent(req.getContent());
        if (req.getLinkUrl() != null) marquee.setLinkUrl(req.getLinkUrl());
        if (req.getLinkType() != null) marquee.setLinkType(req.getLinkType());
        if (req.getPriority() != null) marquee.setPriority(req.getPriority());
        if (req.getBgColor() != null) marquee.setBgColor(req.getBgColor());
        if (req.getTextColor() != null) marquee.setTextColor(req.getTextColor());
        if (req.getStartTime() != null) marquee.setStartTime(req.getStartTime());
        if (req.getEndTime() != null) marquee.setEndTime(req.getEndTime());
        if (req.getIsActive() != null) marquee.setIsActive(req.getIsActive() ? (byte) 1 : (byte) 0);
        
        return ResponseEntity.ok(marqueeService.updateMarquee(marquee));
    }
    
    /**
     * 刪除跑馬燈
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<Void> deleteMarquee(@PathVariable String id) {
        marqueeService.deleteMarquee(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 更新跑馬燈狀態
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<Void> updateStatus(@PathVariable String id, @RequestParam String status) {
        marqueeService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 手動廣播所有啟用中的跑馬燈
     */
    @PostMapping("/broadcast")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<Void> broadcastMarquees() {
        marqueeService.broadcastAllActiveMarquees();
        return ResponseEntity.ok().build();
    }
}
