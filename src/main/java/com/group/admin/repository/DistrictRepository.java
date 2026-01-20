package com.group.admin.repository;

import com.group.admin.entity.District;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 行政區自定義查詢 Repository
 * 這個檔案不會被 MyBatis Generator 覆蓋
 * 
 * 資料庫欄位：id, city, district_name, zip_code, order_num, created_at
 */
@Mapper
public interface DistrictRepository {
    
    @Select("SELECT DISTINCT city FROM district ORDER BY city")
    List<String> selectAllCities();
    
    @Select("SELECT id, city, district_name, zip_code, order_num, created_at FROM district WHERE city = #{city} ORDER BY order_num, district_name")
    List<District> selectByCity(@Param("city") String city);
    
    @Select("SELECT id, city, district_name, zip_code, order_num, created_at FROM district ORDER BY city, order_num, district_name")
    List<District> selectAll();
    
    @Select("SELECT id, city, district_name, zip_code, order_num, created_at FROM district WHERE city = #{city} AND district_name = #{districtName}")
    District selectByCityAndDistrict(@Param("city") String city, @Param("districtName") String districtName);
}
