@echo off
echo 正在刪除重複的 Controller 檔案...

set API_PATH=c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api

echo.
echo 刪除 LotteryController.java (重複)
del /F /Q "%API_PATH%\LotteryController.java"

echo 刪除 FrontendLotteryController.java (重複)
del /F /Q "%API_PATH%\FrontendLotteryController.java"

echo.
echo ✅ 刪除完成！
echo.
echo 剩餘檔案：
dir /B "%API_PATH%\*.java"

echo.
echo 預期應該只有 5 個檔案：
echo - ApiAuthController.java
echo - LotteryBrowseController.java
echo - LotteryDrawController.java
echo - OAuth2Controller.java
echo - UserController.java

pause
