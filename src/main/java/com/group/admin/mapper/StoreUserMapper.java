package com.group.admin.mapper;

import com.group.admin.entity.StoreUser;
import com.group.admin.example.StoreUserExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StoreUserMapper {
    long countByExample(StoreUserExample example);

    int deleteByExample(StoreUserExample example);

    int deleteByPrimaryKey(String id);

    int insert(StoreUser row);

    int insertSelective(StoreUser row);

    List<StoreUser> selectByExample(StoreUserExample example);

    StoreUser selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") StoreUser row, @Param("example") StoreUserExample example);

    int updateByExample(@Param("row") StoreUser row, @Param("example") StoreUserExample example);

    int updateByPrimaryKeySelective(StoreUser row);

    int updateByPrimaryKey(StoreUser row);
}