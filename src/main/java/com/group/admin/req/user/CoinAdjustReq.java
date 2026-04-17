package com.group.admin.req.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoinAdjustReq {
    @NotNull
    private String coinType; // GOLD / BONUS
    @NotNull
    private Long amount; // positive = add, negative = deduct
    private String remark;
}
