package com.group.admin.req.admin;

import lombok.Data;

@Data
public class AccountFilterCondition {
    private String status;
    private String roleType;
    private String storeId;
    private String keyword;
    private String sortBy;   // e.g. created_at, email, display_name, last_login_at
    private String sortDir;  // ASC or DESC
    private int offset;
    private int pageSize;
}
