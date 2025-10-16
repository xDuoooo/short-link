@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 短链接系统中间件服务测试脚本 (Windows批处理版本)

title 短链接系统中间件服务测试

echo.
echo ==========================================
echo 短链接系统中间件服务测试
echo ==========================================
echo.

echo 🔍 测试基础服务连接...
echo.

REM 测试MySQL连接
echo 测试 MySQL 连接...
docker exec mysql mysql -u shortlink -pshortlink123 -e "SELECT 1;" >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ MySQL 连接成功 (localhost:3306)
) else (
    echo ❌ MySQL 连接失败 (localhost:3306)
)

REM 测试Redis连接
echo 测试 Redis 连接...
docker exec redis redis-cli -a redis123 ping >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Redis 连接成功 (localhost:6379)
) else (
    echo ❌ Redis 连接失败 (localhost:6379)
)

REM 测试Zookeeper连接
echo 测试 Zookeeper 连接...
echo srvr | nc localhost 2181 >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Zookeeper 连接成功 (localhost:2181)
) else (
    echo ❌ Zookeeper 连接失败 (localhost:2181)
)

REM 测试Kafka连接
echo 测试 Kafka 连接...
docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Kafka 连接成功 (localhost:9092)
) else (
    echo ❌ Kafka 连接失败 (localhost:9092)
)

echo.
echo 🌐 测试Web服务...
echo.

REM 测试Kafka UI HTTP服务
echo 测试 Kafka UI HTTP服务...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080' -TimeoutSec 5; if ($response.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Kafka UI HTTP服务正常 (http://localhost:8080)
) else (
    echo ❌ Kafka UI HTTP服务异常 (http://localhost:8080)
)

REM 测试MinIO控制台 HTTP服务
echo 测试 MinIO控制台 HTTP服务...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:9001' -TimeoutSec 5; if ($response.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ MinIO控制台 HTTP服务正常 (http://localhost:9001)
) else (
    echo ❌ MinIO控制台 HTTP服务异常 (http://localhost:9001)
)

REM 测试Nacos控制台 HTTP服务
echo 测试 Nacos控制台 HTTP服务...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8848/nacos' -TimeoutSec 5; if ($response.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Nacos控制台 HTTP服务正常 (http://localhost:8848/nacos)
) else (
    echo ❌ Nacos控制台 HTTP服务异常 (http://localhost:8848/nacos)
)

echo.
echo 🐳 检查Docker容器状态...
echo.

docker-compose -f docker-compose.yml ps
if %errorLevel% neq 0 (
    docker compose -f docker-compose.yml ps
)

echo.
echo 📋 最近的服务日志...
echo.

echo MySQL日志 (最近10行):
echo.
docker logs --tail 10 mysql 2>nul
echo.

echo Redis日志 (最近10行):
echo.
docker logs --tail 10 redis 2>nul
echo.

echo Zookeeper日志 (最近10行):
echo.
docker logs --tail 10 zookeeper 2>nul
echo.

echo Kafka日志 (最近10行):
echo.
docker logs --tail 10 kafka 2>nul
echo.

echo ==========================================
echo 测试完成
echo ==========================================
echo.
echo 🎉 如果所有服务都显示正常，说明中间件环境已准备就绪！
echo 💡 如有服务异常，请检查对应的日志信息进行排查。
echo.
pause
