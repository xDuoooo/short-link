import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Card, Typography, Tabs, message, Space, Divider, Modal } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, PhoneOutlined, LinkOutlined, EyeInvisibleOutlined, EyeTwoTone } from '@ant-design/icons';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { AppDispatch, RootState } from '../store';
import { login, register, clearError } from '../store/slices/authSlice';

const { Title } = Typography;

interface LoginForm {
  username: string;
  password: string;
}

interface RegisterForm {
  username: string;
  password: string;
  realName: string;
  phone: string;
  mail: string;
}

const Login: React.FC = () => {
  const [activeTab, setActiveTab] = useState('login');
  const [termsModalVisible, setTermsModalVisible] = useState(false);
  const [privacyModalVisible, setPrivacyModalVisible] = useState(false);
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state: RootState) => state.auth);

  // 切换标签页时清除错误
  useEffect(() => {
    if (error) {
      dispatch(clearError());
    }
  }, [activeTab, dispatch, error]);

  const handleLogin = async (values: LoginForm) => {
    try {
      // 清除之前的错误
      dispatch(clearError());
      await dispatch(login(values)).unwrap();
      message.success('登录成功！');
      navigate('/dashboard');
    } catch (error: any) {
      // 使用更详细的错误信息
      const errorMessage = error?.message || '登录失败，请检查用户名和密码';
      message.error(errorMessage);
    }
  };

  const handleRegister = async (values: RegisterForm) => {
    try {
      // 清除之前的错误
      dispatch(clearError());
      await dispatch(register(values)).unwrap();
      message.success('注册成功！请登录');
      setActiveTab('login');
    } catch (error: any) {
      // 使用更详细的错误信息
      const errorMessage = error?.message || '注册失败，请重试';
      message.error(errorMessage);
    }
  };

  const tabItems = [
    {
      key: 'login',
      label: '登录',
      children: (
        <Form
          name="login"
          onFinish={handleLogin}
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
            name="password"
            rules={[{ required: true, message: '请输入密码!' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              style={{ width: '100%' }}
            >
              登录
            </Button>
          </Form.Item>
        </Form>
      ),
    },
    {
      key: 'register',
      label: '注册',
      children: (
        <Form
          name="register"
          onFinish={handleRegister}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入用户名!' },
              { min: 3, message: '用户名至少3个字符!' },
            ]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名"
            />
          </Form.Item>

          <Form.Item
            name="realName"
            rules={[{ required: true, message: '请输入真实姓名!' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="真实姓名"
            />
          </Form.Item>

          <Form.Item
            name="phone"
            rules={[
              { required: true, message: '请输入手机号!' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号!' },
            ]}
          >
            <Input
              prefix={<PhoneOutlined />}
              placeholder="手机号"
            />
          </Form.Item>

          <Form.Item
            name="mail"
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

          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码!' },
              { min: 6, message: '密码至少6个字符!' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              style={{ width: '100%' }}
            >
              注册
            </Button>
          </Form.Item>
        </Form>
      ),
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
      
      <div className="login-content">
        <div className="login-card">
          <div className="login-header">
            <div className="logo-section">
              <LinkOutlined className="logo-icon" />
              <Title level={2} className="login-title">
                ShortLink
              </Title>
            </div>
            <p className="login-subtitle">
              专业的短链接系统
            </p>
          </div>
          
          <Tabs
            activeKey={activeTab}
            onChange={setActiveTab}
            items={tabItems}
            centered
            className="login-tabs"
          />
          
          {error && (
            <div className="error-message" style={{ 
              background: 'rgba(255, 77, 79, 0.1)',
              border: '1px solid rgba(255, 77, 79, 0.3)',
              color: '#ff4d4f',
              padding: '12px 16px',
              borderRadius: '8px',
              textAlign: 'center',
              marginBottom: '16px',
              fontSize: '14px',
              fontWeight: '500',
              animation: 'shake 0.5s ease-in-out'
            }}>
              <span style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
                ⚠️ {error}
              </span>
            </div>
          )}
          
          <Divider className="login-divider" />
          
          <div className="login-footer">
            <Space direction="vertical" size="small" style={{ width: '100%', textAlign: 'center' }}>
              <span className="footer-text">
                使用我们的服务即表示您同意我们的
                <a href="#" className="footer-link" onClick={(e) => { e.preventDefault(); setTermsModalVisible(true); }}>服务条款</a>
                和
                <a href="#" className="footer-link" onClick={(e) => { e.preventDefault(); setPrivacyModalVisible(true); }}>隐私政策</a>
              </span>
            </Space>
          </div>
        </div>
      </div>
      
      {/* 服务条款弹窗 */}
      <Modal
        title="服务条款"
        open={termsModalVisible}
        onCancel={() => setTermsModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setTermsModalVisible(false)}>
            关闭
          </Button>
        ]}
        width={600}
      >
        <div style={{ maxHeight: '400px', overflowY: 'auto', padding: '16px 0' }}>
          <h3>1. 服务说明</h3>
          <p>ShortLink 是一个专业的短链接系统，为用户提供安全、高效的短链接生成和管理服务。</p>
          
          <h3>2. 用户责任</h3>
          <p>用户在使用本服务时，应当遵守相关法律法规，不得利用本服务从事违法违规活动。</p>
          
          <h3>3. 服务变更</h3>
          <p>我们保留随时修改或终止服务的权利，恕不另行通知。</p>
          
          <h3>4. 免责声明</h3>
          <p>本服务按"现状"提供，我们不保证服务的无中断性或无错误性。</p>
          
          <h3>5. 联系方式</h3>
          <p>如有疑问，请联系我们的客服团队。</p>
        </div>
      </Modal>
      
      {/* 隐私政策弹窗 */}
      <Modal
        title="隐私政策"
        open={privacyModalVisible}
        onCancel={() => setPrivacyModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setPrivacyModalVisible(false)}>
            关闭
          </Button>
        ]}
        width={600}
      >
        <div style={{ maxHeight: '400px', overflowY: 'auto', padding: '16px 0' }}>
          <h3>1. 信息收集</h3>
          <p>我们收集您主动提供的信息，包括用户名、邮箱、手机号等注册信息。</p>
          
          <h3>2. 信息使用</h3>
          <p>我们使用收集的信息来提供、维护和改进我们的服务，以及与您沟通。</p>
          
          <h3>3. 信息保护</h3>
          <p>我们采用行业标准的安全措施来保护您的个人信息，防止未经授权的访问、使用或泄露。</p>
          
          <h3>4. 信息共享</h3>
          <p>我们不会向第三方出售、交易或转让您的个人信息，除非获得您的明确同意。</p>
          
          <h3>5. Cookie 使用</h3>
          <p>我们使用 Cookie 来改善用户体验，您可以通过浏览器设置管理 Cookie。</p>
          
          <h3>6. 政策更新</h3>
          <p>我们可能会不时更新本隐私政策，更新后的政策将在本页面公布。</p>
        </div>
      </Modal>
    </div>
  );
};

export default Login;
