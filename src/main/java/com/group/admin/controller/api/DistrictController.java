package com.group.admin.controller.api;

import com.group.admin.entity.District;
import com.group.admin.mapper.DistrictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行政區 API（前台使用）
 * 提供縣市、鄉鎮市區選擇功能
 */
@Slf4j
@RestController
@RequestMapping("/district")
@RequiredArgsConstructor
public class DistrictController {
    
    private final DistrictMapper districtMapper;
    
    /**
     * 取得所有縣市列表
     */
    @GetMapping("/cities")
    public ResponseEntity<List<String>> getAllCities() {
        List<String> cities = districtMapper.selectAllCities();
        return ResponseEntity.ok(cities);
    }
    
    /**
     * 取得指定縣市的鄉鎮市區列表
     */
    @GetMapping("/districts/{city}")
    public ResponseEntity<List<District>> getDistrictsByCity(@PathVariable String city) {
        List<District> districts = districtMapper.selectByCity(city);
        return ResponseEntity.ok(districts);
    }
    
    /**
     * 取得所有縣市及其鄉鎮市區（樹狀結構）
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, List<District>>> getDistrictTree() {
        List<District> allDistricts = districtMapper.selectAll();
        
        Map<String, List<District>> tree = allDistricts.stream()
            .collect(Collectors.groupingBy(District::getCity));
        
        return ResponseEntity.ok(tree);
    }
    
    /**
     * 取得完整的行政區資料（包含郵遞區號）
     */
    @GetMapping("/all")
    public ResponseEntity<List<District>> getAllDistricts() {
        List<District> districts = districtMapper.selectAll();
        return ResponseEntity.ok(districts);
    }
    
    /**
     * 根據縣市和區域取得完整資訊
     */
    @GetMapping
    public ResponseEntity<District> getDistrict(
            @RequestParam String city,
            @RequestParam String district) {
        District result = districtMapper.selectByCityAndDistrict(city, district);
        return ResponseEntity.ok(result);
    }
}
