@echo off
echo Cleaning up and moving controllers...

REM Delete duplicate files created by AI
del /F /Q "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api\LotteryController.java" 2>nul

REM Move frontend controllers from root to api folder
move /Y "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\LotteryController.java" "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api\" 2>nul
move /Y "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\UserController.java" "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api\" 2>nul
move /Y "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\ApiAuthController.java" "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api\" 2>nul
move /Y "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\OAuth2Controller.java" "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api\" 2>nul

echo Done!
