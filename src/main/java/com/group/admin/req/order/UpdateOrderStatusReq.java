package com.group.admin.req.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 後台更新訂單狀態請求
 */
@Data
public class UpdateOrderStatusReq {

    @NotBlank(message = "目標狀態不可為空")
    private String targetStatus;

    private String trackingNo;

    @Size(max = 500, message = "備註不可超過500字")
    private String remark;
}
