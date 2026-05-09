package com.group.admin.req.emergencyannouncement;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAdminEmergencyAnnouncementStatusReq {

    /** 狀態：DRAFT / ACTIVE / INACTIVE */
    @NotBlank(message = "請選擇公告狀態")
    private String status;
}