# 前後端 / DB 狀態字串一致性盤點（2026-05-16）

本文件整理目前 `kuji-admin`、`kuji-admin-web`、`kuji-client` 三端狀態字串與 enum 使用情況，避免前後端與 DB 各自維護不同語意。

## 本次已修正

### 1. Banner 狀態主字串統一為 `PUBLISHED / UNPUBLISHED`

- 後端 `BannerServiceImpl`、`BannerMapper.xml`、前後台 Banner 頁面本來就以 `PUBLISHED / UNPUBLISHED` 為主。
- 已修正 `AdminBannerController` 的狀態切換入口，正式接受：
  - `PUBLISHED`
  - `UNPUBLISHED`
- 暫時相容舊值：
  - `ACTIVE` -> `PUBLISHED`
  - `INACTIVE` -> `UNPUBLISHED`

### 2. 停用店家時 Banner 連動下架修正

原本 `StoreServiceImpl` 在停用店家時，錯把 Banner 當成 `ACTIVE / INACTIVE`：

- 查詢條件：`status = ACTIVE`
- 更新結果：`status = INACTIVE`

這與 Banner 真實狀態字串不一致，會導致停用店家時 Banner 不會被正確下架。

已修正為：

- 查詢條件：`status = PUBLISHED`
- 更新結果：`status = UNPUBLISHED`

### 3. 後台店家列表篩選值修正

`kuji-admin-web` 的店家列表篩選原本送出：

- `ENABLED`
- `DISABLED`

但後端 / DB 實際使用：

- `ACTIVE`
- `INACTIVE`

已修正前端篩選值為 `ACTIVE / INACTIVE`。

## 已確認一致的區塊

### Banner

- DB / Mapper：`PUBLISHED / UNPUBLISHED`
- 後端 Service：`PUBLISHED / UNPUBLISHED`
- 後台前端：`PUBLISHED / UNPUBLISHED`
- 前台公開 API：以 `PUBLISHED` 為實際資料來源

### News

- 後端 / 前端主要使用：
  - `DRAFT`
  - `PUBLISHED`
  - `UNPUBLISHED`

### Lottery

- 主要狀態：
  - `DRAFT`
  - `WAITING_ON_SHELF`
  - `ON_SHELF`
  - `OFF_SHELF`
  - `FORCED_OFF`
  - `GRAND_PRIZE_DRAWN`
  - `ALL_DRAWN`
  - `DELETED`
- 前後台主要邏輯一致。

### Emergency Announcement

- 主要狀態：
  - `DRAFT`
  - `ACTIVE`
  - `INACTIVE`
- 前後端主要邏輯一致。

### AdminUser / FrontendUser / Store

- 主要狀態：
  - `ACTIVE`
  - `INACTIVE`
- 其中店家後台頁面仍保留 `ENABLED / DISABLED` 相容顯示邏輯，但送值已修正回 `ACTIVE / INACTIVE`。

## 仍需追蹤的中風險項目

### 1. 註解 / Swagger 文件仍有舊描述

目前仍有少數後端註解或說明文字未同步，例如：

- `BannerController` 舊註解曾寫成 `ACTIVE banners`
- `AdminBannerController` 舊註解曾寫成 `ACTIVE|INACTIVE|DRAFT`
- Lottery 某些 request / response 說明仍保留舊規則描述

這類問題不一定造成 runtime 錯誤，但很容易誤導前端或後續開發者。

### 2. 前端顯示層保留舊字串 fallback

例如：

- `ENABLED / DISABLED`
- `INACTIVE` 被當成某些商品卡的「已下架」相容值

這類 fallback 目前偏向相容保護，不是立即錯誤，但若未來完全不再需要舊資料，可再考慮收斂。

## 建議下一步

1. 建立一份正式的「狀態字典」文件，列出每個模組唯一允許的狀態值。
2. 後端將可枚舉狀態盡量集中在 enum / EnumController。
3. 前端的 `statusOptions` 優先改成吃後端 enum endpoint，避免手寫散落。
4. 為 Banner / Lottery / Store / News 各補一組狀態合約測試。

## 建議作法

優先順序建議如下：

1. `高風險`：會寫錯 DB 或查不到資料的狀態值
2. `中風險`：前端顯示與送值不一致
3. `低風險`：註解、Swagger、文件未同步
