package com.group.admin.mapper;

import com.group.admin.entity.Store;
import com.group.admin.example.StoreExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StoreMapper {
    long countByExample(StoreExample example);

    int deleteByExample(StoreExample example);

    int deleteByPrimaryKey(String id);

    int insert(Store row);

    int insertSelective(Store row);

    List<Store> selectByExampleWithBLOBs(StoreExample example);

    List<Store> selectByExample(StoreExample example);

    Store selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") Store row, @Param("example") StoreExample example);

    int updateByExampleWithBLOBs(@Param("row") Store row, @Param("example") StoreExample example);

    int updateByExample(@Param("row") Store row, @Param("example") StoreExample example);

    int updateByPrimaryKeySelective(Store row);

    int updateByPrimaryKeyWithBLOBs(Store row);

    int updateByPrimaryKey(Store row);

    List<Store> selectEnabledStores(@Param("offset") int offset, @Param("limit") int limit);

    Long countEnabledStores();
}