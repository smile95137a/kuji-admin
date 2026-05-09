package com.group.admin.req.cooperation;

import lombok.Data;

@Data
public class CooperationInquiryFilterCondition {

    /** 處理狀態 */
    private String status;

    /** 合作類型 */
    private String type;

    /** 關鍵字：公司、姓名、Email、電話、需求 */
    private String keyword;

    /** 排序欄位 */
    private String sortBy;

    /** 排序方向 ASC / DESC */
    private String sortDir;
}