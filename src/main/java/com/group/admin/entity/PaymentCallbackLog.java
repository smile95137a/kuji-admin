package com.group.admin.entity;

import lombok.Data;

@Data
public class PaymentCallbackLog {
    private Long id;
    private java.time.LocalDateTime createdAt;
    private String eDate;
    private String eMoney;
    private String eOrderno;
    private String ePayaccount;
    private String ePayInfo;
    private String eTime;
    private String orderId;
    private String payAmount;
    private String result;
    private String retMsg;
    private String sendType;
    private String strCheck;
}
