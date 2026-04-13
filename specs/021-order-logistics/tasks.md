# 任務清單：訂單物流基礎

**輸入**：設計文件來自 `/specs/021-order-logistics/`
**分支**：`021-order-logistics` | **建立日期**：2026-04-13

---

## 第一階段：DDL + MBG

- [ ] T001 執行 DDL：建立 `shipping_method` 表
- [ ] T002 插入初始資料：宅配、7-11、全家
- [ ] T003 執行 DDL：Order 表新增 `shipping_method_id`, `payment_method`, `shipping_fee`
- [ ] T004 更新 generatorConfig.xml，執行 `mvn mybatis-generator:generate`（生成 ShippingMethod 相關）

**檢查點**：MBG 生成完畢

---

## 第二階段：ShippingMethod CRUD

- [ ] T005 [P] 建立 `req/shippingmethod/ShippingMethodCreateReq.java`
- [ ] T006 [P] 建立 `req/shippingmethod/ShippingMethodUpdateReq.java`
- [ ] T007 [P] 建立 `res/shippingmethod/ShippingMethodRes.java`
- [ ] T008 建立 `service/ShippingMethodService.java`（介面）
- [ ] T009 建立 `service/impl/ShippingMethodServiceImpl.java`（CRUD + 查詢 ACTIVE 列表）
- [ ] T010 建立 `controller/admin/AdminShippingMethodController.java`：
  - `GET /admin/shipping-methods` — 查詢全部（含 INACTIVE）
  - `POST /admin/shipping-methods` — 新增
  - `PUT /admin/shipping-methods/{id}` — 修改
  - `PUT /admin/shipping-methods/{id}/status` — 啟用/停用
  - `@PreAuthorize("hasRole('ADMIN')")`
- [ ] T011 建立 `controller/api/ShippingMethodController.java`：
  - `GET /api/shipping-methods` — 查詢 ACTIVE 列表（前台用）

**檢查點**：運送方式 CRUD 正常

---

## 第三階段：Stub 服務

- [ ] T012 [P] 建立 `service/payment/PaymentGatewayService.java`（介面）
- [ ] T013 [P] 建立 `service/payment/StubPaymentServiceImpl.java`（@Service，log 記錄但不真正扣款，回傳成功）
- [ ] T014 [P] 建立 `service/logistics/LogisticsService.java`（介面）
- [ ] T015 [P] 建立 `service/logistics/StubLogisticsServiceImpl.java`（@Service，log 記錄但不真正出貨）

**檢查點**：Stub 服務可注入使用

---

## 第四階段：訂單建立邏輯調整

- [ ] T016 修改 `OrderServiceImpl.createOrder()`：
  - 新增同店驗證：所有 prizeBox.storeId 必須相同
  - 新增 shippingMethodId 參數，查詢 ShippingMethod 取得運費
  - 設定 order.shippingFee
  - 設定 order.paymentMethod（預設 STUB）
  - 呼叫 `paymentGatewayService.processPayment()` 處理運費支付
- [ ] T017 修改 `OrderCreateReq`（或對應 DTO）：新增 shippingMethodId、recipientName、recipientPhone、超商欄位
- [ ] T018 更新 `OrderRes`：新增 shippingFee、paymentMethod、shippingMethod 資訊

**檢查點**：訂單建立含運費計算、同店驗證、stub 支付

---

## 第五階段：移除 ShippingMethodEnum 引用

- [ ] T019 在 Spec 017 已標記刪除 ShippingMethodEnum 後，更新所有引用改為讀 DB shipping_method 表
- [ ] T020 `mvn clean package -DskipTests` 確認編譯通過

---

## 依賴關係

```
第一階段（DDL + MBG）         — 無依賴
第二階段（CRUD）              — 依賴第一階段
第三階段（Stub）              — 無依賴，可與第二階段平行
第四階段（訂單邏輯）           — 依賴第二階段 + 第三階段
第五階段（移除 Enum）          — 依賴 Spec 017 + 第二階段
```
