package com.group.admin.req.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateAccountStatusReq {
    @NotBlank
    private String status; // ACTIVE or INACTIVE

    @Size(max = 500)
    private String remark;
}
