package com.group.admin.entity;

import lombok.Data;

@Data
public class UserFavoriteStoreProducts {
    private Long id;
    private Long storeProductId;
    private Long userId;
}
