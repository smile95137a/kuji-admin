package com.group.admin.res.admin;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAccountRes {
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
