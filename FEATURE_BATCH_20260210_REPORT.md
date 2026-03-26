# 📋 功能實作完成報告 (2026-02-10)

## ✅ 編譯狀態：BUILD SUCCESS

---

## 一、刮刮樂 & 抽卡（已存在）

**結論**：系統已完整實作，無需新增程式碼。

- 一番賞/抽卡：`LOTTERY_MODE` - 隨機抽取
- 刮刮樂：`SCRATCH_MODE` - 固定籤位，含「謝謝惠顧」

📖 使用指南請參照：`SCRATCH_AND_DRAW_GUIDE.md`

---

## 二、最新消息分類 + 重要提醒

### 異動檔案
| 檔案 | 變更 |
|------|------|
| `entity/News.java` | 新增 `category`、`important` 欄位 |
| `req/news/NewsCondition.java` | 新增 `category`、`important` 查詢條件 |
| `req/news/NewsCreateReq.java` | 新增 `category`、`important` 欄位 |
| `req/news/NewsUpdateReq.java` | 新增 `category`、`important` 欄位 |
| `res/news/NewsRes.java` | 新增 `category`、`categoryName`、`important` 欄位 |
| `service/impl/NewsServiceImpl.java` | 新增分類/重要性的建立、更新、查詢邏輯 |

### 分類對照
| category 值 | 中文 |
|------------|------|
| `ANNOUNCEMENT` | 公告 |
| `EVENT` | 活動 |
| `SYSTEM` | 系統 |
| 不傳/不篩選 | 全部 |

### 重要提醒
- `important: true` → 設為重要提醒
- 查詢時 `condition.important = true` → 只查重要的

---

## 三、合作諮詢 API

### 新建檔案
| 檔案 | 說明 |
|------|------|
| `entity/ContactInquiry.java` | 合作諮詢實體 |
| `mapper/ContactInquiryMapper.java` | Mapper（annotation-based） |
| `req/contact/ContactInquiryCreateReq.java` | 前台提交 DTO |
| `req/contact/ContactInquiryCondition.java` | 查詢條件（extends BaseCondition） |
| `res/contact/ContactInquiryRes.java` | 回應 DTO |
| `service/ContactInquiryService.java` | Service 介面 |
| `service/impl/ContactInquiryServiceImpl.java` | Service 實作 |
| `controller/api/ContactInquiryController.java` | 前台 Controller |
| `controller/admin/AdminContactInquiryController.java` | 後台 Controller |

### API 端點

#### 前台（無需登入）
| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/contact-inquiry` | 提交合作諮詢 |

#### 後台（需 Admin）
| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/admin/contact-inquiries/list` | 查詢列表 |
| GET | `/api/admin/contact-inquiries/{id}` | 查詢詳情 |
| PUT | `/api/admin/contact-inquiries/{id}/status` | 更新狀態 |
| DELETE | `/api/admin/contact-inquiries/{id}` | 刪除 |

### 合作類型
| cooperationType | 說明 |
|-----------------|------|
| `IP_LICENSE` | IP 授權合作 |
| `DISTRIBUTION` | 經銷通路合作 |
| `CUSTOM_PRIZE` | 客製賞品合作 |
| `MARKETING` | 行銷推廣合作 |
| `OTHER` | 其他 |

### 狀態流程
`PENDING` → `PROCESSING` → `COMPLETED` / `REJECTED`

---

## 四、賞品 Res 新增 content 欄位

### 異動檔案
| 檔案 | 變更 |
|------|------|
| `entity/LotteryPrize.java` | 新增 `content` 欄位 + getter/setter |
| `res/lottery/LotteryPrizeRes.java` | 新增 `content` 欄位 |
| `req/lottery/LotteryPrizeCreateReq.java` | 新增 `content` 欄位 |
| `req/lottery/LotteryPrizeUpdateReq.java` | 新增 `content` 欄位 |
| `mapper/LotteryPrizeMapper.xml` | ResultMap、Blob_Column_List、所有 insert/update 語句 |
| `service/impl/LotteryServiceImpl.java` | 3 處 convertToRes + create + update 新增 content |
| `service/impl/LotteryPrizeServiceImpl.java` | convertToRes + create + update 新增 content |

---

## 五、消費紀錄（重新定義）

### 新建檔案
| 檔案 | 說明 |
|------|------|
| `entity/ConsumptionRecord.java` | 消費紀錄實體 |
| `mapper/ConsumptionRecordMapper.java` | Mapper（annotation-based） |
| `req/consumption/ConsumptionRecordCondition.java` | 查詢條件 |
| `res/consumption/ConsumptionRecordRes.java` | 回應 DTO |
| `service/ConsumptionRecordService.java` | Service 介面 |
| `service/impl/ConsumptionRecordServiceImpl.java` | Service 實作 |
| `controller/api/ConsumptionRecordController.java` | 前台 Controller |
| `controller/admin/AdminConsumptionRecordController.java` | 後台 Controller |

### 消費紀錄 ≠ 儲值紀錄
- ✅ 金幣抽獎消費（DRAW_GOLD）
- ✅ 紅利抽獎消費（DRAW_BONUS）
- ✅ 運費支付（SHIPPING_FEE）
- ❌ 儲值 → 已有 recharge_record 表

### API 端點

#### 前台（需登入）
| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/consumption-records/list` | 查詢我的消費紀錄 |

#### 後台（需 Admin）
| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/admin/consumption-records/list` | 查詢所有消費紀錄 |

### 內部記錄方式
消費紀錄由系統內部自動記錄（在抽獎/下單時呼叫 `ConsumptionRecordService.recordConsumption()`），前台不需要手動建立。

---

## 六、查詢統一使用 Condition

所有新 API 的查詢端點均使用 `QueryReq<XxxCondition>` 模式：

```java
@PostMapping("/list")
public ResponseEntity<List<XxxRes>> query(
    @RequestBody(required = false) QueryReq<XxxCondition> req) { ... }
```

- 不帶參數 → 返回全部
- 帶 `condition` → 動態篩選
- 帶 `page` / `size` → 分頁（由前端控制）

---

## 📦 SQL 異動（部署前必須執行）

檔案：`src/main/resources/db/migration/V20260210__add_news_category_and_contact_inquiry.sql`

內容：
1. `ALTER TABLE news` - 新增 `category`、`important` 欄位
2. `CREATE TABLE contact_inquiry` - 合作諮詢表
3. `CREATE TABLE consumption_record` - 消費紀錄表
4. `ALTER TABLE lottery_prize` - 新增 `content` 欄位

---

## ⚡ 部署步驟

1. 在 RDS 上執行 SQL migration
2. `mvn clean package -DskipTests`
3. 部署至 EC2
