# 短链接系统 - Windows 部署指南

## 🪟 Windows 系统一键部署

本指南专门为 Windows 用户提供简单易用的一键部署方案。

### 📋 系统要求

- **操作系统**: Windows 10/11 (64位)
- **内存**: 至少 8GB RAM
- **磁盘空间**: 至少 10GB 可用空间
- **网络**: 需要互联网连接下载Docker镜像

### 🚀 快速开始

#### 方法一：一键部署（推荐）

1. **下载项目**
   ```cmd
   git clone <your-repo-url>
   cd short-link
   ```

2. **运行一键部署脚本**
   ```cmd
   deployment\install-docker.bat
   ```
   
   脚本会自动：
   - 检测并安装 Docker Desktop
   - 检查系统环境
   - 启动所有中间件服务

3. **验证部署**
   ```cmd
   deployment\test-services.bat
   ```

#### 方法二：分步部署

1. **检查环境**
   ```cmd
   deployment\check-environment.bat
   ```

2. **安装Docker（如需要）**
   ```cmd
   deployment\install-docker.bat
   ```

3. **启动中间件服务**
   ```cmd
   deployment\middleware-start.bat
   ```

4. **测试服务**
   ```cmd
   deployment\test-services.bat
   ```

### 📁 脚本说明

| 脚本文件 | 功能描述 |
|---------|---------|
| `install-docker.bat` | 自动安装Docker Desktop |
| `check-environment.bat` | 检查系统环境和依赖 |
| `middleware-start.bat` | 启动所有中间件服务 |
| `test-services.bat` | 测试服务连接状态 |

### 🌐 服务访问地址

启动成功后，可以通过以下地址访问服务：

| 服务 | 地址 | 账号密码 |
|------|------|---------|
| **Nacos控制台** | http://localhost:8848/nacos | nacos / nacos |
| **Kafka UI** | http://localhost:8080 | - |
| **MinIO控制台** | http://localhost:9001 | minioadmin / minioadmin123 |

### 🔧 常见问题

#### 1. Docker Desktop 启动失败

**问题**: Docker Desktop 无法启动或显示错误

**解决方案**:
- 确保已启用 Hyper-V 或 WSL2
- 重启计算机
- 以管理员身份运行 Docker Desktop
- 检查 Windows 版本是否支持 Docker Desktop

#### 2. 端口被占用

**问题**: 启动时提示端口被占用

**解决方案**:
```cmd
# 查看端口占用
netstat -ano | findstr :3306
netstat -ano | findstr :6379
netstat -ano | findstr :2181
netstat -ano | findstr :9092
netstat -ano | findstr :8080
netstat -ano | findstr :9000
netstat -ano | findstr :9001
netstat -ano | findstr :8848

# 结束占用进程（替换PID为实际进程ID）
taskkill /PID <进程ID> /F
```

#### 3. 权限不足

**问题**: 脚本执行时提示权限不足

**解决方案**:
- 以管理员身份运行命令提示符
- 或者右键点击脚本文件，选择"以管理员身份运行"

#### 4. 网络连接问题

**问题**: 无法下载Docker镜像或连接服务

**解决方案**:
- 检查防火墙设置
- 确保网络连接正常
- 尝试使用VPN或更换网络环境

### 📝 手动启动应用服务

中间件服务启动后，需要手动启动应用服务：

#### 启动后端服务

```cmd
# 启动管理后台
cd admin
mvn spring-boot:run

# 新开命令窗口，启动项目服务
cd project
mvn spring-boot:run

# 新开命令窗口，启动网关服务
cd gateway
mvn spring-boot:run
```

#### 启动前端服务

```cmd
# 新开命令窗口，启动前端
cd frontend
npm install
npm start
```

### 🛠️ 开发环境配置

#### 1. 数据库配置

默认数据库配置：
- **MySQL**: localhost:3306
- **用户名**: shortlink
- **密码**: shortlink123
- **数据库**: shortlink

#### 2. Redis配置

- **地址**: localhost:6379
- **密码**: redis123

#### 3. 环境变量

项目会自动创建 `.env` 文件，包含以下配置：

```env
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
```

### 🔄 服务管理

#### 停止所有服务

```cmd
cd deployment
docker-compose -f docker-compose.yml down
```

#### 重启服务

```cmd
cd deployment
docker-compose -f docker-compose.yml restart
```

#### 查看服务状态

```cmd
cd deployment
docker-compose -f docker-compose.yml ps
```

#### 查看服务日志

```cmd
# 查看所有服务日志
docker-compose -f docker-compose.yml logs

# 查看特定服务日志
docker-compose -f docker-compose.yml logs mysql
docker-compose -f docker-compose.yml logs redis
docker-compose -f docker-compose.yml logs kafka
```

### 📞 技术支持

如果遇到问题，请：

1. 查看服务日志排查问题
2. 检查系统环境是否满足要求
3. 参考常见问题解决方案
4. 提交 Issue 或联系技术支持

### 🎯 下一步

部署完成后，你可以：

1. 访问 Nacos 控制台配置服务
2. 启动前端和后端应用
3. 开始开发和测试
4. 配置生产环境

---

**注意**: 本部署方案适用于开发和测试环境，生产环境部署请参考生产部署文档。
