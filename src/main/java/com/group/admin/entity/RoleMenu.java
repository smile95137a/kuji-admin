package com.group.admin.entity;

import lombok.Data;

@Data
public class RoleMenu {
    private String id;
    private String roleId;
    private String menuId;
    private Boolean canView;
    private Boolean canEdit;
    private Boolean canDelete;
    private java.time.LocalDateTime createdAt;
}
