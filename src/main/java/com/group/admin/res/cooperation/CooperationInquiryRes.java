package com.group.admin.res.cooperation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CooperationInquiryRes {

    private String id;

    private String company;

    private String name;

    private String email;

    private String phone;

    private String type;

    private String message;

    private String status;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date updatedAt;
}