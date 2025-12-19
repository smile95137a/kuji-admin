package com.group.admin.entity;

import lombok.Data;

@Data
public class PointLog {
    private String id;
    private String userId;
    private String type; // deposit/deduct/draw/refund
    private Long amount;
    private Long beforeBalance;
    private Long afterBalance;
    private String remark;
    private java.time.LocalDateTime createDate;
}
