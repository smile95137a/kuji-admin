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
import com.group.admin.service.WalletService;
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
    private final WalletService walletService;

    private static final long DEFAULT_BONUS_PER_USE = 50L;
    private static final int DEFAULT_MAX_USAGE = 100;
    
    @Override
    @Transactional
    public ReferralCodeRes create(ReferralCodeCreateReq req) {
        log.info("🎫 建立推薦碼: code={}, storeId={}", req.getCode(), req.getStoreId());
        
        ReferralCode existingCode = referralCodeRepository.selectByCode(req.getCode());
        if (existingCode != null) {
            throw new BusinessException("推薦碼已存在: " + req.getCode());
        }
        
        Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
        if (store == null) {
            throw new BusinessException("店家不存在: " + req.getStoreId());
        }
        
        ReferralCode referralCode = new ReferralCode();
        referralCode.setId(UUID.randomUUID().toString());
        referralCode.setCode(req.getCode().toUpperCase());
        referralCode.setStoreId(req.getStoreId());
        referralCode.setDescription(req.getDescription());
        referralCode.setOwnerId(req.getStoreId());
        referralCode.setOwnerType("STORE");
        referralCode.setIsActive(true);
        referralCode.setUsedCount(0);
        referralCode.setMaxUsage(DEFAULT_MAX_USAGE);
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
        ReferralCode referralCode = referralCodeRepository.selectByCode(code.toUpperCase());
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
    public List<ReferralCodeRes> getAll() {
        List<ReferralCode> codes = referralCodeRepository.selectAll();
        
        return codes.stream()
                .map(code -> {
                    Store store = storeMapper.selectByPrimaryKey(code.getStoreId());
                    return toReferralCodeRes(code, store != null ? store.getStoreName() : null);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean validateCode(String code) {
        ReferralCode referralCode = referralCodeRepository.selectByCode(code.toUpperCase());
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
        
        ReferralCode referralCode = referralCodeRepository.selectByCode(code.toUpperCase());
        if (referralCode == null || !Boolean.TRUE.equals(referralCode.getIsActive())) {
            log.warn("❌ 推薦碼無效或已停用: {}", code);
            return false;
        }
        
        List<ReferralRecord> existingRecords = referralRecordRepository.selectByUserId(userId);
        if (!existingRecords.isEmpty()) {
            log.warn("❌ 使用者已使用過推薦碼: userId={}", userId);
            return false;
        }
        
        ReferralRecord record = new ReferralRecord();
        record.setId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setReferralCodeId(referralCode.getId());
        record.setStoreId(referralCode.getStoreId());
        record.setUsedCode(code.toUpperCase());
        record.setReferredAt(LocalDateTime.now());
        
        referralRecordMapper.insert(record);
        
        referralCodeMapper.incrementUsageCount(referralCode.getId());
        
        log.info("✅ 推薦碼使用成功: userId={}, code={}", userId, code);
        return true;
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

        ReferralCode referralCode = referralCodeMapper.selectByCode(code.toUpperCase());
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
        if (userId != null && userId.equals(referralCode.getOwnerId())) {
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

        ReferralCode referralCode = referralCodeMapper.selectByCode(code.toUpperCase());
        if (referralCode == null || !Boolean.TRUE.equals(referralCode.getIsActive())) {
            throw new BusinessException("推薦碼無效或已停用");
        }
        if (referralCode.getMaxUsage() != null && referralCode.getUsedCount() >= referralCode.getMaxUsage()) {
            throw new BusinessException("推薦碼已達使用上限");
        }
        String referrerId = referralCode.getOwnerId();
        if (refereeId.equals(referrerId)) {
            throw new BusinessException("不能使用自己的推薦碼");
        }

        // Check if referee already used a referral code
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
        record.setReferralCodeId(referralCode.getId());
        record.setReferrerId(referrerId);
        record.setRefereeId(refereeId);
        record.setRefereeUsername(refereeUsername);
        record.setUsedCode(code.toUpperCase());
        record.setStoreId(referralCode.getStoreId());
        record.setRewardBonus(bonusAmount);
        record.setIsRewardGiven(true);
        record.setRewardGivenAt(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setReferredAt(LocalDateTime.now());

        referralRecordMapper.insertSelective(record);

        // Increment usage count
        referralCodeMapper.incrementUsageCount(referralCode.getId());

        // Award bonus to both parties
        try {
            walletService.addBonus(referrerId, bonusAmount, "REFERRAL_BONUS",
                    record.getId(), "推薦獎勵 - 推薦碼: " + code);
            walletService.addBonus(refereeId, bonusAmount, "REFERRAL_BONUS",
                    record.getId(), "被推薦獎勵 - 推薦碼: " + code);
        } catch (Exception e) {
            log.warn("⚠️ 發放推薦獎勵失敗（錢包可能不存在）: {}", e.getMessage());
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

    // ========== Helper methods ==========

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
        res.setId(record.getId());
        res.setUserId(record.getUserId());
        res.setReferralCodeId(record.getReferralCodeId());
        res.setUsedCode(record.getUsedCode());
        res.setStoreId(record.getStoreId());
        res.setReferredAt(record.getReferredAt());
        
        User user = userMapper.selectByPrimaryKey(record.getUserId());
        if (user != null) {
            res.setUserName(user.getNickname());
        }
        
        Store store = storeMapper.selectByPrimaryKey(record.getStoreId());
        if (store != null) {
            res.setStoreName(store.getStoreName());
        }
        
        return res;
    }
}
