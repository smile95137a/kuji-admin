package com.group.admin.controller.api;

import com.group.admin.entity.District;
import com.group.admin.service.DistrictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 行政區 API（前台使用）
 * 
 * <p>路由：/api/district/**
 * <p>角色：公開（無需登入）
 * 
 * <p>提供台灣縣市、鄉鎮市區選擇功能
 * <p>用於地址輸入、物流設定等場景
 */
@Slf4j
@RestController
@RequestMapping("/district")
@RequiredArgsConstructor
@Tag(name = "行政區域", description = "台灣縣市、鄉鎮市區查詢 API")
public class DistrictController {
    
    private final DistrictService districtService;
    
    /**
     * 取得所有縣市列表
     * 
     * <p>回傳：["台北市", "新北市", "桃園市", ...]
     */
    @GetMapping("/cities")
    @Operation(summary = "取得所有縣市", description = "返回台灣所有縣市列表")
    public ResponseEntity<List<String>> getAllCities() {
        log.info("📍 查詢所有縣市");
        List<String> cities = districtService.getAllCities();
        return ResponseEntity.ok(cities);
    }
    
    /**
     * 取得指定縣市的鄉鎮市區列表
     * 
     * <p>範例：GET /api/district/districts/台北市
     * <p>回傳：[{"city":"台北市","district":"中正區","zipCode":"100"}, ...]
     */
    @GetMapping("/districts/{city}")
    @Operation(summary = "取得指定縣市的鄉鎮市區", description = "返回指定縣市下的所有行政區")
    public ResponseEntity<List<District>> getDistrictsByCity(@PathVariable String city) {
        log.info("📍 查詢縣市行政區: city={}", city);
        List<District> districts = districtService.getDistrictsByCity(city);
        return ResponseEntity.ok(districts);
    }
    
    /**
     * 取得所有縣市及其鄉鎮市區（樹狀結構）
     * 
     * <p>回傳：{"台北市": [District...], "新北市": [District...], ...}
     * <p>適合前端下拉選單使用
     */
    @GetMapping("/tree")
    @Operation(summary = "取得行政區樹狀結構", description = "返回所有縣市及其行政區的樹狀結構")
    public ResponseEntity<Map<String, List<District>>> getDistrictTree() {
        log.info("📍 查詢行政區樹狀結構");
        Map<String, List<District>> tree = districtService.getDistrictTree();
        return ResponseEntity.ok(tree);
    }
    
    /**
     * 取得完整的行政區資料（包含郵遞區號）
     * 
     * <p>回傳所有縣市 + 鄉鎮市區 + 郵遞區號的完整列表
     */
    @GetMapping("/all")
    @Operation(summary = "取得所有行政區", description = "返回所有縣市、鄉鎮市區及郵遞區號")
    public ResponseEntity<List<District>> getAllDistricts() {
        log.info("📍 查詢所有行政區");
        List<District> districts = districtService.getAllDistricts();
        return ResponseEntity.ok(districts);
    }
    
    /**
     * 根據縣市和區域取得完整資訊
     * 
     * <p>範例：GET /api/district?city=臺北市&districtName=中正區
     * <p>回傳：{"city":"臺北市","districtName":"中正區","zipCode":"100"}
     */
    @GetMapping
    @Operation(summary = "查詢指定行政區", description = "根據縣市和區域名稱查詢詳細資訊")
    public ResponseEntity<District> getDistrict(
            @RequestParam String city,
            @RequestParam String districtName) {
        log.info("📍 查詢行政區: city={}, districtName={}", city, districtName);
        District result = districtService.getDistrict(city, districtName);
        return ResponseEntity.ok(result);
    }
}
