package com.group.admin.req.order;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 訂單取消請求
 */
@Data
public class OrderCancelReq {

    @Size(max = 500, message = "取消原因不可超過 500 字元")
    private String cancelReason;

    /** 相容舊版呼叫 getReason() */
    public String getReason() {
        return cancelReason;
    }
}
