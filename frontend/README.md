# 短链接系统前端

这是一个基于React和Ant Design的短链接系统前端应用，提供了完整的短链接管理功能。

## 功能特性

### 🔐 用户认证
- 用户注册和登录
- 个人资料管理
- 安全的身份验证

### 📁 分组管理
- 创建和管理短链接分组
- 分组排序功能
- 分组统计信息

### 🔗 短链接管理
- 创建单个短链接
- 批量创建短链接
- 短链接编辑和更新
- 短链接删除和恢复
- 自动获取网页标题

### 📊 统计分析
- 访问量统计
- 独立访客统计
- 访问记录查看
- 数据可视化图表
- 时间范围筛选

### 🗑️ 回收站
- 删除的短链接管理
- 恢复已删除的短链接
- 彻底删除功能

## 技术栈

- **React 18** - 前端框架
- **TypeScript** - 类型安全
- **Ant Design 5** - UI组件库
- **Redux Toolkit** - 状态管理
- **React Router** - 路由管理
- **Axios** - HTTP客户端
- **Recharts** - 数据可视化
- **Day.js** - 日期处理

## 项目结构

```
src/
├── api/                    # API接口层
│   ├── auth.ts            # 用户认证API
│   ├── group.ts           # 分组管理API
│   ├── shortLink.ts       # 短链接API
│   ├── stats.ts           # 统计API
│   ├── recycleBin.ts      # 回收站API
│   └── index.ts           # API配置
├── components/            # 公共组件
│   └── Layout.tsx         # 布局组件
├── pages/                 # 页面组件
│   ├── Login.tsx          # 登录页面
│   ├── Dashboard.tsx      # 仪表盘
│   ├── GroupManagement.tsx # 分组管理
│   ├── ShortLinkManagement.tsx # 短链接管理
│   ├── ShortLinkStats.tsx # 统计分析
│   ├── RecycleBin.tsx     # 回收站
│   └── UserProfile.tsx    # 用户资料
├── store/                 # Redux状态管理
│   ├── index.ts           # Store配置
│   └── slices/            # Redux切片
│       ├── authSlice.ts   # 认证状态
│       ├── groupSlice.ts  # 分组状态
│       ├── shortLinkSlice.ts # 短链接状态
│       ├── statsSlice.ts  # 统计状态
│       └── recycleBinSlice.ts # 回收站状态
├── App.tsx                # 主应用组件
├── index.tsx              # 应用入口
└── index.css              # 全局样式
```

## 安装和运行

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖
```bash
npm install
```

### 环境配置
创建 `.env` 文件并配置API地址：
```env
REACT_APP_API_BASE_URL=http://localhost:8002
GENERATE_SOURCEMAP=false
```

### 启动开发服务器
```bash
npm start
```

应用将在 http://localhost:3000 启动

### 构建生产版本
```bash
npm run build
```

## API接口

### 用户认证
- `POST /api/short-link/admin/v1/user/login` - 用户登录
- `POST /api/short-link/admin/v1/user` - 用户注册
- `GET /api/short-link/admin/v1/user/{username}` - 获取用户信息
- `PUT /api/short-link/admin/v1/user` - 更新用户信息

### 分组管理
- `GET /api/short-link/admin/v1/group` - 获取分组列表
- `POST /api/short-link/admin/v1/group` - 创建分组
- `PUT /api/short-link/admin/v1/group` - 更新分组
- `DELETE /api/short-link/admin/v1/group` - 删除分组
- `POST /api/short-link/admin/v1/group/sort` - 排序分组

### 短链接管理
- `GET /api/short-link/admin/v1/page` - 获取短链接列表
- `POST /api/short-link/admin/v1/create` - 创建短链接
- `POST /api/short-link/admin/v1/create/batch` - 批量创建短链接
- `POST /api/short-link/admin/v1/update` - 更新短链接
- `GET /api/short-link/admin/v1/title` - 获取URL标题

### 统计分析
- `GET /api/short-link/admin/v1/stats` - 获取单个短链接统计
- `GET /api/short-link/admin/v1/stats/group` - 获取分组统计
- `GET /api/short-link/admin/v1/stats/access-record` - 获取访问记录

### 回收站
- `GET /api/short-link/admin/v1/recycle-bin/page` - 获取回收站列表
- `POST /api/short-link/admin/v1/recycle-bin/save` - 移至回收站
- `POST /api/short-link/admin/v1/recycle-bin/recover` - 恢复短链接
- `POST /api/short-link/admin/v1/recycle-bin/remove` - 彻底删除

## 功能说明

### 仪表盘
- 显示系统概览信息
- 分组数量、短链接数量统计
- 访问量统计
- 最近创建的短链接和分组

### 分组管理
- 创建、编辑、删除分组
- 分组排序功能
- 分组状态管理

### 短链接管理
- 单个创建：支持自定义域名、有效期、描述
- 批量创建：支持Excel导入
- 自动获取网页标题
- 短链接复制功能
- 访问量统计

### 统计分析
- 实时访问数据
- 访问趋势图表
- 设备分布统计
- 访问记录详情
- 时间范围筛选

### 回收站
- 查看已删除的短链接
- 恢复误删的短链接
- 彻底删除功能
- 按分组筛选

## 开发说明

### 状态管理
使用Redux Toolkit进行状态管理，每个功能模块都有对应的slice：
- `authSlice`: 用户认证状态
- `groupSlice`: 分组管理状态
- `shortLinkSlice`: 短链接管理状态
- `statsSlice`: 统计分析状态
- `recycleBinSlice`: 回收站状态

### API调用
所有API调用都通过Redux的异步thunk进行，确保状态的一致性和错误处理。

### 组件设计
- 使用Ant Design组件库
- 响应式设计，支持移动端
- 统一的样式规范
- 良好的用户体验

### 错误处理
- 统一的错误提示
- 网络请求失败处理
- 表单验证错误处理

## 部署

### 生产环境构建
```bash
npm run build
```

### 环境变量
生产环境需要配置正确的API地址：
```env
REACT_APP_API_BASE_URL=https://your-api-domain.com
```

### 静态文件部署
构建后的文件在 `build` 目录，可以部署到任何静态文件服务器。

## 许可证

MIT License
