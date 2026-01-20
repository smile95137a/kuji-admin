package com.group.admin.service.impl;

import com.group.admin.condition.WalletTransactionCondition;
import com.group.admin.entity.User;
import com.group.admin.entity.UserWallet;
import com.group.admin.entity.WalletTransaction;
import com.group.admin.enums.CoinTypeEnum;
import com.group.admin.enums.TransactionTypeEnum;
import com.group.admin.example.UserWalletExample;
import com.group.admin.example.WalletTransactionExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.UserMapper;
import com.group.admin.mapper.UserWalletMapper;
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
 * 錢包服務實作
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    
    private final UserWalletMapper userWalletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public UserWalletRes createWallet(String userId) {
        log.info("🔍 建立錢包：userId={}", userId);
        
        // 檢查是否已存在
        UserWalletExample example = new UserWalletExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<UserWallet> existing = userWalletMapper.selectByExample(example);
        
        if (!existing.isEmpty()) {
            log.warn("⚠️ 錢包已存在：userId={}", userId);
            return convertToRes(existing.get(0));
        }
        
        // 建立錢包
        UserWallet wallet = new UserWallet();
        wallet.setId(UUID.randomUUID().toString());
        wallet.setUserId(userId);
        wallet.setGoldCoins(0L);
        wallet.setBonusCoins(0L);
        wallet.setTotalRecharged(0L);
        wallet.setVersion(0);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        
        userWalletMapper.insert(wallet);
        
        log.info("✅ 錢包建立成功：walletId={}", wallet.getId());
        return convertToRes(wallet);
    }
    
    @Override
    public UserWalletRes getWallet(String userId) {
        UserWalletExample example = new UserWalletExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<UserWallet> wallets = userWalletMapper.selectByExample(example);
        
        if (wallets.isEmpty()) {
            // 自動建立錢包
            log.info("💡 錢包不存在，自動建立：userId={}", userId);
            return createWallet(userId);
        }
        
        return convertToRes(wallets.get(0));
    }
    
    @Override
    @Transactional
    public void deductGold(String userId, Long amount, String transactionType, String relatedId, String description) {
        log.info("🔍 扣除金幣：userId={}, amount={}, type={}", userId, amount, transactionType);
        
        if (amount <= 0) {
            throw new BusinessException("扣除金額必須大於 0");
        }
        
        // 查詢錢包
        UserWalletExample example = new UserWalletExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<UserWallet> wallets = userWalletMapper.selectByExample(example);
        
        if (wallets.isEmpty()) {
            throw new BusinessException("錢包不存在");
        }
        
        UserWallet wallet = wallets.get(0);
        
        // 檢查餘額
        if (wallet.getGoldCoins() < amount) {
            throw new BusinessException("金幣餘額不足");
        }
        
        // 使用樂觀鎖更新
        Long newBalance = wallet.getGoldCoins() - amount;
        wallet.setGoldCoins(newBalance);
        wallet.setVersion(wallet.getVersion() + 1);
        wallet.setUpdatedAt(LocalDateTime.now());
        
        int rows = userWalletMapper.updateByPrimaryKey(wallet);
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
        
        // 查詢錢包
        UserWalletExample example = new UserWalletExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<UserWallet> wallets = userWalletMapper.selectByExample(example);
        
        if (wallets.isEmpty()) {
            throw new BusinessException("錢包不存在");
        }
        
        UserWallet wallet = wallets.get(0);
        
        // 更新餘額
        Long newBalance = wallet.getGoldCoins() + amount;
        wallet.setGoldCoins(newBalance);
        wallet.setVersion(wallet.getVersion() + 1);
        wallet.setUpdatedAt(LocalDateTime.now());
        
        // 如果是儲值，更新累計儲值金額
        if (TransactionTypeEnum.RECHARGE.getCode().equals(transactionType)) {
            wallet.setTotalRecharged(wallet.getTotalRecharged() + amount);
        }
        
        int rows = userWalletMapper.updateByPrimaryKey(wallet);
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
        
        // 查詢錢包
        UserWalletExample example = new UserWalletExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<UserWallet> wallets = userWalletMapper.selectByExample(example);
        
        if (wallets.isEmpty()) {
            throw new BusinessException("錢包不存在");
        }
        
        UserWallet wallet = wallets.get(0);
        
        // 更新餘額
        Long newBalance = wallet.getBonusCoins() + amount;
        wallet.setBonusCoins(newBalance);
        wallet.setVersion(wallet.getVersion() + 1);
        wallet.setUpdatedAt(LocalDateTime.now());
        
        int rows = userWalletMapper.updateByPrimaryKey(wallet);
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
        
        // 查詢錢包
        UserWalletExample example = new UserWalletExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<UserWallet> wallets = userWalletMapper.selectByExample(example);
        
        if (wallets.isEmpty()) {
            throw new BusinessException("錢包不存在");
        }
        
        UserWallet wallet = wallets.get(0);
        
        // 檢查餘額
        if (wallet.getBonusCoins() < amount) {
            throw new BusinessException("紅利點數不足");
        }
        
        // 更新餘額
        Long newBalance = wallet.getBonusCoins() - amount;
        wallet.setBonusCoins(newBalance);
        wallet.setVersion(wallet.getVersion() + 1);
        wallet.setUpdatedAt(LocalDateTime.now());
        
        int rows = userWalletMapper.updateByPrimaryKey(wallet);
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
            if (condition.getUserId() != null && !condition.getUserId().isEmpty()) {
                criteria.andUserIdEqualTo(condition.getUserId());
            }
            if (condition.getTransactionType() != null && !condition.getTransactionType().isEmpty()) {
                criteria.andTransactionTypeEqualTo(condition.getTransactionType());
            }
            if (condition.getCoinType() != null && !condition.getCoinType().isEmpty()) {
                criteria.andCoinTypeEqualTo(condition.getCoinType());
            }
            if (condition.getRelatedId() != null && !condition.getRelatedId().isEmpty()) {
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
        UserWalletExample example = new UserWalletExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<UserWallet> wallets = userWalletMapper.selectByExample(example);
        
        if (wallets.isEmpty()) {
            return false;
        }
        
        return wallets.get(0).getGoldCoins() >= amount;
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
     * 轉換為回應 DTO
     */
    private UserWalletRes convertToRes(UserWallet wallet) {
        // 查詢玩家資訊
        User user = userMapper.selectByPrimaryKey(wallet.getUserId());
        
        return UserWalletRes.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .goldCoins(wallet.getGoldCoins())
                .bonusCoins(wallet.getBonusCoins())
                .totalRecharged(wallet.getTotalRecharged())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
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
}
