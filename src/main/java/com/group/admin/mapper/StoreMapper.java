package com.group.admin.mapper;

import com.group.admin.entity.Store;
import com.group.admin.example.StoreExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StoreMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(Store row);

    Store selectByPrimaryKey(@Param("id") String id);

    List<Store> selectAll();

    int updateByPrimaryKey(Store row);

    List<Store> selectByExample(StoreExample example);

    long countByExample(StoreExample example);

    int deleteByExample(StoreExample example);

}
