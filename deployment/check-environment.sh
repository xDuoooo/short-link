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

# 检查命令是否存在
check_command() {
    local cmd=$1
    local name=$2
    
    if command -v "$cmd" &> /dev/null; then
        local version=$($cmd --version 2>/dev/null | head -n1)
        print_message $GREEN "✅ $name 已安装: $version"
        return 0
    else
        print_message $RED "❌ $name 未安装"
        return 1
    fi
}

# 检查Docker服务状态
check_docker_service() {
    print_message $YELLOW "检查Docker服务状态..."
    
    if docker info &> /dev/null; then
        print_message $GREEN "✅ Docker服务运行正常"
        return 0
    else
        print_message $RED "❌ Docker服务未运行或权限不足"
        print_message $YELLOW "💡 请尝试以下解决方案:"
        print_message $YELLOW "   1. 启动Docker服务: sudo systemctl start docker"
        print_message $YELLOW "   2. 将用户添加到docker组: sudo usermod -aG docker \$USER"
        print_message $YELLOW "   3. 重新登录或执行: newgrp docker"
        return 1
    fi
}

# 检查端口占用
check_port() {
    local port=$1
    local service=$2
    
    if lsof -i :$port &> /dev/null; then
        print_message $YELLOW "⚠️  端口 $port ($service) 已被占用"
        return 1
    else
        print_message $GREEN "✅ 端口 $port ($service) 可用"
        return 0
    fi
}

# 检查磁盘空间
check_disk_space() {
    local available=$(df -h . | awk 'NR==2 {print $4}' | sed 's/G//')
    local available_num=$(echo $available | sed 's/[^0-9.]//g')
    
    if (( $(echo "$available_num > 5" | bc -l) )); then
        print_message $GREEN "✅ 磁盘空间充足: ${available}G 可用"
        return 0
    else
        print_message $YELLOW "⚠️  磁盘空间不足: ${available}G 可用 (建议至少5G)"
        return 1
    fi
}

print_title "短链接系统环境检查"

# 检查必需软件
print_message $BLUE "🔍 检查必需软件..."
echo ""

docker_ok=false
docker_compose_ok=false

if check_command "docker" "Docker"; then
    docker_ok=true
fi

if check_command "docker-compose" "Docker Compose"; then
    docker_compose_ok=true
elif check_command "docker" "Docker Compose (插件)"; then
    if docker compose version &> /dev/null; then
        print_message $GREEN "✅ Docker Compose (插件) 已安装"
        docker_compose_ok=true
    else
        print_message $RED "❌ Docker Compose 插件未安装"
    fi
fi

echo ""

# 检查Docker服务
if [ "$docker_ok" = true ]; then
    check_docker_service
    echo ""
fi

# 检查端口占用
print_message $BLUE "🔌 检查端口占用..."
echo ""

check_port 3306 "MySQL"
check_port 6379 "Redis"
check_port 2181 "Zookeeper"
check_port 9092 "Kafka"
check_port 8080 "Kafka UI"
check_port 9000 "MinIO"
check_port 9001 "MinIO Console"
check_port 8848 "Nacos"

echo ""

# 检查磁盘空间
print_message $BLUE "💾 检查磁盘空间..."
echo ""
check_disk_space

echo ""

# 总结
print_title "检查结果"

if [ "$docker_ok" = true ] && [ "$docker_compose_ok" = true ]; then
    print_message $GREEN "🎉 环境检查通过！可以运行 ./deployment/middleware-start.sh 启动中间件服务"
else
    print_message $RED "❌ 环境检查失败，请先安装必需软件"
    echo ""
    print_message $YELLOW "📋 安装指南:"
    echo ""
    if [ "$docker_ok" = false ]; then
        print_message $BLUE "Docker 安装:"
        print_message $BLUE "  macOS: https://docs.docker.com/desktop/mac/install/"
        print_message $BLUE "  Linux: https://docs.docker.com/engine/install/"
        echo ""
    fi
    
    if [ "$docker_compose_ok" = false ]; then
        print_message $BLUE "Docker Compose 安装:"
        print_message $BLUE "  macOS: 通常随Docker Desktop一起安装"
        print_message $BLUE "  Linux: sudo apt-get install docker-compose-plugin"
        echo ""
    fi
fi

print_message $BLUE "📚 详细文档: ./MIDDLEWARE_SETUP.md"
