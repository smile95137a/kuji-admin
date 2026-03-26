@echo off
REM 設定 AWS 環境變數（開發用）
REM 請將此檔案複製為 set-aws-credentials.bat 並填入真實憑證
REM ⚠️ 不要將包含真實憑證的檔案提交到 Git！

echo 設定 AWS 憑證環境變數...

REM 請替換為您的 AWS 憑證
set AWS_ACCESS_KEY_ID=your_new_access_key_here
set AWS_SECRET_ACCESS_KEY=your_new_secret_key_here

echo.
echo ✅ 環境變數已設定（本次會話有效）
echo.
echo 驗證設定：
echo AWS_ACCESS_KEY_ID: %AWS_ACCESS_KEY_ID:~0,10%...
echo AWS_SECRET_ACCESS_KEY: %AWS_SECRET_ACCESS_KEY:~0,10%...
echo.
echo 💡 提示：此設定只在當前 CMD 視窗有效
echo    如需永久設定，請使用：
echo    setx AWS_ACCESS_KEY_ID "your_key"
echo    setx AWS_SECRET_ACCESS_KEY "your_secret"
echo.
