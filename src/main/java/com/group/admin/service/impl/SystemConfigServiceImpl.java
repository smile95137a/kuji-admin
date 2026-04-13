package com.group.admin.service.impl;

import com.group.admin.entity.SystemConfig;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.SystemConfigMapper;
import com.group.admin.req.systemconfig.SystemConfigCreateReq;
import com.group.admin.req.systemconfig.SystemConfigUpdateReq;
import com.group.admin.res.systemconfig.SystemConfigRes;
import com.group.admin.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    @Override
    @Transactional
    public SystemConfigRes create(SystemConfigCreateReq req) {
        if (systemConfigMapper.countByConfigKey(req.getConfigKey()) > 0) {
            throw new BusinessException("CONFIG_KEY_EXISTS", "configKey 已存在: " + req.getConfigKey());
        }

        validateTypeAndValue(req.getConfigType(), req.getConfigValue());

        LocalDateTime now = LocalDateTime.now();
        SystemConfig entity = new SystemConfig();
        entity.setId(UUID.randomUUID().toString());
        entity.setConfigKey(req.getConfigKey());
        entity.setConfigValue(req.getConfigValue());
        entity.setConfigType(req.getConfigType().toUpperCase(Locale.ROOT));
        entity.setConfigGroup(req.getConfigGroup());
        entity.setDescription(req.getDescription());
        entity.setVersion(0);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        systemConfigMapper.insert(entity);
        return toRes(entity);
    }

    @Override
    @Transactional
    public SystemConfigRes update(String id, SystemConfigUpdateReq req) {
        SystemConfig existing = systemConfigMapper.selectByPrimaryKey(id);
        if (existing == null) {
            throw new BusinessException("CONFIG_NOT_FOUND", "找不到系統參數: " + id);
        }

        validateTypeAndValue(existing.getConfigType(), req.getConfigValue());

        SystemConfig toUpdate = new SystemConfig();
        toUpdate.setId(id);
        toUpdate.setConfigValue(req.getConfigValue());
        toUpdate.setDescription(req.getDescription());
        toUpdate.setVersion(req.getVersion() + 1);
        toUpdate.setUpdatedAt(LocalDateTime.now());

        int updated = systemConfigMapper.updateByPrimaryKeyAndVersion(toUpdate, req.getVersion());
        if (updated == 0) {
            throw new BusinessException("CONFIG_VERSION_CONFLICT", "資料已被其他人更新，請重新整理後再試");
        }

        SystemConfig latest = systemConfigMapper.selectByPrimaryKey(id);
        return toRes(latest);
    }

    @Override
    @Transactional
    public void delete(String id) {
        int deleted = systemConfigMapper.deleteByPrimaryKey(id);
        if (deleted == 0) {
            throw new BusinessException("CONFIG_NOT_FOUND", "找不到系統參數: " + id);
        }
    }

    @Override
    public List<SystemConfigRes> listAll() {
        return systemConfigMapper.selectAll().stream().map(this::toRes).collect(Collectors.toList());
    }

    @Override
    public List<SystemConfigRes> listByGroup(String group) {
        return systemConfigMapper.selectByConfigGroup(group).stream().map(this::toRes).collect(Collectors.toList());
    }

    @Override
    public int getInt(String key, int defaultValue) {
        SystemConfig config = systemConfigMapper.selectByConfigKey(key);
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(config.getConfigValue().trim());
        } catch (NumberFormatException e) {
            log.warn("⚠️ 系統參數 {} 不是有效整數，使用預設值 {}", key, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public String getString(String key, String defaultValue) {
        SystemConfig config = systemConfigMapper.selectByConfigKey(key);
        if (config == null || config.getConfigValue() == null) {
            return defaultValue;
        }
        return config.getConfigValue();
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        SystemConfig config = systemConfigMapper.selectByConfigKey(key);
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return defaultValue;
        }

        String value = config.getConfigValue().trim().toLowerCase(Locale.ROOT);
        if ("true".equals(value) || "1".equals(value) || "yes".equals(value)) {
            return true;
        }
        if ("false".equals(value) || "0".equals(value) || "no".equals(value)) {
            return false;
        }
        log.warn("⚠️ 系統參數 {} 不是有效布林值，使用預設值 {}", key, defaultValue);
        return defaultValue;
    }

    private void validateTypeAndValue(String type, String value) {
        String normalizedType = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);

        switch (normalizedType) {
            case "INTEGER" -> {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new BusinessException("CONFIG_VALUE_INVALID", "configValue 必須是整數");
                }
            }
            case "BOOLEAN" -> {
                String normalizedValue = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
                if (!("true".equals(normalizedValue)
                        || "false".equals(normalizedValue)
                        || "1".equals(normalizedValue)
                        || "0".equals(normalizedValue)
                        || "yes".equals(normalizedValue)
                        || "no".equals(normalizedValue))) {
                    throw new BusinessException("CONFIG_VALUE_INVALID", "configValue 必須是布林值（true/false/1/0）");
                }
            }
            case "STRING" -> {
                // no-op
            }
            default -> throw new BusinessException("CONFIG_TYPE_INVALID", "configType 只能是 INTEGER / STRING / BOOLEAN");
        }
    }

    private SystemConfigRes toRes(SystemConfig entity) {
        return SystemConfigRes.builder()
                .id(entity.getId())
                .configKey(entity.getConfigKey())
                .configValue(entity.getConfigValue())
                .configType(entity.getConfigType())
                .configGroup(entity.getConfigGroup())
                .description(entity.getDescription())
                .version(entity.getVersion())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
