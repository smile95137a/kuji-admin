package com.group.admin.service.impl;

import com.group.admin.entity.ContactInquiry;
import com.group.admin.mapper.ContactInquiryMapper;
import com.group.admin.repository.ContactInquiryRepository;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.contact.ContactInquiryCondition;
import com.group.admin.req.contact.ContactInquiryCreateReq;
import com.group.admin.res.contact.ContactInquiryRes;
import com.group.admin.service.ContactInquiryService;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 合作諮詢 Service 實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactInquiryServiceImpl implements ContactInquiryService {

    private final ContactInquiryMapper contactInquiryMapper;
    private final ContactInquiryRepository contactInquiryRepository;

    @Override
    @Transactional
    public ContactInquiryRes submitInquiry(ContactInquiryCreateReq req) {
        log.info("📩 新增合作諮詢: 公司={}, 聯絡人={}", req.getCompanyName(), req.getContactName());
        
        ContactInquiry inquiry = new ContactInquiry();
        inquiry.setId(UUID.randomUUID().toString());
        inquiry.setCompanyName(req.getCompanyName());
        inquiry.setContactName(req.getContactName());
        inquiry.setEmail(req.getEmail());
        inquiry.setPhone(req.getPhone());
        inquiry.setCooperationType(req.getCooperationType());
        inquiry.setDescription(req.getDescription());
        inquiry.setStatus("PENDING");
        inquiry.setCreatedAt(LocalDateTime.now());
        inquiry.setUpdatedAt(LocalDateTime.now());
        
        contactInquiryMapper.insert(inquiry);
        
        log.info("✅ 合作諮詢提交成功, ID={}", inquiry.getId());
        return convertToRes(inquiry);
    }

    @Override
    public List<ContactInquiryRes> queryInquiries(QueryReq<ContactInquiryCondition> req) {
        log.info("📋 查詢合作諮詢列表");
        
        // 查全部，後面在 Java 做篩選（因為用 annotation Repository）
        List<ContactInquiry> all = contactInquiryRepository.selectAll();
        
        ContactInquiryCondition condition = req != null ? req.getCondition() : null;
        
        // 動態篩選
        List<ContactInquiry> filtered = all.stream()
            .filter(inquiry -> {
                if (condition == null) return true;
                
                // 公司名稱模糊查詢
                if (isNotBlank(condition.getCompanyName()) 
                    && !inquiry.getCompanyName().contains(condition.getCompanyName())) {
                    return false;
                }
                
                // 狀態篩選
                if (isNotBlank(condition.getStatus()) 
                    && !condition.getStatus().equals(inquiry.getStatus())) {
                    return false;
                }
                
                // 合作類型篩選
                if (isNotBlank(condition.getCooperationType()) 
                    && !condition.getCooperationType().equals(inquiry.getCooperationType())) {
                    return false;
                }
                
                // 關鍵字搜尋（搜尋公司名稱與聯絡人）
                if (isNotBlank(condition.getKeyword())) {
                    String kw = condition.getKeyword().toLowerCase();
                    boolean match = (inquiry.getCompanyName() != null && inquiry.getCompanyName().toLowerCase().contains(kw))
                            || (inquiry.getContactName() != null && inquiry.getContactName().toLowerCase().contains(kw))
                            || (inquiry.getEmail() != null && inquiry.getEmail().toLowerCase().contains(kw));
                    if (!match) return false;
                }
                
                // 時間範圍篩選（BaseCondition 使用 LocalDate）
                if (condition.getCreatedAtStart() != null 
                    && inquiry.getCreatedAt() != null
                    && inquiry.getCreatedAt().toLocalDate().isBefore(condition.getCreatedAtStart())) {
                    return false;
                }
                if (condition.getCreatedAtEnd() != null 
                    && inquiry.getCreatedAt() != null
                    && inquiry.getCreatedAt().toLocalDate().isAfter(condition.getCreatedAtEnd())) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // 排序
        if (req != null && isNotBlank(req.getSortBy())) {
            // 預設按建立時間降序
        }
        
        log.info("✅ 查詢到 {} 筆合作諮詢", filtered.size());
        return filtered.stream().map(this::convertToRes).collect(Collectors.toList());
    }

    @Override
    public ContactInquiryRes getInquiryById(String id) {
        log.info("🔍 查詢合作諮詢詳情: id={}", id);
        
        ContactInquiry inquiry = contactInquiryMapper.selectByPrimaryKey(id);
        if (inquiry == null) {
            throw new RuntimeException("合作諮詢不存在");
        }
        
        return convertToRes(inquiry);
    }

    @Override
    @Transactional
    public ContactInquiryRes updateInquiryStatus(String id, String status, String remark) {
        log.info("✏️ 更新合作諮詢狀態: id={}, status={}", id, status);
        
        ContactInquiry inquiry = contactInquiryMapper.selectByPrimaryKey(id);
        if (inquiry == null) {
            throw new RuntimeException("合作諮詢不存在");
        }
        
        String currentUserId = SecurityUtils.getCurrentAdminUserId();
        
        inquiry.setStatus(status);
        inquiry.setRemark(remark);
        inquiry.setProcessedBy(currentUserId);
        inquiry.setProcessedAt(LocalDateTime.now());
        inquiry.setUpdatedAt(LocalDateTime.now());
        
        contactInquiryRepository.updateStatus(inquiry);
        
        log.info("✅ 合作諮詢狀態更新成功");
        return convertToRes(inquiry);
    }

    @Override
    @Transactional
    public void deleteInquiry(String id) {
        log.info("🗑️ 刪除合作諮詢: id={}", id);
        
        ContactInquiry inquiry = contactInquiryMapper.selectByPrimaryKey(id);
        if (inquiry == null) {
            throw new RuntimeException("合作諮詢不存在");
        }
        
        contactInquiryMapper.deleteByPrimaryKey(id);
        log.info("✅ 合作諮詢刪除成功");
    }

    private ContactInquiryRes convertToRes(ContactInquiry inquiry) {
        return ContactInquiryRes.builder()
                .id(inquiry.getId())
                .companyName(inquiry.getCompanyName())
                .contactName(inquiry.getContactName())
                .email(inquiry.getEmail())
                .phone(inquiry.getPhone())
                .cooperationType(inquiry.getCooperationType())
                .cooperationTypeName(getCooperationTypeName(inquiry.getCooperationType()))
                .description(inquiry.getDescription())
                .status(inquiry.getStatus())
                .statusName(getStatusName(inquiry.getStatus()))
                .remark(inquiry.getRemark())
                .processedBy(inquiry.getProcessedBy())
                .processedAt(inquiry.getProcessedAt())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .build();
    }

    private String getStatusName(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "PENDING": return "待處理";
            case "PROCESSING": return "處理中";
            case "COMPLETED": return "已完成";
            case "REJECTED": return "已拒絕";
            default: return status;
        }
    }

    private String getCooperationTypeName(String type) {
        if (type == null) return "其他";
        switch (type) {
            case "IP_LICENSE": return "IP 授權合作";
            case "DISTRIBUTION": return "經銷通路合作";
            case "CUSTOM_PRIZE": return "客製賞品合作";
            case "MARKETING": return "行銷推廣合作";
            case "OTHER": return "其他";
            default: return type;
        }
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
