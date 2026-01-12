package com.group.admin.service.impl;

import com.group.admin.entity.ReferralCode;
import com.group.admin.entity.ReferralRecord;
import com.group.admin.entity.Store;
import com.group.admin.entity.User;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.ReferralCodeMapper;
import com.group.admin.mapper.ReferralRecordMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.referral.ReferralCodeCreateReq;
import com.group.admin.req.referral.ReferralCodeUpdateReq;
import com.group.admin.res.referral.ReferralCodeRes;
import com.group.admin.res.referral.ReferralRecordRes;
import com.group.admin.service.ReferralCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 推薦碼服務實作
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralCodeServiceImpl implements ReferralCodeService {
    
    private final ReferralCodeMapper referralCodeMapper;
    private final ReferralRecordMapper referralRecordMapper;
    private final StoreMapper storeMapper;
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public ReferralCodeRes create(ReferralCodeCreateReq req) {
        log.info("🎫 建立推薦碼: code={}, storeId={}", req.getCode(), req.getStoreId());
        
        // 檢查推薦碼是否已存在
        ReferralCode existingCode = referralCodeMapper.selectByCode(req.getCode());
        if (existingCode != null) {
            throw new BusinessException("推薦碼已存在: " + req.getCode());
        }
        
        // 檢查店家是否存在
        Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
        if (store == null) {
            throw new BusinessException("店家不存在: " + req.getStoreId());
        }
        
        // 建立推薦碼
        ReferralCode referralCode = new ReferralCode();
        referralCode.setId(UUID.randomUUID().toString());
        referralCode.setCode(req.getCode().toUpperCase());
        referralCode.setStoreId(req.getStoreId());
        referralCode.setDescription(req.getDescription());
        referralCode.setIsActive((byte) 1);
        referralCode.setUsedCount(0);
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
        
        // 更新欄位
        if (req.getDescription() != null) {
            referralCode.setDescription(req.getDescription());
        }
        if (req.getIsActive() != null) {
            referralCode.setIsActive(req.getIsActive() ? (byte) 1 : (byte) 0);
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
        ReferralCode referralCode = referralCodeMapper.selectByCode(code.toUpperCase());
        if (referralCode == null) {
            return null;
        }
        
        Store store = storeMapper.selectByPrimaryKey(referralCode.getStoreId());
        return toReferralCodeRes(referralCode, store != null ? store.getStoreName() : null);
    }
    
    @Override
    public List<ReferralCodeRes> getByStoreId(String storeId) {
        List<ReferralCode> codes = referralCodeMapper.selectByStoreId(storeId);
        Store store = storeMapper.selectByPrimaryKey(storeId);
        String storeName = store != null ? store.getStoreName() : null;
        
        return codes.stream()
                .map(code -> toReferralCodeRes(code, storeName))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReferralCodeRes> getAll() {
        List<ReferralCode> codes = referralCodeMapper.selectAll();
        
        return codes.stream()
                .map(code -> {
                    Store store = storeMapper.selectByPrimaryKey(code.getStoreId());
                    return toReferralCodeRes(code, store != null ? store.getStoreName() : null);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean validateCode(String code) {
        ReferralCode referralCode = referralCodeMapper.selectByCode(code.toUpperCase());
        return referralCode != null && referralCode.getIsActive() == 1;
    }
    
    @Override
    @Transactional
    public boolean useCode(String userId, String code) {
        log.info("🎁 使用推薦碼: userId={}, code={}", userId, code);
        
        // 檢查推薦碼是否有效
        ReferralCode referralCode = referralCodeMapper.selectByCode(code.toUpperCase());
        if (referralCode == null || referralCode.getIsActive() != 1) {
            log.warn("❌ 推薦碼無效或已停用: {}", code);
            return false;
        }
        
        // 檢查使用者是否已使用過推薦碼
        ReferralRecord existingRecord = referralRecordMapper.selectByUserId(userId);
        if (existingRecord != null) {
            log.warn("❌ 使用者已使用過推薦碼: userId={}", userId);
            return false;
        }
        
        // 建立推薦記錄
        ReferralRecord record = new ReferralRecord();
        record.setId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setReferralCodeId(referralCode.getId());
        record.setStoreId(referralCode.getStoreId());
        record.setUsedCode(code.toUpperCase());
        record.setReferredAt(LocalDateTime.now());
        
        referralRecordMapper.insert(record);
        
        // 更新推薦碼使用次數
        referralCode.setUsedCount(referralCode.getUsedCount() + 1);
        referralCode.setUpdatedAt(LocalDateTime.now());
        referralCodeMapper.updateByPrimaryKey(referralCode);
        
        log.info("✅ 推薦碼使用成功: userId={}, code={}", userId, code);
        return true;
    }
    
    @Override
    public List<ReferralRecordRes> getRecordsByCodeId(String referralCodeId) {
        List<ReferralRecord> records = referralRecordMapper.selectByReferralCodeId(referralCodeId);
        return records.stream()
                .map(this::toReferralRecordRes)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReferralRecordRes> getRecordsByStoreId(String storeId) {
        List<ReferralRecord> records = referralRecordMapper.selectByStoreId(storeId);
        return records.stream()
                .map(this::toReferralRecordRes)
                .collect(Collectors.toList());
    }
    
    @Override
    public ReferralRecordRes getRecordByUserId(String userId) {
        ReferralRecord record = referralRecordMapper.selectByUserId(userId);
        return record != null ? toReferralRecordRes(record) : null;
    }
    
    /**
     * 轉換為推薦碼回應
     */
    private ReferralCodeRes toReferralCodeRes(ReferralCode code, String storeName) {
        ReferralCodeRes res = new ReferralCodeRes();
        res.setId(code.getId());
        res.setCode(code.getCode());
        res.setStoreId(code.getStoreId());
        res.setStoreName(storeName);
        res.setDescription(code.getDescription());
        res.setIsActive(code.getIsActive() == 1);
        res.setUsedCount(code.getUsedCount());
        res.setCreatedAt(code.getCreatedAt());
        res.setUpdatedAt(code.getUpdatedAt());
        return res;
    }
    
    /**
     * 轉換為推薦記錄回應
     */
    private ReferralRecordRes toReferralRecordRes(ReferralRecord record) {
        ReferralRecordRes res = new ReferralRecordRes();
        res.setId(record.getId());
        res.setUserId(record.getUserId());
        res.setReferralCodeId(record.getReferralCodeId());
        res.setUsedCode(record.getUsedCode());
        res.setStoreId(record.getStoreId());
        res.setReferredAt(record.getReferredAt());
        
        // 查詢使用者名稱
        User user = userMapper.selectByPrimaryKey(record.getUserId());
        if (user != null) {
            res.setUserName(user.getNickname());
        }
        
        // 查詢店家名稱
        Store store = storeMapper.selectByPrimaryKey(record.getStoreId());
        if (store != null) {
            res.setStoreName(store.getStoreName());
        }
        
        return res;
    }
}
