package com.group.admin.res.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 訂單狀態歷程回應
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusLogRes {

    private String fromStatus;
    private String fromStatusLabel;
    private String toStatus;
    private String toStatusLabel;
    /** 僅管理端填入，玩家端為 null */
    private String operatorId;
    private String operatorType;
    private String remark;
    private LocalDateTime createdAt;
}
