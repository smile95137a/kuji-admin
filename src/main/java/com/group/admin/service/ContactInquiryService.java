package com.group.admin.service;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.contact.ContactInquiryCondition;
import com.group.admin.req.contact.ContactInquiryCreateReq;
import com.group.admin.res.contact.ContactInquiryRes;

import java.util.List;

/**
 * 合作諮詢 Service 介面
 */
public interface ContactInquiryService {
    
    /**
     * 提交合作諮詢（前台）
     */
    ContactInquiryRes submitInquiry(ContactInquiryCreateReq req);
    
    /**
     * 查詢合作諮詢列表（後台）
     */
    List<ContactInquiryRes> queryInquiries(QueryReq<ContactInquiryCondition> req);
    
    /**
     * 查詢單一合作諮詢詳情（後台）
     */
    ContactInquiryRes getInquiryById(String id);
    
    /**
     * 更新諮詢處理狀態（後台）
     */
    ContactInquiryRes updateInquiryStatus(String id, String status, String remark);
    
    /**
     * 刪除合作諮詢（後台）
     */
    void deleteInquiry(String id);
}
