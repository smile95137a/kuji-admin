package com.group.admin.mapper;

import com.group.admin.entity.SysConfig;
import com.group.admin.example.SysConfigExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysConfigMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(SysConfig row);

    SysConfig selectByPrimaryKey(@Param("id") String id);

    List<SysConfig> selectAll();

    int updateByPrimaryKey(SysConfig row);

    List<SysConfig> selectByExample(SysConfigExample example);

    long countByExample(SysConfigExample example);

    int deleteByExample(SysConfigExample example);

}
