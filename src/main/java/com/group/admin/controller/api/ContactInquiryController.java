package com.group.admin.controller.api;

import com.group.admin.req.contact.ContactInquiryCreateReq;
import com.group.admin.res.contact.ContactInquiryRes;
import com.group.admin.service.ContactInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 前台合作諮詢 Controller
 * 
 * <p>提供前台廠商合作諮詢表單提交功能（公開，無需登入）</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/contact-inquiry")
@RequiredArgsConstructor
@Tag(name = "前台-合作諮詢", description = "廠商合作諮詢表單提交（無需登入）")
public class ContactInquiryController {

    private final ContactInquiryService contactInquiryService;

    /**
     * 提交合作諮詢
     * 
     * <p>廠商透過前台表單提交合作諮詢，無需登入</p>
     */
    @PostMapping
    @Operation(summary = "提交合作諮詢", description = "廠商透過表單提交合作諮詢（公開 API，無需登入）")
    public ResponseEntity<ContactInquiryRes> submitInquiry(
            @Valid @RequestBody ContactInquiryCreateReq req) {
        
        log.info("📩 [前台] 收到合作諮詢: 公司={}, 聯絡人={}", req.getCompanyName(), req.getContactName());
        ContactInquiryRes result = contactInquiryService.submitInquiry(req);
        return ResponseEntity.ok(result);
    }
}
