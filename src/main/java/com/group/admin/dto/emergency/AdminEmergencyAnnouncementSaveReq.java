package com.group.admin.dto.emergency;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEmergencyAnnouncementSaveReq {

    private String id;

    private String title;

    private String content;

    /**
     * MAINTENANCE / UPDATE / NOTICE
     */
    private String announcementType;

    /**
     * DRAFT / ACTIVE / INACTIVE
     */
    private String status;

    private Date displayStartTime;

    private Date displayEndTime;

    private Date maintenanceStartTime;

    private Date maintenanceEndTime;

    private Boolean forceShow;

    private Integer sortOrder;
}