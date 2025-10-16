#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印函数
print_message() {
    echo -e "${1}${2}${NC}"
}

print_title() {
    echo ""
    echo "=========================================="
    echo -e "${BLUE}$1${NC}"
    echo "=========================================="
}

# 测试服务连接
test_service() {
    local service_name=$1
    local host=$2
    local port=$3
    local timeout=${4:-5}
    
    print_message $YELLOW "测试 $service_name 连接..."
    
    if timeout $timeout bash -c "</dev/tcp/$host/$port" 2>/dev/null; then
        print_message $GREEN "✅ $service_name 连接成功 ($host:$port)"
        return 0
    else
        print_message $RED "❌ $service_name 连接失败 ($host:$port)"
        return 1
    fi
}

# 测试MySQL连接
test_mysql() {
    print_message $YELLOW "测试 MySQL 连接..."
    
    if docker exec mysql mysql -u shortlink -pshortlink123 -e "SELECT 1;" >/dev/null 2>&1; then
        print_message $GREEN "✅ MySQL 连接成功 (localhost:3306)"
        return 0
    else
        print_message $RED "❌ MySQL 连接失败 (localhost:3306)"
        return 1
    fi
}

# 测试Redis连接
test_redis() {
    print_message $YELLOW "测试 Redis 连接..."
    
    if redis-cli -h localhost -p 6379 -a redis123 ping >/dev/null 2>&1; then
        print_message $GREEN "✅ Redis 连接成功 (localhost:6379)"
        return 0
    else
        print_message $RED "❌ Redis 连接失败 (localhost:6379)"
        return 1
    fi
}

# 测试Zookeeper连接
test_zookeeper() {
    print_message $YELLOW "测试 Zookeeper 连接..."
    
    if echo "srvr" | nc localhost 2181 >/dev/null 2>&1; then
        print_message $GREEN "✅ Zookeeper 连接成功 (localhost:2181)"
        return 0
    else
        print_message $RED "❌ Zookeeper 连接失败 (localhost:2181)"
        return 1
    fi
}

# 测试Kafka连接
test_kafka() {
    print_message $YELLOW "测试 Kafka 连接..."
    
    if docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >/dev/null 2>&1; then
        print_message $GREEN "✅ Kafka 连接成功 (localhost:9092)"
        return 0
    else
        print_message $RED "❌ Kafka 连接失败 (localhost:9092)"
        return 1
    fi
}

# 测试HTTP服务
test_http_service() {
    local service_name=$1
    local url=$2
    local timeout=${3:-5}
    
    print_message $YELLOW "测试 $service_name HTTP服务..."
    
    if curl -s --max-time $timeout "$url" > /dev/null 2>&1; then
        print_message $GREEN "✅ $service_name HTTP服务正常 ($url)"
        return 0
    else
        print_message $RED "❌ $service_name HTTP服务异常 ($url)"
        return 1
    fi
}

print_title "短链接系统中间件服务测试"

# 测试基础服务
print_message $BLUE "🔍 测试基础服务连接..."
echo ""

test_mysql
test_redis
test_zookeeper
test_kafka

echo ""

# 测试Web服务
print_message $BLUE "🌐 测试Web服务..."
echo ""

test_http_service "Kafka UI" "http://localhost:8080"
test_http_service "MinIO控制台" "http://localhost:9001"
test_http_service "Nacos控制台" "http://localhost:8848/nacos"

echo ""

# 检查Docker容器状态
print_message $BLUE "🐳 检查Docker容器状态..."
echo ""

docker-compose -f docker-compose.yml ps

echo ""

# 检查服务日志
print_message $BLUE "📋 最近的服务日志..."
echo ""

print_message $YELLOW "MySQL日志 (最近10行):"
docker logs mysql --tail 10 2>/dev/null || print_message $RED "无法获取MySQL日志"

echo ""

print_message $YELLOW "Redis日志 (最近10行):"
docker logs redis --tail 10 2>/dev/null || print_message $RED "无法获取Redis日志"

echo ""

print_message $YELLOW "Zookeeper日志 (最近10行):"
docker logs zookeeper --tail 10 2>/dev/null || print_message $RED "无法获取Zookeeper日志"

echo ""

print_message $YELLOW "Kafka日志 (最近10行):"
docker logs kafka --tail 10 2>/dev/null || print_message $RED "无法获取Kafka日志"

echo ""

print_title "测试完成"

print_message $GREEN "🎉 如果所有服务都显示正常，说明中间件环境已准备就绪！"
print_message $YELLOW "💡 如有服务异常，请检查对应的日志信息进行排查。"
