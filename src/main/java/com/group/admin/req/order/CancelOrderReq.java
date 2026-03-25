package com.group.admin.req.order;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 取消訂單請求
 */
@Data
public class CancelOrderReq {

    @Size(max = 500, message = "取消原因不可超過500字")
    private String cancelReason;
}
