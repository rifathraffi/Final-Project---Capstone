@echo off
REM Docker Quick Start Script for Microservices (Windows)

setlocal enabledelayedexpansion

cls
echo ================================
echo Microservices Docker Setup
echo ================================
echo.

REM Check Docker installation
echo Checking Docker installation...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not installed or not in PATH
    pause
    exit /b 1
)
echo [OK] Docker is installed

REM Check Docker Compose installation
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker Compose is not installed
    pause
    exit /b 1
)
echo [OK] Docker Compose is installed
echo.

:menu
cls
echo ================================
echo Select an option:
echo ================================
echo 1) Build and Start All Services
echo 2) Start Services (without rebuild)
echo 3) Stop Services
echo 4) View Service Status
echo 5) View Logs (All Services)
echo 6) View Logs (Specific Service)
echo 7) Stop and Remove All
echo 8) Exit
echo ================================
set /p choice="Enter your choice [1-8]: "

if "%choice%"=="1" goto build_and_start
if "%choice%"=="2" goto start_services
if "%choice%"=="3" goto stop_services
if "%choice%"=="4" goto view_status
if "%choice%"=="5" goto view_logs
if "%choice%"=="6" goto view_service_logs
if "%choice%"=="7" goto stop_and_remove
if "%choice%"=="8" goto exit_script
goto menu

:build_and_start
cls
echo Building Docker images...
call docker-compose build
if errorlevel 1 (
    echo Build failed
    pause
    goto menu
)
echo [OK] Build complete
echo.
echo Starting services...
call docker-compose up -d
echo [OK] Services starting
echo.
echo Waiting for services to be healthy...
timeout /t 10
call docker-compose ps
pause
goto menu

:start_services
cls
echo Starting services...
call docker-compose up -d
timeout /t 5
call docker-compose ps
pause
goto menu

:stop_services
cls
echo Stopping services...
call docker-compose stop
echo [OK] Services stopped
pause
goto menu

:view_status
cls
echo Service Status:
call docker-compose ps
pause
goto menu

:view_logs
cls
echo Fetching logs (Press Ctrl+C to exit)...
call docker-compose logs -f
pause
goto menu

:view_service_logs
cls
echo Available services:
call docker-compose ps --services
set /p service="Enter service name: "
call docker-compose logs -f %service%
pause
goto menu

:stop_and_remove
cls
set /p confirm="This will stop and remove all containers. Continue? (y/n): "
if /i "%confirm%"=="y" (
    echo Removing services...
    call docker-compose down -v
    echo [OK] All services removed
) else (
    echo Cancelled
)
pause
goto menu

:exit_script
echo Goodbye!
exit /b 0
