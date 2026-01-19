package com.group.admin.service;

import com.group.admin.entity.District;

import java.util.List;
import java.util.Map;

/**
 * 行政區服務介面
 */
public interface DistrictService {
    
    /**
     * 取得所有縣市列表
     */
    List<String> getAllCities();
    
    /**
     * 取得指定縣市的鄉鎮市區列表
     */
    List<District> getDistrictsByCity(String city);
    
    /**
     * 取得所有縣市及其鄉鎮市區（樹狀結構）
     */
    Map<String, List<District>> getDistrictTree();
    
    /**
     * 取得所有行政區資料
     */
    List<District> getAllDistricts();
    
    /**
     * 根據縣市和區域取得完整資訊
     */
    District getDistrict(String city, String district);
}
