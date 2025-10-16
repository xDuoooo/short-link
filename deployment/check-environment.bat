@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 短链接系统环境检查脚本 (Windows批处理版本)

title 短链接系统环境检查

echo.
echo ==========================================
echo 短链接系统环境检查
echo ==========================================
echo.

set "docker_ok=false"
set "docker_compose_ok=false"
set "ports_ok=true"

echo 🔍 检查必需软件...
echo.

REM 检查Docker
where docker >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Docker 已安装
    for /f "tokens=*" %%i in ('docker --version 2^>nul') do set "docker_version=%%i"
    echo    !docker_version!
    set "docker_ok=true"
) else (
    echo ❌ Docker 未安装
)

REM 检查Docker Compose
where docker-compose >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Docker Compose 已安装
    for /f "tokens=*" %%i in ('docker-compose --version 2^>nul') do set "compose_version=%%i"
    echo    !compose_version!
    set "docker_compose_ok=true"
) else (
    docker compose version >nul 2>&1
    if %errorLevel% == 0 (
        echo ✅ Docker Compose (插件) 已安装
        for /f "tokens=*" %%i in ('docker compose version 2^>nul') do set "compose_version=%%i"
        echo    !compose_version!
        set "docker_compose_ok=true"
    ) else (
        echo ❌ Docker Compose 未安装
    )
)

echo.

REM 检查Docker服务状态
if "%docker_ok%"=="true" (
    echo 🔍 检查Docker服务状态...
    docker info >nul 2>&1
    if %errorLevel% == 0 (
        echo ✅ Docker服务运行正常
    ) else (
        echo ❌ Docker服务未运行或权限不足
        echo 💡 请尝试以下解决方案:
        echo    1. 启动Docker Desktop应用程序
        echo    2. 检查Docker Desktop是否正在运行
        echo    3. 重启Docker Desktop
        echo.
    )
)

echo.
echo 🔌 检查端口占用...
echo.

REM 检查端口占用
call :check_port 3306 "MySQL"
call :check_port 6379 "Redis"
call :check_port 2181 "Zookeeper"
call :check_port 9092 "Kafka"
call :check_port 8080 "Kafka UI"
call :check_port 9000 "MinIO"
call :check_port 9001 "MinIO Console"
call :check_port 8848 "Nacos"

echo.
echo 💾 检查磁盘空间...
echo.

REM 检查磁盘空间
for /f "tokens=3" %%i in ('dir /-c ^| find "bytes free"') do set "free_space=%%i"
set /a free_gb=!free_space!/1073741824
if !free_gb! gtr 5 (
    echo ✅ 磁盘空间充足: !free_gb!G 可用
) else (
    echo ⚠️  磁盘空间不足: !free_gb!G 可用 (建议至少5G)
    set "disk_ok=false"
)

echo.

REM 总结检查结果
echo ==========================================
echo 检查结果
echo ==========================================

if "%docker_ok%"=="true" if "%docker_compose_ok%"=="true" if "%ports_ok%"=="true" (
    echo 🎉 环境检查通过！可以运行 deployment\middleware-start.bat 启动中间件服务
    goto :end
)

if "%docker_ok%"=="true" if "%docker_compose_ok%"=="true" if "%ports_ok%"=="false" (
    echo ⚠️  环境检查部分通过，但存在端口冲突
    echo.
    echo 💡 解决方案:
    echo 1. 停止占用端口的服务:
    echo    netstat -ano ^| findstr :3306
    echo    netstat -ano ^| findstr :6379
    echo    netstat -ano ^| findstr :2181
    echo    netstat -ano ^| findstr :9092
    echo    netstat -ano ^| findstr :8080
    echo    netstat -ano ^| findstr :9000
    echo    netstat -ano ^| findstr :9001
    echo    netstat -ano ^| findstr :8848
    echo.
    echo 2. 或者修改 docker-compose.yml 中的端口映射
    echo.
    echo ⚠️  如果继续启动，Docker容器可能会启动失败
    echo.
    set /p continue="是否强制继续启动? (y/N): "
    if /i not "!continue!"=="y" (
        echo ❌ 用户取消启动，请先解决端口冲突问题
        pause
        exit /b 1
    )
    goto :end
)

echo ❌ 环境检查失败，请先解决以下问题:
echo.

if "%docker_ok%"=="false" (
    echo 📋 软件安装指南:
    echo.
    echo Docker 安装:
    echo   Windows: https://docs.docker.com/desktop/windows/install/
    echo   或运行: deployment\install-docker.bat
    echo.
)

if "%docker_compose_ok%"=="false" (
    echo Docker Compose 安装:
    echo   Windows: 通常随Docker Desktop一起安装
    echo   或重新安装Docker Desktop
    echo.
)

if "%disk_ok%"=="false" (
    echo 💾 磁盘空间不足，请清理磁盘空间
    echo.
)

pause
exit /b 1

:check_port
set "port=%~1"
set "service=%~2"
netstat -an | findstr ":%port% " >nul 2>&1
if %errorLevel% == 0 (
    echo ⚠️  端口 %port% (%service%) 已被占用
    set "ports_ok=false"
) else (
    echo ✅ 端口 %port% (%service%) 可用
)
goto :eof

:end
echo.
echo 📚 详细文档: .\MIDDLEWARE_SETUP.md
pause
