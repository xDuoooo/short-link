#!/bin/bash

# Docker自动安装脚本
# 支持 macOS 和 Linux 系统

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

# 检测操作系统
detect_os() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "macos"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if [ -f /etc/os-release ]; then
            . /etc/os-release
            echo "$ID"
        else
            echo "linux"
        fi
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "win32" ]]; then
        echo "windows"
    else
        echo "unknown"
    fi
}

# 检查是否为root用户
check_root() {
    if [[ $EUID -eq 0 ]]; then
        print_message $YELLOW "⚠️  检测到root用户，建议使用普通用户安装Docker"
        read -p "是否继续? (y/N): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# 安装Docker Desktop (macOS)
install_docker_macos() {
    print_message $YELLOW "🍎 检测到macOS系统，准备安装Docker Desktop..."
    
    # 检查是否已安装Homebrew
    if ! command -v brew &> /dev/null; then
        print_message $YELLOW "📦 安装Homebrew..."
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    fi
    
    # 检查是否已安装Docker Desktop
    if [ -d "/Applications/Docker.app" ]; then
        print_message $GREEN "✅ Docker Desktop 已安装"
        return 0
    fi
    
    print_message $YELLOW "📦 通过Homebrew安装Docker Desktop..."
    brew install --cask docker
    
    print_message $GREEN "✅ Docker Desktop 安装完成"
    print_message $YELLOW "🚀 请手动启动Docker Desktop应用程序"
    print_message $YELLOW "💡 启动后，Docker图标会出现在状态栏中"
    
    # 等待用户启动Docker Desktop
    print_message $YELLOW "⏳ 等待Docker Desktop启动..."
    print_message $BLUE "请在Docker Desktop启动后按任意键继续..."
    read -n 1 -s
    
    # 检查Docker是否可用
    local retries=0
    while [ $retries -lt 30 ]; do
        if docker info &> /dev/null; then
            print_message $GREEN "✅ Docker 服务已启动"
            return 0
        fi
        print_message $YELLOW "⏳ 等待Docker服务启动... ($((retries+1))/30)"
        sleep 2
        ((retries++))
    done
    
    print_message $RED "❌ Docker服务启动超时，请手动检查Docker Desktop状态"
    return 1
}

# 安装Docker (Ubuntu/Debian)
install_docker_ubuntu() {
    print_message $YELLOW "🐧 检测到Ubuntu/Debian系统，准备安装Docker..."
    
    # 更新包索引
    print_message $YELLOW "📦 更新包索引..."
    sudo apt-get update
    
    # 安装必要的包
    print_message $YELLOW "📦 安装必要的包..."
    sudo apt-get install -y \
        ca-certificates \
        curl \
        gnupg \
        lsb-release
    
    # 添加Docker官方GPG密钥
    print_message $YELLOW "🔑 添加Docker官方GPG密钥..."
    sudo mkdir -p /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    
    # 设置稳定版仓库
    print_message $YELLOW "📋 设置Docker仓库..."
    echo \
        "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
        $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    # 更新包索引
    print_message $YELLOW "📦 更新包索引..."
    sudo apt-get update
    
    # 安装Docker Engine
    print_message $YELLOW "📦 安装Docker Engine..."
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    
    # 启动Docker服务
    print_message $YELLOW "🚀 启动Docker服务..."
    sudo systemctl start docker
    sudo systemctl enable docker
    
    # 将当前用户添加到docker组
    print_message $YELLOW "👤 将用户添加到docker组..."
    sudo usermod -aG docker $USER
    
    print_message $GREEN "✅ Docker 安装完成"
    print_message $YELLOW "⚠️  请重新登录或执行 'newgrp docker' 以使组权限生效"
    
    # 测试Docker安装
    print_message $YELLOW "🧪 测试Docker安装..."
    if sudo docker run hello-world &> /dev/null; then
        print_message $GREEN "✅ Docker 安装测试成功"
    else
        print_message $RED "❌ Docker 安装测试失败"
        return 1
    fi
}

# 安装Docker (CentOS/RHEL/Fedora)
install_docker_centos() {
    print_message $YELLOW "🐧 检测到CentOS/RHEL/Fedora系统，准备安装Docker..."
    
    # 安装必要的包
    print_message $YELLOW "📦 安装必要的包..."
    sudo yum install -y yum-utils
    
    # 添加Docker仓库
    print_message $YELLOW "📋 添加Docker仓库..."
    sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
    
    # 安装Docker Engine
    print_message $YELLOW "📦 安装Docker Engine..."
    sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    
    # 启动Docker服务
    print_message $YELLOW "🚀 启动Docker服务..."
    sudo systemctl start docker
    sudo systemctl enable docker
    
    # 将当前用户添加到docker组
    print_message $YELLOW "👤 将用户添加到docker组..."
    sudo usermod -aG docker $USER
    
    print_message $GREEN "✅ Docker 安装完成"
    print_message $YELLOW "⚠️  请重新登录或执行 'newgrp docker' 以使组权限生效"
    
    # 测试Docker安装
    print_message $YELLOW "🧪 测试Docker安装..."
    if sudo docker run hello-world &> /dev/null; then
        print_message $GREEN "✅ Docker 安装测试成功"
    else
        print_message $RED "❌ Docker 安装测试失败"
        return 1
    fi
}

# 安装Docker Desktop (Windows)
install_docker_windows() {
    print_message $YELLOW "🪟 检测到Windows系统，准备安装Docker Desktop..."
    
    # 检查是否已安装Docker Desktop
    if [ -d "/c/Program Files/Docker/Docker/Docker Desktop.exe" ] || [ -d "/c/Program Files (x86)/Docker/Docker/Docker Desktop.exe" ]; then
        print_message $GREEN "✅ Docker Desktop 已安装"
        return 0
    fi
    
    print_message $YELLOW "📦 下载Docker Desktop for Windows..."
    
    # 创建临时目录
    local temp_dir="/tmp/docker-install"
    mkdir -p "$temp_dir"
    
    # 下载Docker Desktop
    local download_url="https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe"
    local installer_path="$temp_dir/DockerDesktopInstaller.exe"
    
    print_message $YELLOW "⬇️  正在下载Docker Desktop..."
    if command -v curl &> /dev/null; then
        curl -L -o "$installer_path" "$download_url"
    elif command -v wget &> /dev/null; then
        wget -O "$installer_path" "$download_url"
    else
        print_message $RED "❌ 需要curl或wget来下载Docker Desktop"
        print_message $YELLOW "💡 请手动下载: https://docs.docker.com/desktop/windows/install/"
        return 1
    fi
    
    print_message $GREEN "✅ Docker Desktop 下载完成"
    print_message $YELLOW "🚀 请手动运行安装程序: $installer_path"
    print_message $YELLOW "💡 安装完成后，请启动Docker Desktop"
    
    # 等待用户安装
    print_message $BLUE "请在Docker Desktop安装并启动后按任意键继续..."
    read -n 1 -s
    
    # 检查Docker是否可用
    local retries=0
    while [ $retries -lt 30 ]; do
        if docker info &> /dev/null; then
            print_message $GREEN "✅ Docker 服务已启动"
            return 0
        fi
        print_message $YELLOW "⏳ 等待Docker服务启动... ($((retries+1))/30)"
        sleep 2
        ((retries++))
    done
    
    print_message $RED "❌ Docker服务启动超时，请手动检查Docker Desktop状态"
    return 1
}

# 安装Docker Compose (如果未安装)
install_docker_compose() {
    print_message $YELLOW "🔍 检查Docker Compose..."
    
    if command -v docker-compose &> /dev/null; then
        print_message $GREEN "✅ Docker Compose 已安装"
        return 0
    fi
    
    if docker compose version &> /dev/null; then
        print_message $GREEN "✅ Docker Compose (插件) 已安装"
        return 0
    fi
    
    local os=$(detect_os)
    
    if [[ "$os" == "macos" ]] || [[ "$os" == "windows" ]]; then
        print_message $YELLOW "📦 在$os上，Docker Compose通常随Docker Desktop一起安装"
        print_message $YELLOW "💡 如果仍有问题，请重新启动Docker Desktop"
    else
        print_message $YELLOW "📦 安装Docker Compose插件..."
        sudo apt-get install -y docker-compose-plugin
    fi
}

# 主安装函数
main() {
    print_title "Docker 自动安装脚本"
    
    # 检查是否已安装Docker
    if command -v docker &> /dev/null; then
        print_message $GREEN "✅ Docker 已安装"
        if docker info &> /dev/null; then
            print_message $GREEN "✅ Docker 服务运行正常"
            install_docker_compose
            return 0
        else
            print_message $YELLOW "⚠️  Docker 已安装但服务未运行"
        fi
    fi
    
    # 检查root权限
    check_root
    
    # 检测操作系统
    local os=$(detect_os)
    print_message $BLUE "🖥️  检测到操作系统: $os"
    
    # 根据操作系统安装Docker
    case "$os" in
        "macos")
            install_docker_macos
            ;;
        "ubuntu"|"debian")
            install_docker_ubuntu
            ;;
        "centos"|"rhel"|"fedora")
            install_docker_centos
            ;;
        "windows")
            install_docker_windows
            ;;
        *)
            print_message $RED "❌ 不支持的操作系统: $os"
            print_message $YELLOW "💡 请手动安装Docker:"
            print_message $BLUE "  macOS: https://docs.docker.com/desktop/mac/install/"
            print_message $BLUE "  Windows: https://docs.docker.com/desktop/windows/install/"
            print_message $BLUE "  Linux: https://docs.docker.com/engine/install/"
            exit 1
            ;;
    esac
    
    # 安装Docker Compose
    install_docker_compose
    
    print_title "安装完成"
    print_message $GREEN "🎉 Docker 安装完成！"
    print_message $BLUE "📚 接下来可以运行:"
    print_message $BLUE "  ./deployment/check-environment.sh  # 检查环境"
    print_message $BLUE "  ./deployment/middleware-start.sh   # 启动中间件"
}

# 运行主函数
main "$@"
