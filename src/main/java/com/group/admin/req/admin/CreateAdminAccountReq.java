package com.group.admin.req.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAdminAccountReq {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(max = 100)
    private String displayName;

    private String phone;

    @NotBlank
    private String roleType; // STORE_OWNER or STORE_EDITOR

    @NotBlank
    private String storeId;

    @Size(max = 500)
    private String remark;
}
