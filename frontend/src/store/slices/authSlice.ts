import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { authApi } from '../../api/auth';

export interface User {
  id: string;
  username: string;
  realName: string;
  phone: string;
  mail: string;
  deletionTime: number;
  updateTime: string;
  createTime: string;
  delFlag: number;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
}

const initialState: AuthState = {
  user: null,
  token: localStorage.getItem('token'),
  isAuthenticated: !!localStorage.getItem('token'),
  loading: false,
  error: null,
};

export const login = createAsyncThunk(
  'auth/login',
  async (credentials: { username: string; password: string }) => {
    const response = await authApi.login(credentials);
    localStorage.setItem('token', response.token);
    localStorage.setItem('username', credentials.username);
    // 登录成功后获取用户信息
    const userInfo = await authApi.getUserInfo(credentials.username);
    return { token: response.token, user: userInfo };
  }
);

export const register = createAsyncThunk(
  'auth/register',
  async (userData: {
    username: string;
    password: string;
    realName: string;
    phone: string;
    mail: string;
  }) => {
    await authApi.register(userData);
  }
);

export const getUserInfo = createAsyncThunk(
  'auth/getUserInfo',
  async (username: string) => {
    const response = await authApi.getUserInfo(username);
    return response;
  }
);

export const getActualUserInfo = createAsyncThunk(
  'auth/getActualUserInfo',
  async (username: string) => {
    const response = await authApi.getActualUserInfo(username);
    return response;
  }
);

export const updateUser = createAsyncThunk(
  'auth/updateUser',
  async (userData: {
    username: string;
    realName: string;
    phone: string;
    mail: string;
  }) => {
    await authApi.updateUser(userData);
  }
);

export const checkLogin = createAsyncThunk(
  'auth/checkLogin',
  async (params: { username: string; token: string }) => {
    const response = await authApi.checkLogin(params);
    return response;
  }
);

export const logout = createAsyncThunk('auth/logout', async () => {
  localStorage.removeItem('token');
  localStorage.removeItem('username');
});

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setToken: (state, action: PayloadAction<string>) => {
      state.token = action.payload;
      state.isAuthenticated = true;
    },
  },
  extraReducers: (builder) => {
    builder
      // Login
      .addCase(login.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false;
        state.token = action.payload?.token || '';
        state.isAuthenticated = true;
        state.user = action.payload?.user || null;
        // 登录成功后不需要立即验证token，直接设置为已认证状态
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false;
        // 提供更详细的错误信息
        const errorMessage = action.error.message || '登录失败，请检查用户名和密码';
        state.error = errorMessage;
      })
      // Register
      .addCase(register.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(register.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(register.rejected, (state, action) => {
        state.loading = false;
        // 提供更详细的错误信息
        const errorMessage = action.error.message || '注册失败，请重试';
        state.error = errorMessage;
      })
      // Get User Info
      .addCase(getUserInfo.pending, (state) => {
        state.loading = true;
      })
      .addCase(getUserInfo.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
      })
      .addCase(getUserInfo.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '获取用户信息失败';
      })
      // Get Actual User Info
      .addCase(getActualUserInfo.pending, (state) => {
        state.loading = true;
      })
      .addCase(getActualUserInfo.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
      })
      .addCase(getActualUserInfo.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '获取用户信息失败';
      })
      // Update User
      .addCase(updateUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateUser.fulfilled, (state, action) => {
        state.loading = false;
        // 更新用户信息
        if (state.user) {
          state.user = {
            ...state.user,
            realName: action.meta.arg.realName,
            phone: action.meta.arg.phone,
            mail: action.meta.arg.mail,
          };
        }
      })
      .addCase(updateUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '更新用户信息失败';
      })
      // Check Login
      .addCase(checkLogin.fulfilled, (state, action) => {
        // 只有在应用初始化时才根据checkLogin结果更新认证状态
        // 避免在登录成功后立即清除认证状态
        if (!action.payload) {
          state.isAuthenticated = false;
          state.token = null;
          state.user = null;
          localStorage.removeItem('token');
          localStorage.removeItem('username');
        } else {
          state.isAuthenticated = true;
          // 注意：这里不设置user，因为getUserInfo会单独处理
        }
      })
      .addCase(checkLogin.rejected, (state) => {
        // 如果checkLogin请求失败，也清除认证状态
        state.isAuthenticated = false;
        state.token = null;
        state.user = null;
        localStorage.removeItem('token');
        localStorage.removeItem('username');
      })
      // Logout
      .addCase(logout.fulfilled, (state) => {
        state.user = null;
        state.token = null;
        state.isAuthenticated = false;
        localStorage.removeItem('token');
        localStorage.removeItem('username');
      });
  },
});

export const { clearError, setToken } = authSlice.actions;
export default authSlice.reducer;
