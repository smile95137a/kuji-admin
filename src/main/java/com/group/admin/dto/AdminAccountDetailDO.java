package com.group.admin.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdminAccountDetailDO {
    private String id;
    private String email;
    private String displayName;
    private String phone;
    private String status;
    private Boolean forceChangePassword;
    private LocalDateTime lastLoginAt;
    private String roleType;
    private String storeId;
    private String storeName;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String remark;
}
