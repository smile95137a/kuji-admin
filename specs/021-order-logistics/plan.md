# 實作計畫：訂單物流基礎

**Branch**: `021-order-logistics` | **日期**: 2026-04-13 | **規格**: [spec.md](./spec.md)

## 摘要

建立運送方式 DB 管理（取代 ShippingMethodEnum），新增金流/物流 stub 服務介面，調整訂單建立邏輯加入同店驗證與運費計算，預留萬事達金流和綠界物流的串接介面。

## 技術背景

**語言／版本**：Java 21
**新增資料表**：1（shipping_method）
**新增 Service**：4（ShippingMethodService + Stub Payment + Stub Logistics）

## 架構規範檢查

| 關卡 | 狀態 | 備註 |
|------|------|------|
| DDL-first | ✅ | |
| Controller → Service 分層 | ✅ | |
| Stub 可替換（介面+實作分離） | ✅ | 未來注入真實實作即可 |

## 複雜度追蹤

| 面向 | 預估 |
|------|------|
| 新增 Entity | 1（ShippingMethod） |
| 新增 API | 6（後台 4 + 前台 2） |
| 業務邏輯修改 | OrderServiceImpl（同店驗證 + 運費） |
| 預估工時 | 0.5-1 天 |
