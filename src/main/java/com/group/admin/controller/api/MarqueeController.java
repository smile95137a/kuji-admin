package com.group.admin.controller.api;

import com.group.admin.entity.Marquee;
import com.group.admin.service.MarqueeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 跑馬燈 API（前台使用）
 * 提供跑馬燈即時訊息
 */
@Slf4j
@RestController
@RequestMapping("/marquee")
@RequiredArgsConstructor
public class MarqueeController {
    
    private final MarqueeService marqueeService;
    
    /**
     * 取得所有啟用中的跑馬燈
     */
    @GetMapping
    public ResponseEntity<List<Marquee>> getActiveMarquees() {
        return ResponseEntity.ok(marqueeService.getActiveMarquees());
    }
}
