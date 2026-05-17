package com.group.admin.service.impl;

import com.group.admin.condition.CoinTransactionCondition;
import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotterySession;
import com.group.admin.entity.User;
import com.group.admin.entity.WalletTransaction;
import com.group.admin.enums.CoinTypeEnum;
import com.group.admin.enums.TransactionTypeEnum;
import com.group.admin.example.WalletTransactionExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotterySessionMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.mapper.WalletTransactionMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.wallet.CoinAdjustReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.wallet.CoinTransactionRes;
import com.group.admin.res.wallet.UserCoinRes;
import com.group.admin.service.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 錢包服務，負責使用者金幣與紅利的增減、交易紀錄與查詢。
 *
 * <p>此服務會同步維護：</p>
 * <ul>
 *   <li>{@code user} 表上的 {@code gold_coins} / {@code bonus_coins}</li>
 *   <li>{@code wallet_transaction} 交易流水</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {

    private final WalletTransactionMapper walletTransactionMapper;
    private final UserMapper userMapper;
    private final LotteryMapper lotteryMapper;
    private final LotterySessionMapper lotterySessionMapper;

    @Override
    @Transactional
    public UserCoinRes createWallet(String userId) {
        log.info("建立錢包: userId={}", userId);

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }

        if (user.getGoldCoins() != null && user.getBonusCoins() != null) {
            log.info("錢包已存在，直接回傳: userId={}", userId);
            return convertToRes(user);
        }

        user.setGoldCoins(0L);
        user.setBonusCoins(0L);
        user.setTotalRecharged(0L);
        user.setVersion(0);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateByPrimaryKey(user);

        log.info("錢包初始化完成: userId={}", userId);
        return convertToRes(user);
    }

    @Override
    public UserCoinRes getWallet(String userId) {
        User user = userMapper.selectByPrimaryKey(userId);

        if (user == null) {
            throw new BusinessException("使用者不存在");
        }

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
        log.info("扣除金幣: userId={}, amount={}, type={}", userId, amount, transactionType);

        if (amount <= 0) {
            throw new BusinessException("扣除金幣金額必須大於 0");
        }

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }

        Long currentGold = user.getGoldCoins() != null ? user.getGoldCoins() : 0L;
        if (currentGold < amount) {
            throw new BusinessException("金幣餘額不足");
        }

        Long newBalance = currentGold - amount;
        user.setGoldCoins(newBalance);
        user.setVersion((user.getVersion() != null ? user.getVersion() : 0) + 1);
        user.setUpdatedAt(LocalDateTime.now());

        int rows = userMapper.updateByPrimaryKey(user);
        if (rows == 0) {
            throw new BusinessException("更新金幣餘額失敗，請稍後再試");
        }

        recordTransaction(
                userId,
                CoinTypeEnum.GOLD.getCode(),
                transactionType,
                -amount,
                newBalance,
                relatedId,
                description,
                null
        );

        log.info("扣除金幣完成: newBalance={}", newBalance);
    }

    @Override
    @Transactional
    public void addGold(String userId, Long amount, String transactionType, String relatedId, String description) {
        log.info("增加金幣: userId={}, amount={}, type={}", userId, amount, transactionType);

        if (amount <= 0) {
            throw new BusinessException("增加金幣金額必須大於 0");
        }

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }

        Long currentGold = user.getGoldCoins() != null ? user.getGoldCoins() : 0L;
        Long newBalance = currentGold + amount;
        user.setGoldCoins(newBalance);
        user.setVersion((user.getVersion() != null ? user.getVersion() : 0) + 1);
        user.setUpdatedAt(LocalDateTime.now());

        if (TransactionTypeEnum.RECHARGE.getCode().equals(transactionType)) {
            Long totalRecharged = user.getTotalRecharged() != null ? user.getTotalRecharged() : 0L;
            user.setTotalRecharged(totalRecharged + amount);
        }

        int rows = userMapper.updateByPrimaryKey(user);
        if (rows == 0) {
            throw new BusinessException("更新金幣餘額失敗，請稍後再試");
        }

        recordTransaction(
                userId,
                CoinTypeEnum.GOLD.getCode(),
                transactionType,
                amount,
                newBalance,
                relatedId,
                description,
                null
        );

        log.info("增加金幣完成: newBalance={}", newBalance);
    }

    @Override
    @Transactional
    public void addBonus(String userId, Long amount, String transactionType, String relatedId, String description) {
        log.info("增加紅利: userId={}, amount={}, type={}", userId, amount, transactionType);

        if (amount <= 0) {
            throw new BusinessException("增加紅利金額必須大於 0");
        }

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }

        Long currentBonus = user.getBonusCoins() != null ? user.getBonusCoins() : 0L;
        Long newBalance = currentBonus + amount;
        user.setBonusCoins(newBalance);
        user.setVersion((user.getVersion() != null ? user.getVersion() : 0) + 1);
        user.setUpdatedAt(LocalDateTime.now());

        int rows = userMapper.updateByPrimaryKey(user);
        if (rows == 0) {
            throw new BusinessException("更新紅利餘額失敗，請稍後再試");
        }

        recordTransaction(
                userId,
                CoinTypeEnum.BONUS.getCode(),
                transactionType,
                amount,
                newBalance,
                relatedId,
                description,
                null
        );

        log.info("增加紅利完成: newBalance={}", newBalance);
    }

    @Override
    @Transactional
    public void deductBonus(String userId, Long amount, String transactionType, String relatedId, String description) {
        log.info("扣除紅利: userId={}, amount={}, type={}", userId, amount, transactionType);

        if (amount <= 0) {
            throw new BusinessException("扣除紅利金額必須大於 0");
        }

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }

        Long currentBonus = user.getBonusCoins() != null ? user.getBonusCoins() : 0L;
        if (currentBonus < amount) {
            throw new BusinessException("紅利餘額不足");
        }

        Long newBalance = currentBonus - amount;
        user.setBonusCoins(newBalance);
        user.setVersion((user.getVersion() != null ? user.getVersion() : 0) + 1);
        user.setUpdatedAt(LocalDateTime.now());

        int rows = userMapper.updateByPrimaryKey(user);
        if (rows == 0) {
            throw new BusinessException("更新紅利餘額失敗，請稍後再試");
        }

        recordTransaction(
                userId,
                CoinTypeEnum.BONUS.getCode(),
                transactionType,
                -amount,
                newBalance,
                relatedId,
                description,
                null
        );

        log.info("扣除紅利完成: newBalance={}", newBalance);
    }

    @Override
    @Transactional
    public void adjustCoins(CoinAdjustReq req, String operatorId) {
        log.info(
                "管理員調整點數: userId={}, coinType={}, amount={}, operator={}",
                req.getUserId(),
                req.getCoinType(),
                req.getAmount(),
                operatorId
        );

        String coinType = req.getCoinType();
        Long amount = req.getAmount();

        if (CoinTypeEnum.GOLD.getCode().equals(coinType)) {
            if (amount > 0) {
                addGold(req.getUserId(), amount, TransactionTypeEnum.ADMIN_ADJUST.getCode(), null, req.getReason());
            } else {
                deductGold(req.getUserId(), -amount, TransactionTypeEnum.ADMIN_ADJUST.getCode(), null, req.getReason());
            }
        } else if (CoinTypeEnum.BONUS.getCode().equals(coinType)) {
            if (amount > 0) {
                addBonus(req.getUserId(), amount, TransactionTypeEnum.ADMIN_ADJUST.getCode(), null, req.getReason());
            } else {
                throw new BusinessException("紅利不支援扣除");
            }
        } else {
            throw new BusinessException("無效的幣種");
        }

        log.info("管理員調整點數完成");
    }

    @Override
    public PageResult<CoinTransactionRes> getTransactions(QueryReq<CoinTransactionCondition> req) {
        QueryReq<CoinTransactionCondition> safeReq = normalizeReq(req);
        CoinTransactionCondition condition = safeReq.getCondition();

        WalletTransactionExample example = new WalletTransactionExample();
        WalletTransactionExample.Criteria criteria = example.createCriteria();

        if (condition != null) {
            if (isNotBlank(condition.getUserId())) {
                criteria.andUserIdEqualTo(condition.getUserId());
            }

            String transactionType = isNotBlank(condition.getTransactionType())
                    ? condition.getTransactionType()
                    : condition.getType();
            if (isNotBlank(transactionType)) {
                if ("DRAW_GOLD".equals(transactionType) || "DRAW_BONUS".equals(transactionType)) {
                    criteria.andTransactionTypeEqualTo(TransactionTypeEnum.DRAW.getCode());
                    criteria.andCoinTypeEqualTo("DRAW_BONUS".equals(transactionType)
                            ? CoinTypeEnum.BONUS.getCode()
                            : CoinTypeEnum.GOLD.getCode());
                } else {
                    criteria.andTransactionTypeEqualTo(transactionType);
                }
            }

            if (isNotBlank(condition.getCoinType())) {
                criteria.andCoinTypeEqualTo(condition.getCoinType());
            }
            if (isNotBlank(condition.getRelatedId())) {
                criteria.andRelatedIdEqualTo(condition.getRelatedId());
            }
            if (condition.getCreatedAtStart() != null) {
                criteria.andCreatedAtGreaterThanOrEqualTo(condition.getCreatedAtStart().atStartOfDay());
            }
            if (condition.getCreatedAtEnd() != null) {
                criteria.andCreatedAtLessThanOrEqualTo(condition.getCreatedAtEnd().atTime(23, 59, 59));
            }
        }

        example.setOrderByClause("created_at DESC");

        int page = resolvePage(safeReq.getPage());
        int size = resolveSize(safeReq.getSize());
        int offset = (page - 1) * size;

        long total = walletTransactionMapper.countByExample(example);
        if (total == 0) {
            return PageResult.empty(page, size);
        }

        List<WalletTransaction> transactions = walletTransactionMapper.selectByExamplePaged(example, offset, size);
        return PageResult.of(
                page,
                size,
                total,
                transactions.stream().map(this::convertTransactionToRes).collect(Collectors.toList())
        );
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
     * 建立一筆錢包交易流水。
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
     * User 實體轉為錢包 DTO。
     */
    private UserCoinRes convertToRes(User user) {
        return UserCoinRes.builder()
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
     * 交易流水實體轉為前台/後台共用 DTO。
     */
    private CoinTransactionRes convertTransactionToRes(WalletTransaction transaction) {
        User user = userMapper.selectByPrimaryKey(transaction.getUserId());
        Long amount = transaction.getAmount() != null ? transaction.getAmount() : 0L;
        boolean isIncome = amount >= 0;
        Long absAmount = Math.abs(amount);
        String coinType = transaction.getCoinType();
        Long goldAmount = CoinTypeEnum.GOLD.getCode().equals(coinType) ? absAmount : 0L;
        Long bonusAmount = CoinTypeEnum.BONUS.getCode().equals(coinType) ? absAmount : 0L;
        LotteryRef lotteryRef = resolveLotteryRef(transaction);
        Integer drawIndex = extractDrawIndex(transaction.getDescription());
        Integer ticketNumber = extractTicketNumber(transaction.getDescription());
        Long refundAmount = TransactionTypeEnum.FREE_DRAW_REFUND.getCode().equals(transaction.getTransactionType())
                ? absAmount
                : 0L;

        String compatType = transaction.getTransactionType();
        if (TransactionTypeEnum.DRAW.getCode().equals(transaction.getTransactionType())) {
            compatType = CoinTypeEnum.BONUS.getCode().equals(coinType) ? "DRAW_BONUS" : "DRAW_GOLD";
        }

        String compatTypeName = switch (compatType) {
            case "DRAW_GOLD" -> "抽獎扣款（金幣）";
            case "DRAW_BONUS" -> "抽獎扣款（紅利）";
            case "FREE_DRAW_REFUND" -> "免單退款";
            case "RECHARGE" -> "儲值";
            case "RECYCLE_BONUS" -> "回收紅利";
            case "REFERRAL_BONUS" -> "推薦獎勵";
            case "ADMIN_ADJUST" -> "管理員調整";
            case "EXPIRE" -> "點數過期";
            default -> TransactionTypeEnum.getNameByCode(transaction.getTransactionType());
        };

        return CoinTransactionRes.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .transactionType(transaction.getTransactionType())
                .transactionTypeName(TransactionTypeEnum.getNameByCode(transaction.getTransactionType()))
                .type(compatType)
                .typeName(compatTypeName)
                .coinType(transaction.getCoinType())
                .coinTypeName(CoinTypeEnum.getNameByCode(transaction.getCoinType()))
                .direction(isIncome ? "INCOME" : "EXPENSE")
                .amount(absAmount)
                .goldAmount(goldAmount)
                .bonusAmount(bonusAmount)
                .balanceAfter(transaction.getBalanceAfter())
                .relatedId(transaction.getRelatedId())
                .referenceId(transaction.getRelatedId())
                .lotteryId(lotteryRef.lotteryId())
                .lotteryTitle(lotteryRef.lotteryTitle())
                .drawIndex(drawIndex)
                .ticketNumber(ticketNumber)
                .refundAmount(refundAmount)
                .description(transaction.getDescription())
                .createdBy(transaction.getCreatedBy())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private LotteryRef resolveLotteryRef(WalletTransaction transaction) {
        if (transaction == null) {
            return new LotteryRef(null, null);
        }

        String relatedId = transaction.getRelatedId();
        if (!isNotBlank(relatedId)) {
            return new LotteryRef(null, null);
        }

        Lottery lottery = null;
        if (TransactionTypeEnum.DRAW.getCode().equals(transaction.getTransactionType())) {
            lottery = lotteryMapper.selectByPrimaryKey(relatedId);
        } else if (TransactionTypeEnum.FREE_DRAW_REFUND.getCode().equals(transaction.getTransactionType())) {
            lottery = lotteryMapper.selectByPrimaryKey(relatedId);
            if (lottery == null) {
                LotterySession session = lotterySessionMapper.selectByPrimaryKey(relatedId);
                if (session != null && isNotBlank(session.getLotteryId())) {
                    lottery = lotteryMapper.selectByPrimaryKey(session.getLotteryId());
                }
            }
        }

        if (lottery == null) {
            return new LotteryRef(null, null);
        }
        return new LotteryRef(lottery.getId(), lottery.getTitle());
    }

    private Integer extractDrawIndex(String description) {
        if (!isNotBlank(description)) {
            return null;
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("第\\s*(\\d+)\\s*抽")
                .matcher(description);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private Integer extractTicketNumber(String description) {
        if (!isNotBlank(description)) {
            return null;
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("票號\\s*#?(\\d+)")
                .matcher(description);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private record LotteryRef(String lotteryId, String lotteryTitle) {
    }

    /**
     * 判斷字串是否為非空白。
     */
    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private QueryReq<CoinTransactionCondition> normalizeReq(QueryReq<CoinTransactionCondition> req) {
        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new CoinTransactionCondition());
        }
        return req;
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
}
