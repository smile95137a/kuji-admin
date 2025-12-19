package com.group.admin.entity;

import lombok.Data;

@Data
public class ProductCategory {
    private Long categoryId;
    private String categoryName;
    private String categoryUuid;
    private Long productSort;
    private Long maxProductSort;
}
