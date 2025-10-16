# 短链接系统中间件部署

这个目录包含了短链接系统所需的所有中间件服务的一键部署配置。

## 包含的中间件服务

- **MySQL 8.0** - 数据库服务
- **Redis 7** - 缓存服务  
- **Zookeeper** - Kafka依赖
- **Kafka** - 消息队列服务
- **Kafka UI** - Kafka管理界面
- **MinIO** - 对象存储服务
- **Nacos** - 注册中心

## 快速开始

### 1. 一键启动所有中间件

```bash
./middleware-start.sh
```

### 2. 手动启动

```bash
# 启动所有中间件服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f
```

### 3. 停止服务

```bash
docker-compose down
```

## 服务访问地址

启动成功后，可以通过以下地址访问各个服务：

- **Kafka UI**: http://localhost:8080
- **MinIO控制台**: http://localhost:9001 (账号: minioadmin / minioadmin123)
- **Nacos控制台**: http://localhost:8848/nacos (账号: nacos / nacos)

## 环境配置

脚本会自动创建 `.env` 文件，包含以下默认配置：

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
```

## 测试服务

运行服务测试脚本：

```bash
./test-services.sh
```

## 注意事项

1. 确保已安装 Docker 和 Docker Compose
2. 确保端口 3306, 6379, 2181, 9092, 8080, 9000, 9001, 8848 未被占用
3. 中间件启动后，需要手动启动后端应用和前端服务
