package com.group.admin.req.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "出貨資訊請求")
public class ShipInfoReq {

    @NotBlank(message = "請選擇配送方式")
    @Schema(description = "配送方式（HOME_DELIVERY/SEVEN_ELEVEN/FAMILY_MART）", example = "HOME_DELIVERY")
    private String shippingMethod;

    @Schema(description = "收件人姓名（宅配必填）", example = "王小明")
    private String recipientName;

    @Schema(description = "收件人電話（宅配必填）", example = "0912345678")
    private String recipientPhone;

    @Schema(description = "收件地址（宅配必填）", example = "台北市信義區信義路五段7號")
    private String recipientAddress;

    @Schema(description = "超商分店代碼（超商取貨必填）", example = "167890")
    private String storeCode;

    @Schema(description = "超商分店名稱（超商取貨必填）", example = "信義門市")
    private String storeName;

    @Schema(description = "超商分店地址", example = "台北市信義區松仁路100號")
    private String storeAddress;

    @Schema(description = "備註")
    private String remark;
}
