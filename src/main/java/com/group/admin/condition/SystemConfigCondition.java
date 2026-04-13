package com.group.admin.condition;

import com.group.admin.req.common.BaseCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SystemConfigCondition extends BaseCondition {
    private String configGroup;
    private String configKey;
}
