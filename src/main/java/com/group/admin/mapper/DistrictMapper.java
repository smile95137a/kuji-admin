package com.group.admin.mapper;

import com.group.admin.entity.District;
import com.group.admin.example.DistrictExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DistrictMapper {
    long countByExample(DistrictExample example);

    int deleteByExample(DistrictExample example);

    int deleteByPrimaryKey(String id);

    int insert(District row);

    int insertSelective(District row);

    List<District> selectByExample(DistrictExample example);

    District selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") District row, @Param("example") DistrictExample example);

    int updateByExample(@Param("row") District row, @Param("example") DistrictExample example);

    int updateByPrimaryKeySelective(District row);

    int updateByPrimaryKey(District row);
    
    // ========== 自定義查詢方法（使用 Annotation）==========
    
    /**
     * 查詢所有縣市（不重複）
     */
    @Select("SELECT DISTINCT city FROM district ORDER BY city")
    List<String> selectAllCities();
    
    /**
     * 根據縣市查詢所有區域
     */
    @Select("SELECT * FROM district WHERE city = #{city} ORDER BY district")
    List<District> selectByCity(@Param("city") String city);
    
    /**
     * 查詢所有區域
     */
    @Select("SELECT * FROM district ORDER BY city, district")
    List<District> selectAll();
    
    /**
     * 根據縣市和區域查詢
     */
    @Select("SELECT * FROM district WHERE city = #{city} AND district = #{district}")
    District selectByCityAndDistrict(@Param("city") String city, @Param("district") String district);
}