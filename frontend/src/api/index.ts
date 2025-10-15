import axios from 'axios';
import { requestDeduplication } from '../utils/requestDeduplication';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8000';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 登录接口不需要添加token
    const isLoginRequest = config.url?.includes('/user/login');
    const isRegisterRequest = config.url?.includes('/user') && config.method === 'post' && !config.url?.includes('/user/');
    const isCheckUsernameRequest = config.url?.includes('/user/has-username');
    
    if (isLoginRequest || isRegisterRequest || isCheckUsernameRequest) {
      return config;
    }
    
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    if (token && username) {
      config.headers.token = token;
      config.headers.username = username;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 检查响应结构，如果是统一响应格式，则返回data字段
    if (response.data && typeof response.data === 'object' && 'data' in response.data) {
      // 检查是否成功
      if (response.data.code === '0' || response.data.success === true) {
        return response.data.data;
      } else {
        // 如果是错误响应，抛出错误
        const error = new Error(response.data.message || '请求失败');
        (error as any).response = response;
        throw error;
      }
    }
    return response.data;
  },
  (error) => {
    // 处理401未授权错误
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      window.location.href = '/login';
    }
    
    // 提取更详细的错误信息
    let errorMessage = '请求失败';
    
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    // 根据状态码提供更友好的错误信息
    switch (error.response?.status) {
      case 400:
        errorMessage = '请求参数错误';
        break;
      case 401:
        errorMessage = '用户名或密码错误';
        break;
      case 403:
        errorMessage = '没有权限访问';
        break;
      case 404:
        errorMessage = '请求的资源不存在';
        break;
      case 500:
        errorMessage = '服务器内部错误';
        break;
      case 502:
        errorMessage = '网关错误';
        break;
      case 503:
        errorMessage = '服务暂时不可用';
        break;
      default:
        if (error.code === 'ECONNABORTED') {
          errorMessage = '请求超时，请检查网络连接';
        } else if (error.code === 'NETWORK_ERROR') {
          errorMessage = '网络连接失败，请检查网络';
        }
    }
    
    // 创建新的错误对象，包含更详细的信息
    const enhancedError = new Error(errorMessage);
    enhancedError.name = error.name;
    enhancedError.stack = error.stack;
    
    return Promise.reject(enhancedError);
  }
);

export default api;
