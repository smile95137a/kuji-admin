package com.group.admin.req.systemconfig;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SystemConfigCreateReq {
    @NotBlank(message = "configKey 不能為空")
    @Size(max = 100, message = "configKey 長度不能超過 100")
    private String configKey;

    @NotBlank(message = "configValue 不能為空")
    @Size(max = 500, message = "configValue 長度不能超過 500")
    private String configValue;

    @NotBlank(message = "configType 不能為空")
    @Size(max = 20, message = "configType 長度不能超過 20")
    private String configType;

    @NotBlank(message = "configGroup 不能為空")
    @Size(max = 50, message = "configGroup 長度不能超過 50")
    private String configGroup;

    @Size(max = 500, message = "description 長度不能超過 500")
    private String description;
}
