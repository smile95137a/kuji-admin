@echo off
setlocal enabledelayedexpansion

echo ====== 測試變數設定 ======

set "API_TEST_NAME=測試"
set "API_METHOD=POST"
set "API_URL=http://localhost:8080/api/test"
set "API_DATA={\"test\":\"data\"}"
set "API_TOKEN="
set "API_EXPECT=true"

echo API_TEST_NAME=!API_TEST_NAME!
echo API_METHOD=!API_METHOD!
echo API_URL=!API_URL!
echo API_DATA=!API_DATA!
echo API_TOKEN=!API_TOKEN!
echo API_EXPECT=!API_EXPECT!

echo.
echo ====== 測試 CALL 後的變數 ======
call :test_function

goto :end

:test_function
    echo 在 function 內:
    echo API_TEST_NAME=!API_TEST_NAME!
    echo API_METHOD=!API_METHOD!
    echo API_URL=!API_URL!
    goto :eof

:end
endlocal
pause
