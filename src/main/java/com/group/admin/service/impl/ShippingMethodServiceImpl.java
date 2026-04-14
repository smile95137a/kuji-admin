package com.group.admin.service.impl;

import com.group.admin.entity.ShippingMethod;
import com.group.admin.example.ShippingMethodExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.ShippingMethodMapper;
import com.group.admin.req.shippingmethod.ShippingMethodCreateReq;
import com.group.admin.req.shippingmethod.ShippingMethodUpdateReq;
import com.group.admin.res.shippingmethod.ShippingMethodRes;
import com.group.admin.service.ShippingMethodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingMethodServiceImpl implements ShippingMethodService {

    private final ShippingMethodMapper shippingMethodMapper;

    @Override
    public List<ShippingMethodRes> listAll() {
        ShippingMethodExample example = new ShippingMethodExample();
        example.setOrderByClause("sort_order ASC, created_at ASC");
        return shippingMethodMapper.selectByExample(example).stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShippingMethodRes> listActive() {
        ShippingMethodExample example = new ShippingMethodExample();
        example.createCriteria().andStatusEqualTo("ACTIVE");
        example.setOrderByClause("sort_order ASC, created_at ASC");
        return shippingMethodMapper.selectByExample(example).stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShippingMethodRes create(ShippingMethodCreateReq req) {
        // 檢查 code 唯一性
        ShippingMethodExample codeCheck = new ShippingMethodExample();
        codeCheck.createCriteria().andCodeEqualTo(req.getCode());
        if (shippingMethodMapper.countByExample(codeCheck) > 0) {
            throw new BusinessException("代碼已存在：" + req.getCode());
        }

        ShippingMethod entity = new ShippingMethod();
        entity.setId(UUID.randomUUID().toString());
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setProvider(req.getProvider());
        entity.setFee(req.getFee());
        entity.setStatus("ACTIVE");
        entity.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        shippingMethodMapper.insert(entity);
        log.info("✅ 建立運送方式：id={}, code={}", entity.getId(), entity.getCode());
        return toRes(entity);
    }

    @Override
    @Transactional
    public ShippingMethodRes update(String id, ShippingMethodUpdateReq req) {
        ShippingMethod entity = shippingMethodMapper.selectByPrimaryKey(id);
        if (entity == null) {
            throw new BusinessException("運送方式不存在：" + id);
        }

        // 若更新 code，檢查唯一性
        if (req.getCode() != null && !req.getCode().equals(entity.getCode())) {
            ShippingMethodExample codeCheck = new ShippingMethodExample();
            codeCheck.createCriteria().andCodeEqualTo(req.getCode());
            if (shippingMethodMapper.countByExample(codeCheck) > 0) {
                throw new BusinessException("代碼已存在：" + req.getCode());
            }
        }

        if (req.getName() != null) entity.setName(req.getName());
        if (req.getCode() != null) entity.setCode(req.getCode());
        if (req.getProvider() != null) entity.setProvider(req.getProvider());
        if (req.getFee() != null) entity.setFee(req.getFee());
        if (req.getStatus() != null) entity.setStatus(req.getStatus());
        if (req.getSortOrder() != null) entity.setSortOrder(req.getSortOrder());
        entity.setUpdatedAt(LocalDateTime.now());

        shippingMethodMapper.updateByPrimaryKey(entity);
        log.info("✅ 更新運送方式：id={}", id);
        return toRes(entity);
    }

    @Override
    @Transactional
    public void updateStatus(String id, String status) {
        ShippingMethod entity = shippingMethodMapper.selectByPrimaryKey(id);
        if (entity == null) {
            throw new BusinessException("運送方式不存在：" + id);
        }
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new BusinessException("無效的狀態值：" + status);
        }
        entity.setStatus(status);
        entity.setUpdatedAt(LocalDateTime.now());
        shippingMethodMapper.updateByPrimaryKeySelective(entity);
        log.info("✅ 更新運送方式狀態：id={}, status={}", id, status);
    }

    private ShippingMethodRes toRes(ShippingMethod entity) {
        return ShippingMethodRes.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .provider(entity.getProvider())
                .fee(entity.getFee())
                .status(entity.getStatus())
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
