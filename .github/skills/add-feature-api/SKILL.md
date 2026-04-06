# 新增功能 API Skill

## 適用情境
- 從零建立一個完整的 CRUD 功能模組
- 不知道應該建立哪些檔案、放在哪裡
- 需要同時支援後台（/admin）和前台（/api）路由
- 需要建立符合專案慣例的 Req / Res DTO

---

## 標準分層架構

```
controller/
  admin/AdminXxxController.java     ← 後台 /admin/xxx
  api/XxxController.java            ← 前台 /api/xxx
service/
  XxxService.java                   ← 介面
  impl/XxxServiceImpl.java          ← 實作
condition/XxxCondition.java         ← 查詢條件
req/xxx/
  CreateXxxReq.java
  UpdateXxxReq.java
res/xxx/
  XxxRes.java
entity/Xxx.java                     ← MBG 生成
mapper/XxxMapper.java               ← MBG 生成
example/XxxExample.java             ← MBG 生成（或手動建立）
resources/mapper/XxxMapper.xml      ← MBG 生成
```

---

## Step 1：DDL → MBG（先執行 mbg-workflow skill）

```sql
CREATE TABLE `contact_inquiry` (
  `id`          VARCHAR(36)  NOT NULL PRIMARY KEY,
  `user_id`     VARCHAR(36),
  `name`        VARCHAR(100) NOT NULL,
  `email`       VARCHAR(100) NOT NULL,
  `message`     TEXT         NOT NULL,
  `status`      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

執行 `mvn mybatis-generator:generate` 後繼續以下步驟。

---

## Step 2：建立查詢條件 Condition

```java
// condition/ContactInquiryCondition.java
@Data
@EqualsAndHashCode(callSuper = true)
public class ContactInquiryCondition extends BaseCondition {
    private String status;   // PENDING / REPLIED / CLOSED
    private String keyword;  // 搜尋 name 或 email
    private String userId;   // 前台查詢時過濾使用者
}
```

### BaseCondition（繼承用）

```java
// condition/BaseCondition.java
@Data
public abstract class BaseCondition {
    private LocalDate createdAtStart;
    private LocalDate createdAtEnd;
}
```

---

## Step 3：建立 Req / Res DTO

```java
// req/ContactInquiryCreateReq.java
@Data
public class ContactInquiryCreateReq {
    @NotBlank private String name;
    @NotBlank @Email private String email;
    @NotBlank private String message;
}

// req/ContactInquiryUpdateReq.java
@Data
public class ContactInquiryUpdateReq {
    private String status;  // PENDING / REPLIED / CLOSED
    private String adminNote;
}

