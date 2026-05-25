package com.group.admin.service.impl;

import com.group.admin.entity.SystemConfig;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.SystemConfigMapper;
import com.group.admin.req.systemconfig.SystemConfigCreateReq;
import com.group.admin.req.systemconfig.SystemConfigUpdateReq;
import com.group.admin.res.systemconfig.SystemConfigRes;
import com.group.admin.service.DrawBonusTierConfigParser;
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
    private final DrawBonusTierConfigParser drawBonusTierConfigParser;

    @Override
    @Transactional
    public SystemConfigRes create(SystemConfigCreateReq req) {
        if (systemConfigMapper.countByConfigKey(req.getConfigKey()) > 0) {
            throw new BusinessException("CONFIG_KEY_EXISTS", "configKey 已存在: " + req.getConfigKey());
        }

        validateTypeAndValue(req.getConfigType(), req.getConfigValue());
        validateBusinessRules(req.getConfigKey(), req.getConfigValue());

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
            throw new BusinessException("CONFIG_NOT_FOUND", "找不到系統設定: " + id);
        }

        validateTypeAndValue(existing.getConfigType(), req.getConfigValue());
        validateBusinessRules(existing.getConfigKey(), req.getConfigValue());

        SystemConfig toUpdate = new SystemConfig();
        toUpdate.setId(id);
        toUpdate.setConfigValue(req.getConfigValue());
        toUpdate.setDescription(req.getDescription());
        toUpdate.setVersion(req.getVersion() + 1);
        toUpdate.setUpdatedAt(LocalDateTime.now());

        int updated = systemConfigMapper.updateByPrimaryKeyAndVersion(toUpdate, req.getVersion());
        if (updated == 0) {
            throw new BusinessException("CONFIG_VERSION_CONFLICT", "系統設定版本衝突，請重新整理後再試");
        }

        SystemConfig latest = systemConfigMapper.selectByPrimaryKey(id);
        return toRes(latest);
    }

    @Override
    @Transactional
    public void delete(String id) {
        int deleted = systemConfigMapper.deleteByPrimaryKey(id);
        if (deleted == 0) {
            throw new BusinessException("CONFIG_NOT_FOUND", "找不到系統設定: " + id);
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
            log.warn("系統設定 {} 不是有效整數，改用預設值 {}", key, defaultValue);
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

        log.warn("系統設定 {} 不是有效布林值，改用預設值 {}", key, defaultValue);
        return defaultValue;
    }

    private void validateTypeAndValue(String type, String value) {
        String normalizedType = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);

        switch (normalizedType) {
            case "INTEGER" -> {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new BusinessException("CONFIG_VALUE_INVALID", "configValue 必須為整數");
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
                    throw new BusinessException("CONFIG_VALUE_INVALID", "configValue 必須為 true/false/1/0/yes/no");
                }
            }
            case "STRING" -> {
                // no-op
            }
            default -> throw new BusinessException("CONFIG_TYPE_INVALID", "configType 只支援 INTEGER / STRING / BOOLEAN");
        }
    }

    private void validateBusinessRules(String configKey, String configValue) {
        if (configKey == null || configValue == null) {
            return;
        }

        if (KEY_PROTECTION_INITIAL_MINUTES.equals(configKey)) {
            int initialMinutes = parseRequiredInt(configKey, configValue);
            if (initialMinutes < 1) {
                throw new BusinessException("CONFIG_VALUE_INVALID", "保護期初始分鐘數至少要 1 分鐘");
            }

            int maxMinutes = resolveIntValue(KEY_PROTECTION_MAX_MINUTES, 10, configKey, initialMinutes);
            if (initialMinutes > maxMinutes) {
                throw new BusinessException("CONFIG_VALUE_INVALID", "保護期初始分鐘數不可大於保護期上限分鐘數");
            }
            return;
        }

        if (KEY_PROTECTION_EXTENSION_MINUTES.equals(configKey)) {
            int extensionMinutes = parseRequiredInt(configKey, configValue);
            if (extensionMinutes < 1) {
                throw new BusinessException("CONFIG_VALUE_INVALID", "保護期每次延長分鐘數至少要 1 分鐘");
            }
            return;
        }

        if (KEY_PROTECTION_MAX_MINUTES.equals(configKey)) {
            int maxMinutes = parseRequiredInt(configKey, configValue);
            if (maxMinutes < 1) {
                throw new BusinessException("CONFIG_VALUE_INVALID", "保護期上限分鐘數至少要 1 分鐘");
            }

            int initialMinutes = resolveIntValue(KEY_PROTECTION_INITIAL_MINUTES, 5, configKey, maxMinutes);
            if (maxMinutes < initialMinutes) {
                throw new BusinessException("CONFIG_VALUE_INVALID", "保護期上限分鐘數不可小於初始分鐘數");
            }
            return;
        }

        if (KEY_DRAW_PROTECTION_BASE_SECONDS.equals(configKey)) {
            int baseSeconds = parseRequiredInt(configKey, configValue);
            if (baseSeconds < 1) {
                throw new BusinessException("CONFIG_VALUE_INVALID", "單抽保護秒數至少要 1 秒");
            }

            int maxSeconds = resolveIntValue(KEY_DRAW_PROTECTION_MAX_SECONDS, 600, configKey, baseSeconds);
            if (baseSeconds > maxSeconds) {
                throw new BusinessException("CONFIG_VALUE_INVALID", "單抽保護秒數不可大於保護期上限秒數");
            }
            return;
        }

        if (KEY_DRAW_PROTECTION_EXTRA_SECONDS_PER_DRAW.equals(configKey)) {
            int extraSeconds = parseRequiredInt(configKey, configValue);
            if (extraSeconds < 0) {
                throw new BusinessException("CONFIG_VALUE_INVALID", "每抽額外保護秒數不可小於 0");
            }
            return;
        }

        if (KEY_DRAW_PROTECTION_MAX_SECONDS.equals(configKey)) {
            int maxSeconds = parseRequiredInt(configKey, configValue);
            if (maxSeconds < 1) {
                throw new BusinessException("CONFIG_VALUE_INVALID", "保護期上限秒數至少要 1 秒");
            }

            int baseSeconds = resolveIntValue(KEY_DRAW_PROTECTION_BASE_SECONDS, 300, configKey, maxSeconds);
            if (maxSeconds < baseSeconds) {
                throw new BusinessException("CONFIG_VALUE_INVALID", "保護期上限秒數不可小於單抽保護秒數");
            }
            return;
        }

        if (KEY_DRAW_BONUS_TIERS_JSON.equals(configKey)) {
            try {
                drawBonusTierConfigParser.parse(configValue);
            } catch (IllegalArgumentException ex) {
                throw new BusinessException("CONFIG_VALUE_INVALID", ex.getMessage());
            }
            return;
        }

        if (KEY_MAX_DRAWS_PER_REQUEST.equals(configKey)) {
            int maxDraws = parseRequiredInt(configKey, configValue);
            if (maxDraws < 1) {
                throw new BusinessException("CONFIG_VALUE_INVALID", "單次抽獎上限至少要 1 抽");
            }
        }
    }

    private int resolveIntValue(String targetKey, int defaultValue, String currentKey, int currentValue) {
        if (targetKey.equals(currentKey)) {
            return currentValue;
        }
        return getInt(targetKey, defaultValue);
    }

    private int parseRequiredInt(String configKey, String configValue) {
        try {
            return Integer.parseInt(configValue.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("CONFIG_VALUE_INVALID", configKey + " 必須為整數");
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
