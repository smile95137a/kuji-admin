package com.group.admin.service.impl;

import com.group.admin.condition.WalletTransactionCondition;
import com.group.admin.entity.User;
import com.group.admin.entity.WalletTransaction;
import com.group.admin.enums.CoinTypeEnum;
import com.group.admin.enums.TransactionTypeEnum;
import com.group.admin.example.WalletTransactionExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.UserMapper;
import com.group.admin.mapper.WalletTransactionMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.wallet.WalletAdjustReq;
import com.group.admin.res.wallet.UserWalletRes;
import com.group.admin.res.wallet.WalletTransactionRes;
import com.group.admin.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 錢包服務實作（直接使用 user 表的 gold_coins / bonus_coins）
 * 
 * <p>架構變更說明（2026-02-08）：</p>
 * <p>原本使用獨立的 user_wallet 表存放金幣/紅利，
 * 但 user 表本身已有 gold_coins 和 bonus_coins 欄位，
 * 造成資料冗餘和不一致的風險。</p>
 * <p>現在統一從 user 表讀寫金幣/紅利，不再依賴 user_wallet 表。</p>
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    
    private final WalletTransactionMapper walletTransactionMapper;
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public UserWalletRes createWallet(String userId) {
        log.info("🔍 初始化錢包欄位：userId={}", userId);
        
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        
        // 如果已經有初始值就直接返回
        if (user.getGoldCoins() != null && user.getBonusCoins() != null) {
            log.info("✅ 使用者已有金幣欄位：userId={}", userId);
            return convertToRes(user);
        }
        
        // 初始化金幣欄位
        user.setGoldCoins(0L);
        user.setBonusCoins(0L);
        user.setTotalRecharged(0L);
        user.setVersion(0);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateByPrimaryKey(user);
        
        log.info("✅ 錢包初始化成功：userId={}", userId);
        return convertToRes(user);
    }
    
    @Override
    public UserWalletRes getWallet(String userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        
        // 如果金幣欄位未初始化，自動初始化
        if (user.getGoldCoins() == null) {
            user.setGoldCoins(0L);
            user.setBonusCoins(0L);
            user.setTotalRecharged(0L);
            user.setVersion(0);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateByPrimaryKey(user);
        }
        
        return convertToRes(user);
    }
    
    @Override
    @Transactional
    public void deductGold(String userId, Long amount, String transactionType, String relatedId, String description) {
        log.info("🔍 扣除金幣：userId={}, amount={}, type={}", userId, amount, transactionType);
        
        if (amount <= 0) {
            throw new BusinessException("扣除金額必須大於 0");
        }
        
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        
        Long currentGold = user.getGoldCoins() != null ? user.getGoldCoins() : 0L;
        
        // 檢查餘額
        if (currentGold < amount) {
            throw new BusinessException("金幣餘額不足");
        }
        
        // 更新餘額
        Long newBalance = currentGold - amount;
        user.setGoldCoins(newBalance);
        user.setVersion((user.getVersion() != null ? user.getVersion() : 0) + 1);
        user.setUpdatedAt(LocalDateTime.now());
        
        int rows = userMapper.updateByPrimaryKey(user);
        if (rows == 0) {
            throw new BusinessException("點數扣除失敗，請重試");
        }
        
        // 記錄交易
        recordTransaction(userId, CoinTypeEnum.GOLD.getCode(), transactionType, 
                -amount, newBalance, relatedId, description, null);
        
        log.info("✅ 金幣扣除成功：newBalance={}", newBalance);
    }
    
    @Override
    @Transactional
    public void addGold(String userId, Long amount, String transactionType, String relatedId, String description) {
        log.info("🔍 增加金幣：userId={}, amount={}, type={}", userId, amount, transactionType);
        
        if (amount <= 0) {
            throw new BusinessException("增加金額必須大於 0");
        }
        
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        
        Long currentGold = user.getGoldCoins() != null ? user.getGoldCoins() : 0L;
        
        // 更新餘額
        Long newBalance = currentGold + amount;
        user.setGoldCoins(newBalance);
        user.setVersion((user.getVersion() != null ? user.getVersion() : 0) + 1);
        user.setUpdatedAt(LocalDateTime.now());
        
        // 如果是儲值，更新累計儲值金額
        if (TransactionTypeEnum.RECHARGE.getCode().equals(transactionType)) {
            Long totalRecharged = user.getTotalRecharged() != null ? user.getTotalRecharged() : 0L;
            user.setTotalRecharged(totalRecharged + amount);
        }
        
        int rows = userMapper.updateByPrimaryKey(user);
        if (rows == 0) {
            throw new BusinessException("點數增加失敗，請重試");
        }
        
        // 記錄交易
        recordTransaction(userId, CoinTypeEnum.GOLD.getCode(), transactionType, 
                amount, newBalance, relatedId, description, null);
        
        log.info("✅ 金幣增加成功：newBalance={}", newBalance);
    }
    
    @Override
    @Transactional
    public void addBonus(String userId, Long amount, String transactionType, String relatedId, String description) {
        log.info("🔍 增加紅利：userId={}, amount={}, type={}", userId, amount, transactionType);
        
        if (amount <= 0) {
            throw new BusinessException("增加金額必須大於 0");
        }
        
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        
        Long currentBonus = user.getBonusCoins() != null ? user.getBonusCoins() : 0L;
        
        // 更新餘額
        Long newBalance = currentBonus + amount;
        user.setBonusCoins(newBalance);
        user.setVersion((user.getVersion() != null ? user.getVersion() : 0) + 1);
        user.setUpdatedAt(LocalDateTime.now());
        
        int rows = userMapper.updateByPrimaryKey(user);
        if (rows == 0) {
            throw new BusinessException("點數增加失敗，請重試");
        }
        
        // 記錄交易
        recordTransaction(userId, CoinTypeEnum.BONUS.getCode(), transactionType, 
                amount, newBalance, relatedId, description, null);
        
        log.info("✅ 紅利增加成功：newBalance={}", newBalance);
    }
    
    @Override
    @Transactional
    public void deductBonus(String userId, Long amount, String transactionType, String relatedId, String description) {
        log.info("🔍 扣除紅利：userId={}, amount={}, type={}", userId, amount, transactionType);
        
        if (amount <= 0) {
            throw new BusinessException("扣除金額必須大於 0");
        }
        
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        
        Long currentBonus = user.getBonusCoins() != null ? user.getBonusCoins() : 0L;
        
        // 檢查餘額
        if (currentBonus < amount) {
            throw new BusinessException("紅利點數不足");
        }
        
        // 更新餘額
        Long newBalance = currentBonus - amount;
        user.setBonusCoins(newBalance);
        user.setVersion((user.getVersion() != null ? user.getVersion() : 0) + 1);
        user.setUpdatedAt(LocalDateTime.now());
        
        int rows = userMapper.updateByPrimaryKey(user);
        if (rows == 0) {
            throw new BusinessException("點數扣除失敗，請重試");
        }
        
        // 記錄交易（負數）
        recordTransaction(userId, CoinTypeEnum.BONUS.getCode(), transactionType, 
                -amount, newBalance, relatedId, description, null);
        
        log.info("✅ 紅利扣除成功：newBalance={}", newBalance);
    }
    
    @Override
    @Transactional
    public void adjustCoins(WalletAdjustReq req, String operatorId) {
        log.info("🔍 手動調整點數：userId={}, coinType={}, amount={}, operator={}", 
                req.getUserId(), req.getCoinType(), req.getAmount(), operatorId);
        
        String coinType = req.getCoinType();
        Long amount = req.getAmount();
        
        if (CoinTypeEnum.GOLD.getCode().equals(coinType)) {
            if (amount > 0) {
                addGold(req.getUserId(), amount, TransactionTypeEnum.ADMIN_ADJUST.getCode(), 
                        null, req.getReason());
            } else {
                deductGold(req.getUserId(), -amount, TransactionTypeEnum.ADMIN_ADJUST.getCode(), 
                        null, req.getReason());
            }
        } else if (CoinTypeEnum.BONUS.getCode().equals(coinType)) {
            if (amount > 0) {
                addBonus(req.getUserId(), amount, TransactionTypeEnum.ADMIN_ADJUST.getCode(), 
                        null, req.getReason());
            } else {
                throw new BusinessException("紅利不支援扣除");
            }
        } else {
            throw new BusinessException("無效的幣種");
        }
        
        log.info("✅ 點數調整成功");
    }
    
    @Override
    public List<WalletTransactionRes> getTransactions(QueryReq<WalletTransactionCondition> req) {
        WalletTransactionCondition condition = req != null ? req.getCondition() : null;
        
        WalletTransactionExample example = new WalletTransactionExample();
        WalletTransactionExample.Criteria criteria = example.createCriteria();
        
        if (condition != null) {
            if (isNotBlank(condition.getUserId())) {
                criteria.andUserIdEqualTo(condition.getUserId());
            }
            if (isNotBlank(condition.getTransactionType())) {
                criteria.andTransactionTypeEqualTo(condition.getTransactionType());
            }
            if (isNotBlank(condition.getCoinType())) {
                criteria.andCoinTypeEqualTo(condition.getCoinType());
            }
            if (isNotBlank(condition.getRelatedId())) {
                criteria.andRelatedIdEqualTo(condition.getRelatedId());
            }
            // 日期範圍（LocalDate 轉 LocalDateTime）
            if (condition.getCreatedAtStart() != null) {
                criteria.andCreatedAtGreaterThanOrEqualTo(
                    condition.getCreatedAtStart().atStartOfDay()
                );
            }
            if (condition.getCreatedAtEnd() != null) {
                criteria.andCreatedAtLessThanOrEqualTo(
                    condition.getCreatedAtEnd().atTime(23, 59, 59)
                );
            }
        }
        
        // 排序
        example.setOrderByClause("created_at DESC");
        
        List<WalletTransaction> transactions = walletTransactionMapper.selectByExample(example);
        return transactions.stream().map(this::convertTransactionToRes).collect(Collectors.toList());
    }
    
    @Override
    public boolean hasEnoughGold(String userId, Long amount) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return false;
        }
        Long gold = user.getGoldCoins() != null ? user.getGoldCoins() : 0L;
        return gold >= amount;
    }
    
    /**
     * 記錄交易
     */
    private void recordTransaction(String userId, String coinType, String transactionType, 
                                   Long amount, Long balanceAfter, String relatedId, 
                                   String description, String createdBy) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setUserId(userId);
        transaction.setTransactionType(transactionType);
        transaction.setCoinType(coinType);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedId(relatedId);
        transaction.setDescription(description);
        transaction.setCreatedBy(createdBy);
        transaction.setCreatedAt(LocalDateTime.now());
        
        walletTransactionMapper.insert(transaction);
    }
    
    /**
     * 轉換 User 為錢包回應 DTO（直接從 user 表取金幣/紅利）
     */
    private UserWalletRes convertToRes(User user) {
        return UserWalletRes.builder()
                .id(user.getId())
                .userId(user.getId())
                .userNickname(user.getNickname())
                .userEmail(user.getEmail())
                .goldCoins(user.getGoldCoins() != null ? user.getGoldCoins() : 0L)
                .bonusCoins(user.getBonusCoins() != null ? user.getBonusCoins() : 0L)
                .totalRecharged(user.getTotalRecharged() != null ? user.getTotalRecharged() : 0L)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    /**
     * 轉換交易記錄為回應 DTO
     */
    private WalletTransactionRes convertTransactionToRes(WalletTransaction transaction) {
        // 查詢玩家資訊
        User user = userMapper.selectByPrimaryKey(transaction.getUserId());
        
        return WalletTransactionRes.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .transactionType(transaction.getTransactionType())
                .transactionTypeName(TransactionTypeEnum.getNameByCode(transaction.getTransactionType()))
                .coinType(transaction.getCoinType())
                .coinTypeName(CoinTypeEnum.getNameByCode(transaction.getCoinType()))
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .relatedId(transaction.getRelatedId())
                .description(transaction.getDescription())
                .createdBy(transaction.getCreatedBy())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    /**
     * 檢查字串是否非空白
     * 空字串 "" 會被視為 null 處理
     */
    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
