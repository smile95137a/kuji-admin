package com.group.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 權限變更審計日誌
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionAuditLog {

    private String id;

    private String operatorId;

    private String targetRoleId;

    private String action;

    private String beforeSnapshot;

    private String afterSnapshot;

    private LocalDateTime createdAt;
}
