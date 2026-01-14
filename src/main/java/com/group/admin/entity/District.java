package com.group.admin.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 行政區資料
 * 提供前端縣市、行政區下拉選單
 */
@Data
public class District {
    private String id;
    private String city;
    private String districtName;
    private String zipCode;
    private Integer orderNum;
    private LocalDateTime createdAt;
}
