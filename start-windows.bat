@echo off
setlocal
cd /d "%~dp0"
echo.
echo  ==============================================
echo       FoodBridge - Local Startup
echo  ==============================================
echo.
where docker >nul 2>nul
if errorlevel 1 (
  echo ERROR: Docker was not found.
  echo Install Docker Desktop for Windows, open it, then run this file again.
  pause
  exit /b 1
)
docker compose version >nul 2>nul
if errorlevel 1 (
  echo ERROR: Docker Compose is not available.
  echo Update or reinstall Docker Desktop, then try again.
  pause
  exit /b 1
)
echo Starting MySQL, Spring Boot API and React frontend...
echo First startup can take several minutes while images and dependencies download.
echo Keep this window open. Press Ctrl+C to stop the foreground logs.
echo.
start "FoodBridge - Browser" cmd /c "timeout /t 45 >nul & start http://localhost:5173"
docker compose up --build
pause
