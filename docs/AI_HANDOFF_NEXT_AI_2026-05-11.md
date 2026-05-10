# AI 交接收尾（2026-05-11）

最後更新：2026-05-11

## 一句話總結

本輪已完成「訂單生命週期收斂」與「系統參數管理契約修正」，三個 repo 目前工作樹皆乾淨，變更已 commit 並 push，可直接銜接下一個管理模組巡檢。

## Repo 現況

1. 後端 repo：C:/Users/KD/jimmy/kuji-admin
- branch：feat/smtp-account-hardening
- 最新提交：
  - 2ea40b5 fix(system-config): support configGroup query alias and sync docs
  - 644f5fb feat(report,store): align platform reports and store referral tracking
  - df4de1d feat(order): align lifecycle with payment-failed and repay flow
- 工作樹：乾淨

2. 後台前端 repo：C:/Users/KD/jimmy/kuji-admin-web
- branch：main
- 最新提交：
  - 7241e92 fix(system-config): align configGroup and optimistic lock version
  - b0a530c feat(report,store): sync platform report and store referral ui contracts
- 工作樹：乾淨

3. 前台前端 repo：C:/Users/KD/jimmy/kuji-client
- branch：main
- 最新提交：
  - e2144b1 feat(client): align lottery status display
- 工作樹：乾淨（本輪無新改動）

## 本輪已完成重點

1. 訂單生命週期（後端 + 文件）
- 新增 PAYMENT_FAILED 為正式訂單狀態。
- 付款失敗可重付款，並保留 PrizeBox 綁定直到重付或取消。
- 玩家取消範圍：PAYMENT_PENDING / PAYMENT_FAILED / PENDING。
- 後台取消範圍：PAYMENT_PENDING / PAYMENT_FAILED / PENDING / PREPARING。
- STORE_EDITOR 移除「取消」與「完成」權限。
- 規格、契約、前後台文件已同步更新。

2. 報表與店家招商（既有變更整理上線）
- 推薦碼報表、會員成長、儲值、紅利、抽獎結果的契約/欄位調整已提交並推送。
- 店家招商追蹤欄位（referrer/referral/activatedAt）與 migration 已提交並推送。

3. 系統參數管理（新修正）
- 後端 Controller 查詢支援 group 與 configGroup 參數相容。
- 後台前端 system-config 契約改為對齊後端：
  - group -> configGroup
  - NUMBER -> INTEGER
  - update req 加入 version（樂觀鎖）
  - isEditable 缺省視為可編輯，避免全部唯讀
- 系統參數文件已同步更新。

## 重要檔案（本輪 system-config 修正）

1. 後端
- src/main/java/com/group/admin/controller/admin/AdminSystemConfigController.java
- frontend/admin/10-system-config.md

2. 後台前端
- src/services/adminSystemConfigService.ts
- src/composables/useSystemConfig.ts
- src/components/systemConfig/SystemConfigEditor.vue
- src/components/systemConfig/SystemConfigTable.vue

## 環境限制

1. 目前終端缺少 mvn 指令，無法本機跑後端 compile/test。
2. 目前終端缺少 npm 指令，無法本機跑 admin-web build/type-check。
3. 因此本輪以靜態檢查、程式契約對齊、git 版本封存為主。

## 下一個 AI 建議起手（可直接做）

1. 下一個管理模組建議：會員管理（Member Management）。
2. 先做三方一致性稽核：
- kuji-admin：controller/service/req/res/mapper
- kuji-admin-web：router/view/service
- kuji-client：會員相關顯示與流程影響
3. 先修「契約不一致」與「權限/狀態漏洞」，每一包修完即 commit + push（不開 PR）。

## 建議操作命令

1. 查看後端近期提交
- git -C C:/Users/KD/jimmy/kuji-admin log --oneline -10

2. 查看後台前端近期提交
- git -C C:/Users/KD/jimmy/kuji-admin-web log --oneline -10

3. 交接後第一步稽核（範例）
- 先檢索 member/user/admin-user 相關 controller 與前端路由，再對照文件。
