@echo off
chcp 65001 > nul
echo ============================================
echo KUJI Admin API 測試命令
echo ============================================
echo.

REM 設定 base URL
set BASE_URL=http://localhost:8080/api

echo 📝 測試說明：
echo 1. 先執行「取得 Token」
echo 2. 複製 accessToken 的值
echo 3. 執行「設定 Token」並貼上 Token
echo 4. 開始執行其他測試
echo.
echo ============================================
echo.

:menu
echo.
echo 請選擇測試項目：
echo.
echo [基礎測試]
echo 1. 取得 Admin Token
echo 2. 設定 Token 環境變數
echo.
echo [Enum API - 無需登入]
echo 3. 取得所有 Enum
echo 4. 取得 Banner 狀態選項
echo 5. 取得 News 狀態選項
echo.
echo [店家選項 API - 無需登入]
echo 6. 取得所有店家選項
echo 7. 搜尋店家（關鍵字：玩具）
echo.
echo [圖片上傳 API - 需 Admin 權限]
echo 8. 上傳 News 圖片
echo 9. 上傳 Banner 圖片
echo.
echo [News API]
echo 10. 新增 News（草稿）
echo 11. 查詢 News 列表（後台）
echo 12. 上架 News
echo 13. 查詢 News（前台）
echo 14. 下架 News
echo.
echo [Banner API]
echo 15. 新增 Banner（未上架）
echo 16. 查詢 Banner 列表（後台）
echo 17. 上架 Banner
echo 18. 查詢輪播 Banner（前台）
echo 19. 下架 Banner
echo.
echo 0. 離開
echo.

set /p choice=請輸入選項：

if "%choice%"=="1" goto get_token
if "%choice%"=="2" goto set_token
if "%choice%"=="3" goto enum_all
if "%choice%"=="4" goto banner_status
if "%choice%"=="5" goto news_status
if "%choice%"=="6" goto store_options
if "%choice%"=="7" goto store_search
if "%choice%"=="8" goto upload_news
if "%choice%"=="9" goto upload_banner
if "%choice%"=="10" goto create_news
if "%choice%"=="11" goto list_news
if "%choice%"=="12" goto publish_news
if "%choice%"=="13" goto query_news
if "%choice%"=="14" goto archive_news
if "%choice%"=="15" goto create_banner
if "%choice%"=="16" goto list_banner
if "%choice%"=="17" goto publish_banner
if "%choice%"=="18" goto query_banner
if "%choice%"=="19" goto unpublish_banner
if "%choice%"=="0" exit
goto menu

:get_token
echo.
echo ======================================
echo 取得 Admin Token
echo ======================================
curl -X POST %BASE_URL%/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin@kuji.com\",\"password\":\"admin123\"}"
echo.
echo.
echo 請複製上方的 accessToken 值
pause
goto menu

:set_token
echo.
echo ======================================
echo 設定 Token 環境變數
echo ======================================
set /p TOKEN=請貼上 Token（不含 Bearer）：
set TOKEN=Bearer %TOKEN%
echo.
echo ✅ Token 已設定！
pause
goto menu

:enum_all
echo.
echo ======================================
echo 取得所有 Enum
echo ======================================
curl %BASE_URL%/enums/all
echo.
pause
goto menu

:banner_status
echo.
echo ======================================
echo 取得 Banner 狀態選項
echo ======================================
curl %BASE_URL%/enums/banner-status
echo.
pause
goto menu

:news_status
echo.
echo ======================================
echo 取得 News 狀態選項
echo ======================================
curl %BASE_URL%/enums/news-status
echo.
pause
goto menu

:store_options
echo.
echo ======================================
echo 取得所有店家選項
echo ======================================
curl %BASE_URL%/stores/options
echo.
echo.
echo 請記下店家 ID（value 欄位）供後續測試使用
pause
goto menu

:store_search
echo.
echo ======================================
echo 搜尋店家（關鍵字：玩具）
echo ======================================
curl "%BASE_URL%/stores/search?keyword=玩具"
echo.
pause
goto menu

