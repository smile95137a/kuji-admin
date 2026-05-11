package com.group.admin.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryDrawRecord;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.entity.PointLog;
import com.group.admin.entity.Store;
import com.group.admin.entity.User;
import com.group.admin.enums.GameModeEnum;
import com.group.admin.enums.LotteryCategoryEnum;
import com.group.admin.enums.LotteryStatusEnum;
import com.group.admin.enums.LotterySubCategoryEnum;
import com.group.admin.example.LotteryExample;
import com.group.admin.example.LotteryPrizeExample;
import com.group.admin.example.LotteryTicketExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.LotteryDrawRecordMapper;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.mapper.LotteryTicketMapper;
import com.group.admin.mapper.PointLogMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.req.lottery.LotteryCreateReq;
import com.group.admin.req.lottery.LotteryQueryReq;
import com.group.admin.req.lottery.LotteryUpdateReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.lottery.LotteryListRes;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.CategoryService;
import com.group.admin.service.LotteryService;
import com.group.admin.service.LotteryTicketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽獎商品服務實作
 * 使用 Example 模式進行資料庫操作
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryServiceImpl implements LotteryService {

    private final LotteryMapper lotteryMapper;
    private final LotteryPrizeMapper lotteryPrizeMapper;
    private final LotteryDrawRecordMapper drawRecordMapper;
    private final UserMapper userMapper;
    private final PointLogMapper pointLogMapper;
    private final ObjectMapper objectMapper;
    private final StoreMapper storeMapper;
    private final LotteryTicketService lotteryTicketService;
    private final LotteryTicketMapper lotteryTicketMapper;
    private final CategoryService categoryService;

    private final Random random = new Random();

    private static final String SYSTEM_OPERATOR = "SYSTEM";

    // ==================== 商品管理 CRUD ====================

    @Override
    @Transactional
    public LotteryRes createLottery(LotteryCreateReq req, String operatorId) {
        log.info("創建抽獎商品: title={}, operatorId={}", req.getTitle(), operatorId);

        String normalizedSubCategory = normalizeSubCategory(req.getCategory(), req.getSubCategory());
        validateLotteryFieldUsage(
                req.getCategory(),
                normalizedSubCategory,
                req.getGameMode(),
                req.getFreeDrawThreshold(),
                req.getDesignatedPrizeNumbers());
        String normalizedTheme = upsertThemeIfPresent(req.getTheme());
        categoryService.validateTagNames(req.getTags());

        Lottery lottery = new Lottery();
        lottery.setId(UUID.randomUUID().toString());
        lottery.setStoreId(req.getStoreId());
        lottery.setTitle(req.getTitle());
        lottery.setDescription(req.getDescription());
        lottery.setImageUrl(req.getImageUrl());
        lottery.setCategory(req.getCategory());
        lottery.setSubCategory(normalizedSubCategory);
        String resolvedPlayMode = resolvePlayMode(req.getPlayMode(), req.getCategory(), normalizedSubCategory);
        String resolvedGameMode = resolveGameMode(req.getCategory(), normalizedSubCategory, req.getGameMode());
        String resolvedDelistStrategy = resolveDelistStrategy(
                req.getCategory(),
                normalizedSubCategory,
                req.getDelistStrategy());
        Integer normalizedFreeDrawThreshold = normalizeFreeDrawThreshold(
                req.getCategory(),
                normalizedSubCategory,
                req.getFreeDrawThreshold());
        String normalizedPaymentType = normalizePaymentType(req.getPaymentType());

        lottery.setPricePerDraw(req.getPricePerDraw());
        lottery.setDiscountedPrice(req.getDiscountedPrice());
        lottery.setAutoDiscountEnabled(
                req.getAutoDiscountEnabled() != null && req.getAutoDiscountEnabled() ? (byte) 1 : (byte) 0);
        lottery.setAllowMultiDraw(null);
        lottery.setBonusEnabled(req.getBonusEnabled());
        lottery.setMultiDrawOptions(null);

        if (req.getTags() != null) {
            try {
                lottery.setTags(objectMapper.writeValueAsString(req.getTags()));
            } catch (JsonProcessingException e) {
                log.warn("標籤序列化失敗", e);
            }
        }

        if (req.getGalleryImages() != null) {
            try {
                lottery.setGalleryImages(objectMapper.writeValueAsString(req.getGalleryImages()));
            } catch (JsonProcessingException e) {
                log.warn("圖庫序列化失敗", e);
            }
        }

        lottery.setScheduledAt(req.getScheduledAt());
        lottery.setStartTime(req.getStartTime());
        lottery.setEndTime(req.getEndTime());
        lottery.setMaxDraws(req.getMaxDraws());
        lottery.setTotalDraws(0);
        lottery.setPlayMode(resolvedPlayMode);
        lottery.setGameMode(resolvedGameMode);
        lottery.setStatus(
                resolveLifecycleStatus(req.getStatus(), req.getScheduledAt(), LotteryStatusEnum.OFF_SHELF.getCode()));
        lottery.setOrderNum(req.getOrderNum() != null ? req.getOrderNum() : 0);
        lottery.setHotCount(req.getHotCount());
        lottery.setTheme(normalizedTheme);
        lottery.setBonusPointsPerDraw(req.getBonusPointsPerDraw());
        lottery.setBonusCostPerDraw(req.getBonusCostPerDraw());
        // ✅ 不再設定 weight
        lottery.setCreatedBy(operatorId);
        lottery.setCreatedAt(LocalDateTime.now());
        lottery.setUpdatedAt(LocalDateTime.now());
        lottery.setRemark(req.getRemark());
        lottery.setFreeDrawEnabled(Boolean.TRUE.equals(req.getFreeDrawEnabled()) ? (byte) 1 : (byte) 0);
        lottery.setProtectionDraws(req.getProtectionDraws());
        // T007: paymentType / delistStrategy defaults; freeDrawThreshold validation
        lottery.setPaymentType(normalizedPaymentType);
        lottery.setDelistStrategy(resolvedDelistStrategy);
        lottery.setFreeDrawThreshold(normalizedFreeDrawThreshold);
        lottery.setDesignatedPrizeNumbers(
                sanitizeDesignatedPrizeNumbers(resolvedGameMode, req.getDesignatedPrizeNumbers()));

        lotteryMapper.insert(lottery);
        log.info("抽獎商品創建成功: id={}", lottery.getId());

        return getLotteryById(lottery.getId());
    }

    @Override
    @Transactional
    public LotteryRes updateLottery(LotteryUpdateReq req, String operatorId) {
        log.info("更新抽獎商品: id={}, operatorId={}", req.getId(), operatorId);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(req.getId());
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }

        if (req.getTags() != null) {
            categoryService.validateTagNames(req.getTags());
        }

        // 狀態變更（上/下架）永遠允許
        // 只有內容修改才限制只能在草稿或已下架狀態
        String status = lottery.getStatus();
        boolean isContentUpdate = req.getTitle() != null || req.getDescription() != null
                || req.getImageUrl() != null || req.getCategory() != null
                || req.getSubCategory() != null || req.getPricePerDraw() != null
                || req.getMaxDraws() != null || req.getMultiDrawOptions() != null
                || req.getTags() != null || req.getGalleryImages() != null;
        if (isContentUpdate
                && !LotteryStatusEnum.DRAFT.getCode().equals(status)
                && !LotteryStatusEnum.OFF_SHELF.getCode().equals(status)) {
            throw new BusinessException("已上架的商品不可修改內容，請先下架再編輯");
        }

        String currentCategory = req.getCategory() != null ? req.getCategory() : lottery.getCategory();
        String currentSubCategory = req.getSubCategory() != null ? req.getSubCategory() : lottery.getSubCategory();
        String normalizedSubCategory = normalizeSubCategory(currentCategory, currentSubCategory);
        String resolvedPlayMode = resolvePlayMode(req.getPlayMode(), currentCategory, currentSubCategory);
        String requestGameMode = req.getGameMode() != null ? req.getGameMode() : lottery.getGameMode();
        String resolvedGameMode = resolveGameMode(currentCategory, normalizedSubCategory, requestGameMode);
        String requestDelistStrategy = req.getDelistStrategy() != null ? req.getDelistStrategy()
                : lottery.getDelistStrategy();
        String resolvedDelistStrategy = resolveDelistStrategy(currentCategory, normalizedSubCategory,
                requestDelistStrategy);
        Integer requestFreeDrawThreshold = req.getFreeDrawThreshold() != null
                ? req.getFreeDrawThreshold()
                : lottery.getFreeDrawThreshold();
        Integer normalizedFreeDrawThreshold = normalizeFreeDrawThreshold(
                currentCategory,
                normalizedSubCategory,
                requestFreeDrawThreshold);
        String requestDesignatedPrizeNumbers = req.getDesignatedPrizeNumbers() != null
                ? req.getDesignatedPrizeNumbers()
                : lottery.getDesignatedPrizeNumbers();

        validateLotteryFieldUsage(
                currentCategory,
                normalizedSubCategory,
                req.getGameMode(),
                req.getFreeDrawThreshold(),
                req.getDesignatedPrizeNumbers());

        if (req.getTitle() != null)
            lottery.setTitle(req.getTitle());
        if (req.getDescription() != null)
            lottery.setDescription(req.getDescription());
        if (req.getImageUrl() != null)
            lottery.setImageUrl(req.getImageUrl());
        if (req.getCategory() != null)
            lottery.setCategory(req.getCategory());
        lottery.setSubCategory(normalizedSubCategory);
        if (req.getPricePerDraw() != null)
            lottery.setPricePerDraw(req.getPricePerDraw());
        if (req.getDiscountedPrice() != null)
            lottery.setDiscountedPrice(req.getDiscountedPrice());
        if (req.getAutoDiscountEnabled() != null)
            lottery.setAutoDiscountEnabled(req.getAutoDiscountEnabled() ? (byte) 1 : (byte) 0);
        if (req.getAllowMultiDraw() != null)
            lottery.setAllowMultiDraw(null);
        if (req.getBonusEnabled() != null)
            lottery.setBonusEnabled(req.getBonusEnabled());
        if (req.getMultiDrawOptions() != null)
            lottery.setMultiDrawOptions(null);

        if (req.getTags() != null) {
            try {
                lottery.setTags(objectMapper.writeValueAsString(req.getTags()));
            } catch (JsonProcessingException e) {
                log.warn("標籤序列化失敗", e);
            }
        }

        if (req.getGalleryImages() != null) {
            try {
                lottery.setGalleryImages(objectMapper.writeValueAsString(req.getGalleryImages()));
            } catch (JsonProcessingException e) {
                log.warn("圖庫序列化失敗", e);
            }
        }

        LocalDateTime targetScheduledAt = req.getScheduledAt() != null ? req.getScheduledAt()
                : lottery.getScheduledAt();
        if (req.getScheduledAt() != null)
            lottery.setScheduledAt(req.getScheduledAt());
        if (req.getStartTime() != null)
            lottery.setStartTime(req.getStartTime());
        if (req.getEndTime() != null)
            lottery.setEndTime(req.getEndTime());
        if (req.getMaxDraws() != null)
            lottery.setMaxDraws(req.getMaxDraws());
        if (req.getOrderNum() != null)
            lottery.setOrderNum(req.getOrderNum());
        if (req.getStatus() != null || req.getScheduledAt() != null) {
            lottery.setStatus(resolveLifecycleStatus(req.getStatus(), targetScheduledAt, lottery.getStatus()));
        }
        lottery.setPlayMode(resolvedPlayMode);
        lottery.setGameMode(resolvedGameMode);
        lottery.setDelistStrategy(resolvedDelistStrategy);
        lottery.setFreeDrawThreshold(normalizedFreeDrawThreshold);
        lottery.setDesignatedPrizeNumbers(
                sanitizeDesignatedPrizeNumbers(resolvedGameMode, requestDesignatedPrizeNumbers));
        // T008: new fields update
        if (req.getPaymentType() != null) {
            String normalizedPaymentType = normalizePaymentType(req.getPaymentType());
            // DB 舊資料 paymentType 可能為 null，視為預設值 "GOLD"
            String existingPaymentType = lottery.getPaymentType() != null ? lottery.getPaymentType() : "GOLD";
            if (!LotteryStatusEnum.DRAFT.getCode().equals(status)
                    && !normalizedPaymentType.equals(existingPaymentType)) {
                throw new BusinessException("非草稿商品不可修改 paymentType");
            }
            lottery.setPaymentType(normalizedPaymentType);
        }
        if (req.getFreeDrawEnabled() != null) {
            lottery.setFreeDrawEnabled(req.getFreeDrawEnabled() ? (byte) 1 : (byte) 0);
        }
        if (req.getProtectionDraws() != null) {
            lottery.setProtectionDraws(req.getProtectionDraws());
        }
        // ✅ 不再設定 weight
        if (req.getRemark() != null)
            lottery.setRemark(req.getRemark());

        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKey(lottery);
        log.info("抽獎商品更新成功: id={}", req.getId());

        return getLotteryById(req.getId());
    }

    @Override
    @Transactional
    public void deleteLottery(String id, String operatorId) {
        log.info("刪除抽獎商品: id={}, operatorId={}", id, operatorId);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(id);
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }

        // 只有草稿和已下架狀態可以刪除
        String status = lottery.getStatus();
        if (!LotteryStatusEnum.DRAFT.getCode().equals(status)
                && !LotteryStatusEnum.OFF_SHELF.getCode().equals(status)) {
            throw new BusinessException("只有草稿或已下架狀態的商品可以刪除");
        }

        // 先刪除關聯的獎項（使用 Example 模式）
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria().andLotteryIdEqualTo(id);
        lotteryPrizeMapper.deleteByExample(prizeExample);

        // 刪除商品
        lotteryMapper.deleteByPrimaryKey(id);
        log.info("抽獎商品刪除成功: id={}", id);
    }

    @Override
    public LotteryRes getLotteryById(String id) {
        Lottery lottery = lotteryMapper.selectByPrimaryKey(id);
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }
        return convertToRes(lottery);
    }

    @Override
    public PageResult<LotteryListRes> queryLotteries(LotteryQueryReq req) {
        // 使用 Example 模式進行查詢
        LotteryExample example = new LotteryExample();
        LotteryExample.Criteria criteria = example.createCriteria();

        // ✅ 使用 isNotBlank 處理空字串
        if (isNotBlank(req.getStoreId())) {
            criteria.andStoreIdEqualTo(req.getStoreId());
        }
        if (isNotBlank(req.getKeyword())) {
            criteria.andTitleLike("%" + req.getKeyword() + "%");
        }
        if (isNotBlank(req.getCategory())) {
            criteria.andCategoryEqualTo(req.getCategory());
        }
        if (isNotBlank(req.getTheme())) {
            criteria.andThemeEqualTo(categoryService.resolveCanonicalThemeName(req.getTheme()));
        }
        if (isNotBlank(req.getStatus())) {
            criteria.andStatusEqualTo(req.getStatus());
        }

        // 設置排序
        String sortBy = normalizeLotterySortColumn(req.getSortBy());
        String sortDirection = normalizeSortOrder(req.getSortDirection(), "DESC");
        example.setOrderByClause(sortBy + " " + sortDirection);

        // 獲取總數
        long total = lotteryMapper.countByExample(example);

        // MyBatis Example 不支援直接設置分頁，需使用 RowBounds 或直接查詢後手動分頁
        // 這裡先查詢全部，然後手動分頁（小資料量可行，大資料量建議使用 PageHelper）
        List<Lottery> allLotteries = lotteryMapper.selectByExampleWithBLOBs(example);

        // 手動分頁
        int offset = (req.getPage() - 1) * req.getSize();
        int endIndex = Math.min(offset + req.getSize(), allLotteries.size());
        List<Lottery> lotteries = offset < allLotteries.size()
                ? allLotteries.subList(offset, endIndex)
                : new ArrayList<>();

        List<LotteryListRes> list = lotteries.stream()
                .map(this::convertToListRes)
                .toList();

        return PageResult.of(req.getPage(), req.getSize(), total, list);
    }

    @Override
    public PageResult<LotteryListRes> queryLotteriesByStore(String storeId, LotteryQueryReq req) {
        req.setStoreId(storeId);
        PageResult<LotteryRes> pageResult = queryLotteries(toQueryReq(req));
        List<LotteryListRes> items = pageResult.getData().stream()
                .map(this::convertToListRes)
                .collect(Collectors.toList());
        return PageResult.of(pageResult.getPage(), pageResult.getSize(), pageResult.getTotal(), items);
    }

    // ==================== 狀態管理 ====================

    @Override
    @Transactional
    public LotteryRes publishLottery(String id, String operatorId) {
        log.info("上架商品: id={}, operatorId={}", id, operatorId);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(id);
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }

        // 檢查是否有獎項
        int prizeCount = sumQuantityByLotteryId(id);
        if (prizeCount <= 0) {
            throw new BusinessException("商品尚未設定獎項，無法上架");
        }

        lottery.setStatus(LotteryStatusEnum.ON_SHELF.getCode());
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKey(lottery);
        log.info("商品上架成功: id={}", id);

        return getLotteryById(id);
    }

    @Override
    @Transactional
    public LotteryRes unpublishLottery(String id, String operatorId) {
        log.info("下架商品: id={}, operatorId={}", id, operatorId);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(id);
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }

        lottery.setStatus(LotteryStatusEnum.OFF_SHELF.getCode());
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKey(lottery);
        log.info("商品下架成功: id={}", id);

        return getLotteryById(id);
    }

    @Override
    @Transactional
    public LotteryRes forceOffShelf(String id, String reason, String operatorId) {
        log.info("強制下架商品: id={}, reason={}, operatorId={}", id, reason, operatorId);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(id);
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }

        lottery.setStatus(LotteryStatusEnum.FORCED_OFF.getCode());
        lottery.setRemark(lottery.getRemark() != null
                ? lottery.getRemark() + "\n強制下架原因: " + reason
                : "強制下架原因: " + reason);
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKey(lottery);
        log.info("商品強制下架成功: id={}", id);

        return getLotteryById(id);
    }

    // ==================== 抽獎相關 ====================

    @Override
    @Transactional
    public LotteryDrawRecord draw(String lotteryId, String userId, String costType) {
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            throw new BusinessException("抽獎活動不存在");
        }

        // 檢查商品狀態
        if (!LotteryStatusEnum.ON_SHELF.getCode().equals(lottery.getStatus())) {
            throw new BusinessException("商品未上架或已結束");
        }

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }

        long price = lottery.getPricePerDraw() == null ? 0L : lottery.getPricePerDraw();
        if ("bonus".equalsIgnoreCase(costType)) {
            if (user.getBonusCoins() == null || user.getBonusCoins() < price) {
                throw new BusinessException("紅利金不足");
            }
        } else {
            if (user.getGoldCoins() == null || user.getGoldCoins() < price) {
                throw new BusinessException("儲值金不足");
            }
        }

        // 獲取可用獎項（使用 Example 模式）
        List<LotteryPrize> prizes = selectPrizesByLotteryId(lotteryId);
        if (prizes == null || prizes.stream().noneMatch(p -> p.getRemaining() != null && p.getRemaining() > 0)) {
            // 結束抽獎
            lottery.setStatus(LotteryStatusEnum.ALL_DRAWN.getCode());
            lottery.setUpdatedAt(LocalDateTime.now());
            lotteryMapper.updateByPrimaryKey(lottery);
            throw new BusinessException("獎品已抽完，活動結束");
        }

        // 狀態維持 ON_SHELF，不再使用 IN_PROGRESS

        LotteryPrize selected = selectPrize(prizes, lotteryId);

        if (selected == null) {
            throw new BusinessException("抽獎失敗，請稍後再試");
        }

        // 扣除餘額並更新用戶
        Long beforeGold = user.getGoldCoins() == null ? 0L : user.getGoldCoins();
        Long beforeBonus = user.getBonusCoins() == null ? 0L : user.getBonusCoins();

        if ("bonus".equalsIgnoreCase(costType)) {
            user.setBonusCoins(beforeBonus - price);
        } else {
            user.setGoldCoins(beforeGold - price);
        }
        userMapper.updateByPrimaryKey(user);

        // 記錄點數變動
        PointLog pointLog = new PointLog();
        pointLog.setId(UUID.randomUUID().toString());
        pointLog.setUserId(userId);
        pointLog.setPointType(costType.equalsIgnoreCase("bonus") ? "BONUS" : "GOLD");
        pointLog.setOperationType("DRAW");
        pointLog.setAmount(-price);
        pointLog.setBeforeBalance(costType.equalsIgnoreCase("bonus") ? beforeBonus : beforeGold);
        pointLog.setAfterBalance((costType.equalsIgnoreCase("bonus") ? beforeBonus : beforeGold) - price);
        pointLog.setReferenceType("LOTTERY");
        pointLog.setReferenceId(lotteryId);
        pointLog.setRemark("抽獎消費");
        pointLog.setCreatedAt(LocalDateTime.now());
        pointLogMapper.insert(pointLog);

        // 建立抽獎記錄
        LotteryDrawRecord record = new LotteryDrawRecord();
        record.setId(UUID.randomUUID().toString());
        record.setLotteryId(lotteryId);
        record.setUserId(userId);
        record.setPrizeId(selected.getId());
        record.setCostType(costType);
        record.setCostAmount(price);
        record.setStatus("completed");
        record.setCreatedAt(LocalDateTime.now());
        drawRecordMapper.insert(record);

        // 遞增抽獎次數
        lottery.setTotalDraws((lottery.getTotalDraws() == null ? 0 : lottery.getTotalDraws()) + 1);
        lotteryMapper.updateByPrimaryKey(lottery);

        // 檢查是否需要觸發降價
        checkAndTriggerDiscount(lotteryId);

        return record;
    }

    @Override
    @Transactional
    public List<LotteryDrawRecord> multiDraw(String lotteryId, String userId, int drawCount, String costType) {
        log.info("執行多連抽: lotteryId={}, userId={}, count={}", lotteryId, userId, drawCount);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            throw new BusinessException("抽獎活動不存在");
        }

        // 檢查是否允許多抽
        if (lottery.getAllowMultiDraw() == null || lottery.getAllowMultiDraw() == 0) {
            throw new BusinessException("此商品不支援多連抽");
        }

        // 檢查連抽次數是否在允許範圍內
        List<Integer> allowedOptions = parseMultiDrawOptions(lottery.getMultiDrawOptions());
        if (!allowedOptions.contains(drawCount)) {
            throw new BusinessException("不支援的連抽次數");
        }

        List<LotteryDrawRecord> records = new ArrayList<>();
        for (int i = 0; i < drawCount; i++) {
            try {
                LotteryDrawRecord record = draw(lotteryId, userId, costType);
                records.add(record);
            } catch (BusinessException e) {
                if ("獎品已抽完，活動結束".equals(e.getMessage())) {
                    break; // 獎品抽完提前結束
                }
                throw e;
            }
        }

        log.info("多連抽完成: 實際抽取 {} 次", records.size());
        return records;
    }

    // ==================== 降價機制 ====================

    @Override
    @Transactional
    public void triggerGrandPrizeDiscount(String lotteryId) {
        log.info("觸發大獎售完降價: lotteryId={}", lotteryId);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null || lottery.getDiscountedPrice() == null) {
            return;
        }

        lottery.setPricePerDraw(lottery.getDiscountedPrice());
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKey(lottery);

        log.info("降價完成: lotteryId={}, newPrice={}", lotteryId, lottery.getDiscountedPrice());
    }

    @Override
    public boolean checkAndTriggerDiscount(String lotteryId) {
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null)
            return false;

        // 檢查是否啟用自動降價
        if (lottery.getAutoDiscountEnabled() == null || lottery.getAutoDiscountEnabled() == 0) {
            return false;
        }

        // 檢查是否有折扣價
        if (lottery.getDiscountedPrice() == null) {
            return false;
        }

        // 檢查是否已經降過價
        if (lottery.getPricePerDraw() != null && lottery.getPricePerDraw().equals(lottery.getDiscountedPrice())) {
            return false;
        }

        // 檢查大賞是否還有剩餘
        int grandPrizeRemaining = countGrandPrizeRemaining(lotteryId);
        if (grandPrizeRemaining <= 0) {
            triggerGrandPrizeDiscount(lotteryId);
            return true;
        }

        return false;
    }

    // ==================== 統計相關 ====================

    @Override
    public int getRemainingDrawCount(String lotteryId) {
        return sumRemainingByLotteryId(lotteryId);
    }

    @Override
    public Map<String, Object> getPrizeStatistics(String lotteryId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPrizes", sumQuantityByLotteryId(lotteryId));
        stats.put("remainingPrizes", sumRemainingByLotteryId(lotteryId));
        stats.put("grandPrizeRemaining", countGrandPrizeRemaining(lotteryId));
        return stats;
    }

    @Override
    public List<com.group.admin.res.lottery.LotteryPrizeRes> getPrizesByLotteryId(String lotteryId) {
        List<LotteryPrize> prizes = selectPrizesByLotteryId(lotteryId);
        return prizes.stream()
                .map(this::convertPrizeToRes)
                .collect(Collectors.toList());
    }

    // ==================== 私有輔助方法 ====================

    /**
     * 依 lotteryId 查詢獎項列表
     */
    private List<LotteryPrize> selectPrizesByLotteryId(String lotteryId) {
        LotteryPrizeExample example = new LotteryPrizeExample();
        example.createCriteria().andLotteryIdEqualTo(lotteryId);
        example.setOrderByClause("order_num ASC");
        return lotteryPrizeMapper.selectByExample(example);
    }

    /**
     * 計算獎項總數
     */
    private int sumQuantityByLotteryId(String lotteryId) {
        List<LotteryPrize> prizes = selectPrizesByLotteryId(lotteryId);
        return prizes.stream()
                .mapToInt(p -> p.getQuantity() == null ? 0 : p.getQuantity())
                .sum();
    }

    /**
     * 計算剩餘獎項數
     */
    private int sumRemainingByLotteryId(String lotteryId) {
        List<LotteryPrize> prizes = selectPrizesByLotteryId(lotteryId);
        return prizes.stream()
                .filter(p -> p.getIsLastPrize() == null || p.getIsLastPrize() != 1) // 最後賞不計入剩餘抽數
                .mapToInt(p -> p.getRemaining() == null ? 0 : p.getRemaining())
                .sum();
    }

    /**
     * 計算大賞剩餘數
     */
    private int countGrandPrizeRemaining(String lotteryId) {
        LotteryPrizeExample example = new LotteryPrizeExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andIsGrandPrizeEqualTo((byte) 1)
                .andRemainingGreaterThan(0);
        List<LotteryPrize> grandPrizes = lotteryPrizeMapper.selectByExample(example);
        return grandPrizes.stream()
                .mapToInt(p -> p.getRemaining() == null ? 0 : p.getRemaining())
                .sum();
    }

    /**
     * 根據剩餘數量選擇獎項
     * 
     * ✅ 機率計算：1/剩餘數量
     * - 不使用 weight 欄位
     * - 每個獎項的機率 = remaining / 總剩餘數量
     * - 例如：A賞剩1個，B賞剩3個 → A機率=1/4，B機率=3/4
     */
    private LotteryPrize selectPrize(List<LotteryPrize> prizes, String lotteryId) {
        LotteryPrize selected = null;
        int attempts = 0;

        while (attempts++ < 5) {
            // 計算總剩餘數量（機率 = 1/剩餘數量）
            long total = prizes.stream()
                    .filter(p -> p.getRemaining() != null && p.getRemaining() > 0)
                    .mapToLong(p -> (long) p.getRemaining())
                    .sum();

            if (total <= 0)
                break;

            long r = Math.abs(random.nextLong()) % total;
            long cum = 0;

            for (LotteryPrize p : prizes) {
                if (p.getRemaining() == null || p.getRemaining() <= 0)
                    continue;
                long remaining = (long) p.getRemaining();
                cum += remaining;
                if (r < cum) {
                    // 扣減獎項剩餘數量
                    int updated = decrementPrizeRemaining(p.getId());
                    if (updated > 0) {
                        selected = p;
                        break;
                    } else {
                        // 庫存不足，重新查詢獎項
                        prizes = selectPrizesByLotteryId(lotteryId);
                        selected = null;
                        break;
                    }
                }
            }
            if (selected != null)
                break;
        }

        return selected;
    }

    /**
     * 扣減獎項剩餘數量
     */
    private int decrementPrizeRemaining(String prizeId) {
        LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeId);
        if (prize == null || prize.getRemaining() == null || prize.getRemaining() <= 0) {
            return 0;
        }
        prize.setRemaining(prize.getRemaining() - 1);
        prize.setUpdatedAt(LocalDateTime.now());
        return lotteryPrizeMapper.updateByPrimaryKey(prize);
    }

    /**
     * 轉換為詳細響應
     */
    private LotteryRes convertToRes(Lottery lottery) {
        LotteryRes res = new LotteryRes();
        res.setId(lottery.getId());
        res.setStoreId(lottery.getStoreId());
        res.setTitle(lottery.getTitle());
        res.setDescription(lottery.getDescription());
        res.setImageUrl(lottery.getImageUrl());
        res.setCategory(lottery.getCategory());
        res.setCategoryName(LotteryCategoryEnum.getNameByCode(lottery.getCategory()));
        res.setSubCategory(lottery.getSubCategory());
        res.setPricePerDraw(lottery.getPricePerDraw());
        res.setCurrentPrice(lottery.getPricePerDraw());
        res.setDiscountedPrice(lottery.getDiscountedPrice());
        res.setAutoDiscountEnabled(lottery.getAutoDiscountEnabled() != null && lottery.getAutoDiscountEnabled() == 1);
        res.setDiscountTriggered(lottery.getDiscountedPrice() != null
                && lottery.getPricePerDraw() != null
                && lottery.getPricePerDraw().equals(lottery.getDiscountedPrice()));
        res.setAllowMultiDraw(lottery.getAllowMultiDraw() != null && lottery.getAllowMultiDraw() == 1);
        res.setMultiDrawOptions(parseMultiDrawOptions(lottery.getMultiDrawOptions()));
        res.setScheduledAt(lottery.getScheduledAt());
        res.setStartTime(lottery.getStartTime());
        res.setEndTime(lottery.getEndTime());
        res.setTotalDraws(lottery.getTotalDraws());
        res.setMaxDraws(lottery.getMaxDraws());
        res.setRemainingDraws(sumRemainingByLotteryId(lottery.getId()));
        res.setStatus(lottery.getStatus());
        res.setStatusName(LotteryStatusEnum.getNameByCode(lottery.getStatus()));
        res.setOrderNum(lottery.getOrderNum());
        res.setWeight(lottery.getWeight());
        res.setCreatedBy(lottery.getCreatedBy());
        res.setCreatedAt(lottery.getCreatedAt());
        res.setUpdatedAt(lottery.getUpdatedAt());
        res.setRemark(lottery.getRemark());
        res.setTotalPrizes(sumQuantityByLotteryId(lottery.getId()));
        res.setRemainingPrizes(sumRemainingByLotteryId(lottery.getId()));
        res.setProtectionDraws(lottery.getProtectionDraws());
        res.setFreeDrawEnabled(lottery.getFreeDrawEnabled() != null && lottery.getFreeDrawEnabled() == 1);
        res.setPaymentType(isNotBlank(lottery.getPaymentType()) ? lottery.getPaymentType() : "GOLD");
        res.setFreeDrawThreshold(sanitizeFreeDrawThresholdForResponse(
                lottery.getCategory(),
                lottery.getSubCategory(),
                lottery.getFreeDrawThreshold()));
        res.setDelistStrategy(isNotBlank(lottery.getDelistStrategy()) ? lottery.getDelistStrategy() : "MANUAL");
        return res;
    }

    /**
     * 轉換為列表響應
     */
    private LotteryListRes convertToListRes(Lottery lottery) {
        LotteryListRes res = new LotteryListRes();
        res.setId(lottery.getId());
        res.setStoreId(lottery.getStoreId());
        res.setTitle(lottery.getTitle());
        res.setImageUrl(lottery.getImageUrl());
        res.setCategory(lottery.getCategory());
        res.setCategoryName(LotteryCategoryEnum.getNameByCode(lottery.getCategory()));
        res.setPricePerDraw(lottery.getPricePerDraw());
        res.setCurrentPrice(lottery.getPricePerDraw());
        res.setStatus(lottery.getStatus());
        res.setStatusName(LotteryStatusEnum.getNameByCode(lottery.getStatus()));
        res.setTotalDraws(lottery.getTotalDraws());
        res.setRemainingDraws(sumRemainingByLotteryId(lottery.getId()));
        res.setTotalPrizes(sumQuantityByLotteryId(lottery.getId()));
        res.setRemainingPrizes(sumRemainingByLotteryId(lottery.getId()));
        res.setOrderNum(lottery.getOrderNum());
        res.setCreatedAt(lottery.getCreatedAt());
        return res;
    }

    /**
     * 轉換為列表響應（新架構查詢結果）
     */
    private LotteryListRes convertToListRes(LotteryRes lottery) {
        LotteryListRes res = new LotteryListRes();
        res.setId(lottery.getId());
        res.setStoreId(lottery.getStoreId());
        res.setStoreName(lottery.getStoreName());
        res.setTitle(lottery.getTitle());
        res.setImageUrl(lottery.getImageUrl());
        res.setCategory(lottery.getCategory());
        res.setCategoryName(lottery.getCategoryName());
        res.setPricePerDraw(lottery.getPricePerDraw());
        res.setCurrentPrice(lottery.getCurrentPrice());
        res.setStatus(lottery.getStatus());
        res.setStatusName(lottery.getStatusName());
        res.setTotalDraws(lottery.getTotalDraws());
        res.setRemainingDraws(lottery.getRemainingDraws());
        res.setTotalPrizes(lottery.getTotalPrizes());
        res.setRemainingPrizes(lottery.getRemainingPrizes());
        res.setOrderNum(lottery.getOrderNum());
        res.setCreatedAt(lottery.getCreatedAt());
        return res;
    }

    /**
     * 解析多連抽選項
     */
    private List<Integer> parseMultiDrawOptions(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Integer>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("多抽選項解析失敗: {}", json, e);
            return List.of();
        }
    }

    // ==================== 新架構方法實作 ====================

    /**
     * 查詢商品列表（新架構）
     */
    @Override
    public PageResult<LotteryRes> queryLotteries(QueryReq<LotteryCondition> req) {
        log.info("🔍 [新架構] 查詢商品列表: {}", req);

        QueryReq<LotteryCondition> safeReq = normalizeReq(req);
        LotteryCondition condition = safeReq.getCondition();
        int page = resolvePage(safeReq.getPage());
        int size = resolveSize(safeReq.getSize());

        // 使用 MyBatis Example 動態 SQL
        LotteryExample example = new LotteryExample();
        LotteryExample.Criteria criteria = example.createCriteria();

        // ✅ 所有條件都是可選的（空字串視為 null）
        if (condition != null) {
            // storeId：空字串視為 null（不過濾店家）
            if (isNotBlank(condition.getStoreId())) {
                criteria.andStoreIdEqualTo(condition.getStoreId());
                log.info("🔍 過濾店家: {}", condition.getStoreId());
            } else {
                log.info("🔍 不過濾店家（查詢所有）");
            }

            // title：模糊查詢
            if (isNotBlank(condition.getTitle())) {
                criteria.andTitleLike("%" + condition.getTitle() + "%");
            }

            // status：精確匹配
            if (isNotBlank(condition.getStatus())) {
                criteria.andStatusEqualTo(condition.getStatus());
            }

            // category：精確匹配
            if (isNotBlank(condition.getCategory())) {
                criteria.andCategoryEqualTo(condition.getCategory());
            }

            // subCategory：精確匹配
            if (isNotBlank(condition.getSubCategory())) {
                criteria.andSubCategoryEqualTo(condition.getSubCategory());
            }

            // playMode：精確匹配
            if (isNotBlank(condition.getPlayMode())) {
                criteria.andPlayModeEqualTo(condition.getPlayMode());
            }

            // theme：精確匹配
            if (isNotBlank(condition.getTheme())) {
                criteria.andThemeEqualTo(categoryService.resolveCanonicalThemeName(condition.getTheme()));
            }

            // 價格範圍
            if (condition.getPriceMin() != null) {
                criteria.andPricePerDrawGreaterThanOrEqualTo(condition.getPriceMin());
            }
            if (condition.getPriceMax() != null) {
                criteria.andPricePerDrawLessThanOrEqualTo(condition.getPriceMax());
            }

            // 日期範圍（LocalDate 轉 LocalDateTime）
            if (condition.getCreatedAtStart() != null) {
                criteria.andCreatedAtGreaterThanOrEqualTo(
                        condition.getCreatedAtStart().atStartOfDay());
            }
            if (condition.getCreatedAtEnd() != null) {
                criteria.andCreatedAtLessThanOrEqualTo(
                        condition.getCreatedAtEnd().atTime(23, 59, 59));
            }

            // keyword：模糊查詢
            if (isNotBlank(condition.getKeyword())) {
                criteria.andTitleLike("%" + condition.getKeyword() + "%");
            }
        }

        // 排序
        if (isNotBlank(safeReq.getSortBy())) {
            String order = normalizeSortOrder(safeReq.getSortOrder(), "ASC");
            example.setOrderByClause(normalizeLotterySortColumn(safeReq.getSortBy()) + " " + order);
        } else {
            // ✅ Admin 查詢時預設按 store_id ASC, created_at DESC 排序
            // 這樣同一店家的商品會排在一起，最新的在前面
            if (condition == null || !isNotBlank(condition.getStoreId())) {
                example.setOrderByClause("store_id ASC, created_at DESC");
            } else {
                example.setOrderByClause("created_at DESC");
            }
        }

        long total = lotteryMapper.countByExample(example);
        if (total == 0) {
            return PageResult.empty(page, size);
        }

        // ✅ 查詢全部資料後在 service 層做分頁（維持既有 SQL 結構）
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);

        int offset = (page - 1) * size;
        int endIndex = Math.min(offset + size, lotteries.size());
        List<Lottery> pageLotteries = offset < lotteries.size()
                ? lotteries.subList(offset, endIndex)
                : new ArrayList<>();

        log.info("✅ 查詢成功: 共 {} 筆，本頁 {} 筆", total, pageLotteries.size());

        List<LotteryRes> results = pageLotteries.stream()
                .map(this::convertToResNew)
                .collect(Collectors.toList());

        if (condition != null && "ON_SHELF".equals(condition.getStatus())) {
            sortFrontendVisibleLotteries(results);
        }

        return PageResult.of(page, size, total, results);
    }

    /**
     * 新增商品（新架構）
     */
    @Override
    @Transactional
    public LotteryRes createLottery(LotteryCreateReq req) {
        log.info("➕ [新架構] 新增商品: {}", req.getTitle());

        if (req.getStoreId() == null) {
            throw new BusinessException("店家 ID 不能為空");
        }

        String normalizedSubCategory = normalizeSubCategory(req.getCategory(), req.getSubCategory());
        validateLotteryFieldUsage(
                req.getCategory(),
                normalizedSubCategory,
                req.getGameMode(),
                req.getFreeDrawThreshold(),
                req.getDesignatedPrizeNumbers());

        String normalizedTheme = upsertThemeIfPresent(req.getTheme());
        categoryService.validateTagNames(req.getTags());

        Lottery lottery = new Lottery();
        lottery.setId(UUID.randomUUID().toString());
        lottery.setStoreId(req.getStoreId());
        lottery.setTitle(req.getTitle());
        lottery.setDescription(req.getDescription());
        lottery.setCategory(req.getCategory());
        lottery.setSubCategory(normalizedSubCategory);

        // 自動推算 playMode：前端不需要傳，後端根據 category + subCategory 決定
        String resolvedPlayMode = resolvePlayMode(req.getPlayMode(), req.getCategory(), normalizedSubCategory);
        String resolvedGameMode = resolveGameMode(req.getCategory(), normalizedSubCategory, req.getGameMode());
        String resolvedDelistStrategy = resolveDelistStrategy(
                req.getCategory(),
                normalizedSubCategory,
                req.getDelistStrategy());
        Integer normalizedFreeDrawThreshold = normalizeFreeDrawThreshold(
                req.getCategory(),
                normalizedSubCategory,
                req.getFreeDrawThreshold());
        lottery.setPlayMode(resolvedPlayMode);
        lottery.setGameMode(resolvedGameMode);

        lottery.setStatus(
                resolveLifecycleStatus(req.getStatus(), req.getScheduledAt(), LotteryStatusEnum.OFF_SHELF.getCode()));
        lottery.setPricePerDraw(req.getPricePerDraw());
        lottery.setDiscountedPrice(req.getDiscountedPrice());
        lottery.setAutoDiscountEnabled(
                req.getAutoDiscountEnabled() != null && req.getAutoDiscountEnabled() ? (byte) 1 : (byte) 0);
        lottery.setAllowMultiDraw(null);
        lottery.setMultiDrawOptions(null);
        lottery.setMaxDraws(req.getMaxDraws());
        lottery.setTotalDraws(0);
        lottery.setImageUrl(req.getImageUrl());
        lottery.setOrderNum(req.getOrderNum());
        lottery.setRemark(req.getRemark());
        lottery.setHotCount(req.getHotCount() != null ? req.getHotCount() : 0);
        lottery.setTheme(normalizedTheme);
        lottery.setContent(req.getContent());
        lottery.setScheduledAt(req.getScheduledAt());
        lottery.setStartTime(req.getStartTime());
        lottery.setEndTime(req.getEndTime());
        lottery.setBonusEnabled(req.getBonusEnabled());
        lottery.setBonusPointsPerDraw(req.getBonusPointsPerDraw());
        lottery.setBonusCostPerDraw(req.getBonusCostPerDraw());
        lottery.setFreeDrawEnabled(Boolean.TRUE.equals(req.getFreeDrawEnabled()) ? (byte) 1 : (byte) 0);
        lottery.setProtectionDraws(req.getProtectionDraws());
        // T007: paymentType / delistStrategy defaults; freeDrawThreshold validation
        lottery.setPaymentType(normalizePaymentType(req.getPaymentType()));
        lottery.setDelistStrategy(resolvedDelistStrategy);
        lottery.setFreeDrawThreshold(normalizedFreeDrawThreshold);
        lottery.setDesignatedPrizeNumbers(
                sanitizeDesignatedPrizeNumbers(resolvedGameMode, req.getDesignatedPrizeNumbers()));
        if (req.getTags() != null) {
            lottery.setTags(serializeStringList(req.getTags(), "標籤"));
        }
        if (req.getGalleryImages() != null) {
            lottery.setGalleryImages(serializeStringList(req.getGalleryImages(), "圖庫"));
        }
        lottery.setCreatedAt(LocalDateTime.now());
        lottery.setUpdatedAt(LocalDateTime.now());

        lotteryMapper.insert(lottery);

        log.info("✅ 新增成功: id={}", lottery.getId());

        return convertToResNew(lottery);
    }

    /**
     * 更新商品（新架構）
     */
    @Override
    @Transactional
    public LotteryRes updateLottery(String id, LotteryUpdateReq req) {
        log.info("✏️ [新架構] 更新商品: id={}", id);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(id);
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }

        if (req.getTags() != null) {
            categoryService.validateTagNames(req.getTags());
        }

        String currentCategory = req.getCategory() != null ? req.getCategory() : lottery.getCategory();
        String currentSubCategory = req.getSubCategory() != null ? req.getSubCategory() : lottery.getSubCategory();
        String normalizedSubCategory = normalizeSubCategory(currentCategory, currentSubCategory);
        String resolvedPlayMode = resolvePlayMode(req.getPlayMode(), currentCategory, normalizedSubCategory);
        String requestGameMode = req.getGameMode() != null ? req.getGameMode() : lottery.getGameMode();
        String resolvedGameMode = resolveGameMode(currentCategory, normalizedSubCategory, requestGameMode);
        String requestDelistStrategy = req.getDelistStrategy() != null ? req.getDelistStrategy()
                : lottery.getDelistStrategy();
        String resolvedDelistStrategy = resolveDelistStrategy(currentCategory, normalizedSubCategory,
                requestDelistStrategy);
        Integer requestFreeDrawThreshold = req.getFreeDrawThreshold() != null
                ? req.getFreeDrawThreshold()
                : lottery.getFreeDrawThreshold();
        Integer normalizedFreeDrawThreshold = normalizeFreeDrawThreshold(
                currentCategory,
                normalizedSubCategory,
                requestFreeDrawThreshold);
        String requestDesignatedPrizeNumbers = req.getDesignatedPrizeNumbers() != null
                ? req.getDesignatedPrizeNumbers()
                : lottery.getDesignatedPrizeNumbers();

        validateLotteryFieldUsage(
                currentCategory,
                normalizedSubCategory,
                req.getGameMode(),
                req.getFreeDrawThreshold(),
                req.getDesignatedPrizeNumbers());

        // 更新欄位（所有欄位都是可選的）
        if (req.getTitle() != null) {
            lottery.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            lottery.setDescription(req.getDescription());
        }
        if (req.getImageUrl() != null) {
            lottery.setImageUrl(req.getImageUrl());
        }
        if (req.getCategory() != null) {
            lottery.setCategory(req.getCategory());
        }
        lottery.setSubCategory(normalizedSubCategory);
        lottery.setPlayMode(resolvedPlayMode);
        lottery.setGameMode(resolvedGameMode);
        if (req.getPricePerDraw() != null) {
            lottery.setPricePerDraw(req.getPricePerDraw());
        }
        if (req.getDiscountedPrice() != null) {
            lottery.setDiscountedPrice(req.getDiscountedPrice());
        }
        if (req.getAutoDiscountEnabled() != null) {
            lottery.setAutoDiscountEnabled(req.getAutoDiscountEnabled() ? (byte) 1 : (byte) 0);
        }
        if (req.getAllowMultiDraw() != null) {
            lottery.setAllowMultiDraw(null);
        }
        if (req.getMultiDrawOptions() != null) {
            lottery.setMultiDrawOptions(null);
        }
        if (req.getBonusEnabled() != null) {
            lottery.setBonusEnabled(req.getBonusEnabled());
        }
        if (req.getBonusPointsPerDraw() != null) {
            lottery.setBonusPointsPerDraw(req.getBonusPointsPerDraw());
        }
        if (req.getBonusCostPerDraw() != null) {
            lottery.setBonusCostPerDraw(req.getBonusCostPerDraw());
        }
        if (req.getMaxDraws() != null) {
            lottery.setMaxDraws(req.getMaxDraws());
        }
        if (req.getOrderNum() != null) {
            lottery.setOrderNum(req.getOrderNum());
        }
        if (req.getWeight() != null) {
            lottery.setWeight(req.getWeight());
        }
        if (req.getRemark() != null) {
            lottery.setRemark(req.getRemark());
        }
        if (req.getTheme() != null) {
            lottery.setTheme(upsertThemeIfPresent(req.getTheme()));
        }
        if (req.getContent() != null) {
            lottery.setContent(req.getContent());
        }
        if (req.getHotCount() != null) {
            lottery.setHotCount(req.getHotCount());
        }
        if (req.getStatus() != null || req.getScheduledAt() != null) {
            LocalDateTime targetScheduledAt = req.getScheduledAt() != null ? req.getScheduledAt()
                    : lottery.getScheduledAt();
            lottery.setStatus(resolveLifecycleStatus(req.getStatus(), targetScheduledAt, lottery.getStatus()));
        }
        if (req.getScheduledAt() != null) {
            lottery.setScheduledAt(req.getScheduledAt());
        }
        if (req.getStartTime() != null) {
            lottery.setStartTime(req.getStartTime());
        }
        if (req.getEndTime() != null) {
            lottery.setEndTime(req.getEndTime());
        }
        if (req.getTags() != null) {
            lottery.setTags(serializeStringList(req.getTags(), "標籤"));
        }
        if (req.getGalleryImages() != null) {
            lottery.setGalleryImages(serializeStringList(req.getGalleryImages(), "圖庫"));
        }
        // T008: new fields update
        if (req.getPaymentType() != null) {
            String normalizedPaymentType = normalizePaymentType(req.getPaymentType());
            // DB 舊資料 paymentType 可能為 null，視為預設值 "GOLD"
            String existingPaymentType = lottery.getPaymentType() != null ? lottery.getPaymentType() : "GOLD";
            if (!LotteryStatusEnum.DRAFT.getCode().equals(lottery.getStatus())
                    && !normalizedPaymentType.equals(existingPaymentType)) {
                throw new BusinessException("非草稿商品不可修改 paymentType");
            }
            lottery.setPaymentType(normalizedPaymentType);
        }
        if (req.getFreeDrawEnabled() != null) {
            lottery.setFreeDrawEnabled(req.getFreeDrawEnabled() ? (byte) 1 : (byte) 0);
        }
        if (req.getProtectionDraws() != null) {
            lottery.setProtectionDraws(req.getProtectionDraws());
        }
        lottery.setFreeDrawThreshold(normalizedFreeDrawThreshold);
        lottery.setDelistStrategy(resolvedDelistStrategy);
        // 🆕 SCRATCH_STORE 大獎指定號碼：若有變更則同步清除籤位，待重新生成
        if (req.getDesignatedPrizeNumbers() != null) {
            String currentDesignated = lottery.getDesignatedPrizeNumbers();
            if (!req.getDesignatedPrizeNumbers().equals(currentDesignated)
                    && lottery.getTicketsGenerated() != null && lottery.getTicketsGenerated() == 1) {
                LotteryTicketExample ticketEx = new LotteryTicketExample();
                ticketEx.createCriteria().andLotteryIdEqualTo(id);
                lotteryTicketMapper.deleteByExample(ticketEx);
                lottery.setTicketsGenerated((byte) 0);
                log.info("🔄 designatedPrizeNumbers 變更，籤位已清除，待重新生成: lotteryId={}", id);
            }
            lottery.setDesignatedPrizeNumbers(req.getDesignatedPrizeNumbers());
        }

        lottery.setUpdatedAt(LocalDateTime.now());

        lotteryMapper.updateByPrimaryKeyWithBLOBs(lottery);

        log.info("✅ 更新成功");

        Lottery refreshedLottery = lotteryMapper.selectByPrimaryKey(id);
        if (refreshedLottery == null) {
            throw new BusinessException("商品更新後查詢失敗");
        }

        return convertToResNew(refreshedLottery);
    }

    /**
     * 刪除商品（新架構）
     */
    @Override
    @Transactional
    public void deleteLottery(String id) {
        log.info("🗑️ [新架構] 刪除商品: id={}", id);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(id);
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }

        // 只能刪除下架的商品
        if ("ON_SHELF".equals(lottery.getStatus())) {
            throw new BusinessException("無法刪除已上架的商品");
        }

        lotteryMapper.deleteByPrimaryKey(id);

        log.info("✅ 刪除成功");
    }

    /**
     * 取得商品詳情（新架構）
     */
    @Override
    public LotteryRes getLottery(String id) {
        log.info("🔍 [新架構] 查詢商品詳情: id={}", id);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(id);
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }

        return convertToResNew(lottery);
    }

    /**
     * 更新商品狀態（新架構）
     */
    @Override
    @Transactional
    public LotteryRes updateStatus(String id, String status) {
        log.info("🔄 [新架構] 更新商品狀態: id={}, status={}", id, status);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(id);
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }

        // 🆕 上架前置驗證（先驗證，再寫入 DB，避免無效的狀態變更）
        if ("ON_SHELF".equals(status)) {
            validateCanGoOnShelf(lottery);
        }

        lottery.setStatus(status);
        lottery.setUpdatedAt(LocalDateTime.now());

        lotteryMapper.updateByPrimaryKey(lottery);

        // 🆕 上架時自動生成籤位（若尚未生成）
        if ("ON_SHELF".equals(status)) {
            ensureTicketsGeneratedForOnShelf(lottery);
        }

        log.info("✅ 狀態更新成功");

        return convertToResNew(lottery);
    }

    /**
     * 轉換為 Res（新架構完整版）
     */
    private LotteryRes convertToResNew(Lottery lottery) {
        LotteryRes res = new LotteryRes();

        // 基本資訊
        res.setId(lottery.getId());
        res.setStoreId(lottery.getStoreId());

        // ✅ 查詢店家名稱
        if (lottery.getStoreId() != null) {
            Store store = storeMapper.selectByPrimaryKey(lottery.getStoreId());
            if (store != null) {
                res.setStoreName(store.getStoreName());
            }
        }

        res.setTitle(lottery.getTitle());
        res.setDescription(lottery.getDescription());
        res.setImageUrl(lottery.getImageUrl());

        // ✅ 分類資訊（加上中文名稱）
        res.setCategory(lottery.getCategory());
        res.setCategoryName(LotteryCategoryEnum.getNameByCode(lottery.getCategory()));
        res.setSubCategory(lottery.getSubCategory());
        res.setSubCategoryName(LotterySubCategoryEnum.getNameByCode(lottery.getSubCategory())); // ✅ 新增子分類中文名稱
        res.setPlayMode(lottery.getPlayMode());

        // 價格相關
        res.setPricePerDraw(lottery.getPricePerDraw());
        res.setCurrentPrice(lottery.getPricePerDraw()); // ✅ 加上當前價格
        res.setDiscountedPrice(lottery.getDiscountedPrice());
        res.setAutoDiscountEnabled(lottery.getAutoDiscountEnabled() != null && lottery.getAutoDiscountEnabled() == 1);
        res.setDiscountTriggered(false); // 需要額外計算邏輯

        // 多抽選項
        res.setAllowMultiDraw(lottery.getAllowMultiDraw() != null && lottery.getAllowMultiDraw() == 1);
        if (lottery.getMultiDrawOptions() != null && !lottery.getMultiDrawOptions().isEmpty()) {
            try {
                // Support both JSON array format "[10,20]" and comma-separated "10,20"
                String raw = lottery.getMultiDrawOptions().trim();
                if (raw.startsWith("[")) {
                    raw = raw.substring(1, raw.length() - 1).trim();
                }
                if (raw.isEmpty()) {
                    res.setMultiDrawOptions(List.of());
                } else {
                    List<Integer> options = Arrays.stream(raw.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                    res.setMultiDrawOptions(options);
                }
            } catch (Exception e) {
                log.warn("⚠️ 解析多抽選項失敗: {}", lottery.getMultiDrawOptions());
                res.setMultiDrawOptions(List.of());
            }
        } else {
            res.setMultiDrawOptions(List.of());
        }

        // 紅利相關
        res.setBonusEnabled(lottery.getBonusEnabled());
        res.setBonusPointsPerDraw(lottery.getBonusPointsPerDraw());
        res.setBonusCostPerDraw(lottery.getBonusCostPerDraw());

        // 標籤與圖庫
        if (lottery.getTags() != null && !lottery.getTags().isEmpty()) {
            res.setTags(parseStringList(lottery.getTags(), "標籤"));
        } else {
            res.setTags(List.of());
        }

        if (lottery.getGalleryImages() != null && !lottery.getGalleryImages().isEmpty()) {
            res.setGalleryImages(parseStringList(lottery.getGalleryImages(), "圖庫"));
        } else {
            res.setGalleryImages(List.of());
        }

        res.setTheme(lottery.getTheme());
        res.setHotCount(lottery.getHotCount() != null ? lottery.getHotCount() : 0);

        // 時間相關
        res.setScheduledAt(lottery.getScheduledAt());
        res.setStartTime(lottery.getStartTime());
        res.setEndTime(lottery.getEndTime());

        // 抽數統計
        res.setTotalDraws(lottery.getTotalDraws() != null ? lottery.getTotalDraws() : 0);
        res.setMaxDraws(lottery.getMaxDraws() != null ? lottery.getMaxDraws() : 0);
        res.setRemainingDraws(calculateRemainingDraws(lottery));

        // ✅ 狀態與排序（加上中文名稱）
        res.setStatus(lottery.getStatus());
        res.setStatusName(LotteryStatusEnum.getNameByCode(lottery.getStatus()));
        res.setOrderNum(lottery.getOrderNum() != null ? lottery.getOrderNum() : 0);
        res.setWeight(lottery.getWeight() != null ? lottery.getWeight() : 0);

        // 系統欄位
        res.setCreatedBy(lottery.getCreatedBy());
        res.setCreatedAt(lottery.getCreatedAt());
        res.setUpdatedAt(lottery.getUpdatedAt());
        res.setRemark(lottery.getRemark());

        // ✅ 獎項統計
        res.setTotalPrizes(sumQuantityByLotteryId(lottery.getId()));
        res.setRemainingPrizes(sumRemainingByLotteryId(lottery.getId()));

        // ✅ 新增欄位（前台商品詳情需要）
        res.setProtectionDraws(lottery.getProtectionDraws());
        res.setProtectionMinutes(lottery.getProtectionMinutes());
        res.setContent(lottery.getContent());
        res.setGameMode(lottery.getGameMode());
        res.setFreeDrawEnabled(lottery.getFreeDrawEnabled() != null && lottery.getFreeDrawEnabled() == 1);
        res.setDesignatedPrizeNumbers(lottery.getDesignatedPrizeNumbers());
        res.setTicketsGenerated(lottery.getTicketsGenerated() != null && lottery.getTicketsGenerated() == 1);
        // T009: new fields
        res.setPaymentType(isNotBlank(lottery.getPaymentType()) ? lottery.getPaymentType() : "GOLD");
        res.setFreeDrawThreshold(sanitizeFreeDrawThresholdForResponse(
                lottery.getCategory(),
                lottery.getSubCategory(),
                lottery.getFreeDrawThreshold()));
        res.setDelistStrategy(isNotBlank(lottery.getDelistStrategy()) ? lottery.getDelistStrategy() : "MANUAL");

        return res;
    }

    /**
     * 計算剩餘抽數
     */
    private Integer calculateRemainingDraws(Lottery lottery) {
        if (lottery.getMaxDraws() == null || lottery.getMaxDraws() == 0) {
            return 0; // 無限制或未設定
        }
        int totalDraws = lottery.getTotalDraws() != null ? lottery.getTotalDraws() : 0;
        return Math.max(0, lottery.getMaxDraws() - totalDraws);
    }

    private void sortFrontendVisibleLotteries(List<LotteryRes> lotteries) {
        Map<String, Integer> grandPrizeRemainingCache = new HashMap<>();
        lotteries.sort(
                Comparator
                        .comparingInt(
                                (LotteryRes lottery) -> frontendDisplayPriority(lottery, grandPrizeRemainingCache))
                        .thenComparing(LotteryRes::getOrderNum, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(LotteryRes::getHotCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(LotteryRes::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
    }

    private int frontendDisplayPriority(LotteryRes lottery, Map<String, Integer> grandPrizeRemainingCache) {
        boolean onShelf = "ON_SHELF".equals(lottery.getStatus());
        boolean soldOut = lottery.getRemainingDraws() != null && lottery.getRemainingDraws() <= 0;
        int grandPrizeRemaining = grandPrizeRemainingCache.computeIfAbsent(
                lottery.getId(),
                this::countGrandPrizeRemaining);
        boolean grandPrizeSoldOut = grandPrizeRemaining <= 0 && hasAnyGrandPrizeConfigured(lottery.getId());

        if (onShelf && !soldOut && !grandPrizeSoldOut) {
            return 0;
        }
        if (onShelf && !soldOut) {
            return 1;
        }
        if (onShelf) {
            return 2;
        }
        return 3;
    }

    private boolean hasAnyGrandPrizeConfigured(String lotteryId) {
        LotteryPrizeExample example = new LotteryPrizeExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andIsGrandPrizeEqualTo((byte) 1);
        return lotteryPrizeMapper.countByExample(example) > 0;
    }

    /**
     * 根據 category + subCategory 自動推算 playMode
     *
     * <pre>
     * category                    subCategory      → playMode
     * GACHA                       -                → LOTTERY_MODE
     * OFFICIAL_ICHIBAN            -                → LOTTERY_MODE
     * TRADING_CARD                -                → LOTTERY_MODE
     * CUSTOM_GACHA                SCRATCH_MODE     → SCRATCH_MODE
     * CUSTOM_GACHA                LOTTERY_MODE     → LOTTERY_MODE
     * CUSTOM_GACHA                (null)           → LOTTERY_MODE
     * SCRATCH（舊值，已棄用）      -                → SCRATCH_MODE
     * </pre>
     *
     * @param explicit    前端傳入值（僅保留參數相容，實際上不採用）
     * @param category    商品大分類
     * @param subCategory 自製賞子類型
     * @return 推算後的 playMode
     */
    private String resolvePlayMode(String explicit, String category, String subCategory) {
        if (category == null) {
            return "LOTTERY_MODE";
        }
        return switch (category) {
            case "GACHA", "OFFICIAL_ICHIBAN", "TRADING_CARD" -> "LOTTERY_MODE";
            case "CUSTOM_GACHA" -> "SCRATCH_MODE".equals(subCategory) ? "SCRATCH_MODE" : "LOTTERY_MODE";
            case "SCRATCH" -> "SCRATCH_MODE"; // 舊資料相容
            default -> "LOTTERY_MODE";
        };
    }

    private String normalizeSubCategory(String category, String subCategory) {
        if (!"CUSTOM_GACHA".equals(category)) {
            if (isNotBlank(subCategory)) {
                throw new BusinessException("只有 CUSTOM_GACHA 可設定 subCategory");
            }
            return null;
        }

        if (!isNotBlank(subCategory)) {
            throw new BusinessException("CUSTOM_GACHA 必須設定 subCategory");
        }

        LotterySubCategoryEnum subCategoryEnum = LotterySubCategoryEnum.fromCode(subCategory);
        if (subCategoryEnum == null) {
            throw new BusinessException("CUSTOM_GACHA 的 subCategory 僅允許 LOTTERY_MODE/SCRATCH_MODE");
        }

        return switch (subCategoryEnum) {
            case LOTTERY_MODE, SCRATCH_MODE -> subCategoryEnum.getCode();
            default -> throw new BusinessException("目前僅支援 CUSTOM_GACHA 的 LOTTERY_MODE/SCRATCH_MODE");
        };
    }

    private void validateLotteryFieldUsage(
            String category,
            String subCategory,
            String requestGameMode,
            Integer requestFreeDrawThreshold,
            String requestDesignatedPrizeNumbers) {
        boolean isScratchMode = isScratchMode(category, subCategory);

        if (!isScratchMode && isNotBlank(requestGameMode)) {
            throw new BusinessException("只有 CUSTOM_GACHA 的 SCRATCH_MODE 可手動設定 gameMode");
        }

        if (!isScratchMode && requestFreeDrawThreshold != null) {
            throw new BusinessException("freeDrawThreshold 僅限 CUSTOM_GACHA 的 SCRATCH_MODE 使用");
        }

        if (isNotBlank(requestDesignatedPrizeNumbers)) {
            if (!isScratchMode) {
                throw new BusinessException("designatedPrizeNumbers 僅限刮刮樂商品使用");
            }
            if (!GameModeEnum.SCRATCH_STORE.getCode().equals(requestGameMode)) {
                throw new BusinessException("designatedPrizeNumbers 僅限 SCRATCH_STORE 模式設定");
            }
        }
    }

    private boolean isScratchMode(String category, String subCategory) {
        return "CUSTOM_GACHA".equals(category) && LotterySubCategoryEnum.SCRATCH_MODE.getCode().equals(subCategory);
    }

    private String sanitizeDesignatedPrizeNumbers(String resolvedGameMode, String designatedPrizeNumbers) {
        if (!GameModeEnum.SCRATCH_STORE.getCode().equals(resolvedGameMode)) {
            return null;
        }
        return isNotBlank(designatedPrizeNumbers) ? designatedPrizeNumbers : null;
    }

    private String resolveGameMode(String category, String subCategory, String requestGameMode) {
        if ("OFFICIAL_ICHIBAN".equals(category) || "TRADING_CARD".equals(category)) {
            return "TICKET";
        }
        if ("GACHA".equals(category)) {
            return "RANDOM";
        }
        if ("CUSTOM_GACHA".equals(category)) {
            if ("SCRATCH_MODE".equals(subCategory)) {
                if (!isNotBlank(requestGameMode)) {
                    throw new BusinessException("刮刮樂商品必須設定 gameMode");
                }
                if (!List.of("SCRATCH_STORE", "SCRATCH_PLAYER").contains(requestGameMode)) {
                    throw new BusinessException("刮刮樂 gameMode 僅允許 SCRATCH_STORE/SCRATCH_PLAYER");
                }
                return requestGameMode;
            }
            return null;
        }
        throw new BusinessException("不支援的商品分類: " + category);
    }

    private String resolveDelistStrategy(String category, String subCategory, String requestDelistStrategy) {
        if ("OFFICIAL_ICHIBAN".equals(category)) {
            if (!isNotBlank(requestDelistStrategy)) {
                throw new BusinessException("OFFICIAL_ICHIBAN 必須設定 delistStrategy");
            }
            if (!List.of("GRAND_PRIZE_DRAWN", "ALL_DRAWN", "MANUAL").contains(requestDelistStrategy)) {
                throw new BusinessException("delistStrategy 僅允許 GRAND_PRIZE_DRAWN/ALL_DRAWN/MANUAL");
            }
            return requestDelistStrategy;
        }
        if ("TRADING_CARD".equals(category) || "GACHA".equals(category)) {
            return "ALL_DRAWN";
        }
        if ("CUSTOM_GACHA".equals(category)) {
            return "SCRATCH_MODE".equals(subCategory) ? "GRAND_PRIZE_DRAWN" : "ALL_DRAWN";
        }
        throw new BusinessException("不支援的商品分類: " + category);
    }

    private Integer normalizeFreeDrawThreshold(String category, String subCategory, Integer threshold) {
        if (!"CUSTOM_GACHA".equals(category) || !"SCRATCH_MODE".equals(subCategory)) {
            return null;
        }
        if (threshold == null) {
            return null;
        }
        if (threshold < 1) {
            throw new BusinessException("免費抽門檻必須大於或等於 1");
        }
        return threshold;
    }

    private Integer sanitizeFreeDrawThresholdForResponse(String category, String subCategory, Integer threshold) {
        if (!"CUSTOM_GACHA".equals(category) || !"SCRATCH_MODE".equals(subCategory)) {
            return null;
        }
        if (threshold == null || threshold < 1) {
            return null;
        }
        return threshold;
    }

    private String normalizePaymentType(String paymentType) {
        if (!isNotBlank(paymentType)) {
            return "GOLD";
        }
        if (!List.of("GOLD", "BONUS").contains(paymentType)) {
            throw new BusinessException("paymentType 僅允許 GOLD/BONUS");
        }
        return paymentType;
    }

    private String serializeStringList(List<String> values, String fieldName) {
        if (values == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            log.warn("{}序列化失敗", fieldName, e);
            return "[]";
        }
    }

    private List<String> parseStringList(String raw, String fieldName) {
        if (!isNotBlank(raw)) {
            return List.of();
        }

        try {
            return objectMapper.readValue(raw,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception jsonEx) {
            List<String> parsed = Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(this::isNotBlank)
                    .collect(Collectors.toList());
            if (!parsed.isEmpty()) {
                return parsed;
            }
            log.warn("⚠️ 解析{}失敗: {}", fieldName, raw, jsonEx);
            return List.of();
        }
    }

    // ==================== 複製商品功能 ====================

    @Override
    @Transactional
    public LotteryRes copyLottery(String sourceLotteryId, String newTitle, Boolean regenerateTickets,
            String newStatus) {
        log.info("🔄 開始複製商品: sourceLotteryId={}, newTitle={}, regenerateTickets={}, newStatus={}",
                sourceLotteryId, newTitle, regenerateTickets, newStatus);

        // 1. 查詢來源商品
        Lottery sourceLottery = lotteryMapper.selectByPrimaryKey(sourceLotteryId);
        if (sourceLottery == null) {
            log.error("❌ 來源商品不存在: sourceLotteryId={}", sourceLotteryId);
            throw new BusinessException("LOTTERY_NOT_FOUND", "來源商品不存在");
        }

        // 2. 複製 Lottery 主表
        Lottery newLottery = new Lottery();
        String newLotteryId = UUID.randomUUID().toString();

        newLottery.setId(newLotteryId);
        newLottery.setStoreId(sourceLottery.getStoreId());

        // 標題處理：若沒提供新標題，自動加上「（複製）」後綴
        if (newTitle != null && !newTitle.isEmpty()) {
            newLottery.setTitle(newTitle);
        } else {
            newLottery.setTitle(sourceLottery.getTitle() + "（複製）");
        }

        newLottery.setImageUrl(sourceLottery.getImageUrl());
        newLottery.setCategory(sourceLottery.getCategory());
        newLottery.setSubCategory(sourceLottery.getSubCategory());
        newLottery.setGameMode(sourceLottery.getGameMode());
        newLottery.setPricePerDraw(sourceLottery.getPricePerDraw());
        newLottery.setDiscountedPrice(sourceLottery.getDiscountedPrice());
        newLottery.setAutoDiscountEnabled(sourceLottery.getAutoDiscountEnabled());
        newLottery.setAllowMultiDraw(null);
        newLottery.setMultiDrawOptions(null);
        newLottery.setScheduledAt(sourceLottery.getScheduledAt());
        newLottery.setStartTime(sourceLottery.getStartTime());
        newLottery.setEndTime(sourceLottery.getEndTime());

        // ✅ 抽數相關：重置為 0
        newLottery.setTotalDraws(0);
        newLottery.setMaxDraws(sourceLottery.getMaxDraws());
        newLottery.setProtectionDraws(null);
        newLottery.setProtectionMinutes(null);
        newLottery.setFreeDrawEnabled(sourceLottery.getFreeDrawEnabled());
        newLottery.setPaymentType(isNotBlank(sourceLottery.getPaymentType()) ? sourceLottery.getPaymentType() : "GOLD");
        newLottery.setFreeDrawThreshold(sanitizeFreeDrawThresholdForResponse(
                sourceLottery.getCategory(),
                sourceLottery.getSubCategory(),
                sourceLottery.getFreeDrawThreshold()));
        newLottery.setDelistStrategy(
                isNotBlank(sourceLottery.getDelistStrategy()) ? sourceLottery.getDelistStrategy() : "MANUAL");
        newLottery.setDesignatedPrizeNumbers(sourceLottery.getDesignatedPrizeNumbers());

        // ✅ 籤號生成標記：根據參數決定
        if (regenerateTickets != null && regenerateTickets) {
            newLottery.setTicketsGenerated((byte) 0); // 需要重新生成
        } else {
            newLottery.setTicketsGenerated(sourceLottery.getTicketsGenerated());
        }

        // ✅ 狀態：預設 OFF_SHELF，避免立即上架
        if (newStatus != null && !newStatus.isEmpty()) {
            newLottery.setStatus(newStatus);
        } else {
            newLottery.setStatus("OFF_SHELF");
        }

        newLottery.setOrderNum(sourceLottery.getOrderNum());
        newLottery.setWeight(sourceLottery.getWeight());
        newLottery.setCreatedBy(sourceLottery.getCreatedBy());
        newLottery.setCreatedAt(LocalDateTime.now());
        newLottery.setUpdatedAt(LocalDateTime.now());
        newLottery.setDescription(sourceLottery.getDescription());
        newLottery.setRemark("複製自商品：" + sourceLottery.getTitle());

        lotteryMapper.insert(newLottery);
        log.info("✅ Lottery 主表複製完成: newLotteryId={}", newLotteryId);

        // 3. 複製所有 LotteryPrize（獎項）
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria().andLotteryIdEqualTo(sourceLotteryId);
        List<LotteryPrize> sourcePrizes = lotteryPrizeMapper.selectByExample(prizeExample);

        if (sourcePrizes != null && !sourcePrizes.isEmpty()) {
            for (LotteryPrize sourcePrize : sourcePrizes) {
                LotteryPrize newPrize = new LotteryPrize();

                newPrize.setId(UUID.randomUUID().toString());
                newPrize.setLotteryId(newLotteryId); // ✅ 關聯到新商品
                newPrize.setName(sourcePrize.getName());
                newPrize.setImageUrl(sourcePrize.getImageUrl());
                newPrize.setLevel(sourcePrize.getLevel());
                newPrize.setPrizeNumber(sourcePrize.getPrizeNumber());
                newPrize.setQuantity(sourcePrize.getQuantity());
                newPrize.setRemaining(sourcePrize.getQuantity()); // ✅ 重置為原始數量
                newPrize.setWeight(sourcePrize.getWeight());
                newPrize.setPrizeType(sourcePrize.getPrizeType());
                newPrize.setPointValue(sourcePrize.getPointValue());
                newPrize.setIsLastPrize(sourcePrize.getIsLastPrize());
                newPrize.setIsGrandPrize(sourcePrize.getIsGrandPrize());
                newPrize.setOrderNum(sourcePrize.getOrderNum());
                newPrize.setCreatedAt(LocalDateTime.now());
                newPrize.setUpdatedAt(LocalDateTime.now());
                newPrize.setDescription(sourcePrize.getDescription());

                lotteryPrizeMapper.insert(newPrize);
            }

            log.info("✅ LotteryPrize 複製完成: 共複製 {} 個獎項", sourcePrizes.size());
        } else {
            log.warn("⚠️ 來源商品沒有獎項資料");
        }

        // 4. 如果需要重新生成籤號，這裡可以呼叫籤號生成邏輯
        // （預留，目前先不實作，等待籤號系統完整後再補）
        if (regenerateTickets != null && regenerateTickets) {
            log.info("ℹ️ 需要重新生成籤號（待實作）");
            // TODO: 呼叫 LotteryTicketService.generateTickets(newLotteryId)
        }

        log.info("🎉 商品複製完成: newLotteryId={}, newTitle={}", newLotteryId, newLottery.getTitle());

        return convertToResNew(newLottery);
    }

    // ==================== 整合 API（商品+獎品一起操作）====================

    @Override
    @Transactional
    public com.group.admin.res.lottery.LotteryWithPrizesRes createLotteryWithPrizes(
            com.group.admin.req.lottery.LotteryWithPrizesCreateReq req,
            String operatorId) {

        if (req.getPrizes() == null || req.getPrizes().isEmpty()) {
            throw new BusinessException("整合建立商品時，必須至少帶 1 筆獎品資料");
        }

        log.info("📦 建立商品與獎品: title={}, prizeCount={}, operatorId={}",
                req.getLottery().getTitle(),
                req.getPrizes() != null ? req.getPrizes().size() : 0,
                operatorId);

        // Step 1: 建立商品
        LotteryRes lotteryRes = createLottery(req.getLottery(), operatorId);
        String lotteryId = lotteryRes.getId();

        log.info("✅ 商品建立成功: lotteryId={}", lotteryId);

        // Step 2: 批次建立獎品
        List<com.group.admin.res.lottery.LotteryPrizeRes> prizeResList = new ArrayList<>();
        log.info("🎁 開始批次新增獎品: count={}", req.getPrizes().size());

        // 🆕 刮刮樂模式：強制驗證必須且只能有 1 個大獎，且 quantity=1
        String reqGameMode = req.getLottery().getGameMode();
        if (GameModeEnum.SCRATCH_STORE.getCode().equals(reqGameMode)
                || GameModeEnum.SCRATCH_PLAYER.getCode().equals(reqGameMode)) {
            validateScratchPrizes(reqGameMode, req.getPrizes());
        }

        for (com.group.admin.req.lottery.LotteryPrizeCreateReq prizeReq : req.getPrizes()) {
            // 設定 lotteryId
            prizeReq.setLotteryId(lotteryId);

            // 建立獎品
            LotteryPrize prize = new LotteryPrize();
            prize.setId(UUID.randomUUID().toString());
            prize.setLotteryId(lotteryId);
            prize.setName(prizeReq.getName());
            prize.setDescription(prizeReq.getDescription());
            prize.setContent(prizeReq.getContent());
            prize.setImageUrl(prizeReq.getImageUrl());
            prize.setLevel(prizeReq.getLevel());
            prize.setPrizeNumber(prizeReq.getPrizeNumber());
            prize.setQuantity(prizeReq.getQuantity());
            prize.setRemaining(prizeReq.getQuantity()); // 初始剩餘 = 總數量
            // ✅ 不再設定 weight，使用 1/剩餘數量 機率
            prize.setPrizeType(prizeReq.getPrizeType());
            prize.setPointValue(prizeReq.getPointValue());
            prize.setIsLastPrize(prizeReq.getIsLastPrize() != null && prizeReq.getIsLastPrize() ? (byte) 1 : (byte) 0);
            prize.setIsGrandPrize(
                    prizeReq.getIsGrandPrize() != null && prizeReq.getIsGrandPrize() ? (byte) 1 : (byte) 0);
            prize.setCreatedAt(LocalDateTime.now());
            prize.setUpdatedAt(LocalDateTime.now());

            lotteryPrizeMapper.insert(prize);

            // 轉換為 Res
            prizeResList.add(convertPrizeToRes(prize));
        }

        log.info("✅ 獎品批次新增完成: count={}", prizeResList.size());

        // Step 2.5: 🆕 自動計算 maxDraws 並更新商品
        // 計算非最後賞獎品總數（最後賞不加入籤位池）
        int totalPrizeQuantity = req.getPrizes().stream()
                .filter(p -> p.getIsLastPrize() == null || !p.getIsLastPrize())
                .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                .sum();
        int lastPrizeQuantity = (int) req.getPrizes().stream()
                .filter(p -> p.getIsLastPrize() != null && p.getIsLastPrize())
                .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                .sum();

        // 使用與 createLottery 相同的推算邏輯（前端不傳 playMode，需自動推算）
        String playMode = resolvePlayMode(
                req.getLottery().getPlayMode(),
                req.getLottery().getCategory(),
                req.getLottery().getSubCategory());

        log.info("🎰 籤位生成準備: playMode={}, 非最後賞獎品總數={}, 最後賞數量={}",
                playMode, totalPrizeQuantity, lastPrizeQuantity);

        // ✅ 檢查獎品總數（排除最後賞後仍需有獎品）
        if (totalPrizeQuantity <= 0) {
            String errorMsg = "非最後賞獎品總數必須大於 0！請至少新增一個非最後賞獎品。";
            log.error("❌ {}", errorMsg);
            throw new BusinessException(errorMsg);
        }

        // 🆕 後端自動計算 maxDraws（根據遊戲模式）
        int calculatedMaxDraws;
        Integer frontendMaxDraws = req.getLottery().getMaxDraws();

        if ("LOTTERY_MODE".equals(playMode)) {
            // 一番賞/扭蛋/卡牌：maxDraws = 非最後賞獎品總數
            calculatedMaxDraws = totalPrizeQuantity;
            log.info("🎯 一番賞模式：自動設定 maxDraws = 非最後賞獎品總數 = {}", calculatedMaxDraws);

            // 如果前端傳入的 maxDraws 與計算值不符，警告並覆寫
            if (frontendMaxDraws != null && frontendMaxDraws != totalPrizeQuantity) {
                log.warn("⚠️ 一番賞模式：前端傳入 maxDraws={} 與非最後賞獎品總數={} 不符，已自動覆寫",
                        frontendMaxDraws, totalPrizeQuantity);
            }
        } else if ("SCRATCH_MODE".equals(playMode)) {
            // 刮刮樂：使用前端傳入的 maxDraws（支援謝謝惠顧）
            if (frontendMaxDraws != null && frontendMaxDraws >= totalPrizeQuantity) {
                calculatedMaxDraws = frontendMaxDraws;
                int thanksgivingCount = frontendMaxDraws - totalPrizeQuantity;
                log.info("🎰 刮刮樂模式：使用前端設定 maxDraws = {}（獎品 {} 個 + 謝謝惠顧 {} 個）",
                        calculatedMaxDraws, totalPrizeQuantity, thanksgivingCount);
            } else if (frontendMaxDraws != null && frontendMaxDraws < totalPrizeQuantity) {
                // 前端設定的 maxDraws 小於獎品總數，不合理，拋出錯誤
                String errorMsg = String.format(
                        "刮刮樂模式錯誤：總抽數(%d)不能小於獎品總數(%d)！請調整設定。",
                        frontendMaxDraws, totalPrizeQuantity);
                log.error("❌ {}", errorMsg);
                throw new BusinessException(errorMsg);
            } else {
                // 前端未設定 maxDraws，預設為獎品總數（沒有謝謝惠顧）
                calculatedMaxDraws = totalPrizeQuantity;
                log.info("🎰 刮刮樂模式：前端未設定 maxDraws，預設 = 獎品總數 = {}（無謝謝惠顧）", calculatedMaxDraws);
            }
        } else {
            // 未知模式，預設為獎品總數
            calculatedMaxDraws = totalPrizeQuantity;
            log.warn("⚠️ 未知遊戲模式: {}，預設設定 maxDraws = 獎品總數 = {}", playMode, calculatedMaxDraws);
        }

        // 🆕 更新商品的 maxDraws（覆寫前端傳入的值）
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery != null) {
            lottery.setMaxDraws(calculatedMaxDraws);
            lottery.setUpdatedAt(LocalDateTime.now());
            lotteryMapper.updateByPrimaryKey(lottery);
            log.info("✅ 已更新商品 maxDraws: lotteryId={}, maxDraws={}", lotteryId, calculatedMaxDraws);

            // 同步更新回傳的 lotteryRes
            lotteryRes.setMaxDraws(calculatedMaxDraws);
            lotteryRes.setRemainingDraws(calculatedMaxDraws);
        } else {
            String errorMsg = "更新 maxDraws 失敗：找不到商品 ID = " + lotteryId;
            log.error("❌ {}", errorMsg);
            throw new BusinessException(errorMsg);
        }

        // 🆕 生成籤位（一番賞模式：每個籤位有獎品；刮刮樂：目前也是每個籤位有獎品，未來可擴充謝謝惠顧）
        lotteryTicketService.generateTickets(lotteryId);
        log.info("✅ 籤位生成完成: lotteryId={}, maxDraws={}", lotteryId, calculatedMaxDraws);
        // 🆕 標記籤位已生成，防止 ON_SHELF 時重複生成
        Lottery lotteryForTicketFlag = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lotteryForTicketFlag != null) {
            lotteryForTicketFlag.setTicketsGenerated((byte) 1);
            lotteryForTicketFlag.setUpdatedAt(LocalDateTime.now());
            lotteryMapper.updateByPrimaryKeySelective(lotteryForTicketFlag);
            log.info("✅ ticketsGenerated 標記已更新: lotteryId={}", lotteryId);
        }

        // Step 3: 組裝回應
        return buildLotteryWithPrizesRes(lotteryRes, prizeResList);
    }

    @Override
    @Transactional
    public com.group.admin.res.lottery.LotteryWithPrizesRes updateLotteryWithPrizes(
            com.group.admin.req.lottery.LotteryWithPrizesUpdateReq req,
            String operatorId) {

        String lotteryId = req.getLotteryId();
        log.info("📝 更新商品與獎品: lotteryId={}, operatorId={}", lotteryId, operatorId);

        // Step 1: 更新商品（如果有提供 lottery 更新資訊）
        // ⚠️ 使用新版 updateLottery(id, req) 而非舊版，避免已上架商品被誤攔截
        LotteryRes lotteryRes;
        if (req.getLottery() != null) {
            lotteryRes = updateLottery(lotteryId, req.getLottery());
            log.info("✅ 商品更新成功: lotteryId={}", lotteryId);
        } else {
            // 沒有提供更新資訊，直接查詢現有資料
            Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
            if (lottery == null) {
                throw new BusinessException("商品不存在: " + lotteryId);
            }
            lotteryRes = convertToResNew(lottery);
        }

        Lottery currentLottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (currentLottery == null) {
            throw new BusinessException("商品不存在: " + lotteryId);
        }

        // Step 2: 更新/新增獎品（如果有提供 prizes）
        List<com.group.admin.res.lottery.LotteryPrizeRes> prizeResList = new ArrayList<>();

        if (req.getPrizes() != null && !req.getPrizes().isEmpty()) {
            if (!LotteryStatusEnum.DRAFT.getCode().equals(currentLottery.getStatus())
                    && !LotteryStatusEnum.OFF_SHELF.getCode().equals(currentLottery.getStatus())) {
                throw new BusinessException("商品已上架或不可編輯時，不可直接調整獎品；請先下架後再修改");
            }

            log.info("🎁 處理獎品更新: count={}", req.getPrizes().size());

            for (com.group.admin.req.lottery.LotteryPrizeUpdateReq prizeReq : req.getPrizes()) {
                if (prizeReq.getId() != null && !prizeReq.getId().isBlank()) {
                    // 有 ID → 更新現有獎品
                    LotteryPrize existingPrize = lotteryPrizeMapper.selectByPrimaryKey(prizeReq.getId());
                    if (existingPrize == null) {
                        log.warn("⚠️ 獎品不存在，跳過: prizeId={}", prizeReq.getId());
                        continue;
                    }

                    // 更新欄位（只更新非 null 的欄位）
                    if (prizeReq.getName() != null)
                        existingPrize.setName(prizeReq.getName());
                    if (prizeReq.getDescription() != null)
                        existingPrize.setDescription(prizeReq.getDescription());
                    if (prizeReq.getContent() != null)
                        existingPrize.setContent(prizeReq.getContent());
                    if (prizeReq.getImageUrl() != null)
                        existingPrize.setImageUrl(prizeReq.getImageUrl());
                    if (prizeReq.getLevel() != null)
                        existingPrize.setLevel(prizeReq.getLevel());
                    if (prizeReq.getPrizeNumber() != null)
                        existingPrize.setPrizeNumber(prizeReq.getPrizeNumber());
                    if (prizeReq.getQuantity() != null)
                        existingPrize.setQuantity(prizeReq.getQuantity());
                    if (prizeReq.getWeight() != null)
                        existingPrize.setWeight(prizeReq.getWeight());
                    if (prizeReq.getPrizeType() != null)
                        existingPrize.setPrizeType(prizeReq.getPrizeType());
                    if (prizeReq.getPointValue() != null)
                        existingPrize.setPointValue(prizeReq.getPointValue());
                    if (prizeReq.getIsLastPrize() != null)
                        existingPrize.setIsLastPrize(prizeReq.getIsLastPrize() ? (byte) 1 : (byte) 0);
                    if (prizeReq.getIsGrandPrize() != null)
                        existingPrize.setIsGrandPrize(prizeReq.getIsGrandPrize() ? (byte) 1 : (byte) 0);
                    existingPrize.setUpdatedAt(LocalDateTime.now());

                    lotteryPrizeMapper.updateByPrimaryKey(existingPrize);
                    prizeResList.add(convertPrizeToRes(existingPrize));

                    log.info("✅ 獎品更新成功: prizeId={}", prizeReq.getId());

                } else {
                    // 沒有 ID → 新增獎品
                    LotteryPrize newPrize = new LotteryPrize();
                    newPrize.setId(UUID.randomUUID().toString());
                    newPrize.setLotteryId(lotteryId);
                    newPrize.setName(prizeReq.getName());
                    newPrize.setDescription(prizeReq.getDescription());
                    newPrize.setContent(prizeReq.getContent());
                    newPrize.setImageUrl(prizeReq.getImageUrl());
                    newPrize.setLevel(prizeReq.getLevel());
                    newPrize.setPrizeNumber(prizeReq.getPrizeNumber());
                    newPrize.setQuantity(prizeReq.getQuantity() != null ? prizeReq.getQuantity() : 1);
                    newPrize.setRemaining(prizeReq.getQuantity() != null ? prizeReq.getQuantity() : 1);
                    newPrize.setWeight(prizeReq.getWeight() != null ? prizeReq.getWeight() : 1);
                    newPrize.setPrizeType(prizeReq.getPrizeType());
                    newPrize.setPointValue(prizeReq.getPointValue());
                    newPrize.setIsLastPrize(
                            prizeReq.getIsLastPrize() != null && prizeReq.getIsLastPrize() ? (byte) 1 : (byte) 0);
                    newPrize.setIsGrandPrize(
                            prizeReq.getIsGrandPrize() != null && prizeReq.getIsGrandPrize() ? (byte) 1 : (byte) 0);
                    newPrize.setCreatedAt(LocalDateTime.now());
                    newPrize.setUpdatedAt(LocalDateTime.now());

                    lotteryPrizeMapper.insert(newPrize);
                    prizeResList.add(convertPrizeToRes(newPrize));

                    log.info("✅ 獎品新增成功: prizeId={}", newPrize.getId());
                }
            }

            currentLottery = lotteryMapper.selectByPrimaryKey(lotteryId);
            if (currentLottery == null) {
                throw new BusinessException("商品不存在: " + lotteryId);
            }

            List<LotteryPrize> allPrizes = getLotteryPrizes(lotteryId);
            syncLotteryMaxDraws(currentLottery, req.getLottery(), allPrizes);
            resetGeneratedTickets(currentLottery, "獎品設定已變更，需重新生成籤位");

            prizeResList = allPrizes.stream()
                    .map(this::convertPrizeToRes)
                    .collect(Collectors.toList());
        }

        // 如果沒有提供 prizes，查詢現有所有獎品
        if (prizeResList.isEmpty()) {
            List<LotteryPrize> existingPrizes = getLotteryPrizes(lotteryId);
            prizeResList = existingPrizes.stream()
                    .map(this::convertPrizeToRes)
                    .collect(Collectors.toList());
        }

        // Step 3: 組裝回應
        return buildLotteryWithPrizesRes(lotteryRes, prizeResList);
    }

    @Override
    public com.group.admin.res.lottery.LotteryWithPrizesRes getLotteryWithPrizes(String lotteryId) {
        log.info("🔍 查詢商品與獎品: lotteryId={}", lotteryId);

        // Step 1: 查詢商品
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            throw new BusinessException("商品不存在: " + lotteryId);
        }

        LotteryRes lotteryRes = convertToResNew(lottery);

        // Step 2: 查詢所有獎品
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria().andLotteryIdEqualTo(lotteryId);
        prizeExample.setOrderByClause("order_num ASC, level ASC");

        List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(prizeExample);
        List<com.group.admin.res.lottery.LotteryPrizeRes> prizeResList = prizes.stream()
                .map(this::convertPrizeToRes)
                .collect(Collectors.toList());

        log.info("✅ 查詢成功: lotteryId={}, prizeCount={}", lotteryId, prizeResList.size());

        // Step 3: 組裝回應
        return buildLotteryWithPrizesRes(lotteryRes, prizeResList);
    }

    /**
     * 組裝 LotteryWithPrizesRes 回應
     */
    private com.group.admin.res.lottery.LotteryWithPrizesRes buildLotteryWithPrizesRes(
            LotteryRes lotteryRes,
            List<com.group.admin.res.lottery.LotteryPrizeRes> prizeResList) {

        // 計算統計資訊
        int totalPrizeCount = prizeResList.stream()
                .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                .sum();

        int remainingPrizeCount = prizeResList.stream()
                .mapToInt(p -> p.getRemaining() != null ? p.getRemaining() : 0)
                .sum();

        // 🆕 計算謝謝惠顧數量（maxDraws - 獎品總數）
        int maxDraws = lotteryRes.getMaxDraws() != null ? lotteryRes.getMaxDraws() : 0;
        int thanksgivingCount = Math.max(0, maxDraws - totalPrizeCount);

        double progressPercentage = totalPrizeCount > 0
                ? ((totalPrizeCount - remainingPrizeCount) * 100.0 / totalPrizeCount)
                : 0.0;

        // 查詢店家名稱
        String storeName = null;
        if (lotteryRes.getStoreId() != null) {
            Store store = storeMapper.selectByPrimaryKey(lotteryRes.getStoreId());
            if (store != null) {
                storeName = store.getStoreName();
            }
        }

        return com.group.admin.res.lottery.LotteryWithPrizesRes.builder()
                .id(lotteryRes.getId())
                .storeId(lotteryRes.getStoreId())
                .storeName(storeName)
                .title(lotteryRes.getTitle())
                .description(lotteryRes.getDescription())
                .imageUrl(lotteryRes.getImageUrl())
                .category(lotteryRes.getCategory())
                .subCategory(lotteryRes.getSubCategory())
                .playMode(lotteryRes.getPlayMode())
                .pricePerDraw(lotteryRes.getPricePerDraw())
                .discountedPrice(lotteryRes.getDiscountedPrice())
                .autoDiscountEnabled(lotteryRes.getAutoDiscountEnabled())
                .allowMultiDraw(lotteryRes.getAllowMultiDraw())
                .multiDrawOptions(lotteryRes.getMultiDrawOptions())
                .bonusEnabled(lotteryRes.getBonusEnabled())
                .bonusPointsPerDraw(lotteryRes.getBonusPointsPerDraw())
                .bonusCostPerDraw(lotteryRes.getBonusCostPerDraw())
                .tags(lotteryRes.getTags())
                .galleryImages(lotteryRes.getGalleryImages())
                .theme(lotteryRes.getTheme())
                .hotCount(lotteryRes.getHotCount())
                .paymentType(lotteryRes.getPaymentType())
                .freeDrawThreshold(lotteryRes.getFreeDrawThreshold())
                .delistStrategy(lotteryRes.getDelistStrategy())
                .orderNum(lotteryRes.getOrderNum())
                .remark(lotteryRes.getRemark())
                .content(lotteryRes.getContent())
                .startTime(lotteryRes.getStartTime())
                .endTime(lotteryRes.getEndTime())
                .maxDraws(lotteryRes.getMaxDraws())
                .totalDraws(lotteryRes.getTotalDraws())
                .remainingDraws(lotteryRes.getRemainingDraws())
                .status(lotteryRes.getStatus())
                .scheduledAt(lotteryRes.getScheduledAt())
                .createdAt(lotteryRes.getCreatedAt())
                .updatedAt(lotteryRes.getUpdatedAt())
                .prizes(prizeResList)
                .totalPrizeCount(totalPrizeCount)
                .remainingPrizeCount(remainingPrizeCount)
                .thanksgivingCount(thanksgivingCount)
                .progressPercentage(Math.round(progressPercentage * 100.0) / 100.0)
                .build();
    }

    /**
     * 轉換 LotteryPrize Entity → LotteryPrizeRes
     */
    private com.group.admin.res.lottery.LotteryPrizeRes convertPrizeToRes(LotteryPrize prize) {
        com.group.admin.res.lottery.LotteryPrizeRes res = new com.group.admin.res.lottery.LotteryPrizeRes();
        res.setId(prize.getId());
        res.setLotteryId(prize.getLotteryId());
        res.setName(prize.getName());
        res.setDescription(prize.getDescription());
        res.setContent(prize.getContent());
        res.setImageUrl(prize.getImageUrl());
        res.setLevel(prize.getLevel());
        res.setPrizeNumber(prize.getPrizeNumber());
        res.setQuantity(prize.getQuantity());
        res.setRemaining(prize.getRemaining());
        res.setWeight(prize.getWeight());
        res.setPrizeType(prize.getPrizeType());
        res.setPointValue(prize.getPointValue());
        res.setIsLastPrize(prize.getIsLastPrize() != null && prize.getIsLastPrize() == 1);
        res.setIsGrandPrize(prize.getIsGrandPrize() != null && prize.getIsGrandPrize() == 1);
        res.setOrderNum(prize.getOrderNum());
        res.setCreatedAt(prize.getCreatedAt());
        res.setUpdatedAt(prize.getUpdatedAt());
        return res;
    }

    @Override
    public List<com.group.admin.res.lottery.LotteryWithPrizesRes> getAllLotteriesWithPrizes(
            com.group.admin.req.common.QueryReq<com.group.admin.req.lottery.LotteryCondition> req) {

        log.info("🔍 查詢所有商品與獎品列表");

        // Step 1: 建構查詢條件
        LotteryCondition condition = (req != null && req.getCondition() != null)
                ? req.getCondition()
                : new LotteryCondition();

        LotteryExample example = new LotteryExample();
        LotteryExample.Criteria criteria = example.createCriteria();

        // 應用查詢條件（只有非空字串才加入條件）
        if (isNotBlank(condition.getStoreId())) {
            criteria.andStoreIdEqualTo(condition.getStoreId());
        }
        if (isNotBlank(condition.getTitle())) {
            criteria.andTitleLike("%" + condition.getTitle() + "%");
        }
        if (isNotBlank(condition.getStatus())) {
            criteria.andStatusEqualTo(condition.getStatus());
        }
        if (isNotBlank(condition.getCategory())) {
            criteria.andCategoryEqualTo(condition.getCategory());
        }
        if (isNotBlank(condition.getSubCategory())) {
            criteria.andSubCategoryEqualTo(condition.getSubCategory());
        }
        if (isNotBlank(condition.getPlayMode())) {
            criteria.andPlayModeEqualTo(condition.getPlayMode());
        }
        if (condition.getPriceMin() != null) {
            criteria.andPricePerDrawGreaterThanOrEqualTo(condition.getPriceMin());
        }
        if (condition.getPriceMax() != null) {
            criteria.andPricePerDrawLessThanOrEqualTo(condition.getPriceMax());
        }

        // 排序
        if (req != null && req.getSortBy() != null) {
            String order = normalizeSortOrder(req.getSortOrder(), "ASC");
            example.setOrderByClause(normalizeLotterySortColumn(req.getSortBy()) + " " + order);
        } else {
            example.setOrderByClause("created_at DESC");
        }

        // Step 2: 查詢所有商品
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);
        log.info("✅ 查詢到 {} 個商品", lotteries.size());

        // Step 3: 為每個商品查詢獎品列表
        List<com.group.admin.res.lottery.LotteryWithPrizesRes> result = lotteries.stream()
                .map(lottery -> {
                    // 轉換商品資訊
                    LotteryRes lotteryRes = convertToResNew(lottery);

                    // 查詢該商品的所有獎品
                    LotteryPrizeExample prizeExample = new LotteryPrizeExample();
                    prizeExample.createCriteria().andLotteryIdEqualTo(lottery.getId());
                    prizeExample.setOrderByClause("order_num ASC, level ASC");

                    List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(prizeExample);
                    List<com.group.admin.res.lottery.LotteryPrizeRes> prizeResList = prizes.stream()
                            .map(this::convertPrizeToRes)
                            .collect(Collectors.toList());

                    // 組裝完整回應
                    return buildLotteryWithPrizesRes(lotteryRes, prizeResList);
                })
                .collect(Collectors.toList());

        log.info("✅ 查詢成功: 返回 {} 個商品（包含獎品）", result.size());
        return result;
    }

    // ==================== 熱度管理 ====================

    /**
     * 增加商品熱度（hotCount +1）
     * 
     * @param lotteryId 商品 ID
     * @return 更新後的 hotCount
     */
    @Override
    public int incrementHotCount(String lotteryId) {
        log.info("🔥 增加商品熱度: lotteryId={}", lotteryId);

        // 查詢商品
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            log.error("❌ 商品不存在: lotteryId={}", lotteryId);
            throw new BusinessException("商品不存在");
        }

        // 更新 hotCount（原子性操作）
        int currentHotCount = lottery.getHotCount() != null ? lottery.getHotCount() : 0;
        int newHotCount = currentHotCount + 1;

        lottery.setHotCount(newHotCount);
        lottery.setUpdatedAt(LocalDateTime.now());

        lotteryMapper.updateByPrimaryKey(lottery);

        log.info("✅ 熱度更新成功: lotteryId={}, oldCount={}, newCount={}",
                lotteryId, currentHotCount, newHotCount);

        return newHotCount;
    }

    // ==================== 狀態變更（含 FSM 驗證）====================

    private static final Map<String, List<String>> VALID_TRANSITIONS = Map.ofEntries(
            Map.entry(LotteryStatusEnum.DRAFT.getCode(), List.of(
                    LotteryStatusEnum.ON_SHELF.getCode(),
                    LotteryStatusEnum.OFF_SHELF.getCode(),
                    LotteryStatusEnum.WAITING_ON_SHELF.getCode(),
                    LotteryStatusEnum.FORCED_OFF.getCode(),
                    LotteryStatusEnum.DELETED.getCode())),
            Map.entry(LotteryStatusEnum.WAITING_ON_SHELF.getCode(), List.of(
                    LotteryStatusEnum.ON_SHELF.getCode(),
                    LotteryStatusEnum.OFF_SHELF.getCode(),
                    LotteryStatusEnum.FORCED_OFF.getCode())),
            Map.entry(LotteryStatusEnum.ON_SHELF.getCode(), List.of(
                    LotteryStatusEnum.OFF_SHELF.getCode(),
                    LotteryStatusEnum.FORCED_OFF.getCode(),
                    LotteryStatusEnum.GRAND_PRIZE_DRAWN.getCode(),
                    LotteryStatusEnum.ALL_DRAWN.getCode())),
            Map.entry(LotteryStatusEnum.OFF_SHELF.getCode(), List.of(
                    LotteryStatusEnum.ON_SHELF.getCode(),
                    LotteryStatusEnum.WAITING_ON_SHELF.getCode(),
                    LotteryStatusEnum.FORCED_OFF.getCode(),
                    LotteryStatusEnum.DELETED.getCode())),
            Map.entry(LotteryStatusEnum.GRAND_PRIZE_DRAWN.getCode(), List.of(
                    LotteryStatusEnum.OFF_SHELF.getCode(),
                    LotteryStatusEnum.FORCED_OFF.getCode())),
            Map.entry(LotteryStatusEnum.ALL_DRAWN.getCode(), List.of(
                    LotteryStatusEnum.OFF_SHELF.getCode(),
                    LotteryStatusEnum.FORCED_OFF.getCode())),
            Map.entry(LotteryStatusEnum.FORCED_OFF.getCode(), List.of(
                    LotteryStatusEnum.OFF_SHELF.getCode())));

    @Override
    @Transactional
    public LotteryRes changeStatus(String lotteryId, String targetStatus, String reason, String operatorId) {
        log.info("🔄 變更商品狀態: lotteryId={}, targetStatus={}, operatorId={}", lotteryId, targetStatus, operatorId);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            throw new BusinessException("商品不存在: " + lotteryId);
        }

        String currentStatus = lottery.getStatus();
        if (currentStatus == null) {
            currentStatus = LotteryStatusEnum.DRAFT.getCode();
        }

        // FSM 轉換驗證
        List<String> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, List.of());
        if (!allowed.contains(targetStatus)) {
            throw new BusinessException(String.format(
                    "不合法的狀態轉換: %s → %s（允許: %s）", currentStatus, targetStatus, allowed));
        }

        // FORCED_OFF 需要 reason
        if (LotteryStatusEnum.FORCED_OFF.getCode().equals(targetStatus) && (reason == null || reason.isBlank())) {
            log.warn("⚠️ 強制下架未提供原因");
        }

        // 特殊轉換邏輯
        if (LotteryStatusEnum.FORCED_OFF.getCode().equals(targetStatus) && reason != null) {
            lottery.setRemark(reason);
        }
        // 🆕 上架前置驗證（先驗證，再寫入 DB，避免無效的狀態變更）
        if (LotteryStatusEnum.ON_SHELF.getCode().equals(targetStatus)) {
            validateCanGoOnShelf(lottery);
        }

        lottery.setStatus(targetStatus);
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKey(lottery);

        // 上架時自動生成籤位
        if (LotteryStatusEnum.ON_SHELF.getCode().equals(targetStatus)) {
            ensureTicketsGeneratedForOnShelf(lottery);
        }

        log.info("✅ 狀態變更成功: {} → {}", currentStatus, targetStatus);
        return convertToResNew(lottery);
    }

    // ==================== 輔助方法 ====================

    /**
     * 🆕 刮刮樂獎品數量驗證：必須且只能有 1 個大獎，且 quantity=1
     * 
     * @param gameMode 遊戲模式（SCRATCH_STORE 或 SCRATCH_PLAYER）
     * @param prizes   獎品列表
     */
    private void validateScratchPrizes(String gameMode,
            List<com.group.admin.req.lottery.LotteryPrizeCreateReq> prizes) {
        if (prizes == null || prizes.size() != 1) {
            throw new BusinessException("刮刮樂模式只允許 1 筆大獎獎品，其餘格子由系統視為謝謝惠顧。");
        }

        com.group.admin.req.lottery.LotteryPrizeCreateReq grandPrize = prizes.get(0);
        if (!Boolean.TRUE.equals(grandPrize.getIsGrandPrize())) {
            throw new BusinessException("刮刮樂模式唯一的獎品必須標記為大獎。");
        }
        if (!"GRAND".equalsIgnoreCase(String.valueOf(grandPrize.getLevel()))) {
            throw new BusinessException("刮刮樂模式唯一的大獎 level 必須固定為 GRAND。");
        }

        long grandPrizeItemCount = prizes.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsGrandPrize()))
                .count();
        long grandPrizeTotal = prizes.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsGrandPrize()))
                .mapToLong(p -> p.getQuantity() != null ? p.getQuantity() : 1)
                .sum();

        if (grandPrizeItemCount != 1 || grandPrizeTotal != 1) {
            throw new BusinessException(
                    "刮刮樂模式：必須且只能設定 1 個大獎，且大獎 quantity 必須為 1。"
                            + "目前大獎筆數=" + grandPrizeItemCount + "，大獎數量=" + grandPrizeTotal + "。");
        }
    }

    private void validateScratchPrizeEntities(List<LotteryPrize> prizes) {
        if (prizes == null || prizes.size() != 1) {
            throw new BusinessException("刮刮樂模式只允許 1 筆大獎獎品，其餘格子由系統視為謝謝惠顧。");
        }

        LotteryPrize grandPrize = prizes.get(0);
        if (grandPrize.getIsGrandPrize() == null || grandPrize.getIsGrandPrize() != 1) {
            throw new BusinessException("刮刮樂模式唯一的獎品必須標記為大獎。");
        }
        if (!"GRAND".equalsIgnoreCase(String.valueOf(grandPrize.getLevel()))) {
            throw new BusinessException("刮刮樂模式唯一的大獎 level 必須固定為 GRAND。");
        }

        long grandPrizeItemCount = prizes.stream()
                .filter(p -> p.getIsGrandPrize() != null && p.getIsGrandPrize() == 1)
                .count();
        long grandPrizeTotal = prizes.stream()
                .filter(p -> p.getIsGrandPrize() != null && p.getIsGrandPrize() == 1)
                .mapToLong(p -> p.getQuantity() != null ? p.getQuantity() : 1)
                .sum();

        if (grandPrizeItemCount != 1 || grandPrizeTotal != 1) {
            throw new BusinessException(
                    "刮刮樂模式：必須且只能設定 1 個大獎，且大獎 quantity 必須為 1。"
                            + "目前大獎筆數=" + grandPrizeItemCount + "，大獎數量=" + grandPrizeTotal + "。");
        }
    }

    /**
     * 🆕 SCRATCH_STORE 上架前置驗證：必須先指定大獎號碼才能上架
     * SCRATCH_PLAYER 和其他模式不受此限制。
     * 
     * @param lottery 商品實體
     */
    private void validateCanGoOnShelf(Lottery lottery) {
        List<LotteryPrize> prizes = getLotteryPrizes(lottery.getId());
        if (prizes.isEmpty()) {
            throw new BusinessException("商品至少需要 1 筆獎品資料，才能上架");
        }

        int nonLastPrizeQuantity = prizes.stream()
                .filter(p -> p.getIsLastPrize() == null || p.getIsLastPrize() != 1)
                .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                .sum();
        if (nonLastPrizeQuantity <= 0) {
            throw new BusinessException("商品至少需要 1 個非最後賞獎品，才能上架");
        }

        if ("LOTTERY_MODE".equals(lottery.getPlayMode())) {
            if (lottery.getMaxDraws() == null || !lottery.getMaxDraws().equals(nonLastPrizeQuantity)) {
                throw new BusinessException("抽籤型商品的 maxDraws 必須與非最後賞獎品總數一致，請先重新儲存商品與獎品");
            }
        }

        if ("SCRATCH_MODE".equals(lottery.getPlayMode())) {
            validateScratchPrizeEntities(prizes);
            if (lottery.getMaxDraws() == null || lottery.getMaxDraws() < nonLastPrizeQuantity) {
                throw new BusinessException("刮刮樂商品的 maxDraws 不能小於真實獎品總數，請先重新儲存商品與獎品");
            }
        }

        if (GameModeEnum.SCRATCH_STORE.getCode().equals(lottery.getGameMode())) {
            String d = lottery.getDesignatedPrizeNumbers();
            if (d == null || d.isBlank()) {
                throw new BusinessException(
                        "SCRATCH_STORE 模式：請先在後台指定大獎號碼（designatedPrizeNumbers），才能上架。");
            }
        }
    }

    private String resolveLifecycleStatus(String requestedStatus, LocalDateTime scheduledAt, String fallbackStatus) {
        LocalDateTime now = LocalDateTime.now();
        String resolvedStatus = isNotBlank(requestedStatus) ? requestedStatus : fallbackStatus;

        if (!isNotBlank(resolvedStatus)) {
            resolvedStatus = LotteryStatusEnum.OFF_SHELF.getCode();
        }

        if (List.of(
                LotteryStatusEnum.GRAND_PRIZE_DRAWN.getCode(),
                LotteryStatusEnum.ALL_DRAWN.getCode(),
                LotteryStatusEnum.DELETED.getCode()).contains(resolvedStatus)) {
            throw new BusinessException("GRAND_PRIZE_DRAWN / ALL_DRAWN / DELETED 為系統狀態，不可在建立或更新商品時手動指定");
        }

        boolean hasFutureSchedule = scheduledAt != null && scheduledAt.isAfter(now);
        if (hasFutureSchedule && List.of(
                LotteryStatusEnum.DRAFT.getCode(),
                LotteryStatusEnum.OFF_SHELF.getCode(),
                LotteryStatusEnum.ON_SHELF.getCode(),
                LotteryStatusEnum.WAITING_ON_SHELF.getCode()).contains(resolvedStatus)) {
            return LotteryStatusEnum.WAITING_ON_SHELF.getCode();
        }

        if (LotteryStatusEnum.WAITING_ON_SHELF.getCode().equals(resolvedStatus) && !hasFutureSchedule) {
            return LotteryStatusEnum.OFF_SHELF.getCode();
        }

        return resolvedStatus;
    }

    private List<LotteryPrize> getLotteryPrizes(String lotteryId) {
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria().andLotteryIdEqualTo(lotteryId);
        prizeExample.setOrderByClause("order_num ASC, level ASC");
        return lotteryPrizeMapper.selectByExample(prizeExample);
    }

    private void syncLotteryMaxDraws(Lottery lottery, LotteryUpdateReq lotteryReq, List<LotteryPrize> prizes) {
        int nonLastPrizeQuantity = prizes.stream()
                .filter(p -> p.getIsLastPrize() == null || p.getIsLastPrize() != 1)
                .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                .sum();

        if (nonLastPrizeQuantity <= 0) {
            throw new BusinessException("非最後賞獎品總數必須大於 0，請至少保留 1 個真實獎品");
        }

        Integer requestedMaxDraws = lotteryReq != null ? lotteryReq.getMaxDraws() : null;
        int calculatedMaxDraws;

        if ("LOTTERY_MODE".equals(lottery.getPlayMode())) {
            calculatedMaxDraws = nonLastPrizeQuantity;
        } else if ("SCRATCH_MODE".equals(lottery.getPlayMode())) {
            validateScratchPrizeEntities(prizes);
            if (requestedMaxDraws != null) {
                if (requestedMaxDraws < nonLastPrizeQuantity) {
                    throw new BusinessException(String.format(
                            "刮刮樂模式錯誤：總抽數(%d)不能小於獎品總數(%d)",
                            requestedMaxDraws,
                            nonLastPrizeQuantity));
                }
                calculatedMaxDraws = requestedMaxDraws;
            } else if (lottery.getMaxDraws() != null && lottery.getMaxDraws() >= nonLastPrizeQuantity) {
                calculatedMaxDraws = lottery.getMaxDraws();
            } else {
                calculatedMaxDraws = nonLastPrizeQuantity;
            }
        } else {
            calculatedMaxDraws = nonLastPrizeQuantity;
        }

        lottery.setMaxDraws(calculatedMaxDraws);
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKeySelective(lottery);
    }

    private void resetGeneratedTickets(Lottery lottery, String reason) {
        if (lottery.getTicketsGenerated() == null || lottery.getTicketsGenerated() != 1) {
            return;
        }

        LotteryTicketExample ticketExample = new LotteryTicketExample();
        ticketExample.createCriteria().andLotteryIdEqualTo(lottery.getId());
        lotteryTicketMapper.deleteByExample(ticketExample);

        lottery.setTicketsGenerated((byte) 0);
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKeySelective(lottery);
        log.info("🔄 {}: lotteryId={}", reason, lottery.getId());
    }

    private void ensureTicketsGeneratedForOnShelf(Lottery lottery) {
        Boolean generated = lottery.getTicketsGenerated() != null && lottery.getTicketsGenerated() == 1;
        if (generated) {
            log.info("ℹ️ 籤位已存在，略過生成: lotteryId={}", lottery.getId());
            return;
        }

        log.info("🎫 籤位尚未生成，自動執行 generateTickets: lotteryId={}", lottery.getId());
        try {
            lotteryTicketService.generateTickets(lottery.getId());
            lottery.setTicketsGenerated((byte) 1);
            lottery.setUpdatedAt(LocalDateTime.now());
            lotteryMapper.updateByPrimaryKeySelective(lottery);
            log.info("✅ 籤位自動生成完成: lotteryId={}", lottery.getId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("商品上架失敗：籤位生成失敗，請先確認獎品與抽數設定。原因: " + e.getMessage());
        }
    }

    /**
     * 商品主題採用共享字典：有值時自動建立或回收既有主題名稱。
     */
    private String upsertThemeIfPresent(String theme) {
        if (!isNotBlank(theme)) {
            return null;
        }

        return categoryService.upsertTheme(theme, null, null).getName();
    }

    /**
     * 判斷字串是否非空白
     * 空字串 "" 視為空白
     * 
     * @param str 字串
     * @return 是否非空白
     */
    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private QueryReq<LotteryCondition> normalizeReq(QueryReq<LotteryCondition> req) {
        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new LotteryCondition());
        }
        return req;
    }

    private QueryReq<LotteryCondition> toQueryReq(LotteryQueryReq req) {
        QueryReq<LotteryCondition> queryReq = new QueryReq<>();
        LotteryCondition condition = new LotteryCondition();
        condition.setStoreId(req.getStoreId());
        condition.setTitle(req.getKeyword());
        condition.setCategory(req.getCategory());
        condition.setTheme(req.getTheme());
        condition.setStatus(req.getStatus());
        queryReq.setCondition(condition);
        queryReq.setPage(req.getPage());
        queryReq.setSize(req.getSize());
        queryReq.setSortBy(req.getSortBy());
        queryReq.setSortOrder(req.getSortDirection());
        return queryReq;
    }

    private String normalizeLotterySortColumn(String rawSortBy) {
        if (!isNotBlank(rawSortBy)) {
            return "created_at";
        }
        return switch (rawSortBy) {
            case "createdAt", "created_at" -> "created_at";
            case "orderNum", "order_num" -> "order_num";
            case "weight" -> "weight";
            case "hotCount", "hot_count" -> "hot_count";
            case "pricePerDraw", "price_per_draw" -> "price_per_draw";
            case "discountedPrice", "discounted_price" -> "discounted_price";
            case "totalDraws", "total_draws" -> "total_draws";
            case "maxDraws", "max_draws" -> "max_draws";
            case "updatedAt", "updated_at" -> "updated_at";
            case "title" -> "title";
            case "status" -> "status";
            case "category" -> "category";
            case "storeId", "store_id" -> "store_id";
            default -> "created_at";
        };
    }

    private String normalizeSortOrder(String rawSortOrder, String defaultOrder) {
        if (!isNotBlank(rawSortOrder)) {
            return defaultOrder;
        }
        return "ASC".equalsIgnoreCase(rawSortOrder) ? "ASC" : "DESC";
    }

    private int resolvePage(Integer page) {
        return page != null && page > 0 ? page : 1;
    }

    private int resolveSize(Integer size) {
        if (size == null || size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }

    // ==================== T010: 自動下架（checkAndDelist）====================

    @Override
    @Transactional
    public void checkAndDelist(String lotteryId) {
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null || !LotteryStatusEnum.ON_SHELF.getCode().equals(lottery.getStatus()))
            return;
        String strategy = isNotBlank(lottery.getDelistStrategy()) ? lottery.getDelistStrategy() : "ALL_DRAWN";
        String targetStatus = null;

        if (isDrawableContentExhausted(lottery)) {
            targetStatus = LotteryStatusEnum.ALL_DRAWN.getCode();
        } else if ("GRAND_PRIZE_DRAWN".equals(strategy) && isGrandPrizeDrawnOut(lotteryId)) {
            targetStatus = LotteryStatusEnum.GRAND_PRIZE_DRAWN.getCode();
        }

        if (targetStatus != null) {
            log.info("🏁 自動狀態流轉觸發: lotteryId={}, strategy={}, targetStatus={}",
                    lotteryId, strategy, targetStatus);
            Lottery upd = new Lottery();
            upd.setId(lotteryId);
            upd.setStatus(targetStatus);
            upd.setUpdatedAt(LocalDateTime.now());
            lotteryMapper.updateByPrimaryKeySelective(upd);
        }
    }

    private boolean isGrandPrizeDrawnOut(String lotteryId) {
        LotteryPrizeExample ex = new LotteryPrizeExample();
        ex.createCriteria().andLotteryIdEqualTo(lotteryId).andIsGrandPrizeEqualTo((byte) 1);
        List<LotteryPrize> grandPrizes = lotteryPrizeMapper.selectByExample(ex);
        return !grandPrizes.isEmpty() && grandPrizes.stream()
                .allMatch(p -> p.getRemaining() != null && p.getRemaining() <= 0);
    }

    private boolean isDrawableContentExhausted(Lottery lottery) {
        String lotteryId = lottery.getId();
        if (hasTicketPool(lotteryId)) {
            LotteryTicketExample availableExample = new LotteryTicketExample();
            availableExample.createCriteria()
                    .andLotteryIdEqualTo(lotteryId)
                    .andStatusEqualTo("AVAILABLE");
            return lotteryTicketMapper.countByExample(availableExample) == 0;
        }

        LotteryPrizeExample remainingPrizeExample = new LotteryPrizeExample();
        remainingPrizeExample.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andRemainingGreaterThan(0);
        return lotteryPrizeMapper.countByExample(remainingPrizeExample) == 0;
    }

    private boolean hasTicketPool(String lotteryId) {
        LotteryTicketExample ticketExample = new LotteryTicketExample();
        ticketExample.createCriteria().andLotteryIdEqualTo(lotteryId);
        return lotteryTicketMapper.countByExample(ticketExample) > 0;
    }

    @Override
    public void promoteScheduledLotteries() {
        log.info("⏰ [Scheduled] 檢查定時上架商品...");
        List<Lottery> list = lotteryMapper.selectScheduledForPromotion();
        for (Lottery l : list) {
            try {
                changeStatus(l.getId(), LotteryStatusEnum.ON_SHELF.getCode(), "scheduled promotion", SYSTEM_OPERATOR);
                log.info("✅ [Scheduled] 商品自動上架：id={}, title={}", l.getId(), l.getTitle());
            } catch (BusinessException e) {
                log.warn("⚠️ [Scheduled] 商品自動上架失敗：id={}, title={}, reason={}", l.getId(), l.getTitle(), e.getMessage());
            }
        }
    }

    @Override
    public void promoteDrawableLotteries() {
        // DRAWABLE 狀態已移除，此方法保留介面相容性，不做任何事
        log.debug("⏰ [Scheduled] promoteDrawableLotteries 已停用");
    }
}
