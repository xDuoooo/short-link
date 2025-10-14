#!/bin/bash

# 短链接系统前端启动脚本

echo "🚀 启动短链接系统前端..."

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo "❌ Node.js 未安装，请先安装 Node.js 16+ 版本"
    exit 1
fi

# 检查npm是否安装
if ! command -v npm &> /dev/null; then
    echo "❌ npm 未安装，请先安装 npm"
    exit 1
fi

# 检查Node.js版本
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 16 ]; then
    echo "❌ Node.js 版本过低，需要 16+ 版本，当前版本: $(node -v)"
    exit 1
fi

echo "✅ Node.js 版本检查通过: $(node -v)"

# 检查是否存在node_modules
if [ ! -d "node_modules" ]; then
    echo "📦 安装依赖包..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ 依赖安装失败"
        exit 1
    fi
    echo "✅ 依赖安装完成"
else
    echo "✅ 依赖已存在"
fi

# 检查是否存在.env文件
if [ ! -f ".env" ]; then
    echo "📝 创建环境配置文件..."
    cat > .env << EOF
REACT_APP_API_BASE_URL=http://localhost:8000
GENERATE_SOURCEMAP=false
EOF
    echo "✅ 环境配置文件已创建"
else
    echo "✅ 环境配置文件已存在"
fi

echo "🎯 启动开发服务器..."
echo "📱 前端地址: http://localhost:3000"
echo ""
echo "按 Ctrl+C 停止服务器"
echo ""

# 启动开发服务器
npm start
