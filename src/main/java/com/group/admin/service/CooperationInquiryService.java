package com.group.admin.service;

import com.group.admin.entity.CooperationInquiry;
import com.group.admin.mapper.CooperationInquiryMapper;
import com.group.admin.req.cooperation.CooperationInquiryFilterCondition;
import com.group.admin.req.cooperation.CreateCooperationInquiryReq;
import com.group.admin.req.cooperation.UpdateCooperationInquiryStatusReq;
import com.group.admin.res.cooperation.CooperationInquiryRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CooperationInquiryService {

    private final CooperationInquiryMapper cooperationInquiryMapper;

    /**
     * 前台送出合作洽談表單
     */
    public CooperationInquiryRes createInquiry(CreateCooperationInquiryReq req) {
        validateCreate(req);

        Date now = new Date();

        CooperationInquiry entity = new CooperationInquiry();
        entity.setId(UUID.randomUUID().toString());
        entity.setCompany(trimToNull(req.getCompany()));
        entity.setName(req.getName().trim());
        entity.setEmail(req.getEmail().trim());
        entity.setPhone(trimToNull(req.getPhone()));
        entity.setType(req.getType().trim());
        entity.setMessage(req.getMessage().trim());
        entity.setStatus("PENDING");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        cooperationInquiryMapper.insertSelective(entity);

        return getInquiry(entity.getId());
    }

    /**
     * 後台查詢合作洽談列表
     */
    public Map<String, Object> listInquiries(
            CooperationInquiryFilterCondition filters,
            int page,
            int size
    ) {
        CooperationInquiryFilterCondition safeFilters =
                filters == null ? new CooperationInquiryFilterCondition() : filters;

        List<CooperationInquiry> allList = cooperationInquiryMapper.selectAll();

        List<CooperationInquiry> filteredList = allList.stream()
                .filter(item -> matchStatus(item, safeFilters.getStatus()))
                .filter(item -> matchType(item, safeFilters.getType()))
                .filter(item -> matchKeyword(item, safeFilters.getKeyword()))
                .collect(Collectors.toList());

        sortList(filteredList, safeFilters.getSortBy(), safeFilters.getSortDir());

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : size;

        int total = filteredList.size();
        int fromIndex = Math.min(safePage * safeSize, total);
        int toIndex = Math.min(fromIndex + safeSize, total);

        List<CooperationInquiryRes> content = filteredList
                .subList(fromIndex, toIndex)
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("list", content);
        result.put("page", safePage);
        result.put("size", safeSize);
        result.put("totalElements", total);
        result.put("totalPages", safeSize == 0 ? 0 : (int) Math.ceil((double) total / safeSize));

        return result;
    }

    /**
     * 後台查詢單筆
     */
    public CooperationInquiryRes getInquiry(String id) {
        if (!StringUtils.hasText(id)) {
            throw new RuntimeException("查無合作洽談資料");
        }

        CooperationInquiry entity = cooperationInquiryMapper.selectByPrimaryKey(id);

        if (entity == null) {
            throw new RuntimeException("查無合作洽談資料");
        }

        return toRes(entity);
    }

    /**
     * 後台更新處理狀態
     */
    public CooperationInquiryRes updateStatus(
            String id,
            UpdateCooperationInquiryStatusReq req
    ) {
        if (!StringUtils.hasText(id)) {
            throw new RuntimeException("查無合作洽談資料");
        }

        if (req == null || !StringUtils.hasText(req.getStatus())) {
            throw new RuntimeException("請選擇處理狀態");
        }

        validateStatus(req.getStatus());

        CooperationInquiry old = cooperationInquiryMapper.selectByPrimaryKey(id);

        if (old == null) {
            throw new RuntimeException("查無合作洽談資料");
        }

        CooperationInquiry update = new CooperationInquiry();
        update.setId(id);
        update.setStatus(req.getStatus());
        update.setRemark(trimToNull(req.getRemark()));
        update.setUpdatedAt(new Date());

        cooperationInquiryMapper.updateByPrimaryKeySelective(update);

        return getInquiry(id);
    }

    /**
     * 後台刪除
     */
    public void deleteInquiry(String id) {
        if (!StringUtils.hasText(id)) {
            throw new RuntimeException("查無合作洽談資料");
        }

        CooperationInquiry old = cooperationInquiryMapper.selectByPrimaryKey(id);

        if (old == null) {
            throw new RuntimeException("查無合作洽談資料");
        }

        cooperationInquiryMapper.deleteByPrimaryKey(id);
    }

    private void validateCreate(CreateCooperationInquiryReq req) {
        if (req == null) {
            throw new RuntimeException("請輸入合作洽談資料");
        }

        if (!StringUtils.hasText(req.getName())) {
            throw new RuntimeException("請輸入聯絡人姓名");
        }

        if (!StringUtils.hasText(req.getEmail())) {
            throw new RuntimeException("請輸入電子郵件");
        }

        if (!StringUtils.hasText(req.getType())) {
            throw new RuntimeException("請選擇合作類型");
        }

        if (!StringUtils.hasText(req.getMessage())) {
            throw new RuntimeException("請輸入需求簡述");
        }

        validateType(req.getType());
    }

    private void validateType(String type) {
        List<String> validTypes = new ArrayList<>();
        validTypes.add("IP");
        validTypes.add("SUPPLY");
        validTypes.add("CHANNEL");
        validTypes.add("MARKETING");

        if (!validTypes.contains(type)) {
            throw new RuntimeException("合作類型不合法");
        }
    }

    private void validateStatus(String status) {
        List<String> validStatuses = new ArrayList<>();
        validStatuses.add("PENDING");
        validStatuses.add("PROCESSING");
        validStatuses.add("DONE");
        validStatuses.add("CLOSED");

        if (!validStatuses.contains(status)) {
            throw new RuntimeException("處理狀態不合法");
        }
    }

    private boolean matchStatus(CooperationInquiry item, String status) {
        if (!StringUtils.hasText(status)) return true;
        return status.equals(item.getStatus());
    }

    private boolean matchType(CooperationInquiry item, String type) {
        if (!StringUtils.hasText(type)) return true;
        return type.equals(item.getType());
    }

    private boolean matchKeyword(CooperationInquiry item, String keyword) {
        if (!StringUtils.hasText(keyword)) return true;

        String lowerKeyword = keyword.trim().toLowerCase();

        String company = item.getCompany() == null ? "" : item.getCompany().toLowerCase();
        String name = item.getName() == null ? "" : item.getName().toLowerCase();
        String email = item.getEmail() == null ? "" : item.getEmail().toLowerCase();
        String phone = item.getPhone() == null ? "" : item.getPhone().toLowerCase();
        String message = item.getMessage() == null ? "" : item.getMessage().toLowerCase();

        return company.contains(lowerKeyword)
                || name.contains(lowerKeyword)
                || email.contains(lowerKeyword)
                || phone.contains(lowerKeyword)
                || message.contains(lowerKeyword);
    }

    private void sortList(List<CooperationInquiry> list, String sortBy, String sortDir) {
        String safeSortBy = StringUtils.hasText(sortBy) ? sortBy : "createdAt";
        String safeSortDir = StringUtils.hasText(sortDir) ? sortDir : "DESC";

        Comparator<CooperationInquiry> comparator;

        switch (safeSortBy) {
            case "company":
                comparator = Comparator.comparing(
                        item -> item.getCompany() == null ? "" : item.getCompany()
                );
                break;
            case "name":
                comparator = Comparator.comparing(
                        item -> item.getName() == null ? "" : item.getName()
                );
                break;
            case "email":
                comparator = Comparator.comparing(
                        item -> item.getEmail() == null ? "" : item.getEmail()
                );
                break;
            case "type":
                comparator = Comparator.comparing(
                        item -> item.getType() == null ? "" : item.getType()
                );
                break;
            case "status":
                comparator = Comparator.comparing(
                        item -> item.getStatus() == null ? "" : item.getStatus()
                );
                break;
            case "updatedAt":
                comparator = Comparator.comparing(
                        CooperationInquiry::getUpdatedAt,
                        Comparator.nullsLast(Date::compareTo)
                );
                break;
            case "createdAt":
            default:
                comparator = Comparator.comparing(
                        CooperationInquiry::getCreatedAt,
                        Comparator.nullsLast(Date::compareTo)
                );
                break;
        }

        if ("DESC".equalsIgnoreCase(safeSortDir)) {
            comparator = comparator.reversed();
        }

        list.sort(comparator);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) return null;
        return value.trim();
    }

    private CooperationInquiryRes toRes(CooperationInquiry entity) {
        return CooperationInquiryRes.builder()
                .id(entity.getId())
                .company(entity.getCompany())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .type(entity.getType())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}