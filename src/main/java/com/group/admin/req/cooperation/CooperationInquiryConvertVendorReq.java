package com.group.admin.req.cooperation;

import lombok.Data;

/**
 * 合作洽談轉成廠商帳號 Request
 */
@Data
public class CooperationInquiryConvertVendorReq {

    /**
     * 合作洽談 ID
     */
    private String id;

    /**
     * 廠商帳號初始密碼
     *
     * 若未傳入，後端會使用預設密碼：
     * Vendor@123456
     */
    private String password;

    /**
     * 轉成廠商備註
     */
    private String remark;
}