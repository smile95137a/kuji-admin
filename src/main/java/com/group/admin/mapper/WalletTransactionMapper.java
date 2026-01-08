package com.group.admin.mapper;

import com.group.admin.entity.WalletTransaction;
import com.group.admin.example.WalletTransactionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface WalletTransactionMapper {
    long countByExample(WalletTransactionExample example);

    int deleteByExample(WalletTransactionExample example);

    int deleteByPrimaryKey(String id);

    int insert(WalletTransaction row);

    int insertSelective(WalletTransaction row);

    List<WalletTransaction> selectByExample(WalletTransactionExample example);

    WalletTransaction selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") WalletTransaction row, @Param("example") WalletTransactionExample example);

    int updateByExample(@Param("row") WalletTransaction row, @Param("example") WalletTransactionExample example);

    int updateByPrimaryKeySelective(WalletTransaction row);

    int updateByPrimaryKey(WalletTransaction row);
}