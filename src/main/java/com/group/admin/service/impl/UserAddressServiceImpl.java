package com.group.admin.service.impl;

import com.group.admin.entity.UserAddress;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.UserAddressMapper;
import com.group.admin.repository.UserAddressRepository;
import com.group.admin.req.address.UserAddressCreateReq;
import com.group.admin.req.address.UserAddressUpdateReq;
import com.group.admin.res.address.UserAddressRes;
import com.group.admin.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 使用者地址服務實作
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {
    
    private final UserAddressMapper userAddressMapper;
    private final UserAddressRepository userAddressRepository;
    
    @Override
    @Transactional
    public UserAddressRes create(String userId, UserAddressCreateReq req) {
        log.info("📍 新增收件地址: userId={}", userId);
        
        // 如果設為預設地址，先清除其他預設
        if (req.getIsDefault() != null && req.getIsDefault()) {
            userAddressRepository.clearDefaultByUserId(userId);
        }
        
        // 如果是第一個地址，自動設為預設
        List<UserAddress> existingAddresses = userAddressRepository.selectByUserId(userId);
        boolean isFirst = existingAddresses.isEmpty();
        
        UserAddress address = new UserAddress();
        address.setId(UUID.randomUUID().toString());
        address.setUserId(userId);
        address.setRecipientName(req.getRecipientName());
        address.setRecipientPhone(req.getRecipientPhone());
        address.setCity(req.getCity());
        address.setDistrict(req.getDistrict());
        address.setPostalCode(req.getZipCode());
        address.setAddress(req.getAddress());
        address.setIsDefault(isFirst || (req.getIsDefault() != null && req.getIsDefault()));
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        
        userAddressMapper.insert(address);
        log.info("✅ 收件地址新增成功: id={}", address.getId());
        
        return toUserAddressRes(address);
    }
    
    @Override
    @Transactional
    public UserAddressRes update(String userId, String addressId, UserAddressUpdateReq req) {
        log.info("📝 更新收件地址: userId={}, addressId={}", userId, addressId);
        
        UserAddress address = userAddressMapper.selectByPrimaryKey(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或無權限操作");
        }
        
        // 如果設為預設地址，先清除其他預設
        if (req.getIsDefault() != null && req.getIsDefault()) {
            userAddressRepository.clearDefaultByUserId(userId);
        }
        
        // 更新欄位
        if (req.getRecipientName() != null) {
            address.setRecipientName(req.getRecipientName());
        }
        if (req.getRecipientPhone() != null) {
            address.setRecipientPhone(req.getRecipientPhone());
        }
        if (req.getCity() != null) {
            address.setCity(req.getCity());
        }
        if (req.getDistrict() != null) {
            address.setDistrict(req.getDistrict());
        }
        if (req.getZipCode() != null) {
            address.setPostalCode(req.getZipCode());
        }
        if (req.getAddress() != null) {
            address.setAddress(req.getAddress());
        }
        if (req.getIsDefault() != null) {
            address.setIsDefault(req.getIsDefault());
        }
        address.setUpdatedAt(LocalDateTime.now());
        
        userAddressMapper.updateByPrimaryKey(address);
        log.info("✅ 收件地址更新成功: id={}", addressId);
        
        return toUserAddressRes(address);
    }
    
    @Override
    @Transactional
    public void delete(String userId, String addressId) {
        log.info("🗑️ 刪除收件地址: userId={}, addressId={}", userId, addressId);
        
        UserAddress address = userAddressMapper.selectByPrimaryKey(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或無權限操作");
        }
        
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        userAddressMapper.deleteByPrimaryKey(addressId);
        
        // 如果刪除的是預設地址，自動將第一個設為預設
        if (wasDefault) {
            List<UserAddress> remainingAddresses = userAddressRepository.selectByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                UserAddress firstAddress = remainingAddresses.get(0);
                firstAddress.setIsDefault(true);
                firstAddress.setUpdatedAt(LocalDateTime.now());
                userAddressMapper.updateByPrimaryKey(firstAddress);
            }
        }
        
        log.info("✅ 收件地址刪除成功: id={}", addressId);
    }
    
    @Override
    public UserAddressRes getById(String userId, String addressId) {
        UserAddress address = userAddressMapper.selectByPrimaryKey(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或無權限操作");
        }
        return toUserAddressRes(address);
    }
    
    @Override
    public List<UserAddressRes> getByUserId(String userId) {
        List<UserAddress> addresses = userAddressRepository.selectByUserId(userId);
        return addresses.stream()
                .map(this::toUserAddressRes)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserAddressRes getDefaultByUserId(String userId) {
        UserAddress address = userAddressRepository.selectDefaultByUserId(userId);
        return address != null ? toUserAddressRes(address) : null;
    }
    
    @Override
    @Transactional
    public UserAddressRes setDefault(String userId, String addressId) {
        log.info("⭐ 設定預設地址: userId={}, addressId={}", userId, addressId);
        
        UserAddress address = userAddressMapper.selectByPrimaryKey(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或無權限操作");
        }
        
        // 清除其他預設
        userAddressRepository.clearDefaultByUserId(userId);
        
        // 設定為預設
        address.setIsDefault(true);
        address.setUpdatedAt(LocalDateTime.now());
        userAddressMapper.updateByPrimaryKey(address);
        
        log.info("✅ 預設地址設定成功: id={}", addressId);
        return toUserAddressRes(address);
    }
    
    /**
     * 轉換為地址回應
     */
    private UserAddressRes toUserAddressRes(UserAddress address) {
        UserAddressRes res = new UserAddressRes();
        res.setId(address.getId());
        res.setUserId(address.getUserId());
        res.setLabel(null); // Entity 沒有 label 欄位
        res.setRecipientName(address.getRecipientName());
        res.setRecipientPhone(address.getRecipientPhone());
        res.setCity(address.getCity());
        res.setDistrict(address.getDistrict());
        res.setZipCode(address.getPostalCode()); // Entity 使用 postalCode
        res.setAddress(address.getAddress());
        res.setIsDefault(Boolean.TRUE.equals(address.getIsDefault()));
        res.setCreatedAt(address.getCreatedAt());
        res.setUpdatedAt(address.getUpdatedAt());
        
        // 組合完整地址
        StringBuilder fullAddress = new StringBuilder();
        if (address.getPostalCode() != null && !address.getPostalCode().isEmpty()) {
            fullAddress.append(address.getPostalCode()).append(" ");
        }
        if (address.getCity() != null) {
            fullAddress.append(address.getCity());
        }
        if (address.getDistrict() != null) {
            fullAddress.append(address.getDistrict());
        }
        if (address.getAddress() != null) {
            fullAddress.append(address.getAddress());
        }
        res.setFullAddress(fullAddress.toString());
        
        return res;
    }
}
