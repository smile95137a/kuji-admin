package com.group.admin.req.shippingmethod;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShippingMethodUpdateReq {

    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String code;

    @Size(max = 100)
    private String provider;

    @Min(0)
    private Long fee;

    private String status;

    private Integer sortOrder;
}
