# MBG 工作流程 Skill

## 適用情境
- 新增資料表後需要產生 Entity / Mapper / Example
- 修改資料表欄位後需要重新生成
- 需要了解如何正確設定 `generatorConfig.xml`
- 需要了解 MBG 生成後的標準使用方式

---

## 核心原則：DDL First

**任何新 Entity 都必須從 DDL 開始，不得手動建立。**

```
DDL → MySQL 執行 → 更新 generatorConfig.xml → 執行 MBG → 使用 Example 查詢
```

---

## Step 1：撰寫 DDL 並在 MySQL 執行

```sql
-- 範例：新增 banner 表
CREATE TABLE `banner` (
  `id`          VARCHAR(36)  NOT NULL PRIMARY KEY,
  `store_id`    VARCHAR(36)  NOT NULL,
  `image_url`   VARCHAR(255) NOT NULL,
  `link_url`    VARCHAR(255),
  `sort_order`  INT          NOT NULL DEFAULT 0,
  `is_active`   TINYINT      NOT NULL DEFAULT 1,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

> ⚠️ UUID 主鍵一律用 `VARCHAR(36)`，不使用 AUTO_INCREMENT BIGINT。

---

## Step 2：更新 generatorConfig.xml

配置檔位置：`src/main/resources/generatorConfig.xml`

在 `</context>` 前新增 `<table>` 元素：

```xml
<!-- 新增的表 -->
<table tableName="banner"
       enableCountByExample="false"
       enableUpdateByExample="false"
       enableDeleteByExample="false"
       enableSelectByExample="false"
       selectByExampleQueryId="false">
    <generatedKey column="id" sqlStatement="JDBC" identity="true"/>
</table>
```

> **注意**：現有專案所有 table 都禁用 ByExample（`false`），保持一致。

完整配置位置說明：
```
javaModelGenerator  → com.group.admin.entity   (Entity POJO)
sqlMapGenerator     → src/main/resources/mapper  (XXXMapper.xml)
javaClientGenerator → com.group.admin.mapper     (XXXMapper.java 介面)
```

---

## Step 3：執行 MBG

```bash
mvn mybatis-generator:generate
```

> ⚠️ MBG 會覆寫已存在的相同檔案，但不會刪除手動新增的方法。
> 若 Mapper.java 已有手動自定義方法，建議先備份。

執行後產生的檔案：
```
entity/Banner.java               ← POJO（對應 banner 表所有欄位）
mapper/BannerMapper.java         ← 介面（基礎 CRUD）
mapper/BannerMapper.xml          ← SQL 映射（位於 resources/mapper/）
```

> ⚠️ 注意：`example/BannerExample.java` 不會自動生成（因為 `enableSelectByExample=false`）。
> 專案使用自訂 Example 類別，位於 `example/` 套件，需要手動建立或從現有的複製。

---

## Step 4：標準 Mapper 方法（MBG 自動生成）

```java
// BannerMapper.java（MBG 生成的基礎 CRUD）
int deleteByPrimaryKey(String id);
int insert(Banner row);
Banner selectByPrimaryKey(String id);
List<Banner> selectAll();
int updateByPrimaryKey(Banner row);
```

---

## Step 5：Example 查詢（推薦方式）

若專案中已有對應的 Example 類別：

```java
// 查詢啟用中的 Banner（按排序）
BannerExample example = new BannerExample();
example.createCriteria()
    .andIsActiveEqualTo(1)
    .andStoreIdEqualTo(storeId);
example.setOrderByClause("sort_order ASC");
List<Banner> banners = bannerMapper.selectByExample(example);

// OR 查詢
BannerExample example = new BannerExample();
example.createCriteria().andStoreIdEqualTo(store1Id);
example.or().andStoreIdEqualTo(store2Id);
```

### 何時手寫 SQL

| 情境 | 是否手寫 |
|------|---------|
| 單表條件查詢 | ❌ 用 Example |
| 單表排序分頁 | ❌ 用 Example + setOrderByClause |
| JOIN 兩張表 | ✅ 手寫 XML |
| 子查詢 | ✅ 手寫 XML |
| 聚合 (COUNT/SUM/AVG) | ✅ 手寫 XML |

手寫 SQL 命名規範（在 Mapper.java 宣告後寫入 XML）：
```java
// 查詢
List<BannerWithStore> selectBannerWithStore(@Param("storeId") String storeId);
// 新增
int insertBannerBatch(@Param("list") List<Banner> list);
// 更新
int updateBannerStatusByStoreId(@Param("storeId") String storeId, @Param("status") int status);
// 刪除
int deleteBannerByStoreId(@Param("storeId") String storeId);
```

---

## ⚠️ 禁止操作

- ❌ 不要手動建立 Entity（必須從 DDL 執行後由 MBG 生成）
- ❌ 不要修改 MBG 生成的基礎方法（`insert`、`selectByPrimaryKey` 等）
- ❌ 不要在 XML 中用 `#{example.xxx}` 引用 Example 物件屬性
- ❌ 不要在沒有資料表的情況下憑空推測欄位結構
- ❌ 不要在同一個 Mapper.xml 同時存在 MBG 維護區和手寫區（分開管理）

---

## 常用命令速查

```bash
# 重新生成所有 Entity/Mapper（以 generatorConfig.xml 為準）
mvn mybatis-generator:generate

# 建構並確認編譯通過
mvn clean package -DskipTests

# 開發模式啟動
mvn spring-boot:run
```
