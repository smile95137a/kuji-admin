package com.group.admin.service.logistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConvenienceStore {
    private String storeCode;
    private String storeName;
    private String storeAddress;
    private String storeType;
}
