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

ports_ok=true
check_port 3306 "MySQL" || ports_ok=false
check_port 6379 "Redis" || ports_ok=false
check_port 2181 "Zookeeper" || ports_ok=false
check_port 9092 "Kafka" || ports_ok=false
check_port 8080 "Kafka UI" || ports_ok=false
check_port 9000 "MinIO" || ports_ok=false
check_port 9001 "MinIO Console" || ports_ok=false
check_port 8848 "Nacos" || ports_ok=false

echo ""

# 检查磁盘空间
print_message $BLUE "💾 检查磁盘空间..."
echo ""
disk_ok=true
check_disk_space || disk_ok=false

echo ""

# 总结
print_title "检查结果"

# 综合检查结果
if [ "$docker_ok" = true ] && [ "$docker_compose_ok" = true ] && [ "$ports_ok" = true ] && [ "$disk_ok" = true ]; then
    print_message $GREEN "🎉 环境检查通过！可以运行 ./deployment/middleware-start.sh 启动中间件服务"
elif [ "$docker_ok" = true ] && [ "$docker_compose_ok" = true ] && [ "$ports_ok" = false ]; then
    print_message $YELLOW "⚠️  环境检查部分通过，但存在端口冲突"
    echo ""
    print_message $YELLOW "💡 解决方案:"
    print_message $BLUE "1. 停止占用端口的服务:"
    print_message $BLUE "   sudo lsof -ti:3306 | xargs kill -9  # MySQL"
    print_message $BLUE "   sudo lsof -ti:6379 | xargs kill -9  # Redis"
    print_message $BLUE "   sudo lsof -ti:2181 | xargs kill -9  # Zookeeper"
    print_message $BLUE "   sudo lsof -ti:9092 | xargs kill -9  # Kafka"
    print_message $BLUE "   sudo lsof -ti:8080 | xargs kill -9  # Kafka UI"
    print_message $BLUE "   sudo lsof -ti:9000 | xargs kill -9  # MinIO"
    print_message $BLUE "   sudo lsof -ti:9001 | xargs kill -9  # MinIO Console"
    print_message $BLUE "   sudo lsof -ti:8848 | xargs kill -9  # Nacos"
    echo ""
    print_message $BLUE "2. 或者修改 docker-compose.yml 中的端口映射"
    echo ""
    print_message $YELLOW "⚠️  如果继续启动，Docker容器可能会启动失败"
    echo ""
    read -p "是否强制继续启动? (y/N): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_message $YELLOW "⚠️  用户选择强制启动，可能会遇到端口冲突问题"
    else
        print_message $YELLOW "❌ 用户取消启动，请先解决端口冲突问题"
        exit 1
    fi
else
    print_message $RED "❌ 环境检查失败，请先解决以下问题:"
    echo ""
    
    if [ "$docker_ok" = false ] || [ "$docker_compose_ok" = false ]; then
        print_message $YELLOW "📋 软件安装指南:"
        echo ""
    if [ "$docker_ok" = false ]; then
        print_message $BLUE "Docker 安装:"
        print_message $BLUE "  自动安装: ./deployment/install-docker.sh"
        print_message $BLUE "  手动安装:"
        print_message $BLUE "    macOS: https://docs.docker.com/desktop/mac/install/"
        print_message $BLUE "    Windows: https://docs.docker.com/desktop/windows/install/"
        print_message $BLUE "    Linux: https://docs.docker.com/engine/install/"
        echo ""
        
        # 询问是否自动安装Docker
        read -p "是否自动安装Docker? (y/N): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            if [ -f "./deployment/install-docker.sh" ]; then
                print_message $YELLOW "🚀 开始自动安装Docker..."
                chmod +x ./deployment/install-docker.sh
                ./deployment/install-docker.sh
                if [ $? -eq 0 ]; then
                    print_message $GREEN "✅ Docker 安装完成，请重新运行环境检查"
                    exit 0
                else
                    print_message $RED "❌ Docker 安装失败，请手动安装"
                    exit 1
                fi
            else
                print_message $RED "❌ 自动安装脚本不存在"
                exit 1
            fi
        fi
    fi
        
        if [ "$docker_compose_ok" = false ]; then
            print_message $BLUE "Docker Compose 安装:"
            print_message $BLUE "  macOS: 通常随Docker Desktop一起安装"
            print_message $BLUE "  Linux: sudo apt-get install docker-compose-plugin"
            echo ""
        fi
    fi
    
    if [ "$disk_ok" = false ]; then
        print_message $YELLOW "💾 磁盘空间不足，请清理磁盘空间"
        echo ""
    fi
    
    exit 1
fi

print_message $BLUE "📚 详细文档: ./MIDDLEWARE_SETUP.md"
