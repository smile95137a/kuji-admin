# Data Model: 系統參數管理

**Feature**: `016-system-config`
**Date**: 2026-04-13

---

## Entity: SystemConfig (系統參數)

**Table**: `system_config`
**Package**: `com.group.admin.entity.SystemConfig`

### DDL

```sql
CREATE TABLE `system_config` (
  `id`            VARCHAR(36)   NOT NULL PRIMARY KEY,
  `config_key`    VARCHAR(100)  NOT NULL UNIQUE,
  `config_value`  VARCHAR(500)  NOT NULL,
  `config_type`   VARCHAR(20)   NOT NULL DEFAULT 'STRING' COMMENT 'INTEGER / STRING / BOOLEAN',
  `config_group`  VARCHAR(50)   NOT NULL DEFAULT 'GENERAL' COMMENT '參數分組',
  `description`   VARCHAR(500)  NULL COMMENT '參數說明',
  `version`       INT           NOT NULL DEFAULT 0 COMMENT '樂觀鎖版本號',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系統參數設定表';
```

### 初始資料

```sql
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_type`, `config_group`, `description`) VALUES
(UUID(), 'protection_initial_minutes', '5', 'INTEGER', 'DRAW', '保護初始時間（分鐘）'),
(UUID(), 'protection_extension_minutes', '2', 'INTEGER', 'DRAW', '每次操作延長時間（分鐘）'),
(UUID(), 'protection_max_minutes', '10', 'INTEGER', 'DRAW', '保護最大時間（分鐘）'),
(UUID(), 'max_draws_per_request', '10', 'INTEGER', 'DRAW', '單次 API 最大抽獎數');
```

### Fields

| Field | Type | DB Column | Nullable | Description |
|-------|------|-----------|----------|-------------|
| `id` | `String` | `id` VARCHAR(36) PK | No | UUID |
| `configKey` | `String` | `config_key` VARCHAR(100) UNIQUE | No | 參數唯一鍵 |
| `configValue` | `String` | `config_value` VARCHAR(500) | No | 參數值（統一字串存儲，讀取時型別轉換） |
| `configType` | `String` | `config_type` VARCHAR(20) | No | 型別：INTEGER / STRING / BOOLEAN |
| `configGroup` | `String` | `config_group` VARCHAR(50) | No | 分組，用於前端分類顯示 |
| `description` | `String` | `description` VARCHAR(500) | Yes | 人類可讀的參數說明 |
| `version` | `Integer` | `version` INT | No | 樂觀鎖版本號，UPDATE 時 +1 |
| `createdAt` | `LocalDateTime` | `created_at` | No | 建立時間 |
| `updatedAt` | `LocalDateTime` | `updated_at` | No | 更新時間 |

### Validation Rules

- `configKey` 唯一，不可重複
- `configValue` 必須符合 `configType` 宣告的型別（如 INTEGER 則必須可轉為整數）
- `version` 用於樂觀鎖：UPDATE 時 WHERE version = #{oldVersion}，更新失敗則拋出並發衝突異常

---

## DTO 設計

### SystemConfigCreateReq

| Field | Type | Validation | Required |
|-------|------|------------|----------|
| `configKey` | String | `@NotBlank @Size(max=100)` | Yes |
| `configValue` | String | `@NotBlank @Size(max=500)` | Yes |
| `configType` | String | `@NotBlank`，限定 INTEGER/STRING/BOOLEAN | Yes |
| `configGroup` | String | `@NotBlank @Size(max=50)` | Yes |
| `description` | String | `@Size(max=500)` | No |

### SystemConfigUpdateReq

| Field | Type | Validation | Required |
|-------|------|------------|----------|
| `configValue` | String | `@NotBlank @Size(max=500)` | Yes |
| `description` | String | `@Size(max=500)` | No |
| `version` | Integer | `@NotNull` | Yes（樂觀鎖） |

### SystemConfigRes

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | UUID |
| `configKey` | String | 參數鍵 |
| `configValue` | String | 參數值 |
| `configType` | String | 型別 |
| `configGroup` | String | 分組 |
| `description` | String | 說明 |
| `version` | Integer | 版本號 |
| `updatedAt` | String | 最後更新時間 |

---

## Service 對外介面

### SystemConfigService

```java
public interface SystemConfigService {
    // CRUD
    SystemConfigRes create(SystemConfigCreateReq req);
    SystemConfigRes update(String id, SystemConfigUpdateReq req);
    void delete(String id);
    List<SystemConfigRes> listAll();
    List<SystemConfigRes> listByGroup(String group);
    
    // 業務讀取（帶預設值 fallback）
    int getInt(String key, int defaultValue);
    String getString(String key, String defaultValue);
    boolean getBoolean(String key, boolean defaultValue);
}
```

### 便捷常數（建議定義在 SystemConfigService 或獨立常數類別）

```java
String KEY_PROTECTION_INITIAL_MINUTES = "protection_initial_minutes";
String KEY_PROTECTION_EXTENSION_MINUTES = "protection_extension_minutes";
String KEY_PROTECTION_MAX_MINUTES = "protection_max_minutes";
String KEY_MAX_DRAWS_PER_REQUEST = "max_draws_per_request";
```

---

## ER 關聯

```
system_config (獨立表，無 FK)
```

此表不與其他業務表建立外鍵關係，僅供 Service 層讀取使用。
