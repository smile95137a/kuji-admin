package com.group.admin.mapper;

import com.group.admin.entity.UserWallet;
import com.group.admin.example.UserWalletExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserWalletMapper {
    long countByExample(UserWalletExample example);

    int deleteByExample(UserWalletExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserWallet row);

    int insertSelective(UserWallet row);

    List<UserWallet> selectByExample(UserWalletExample example);

    UserWallet selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") UserWallet row, @Param("example") UserWalletExample example);

    int updateByExample(@Param("row") UserWallet row, @Param("example") UserWalletExample example);

    int updateByPrimaryKeySelective(UserWallet row);

    int updateByPrimaryKey(UserWallet row);
}