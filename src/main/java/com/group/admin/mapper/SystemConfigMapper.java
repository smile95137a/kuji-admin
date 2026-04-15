package com.group.admin.mapper;

import com.group.admin.entity.SystemConfig;
import com.group.admin.example.SystemConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SystemConfigMapper {
    long countByExample(SystemConfigExample example);

    int deleteByExample(SystemConfigExample example);

    int deleteByPrimaryKey(String id);

    int insert(SystemConfig row);

    int insertSelective(SystemConfig row);

    List<SystemConfig> selectByExample(SystemConfigExample example);

    SystemConfig selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") SystemConfig row, @Param("example") SystemConfigExample example);

    int updateByExample(@Param("row") SystemConfig row, @Param("example") SystemConfigExample example);

    int updateByPrimaryKeySelective(SystemConfig row);

    int updateByPrimaryKey(SystemConfig row);

    // ---------- custom methods ----------

    List<SystemConfig> selectAll();

    SystemConfig selectByConfigKey(@Param("configKey") String configKey);

    List<SystemConfig> selectByConfigGroup(@Param("configGroup") String configGroup);

    int countByConfigKey(@Param("configKey") String configKey);

    int updateByPrimaryKeyAndVersion(@Param("entity") SystemConfig entity, @Param("version") Integer version);
}