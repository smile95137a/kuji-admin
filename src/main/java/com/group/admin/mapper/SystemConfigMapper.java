package com.group.admin.mapper;

import com.group.admin.entity.SystemConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SystemConfigMapper {
    int insert(SystemConfig row);

    int insertSelective(SystemConfig row);

    SystemConfig selectByPrimaryKey(String id);

    SystemConfig selectByConfigKey(String configKey);

    List<SystemConfig> selectAll();

    List<SystemConfig> selectByConfigGroup(String configGroup);

    int countByConfigKey(String configKey);

    int updateByPrimaryKeySelective(SystemConfig row);

    int updateByPrimaryKeyAndVersion(@Param("row") SystemConfig row, @Param("version") Integer version);

    int deleteByPrimaryKey(String id);
}
