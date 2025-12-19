package com.group.admin.mapper;

import com.group.admin.entity.StoreProduct;
import com.group.admin.example.StoreProductExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StoreProductMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(StoreProduct row);

    StoreProduct selectByPrimaryKey(@Param("id") String id);

    List<StoreProduct> selectAll();

    int updateByPrimaryKey(StoreProduct row);

    List<StoreProduct> selectByExample(StoreProductExample example);

    long countByExample(StoreProductExample example);

    int deleteByExample(StoreProductExample example);

}
