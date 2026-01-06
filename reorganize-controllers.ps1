# PowerShell Script to reorganize controllers

$controllerPath = "c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller"

Write-Host "Starting controller reorganization..." -ForegroundColor Green

# Step 1: Delete duplicate files in api folder
Write-Host "`n[1/3] Deleting duplicate files..." -ForegroundColor Yellow
Remove-Item "$controllerPath\api\LotteryController.java" -Force -ErrorAction SilentlyContinue
Remove-Item "$controllerPath\api\LotteryDrawController.java" -Force -ErrorAction SilentlyContinue

# Step 2: Move frontend controllers to api folder
Write-Host "`n[2/3] Moving frontend controllers to api folder..." -ForegroundColor Yellow
Move-Item "$controllerPath\LotteryController.java" "$controllerPath\api\" -Force -ErrorAction SilentlyContinue
Move-Item "$controllerPath\UserController.java" "$controllerPath\api\" -Force -ErrorAction SilentlyContinue
Move-Item "$controllerPath\ApiAuthController.java" "$controllerPath\api\" -Force -ErrorAction SilentlyContinue
Move-Item "$controllerPath\OAuth2Controller.java" "$controllerPath\api\" -Force -ErrorAction SilentlyContinue
Move-Item "$controllerPath\LotteryDrawController.java" "$controllerPath\api\" -Force -ErrorAction SilentlyContinue

# Step 3: List final structure
Write-Host "`n[3/3] Final structure:" -ForegroundColor Yellow
Write-Host "`nAdmin folder (backend):"
Get-ChildItem "$controllerPath\admin" -Name

Write-Host "`nAPI folder (frontend):"
Get-ChildItem "$controllerPath\api" -Name

Write-Host "`nRoot folder (should be clean):"
Get-ChildItem "$controllerPath" -File -Name

Write-Host "`n✅ Done!" -ForegroundColor Green
