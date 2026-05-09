package com.group.admin.service;

import com.group.admin.entity.AdminEmergencyAnnouncement;
import com.group.admin.mapper.AdminEmergencyAnnouncementMapper;
import com.group.admin.req.emergencyannouncement.AdminEmergencyAnnouncementFilterCondition;
import com.group.admin.req.emergencyannouncement.CreateAdminEmergencyAnnouncementReq;
import com.group.admin.req.emergencyannouncement.UpdateAdminEmergencyAnnouncementReq;
import com.group.admin.req.emergencyannouncement.UpdateAdminEmergencyAnnouncementStatusReq;
import com.group.admin.res.emergencyannouncement.AdminEmergencyAnnouncementRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEmergencyAnnouncementService {

    private final AdminEmergencyAnnouncementMapper adminEmergencyAnnouncementMapper;

    /**
     * 新增緊急公告
     */
    public AdminEmergencyAnnouncementRes createAnnouncement(
            CreateAdminEmergencyAnnouncementReq req,
            String adminUserId
    ) {
        validateCreate(req);

        Date now = new Date();

        AdminEmergencyAnnouncement entity = new AdminEmergencyAnnouncement();
        entity.setId(UUID.randomUUID().toString());
        entity.setTitle(req.getTitle());
        entity.setContent(req.getContent());
        entity.setAnnouncementType(req.getAnnouncementType());
        entity.setStatus(req.getStatus());
        entity.setDisplayStartTime(req.getDisplayStartTime());
        entity.setDisplayEndTime(req.getDisplayEndTime());
        entity.setMaintenanceStartTime(req.getMaintenanceStartTime());
        entity.setMaintenanceEndTime(req.getMaintenanceEndTime());
        entity.setForceShow(Boolean.TRUE.equals(req.getForceShow()));
        entity.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        entity.setCreatedBy(adminUserId);
        entity.setCreatedAt(now);
        entity.setUpdatedBy(adminUserId);
        entity.setUpdatedAt(now);

        adminEmergencyAnnouncementMapper.insertSelective(entity);

        return getAnnouncement(entity.getId());
    }

    /**
     * 查詢緊急公告列表
     */
    public Map<String, Object> listAnnouncements(
            AdminEmergencyAnnouncementFilterCondition filters,
            int page,
            int size
    ) {
        List<AdminEmergencyAnnouncement> allList =
                adminEmergencyAnnouncementMapper.selectAll();

        List<AdminEmergencyAnnouncement> filteredList = allList.stream()
                .filter(item -> matchStatus(item, filters.getStatus()))
                .filter(item -> matchAnnouncementType(item, filters.getAnnouncementType()))
                .filter(item -> matchKeyword(item, filters.getKeyword()))
                .collect(Collectors.toList());

        sortList(filteredList, filters.getSortBy(), filters.getSortDir());

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : size;

        int total = filteredList.size();
        int fromIndex = Math.min(safePage * safeSize, total);
        int toIndex = Math.min(fromIndex + safeSize, total);

        List<AdminEmergencyAnnouncementRes> content = filteredList
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
     * 查詢目前有效緊急公告
     */
    public Map<String, Object> listActiveAnnouncements() {
        List<AdminEmergencyAnnouncementRes> list =
                adminEmergencyAnnouncementMapper.selectActive(new Date())
                        .stream()
                        .map(this::toRes)
                        .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", list);
        result.put("list", list);
        result.put("totalElements", list.size());

        return result;
    }

    /**
     * 查詢單筆緊急公告
     */
    public AdminEmergencyAnnouncementRes getAnnouncement(String id) {
        if (!StringUtils.hasText(id)) {
            throw new RuntimeException("查無公告資料");
        }

        AdminEmergencyAnnouncement entity =
                adminEmergencyAnnouncementMapper.selectByPrimaryKey(id);

        if (entity == null) {
            throw new RuntimeException("查無公告資料");
        }

        return toRes(entity);
    }

    /**
     * 編輯緊急公告
     */
    public AdminEmergencyAnnouncementRes updateAnnouncement(
            String id,
            UpdateAdminEmergencyAnnouncementReq req,
            String adminUserId
    ) {
        if (!StringUtils.hasText(id)) {
            throw new RuntimeException("查無公告資料");
        }

        validateUpdate(req);

        AdminEmergencyAnnouncement old =
                adminEmergencyAnnouncementMapper.selectByPrimaryKey(id);

        if (old == null) {
            throw new RuntimeException("查無公告資料");
        }

        AdminEmergencyAnnouncement entity = new AdminEmergencyAnnouncement();
        entity.setId(id);
        entity.setTitle(req.getTitle());
        entity.setContent(req.getContent());
        entity.setAnnouncementType(req.getAnnouncementType());
        entity.setStatus(req.getStatus());
        entity.setDisplayStartTime(req.getDisplayStartTime());
        entity.setDisplayEndTime(req.getDisplayEndTime());
        entity.setMaintenanceStartTime(req.getMaintenanceStartTime());
        entity.setMaintenanceEndTime(req.getMaintenanceEndTime());
        entity.setForceShow(Boolean.TRUE.equals(req.getForceShow()));
        entity.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        entity.setUpdatedBy(adminUserId);
        entity.setUpdatedAt(new Date());

        adminEmergencyAnnouncementMapper.updateByPrimaryKeySelective(entity);

        return getAnnouncement(id);
    }

    /**
     * 更新狀態
     */
    public AdminEmergencyAnnouncementRes updateStatus(
            String id,
            UpdateAdminEmergencyAnnouncementStatusReq req,
            String adminUserId
    ) {
        if (!StringUtils.hasText(id)) {
            throw new RuntimeException("查無公告資料");
        }

        if (req == null || !StringUtils.hasText(req.getStatus())) {
            throw new RuntimeException("請選擇公告狀態");
        }

        validateStatus(req.getStatus());

        AdminEmergencyAnnouncement old =
                adminEmergencyAnnouncementMapper.selectByPrimaryKey(id);

        if (old == null) {
            throw new RuntimeException("查無公告資料");
        }

        AdminEmergencyAnnouncement entity = new AdminEmergencyAnnouncement();
        entity.setId(id);
        entity.setStatus(req.getStatus());
        entity.setUpdatedBy(adminUserId);
        entity.setUpdatedAt(new Date());

        adminEmergencyAnnouncementMapper.updateByPrimaryKeySelective(entity);

        return getAnnouncement(id);
    }

    /**
     * 刪除緊急公告
     */
    public void deleteAnnouncement(String id, String adminUserId) {
        if (!StringUtils.hasText(id)) {
            throw new RuntimeException("查無公告資料");
        }

        AdminEmergencyAnnouncement old =
                adminEmergencyAnnouncementMapper.selectByPrimaryKey(id);

        if (old == null) {
            throw new RuntimeException("查無公告資料");
        }

        adminEmergencyAnnouncementMapper.deleteByPrimaryKey(id);
    }

    /**
     * 舊版方法保留：查全部
     */
    public List<AdminEmergencyAnnouncementRes> queryAll() {
        return adminEmergencyAnnouncementMapper.selectAll()
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    /**
     * 舊版方法保留：查目前有效
     */
    public List<AdminEmergencyAnnouncementRes> queryActive() {
        return adminEmergencyAnnouncementMapper.selectActive(new Date())
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    /**
     * 舊版方法保留：查單筆
     */
    public AdminEmergencyAnnouncementRes detail(String id) {
        return getAnnouncement(id);
    }

    /**
     * 舊版方法保留：啟用
     */
    public void activate(String id) {
        UpdateAdminEmergencyAnnouncementStatusReq req =
                new UpdateAdminEmergencyAnnouncementStatusReq();
        req.setStatus("ACTIVE");
        updateStatus(id, req, "system");
    }

    /**
     * 舊版方法保留：停用
     */
    public void deactivate(String id) {
        UpdateAdminEmergencyAnnouncementStatusReq req =
                new UpdateAdminEmergencyAnnouncementStatusReq();
        req.setStatus("INACTIVE");
        updateStatus(id, req, "system");
    }

    /**
     * 舊版方法保留：刪除
     */
    public void delete(String id) {
        deleteAnnouncement(id, "system");
    }
    
    /**
     * 前台查詢目前有效緊急公告
     *
     * 條件：
     * 1. status = ACTIVE
     * 2. displayStartTime <= now
     * 3. displayEndTime >= now
     */
    public List<AdminEmergencyAnnouncementRes> getActiveAnnouncements() {
        return adminEmergencyAnnouncementMapper.selectActive(new Date())
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    private boolean matchStatus(AdminEmergencyAnnouncement item, String status) {
        if (!StringUtils.hasText(status)) {
            return true;
        }

        return status.equals(item.getStatus());
    }

    private boolean matchAnnouncementType(
            AdminEmergencyAnnouncement item,
            String announcementType
    ) {
        if (!StringUtils.hasText(announcementType)) {
            return true;
        }

        return announcementType.equals(item.getAnnouncementType());
    }

    private boolean matchKeyword(AdminEmergencyAnnouncement item, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }

        String lowerKeyword = keyword.trim().toLowerCase();

        String title = item.getTitle() == null ? "" : item.getTitle().toLowerCase();
        String content = item.getContent() == null ? "" : item.getContent().toLowerCase();

        return title.contains(lowerKeyword) || content.contains(lowerKeyword);
    }

    private void sortList(
            List<AdminEmergencyAnnouncement> list,
            String sortBy,
            String sortDir
    ) {
        String safeSortBy = StringUtils.hasText(sortBy) ? sortBy : "updatedAt";
        String safeSortDir = StringUtils.hasText(sortDir) ? sortDir : "DESC";

        Comparator<AdminEmergencyAnnouncement> comparator;

        switch (safeSortBy) {
            case "title":
                comparator = Comparator.comparing(
                        item -> item.getTitle() == null ? "" : item.getTitle()
                );
                break;

            case "announcementType":
                comparator = Comparator.comparing(
                        item -> item.getAnnouncementType() == null ? "" : item.getAnnouncementType()
                );
                break;

            case "status":
                comparator = Comparator.comparing(
                        item -> item.getStatus() == null ? "" : item.getStatus()
                );
                break;

            case "displayStartTime":
                comparator = Comparator.comparing(
                        AdminEmergencyAnnouncement::getDisplayStartTime,
                        Comparator.nullsLast(Date::compareTo)
                );
                break;

            case "displayEndTime":
                comparator = Comparator.comparing(
                        AdminEmergencyAnnouncement::getDisplayEndTime,
                        Comparator.nullsLast(Date::compareTo)
                );
                break;

            case "createdAt":
                comparator = Comparator.comparing(
                        AdminEmergencyAnnouncement::getCreatedAt,
                        Comparator.nullsLast(Date::compareTo)
                );
                break;

            case "updatedAt":
            default:
                comparator = Comparator.comparing(
                        AdminEmergencyAnnouncement::getUpdatedAt,
                        Comparator.nullsLast(Date::compareTo)
                );
                break;
        }

        if ("DESC".equalsIgnoreCase(safeSortDir)) {
            comparator = comparator.reversed();
        }

        list.sort(comparator);
    }

    private void validateCreate(CreateAdminEmergencyAnnouncementReq req) {
        if (req == null) {
            throw new RuntimeException("請輸入公告資料");
        }

        validateCommon(
                req.getTitle(),
                req.getContent(),
                req.getAnnouncementType(),
                req.getStatus(),
                req.getDisplayStartTime(),
                req.getDisplayEndTime(),
                req.getMaintenanceStartTime(),
                req.getMaintenanceEndTime()
        );
    }

    private void validateUpdate(UpdateAdminEmergencyAnnouncementReq req) {
        if (req == null) {
            throw new RuntimeException("請輸入公告資料");
        }

        validateCommon(
                req.getTitle(),
                req.getContent(),
                req.getAnnouncementType(),
                req.getStatus(),
                req.getDisplayStartTime(),
                req.getDisplayEndTime(),
                req.getMaintenanceStartTime(),
                req.getMaintenanceEndTime()
        );
    }

    private void validateCommon(
            String title,
            String content,
            String announcementType,
            String status,
            Date displayStartTime,
            Date displayEndTime,
            Date maintenanceStartTime,
            Date maintenanceEndTime
    ) {
        if (!StringUtils.hasText(title)) {
            throw new RuntimeException("請輸入公告標題");
        }

        if (!StringUtils.hasText(content)) {
            throw new RuntimeException("請輸入公告內容");
        }

        validateAnnouncementType(announcementType);
        validateStatus(status);

        if (displayStartTime == null) {
            throw new RuntimeException("請選擇公告顯示開始時間");
        }

        if (displayEndTime == null) {
            throw new RuntimeException("請選擇公告顯示結束時間");
        }

        if (displayEndTime.before(displayStartTime)) {
            throw new RuntimeException("公告顯示結束時間不可早於開始時間");
        }

        if (
                maintenanceStartTime != null &&
                        maintenanceEndTime != null &&
                        maintenanceEndTime.before(maintenanceStartTime)
        ) {
            throw new RuntimeException("維修/更新結束時間不可早於開始時間");
        }
    }

    private void validateAnnouncementType(String announcementType) {
        if (!StringUtils.hasText(announcementType)) {
            throw new RuntimeException("請選擇公告類型");
        }

        List<String> validTypes = new ArrayList<>();
        validTypes.add("MAINTENANCE");
        validTypes.add("UPDATE");
        validTypes.add("NOTICE");

        if (!validTypes.contains(announcementType)) {
            throw new RuntimeException("公告類型不合法");
        }
    }

    private void validateStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new RuntimeException("請選擇公告狀態");
        }

        List<String> validStatuses = new ArrayList<>();
        validStatuses.add("DRAFT");
        validStatuses.add("ACTIVE");
        validStatuses.add("INACTIVE");

        if (!validStatuses.contains(status)) {
            throw new RuntimeException("公告狀態不合法");
        }
    }

    private AdminEmergencyAnnouncementRes toRes(AdminEmergencyAnnouncement entity) {
        return AdminEmergencyAnnouncementRes.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .announcementType(entity.getAnnouncementType())
                .status(entity.getStatus())
                .displayStartTime(entity.getDisplayStartTime())
                .displayEndTime(entity.getDisplayEndTime())
                .maintenanceStartTime(entity.getMaintenanceStartTime())
                .maintenanceEndTime(entity.getMaintenanceEndTime())
                .forceShow(entity.getForceShow())
                .sortOrder(entity.getSortOrder())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}