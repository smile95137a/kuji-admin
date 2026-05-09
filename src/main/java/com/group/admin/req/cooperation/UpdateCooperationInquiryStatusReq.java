package com.group.admin.req.cooperation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCooperationInquiryStatusReq {

    /** PENDING / PROCESSING / DONE / CLOSED */
    @NotBlank(message = "請選擇處理狀態")
    private String status;

    /** 後台備註 */
    private String remark;
}