package com.group.admin.req.logistics;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreMapReq {

    @NotBlank(message = "物流方式不可為空")
    private String shippingMethod;

    @NotBlank(message = "返回網址不可為空")
    private String returnUrl;
}
