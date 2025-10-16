@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM Docker自动安装脚本 (Windows批处理版本)
REM 支持Windows 10/11系统

title Docker 自动安装脚本

echo.
echo ==========================================
echo Docker 自动安装脚本
echo ==========================================
echo.

REM 检查管理员权限
net session >nul 2>&1
if %errorLevel% == 0 (
    echo ⚠️  检测到管理员权限，建议使用普通用户安装Docker Desktop
    set /p continue="是否继续? (y/N): "
    if /i not "!continue!"=="y" exit /b 1
) else (
    echo ✅ 使用普通用户权限运行
)

echo.
echo 🔍 检查Docker安装状态...

REM 检查Docker是否已安装
where docker >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Docker 已安装
    docker info >nul 2>&1
    if %errorLevel% == 0 (
        echo ✅ Docker 服务运行正常
        goto :check_compose
    ) else (
        echo ⚠️  Docker 已安装但服务未运行
        echo 💡 请启动Docker Desktop应用程序
        goto :wait_docker
    )
)

echo.
echo 🪟 检测到Windows系统，准备安装Docker Desktop...

REM 检查Docker Desktop是否已安装
if exist "C:\Program Files\Docker\Docker\Docker Desktop.exe" (
    echo ✅ Docker Desktop 已安装
    goto :start_docker
)
if exist "C:\Program Files (x86)\Docker\Docker\Docker Desktop.exe" (
    echo ✅ Docker Desktop 已安装
    goto :start_docker
)

echo.
echo 📦 下载Docker Desktop for Windows...

REM 创建临时目录
if not exist "%TEMP%\docker-install" mkdir "%TEMP%\docker-install"
set "installer_path=%TEMP%\docker-install\DockerDesktopInstaller.exe"

REM 下载Docker Desktop
echo ⬇️  正在下载Docker Desktop...
powershell -Command "& {Invoke-WebRequest -Uri 'https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe' -OutFile '%installer_path%'}"

if not exist "%installer_path%" (
    echo ❌ Docker Desktop 下载失败
    echo 💡 请手动下载: https://docs.docker.com/desktop/windows/install/
    pause
    exit /b 1
)

echo ✅ Docker Desktop 下载完成
echo 🚀 正在启动安装程序...

REM 运行安装程序
start "" "%installer_path%"

echo.
echo 💡 请按照安装向导完成Docker Desktop的安装
echo 💡 安装完成后，请启动Docker Desktop
echo.
set /p continue="安装完成后按任意键继续..."

:start_docker
echo.
echo 🚀 启动Docker Desktop...
start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe" 2>nul
if %errorLevel% neq 0 (
    start "" "C:\Program Files (x86)\Docker\Docker\Docker Desktop.exe" 2>nul
)

:wait_docker
echo.
echo ⏳ 等待Docker服务启动...
set /a retries=0
:wait_loop
if %retries% geq 30 (
    echo ❌ Docker服务启动超时，请手动检查Docker Desktop状态
    pause
    exit /b 1
)

docker info >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Docker 服务已启动
    goto :check_compose
)

echo ⏳ 等待Docker服务启动... (!retries!/30)
timeout /t 2 /nobreak >nul
set /a retries+=1
goto :wait_loop

:check_compose
echo.
echo 🔍 检查Docker Compose...

docker-compose --version >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Docker Compose 已安装
    goto :success
)

docker compose version >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Docker Compose (插件) 已安装
    goto :success
)

echo ⚠️  Docker Compose 未检测到
echo 💡 在Windows上，Docker Compose通常随Docker Desktop一起安装
echo 💡 如果仍有问题，请重新启动Docker Desktop

:success
echo.
echo ==========================================
echo 安装完成
echo ==========================================
echo.
echo 🎉 Docker 安装完成！
echo.
echo 📚 接下来可以运行:
echo   deployment\check-environment.bat  # 检查环境
echo   deployment\middleware-start.bat   # 启动中间件
echo.
pause
exit /b 0
