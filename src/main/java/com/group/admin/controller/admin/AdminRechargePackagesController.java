package com.group.admin.controller.admin;

import com.group.admin.service.RechargePlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/recharge-packages")
@RequiredArgsConstructor
public class AdminRechargePackagesController {

    private final RechargePlanService rechargePlanService;

    @GetMapping
    public ResponseEntity<List<?>> listPackages(
            @RequestParam(required = false) Boolean isActive) {
        log.info("🔍 [Admin] GET /admin/recharge-packages, isActive={}", isActive);
        List<?> plans;
        if (isActive != null && isActive) {
            plans = rechargePlanService.getActivePlans();
        } else {
            plans = rechargePlanService.getAllPlans();
        }
        return ResponseEntity.ok(plans);
    }
}