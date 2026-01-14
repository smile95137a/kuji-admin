package com.group.admin.mapper;

import com.group.admin.entity.District;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 行政區 Mapper
 */
@Mapper
public interface DistrictMapper {
    
    @Select("SELECT * FROM district ORDER BY city, order_num")
    List<District> selectAll();
    
    @Select("SELECT DISTINCT city FROM district ORDER BY order_num")
    List<String> selectAllCities();
    
    @Select("SELECT * FROM district WHERE city = #{city} ORDER BY order_num")
    List<District> selectByCity(String city);
    
    @Select("SELECT * FROM district WHERE id = #{id}")
    District selectById(String id);
    
    @Select("SELECT * FROM district WHERE city = #{city} AND district_name = #{districtName}")
    District selectByCityAndDistrict(@Param("city") String city, @Param("districtName") String districtName);
}
