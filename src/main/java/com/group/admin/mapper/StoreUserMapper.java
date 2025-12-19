package com.group.admin.mapper;

import com.group.admin.entity.StoreUser;
import com.group.admin.example.StoreUserExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StoreUserMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(StoreUser row);

    StoreUser selectByPrimaryKey(@Param("id") String id);

    List<StoreUser> selectAll();

    int updateByPrimaryKey(StoreUser row);

    List<StoreUser> selectByExample(StoreUserExample example);

    long countByExample(StoreUserExample example);

    int deleteByExample(StoreUserExample example);

}
