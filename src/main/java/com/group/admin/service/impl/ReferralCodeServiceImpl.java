package com.group.admin.service.impl;

import com.group.admin.entity.ReferralCode;
import com.group.admin.entity.ReferralRecord;
import com.group.admin.entity.Store;
import com.group.admin.entity.User;
import com.group.admin.example.ReferralCodeExample;
import com.group.admin.example.ReferralRecordExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.ReferralCodeMapper;
import com.group.admin.mapper.ReferralRecordMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.repository.ReferralCodeRepository;
import com.group.admin.repository.ReferralRecordRepository;
import com.group.admin.req.referral.ReferralCodeCreateReq;
import com.group.admin.req.referral.ReferralCodeUpdateReq;
import com.group.admin.res.referral.ReferralCodeRes;
import com.group.admin.res.referral.ReferralRecordRes;
import com.group.admin.res.referral.ReferralStatsRes;
import com.group.admin.service.ReferralCodeService;
import com.group.admin.service.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralCodeServiceImpl implements ReferralCodeService {
    
    private final ReferralCodeMapper referralCodeMapper;
    private final ReferralRecordMapper referralRecordMapper;
    private final ReferralCodeRepository referralCodeRepository;
    private final ReferralRecordRepository referralRecordRepository;
    private final StoreMapper storeMapper;
    private final UserMapper userMapper;
    private final CoinService walletService;

    private static final long DEFAULT_BONUS_PER_USE = 50L;
    private static final int DEFAULT_MAX_USAGE = 100;
    
    @Override
    @Transactional
    public ReferralCodeRes create(ReferralCodeCreateReq req) {
        log.info("🎫 建立推薦碼: code={}, storeId={}", req.getCode(), req.getStoreId());
        
        String normalizedCode = normalizeCodeOrGenerate(req.getCode());
        String storeId = normalizeId(req.getStoreId());
        log.info("建立店家推薦碼 code={}, storeId={}", normalizedCode, storeId);

        if (storeId == null) {
            throw new BusinessException("請選擇店家");
        }

        ReferralCode existingCode = referralCodeRepository.selectByCode(normalizedCode);
        if (existingCode != null) {
            throw new BusinessException("推薦碼已存在: " + req.getCode());
        }

        Store store = storeMapper.selectByPrimaryKey(storeId);
        if (store == null) {
            throw new BusinessException("店家不存在: " + storeId);
        }
        assertStoreHasNoOtherReferralCode(storeId, null);
        
        ReferralCode referralCode = new ReferralCode();
        referralCode.setId(UUID.randomUUID().toString());
        referralCode.setCode(normalizedCode);
        referralCode.setStoreId(storeId);
        referralCode.setDescription(req.getDescription());
        referralCode.setOwnerId(storeId);
        referralCode.setOwnerType("STORE");
        referralCode.setIsActive(true);
        referralCode.setUsedCount(0);
        referralCode.setMaxUsage(DEFAULT_MAX_USAGE);
        referralCode.setRewardGold(0L);
        referralCode.setRewardBonus(DEFAULT_BONUS_PER_USE);
        referralCode.setCreatedAt(LocalDateTime.now());
        referralCode.setUpdatedAt(LocalDateTime.now());

        referralCodeMapper.insert(referralCode);
        log.info("✅ 推薦碼建立成功: id={}", referralCode.getId());
        
        return toReferralCodeRes(referralCode, store.getStoreName());
    }
    
    @Override
    @Transactional
    public ReferralCodeRes update(String id, ReferralCodeUpdateReq req) {
        log.info("📝 更新推薦碼: id={}", id);
        
        ReferralCode referralCode = referralCodeMapper.selectByPrimaryKey(id);
        if (referralCode == null) {
            throw new BusinessException("推薦碼不存在: " + id);
        }

        String requestedStoreId = normalizeId(req.getStoreId());
        if (requestedStoreId != null && !requestedStoreId.equals(referralCode.getStoreId())) {
            Store requestedStore = storeMapper.selectByPrimaryKey(requestedStoreId);
            if (requestedStore == null) {
                throw new BusinessException("店家不存在: " + requestedStoreId);
            }
            referralCode.setStoreId(requestedStoreId);
            referralCode.setOwnerId(requestedStoreId);
        }
        assertStoreHasNoOtherReferralCode(referralCode.getStoreId(), id);
        
        if (req.getDescription() != null) {
            referralCode.setDescription(req.getDescription());
        }
        if (req.getIsActive() != null) {
            referralCode.setIsActive(req.getIsActive());
        }
        referralCode.setUpdatedAt(LocalDateTime.now());
        
        referralCodeMapper.updateByPrimaryKey(referralCode);
        log.info("✅ 推薦碼更新成功: id={}", id);
        
        Store store = storeMapper.selectByPrimaryKey(referralCode.getStoreId());
        return toReferralCodeRes(referralCode, store != null ? store.getStoreName() : null);
    }
    
    @Override
    @Transactional
    public void delete(String id) {
        log.info("🗑️ 刪除推薦碼: id={}", id);
        
        ReferralCode referralCode = referralCodeMapper.selectByPrimaryKey(id);
        if (referralCode == null) {
            throw new BusinessException("推薦碼不存在: " + id);
        }
        
        referralCodeMapper.deleteByPrimaryKey(id);
        log.info("✅ 推薦碼刪除成功: id={}", id);
    }
    
    @Override
    public ReferralCodeRes getById(String id) {
        ReferralCode referralCode = referralCodeMapper.selectByPrimaryKey(id);
        if (referralCode == null) {
            throw new BusinessException("推薦碼不存在: " + id);
        }
        
        Store store = storeMapper.selectByPrimaryKey(referralCode.getStoreId());
        return toReferralCodeRes(referralCode, store != null ? store.getStoreName() : null);
    }
    
    @Override
    public ReferralCodeRes getByCode(String code) {
        String normalizedCode = normalizeCode(code);
        if (normalizedCode == null) {
            return null;
        }
        ReferralCode referralCode = referralCodeRepository.selectByCode(normalizedCode);
        if (referralCode == null) {
            return null;
        }
        
        Store store = storeMapper.selectByPrimaryKey(referralCode.getStoreId());
        return toReferralCodeRes(referralCode, store != null ? store.getStoreName() : null);
    }
    
    @Override
    public List<ReferralCodeRes> getByStoreId(String storeId) {
        List<ReferralCode> codes = referralCodeRepository.selectByStoreId(storeId);
        Store store = storeMapper.selectByPrimaryKey(storeId);
        String storeName = store != null ? store.getStoreName() : null;
        
        return codes.stream()
                .map(code -> toReferralCodeRes(code, storeName))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReferralCodeRes> getAll(String storeId, Boolean isActive) {
        ReferralCodeExample example = new ReferralCodeExample();
        ReferralCodeExample.Criteria criteria = example.createCriteria();
        if (storeId != null && !storeId.isBlank()) {
            criteria.andStoreIdEqualTo(storeId);
        }
        if (isActive != null) {
            criteria.andIsActiveEqualTo(isActive);
        }
        List<ReferralCode> codes = referralCodeMapper.selectByExample(example);
        
        return codes.stream()
                .map(code -> {
                    Store store = storeMapper.selectByPrimaryKey(code.getStoreId());
                    return toReferralCodeRes(code, store != null ? store.getStoreName() : null);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean validateCode(String code) {
        String normalizedCode = normalizeCode(code);
        if (normalizedCode == null) {
            return false;
        }
        ReferralCode referralCode = referralCodeRepository.selectByCode(normalizedCode);
        if (referralCode == null || !Boolean.TRUE.equals(referralCode.getIsActive())) {
            return false;
        }
        if (referralCode.getMaxUsage() != null && referralCode.getUsedCount() >= referralCode.getMaxUsage()) {
            return false;
        }
        return true;
    }
    
    @Override
    @Transactional
    public boolean useCode(String userId, String code) {
        log.info("🎁 使用推薦碼: userId={}, code={}", userId, code);

        try {
            applyReferral(userId, code);
            log.info("推薦碼綁定成功: userId={}, code={}", userId, code);
            return true;
        } catch (BusinessException e) {
            log.warn("推薦碼無法使用: userId={}, code={}, reason={}", userId, code, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void useCode(String userId, String code, String registrationEmail) {
        log.info("🎁 使用推薦碼 (含 email): userId={}, code={}", userId, code);
        boolean result = useCode(userId, code);
        if (!result) {
            throw new com.group.admin.exception.BusinessException("推薦碼無效或已使用");
        }
    }

    @Override
    public com.group.admin.res.referral.ReferralValidateRes validateForRegistration(String code) {
        log.info("🔍 驗證推薦碼 (註冊): code={}", code);
        String normalizedCode = normalizeCode(code);
        if (normalizedCode == null) {
            return new com.group.admin.res.referral.ReferralValidateRes(false, code, null);
        }

        ReferralCode referralCode = referralCodeRepository.selectByCode(normalizedCode);
        boolean valid = referralCode != null
                && Boolean.TRUE.equals(referralCode.getIsActive())
                && (referralCode.getMaxUsage() == null || referralCode.getUsedCount() < referralCode.getMaxUsage());

        String storeName = null;
        if (valid && referralCode.getStoreId() != null) {
            Store store = storeMapper.selectByPrimaryKey(referralCode.getStoreId());
            storeName = store != null ? store.getStoreName() : null;
        }

        return new com.group.admin.res.referral.ReferralValidateRes(valid, normalizedCode, storeName);
    }
    
    @Override
    public List<ReferralRecordRes> getRecordsByCodeId(String referralCodeId) {
        List<ReferralRecord> records = referralRecordRepository.selectByReferralCodeId(referralCodeId);
        return records.stream()
                .map(this::toReferralRecordRes)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReferralRecordRes> getRecordsByStoreId(String storeId) {
        List<ReferralRecord> records = referralRecordRepository.selectByStoreId(storeId);
        return records.stream()
                .map(this::toReferralRecordRes)
                .collect(Collectors.toList());
    }
    
    @Override
    public ReferralRecordRes getRecordByUserId(String userId) {
        List<ReferralRecord> records = referralRecordRepository.selectByUserId(userId);
        return !records.isEmpty() ? toReferralRecordRes(records.get(0)) : null;
    }

    // ========== 012-referral-code new implementations ==========

    @Override
    @Transactional
    public ReferralCodeRes generateCode(String userId) {
        log.info("🎫 為使用者產生推薦碼: userId={}", userId);

        // Check if user already has an active code
        ReferralCodeExample example = new ReferralCodeExample();
        example.createCriteria()
                .andOwnerIdEqualTo(userId)
                .andOwnerTypeEqualTo("USER")
                .andIsActiveEqualTo(true);
        List<ReferralCode> existing = referralCodeMapper.selectByExample(example);
        if (!existing.isEmpty()) {
            log.info("🔄 使用者已有推薦碼，直接返回");
            return toReferralCodeRes(existing.get(0), null);
        }

        // Generate unique code
        String code = generateUniqueCode();

        ReferralCode referralCode = new ReferralCode();
        referralCode.setId(UUID.randomUUID().toString());
        referralCode.setCode(code);
        referralCode.setOwnerId(userId);
        referralCode.setOwnerType("USER");
        referralCode.setDescription("使用者推薦碼");
        referralCode.setRewardGold(0L);
        referralCode.setRewardBonus(DEFAULT_BONUS_PER_USE);
        referralCode.setMaxUsage(DEFAULT_MAX_USAGE);
        referralCode.setUsedCount(0);
        referralCode.setIsActive(true);
        referralCode.setCreatedAt(LocalDateTime.now());
        referralCode.setUpdatedAt(LocalDateTime.now());

        referralCodeMapper.insertSelective(referralCode);
        log.info("✅ 推薦碼產生成功: code={}", code);

        return toReferralCodeRes(referralCode, null);
    }

    @Override
    public ReferralCodeRes validateCodeForUser(String code, String userId) {
        log.info("🔍 驗證推薦碼: code={}, userId={}", code, userId);

        String normalizedCode = normalizeCode(code);
        if (normalizedCode == null) {
            throw new BusinessException("推薦碼不能為空");
        }

        ReferralCode referralCode = referralCodeMapper.selectByCode(normalizedCode);
        if (referralCode == null) {
            throw new BusinessException("推薦碼不存在");
        }
        if (!Boolean.TRUE.equals(referralCode.getIsActive())) {
            throw new BusinessException("推薦碼已停用");
        }
        if (referralCode.getMaxUsage() != null && referralCode.getUsedCount() >= referralCode.getMaxUsage()) {
            throw new BusinessException("推薦碼已達使用上限");
        }
        // Self-referral check
        if (isUserOwnedCode(referralCode) && userId != null && userId.equals(referralCode.getOwnerId())) {
            throw new BusinessException("不能使用自己的推薦碼");
        }

        Store store = referralCode.getStoreId() != null 
                ? storeMapper.selectByPrimaryKey(referralCode.getStoreId()) : null;
        return toReferralCodeRes(referralCode, store != null ? store.getStoreName() : null);
    }

    @Override
    @Transactional
    public void applyReferral(String refereeId, String code) {
        log.info("🎁 套用推薦碼: refereeId={}, code={}", refereeId, code);

        String normalizedCode = normalizeCode(code);
        if (normalizedCode == null) {
            throw new BusinessException("推薦碼不能為空");
        }

        ReferralCode referralCode = referralCodeMapper.selectByCode(normalizedCode);
        if (referralCode == null || !Boolean.TRUE.equals(referralCode.getIsActive())) {
            throw new BusinessException("推薦碼無效或已停用");
        }
        if (referralCode.getMaxUsage() != null && referralCode.getUsedCount() >= referralCode.getMaxUsage()) {
            throw new BusinessException("推薦碼已達使用上限");
        }
        boolean userOwnedCode = isUserOwnedCode(referralCode);
        String referrerId = userOwnedCode ? referralCode.getOwnerId() : null;
        if (userOwnedCode && refereeId.equals(referrerId)) {
            throw new BusinessException("不能使用自己的推薦碼");
        }

        // Check if referee already used a referral code
        List<ReferralRecord> existingUserRecords = referralRecordRepository.selectByUserId(refereeId);
        if (!existingUserRecords.isEmpty()) {
            throw new BusinessException("此使用者已使用過推薦碼");
        }
        ReferralRecordExample existCheck = new ReferralRecordExample();
        existCheck.createCriteria().andRefereeIdEqualTo(refereeId);
        if (referralRecordMapper.countByExample(existCheck) > 0) {
            throw new BusinessException("您已使用過推薦碼");
        }

        long bonusAmount = referralCode.getRewardBonus() != null ? referralCode.getRewardBonus() : DEFAULT_BONUS_PER_USE;

        // Lookup referee username
        User refereeUser = userMapper.selectByPrimaryKey(refereeId);
        String refereeUsername = refereeUser != null ? refereeUser.getNickname() : null;

        // Create referral record
        ReferralRecord record = new ReferralRecord();
        record.setId(UUID.randomUUID().toString());
        record.setUserId(refereeId);
        record.setReferralCodeId(referralCode.getId());
        record.setReferralCode(normalizedCode);
        record.setReferrerId(referrerId);
        record.setRefereeId(refereeId);
        record.setRefereeUsername(refereeUsername);
        record.setUsedCode(normalizedCode);
        record.setStoreId(referralCode.getStoreId());
        record.setRewardBonus(bonusAmount);
        record.setRewardGold(referralCode.getRewardGold() != null ? referralCode.getRewardGold() : 0L);
        record.setIsRewardGiven(false);
        record.setCreatedAt(LocalDateTime.now());
        record.setReferredAt(LocalDateTime.now());

        referralRecordMapper.insertSelective(record);

        // Increment usage count
        referralCodeMapper.incrementUsageCount(referralCode.getId());

        boolean rewardGiven = false;
        if (bonusAmount > 0) {
            try {
                walletService.addBonus(refereeId, bonusAmount, "REFERRAL_BONUS",
                        record.getId(), "使用推薦碼獎勵 - 推薦碼: " + normalizedCode);
                rewardGiven = true;
            } catch (Exception e) {
                log.warn("發送被推薦人推薦獎勵失敗: userId={}, error={}", refereeId, e.getMessage());
            }

            if (userOwnedCode && referrerId != null) {
                try {
                    walletService.addBonus(referrerId, bonusAmount, "REFERRAL_BONUS",
                            record.getId(), "推薦好友獎勵 - 推薦碼: " + normalizedCode);
                } catch (Exception e) {
                    log.warn("發送推薦人推薦獎勵失敗: userId={}, error={}", referrerId, e.getMessage());
                }
            }
        }

        if (rewardGiven) {
            ReferralRecord rewardUpdate = new ReferralRecord();
            rewardUpdate.setId(record.getId());
            rewardUpdate.setIsRewardGiven(true);
            rewardUpdate.setRewardGivenAt(LocalDateTime.now());
            referralRecordMapper.updateByPrimaryKeySelective(rewardUpdate);
        }

        log.info("✅ 推薦碼套用成功: referrer={}, referee={}, bonus={}", referrerId, refereeId, bonusAmount);
    }

    @Override
    public ReferralStatsRes getMyReferralStats(String userId) {
        log.info("📊 取得推薦統計: userId={}", userId);

        // Find user's active referral code
        ReferralCodeExample codeExample = new ReferralCodeExample();
        codeExample.createCriteria()
                .andOwnerIdEqualTo(userId)
                .andOwnerTypeEqualTo("USER")
                .andIsActiveEqualTo(true);
        List<ReferralCode> codes = referralCodeMapper.selectByExample(codeExample);
        ReferralCodeRes activeCode = codes.isEmpty() ? null : toReferralCodeRes(codes.get(0), null);

        // Get referral records where user is the referrer
        ReferralRecordExample recordExample = new ReferralRecordExample();
        recordExample.createCriteria().andReferrerIdEqualTo(userId);
        List<ReferralRecord> records = referralRecordMapper.selectByExample(recordExample);

        int totalReferrals = records.size();
        long totalBonusEarned = records.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsRewardGiven()))
                .mapToLong(r -> r.getRewardBonus() != null ? r.getRewardBonus() : 0L)
                .sum();

        List<ReferralStatsRes.ReferralHistoryItem> history = records.stream()
                .map(r -> ReferralStatsRes.ReferralHistoryItem.builder()
                        .refereeId(r.getRefereeId())
                        .refereeUsername(r.getRefereeUsername())
                        .bonusAmount(r.getRewardBonus())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ReferralStatsRes.builder()
                .totalReferrals(totalReferrals)
                .totalBonusEarned(totalBonusEarned)
                .activeCode(activeCode)
                .referralHistory(history)
                .build();
    }

    @Override
    @Transactional
    public void disableCode(String codeId, String userId) {
        log.info("🚫 停用推薦碼: codeId={}, userId={}", codeId, userId);

        ReferralCode referralCode = referralCodeMapper.selectByPrimaryKey(codeId);
        if (referralCode == null) {
            throw new BusinessException("推薦碼不存在");
        }
        // Ownership check
        if (!referralCode.getOwnerId().equals(userId)) {
            throw new BusinessException("無權限停用此推薦碼");
        }

        referralCode.setIsActive(false);
        referralCode.setUpdatedAt(LocalDateTime.now());
        referralCodeMapper.updateByPrimaryKeySelective(referralCode);

        log.info("✅ 推薦碼已停用: codeId={}", codeId);
    }

    @Override
    public ReferralCodeRes disableCode(String id) {
        log.info("🚫 管理端停用推薦碼: id={}", id);
        ReferralCode referralCode = referralCodeMapper.selectByPrimaryKey(id);
        if (referralCode == null) {
            throw new BusinessException("推薦碼不存在");
        }
        referralCode.setIsActive(false);
        referralCode.setUpdatedAt(LocalDateTime.now());
        referralCodeMapper.updateByPrimaryKeySelective(referralCode);
        Store store = storeMapper.selectByPrimaryKey(referralCode.getStoreId());
        return toReferralCodeRes(referralCode, store != null ? store.getStoreName() : null);
    }

    // ========== Helper methods ==========

    private String normalizeCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return code.trim().toUpperCase();
    }

    private String normalizeCodeOrGenerate(String code) {
        String normalizedCode = normalizeCode(code);
        return normalizedCode != null ? normalizedCode : generateUniqueCode();
    }

    private String normalizeId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return id.trim();
    }

    private void assertStoreHasNoOtherReferralCode(String storeId, String currentCodeId) {
        List<ReferralCode> existingStoreCodes = referralCodeRepository.selectByStoreId(storeId);
        boolean hasOtherCode = existingStoreCodes.stream()
                .anyMatch(code -> currentCodeId == null || !currentCodeId.equals(code.getId()));
        if (hasOtherCode) {
            throw new BusinessException("此店家已有推薦碼，一個店家只能設定一個推薦碼");
        }
    }

    private boolean isUserOwnedCode(ReferralCode referralCode) {
        return referralCode != null && "USER".equalsIgnoreCase(referralCode.getOwnerType());
    }

    private String generateUniqueCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
            }
            String code = sb.toString();
            if (referralCodeMapper.selectByCode(code) == null) {
                return code;
            }
        }
        // Fallback: UUID-based
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private ReferralCodeRes toReferralCodeRes(ReferralCode code, String storeName) {
        ReferralCodeRes res = new ReferralCodeRes();
        res.setId(code.getId());
        res.setCode(code.getCode());
        res.setStoreId(code.getStoreId());
        res.setStoreName(storeName);
        res.setDescription(code.getDescription());
        res.setIsActive(Boolean.TRUE.equals(code.getIsActive()));
        res.setUsedCount(code.getUsedCount());
        res.setCreatedAt(code.getCreatedAt());
        res.setUpdatedAt(code.getUpdatedAt());
        return res;
    }
    
    private ReferralRecordRes toReferralRecordRes(ReferralRecord record) {
        ReferralRecordRes res = new ReferralRecordRes();
        String referredUserId = record.getUserId() != null ? record.getUserId() : record.getRefereeId();
        res.setId(record.getId());
        res.setUserId(referredUserId);
        res.setRefereeUsername(record.getRefereeUsername());
        res.setReferralCodeId(record.getReferralCodeId());
        res.setUsedCode(record.getUsedCode());
        res.setStoreId(record.getStoreId());
        res.setRewardBonus(record.getRewardBonus());
        res.setIsRewardGiven(record.getIsRewardGiven());
        res.setRewardGivenAt(record.getRewardGivenAt());
        res.setReferredAt(record.getReferredAt());
        res.setCreatedAt(record.getCreatedAt());
        
        User user = userMapper.selectByPrimaryKey(referredUserId);
        if (user != null) {
            res.setUserName(user.getNickname());
        } else {
            res.setUserName(record.getRefereeUsername());
        }
        
        Store store = storeMapper.selectByPrimaryKey(record.getStoreId());
        if (store != null) {
            res.setStoreName(store.getStoreName());
        }
        
        return res;
    }

    @Override
    public List<com.group.admin.res.referral.AdminReferralStatsRes> getReferralStats(com.group.admin.condition.report.ReferralReportCondition condition) {
        log.info("📊 [Referral] 查詢推薦統計：condition={}", condition);
        List<java.util.Map<String, Object>> storeStats = referralCodeRepository.selectStatsByStore(condition);
        List<java.util.Map<String, Object>> timelineRows = referralRecordRepository.selectTimelineByStore(condition);
        java.util.Map<String, List<com.group.admin.res.referral.AdminReferralStatsRes.DailyCount>> timelineByStore =
                timelineRows.stream().collect(Collectors.groupingBy(
                        row -> String.valueOf(row.get("storeId")),
                        Collectors.mapping(row -> new com.group.admin.res.referral.AdminReferralStatsRes.DailyCount(
                                        String.valueOf(row.get("referralDate")),
                                        toLong(row.get("dailyCount"))),
                                Collectors.toList())));

        return storeStats.stream()
                .map(row -> new com.group.admin.res.referral.AdminReferralStatsRes(
                        String.valueOf(row.get("storeId")),
                        String.valueOf(row.get("storeName")),
                        toLong(row.get("totalReferrals")),
                        toLong(row.get("activeCodeCount")),
                        timelineByStore.getOrDefault(String.valueOf(row.get("storeId")), List.of())))
                .collect(Collectors.toList());
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
