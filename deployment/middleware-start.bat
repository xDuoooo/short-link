@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 短链接系统中间件一键启动脚本 (Windows批处理版本)
REM 启动基础中间件服务：MySQL、Redis、Kafka、MinIO、Nacos
REM 不包含前端和后端应用服务

title 短链接系统中间件一键启动

echo.
echo ==========================================
echo 短链接系统中间件一键启动
echo ==========================================
echo.

REM 检查Docker
where docker >nul 2>&1
if %errorLevel% neq 0 (
echo ❌ Docker 未安装，请先安装 Docker
echo 💡 运行: deployment\install-docker.bat
echo.
echo 按任意键退出...
pause
exit /b 1
)

REM 检查Docker Compose
where docker-compose >nul 2>&1
if %errorLevel% neq 0 (
    docker compose version >nul 2>&1
    if %errorLevel% neq 0 (
echo ❌ Docker Compose 未安装，请先安装 Docker Compose
echo 💡 重新安装Docker Desktop或运行: deployment\install-docker.bat
echo.
echo 按任意键退出...
pause
exit /b 1
    )
)

echo 🔍 检查运行环境...
if exist ".\check-environment.bat" (
    call .\check-environment.bat
    echo.
    set /p continue="是否继续启动中间件服务? (y/N): "
if /i not "!continue!"=="y" (
    echo ❌ 用户取消启动
echo.
echo 按任意键退出...
    pause
    exit /b 0
)
) else (
    echo ⚠️  环境检查脚本不存在，跳过环境检查
)

echo.
echo 📝 创建环境配置文件...
if not exist ".env" (
    (
        echo # 数据库配置
        echo MYSQL_ROOT_PASSWORD=root123456
        echo MYSQL_DATABASE=shortlink
        echo MYSQL_USER=shortlink
        echo MYSQL_PASSWORD=shortlink123
        echo.
        echo # Redis配置
        echo REDIS_PASSWORD=redis123
        echo.
        echo # MinIO配置
        echo MINIO_ROOT_USER=minioadmin
        echo MINIO_ROOT_PASSWORD=minioadmin123
        echo.
        echo # 邮件配置
        echo EMAIL_USERNAME=your-email@qq.com
        echo EMAIL_PASSWORD=your-email-password
        echo.
        echo # 高德地图API Key
        echo AMAP_KEY=your-amap-key
        echo.
        echo # 域名配置
        echo SHORT_LINK_DOMAIN=http://localhost:8000
    ) > .env
    echo ✅ 环境配置文件已创建
)

echo.
echo 📁 创建日志目录...
if not exist "logs" mkdir logs
if not exist "logs\mysql" mkdir logs\mysql
if not exist "logs\redis" mkdir logs\redis
if not exist "logs\zookeeper" mkdir logs\zookeeper
if not exist "logs\kafka" mkdir logs\kafka
if not exist "logs\minio" mkdir logs\minio
if not exist "logs\nacos" mkdir logs\nacos

echo.
echo 🚀 启动中间件服务...
docker-compose -f docker-compose.yml up -d
if %errorLevel% neq 0 (
    echo ❌ 启动失败，尝试使用docker compose命令...
    docker compose -f docker-compose.yml up -d
    if %errorLevel% neq 0 (
echo ❌ 启动失败，请检查Docker和docker-compose.yml配置
echo.
echo 按任意键退出...
pause
exit /b 1
    )
)

echo.
echo ⏳ 等待服务启动...
timeout /t 30 /nobreak >nul

echo.
echo 🔧 修复Nacos数据库权限...
docker exec mysql mysql -uroot -proot123456 -e "GRANT ALL PRIVILEGES ON nacos.* TO 'shortlink'@'%%'; FLUSH PRIVILEGES;" >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Nacos数据库权限修复成功
) else (
    echo ⚠️  Nacos数据库权限修复失败，可能需要手动处理
)

echo.
echo 🔍 检查服务状态...
docker-compose -f docker-compose.yml ps
if %errorLevel% neq 0 (
    docker compose -f docker-compose.yml ps
)

echo.
echo 🧪 运行服务连接测试...
if exist ".\test-services.bat" (
    call .\test-services.bat
) else (
    echo ❌ 测试脚本不存在，跳过服务测试
)

echo.
echo ==========================================
echo 启动完成
echo ==========================================
echo.
echo 🌐 服务访问地址:
echo.
echo Zookeeper:    localhost:2181
echo Kafka:        localhost:9092
echo Kafka UI:     http://localhost:8080
echo MinIO控制台:  http://localhost:9001
echo Nacos控制台:  http://localhost:8848/nacos
echo 默认账号:     minioadmin / minioadmin123
echo Nacos账号:    nacos / nacos
echo.

echo 📝 接下来请手动启动应用服务:
echo.
echo 1. 启动后端服务:
echo    cd admin ^&^& mvn spring-boot:run
echo    cd project ^&^& mvn spring-boot:run
echo    cd gateway ^&^& mvn spring-boot:run
echo.
echo 2. 启动前端:
echo    cd frontend ^&^& npm start
echo.

echo ✅ 中间件服务已准备就绪！
echo.
echo 🧪 服务测试: deployment\test-services.bat
echo.
echo 按任意键退出...
pause
