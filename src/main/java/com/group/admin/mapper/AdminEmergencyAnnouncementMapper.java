package com.group.admin.mapper;

import com.group.admin.entity.AdminEmergencyAnnouncement;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AdminEmergencyAnnouncementMapper {

    int insert(AdminEmergencyAnnouncement row);

    int insertSelective(AdminEmergencyAnnouncement row);

    AdminEmergencyAnnouncement selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AdminEmergencyAnnouncement row);

    int updateByPrimaryKey(AdminEmergencyAnnouncement row);

    int deleteByPrimaryKey(String id);

    List<AdminEmergencyAnnouncement> selectAll();

    List<AdminEmergencyAnnouncement> selectActive(@Param("now") Date now);
}