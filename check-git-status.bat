@echo off
chcp 65001 >nul
echo ================================================
echo  檢查 Git 推送狀態
echo ================================================
echo.

echo [1] 檢查本地 Git 狀態...
git status
echo.

echo [2] 檢查最近的 commits...
git log --oneline -3
echo.

echo [3] 檢查是否還有未推送的 commits...
git log origin/main..HEAD --oneline
echo.

echo [4] 檢查遠端分支狀態...
git remote show origin
echo.

echo ================================================
echo  檢查完成
echo ================================================
pause