:upload_news
echo.
echo ======================================
echo 上傳 News 圖片
echo ======================================
set /p FILE_PATH=請輸入圖片路徑（例如：C:\test.jpg）：
curl -X POST %BASE_URL%/admin/upload/news ^
  -H "Authorization: %TOKEN%" ^
  -F "file=@%FILE_PATH%"
echo.
echo.
echo 請記下 imageUrl 供後續測試使用
pause
goto menu

:upload_banner
echo.
echo ======================================
echo 上傳 Banner 圖片
echo ======================================
set /p FILE_PATH=請輸入圖片路徑（例如：C:\banner.jpg）：
curl -X POST %BASE_URL%/admin/upload/banner ^
  -H "Authorization: %TOKEN%" ^
  -F "file=@%FILE_PATH%"
echo.
echo.
echo 請記下 imageUrl 供後續測試使用
pause
goto menu

:create_news
echo.
echo ======================================
echo 新增 News（草稿）
echo ======================================
set /p IMAGE_URL=請輸入 imageUrl（例如：/img/news/xxx.jpg）：
curl -X POST %BASE_URL%/admin/news ^
  -H "Authorization: %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"春節活動開跑！\",\"content\":\"春節期間推出限定活動，參加抽獎就有機會獲得豐富獎品！\",\"imageUrl\":\"%IMAGE_URL%\",\"status\":\"DRAFT\"}"
echo.
echo.
echo 請記下 News ID 供後續測試使用
pause
goto menu

:list_news
echo.
echo ======================================
echo 查詢 News 列表（後台）
echo ======================================
curl -X POST %BASE_URL%/admin/news/list ^
  -H "Authorization: %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{}"
echo.
pause
goto menu

:publish_news
echo.
echo ======================================
echo 上架 News
echo ======================================
set /p NEWS_ID=請輸入 News ID：
curl -X POST %BASE_URL%/admin/news/%NEWS_ID%/publish ^
  -H "Authorization: %TOKEN%"
echo.
pause
goto menu

:query_news
echo.
echo ======================================
echo 查詢 News（前台）
echo ======================================
curl %BASE_URL%/news
echo.
pause
goto menu

:archive_news
echo.
echo ======================================
echo 下架 News
echo ======================================
set /p NEWS_ID=請輸入 News ID：
curl -X POST %BASE_URL%/admin/news/%NEWS_ID%/archive ^
  -H "Authorization: %TOKEN%"
echo.
pause
goto menu

:create_banner
echo.
echo ======================================
echo 新增 Banner（未上架）
echo ======================================
set /p STORE_ID=請輸入店家 ID：
set /p IMAGE_URL=請輸入 imageUrl（例如：/img/banner/xxx.jpg）：
curl -X POST %BASE_URL%/admin/banner ^
  -H "Authorization: %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"storeId\":\"%STORE_ID%\",\"title\":\"春節限時優惠\",\"imageUrl\":\"%IMAGE_URL%\",\"orderNum\":1,\"status\":\"UNPUBLISHED\"}"
echo.
echo.
echo 請記下 Banner ID 供後續測試使用
pause
goto menu

:list_banner
echo.
echo ======================================
echo 查詢 Banner 列表（後台）
echo ======================================
curl -X POST %BASE_URL%/admin/banner/list ^
  -H "Authorization: %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{}"
echo.
pause
goto menu

:publish_banner
echo.
echo ======================================
echo 上架 Banner
echo ======================================
set /p BANNER_ID=請輸入 Banner ID：
curl -X POST %BASE_URL%/admin/banner/%BANNER_ID%/publish ^
  -H "Authorization: %TOKEN%"
echo.
pause
goto menu

:query_banner
echo.
echo ======================================
echo 查詢輪播 Banner（前台）
echo ======================================
curl %BASE_URL%/banner/carousel
echo.
pause
goto menu

:unpublish_banner
echo.
echo ======================================
echo 下架 Banner
echo ======================================
set /p BANNER_ID=請輸入 Banner ID：
curl -X POST %BASE_URL%/admin/banner/%BANNER_ID%/unpublish ^
  -H "Authorization: %TOKEN%"
echo.
pause
goto menu
