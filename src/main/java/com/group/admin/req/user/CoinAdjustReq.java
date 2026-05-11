package com.group.admin.req.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoinAdjustReq {
    @NotBlank
    private String coinType; // GOLD / BONUS
    @NotNull
    private Long amount; // positive = add, negative = deduct
    @NotBlank
    private String remark;
}