// res/ContactInquiryRes.java
@Data
public class ContactInquiryRes {
    private String id;
    private String name;
    private String email;
    private String message;
    private String status;
    private LocalDateTime createdAt;
}
```

---

## Step 4：建立 Service 介面與實作

```java
// service/ContactInquiryService.java
public interface ContactInquiryService {
    List<ContactInquiryRes> queryList(QueryReq<ContactInquiryCondition> req);
    ContactInquiryRes getById(String id);
    String create(ContactInquiryCreateReq req, String userId);
    void update(String id, ContactInquiryUpdateReq req);
    void delete(String id);
}
```

```java
// service/impl/ContactInquiryServiceImpl.java
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactInquiryServiceImpl implements ContactInquiryService {

    private final ContactInquiryMapper contactInquiryMapper;

    @Override
    public List<ContactInquiryRes> queryList(QueryReq<ContactInquiryCondition> req) {
        ContactInquiryCondition condition = req != null ? req.getCondition() : null;

        ContactInquiryExample example = new ContactInquiryExample();
        ContactInquiryExample.Criteria criteria = example.createCriteria();

        // ✅ 所有條件都是可選的（null check）
        if (condition != null) {
            if (condition.getStatus() != null) {
                criteria.andStatusEqualTo(condition.getStatus());
            }
            if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
                criteria.andNameLike("%" + condition.getKeyword() + "%");
            }
            if (condition.getCreatedAtStart() != null) {
                criteria.andCreatedAtGreaterThanOrEqualTo(condition.getCreatedAtStart().atStartOfDay());
            }
            if (condition.getCreatedAtEnd() != null) {
                criteria.andCreatedAtLessThanOrEqualTo(condition.getCreatedAtEnd().plusDays(1).atStartOfDay());
            }
        }

        // 排序
        if (req != null && req.getSortBy() != null) {
            String order = req.getSortOrder() != null ? req.getSortOrder() : "DESC";
            example.setOrderByClause(req.getSortBy() + " " + order);
        } else {
            example.setOrderByClause("created_at DESC");
        }

        return contactInquiryMapper.selectByExample(example)
                .stream().map(this::toRes).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String create(ContactInquiryCreateReq req, String userId) {
        ContactInquiry entity = new ContactInquiry();
        entity.setId(UUID.randomUUID().toString());
        entity.setUserId(userId);
        entity.setName(req.getName());
        entity.setEmail(req.getEmail());
        entity.setMessage(req.getMessage());
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        contactInquiryMapper.insert(entity);
        log.info("✅ 新增詢問：id={}", entity.getId());
        return entity.getId();
    }

    private ContactInquiryRes toRes(ContactInquiry entity) {
        ContactInquiryRes res = new ContactInquiryRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }
}
```

---

## Step 5：建立 Controller

### 後台 Controller（/admin/**）

```java
// controller/admin/AdminContactInquiryController.java
@Slf4j
@RestController
@RequestMapping("/admin/contact-inquiries")
@RequiredArgsConstructor
@Tag(name = "後台-詢問管理")
public class AdminContactInquiryController {

    private final ContactInquiryService service;

    @PostMapping("/list")
    public ResponseEntity<List<ContactInquiryRes>> queryList(
            @RequestBody(required = false) QueryReq<ContactInquiryCondition> req) {
        return ResponseEntity.ok(service.queryList(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactInquiryRes> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable String id,
                                       @RequestBody @Valid ContactInquiryUpdateReq req) {
        service.update(id, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
```

### 前台 Controller（/api/**）

```java
// controller/api/ContactInquiryController.java
@Slf4j
@RestController
@RequestMapping("/api/contact-inquiries")
@RequiredArgsConstructor
@Tag(name = "前台-聯絡我們")
public class ContactInquiryController {

    private final ContactInquiryService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody @Valid ContactInquiryCreateReq req) {
        String userId = SecurityUtils.getCurrentApiUserId();
        return ResponseEntity.ok(service.create(req, userId));
    }
}
```

---

## 通用規則

### storeId 自動帶入（StoreOwner/Editor 用）

```java
// Controller 中自動帶入 storeId
String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
if (req.getCondition() == null) req.setCondition(new XxxCondition());
req.getCondition().setStoreId(storeId);
```

### 前端分頁（後端回傳全筆）

```java
// ✅ 後端直接返回 List，前端自己切
return ResponseEntity.ok(service.queryList(req));  // List<XxxRes>

// ❌ 不使用 PageHelper 或 Page 物件
```

### AOP 自動包裝回應

Controller 直接返回 `ResponseEntity<T>` 或物件，AOP 自動包成：

```json
{
  "success": true,
  "data": {...},
  "error": null,
  "meta": { "timestamp": "...", "requestId": "..." }
}
```

---

## ⚠️ 禁止操作

- ❌ 不要在 Controller 寫業務邏輯（放 Service）
- ❌ 不要在 Service 直接引用 HttpServletRequest
- ❌ 不要手動建立 `ApiResponse`（AOP 自動處理）
- ❌ 不要讓查詢 API 要求全部條件必填（所有 Condition 欄位可選）
- ❌ 不要讓前端傳 storeId（後端自動帶入）
- ❌ 不要用 PageHelper / Page 物件返回分頁（返回 List）
