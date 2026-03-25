package com.group.admin.res.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 訂單狀態變更歷史回應
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
    private String operatorId;
    private String operatorType;
    private String remark;
    private LocalDateTime createdAt;
}
