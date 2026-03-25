package com.group.admin.controller.admin;

import com.group.admin.req.draw.AdminDrawHistoryReq;
import com.group.admin.res.draw.AdminDrawHistoryRes;
import com.group.admin.service.AdminDrawHistoryService;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 後台抽獎歷史查詢 API
 */
@Slf4j
@RestController
@RequestMapping("/admin/lottery")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
public class AdminDrawHistoryController {

    private final AdminDrawHistoryService adminDrawHistoryService;

    @GetMapping("/{lotteryId}/draws")
    public ResponseEntity<AdminDrawHistoryRes> getDrawHistory(
            @PathVariable String lotteryId,
            @ModelAttribute AdminDrawHistoryReq req) {

        String callerId = SecurityUtils.getCurrentAdminUserId();
        String callerRole = getCurrentRole();

        log.info("📊 [Admin] 查詢抽獎歷史: lotteryId={}, callerId={}, role={}", lotteryId, callerId, callerRole);

        AdminDrawHistoryRes result = adminDrawHistoryService.getDrawHistory(lotteryId, callerId, callerRole, req);
        return ResponseEntity.ok(result);
    }

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role)) return "ROLE_ADMIN";
                if ("ROLE_STORE_OWNER".equals(role)) return "ROLE_STORE_OWNER";
            }
        }
        return "UNKNOWN";
    }
}
