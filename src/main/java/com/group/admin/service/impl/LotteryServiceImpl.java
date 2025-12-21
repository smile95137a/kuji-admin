package com.group.admin.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryDrawRecord;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.entity.PointLog;
import com.group.admin.entity.User;
import com.group.admin.enums.LotteryCategoryEnum;
import com.group.admin.enums.LotteryStatusEnum;
import com.group.admin.example.LotteryExample;
import com.group.admin.example.LotteryPrizeExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.LotteryDrawRecordMapper;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.mapper.PointLogMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.lottery.LotteryCreateReq;
import com.group.admin.req.lottery.LotteryQueryReq;
import com.group.admin.req.lottery.LotteryUpdateReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.lottery.LotteryListRes;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryService;

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
    
    private final Random random = new Random();

    // ==================== 商品管理 CRUD ====================

    @Override
    @Transactional
    public LotteryRes createLottery(LotteryCreateReq req, String operatorId) {
        log.info("創建抽獎商品: title={}, operatorId={}", req.getTitle(), operatorId);
        
        Lottery lottery = new Lottery();
        lottery.setId(UUID.randomUUID().toString());
        lottery.setStoreId(req.getStoreId());
        lottery.setTitle(req.getTitle());
        lottery.setDescription(req.getDescription());
        lottery.setImageUrl(req.getImageUrl());
        lottery.setCategory(req.getCategory());
        lottery.setSubCategory(req.getSubCategory());
        lottery.setPricePerDraw(req.getPricePerDraw());
        lottery.setDiscountedPrice(req.getDiscountedPrice());
        lottery.setAutoDiscountEnabled(req.getAutoDiscountEnabled() != null && req.getAutoDiscountEnabled() ? (byte) 1 : (byte) 0);
        lottery.setAllowMultiDraw(req.getAllowMultiDraw() != null && req.getAllowMultiDraw() ? (byte) 1 : (byte) 0);
        
        if (req.getMultiDrawOptions() != null) {
            try {
                lottery.setMultiDrawOptions(objectMapper.writeValueAsString(req.getMultiDrawOptions()));
            } catch (JsonProcessingException e) {
                log.warn("多抽選項序列化失敗", e);
            }
        }
        
        lottery.setScheduledAt(req.getScheduledAt());
        lottery.setStartTime(req.getStartTime());
        lottery.setEndTime(req.getEndTime());
        lottery.setMaxDraws(req.getMaxDraws());
        lottery.setTotalDraws(0);
        lottery.setStatus(LotteryStatusEnum.DRAFT.getCode());
        lottery.setOrderNum(req.getOrderNum() != null ? req.getOrderNum() : 0);
        lottery.setWeight(req.getWeight() != null ? req.getWeight() : 1);
        lottery.setCreatedBy(operatorId);
        lottery.setCreatedAt(LocalDateTime.now());
        lottery.setUpdatedAt(LocalDateTime.now());
        lottery.setRemark(req.getRemark());
        
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
        
        // 只有草稿和已下架狀態可以修改
        String status = lottery.getStatus();
        if (!LotteryStatusEnum.DRAFT.getCode().equals(status) 
                && !LotteryStatusEnum.OFF_SHELF.getCode().equals(status)) {
            throw new BusinessException("只有草稿或已下架狀態的商品可以修改");
        }
        
        if (req.getTitle() != null) lottery.setTitle(req.getTitle());
        if (req.getDescription() != null) lottery.setDescription(req.getDescription());
        if (req.getImageUrl() != null) lottery.setImageUrl(req.getImageUrl());
        if (req.getCategory() != null) lottery.setCategory(req.getCategory());
        if (req.getSubCategory() != null) lottery.setSubCategory(req.getSubCategory());
        if (req.getPricePerDraw() != null) lottery.setPricePerDraw(req.getPricePerDraw());
        if (req.getDiscountedPrice() != null) lottery.setDiscountedPrice(req.getDiscountedPrice());
        if (req.getAutoDiscountEnabled() != null) lottery.setAutoDiscountEnabled(req.getAutoDiscountEnabled() ? (byte) 1 : (byte) 0);
        if (req.getAllowMultiDraw() != null) lottery.setAllowMultiDraw(req.getAllowMultiDraw() ? (byte) 1 : (byte) 0);
        
        if (req.getMultiDrawOptions() != null) {
            try {
                lottery.setMultiDrawOptions(objectMapper.writeValueAsString(req.getMultiDrawOptions()));
            } catch (JsonProcessingException e) {
                log.warn("多抽選項序列化失敗", e);
            }
        }
        
        if (req.getScheduledAt() != null) lottery.setScheduledAt(req.getScheduledAt());
        if (req.getStartTime() != null) lottery.setStartTime(req.getStartTime());
        if (req.getEndTime() != null) lottery.setEndTime(req.getEndTime());
        if (req.getMaxDraws() != null) lottery.setMaxDraws(req.getMaxDraws());
        if (req.getOrderNum() != null) lottery.setOrderNum(req.getOrderNum());
        if (req.getWeight() != null) lottery.setWeight(req.getWeight());
        if (req.getRemark() != null) lottery.setRemark(req.getRemark());
        
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
        
        if (req.getStoreId() != null) {
            criteria.andStoreIdEqualTo(req.getStoreId());
        }
        if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
            criteria.andTitleLike("%" + req.getKeyword() + "%");
        }
        if (req.getCategory() != null && !req.getCategory().isEmpty()) {
            criteria.andCategoryEqualTo(req.getCategory());
        }
        if (req.getStatus() != null && !req.getStatus().isEmpty()) {
            criteria.andStatusEqualTo(req.getStatus());
        }
        
        // 設置排序
        String sortBy = req.getSortBy() != null ? req.getSortBy() : "created_at";
        String sortDirection = req.getSortDirection() != null ? req.getSortDirection() : "DESC";
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
        return queryLotteries(req);
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
        if (!LotteryStatusEnum.ON_SHELF.getCode().equals(lottery.getStatus()) 
                && !LotteryStatusEnum.IN_PROGRESS.getCode().equals(lottery.getStatus())) {
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
            lottery.setStatus(LotteryStatusEnum.ENDED.getCode());
            lottery.setUpdatedAt(LocalDateTime.now());
            lotteryMapper.updateByPrimaryKey(lottery);
            throw new BusinessException("獎品已抽完，活動結束");
        }

        // 更新狀態為抽獎中
        if (LotteryStatusEnum.ON_SHELF.getCode().equals(lottery.getStatus())) {
            lottery.setStatus(LotteryStatusEnum.IN_PROGRESS.getCode());
            lottery.setUpdatedAt(LocalDateTime.now());
            lotteryMapper.updateByPrimaryKey(lottery);
        }

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
        if (lottery == null) return false;
        
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
     * 根據權重選擇獎項
     */
    private LotteryPrize selectPrize(List<LotteryPrize> prizes, String lotteryId) {
        LotteryPrize selected = null;
        int attempts = 0;
        
        while (attempts++ < 5) {
            // 計算總權重
            long total = prizes.stream()
                    .filter(p -> p.getRemaining() != null && p.getRemaining() > 0)
                    .mapToLong(p -> (long) p.getRemaining() * (p.getWeight() == null || p.getWeight() == 0 ? 1 : p.getWeight()))
                    .sum();
            
            if (total <= 0) break;
            
            long r = Math.abs(random.nextLong()) % total;
            long cum = 0;
            
            for (LotteryPrize p : prizes) {
                if (p.getRemaining() == null || p.getRemaining() <= 0) continue;
                long weight = (long) p.getRemaining() * (p.getWeight() == null || p.getWeight() == 0 ? 1 : p.getWeight());
                cum += weight;
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
            if (selected != null) break;
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
     * 解析多連抽選項
     */
    private List<Integer> parseMultiDrawOptions(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Integer>>() {});
        } catch (JsonProcessingException e) {
            log.warn("多抽選項解析失敗: {}", json, e);
            return List.of();
        }
    }
}
