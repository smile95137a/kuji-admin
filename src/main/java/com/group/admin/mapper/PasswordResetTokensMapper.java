package com.group.admin.mapper;

import com.group.admin.entity.PasswordResetTokens;
import com.group.admin.example.PasswordResetTokensExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PasswordResetTokensMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(PasswordResetTokens row);

    PasswordResetTokens selectByPrimaryKey(@Param("id") Long id);

    List<PasswordResetTokens> selectAll();

    int updateByPrimaryKey(PasswordResetTokens row);

    List<PasswordResetTokens> selectByExample(PasswordResetTokensExample example);

    long countByExample(PasswordResetTokensExample example);

    int deleteByExample(PasswordResetTokensExample example);

}
