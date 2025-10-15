import api from './index';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  realName: string;
  phone: string;
  mail: string;
}

export interface UserInfo {
  id: string;
  username: string;
  realName: string;
  phone: string;
  mail: string;
  avatar?: string;
  deletionTime: number;
  updateTime: string;
  createTime: string;
  delFlag: number;
}

export interface UpdateUserRequest {
  username: string;
  realName: string;
  phone: string;
  mail: string;
  avatar?: string;
}

export interface CheckLoginRequest {
  username: string;
  token: string;
}

export const authApi = {
  // 用户登录
  login: (data: LoginRequest): Promise<LoginResponse> => {
    return api.post('/api/short-link/admin/v1/user/login', data);
  },

  // 用户注册
  register: (data: RegisterRequest): Promise<void> => {
    return api.post('/api/short-link/admin/v1/user', data);
  },

  // 获取用户信息
  getUserInfo: (username: string): Promise<UserInfo> => {
    return api.get(`/api/short-link/admin/v1/user/${username}`);
  },

  // 获取用户真实信息（无脱敏）
  getActualUserInfo: (username: string): Promise<UserInfo> => {
    return api.get(`/api/short-link/admin/v1/actual/user/${username}`);
  },

  // 更新用户信息
  updateUser: (data: UpdateUserRequest): Promise<void> => {
    return api.put('/api/short-link/admin/v1/user', data);
  },

  // 检查用户是否登录
  checkLogin: (data: CheckLoginRequest): Promise<boolean> => {
    return api.get('/api/short-link/admin/v1/user/check-login', {
      params: data,
    });
  },

  // 检查用户名是否存在
  hasUsername: (username: string): Promise<boolean> => {
    return api.get('/api/short-link/admin/v1/user/has-username', {
      params: { username },
    });
  },

  // 上传头像
  uploadAvatar: (file: File, username: string): Promise<{ url: string }> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('username', username);
    return api.post('/api/short-link/admin/v1/avatar/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }).then(response => ({ url: response.data }));
  },
};
