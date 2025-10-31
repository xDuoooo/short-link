import React, { useState } from 'react';
import {
  Card,
  Form,
  Input,
  Button,
  message,
  Typography,
  Row,
  Col,
  Radio,
  Space,
  Divider,
} from 'antd';
import {
  LockOutlined,
  MailOutlined,
  SafetyOutlined,
  ArrowLeftOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import { authApi } from '../api/auth';

const { Title, Text } = Typography;

interface ChangePasswordFormData {
  currentPassword?: string;
  newPassword: string;
  confirmPassword: string;
  emailCode?: string;
}

const ChangePassword: React.FC = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const { user } = useSelector((state: RootState) => state.auth);
  const [changeType, setChangeType] = useState<'current' | 'email'>('current');
  const [codeLoading, setCodeLoading] = useState(false);
  const [codeCountdown, setCodeCountdown] = useState(0);

  const handleSendCode = async () => {
    if (!user) {
      message.error('用户信息不存在');
      return;
    }
    
    try {
      setCodeLoading(true);
      await authApi.sendEmailCode({
        username: user.username,
        email: user.mail
      });
      message.success('验证码已发送到您的邮箱，请注意查收');
      setCodeCountdown(60);
      
      // 开始倒计时
      const timer = setInterval(() => {
        setCodeCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (error) {
      message.error('发送验证码失败');
    } finally {
      setCodeLoading(false);
    }
  };

  const handleSubmit = async (values: ChangePasswordFormData) => {
    if (!user) {
      message.error('用户信息不存在');
      return;
    }
    
    try {
      if (changeType === 'current') {
        await authApi.changePassword({
          username: user.username,
          currentPassword: values.currentPassword!,
          newPassword: values.newPassword,
          changeType: 'PASSWORD',
        });
      } else {
        await authApi.changePassword({
          username: user.username,
          emailCode: values.emailCode!,
          newPassword: values.newPassword,
          changeType: 'EMAIL',
        });
      }
      
      message.success('密码修改成功');
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        navigate('/profile');
      }, 1500);
    } catch (error: any) {
      console.error('修改密码失败:', error);
      
      // 根据错误类型显示具体的错误信息
      let errorMessage = '密码修改失败';
      const errorResponse = error.response?.data;
      
      if (errorResponse?.message) {
        const message = errorResponse.message;
        if (message.includes('验证码') || message.includes('code') || message.includes('Code')) {
          errorMessage = '邮箱验证码错误或已过期';
        } else if (message.includes('密码') || message.includes('password') || message.includes('Password')) {
          errorMessage = message;
        } else if (message.includes('用户') || message.includes('user') || message.includes('User')) {
          errorMessage = message;
        } else {
          errorMessage = message;
        }
      }
      
      message.error(errorMessage);
    }
  };

  const validateConfirmPassword = (_: any, value: string) => {
    if (!value) {
      return Promise.reject(new Error('请确认新密码'));
    }
    if (value !== form.getFieldValue('newPassword')) {
      return Promise.reject(new Error('两次输入的密码不一致'));
    }
    return Promise.resolve();
  };

  return (
    <div style={{ maxWidth: 600, margin: '0 auto', padding: '24px' }}>
      <Space style={{ marginBottom: 24 }}>
        <Button
          type="text"
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate('/profile')}
        >
          返回个人资料
        </Button>
      </Space>

      <Card>
        <Title level={2} style={{ marginBottom: 24, textAlign: 'center' }}>
          <SafetyOutlined style={{ marginRight: 8 }} />
          修改密码
        </Title>

        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          autoComplete="off"
        >
          <Form.Item label="修改方式">
            <Radio.Group
              value={changeType}
              onChange={(e) => setChangeType(e.target.value)}
            >
              <Radio value="current">使用当前密码</Radio>
              <Radio value="email">使用邮箱验证码</Radio>
            </Radio.Group>
          </Form.Item>

          <Divider />

          {changeType === 'current' && (
            <Form.Item
              name="currentPassword"
              label="当前密码"
              rules={[
                { required: true, message: '请输入当前密码!' },
                { min: 6, message: '密码至少6位字符!' },
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="请输入当前密码"
                size="large"
              />
            </Form.Item>
          )}

          {changeType === 'email' && (
            <Form.Item
              name="emailCode"
              label="邮箱验证码"
              rules={[
                { required: true, message: '请输入验证码!' },
                { len: 4, message: '验证码为4位数字!' },
                { pattern: /^\d{4}$/, message: '请输入4位数字验证码!' },
              ]}
            >
              <Input
                prefix={<MailOutlined />}
                placeholder="请输入4位验证码"
                size="large"
                maxLength={4}
                suffix={
                  <Button
                    type="link"
                    onClick={handleSendCode}
                    loading={codeLoading}
                    disabled={codeCountdown > 0}
                    style={{ padding: 0 }}
                  >
                    {codeCountdown > 0 ? `${codeCountdown}s` : '发送验证码'}
                  </Button>
                }
              />
            </Form.Item>
          )}

          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[
              { required: true, message: '请输入新密码!' },
              { min: 6, message: '密码至少6位字符!' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请输入新密码"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="确认新密码"
            rules={[
              { required: true, message: '请确认新密码!' },
              { validator: validateConfirmPassword },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请再次输入新密码"
              size="large"
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'center' }}>
            <Space size="large">
              <Button
                size="large"
                onClick={() => navigate('/profile')}
                style={{ minWidth: 100 }}
              >
                取消
              </Button>
              <Button
                type="primary"
                htmlType="submit"
                size="large"
                style={{ minWidth: 100 }}
              >
                确认修改
              </Button>
            </Space>
          </Form.Item>
        </Form>

        <div style={{ marginTop: 24, padding: 16, backgroundColor: '#f6f8fa', borderRadius: 6 }}>
          <Text type="secondary">
            <SafetyOutlined style={{ marginRight: 4 }} />
            安全提示：
          </Text>
          <ul style={{ margin: '8px 0 0 0', paddingLeft: 20, color: '#666' }}>
            <li>密码长度至少6位字符</li>
            <li>建议使用字母、数字和特殊字符的组合</li>
            <li>不要使用过于简单的密码</li>
            {changeType === 'email' && <li>验证码5分钟内有效，请及时使用</li>}
          </ul>
        </div>
      </Card>
    </div>
  );
};

export default ChangePassword;
