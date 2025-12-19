package com.group.admin.mapper;

import com.group.admin.entity.UserTransaction;
import com.group.admin.example.UserTransactionExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserTransactionMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(UserTransaction row);

    UserTransaction selectByPrimaryKey(@Param("id") Long id);

    List<UserTransaction> selectAll();

    int updateByPrimaryKey(UserTransaction row);

    List<UserTransaction> selectByExample(UserTransactionExample example);

    long countByExample(UserTransactionExample example);

    int deleteByExample(UserTransactionExample example);

}
