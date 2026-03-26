@echo off
chcp 65001 >nul
echo ========================================
echo 📚 整理專案文件到分類資料夾
echo ========================================
echo.

REM 建立資料夾
echo [1/7] 建立分類資料夾...
if not exist "docs\01-架構說明" mkdir "docs\01-架構說明"
if not exist "docs\02-API文件" mkdir "docs\02-API文件"
if not exist "docs\03-資料庫相關" mkdir "docs\03-資料庫相關"
if not exist "docs\04-部署指南" mkdir "docs\04-部署指南"
if not exist "docs\05-測試相關" mkdir "docs\05-測試相關"
if not exist "docs\06-問題修復" mkdir "docs\06-問題修復"

REM 架構說明
echo [2/7] 整理架構說明文件...
move /Y "ARCHITECTURE_ANALYSIS.md" "docs\01-架構說明\" 2>nul
move /Y "ARCHITECTURE_REFACTORING_SUMMARY.md" "docs\01-架構說明\" 2>nul
move /Y "API_SECURITY_IMPROVEMENTS.md" "docs\01-架構說明\" 2>nul
move /Y "CONTROLLER_REORGANIZATION_COMPLETE.md" "docs\01-架構說明\" 2>nul
move /Y "JWT_STOREID_ENHANCEMENT_COMPLETE.md" "docs\01-架構說明\" 2>nul
move /Y "FRONTEND_BACKEND_STORE_API_SEPARATION.md" "docs\01-架構說明\" 2>nul

REM API 文件
echo [3/7] 整理 API 文件...
move /Y "LOTTERY_WITH_PRIZES_API_GUIDE.md" "docs\02-API文件\" 2>nul
move /Y "API_TEST_GUIDE.md" "docs\02-API文件\" 2>nul
move /Y "API_TEST_GUIDE_PRIZE_BOX_WALLET_ORDER.md" "docs\02-API文件\" 2>nul
move /Y "LOTTERY_COPY_API_TEST_GUIDE.md" "docs\02-API文件\" 2>nul
move /Y "GET_USER_MENU_GUIDE.md" "docs\02-API文件\" 2>nul
move /Y "ROLE_PERMISSION_TEST_GUIDE.md" "docs\02-API文件\" 2>nul
move /Y "doc\DRAW_FLOW.md" "docs\02-API文件\" 2>nul
move /Y "doc\API.md" "docs\02-API文件\" 2>nul

REM 資料庫相關
echo [4/7] 整理資料庫文件...
move /Y "MISSING_TABLES_GUIDE.md" "docs\03-資料庫相關\" 2>nul
move /Y "missing_tables_ddl.sql" "docs\03-資料庫相關\" 2>nul
move /Y "doc\GENERATOR_USAGE_GUIDE.md" "docs\03-資料庫相關\" 2>nul
move /Y "doc\GENERATOR_USAGE.md" "docs\03-資料庫相關\" 2>nul
move /Y "doc\GENERATOR_SAFETY_GUIDE.md" "docs\03-資料庫相關\" 2>nul
move /Y "doc\DATA_INITIALIZATION.md" "docs\03-資料庫相關\" 2>nul
move /Y "init-prize-box-order-wallet.sql" "docs\03-資料庫相關\" 2>nul

REM 部署指南
echo [5/7] 整理部署文件...
move /Y "DEPLOY_GUIDE.md" "docs\04-部署指南\" 2>nul
move /Y "EC2_QUICK_DEPLOY_COMMANDS.md" "docs\04-部署指南\" 2>nul
move /Y "EC2_JAVA_SETUP_GUIDE.md" "docs\04-部署指南\" 2>nul
move /Y "PRODUCTION_DEPLOYMENT_GUIDE.md" "docs\04-部署指南\" 2>nul
move /Y "PRODUCTION_CONFIG_SUMMARY.md" "docs\04-部署指南\" 2>nul
move /Y "PRODUCTION_MIGRATION_COMPLETE.md" "docs\04-部署指南\" 2>nul
move /Y "PORT_80_DEPLOYMENT_GUIDE.md" "docs\04-部署指南\" 2>nul
move /Y "AWS_S3_SETUP_GUIDE.md" "docs\04-部署指南\" 2>nul
move /Y "CORS_FIX_AND_DEPLOY.md" "docs\04-部署指南\" 2>nul
move /Y "HEALTH_CHECK_SETUP.md" "docs\04-部署指南\" 2>nul

REM 測試相關
echo [6/7] 整理測試文件...
move /Y "QUICK_TEST_GUIDE.md" "docs\05-測試相關\" 2>nul
move /Y "COMPLETE_TEST_PLAN.md" "docs\05-測試相關\" 2>nul
move /Y "FEATURE_TEST_GUIDE.md" "docs\05-測試相關\" 2>nul
move /Y "COMPLETE_FEATURE_TEST_GUIDE.md" "docs\05-測試相關\" 2>nul
move /Y "READY_TO_TEST.md" "docs\05-測試相關\" 2>nul
move /Y "PRE_LAUNCH_CHECKLIST.md" "docs\05-測試相關\" 2>nul
move /Y "*.postman_collection.json" "docs\05-測試相關\" 2>nul

REM 問題修復
echo [7/7] 整理問題修復文件...
move /Y "API_403_FIX_SUMMARY.md" "docs\06-問題修復\" 2>nul
move /Y "ADMINJWTFILTER_ERROR_ANALYSIS.md" "docs\06-問題修復\" 2>nul
move /Y "FOREIGN_KEY_FIX_GUIDE.md" "docs\06-問題修復\" 2>nul
move /Y "FOREIGN_KEY_ERROR_FIX_SUMMARY.md" "docs\06-問題修復\" 2>nul
move /Y "MENU_NULL_FIX.md" "docs\06-問題修復\" 2>nul
move /Y "CREATE_STORE_OWNER_FIX_REPORT.md" "docs\06-問題修復\" 2>nul
move /Y "CREATE_STORE_OWNER_ERROR_DIAGNOSIS.md" "docs\06-問題修復\" 2>nul
move /Y "BACKEND_NOT_RUNNING_FIX.md" "docs\06-問題修復\" 2>nul
move /Y "doc\COMPILE_ERROR_FIX.md" "docs\06-問題修復\" 2>nul
move /Y "doc\COMPILATION_FIX_SUMMARY.md" "docs\06-問題修復\" 2>nul
move /Y "doc\COMPILATION_FIX_ROUND2.md" "docs\06-問題修復\" 2>nul
move /Y "FIX_SUMMARY.md" "docs\06-問題修復\" 2>nul

echo.
echo ========================================
echo ✅ 文件整理完成！
echo ========================================
echo.
echo 📂 所有文件已整理到 docs\ 資料夾
echo 📖 查看總覽：docs\README.md
echo.
pause
