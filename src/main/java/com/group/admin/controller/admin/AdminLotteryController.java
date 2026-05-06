package com.group.admin.controller.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.annotation.AuditLog;
import com.group.admin.entity.StoreUser;
import com.group.admin.enums.AuditLogType;
import com.group.admin.example.StoreUserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.req.lottery.LotteryCopyReq;
import com.group.admin.req.lottery.LotteryCreateReq;
import com.group.admin.req.lottery.LotteryStatusChangeReq;
import com.group.admin.req.lottery.LotteryUpdateReq;
import com.group.admin.req.lottery.LotteryWithPrizesCreateReq;
import com.group.admin.req.lottery.LotteryWithPrizesUpdateReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.res.lottery.LotteryWithPrizesRes;
import com.group.admin.service.LotteryService;
import com.group.admin.service.LotteryTicketService;
import com.group.admin.service.LotteryTicketService.DesignatedWinningNumber;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyDescriptor;
import java.util.List;

/**
 * 後台商品管理 API
 * 
 * 路由：/admin/lottery/**
 * 角色：ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR
 * 
 * 特色：
 * 1. 自動從 JWT Token 帶入 storeId
 * 2. 使用 Condition + QueryReq 模式查詢
 * 3. 返回全部資料，前端做分頁
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/admin/lottery")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "後台商品管理", description = "後台商品 CRUD API")
public class AdminLotteryController {

    private final LotteryService lotteryService;
    private final LotteryTicketService lotteryTicketService;
    private final StoreUserMapper storeUserMapper;
    private final ObjectMapper objectMapper;

    /**
     * 查詢商品列表（後台）
     * 
     * ✅ 自動帶入當前使用者的 StoreID（從資料庫查詢）
     * ✅ 前端做分頁，後端返回全部資料
     * ✅ 所有查詢條件都是可選的
     * 
     * @param req 查詢請求（可選）
     * @return 商品列表
     */
    @PostMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "查詢商品列表", description = "後台查詢商品，自動過濾當前店家")
    public ResponseEntity<PageResult<LotteryRes>> queryLotteries(
            @RequestBody(required = false) QueryReq<LotteryCondition> req) {
        
        String userId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        boolean isStoreOwner = SecurityUtils.isStoreOwner();
        boolean isStoreEditor = SecurityUtils.isStoreEditor();
        
        log.info("🔍 查詢商品列表: userId={}, isAdmin={}, isStoreOwner={}, isStoreEditor={}", 
                 userId, isAdmin, isStoreOwner, isStoreEditor);
        log.info("🔍 請求條件: {}", req);
        
        // 非 Admin 需要過濾店家
        if (!isAdmin) {
            // 從資料庫查詢使用者的店家列表
            StoreUserExample storeUserExample = new StoreUserExample();
            storeUserExample.createCriteria().andAdminUserIdEqualTo(userId);
            List<StoreUser> storeUsers = storeUserMapper.selectByExample(storeUserExample);
            
            if (!storeUsers.isEmpty()) {
                String storeId = storeUsers.get(0).getStoreId();
                
                // 自動設定 storeId
                if (req == null) {
                    req = new QueryReq<>();
                }
                if (req.getCondition() == null) {
                    req.setCondition(new LotteryCondition());
                }
                req.getCondition().setStoreId(storeId);
                
                log.info("🔒 過濾店家: storeId={}", storeId);
            } else {
                log.warn("⚠️ 使用者沒有關聯任何店家: userId={}", userId);
            }
        } else {
            log.info("👑 Admin 可查看所有店家的商品");
        }
        
        PageResult<LotteryRes> result = lotteryService.queryLotteries(req);
        
        log.info("✅ 查詢成功: 共 {} 筆", result.getTotal());
        return ResponseEntity.ok(result);
    }

    /**
     * 新增商品
     * 
     * ✅ 自動帶入當前使用者的 StoreID（從資料庫查詢）
     * ✅ Admin 可以選擇任何店家，StoreOwner 自動使用第一個店家
     * 
     * @param req 商品建立請求
     * @return 建立的商品
     */
    @AuditLog(type = AuditLogType.ADMIN_ACTION, action = "CREATE", targetType = "LOTTERY")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "新增商品", description = "自動帶入店家 ID")
    public ResponseEntity<LotteryRes> createLottery(
            @Valid @RequestBody LotteryCreateReq req) {
        
        String userId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        
        log.info("➕ 新增商品: userId={}, isAdmin={}, title={}, 前端傳入 storeId={}", 
                 userId, isAdmin, req.getTitle(), req.getStoreId());
        
        // 處理 storeId
        if (req.getStoreId() == null || req.getStoreId().isBlank()) {
            // 前端沒有傳 storeId，後端自動帶入
            if (isAdmin) {
                // Admin 必須明確指定店家
                throw new BusinessException("Admin 新增商品時必須指定店家 ID");
            }
            
            // StoreOwner/Editor：自動查詢並使用第一個店家
            StoreUserExample example = new StoreUserExample();
            example.createCriteria().andAdminUserIdEqualTo(userId);
            List<StoreUser> storeUsers = storeUserMapper.selectByExample(example);
            
            if (storeUsers.isEmpty()) {
                throw new BusinessException("無法取得店家資訊，請聯繫管理員");
            }
            
            String storeId = storeUsers.get(0).getStoreId();
            req.setStoreId(storeId);
            log.info("🔧 [自動帶入] storeId={}", storeId);
        } else {
            log.info("✅ [前端提供] storeId={}", req.getStoreId());
        }
        
        LotteryRes result = lotteryService.createLottery(req);
        
        log.info("✅ 新增成功: id={}, storeId={}", result.getId(), result.getStoreId());
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(result);
    }

    /**
     * 更新商品
     * 
     * ✅ 驗證使用者是否有權限修改此商品
     * ⚠️ 使用正則表達式限制 id 必須是 UUID 格式
     * 
     * @param id 商品 ID（UUID 格式）
     * @param req 更新請求
     * @return 更新後的商品
     */
    @AuditLog(type = AuditLogType.ADMIN_ACTION, action = "UPDATE", targetType = "LOTTERY")
    @PutMapping("/{id:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "更新商品", description = "更新商品資訊")
    public ResponseEntity<LotteryRes> updateLottery(
            @PathVariable String id,
            @Valid @RequestBody LotteryUpdateReq req) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        log.info("✏️ 更新商品: userId={}, lotteryId={}", userId, id);
        
        // 驗證權限（Service 層會檢查）
        LotteryRes result = lotteryService.updateLottery(id, req);
        
        log.info("✅ 更新成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 刪除商品
     * 
     * ✅ 驗證使用者是否有權限刪除此商品
     * ⚠️ 使用正則表達式限制 id 必須是 UUID 格式
     * 
     * @param id 商品 ID（UUID 格式）
     * @return 刪除結果
     */
    @DeleteMapping("/{id:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "刪除商品", description = "刪除商品（僅 Admin 和店主）")
    public ResponseEntity<Void> deleteLottery(@PathVariable String id) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        log.info("🗑️ 刪除商品: userId={}, lotteryId={}", userId, id);
        
        lotteryService.deleteLottery(id);
        
        log.info("✅ 刪除成功");
        return ResponseEntity.noContent().build();
    }

    /**
     * 取得商品詳情
     * 
     * ⚠️ 使用正則表達式限制 id 必須是 UUID 格式
     * 避免與 /list、/on-shelf 等路徑衝突
     * 
     * @param id 商品 ID（UUID 格式）
     * @return 商品詳情
     */
    @GetMapping("/{id:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "取得商品詳情", description = "查詢單一商品")
    public ResponseEntity<LotteryRes> getLottery(@PathVariable String id) {
        
        log.info("🔍 查詢商品詳情: lotteryId={}", id);
        
        LotteryRes result = lotteryService.getLottery(id);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 上架商品
     * 
     * ⚠️ 使用正則表達式限制 id 必須是 UUID 格式
     * 
     * @param id 商品 ID（UUID 格式）
     * @return 更新後的商品
     */
    @AuditLog(type = AuditLogType.ADMIN_ACTION, action = "ON_SHELF", targetType = "LOTTERY")
    @PostMapping("/{id:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}/on-shelf")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "上架商品", description = "將商品設為上架狀態")
    public ResponseEntity<LotteryRes> onShelf(@PathVariable String id) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        log.info("📤 上架商品: userId={}, lotteryId={}", userId, id);
        
        LotteryRes result = lotteryService.updateStatus(id, "ON_SHELF");
        
        log.info("✅ 上架成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 下架商品
     * 
     * ⚠️ 使用正則表達式限制 id 必須是 UUID 格式
     * 
     * @param id 商品 ID（UUID 格式）
     * @return 更新後的商品
     */
    @AuditLog(type = AuditLogType.ADMIN_ACTION, action = "OFF_SHELF", targetType = "LOTTERY")
    @PostMapping("/{id:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}/off-shelf")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "下架商品", description = "將商品設為下架狀態")
    public ResponseEntity<LotteryRes> offShelf(@PathVariable String id) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        log.info("📥 下架商品: userId={}, lotteryId={}", userId, id);
        
        LotteryRes result = lotteryService.updateStatus(id, "OFF_SHELF");
        
        log.info("✅ 下架成功");
        return ResponseEntity.ok(result);
    }
    
    /**
     * 複製商品（完整複製）
     * 
     * 複製內容：
     * 1. Lottery 主表（產生新 ID、標題加上「複製」）
     * 2. 所有 LotteryPrize（獎項）
     * 3. 可選擇是否重新生成籤號
     * 
     * 預設行為：
     * - 新商品標題：原標題 + "（複製）"
     * - 新商品狀態：OFF_SHELF（避免立即上架）
     * - 抽數統計：重置為 0
     * - 獎項數量：重置為原始數量
     * 
     * @param req 複製請求
     * @return 複製後的商品
     */
    @PostMapping("/copy")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "複製商品", description = "將指定商品完整複製（包含所有獎項）")
    public ResponseEntity<LotteryRes> copyLottery(@Valid @RequestBody LotteryCopyReq req) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        log.info("📋 複製商品: userId={}, sourceLotteryId={}, newTitle={}, regenerateTickets={}, newStatus={}", 
                 userId, req.getSourceLotteryId(), req.getNewTitle(), 
                 req.getRegenerateTickets(), req.getNewStatus());
        
        // 呼叫 Service 層複製邏輯
        LotteryRes result = lotteryService.copyLottery(
                req.getSourceLotteryId(), 
                req.getNewTitle(), 
                req.getRegenerateTickets(), 
                req.getNewStatus()
        );
        
        log.info("✅ 複製成功: newLotteryId={}, newTitle={}", result.getId(), result.getTitle());
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(result);
    }

    /**
     * 複製商品（路徑參數版）
     * 
     * @param id 來源商品 ID（UUID 格式）
     * @return 複製後的商品
     */
    @PostMapping("/{id:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}/copy")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "複製商品（路徑參數版）", description = "根據商品 ID 複製商品")
    public ResponseEntity<LotteryRes> copyLotteryById(@PathVariable String id) {
        
        String userId = SecurityUtils.getCurrentUserId();
        log.info("📋 複製商品(path): userId={}, sourceLotteryId={}", userId, id);
        
        LotteryRes result = lotteryService.copyLottery(id, null, true, null);
        
        log.info("✅ 複製成功: newLotteryId={}, newTitle={}", result.getId(), result.getTitle());
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(result);
    }

    /**
     * 變更商品狀態（含 FSM 轉換驗證）
     * 
     * @param id  商品 ID（UUID 格式）
     * @param req 狀態變更請求
     * @return 更新後的商品
     */
    @PutMapping("/{id:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "變更商品狀態", description = "變更商品狀態，含 FSM 轉換驗證")
    public ResponseEntity<LotteryRes> changeStatus(
            @PathVariable String id,
            @Valid @RequestBody LotteryStatusChangeReq req) {
        
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔄 變更商品狀態: userId={}, lotteryId={}, targetStatus={}", userId, id, req.getTargetStatus());
        
        LotteryRes result = lotteryService.changeStatus(id, req.getTargetStatus(), req.getReason(), userId);
        
        log.info("✅ 狀態變更成功: newStatus={}", result.getStatus());
        return ResponseEntity.ok(result);
    }

    // ==================== 商品+獎品整合 API（原 AdminLotteryWithPrizesController）====================
    // ⚠️ URL: /admin/lottery/with-prizes/** (原為 /admin/lottery-with-prizes/**)

    @PostMapping("/with-prizes")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "建立商品與獎品", description = "一支 API 同時建立商品和獎品")
    public ResponseEntity<LotteryWithPrizesRes> createLotteryWithPrizes(
            @Valid @RequestBody LotteryWithPrizesCreateReq req) {
        
        String userId = SecurityUtils.getCurrentUserId();
        String storeId = getStoreIdByUserId(userId);
        if (storeId != null) {
            req.getLottery().setStoreId(storeId);
        }
        
        log.info("📦 建立商品與獎品: userId={}, storeId={}, title={}", userId, storeId, req.getLottery().getTitle());
        
        LotteryWithPrizesRes result = lotteryService.createLotteryWithPrizes(req, userId);
        
        log.info("✅ 建立成功: lotteryId={}", result.getId());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/with-prizes/{lotteryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "更新商品與獎品", description = "一支 API 同時更新商品和獎品")
    public ResponseEntity<LotteryWithPrizesRes> updateLotteryWithPrizes(
            @PathVariable String lotteryId,
            @RequestBody JsonNode body) {
        
        String userId = SecurityUtils.getCurrentUserId();
        log.info("📝 更新商品與獎品: userId={}, lotteryId={}", userId, lotteryId);

        LotteryWithPrizesUpdateReq req = parseLotteryWithPrizesUpdateReq(body);
        req.setLotteryId(lotteryId);
        LotteryWithPrizesRes result = lotteryService.updateLotteryWithPrizes(req, userId);
        
        log.info("✅ 更新成功: lotteryId={}", result.getId());
        return ResponseEntity.ok(result);
    }

    private LotteryWithPrizesUpdateReq parseLotteryWithPrizesUpdateReq(JsonNode body) {
        LotteryWithPrizesUpdateReq req = objectMapper.convertValue(body, LotteryWithPrizesUpdateReq.class);

        // 相容兩種格式：
        // 1. 標準格式：{ lottery: { ... }, prizes: [...] }
        // 2. 舊格式：{ title: ..., description: ..., prizes: [...] }
        // 若兩者同時存在，優先保留 lottery 內的值，外層扁平欄位只補缺漏。
        if (body != null && body.isObject()) {
            LotteryUpdateReq flatReq = objectMapper.convertValue(body, LotteryUpdateReq.class);
            if (req.getLottery() == null) {
                req.setLottery(flatReq);
            } else {
                req.setLottery(mergeLotteryUpdateReq(req.getLottery(), flatReq));
            }
        }

        return req;
    }

    private LotteryUpdateReq mergeLotteryUpdateReq(LotteryUpdateReq primary, LotteryUpdateReq fallback) {
        if (primary == null) {
            return fallback;
        }
        if (fallback == null) {
            return primary;
        }

        LotteryUpdateReq merged = new LotteryUpdateReq();
        copyNonNullProperties(fallback, merged);
        copyNonNullProperties(primary, merged);
        return merged;
    }

    private void copyNonNullProperties(Object source, Object target) {
        BeanWrapper sourceWrapper = new BeanWrapperImpl(source);
        BeanWrapper targetWrapper = new BeanWrapperImpl(target);

        for (PropertyDescriptor propertyDescriptor : sourceWrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            if ("class".equals(propertyName) || !sourceWrapper.isReadableProperty(propertyName)) {
                continue;
            }

            Object value = sourceWrapper.getPropertyValue(propertyName);
            if (value != null && targetWrapper.isWritableProperty(propertyName)) {
                targetWrapper.setPropertyValue(propertyName, value);
            }
        }
    }

    @GetMapping("/with-prizes/{lotteryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "查詢商品與獎品", description = "一支 API 返回商品和獎品完整資訊")
    public ResponseEntity<LotteryWithPrizesRes> getLotteryWithPrizes(@PathVariable String lotteryId) {
        
        log.info("🔍 查詢商品與獎品: lotteryId={}", lotteryId);
        LotteryWithPrizesRes result = lotteryService.getLotteryWithPrizes(lotteryId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/with-prizes/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "查詢所有商品與獎品", description = "條件查詢商品和獎品，自動過濾店家")
    public ResponseEntity<List<LotteryWithPrizesRes>> getAllLotteriesWithPrizes(
            @RequestBody(required = false) QueryReq<LotteryCondition> req) {
        
        String userId = SecurityUtils.getCurrentUserId();
        String storeId = getStoreIdByUserId(userId);
        if (storeId != null) {
            if (req == null) req = new QueryReq<>();
            if (req.getCondition() == null) req.setCondition(new LotteryCondition());
            req.getCondition().setStoreId(storeId);
        }
        
        log.info("🔍 查詢所有商品與獎品: userId={}, storeId={}", userId, storeId);
        List<LotteryWithPrizesRes> result = lotteryService.getAllLotteriesWithPrizes(req);
        log.info("✅ 查詢成功: 返回 {} 個商品", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 後台相容路由：指定刮刮樂大獎位置
     *
     * 相容舊版路徑：
     * 1) /admin/lottery/{lotteryId}/designate-prize
     * 2) /admin/lottery/{lotteryId}/designate-prize-positions
     *
     * 新版建議路徑：
     * 3) /admin/lottery/{lotteryId}/designate
     */
    @PostMapping({
            "/{lotteryId}/designate-prize",
            "/{lotteryId}/designate-prize-positions",
            "/{lotteryId}/designate"
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "指定刮刮樂大獎位置（後台相容路由）", description = "相容舊版 designate-prize 路徑")
    public ResponseEntity<DesignatePrizeResponse> designatePrizePositions(
            @PathVariable String lotteryId,
            @RequestBody DesignatePrizeRequest req) {

        String userId = SecurityUtils.getCurrentUserId();
        int count = req == null || req.designations() == null ? 0 : req.designations().size();
        log.info("🎯 [Admin] 指定大獎位置: lotteryId={}, userId={}, count={}", lotteryId, userId, count);

        if (req == null || req.designations() == null || req.designations().isEmpty()) {
            throw new BusinessException("designations 不可為空");
        }

        lotteryTicketService.designatePrizePositions(lotteryId, userId, req.designations());
        List<DesignatedWinningNumber> designatedNumbers = lotteryTicketService.getDesignatedWinningNumbers(lotteryId);

        return ResponseEntity.ok(new DesignatePrizeResponse(
                true,
                "大獎位置指定完成，共 " + req.designations().size() + " 個",
                designatedNumbers
        ));
    }

    private String getStoreIdByUserId(String userId) {
        if (userId == null) return null;
        StoreUserExample example = new StoreUserExample();
        example.createCriteria().andAdminUserIdEqualTo(userId);
        List<StoreUser> storeUsers = storeUserMapper.selectByExample(example);
        if (storeUsers.isEmpty()) {
            log.warn("⚠️ 使用者沒有關聯店家: userId={}", userId);
            return null;
        }
        String storeId = storeUsers.get(0).getStoreId();
        log.info("🏪 查詢到店家: userId={}, storeId={}", userId, storeId);
        return storeId;
    }

    public record DesignatePrizeRequest(
            List<LotteryTicketService.PrizeDesignation> designations
    ) {}

    public record DesignatePrizeResponse(
            boolean success,
            String message,
            List<DesignatedWinningNumber> designatedWinningNumbers
    ) {}
}
