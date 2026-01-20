package com.group.admin.service.impl;

import com.group.admin.entity.District;
import com.group.admin.repository.DistrictRepository;
import com.group.admin.service.DistrictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行政區服務實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {
    
    private final DistrictRepository districtRepository;
    
    @Override
    public List<String> getAllCities() {
        return districtRepository.selectAllCities();
    }
    
    @Override
    public List<District> getDistrictsByCity(String city) {
        return districtRepository.selectByCity(city);
    }
    
    @Override
    public Map<String, List<District>> getDistrictTree() {
        List<District> allDistricts = districtRepository.selectAll();
        return allDistricts.stream()
            .collect(Collectors.groupingBy(District::getCity));
    }
    
    @Override
    public List<District> getAllDistricts() {
        return districtRepository.selectAll();
    }
    
    @Override
    public District getDistrict(String city, String districtName) {
        return districtRepository.selectByCityAndDistrict(city, districtName);
    }
}
