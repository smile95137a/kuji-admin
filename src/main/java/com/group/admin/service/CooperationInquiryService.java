package com.group.admin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.CooperationInquiry;
import com.group.admin.entity.CooperationInquiryStatusLog;
import com.group.admin.entity.Role;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.CooperationInquiryMapper;
import com.group.admin.mapper.CooperationInquiryStatusLogMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.req.cooperation.CooperationInquiryConvertVendorReq;
import com.group.admin.req.cooperation.CooperationInquiryFilterCondition;
import com.group.admin.req.cooperation.CreateCooperationInquiryReq;
import com.group.admin.req.cooperation.UpdateCooperationInquiryStatusReq;
import com.group.admin.res.cooperation.CooperationInquiryRes;
import com.group.admin.res.cooperation.CooperationInquiryStatusLogRes;

import lombok.RequiredArgsConstructor;

/**
 * 合作洽談 Service
 */
@Service
@RequiredArgsConstructor
public class CooperationInquiryService {

    /**
     * 廠商帳號對應角色
     *
     * 目前使用既有店家負責人角色。
     */
    private static final String VENDOR_ROLE_CODE = "ROLE_STORE_OWNER";

    /**
     * 廠商帳號預設密碼 fallback
     *
     * 原則上會優先使用合作洽談聯絡電話作為初始密碼。
     * 若前端未傳密碼，且合作洽談也沒有電話，才會使用此預設值。
     */
    private static final String DEFAULT_VENDOR_PASSWORD = "123456";

    private final CooperationInquiryMapper cooperationInquiryMapper;
    private final CooperationInquiryStatusLogMapper cooperationInquiryStatusLogMapper;
    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 前台送出合作洽談表單
     *
     * @param req 合作洽談建立資料
     * @return 合作洽談資料
     */
    public CooperationInquiryRes createInquiry(CreateCooperationInquiryReq req) {
        validateCreate(req);

        LocalDateTime now = LocalDateTime.now();

        CooperationInquiry entity = new CooperationInquiry();
        entity.setId(UUID.randomUUID().toString());
        entity.setCompany(trimToNull(req.getCompany()));
        entity.setName(req.getName().trim());
        entity.setEmail(req.getEmail().trim());
        entity.setPhone(trimToNull(req.getPhone()));
        entity.setType(req.getType().trim());
        entity.setMessage(req.getMessage().trim());
        entity.setStatus("PENDING");
        entity.setConvertedToVendor(false);
        entity.setVendorAdminUserId(null);
        entity.setDeleted(false);
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        cooperationInquiryMapper.insertSelective(entity);

        return getInquiry(entity.getId());
    }

    /**
     * 後台查詢合作洽談列表
     *
     * 注意：列表不帶 statusLogs，避免每筆資料都查歷程造成效能問題。
     *
     * @param filters 查詢條件
     * @param page 頁碼，從 0 開始
     * @param size 每頁筆數
     * @return 分頁查詢結果
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
                .map(item -> toRes(item, false))
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
     * 後台查詢單筆合作洽談資料
     *
     * 注意：明細會帶 statusLogs，前端可直接用 detail.statusLogs 顯示處理歷程。
     *
     * @param id 合作洽談 ID
     * @return 合作洽談資料
     */
    public CooperationInquiryRes getInquiry(String id) {
        if (!StringUtils.hasText(id)) {
            throw new RuntimeException("查無合作洽談資料");
        }

        CooperationInquiry entity = cooperationInquiryMapper.selectByPrimaryKey(id);

        if (entity == null) {
            throw new RuntimeException("查無合作洽談資料");
        }

        return toRes(entity, true);
    }

    /**
     * 後台查詢合作洽談狀態異動歷程
     *
     * 若之後前端想獨立刷新歷程，也可以使用這個方法。
     *
     * @param inquiryId 合作洽談 ID
     * @return 狀態異動歷程
     */
    public List<CooperationInquiryStatusLogRes> getStatusLogs(String inquiryId) {
        if (!StringUtils.hasText(inquiryId)) {
            throw new RuntimeException("查無合作洽談資料");
        }

        CooperationInquiry inquiry = cooperationInquiryMapper.selectByPrimaryKey(inquiryId);

        if (inquiry == null) {
            throw new RuntimeException("查無合作洽談資料");
        }

        return cooperationInquiryStatusLogMapper.selectByInquiryId(inquiryId)
                .stream()
                .map(this::toStatusLogRes)
                .collect(Collectors.toList());
    }

