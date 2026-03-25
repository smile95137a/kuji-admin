package com.group.admin.controller.admin;

import com.group.admin.res.prizebox.PrizeBoxItemRes;
import com.group.admin.res.prizebox.PrizeBoxSummaryRes;
import com.group.admin.service.PrizeBoxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 後台獎品盒管理 Controller
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/prize-box")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPrizeBoxController {
    
    private final PrizeBoxService prizeBoxService;
    
    /**
     * 查詢玩家獎品盒
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<PrizeBoxItemRes>> getPrizeBox(@PathVariable String userId) {
        log.info("🔍 [Admin] 查詢玩家獎品盒：userId={}", userId);
        List<PrizeBoxItemRes> prizeBox = prizeBoxService.getPrizeBox(userId);
        return ResponseEntity.ok(prizeBox);
    }
    
    /**
     * 按店家分組查詢獎品盒
     */
    @GetMapping("/summary/{userId}")
    public ResponseEntity<List<PrizeBoxSummaryRes>> getSummaryByStore(@PathVariable String userId) {
        log.info("🔍 [Admin] 查詢玩家獎品盒（按店家分組）：userId={}", userId);
        List<PrizeBoxSummaryRes> summary = prizeBoxService.getSummaryByStore(userId);
        return ResponseEntity.ok(summary);
    }
}
