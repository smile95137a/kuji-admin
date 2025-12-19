package com.group.admin.mapper;

import com.group.admin.entity.SignIn;
import com.group.admin.example.SignInExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SignInMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(SignIn row);

    SignIn selectByPrimaryKey(@Param("id") Long id);

    List<SignIn> selectAll();

    int updateByPrimaryKey(SignIn row);

    List<SignIn> selectByExample(SignInExample example);

    long countByExample(SignInExample example);

    int deleteByExample(SignInExample example);

}
