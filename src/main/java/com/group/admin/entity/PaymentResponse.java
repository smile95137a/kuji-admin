package com.group.admin.entity;

import lombok.Data;

@Data
public class PaymentResponse {
    private String orderId;
    private String amount;
    private String avCode;
    private String bankName;
    private String checkString;
    private String currency;
    private String date;
    private String ePayAccount;
    private String invoiceNo;
    private String number;
    private String orderNo;
    private String outlay;
    private String result;
    private String retMsg;
    private String sendType;
    private String time;
    private Long userId;
}
