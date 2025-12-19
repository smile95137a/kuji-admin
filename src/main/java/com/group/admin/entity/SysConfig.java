package com.group.admin.entity;

import lombok.Data;

@Data
public class SysConfig {
    private String variable;
    private String value;
    private java.time.LocalDateTime setTime;
    private String setBy;
}
