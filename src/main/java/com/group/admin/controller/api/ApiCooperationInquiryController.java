package com.group.admin.controller.api;

import com.group.admin.req.cooperation.CreateCooperationInquiryReq;
import com.group.admin.res.cooperation.CooperationInquiryRes;
import com.group.admin.service.CooperationInquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public Cooperation Inquiry API.
 * No authentication required.
 */
@Slf4j
@RestController
@RequestMapping({"/cooperation-inquiry", "/cooperation-inquiries"})
@RequiredArgsConstructor
public class ApiCooperationInquiryController {

    private final CooperationInquiryService cooperationInquiryService;

    /**
     * POST /api/cooperation-inquiry
     * POST /api/cooperation-inquiries
     */
    @PostMapping({"", "/submit"})
    public ResponseEntity<CooperationInquiryRes> submitInquiry(
            @Valid @RequestBody CreateCooperationInquiryReq req
    ) {
        log.info("前台送出合作洽談表單: company={}, name={}, type={}",
                req.getCompany(),
                req.getName(),
                req.getType()
        );

        return ResponseEntity.status(201).body(
                cooperationInquiryService.createInquiry(req)
        );
    }
}