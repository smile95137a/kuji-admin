package com.group.admin.mapper;

import com.group.admin.entity.RefreshToken;
import com.group.admin.example.RefreshTokenExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RefreshTokenMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(RefreshToken row);

    RefreshToken selectByPrimaryKey(@Param("id") String id);

    List<RefreshToken> selectAll();

    int updateByPrimaryKey(RefreshToken row);

    List<RefreshToken> selectByExample(RefreshTokenExample example);

    long countByExample(RefreshTokenExample example);

    int deleteByExample(RefreshTokenExample example);

}
