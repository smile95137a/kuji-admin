# 實作計畫：程式碼品質修復

**Branch**: `022-code-quality` | **日期**: 2026-04-13 | **規格**: [spec.md](./spec.md)

## 摘要

修復專案中的程式碼品質問題：7 個 Controller 的分層違規、重複 ApiResponse 類別、廢棄 Filter 檔案、OAuth2Controller 反模式、SystemLogServiceImpl 的 Servlet 依賴、LotteryServiceImpl 的方法去重。純重構性質，不新增功能。

## 技術背景

**語言／版本**：Java 21
**影響範圍**：Controller 層 + Service 層
**風險等級**：中（重構不改變行為，但需仔細驗證）

## 架構規範檢查

| 關卡 | 狀態 | 備註 |
|------|------|------|
| Controller 不直接用 Mapper | ⚠️ 修復中 | 這是本 Spec 的主要目標 |
| Service 層不引用 Servlet API | ⚠️ 修復中 | SystemLogServiceImpl |
| AOP 自動包裝 ApiResponse | ⚠️ 修復中 | OAuth2Controller |

## 複雜度追蹤

| 面向 | 預估 |
|------|------|
| 刪除廢棄檔案 | 3 個 |
| 修復 Controller | 7 個 |
| 修復 Service | 2 個（OAuth2、SystemLog） |
| LotteryServiceImpl 去重 | ~10 個方法移除 |
| 預估工時 | 1 天 |
