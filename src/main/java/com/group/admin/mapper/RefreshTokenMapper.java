package com.group.admin.mapper;

import com.group.admin.entity.RefreshToken;
import com.group.admin.example.RefreshTokenExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RefreshTokenMapper {
    long countByExample(RefreshTokenExample example);

    int deleteByExample(RefreshTokenExample example);

    int deleteByPrimaryKey(String id);

    int insert(RefreshToken row);

    int insertSelective(RefreshToken row);

    List<RefreshToken> selectByExample(RefreshTokenExample example);

    RefreshToken selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") RefreshToken row, @Param("example") RefreshTokenExample example);

    int updateByExample(@Param("row") RefreshToken row, @Param("example") RefreshTokenExample example);

    int updateByPrimaryKeySelective(RefreshToken row);

    int updateByPrimaryKey(RefreshToken row);
}