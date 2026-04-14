package com.group.admin.req.shippingmethod;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShippingMethodCreateReq {

    @NotBlank(message = "運送方式名稱不得為空")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "代碼不得為空")
    @Size(max = 50)
    private String code;

    @Size(max = 100)
    private String provider;

    @NotNull(message = "運費不得為空")
    @Min(0)
    private Long fee;

    private Integer sortOrder = 0;
}