    /**
     * 後台更新處理狀態
     *
     * 注意：
     * 每次按下儲存都會新增一筆狀態異動紀錄。
     * 即使狀態沒有改變，例如 DONE -> DONE，只要有修改備註也會留下紀錄。
     *
     * @param id 合作洽談 ID
     * @param req 狀態更新資料
     * @return 合作洽談資料
     */
    @Transactional
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

        if (Boolean.TRUE.equals(old.getDeleted())) {
            throw new RuntimeException("此合作洽談已刪除，無法更新狀態");
        }

        String beforeStatus = old.getStatus();
        String afterStatus = req.getStatus();
        String newRemark = trimToNull(req.getRemark());

        CooperationInquiry update = new CooperationInquiry();
        update.setId(id);
        update.setStatus(afterStatus);
        update.setRemark(newRemark);
        update.setUpdatedAt(LocalDateTime.now());

        cooperationInquiryMapper.updateByPrimaryKeySelective(update);

        /*
         * 重點：
         * 這裡不判斷 beforeStatus / afterStatus 是否相同。
         * 只要按儲存，就會新增一筆 log。
         * 所以只改備註也會留下紀錄。
         */
        insertStatusLog(
                id,
                beforeStatus,
                afterStatus,
                newRemark,
                null
        );

        return getInquiry(id);
    }

    /**
     * 後台合作洽談轉成廠商 AdminUser
     *
     * @param req 轉廠商資料
     * @return 建立完成的廠商帳號
     */
    @Transactional
    public AdminUser convertToVendor(CooperationInquiryConvertVendorReq req) {
        if (req == null || !StringUtils.hasText(req.getId())) {
            throw new RuntimeException("查無合作洽談資料");
        }

        CooperationInquiry inquiry = cooperationInquiryMapper.selectByPrimaryKey(req.getId());

        if (inquiry == null) {
            throw new RuntimeException("查無合作洽談資料");
        }

        if (Boolean.TRUE.equals(inquiry.getDeleted())) {
            throw new RuntimeException("此合作洽談已刪除，無法轉成廠商");
        }

        if (Boolean.TRUE.equals(inquiry.getConvertedToVendor())) {
            throw new RuntimeException("此合作洽談已轉成廠商");
        }

        if (StringUtils.hasText(inquiry.getVendorAdminUserId())) {
            throw new RuntimeException("此合作洽談已存在廠商帳號");
        }

        if (!StringUtils.hasText(inquiry.getEmail())) {
            throw new RuntimeException("合作洽談沒有 Email，無法建立廠商帳號");
        }

        AdminUser existsEmailUser = adminUserMapper.selectByEmail(inquiry.getEmail());
        if (existsEmailUser != null) {
            throw new RuntimeException("此 Email 已存在後台帳號，無法重複建立廠商");
        }

        Role vendorRole = roleMapper.selectByCode(VENDOR_ROLE_CODE);
        if (vendorRole == null) {
            throw new RuntimeException("查無店家負責人角色 ROLE_STORE_OWNER，請先建立角色資料");
        }

        LocalDateTime now = LocalDateTime.now();

        String username = buildVendorUsername(inquiry);

        AdminUser existsUsernameUser = adminUserMapper.selectByUsername(username);
        if (existsUsernameUser != null) {
            username = username + "_" + System.currentTimeMillis();
        }

        /*
         * 廠商初始密碼規則：
         * 1. 如果前端有傳 password，優先使用前端傳入值。
         * 2. 如果前端未傳 password，使用合作洽談聯絡電話。
         * 3. 如果電話也沒有，才使用 fallback 預設密碼 123456。
         */
        String rawPassword = trimToNull(req.getPassword());
        if (!StringUtils.hasText(rawPassword)) {
            rawPassword = trimToNull(inquiry.getPhone());
        }
        if (!StringUtils.hasText(rawPassword)) {
            rawPassword = DEFAULT_VENDOR_PASSWORD;
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setId(UUID.randomUUID().toString());
        adminUser.setUsername(username);
        adminUser.setPassword(passwordEncoder.encode(rawPassword));
        adminUser.setEmail(inquiry.getEmail());
        adminUser.setDisplayName(buildVendorDisplayName(inquiry));
        adminUser.setPhone(inquiry.getPhone());
        adminUser.setStatus("ACTIVE");
        adminUser.setForceChangePassword(true);
        adminUser.setFailedLoginAttempts(0);
        adminUser.setLockedUntil(null);
        adminUser.setCreatedBy(null);
        adminUser.setCreatedAt(now);
        adminUser.setUpdatedBy(null);
        adminUser.setUpdatedAt(now);
        adminUser.setRemark(buildVendorRemark(inquiry, req.getRemark()));

        adminUserMapper.insert(adminUser);

        AdminUserRole adminUserRole = new AdminUserRole();
        adminUserRole.setId(UUID.randomUUID().toString());
        adminUserRole.setAdminUserId(adminUser.getId());
        adminUserRole.setRoleId(vendorRole.getId());
        adminUserRole.setCreatedAt(now);

        adminUserRoleMapper.insert(adminUserRole);

        CooperationInquiry update = new CooperationInquiry();
        update.setId(inquiry.getId());
        update.setStatus("DONE");
        update.setRemark(trimToNull(req.getRemark()));
        update.setConvertedToVendor(true);
        update.setVendorAdminUserId(adminUser.getId());
        update.setUpdatedAt(now);

        cooperationInquiryMapper.updateByPrimaryKeySelective(update);

        insertStatusLog(
                inquiry.getId(),
                inquiry.getStatus(),
                "DONE",
                "轉成廠商帳號：" + adminUser.getUsername() + appendRemark(req.getRemark()),
                null
        );

        return adminUser;
    }

    /**
     * 後台刪除合作洽談
     *
     * 注意：這裡是軟刪除，不是真的刪除資料。
     * 會標記 deleted = true，並將狀態改成 CLOSED。
     *
     * @param id 合作洽談 ID
     */
    @Transactional
    public void deleteInquiry(String id) {
        if (!StringUtils.hasText(id)) {
            throw new RuntimeException("查無合作洽談資料");
        }

        CooperationInquiry old = cooperationInquiryMapper.selectByPrimaryKey(id);

        if (old == null) {
            throw new RuntimeException("查無合作洽談資料");
        }

        if (Boolean.TRUE.equals(old.getDeleted())) {
            throw new RuntimeException("此合作洽談已刪除");
        }

        LocalDateTime now = LocalDateTime.now();

        cooperationInquiryMapper.softDeleteByPrimaryKey(
                id,
                null,
                now
        );

        insertStatusLog(
                id,
                old.getStatus(),
                "CLOSED",
                "刪除合作洽談",
                null
        );
    }

    /**
     * 新增狀態異動紀錄
     *
     * @param inquiryId 合作洽談 ID
     * @param beforeStatus 異動前狀態
     * @param afterStatus 異動後狀態
     * @param remark 異動備註
     * @param operator 操作者
     */
    private void insertStatusLog(
            String inquiryId,
            String beforeStatus,
            String afterStatus,
            String remark,
            AdminUser operator
    ) {
        CooperationInquiryStatusLog log = new CooperationInquiryStatusLog();
        log.setId(UUID.randomUUID().toString());
        log.setInquiryId(inquiryId);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setRemark(trimToNull(remark));

        if (operator != null) {
            log.setOperatorId(operator.getId());
            log.setOperatorUsername(operator.getUsername());
            log.setOperatorDisplayName(operator.getDisplayName());
        }

        log.setCreatedAt(LocalDateTime.now());

        cooperationInquiryStatusLogMapper.insertSelective(log);
    }

    /**
     * 驗證合作洽談新增資料
     *
     * @param req 合作洽談新增資料
     */
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

    /**
     * 驗證合作類型
     *
     * @param type 合作類型
     */
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

    /**
     * 驗證處理狀態
     *
     * @param status 處理狀態
     */
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

    /**
     * 狀態篩選
     *
     * @param item 合作洽談資料
     * @param status 狀態
     * @return 是否符合
     */
    private boolean matchStatus(CooperationInquiry item, String status) {
        if (!StringUtils.hasText(status)) {
            return true;
        }

        return status.equals(item.getStatus());
    }

    /**
     * 合作類型篩選
     *
     * @param item 合作洽談資料
     * @param type 合作類型
     * @return 是否符合
     */
    private boolean matchType(CooperationInquiry item, String type) {
        if (!StringUtils.hasText(type)) {
            return true;
        }

        return type.equals(item.getType());
    }

    /**
     * 關鍵字篩選
     *
     * @param item 合作洽談資料
     * @param keyword 關鍵字
     * @return 是否符合
     */
    private boolean matchKeyword(CooperationInquiry item, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }

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

    /**
     * 合作洽談列表排序
     *
     * @param list 合作洽談列表
     * @param sortBy 排序欄位
     * @param sortDir 排序方向
     */
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
                        Comparator.nullsLast(LocalDateTime::compareTo)
                );
                break;
            case "createdAt":
            default:
                comparator = Comparator.comparing(
                        CooperationInquiry::getCreatedAt,
                        Comparator.nullsLast(LocalDateTime::compareTo)
                );
                break;
        }

        if ("DESC".equalsIgnoreCase(safeSortDir)) {
            comparator = comparator.reversed();
        }

        list.sort(comparator);
    }

    /**
     * 字串 trim，空字串轉 null
     *
     * @param value 原始字串
     * @return trim 後字串，若為空則回傳 null
     */
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    /**
     * 建立廠商帳號 username
     *
     * @param inquiry 合作洽談資料
     * @return 廠商帳號
     */
    private String buildVendorUsername(CooperationInquiry inquiry) {
        if (StringUtils.hasText(inquiry.getEmail()) && inquiry.getEmail().contains("@")) {
            return inquiry.getEmail()
                    .substring(0, inquiry.getEmail().indexOf("@"))
                    .trim();
        }

        return "vendor_" + System.currentTimeMillis();
    }

    /**
     * 建立廠商帳號顯示名稱
     *
     * @param inquiry 合作洽談資料
     * @return 顯示名稱
     */
    private String buildVendorDisplayName(CooperationInquiry inquiry) {
        if (StringUtils.hasText(inquiry.getCompany())) {
            return inquiry.getCompany();
        }

        if (StringUtils.hasText(inquiry.getName())) {
            return inquiry.getName();
        }

        return "廠商";
    }

    /**
     * 建立廠商帳號備註
     *
     * @param inquiry 合作洽談資料
     * @param remark 備註
     * @return 廠商帳號備註
     */
    private String buildVendorRemark(CooperationInquiry inquiry, String remark) {
        StringBuilder sb = new StringBuilder();
        sb.append("由合作洽談轉入");

        if (StringUtils.hasText(inquiry.getCompany())) {
            sb.append("，公司：").append(inquiry.getCompany());
        }

        if (StringUtils.hasText(inquiry.getName())) {
            sb.append("，聯絡人：").append(inquiry.getName());
        }

        if (StringUtils.hasText(inquiry.getType())) {
            sb.append("，合作類型：").append(inquiry.getType());
        }

        if (StringUtils.hasText(remark)) {
            sb.append("，備註：").append(remark.trim());
        }

        return sb.toString();
    }

    /**
     * 補上備註文字
     *
     * @param remark 備註
     * @return 備註文字
     */
    private String appendRemark(String remark) {
        if (!StringUtils.hasText(remark)) {
            return "";
        }

        return "，備註：" + remark.trim();
    }

    /**
     * 合作洽談 Entity 轉 Response
     *
     * @param entity 合作洽談 Entity
     * @param includeStatusLogs 是否包含狀態異動歷程
     * @return 合作洽談 Response
     */
    private CooperationInquiryRes toRes(CooperationInquiry entity, boolean includeStatusLogs) {
        List<CooperationInquiryStatusLogRes> statusLogs = new ArrayList<>();

        if (includeStatusLogs) {
            statusLogs = cooperationInquiryStatusLogMapper.selectByInquiryId(entity.getId())
                    .stream()
                    .map(this::toStatusLogRes)
                    .collect(Collectors.toList());
        }

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
                .convertedToVendor(entity.getConvertedToVendor())
                .vendorAdminUserId(entity.getVendorAdminUserId())
                .deleted(entity.getDeleted())
                .deletedAt(entity.getDeletedAt())
                .deletedBy(entity.getDeletedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .statusLogs(statusLogs)
                .build();
    }

    /**
     * 狀態異動紀錄 Entity 轉 Response
     *
     * @param entity 狀態異動紀錄 Entity
     * @return 狀態異動紀錄 Response
     */
    private CooperationInquiryStatusLogRes toStatusLogRes(CooperationInquiryStatusLog entity) {
        return CooperationInquiryStatusLogRes.builder()
                .id(entity.getId())
                .inquiryId(entity.getInquiryId())
                .beforeStatus(entity.getBeforeStatus())
                .afterStatus(entity.getAfterStatus())
                .remark(entity.getRemark())
                .operatorId(entity.getOperatorId())
                .operatorUsername(entity.getOperatorUsername())
                .operatorDisplayName(entity.getOperatorDisplayName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}