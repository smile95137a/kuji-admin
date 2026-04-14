package com.group.admin.req.wallet;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RechargeReq {
    @NotBlank(message = "planId 不可為空")
    private String planId;
}
