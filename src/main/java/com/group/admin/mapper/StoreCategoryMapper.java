package com.group.admin.mapper;

import com.group.admin.entity.StoreCategory;
import com.group.admin.example.StoreCategoryExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StoreCategoryMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(StoreCategory row);

    StoreCategory selectByPrimaryKey(@Param("id") String id);

    List<StoreCategory> selectAll();

    int updateByPrimaryKey(StoreCategory row);

    List<StoreCategory> selectByExample(StoreCategoryExample example);

    long countByExample(StoreCategoryExample example);

    int deleteByExample(StoreCategoryExample example);

}
