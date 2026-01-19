package com.group.admin.repository;

import com.group.admin.entity.District;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 行政區自定義查詢 Repository
 * 這個檔案不會被 MyBatis Generator 覆蓋
 */
@Mapper
public interface DistrictRepository {
    
    @Select("SELECT DISTINCT city FROM district ORDER BY city")
    List<String> selectAllCities();
    
    @Select("SELECT * FROM district WHERE city = #{city} ORDER BY district")
    List<District> selectByCity(@Param("city") String city);
    
    @Select("SELECT * FROM district ORDER BY city, district")
    List<District> selectAll();
    
    @Select("SELECT * FROM district WHERE city = #{city} AND district = #{district}")
    District selectByCityAndDistrict(@Param("city") String city, @Param("district") String district);
}
