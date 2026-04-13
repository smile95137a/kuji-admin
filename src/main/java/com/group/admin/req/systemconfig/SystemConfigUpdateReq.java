package com.group.admin.req.systemconfig;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SystemConfigUpdateReq {
    @NotBlank(message = "configValue 不能為空")
    @Size(max = 500, message = "configValue 長度不能超過 500")
    private String configValue;

    @Size(max = 500, message = "description 長度不能超過 500")
    private String description;

    @NotNull(message = "version 不能為空")
    private Integer version;
}
