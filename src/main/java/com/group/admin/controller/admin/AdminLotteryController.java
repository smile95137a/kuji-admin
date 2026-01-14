package com.group.admin.controller.admin;

import com.group.admin.entity.StoreUser;
import com.group.admin.example.StoreUserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.req.lottery.LotteryCopyReq;
import com.group.admin.req.lottery.LotteryCreateReq;
import com.group.admin.req.lottery.LotteryUpdateReq;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    private final StoreUserMapper storeUserMapper;

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
    public ResponseEntity<List<LotteryRes>> queryLotteries(
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
        
        List<LotteryRes> result = lotteryService.queryLotteries(req);
        
        log.info("✅ 查詢成功: 共 {} 筆", result.size());
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
        return ResponseEntity.ok(result);
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
        return ResponseEntity.ok(result);
    }
}
