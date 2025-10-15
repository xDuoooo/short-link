import React, { useEffect, useRef } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from './store';
import { checkLogin, getUserInfo } from './store/slices/authSlice';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import GroupManagement from './pages/GroupManagement';
import GroupShortLinks from './pages/GroupShortLinks';
import ShortLinkStats from './pages/ShortLinkStats';
import RecycleBin from './pages/RecycleBin';
import UserProfile from './pages/UserProfile';

const App: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);

  useEffect(() => {
    // 只在应用初始化时检查登录状态，避免登录成功后立即验证
    const savedToken = localStorage.getItem('token');
    const savedUsername = localStorage.getItem('username');
    console.log('App初始化检查:', { savedToken: !!savedToken, savedUsername, isAuthenticated });
    
    if (savedToken && savedUsername) {
      if (!isAuthenticated) {
        // 如果未认证，先验证登录状态
        console.log('开始验证登录状态...');
        dispatch(checkLogin({ username: savedUsername, token: savedToken }))
          .then((result) => {
            console.log('checkLogin结果:', result.payload);
            // 如果验证成功，获取用户信息
            if (result.payload) {
              console.log('开始获取用户信息...');
              return dispatch(getUserInfo(savedUsername));
            }
          })
          .then((userResult) => {
            if (userResult) {
              console.log('getUserInfo结果:', userResult.payload);
            }
          })
          .catch((error) => {
            console.error('checkLogin或getUserInfo失败:', error);
          });
      } else {
        // 如果已经认证但没有用户信息，直接获取用户信息
        if (!user) {
          console.log('已认证但缺少用户信息，开始获取用户信息...');
          dispatch(getUserInfo(savedUsername))
            .then((userResult) => {
              console.log('getUserInfo结果:', userResult.payload);
            })
            .catch((error) => {
              console.error('getUserInfo失败:', error);
            });
        }
      }
    }
  }, [dispatch, isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/groups" element={<GroupManagement />} />
        <Route path="/groups/:gid/shortlinks" element={<GroupShortLinks />} />
        <Route path="/stats" element={<ShortLinkStats />} />
        <Route path="/recycle" element={<RecycleBin />} />
        <Route path="/profile" element={<UserProfile />} />
        <Route path="/login" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </Layout>
  );
};

export default App;
