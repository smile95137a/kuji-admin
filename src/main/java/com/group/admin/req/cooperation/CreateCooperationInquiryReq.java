package com.group.admin.req.cooperation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCooperationInquiryReq {

    /** 公司 / 單位名稱 */
    private String company;

    /** 聯絡人姓名 */
    @NotBlank(message = "請輸入聯絡人姓名")
    private String name;

    /** 電子郵件 */
    @NotBlank(message = "請輸入電子郵件")
    @Email(message = "電子郵件格式不正確")
    private String email;

    /** 聯絡電話 */
    private String phone;

    /** 合作類型：IP / SUPPLY / CHANNEL / MARKETING */
    @NotBlank(message = "請選擇合作類型")
    private String type;

    /** 需求簡述 */
    @NotBlank(message = "請輸入需求簡述")
    private String message;
}