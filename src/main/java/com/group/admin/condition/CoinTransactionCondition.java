package com.group.admin.condition;

import com.group.admin.req.common.BaseCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoinTransactionCondition extends BaseCondition {

    private String userId;
    private String transactionType;
    // 舊前台相容欄位
    private String type;
    private String coinType;
    private String relatedId;
}