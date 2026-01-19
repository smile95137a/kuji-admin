package com.group.admin.mapper;

import com.group.admin.entity.UserAddress;
import com.group.admin.example.UserAddressExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserAddressMapper {
    long countByExample(UserAddressExample example);

    int deleteByExample(UserAddressExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserAddress row);

    int insertSelective(UserAddress row);

    List<UserAddress> selectByExample(UserAddressExample example);

    UserAddress selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") UserAddress row, @Param("example") UserAddressExample example);

    int updateByExample(@Param("row") UserAddress row, @Param("example") UserAddressExample example);

    int updateByPrimaryKeySelective(UserAddress row);

    int updateByPrimaryKey(UserAddress row);
}