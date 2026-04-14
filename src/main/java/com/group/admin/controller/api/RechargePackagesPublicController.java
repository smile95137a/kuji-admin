package com.group.admin.controller.api;

import com.group.admin.result.ApiResponse;
import com.group.admin.service.RechargePlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/recharge-plans")
@RequiredArgsConstructor
public class RechargePackagesPublicController {

    private final RechargePlanService rechargePlanService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<?>>> listActivePlans() {
        log.info("🔍 [API] GET /recharge-plans");
        return ResponseEntity.ok(ApiResponse.success(rechargePlanService.getActivePlans()));
    }
}
