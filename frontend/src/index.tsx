import React from 'react';
import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { store } from './store';
import App from './App';
import './index.css';

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

// 在开发环境中禁用StrictMode以避免重复请求
const isDevelopment = process.env.NODE_ENV === 'development';

root.render(
  isDevelopment ? (
    <Provider store={store}>
      <BrowserRouter>
        <ConfigProvider locale={zhCN}>
          <App />
        </ConfigProvider>
      </BrowserRouter>
    </Provider>
  ) : (
    <React.StrictMode>
      <Provider store={store}>
        <BrowserRouter>
          <ConfigProvider locale={zhCN}>
            <App />
          </ConfigProvider>
        </BrowserRouter>
      </Provider>
    </React.StrictMode>
  )
);
