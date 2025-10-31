import React, { useState } from 'react';
import { message, Input, Button, Typography } from 'antd';
import { MailOutlined, SafetyOutlined } from '@ant-design/icons';
import { authApi } from '../api/auth';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';

const { Title } = Typography;

const ChangeEmail: React.FC = () => {
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const navigate = useNavigate();
  const user = useSelector((state: RootState) => state.auth.user);
  const dispatch = useDispatch();

  // 发送验证码
  const handleSendCode = async () => {
    if (!email || !/^\S+@\S+\.\S+$/.test(email)) {
      message.error('请输入正确的新邮箱');
      return;
    }
    setLoading(true);
    try {
      await authApi.sendChangeEmailCode({ username: user.username, email });
      message.success('验证码已发送到新邮箱');
      setCountdown(60);
      const timer = setInterval(() => {
        setCountdown(prev => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (err: any) {
      if (err.message && err.message.includes('已被注册')) {
        message.error('该邮箱已被注册');
      } else {
        message.error(err.message || '验证码发送失败');
      }
    }
    setLoading(false);
  };

  // 提交更改
  const handleSubmit = async () => {
    if (!email || !/^\S+@\S+\.\S+$/.test(email)) {
      message.error('请输入正确的新邮箱');
      return;
    }
    if (!code) {
      message.error('请输入验证码');
      return;
    }
    setLoading(true);
    try {
      await authApi.changeEmail({
        username: user.username,
        newEmail: email,
        emailCode: code,
      });
      // 邮箱修改成功后刷新用户信息
      if (user && user.username && typeof dispatch === 'function') {
        // @ts-ignore
        await dispatch(require('../store/slices/authSlice').getActualUserInfo(user.username));
      }
      message.success('邮箱修改成功');
      navigate(-1); // 返回上一页
    } catch (err: any) {
      if (err.message && err.message.includes('已被注册')) {
        message.error('该邮箱已被注册');
      } else {
        message.error(err.message || '邮箱修改失败，请检查验证码');
      }
    }
    setLoading(false);
  };

  return (
    <div style={{ maxWidth: 420, margin: '40px auto', padding: 24, background: '#fff', borderRadius: 8 }}>
      <Title level={3} style={{ textAlign: 'center' }}>修改邮箱</Title>
      <Input
        prefix={<MailOutlined />}
        placeholder="新邮箱"
        value={email}
        onChange={e => setEmail(e.target.value)}
        style={{ marginBottom: 16 }}
        disabled={loading}
      />
      <div style={{ display: 'flex', marginBottom: 16 }}>
        <Input
          prefix={<SafetyOutlined />}
          placeholder="邮箱验证码"
          value={code}
          onChange={e => setCode(e.target.value)}
          disabled={loading}
        />
        <Button
          type="primary"
          onClick={handleSendCode}
          disabled={loading || countdown > 0}
          style={{ marginLeft: 8, width: 120 }}
        >
          {countdown > 0 ? `重新获取(${countdown}s)` : '获取验证码'}
        </Button>
      </div>
      <Button type="primary" block loading={loading} onClick={handleSubmit}>
        完成修改
      </Button>
      <Button block style={{ marginTop: 8 }} onClick={() => navigate(-1)}>
        取消
      </Button>
    </div>
  );
};

export default ChangeEmail;
