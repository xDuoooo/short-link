#!/bin/bash

# 短链接系统中间件一键启动脚本
# 启动基础中间件服务：MySQL、Redis、Kafka、MinIO、Nacos
# 不包含前端和后端应用服务

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

print_title() {
    echo ""
    print_message $BLUE "=========================================="
    print_message $BLUE "$1"
    print_message $BLUE "=========================================="
    echo ""
}

# 检查Docker
if ! command -v docker &> /dev/null; then
    print_message $RED "❌ Docker 未安装，请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_message $RED "❌ Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi

print_title "短链接系统中间件一键启动"

# 检查环境
print_message $YELLOW "🔍 检查运行环境..."
if [ -f "./check-environment.sh" ]; then
    ./check-environment.sh
    echo ""
    read -p "是否继续启动中间件服务? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_message $YELLOW "❌ 用户取消启动"
        exit 0
    fi
else
    print_message $YELLOW "⚠️  环境检查脚本不存在，跳过环境检查"
fi

# 创建环境文件
if [ ! -f ".env" ]; then
    print_message $YELLOW "📝 创建环境配置文件..."
    cat > .env << EOF
# 数据库配置
MYSQL_ROOT_PASSWORD=root123456
MYSQL_DATABASE=shortlink
MYSQL_USER=shortlink
MYSQL_PASSWORD=shortlink123

# Redis配置
REDIS_PASSWORD=redis123

# MinIO配置
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin123

# 邮件配置
EMAIL_USERNAME=your-email@qq.com
EMAIL_PASSWORD=your-email-password

# 高德地图API Key
AMAP_KEY=your-amap-key

# 域名配置
SHORT_LINK_DOMAIN=http://localhost:8000
EOF
    print_message $GREEN "✅ 环境配置文件已创建"
fi

# 创建目录
mkdir -p logs/{mysql,redis,zookeeper,kafka,minio,nacos}

# 启动中间件服务
print_message $YELLOW "🚀 启动中间件服务..."
docker-compose -f docker-compose.yml up -d

# 等待服务启动
print_message $YELLOW "⏳ 等待服务启动..."
sleep 30

# 修复Nacos数据库权限问题
print_message $YELLOW "🔧 修复Nacos数据库权限..."
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-root123456}
if docker exec mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "GRANT ALL PRIVILEGES ON nacos.* TO 'shortlink'@'%'; FLUSH PRIVILEGES;" 2>/dev/null; then
    print_message $GREEN "✅ Nacos数据库权限修复成功"
else
    print_message $YELLOW "⚠️  Nacos数据库权限修复失败，可能需要手动处理"
fi

# 检查服务状态
print_message $YELLOW "🔍 检查服务状态..."
docker-compose -f docker-compose.yml ps

# 运行服务测试
print_message $YELLOW "🧪 运行服务连接测试..."
if [ -f "./test-services.sh" ]; then
    ./test-services.sh
else
    print_message $RED "❌ 测试脚本不存在，跳过服务测试"
fi

print_title "启动完成"

echo ""
print_message $GREEN "🌐 服务访问地址:"
echo ""
print_message $BLUE "Zookeeper:    localhost:2181"
print_message $BLUE "Kafka:        localhost:9092"
print_message $BLUE "Kafka UI:     http://localhost:8080"
print_message $BLUE "MinIO控制台:  http://localhost:9001"
print_message $BLUE "Nacos控制台:  http://localhost:8848/nacos"
print_message $BLUE "默认账号:     minioadmin / minioadmin123"
print_message $BLUE "Nacos账号:    nacos / nacos"
echo ""

print_message $YELLOW "📝 接下来请手动启动应用服务:"
echo ""
print_message $BLUE "1. 启动后端服务:"
print_message $BLUE "   cd admin && mvn spring-boot:run"
print_message $BLUE "   cd project && mvn spring-boot:run"
print_message $BLUE "   cd gateway && mvn spring-boot:run"
echo ""
print_message $BLUE "2. 启动前端:"
print_message $BLUE "   cd frontend && npm start"
echo ""

print_message $GREEN "✅ 中间件服务已准备就绪！"
echo ""
print_message $BLUE "🧪 服务测试: ./test-services.sh"
