package com.group.admin.res.systemconfig;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SystemConfigRes {
    private String id;
    private String configKey;
    private String configValue;
    private String configType;
    private String configGroup;
    private String description;
    private Integer version;
    private LocalDateTime updatedAt;
}
