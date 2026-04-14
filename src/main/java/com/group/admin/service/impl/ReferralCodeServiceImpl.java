package com.group.admin.service.impl;

import com.group.admin.condition.report.ReferralReportCondition;
import com.group.admin.entity.AdminUser;
import com.group.admin.entity.ReferralCode;
import com.group.admin.entity.ReferralRecord;
import com.group.admin.entity.Store;
import com.group.admin.entity.StoreUser;
import com.group.admin.entity.User;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.ReferralCodeExample;
import com.group.admin.example.ReferralRecordExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.ReferralCodeMapper;
import com.group.admin.mapper.ReferralRecordMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.repository.ReferralCodeRepository;
import com.group.admin.repository.ReferralRecordRepository;
import com.group.admin.req.referral.ReferralCodeCreateReq;
import com.group.admin.req.referral.ReferralCodeUpdateReq;
import com.group.admin.res.referral.AdminReferralStatsRes;
import com.group.admin.res.referral.ReferralCodeRes;
import com.group.admin.res.referral.ReferralRecordRes;
import com.group.admin.res.referral.ReferralStatsRes;
import com.group.admin.res.referral.ReferralValidateRes;
import com.group.admin.service.ReferralCodeService;
import com.group.admin.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final AdminUserMapper adminUserMapper;
    private final StoreUserMapper storeUserMapper;
    private final WalletService walletService;

    private static final long DEFAULT_BONUS_PER_USE = 50L;
    private static final int DEFAULT_MAX_USAGE = 100;
    
    @Override
    @Transactional
    public ReferralCodeRes create(ReferralCodeCreateReq req) {
        log.info("🎫 建立推薦碼: storeId={}", req.getStoreId());

        Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
        if (store == null) {
            throw new BusinessException("店家不存在: " + req.getStoreId());
        }

        // Auto-generate 8-char uppercase alphanumeric code with collision retry (max 5)
        String code;
        if (req.getCode() != null && !req.getCode().trim().isEmpty()) {
            code = req.getCode().trim().toUpperCase();
            if (referralCodeRepository.selectByCode(code) != null) {
                throw new BusinessException("推薦碼已存在: " + code);
            }
        } else {
            code = null;
            for (int attempt = 0; attempt < 5; attempt++) {
                String candidate = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
                if (referralCodeRepository.selectByCode(candidate) == null) {
                    code = candidate;
                    break;
                }
            }
            if (code == null) {
                throw new BusinessException("推薦碼生成失敗，請重試");
            }
        }
        log.info("🎫 生成推薦碼: code={}", code);

        ReferralCode referralCode = new ReferralCode();
        referralCode.setId(UUID.randomUUID().toString());
        referralCode.setCode(code);
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
    public List<ReferralCodeRes> getAll(String storeId, Boolean isActive) {
        ReferralCodeExample example = new ReferralCodeExample();
        ReferralCodeExample.Criteria criteria = example.createCriteria();
        if (storeId != null && !storeId.isEmpty()) {
            criteria.andStoreIdEqualTo(storeId);
        }
        if (isActive != null) {
            criteria.andIsActiveEqualTo(isActive);
        }
        example.setOrderByClause("created_at DESC");
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

    // ===== US1: Admin disable code (T008) =====

    @Override
    @Transactional
    public ReferralCodeRes disableCode(String id) {
        log.info("🚫 [Admin] 停用推薦碼: id={}", id);

        ReferralCode code = referralCodeMapper.selectByPrimaryKey(id);
        if (code == null) {
            throw new BusinessException("推薦碼不存在");
        }
        if (!Boolean.TRUE.equals(code.getIsActive())) {
            throw new BusinessException("推薦碼已經是停用狀態");
        }

        code.setIsActive(false);
        code.setUpdatedAt(LocalDateTime.now());
        referralCodeMapper.updateByPrimaryKeySelective(code);
        log.info("✅ 推薦碼已停用: id={}", id);

        Store store = storeMapper.selectByPrimaryKey(code.getStoreId());
        return toReferralCodeRes(code, store != null ? store.getStoreName() : null);
    }

    // ===== US2: Validate & registration (T011) =====

    @Override
    public ReferralValidateRes validateForRegistration(String code) {
        if (code == null) return new ReferralValidateRes(false, null, null);

        String normalised = code.trim().toUpperCase();

        ReferralCode referralCode = referralCodeRepository.selectByCode(normalised);
        if (referralCode == null || !Boolean.TRUE.equals(referralCode.getIsActive())) {
            return new ReferralValidateRes(false, normalised, null);
        }

        Store store = referralCode.getStoreId() != null
                ? storeMapper.selectByPrimaryKey(referralCode.getStoreId()) : null;
        if (store == null || !"ACTIVE".equals(store.getStatus())) {
            return new ReferralValidateRes(false, normalised, null);
        }

        if (referralCode.getMaxUsage() != null
                && referralCode.getUsedCount() >= referralCode.getMaxUsage()) {
            return new ReferralValidateRes(false, normalised, null);
        }

        if (referralCode.getValidUntil() != null
                && LocalDateTime.now().isAfter(referralCode.getValidUntil())) {
            return new ReferralValidateRes(false, normalised, null);
        }

        return new ReferralValidateRes(true, normalised, store.getStoreName());
    }

    // ===== US2: useCode with protection (T014) =====

    @Override
    @Transactional
    public void useCode(String userId, String code, String registrationEmail) {
        log.info("🎁 使用推薦碼（含防護）: userId={}, code={}", userId, code);

        String normalised = code.trim().toUpperCase();

        ReferralCode referralCode = referralCodeRepository.selectByCode(normalised);
        if (referralCode == null) {
            throw new BusinessException("推薦碼不存在");
        }
        if (!Boolean.TRUE.equals(referralCode.getIsActive())) {
            throw new BusinessException("REFERRAL_CODE_DISABLED", "推薦碼已停用");
        }

        // Store active check
        Store store = referralCode.getStoreId() != null
                ? storeMapper.selectByPrimaryKey(referralCode.getStoreId()) : null;
        if (store == null || !"ACTIVE".equals(store.getStatus())) {
            throw new BusinessException("STORE_INACTIVE", "店家已停用");
        }

        // Self-referral check: find admin user(s) linked to the store, compare email
        if (registrationEmail != null && referralCode.getStoreId() != null) {
            StoreUserExample suExample = new StoreUserExample();
            suExample.createCriteria().andStoreIdEqualTo(referralCode.getStoreId());
            List<StoreUser> storeUsers = storeUserMapper.selectByExample(suExample);
            for (StoreUser su : storeUsers) {
                AdminUser adminUser = adminUserMapper.selectByPrimaryKey(su.getAdminUserId());
                if (adminUser != null
                        && registrationEmail.equalsIgnoreCase(adminUser.getEmail())) {
                    throw new BusinessException("SELF_REFERRAL_NOT_ALLOWED", "不能使用店家負責人的推薦碼");
                }
            }
        }

        // Usage limit check
        if (referralCode.getMaxUsage() != null
                && referralCode.getUsedCount() >= referralCode.getMaxUsage()) {
            throw new BusinessException("推薦碼已達使用上限");
        }

        // Duplicate use check
        List<ReferralRecord> existing = referralRecordRepository.selectByUserId(userId);
        if (!existing.isEmpty()) {
            throw new BusinessException("您已使用過推薦碼");
        }

        ReferralRecord record = new ReferralRecord();
        record.setId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setReferralCodeId(referralCode.getId());
        record.setStoreId(referralCode.getStoreId());
        record.setUsedCode(normalised);
        record.setReferredAt(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());

        referralRecordMapper.insert(record);
        referralCodeMapper.incrementUsageCount(referralCode.getId());

        log.info("✅ 推薦碼使用成功: userId={}, code={}", userId, normalised);
    }

    // ===== US3: Admin referral stats (T019) =====

    @Override
    public List<AdminReferralStatsRes> getReferralStats(ReferralReportCondition condition) {
        if (condition == null) condition = new ReferralReportCondition();

        // Default date range: last 30 days
        if (condition.getStartDate() == null) {
            condition.setStartDate(LocalDate.now().minusDays(30));
        }
        if (condition.getEndDate() == null) {
            condition.setEndDate(LocalDate.now());
        }

        // Validate date range
        if (condition.getStartDate().isAfter(condition.getEndDate())) {
            throw new BusinessException("開始日期不能晚於結束日期");
        }

        List<Map<String, Object>> statsRows =
                referralCodeRepository.selectStatsByStore(condition);
        List<Map<String, Object>> timelineRows =
                referralRecordRepository.selectTimelineByStore(condition);

        // Group timeline by storeId
        Map<String, List<AdminReferralStatsRes.DailyCount>> timelineMap = new LinkedHashMap<>();
        for (Map<String, Object> row : timelineRows) {
            String storeId = (String) row.get("storeId");
            String date = row.get("referralDate") != null ? row.get("referralDate").toString() : null;
            Long count = toLong(row.get("dailyCount"));
            timelineMap.computeIfAbsent(storeId, k -> new ArrayList<>())
                    .add(new AdminReferralStatsRes.DailyCount(date, count));
        }

        List<AdminReferralStatsRes> result = new ArrayList<>();
        for (Map<String, Object> row : statsRows) {
            String storeId = (String) row.get("storeId");
            String storeName = (String) row.get("storeName");
            Long totalReferrals = toLong(row.get("totalReferrals"));
            Long activeCodeCount = toLong(row.get("activeCodeCount"));
            List<AdminReferralStatsRes.DailyCount> timeline =
                    timelineMap.getOrDefault(storeId, new ArrayList<>());
            result.add(new AdminReferralStatsRes(storeId, storeName, totalReferrals,
                    activeCodeCount, timeline));
        }
        return result;
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
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
