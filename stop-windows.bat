@echo off
cd /d "%~dp0"
echo Stopping FoodBridge containers...
docker compose down
echo FoodBridge is stopped. Your MySQL data and uploaded images are preserved.
pause
