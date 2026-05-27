package com.group.admin.controller.api;

import com.group.admin.req.logistics.StoreMapReq;
import com.group.admin.res.logistics.StoreMapRes;
import com.group.admin.service.logistics.LogisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    @PostMapping("/store-map")
    public ResponseEntity<StoreMapRes> createStoreMapUrl(@Valid @RequestBody StoreMapReq req) {
        String mapUrl = logisticsService.createStoreSelectorUrl(req.getShippingMethod(), req.getReturnUrl());
        return ResponseEntity.ok(new StoreMapRes(mapUrl));
    }

    @PostMapping(value = "/status-callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleStatusCallback(@RequestParam Map<String, String> params) {
        String response = logisticsService.handleStatusCallback(params);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(response);
    }
}
