package com.group.admin.req.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateAccountRoleReq {
    @NotBlank
    private String roleType; // STORE_OWNER or STORE_EDITOR

    @NotBlank
    private String storeId;
}
