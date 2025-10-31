import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, Steps, message, Space, Divider } from 'antd';
import { UserOutlined, MailOutlined, LockOutlined, ArrowLeftOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { AppDispatch } from '../store';
import { setToken } from '../store/slices/authSlice';
import { authApi } from '../api/auth';

const { Title, Text } = Typography;

interface SendEmailForm {
  username: string;
  email: string;
}

interface ResetPasswordForm {
  username: string;
  email: string;
  newPassword: string;
  emailCode: string;
}

const ForgotPassword: React.FC = () => {
  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [userInfo, setUserInfo] = useState<{ username: string; email: string } | null>(null);
  const [resendCountdown, setResendCountdown] = useState(0);
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();

  const handleSendEmail = async (values: SendEmailForm) => {
    try {
      setLoading(true);
      await authApi.sendForgotPasswordEmailCode(values);
      message.success('验证码已发送到您的邮箱，请查收');
      setUserInfo(values);
      setCurrentStep(1);
      // 开始60秒倒计时
      startCountdown();
    } catch (error: any) {
      message.error(error?.message || '发送验证码失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (values: ResetPasswordForm) => {
    try {
      setLoading(true);
      await authApi.forgotPassword(values);
      message.success('密码重置成功，正在自动登录...');
      
      // 自动登录
      const loginData = {
        username: userInfo?.username || '',
        password: values.newPassword
      };
      
      try {
        const response = await authApi.login(loginData);
        localStorage.setItem('token', response.token);
        localStorage.setItem('username', loginData.username);
        // 更新 Redux store
        dispatch(setToken(response.token));
        // 获取用户信息
        const userInfo = await authApi.getUserInfo(loginData.username);
        localStorage.setItem('user', JSON.stringify(userInfo));
        message.success('登录成功');
        navigate('/dashboard');
      } catch (loginError: any) {
        message.warning('密码重置成功，但自动登录失败，请手动登录');
        navigate('/login');
      }
    } catch (error: any) {
      message.error(error?.message || '密码重置失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleBackToLogin = () => {
    navigate('/login');
  };

  const startCountdown = () => {
    setResendCountdown(60);
    const timer = setInterval(() => {
      setResendCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const handleResendCode = async () => {
    if (!userInfo || resendCountdown > 0) return;
    try {
      setLoading(true);
      await authApi.sendForgotPasswordEmailCode(userInfo);
      message.success('验证码已重新发送');
      // 重新开始倒计时
      startCountdown();
    } catch (error: any) {
      message.error('重新发送失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  const steps = [
    {
      title: '验证身份',
      description: '输入用户名和邮箱',
    },
    {
      title: '重置密码',
      description: '输入验证码和新密码',
    },
    {
      title: '完成',
      description: '密码重置成功',
    },
  ];

  return (
    <div className="modern-login-container">
      <div className="login-background">
        <div className="floating-shapes">
          <div className="shape shape-1"></div>
          <div className="shape shape-2"></div>
          <div className="shape shape-3"></div>
          <div className="shape shape-4"></div>
        </div>
      </div>
      
      <div className="login-content forgot-password-content">
        <div className="login-card forgot-password-card">
          <div className="login-header">
            <div className="logo-section">
              <Title level={2} className="login-title">
                找回密码
              </Title>
            </div>
            <p className="login-subtitle">
              通过邮箱验证重置您的密码
            </p>
          </div>
          
          <Steps
            current={currentStep}
            items={steps}
            className="forgot-password-steps"
            style={{ marginBottom: '24px' }}
          />
          
          {currentStep === 0 && (
            <Form
              name="sendEmail"
              onFinish={handleSendEmail}
              autoComplete="off"
              size="large"
            >
              <Form.Item
                name="username"
                rules={[{ required: true, message: '请输入用户名!' }]}
              >
                <Input
                  prefix={<UserOutlined />}
                  placeholder="用户名"
                />
              </Form.Item>

              <Form.Item
                name="email"
                rules={[
                  { required: true, message: '请输入邮箱!' },
                  { type: 'email', message: '请输入正确的邮箱!' },
                ]}
              >
                <Input
                  prefix={<MailOutlined />}
                  placeholder="邮箱"
                />
              </Form.Item>

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={loading}
                  style={{ width: '100%' }}
                >
                  发送验证码
                </Button>
              </Form.Item>
            </Form>
          )}

          {currentStep === 1 && (
            <Form
              name="resetPassword"
              onFinish={handleResetPassword}
              autoComplete="off"
              size="large"
              initialValues={userInfo || {}}
            >
              {/* 隐藏用户名和邮箱字段，但保留在表单数据中 */}
              <Form.Item name="username" style={{ display: 'none' }}>
                <Input />
              </Form.Item>
              <Form.Item name="email" style={{ display: 'none' }}>
                <Input />
              </Form.Item>

              <Form.Item
                name="emailCode"
                rules={[{ required: true, message: '请输入邮箱验证码!' }]}
                style={{ marginBottom: '16px' }}
              >
                <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                  <Input
                    placeholder="邮箱验证码"
                    style={{ flex: 1 }}
                  />
                  <Button
                    type="link"
                    onClick={handleResendCode}
                    loading={loading}
                    disabled={resendCountdown > 0}
                    style={{ padding: '0', minWidth: '80px' }}
                  >
                    {resendCountdown > 0 ? `${resendCountdown}秒后重发` : '重新发送'}
                  </Button>
                </div>
              </Form.Item>

              <Form.Item
                name="newPassword"
                rules={[
                  { required: true, message: '请输入新密码!' },
                  { min: 6, message: '密码至少6个字符!' },
                ]}
              >
                <Input.Password
                  prefix={<LockOutlined />}
                  placeholder="新密码"
                />
              </Form.Item>

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={loading}
                  style={{ width: '100%' }}
                >
                  重置密码
                </Button>
              </Form.Item>
            </Form>
          )}

          {currentStep === 2 && (
            <div style={{ textAlign: 'center', padding: '40px 0' }}>
              <CheckCircleOutlined 
                style={{ 
                  fontSize: '64px', 
                  color: '#52c41a', 
                  marginBottom: '24px' 
                }} 
              />
              <Title level={3} style={{ color: '#52c41a', marginBottom: '16px' }}>
                密码重置成功！
              </Title>
              <Text type="secondary" style={{ display: 'block', marginBottom: '32px' }}>
                您的密码已成功重置，请使用新密码登录
              </Text>
              <Button
                type="primary"
                size="large"
                onClick={handleBackToLogin}
                style={{ width: '200px' }}
              >
                返回登录
              </Button>
            </div>
          )}
          
          <Divider className="login-divider" />
          
          <div className="login-footer">
            <Space direction="vertical" size="small" style={{ width: '100%', textAlign: 'center' }}>
              <Button
                type="link"
                icon={<ArrowLeftOutlined />}
                onClick={handleBackToLogin}
                style={{ padding: '0' }}
              >
                返回登录页面
              </Button>
            </Space>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
