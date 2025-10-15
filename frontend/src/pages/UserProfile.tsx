import React, { useEffect, useState } from 'react';
import {
  Card,
  Form,
  Input,
  Button,
  message,
  Typography,
  Row,
  Col,
  Avatar,
  Space,
  Divider,
  Upload,
  Modal,
} from 'antd';
import {
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  EditOutlined,
  SaveOutlined,
  CameraOutlined,
  LoadingOutlined,
} from '@ant-design/icons';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store';
import { getUserInfo, getActualUserInfo, updateUser } from '../store/slices/authSlice';
import { authApi } from '../api/auth';

const { Title } = Typography;

interface UserFormData {
  realName: string;
  phone: string;
  mail: string;
  avatar?: string;
}

const UserProfile: React.FC = () => {
  const [isEditing, setIsEditing] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [form] = Form.useForm();
  const dispatch = useDispatch<AppDispatch>();
  const { user, loading } = useSelector((state: RootState) => state.auth);

  useEffect(() => {
    if (user) {
      form.setFieldsValue({
        realName: user.realName,
        phone: user.phone,
        mail: user.mail,
        avatar: user.avatar,
      });
    }
  }, [user, form]);

  const handleEdit = async () => {
    if (!user) return;
    
    try {
      // 获取未脱敏的用户信息
      const actualUserInfo = await dispatch(getActualUserInfo(user.username)).unwrap();
      
      // 使用未脱敏的信息填充表单
      form.setFieldsValue({
        realName: actualUserInfo.realName,
        phone: actualUserInfo.phone,
        mail: actualUserInfo.mail,
        avatar: actualUserInfo.avatar,
      });
      
      setIsEditing(true);
    } catch (error) {
      message.error('获取用户信息失败');
    }
  };

  const handleCancel = () => {
    setIsEditing(false);
    // 恢复到原始用户数据，而不是清空表单
    if (user) {
      form.setFieldsValue({
        realName: user.realName,
        phone: user.phone,
        mail: user.mail,
        avatar: user.avatar,
      });
    }
  };

  const handleSubmit = async (values: UserFormData) => {
    if (!user) return;
    
    try {
      await dispatch(updateUser({
        username: user.username,
        ...values,
      })).unwrap();
      
      // 重新获取用户信息以确保数据同步
      await dispatch(getActualUserInfo(user.username)).unwrap();
      
      message.success('更新成功');
      setIsEditing(false);
    } catch (error) {
      message.error('更新失败');
    }
  };

  const handleAvatarUpload = async (file: File) => {
    if (!user) return false;
    
    setUploading(true);
    try {
      const response = await authApi.uploadAvatar(file, user.username);
      form.setFieldsValue({ avatar: response.url });
      
      // 重新获取用户信息以确保数据同步
      await dispatch(getActualUserInfo(user.username)).unwrap();
      
      message.success('头像上传成功');
    } catch (error) {
      message.error('头像上传失败');
    } finally {
      setUploading(false);
    }
    return false; // 阻止默认上传行为
  };

  const beforeUpload = (file: File) => {
    const isImage = file.type.startsWith('image/');
    if (!isImage) {
      message.error('只能上传图片文件!');
      return false;
    }
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isLt2M) {
      message.error('图片大小不能超过 2MB!');
      return false;
    }
    return true;
  };

  return (
    <div>
      <Title level={2} style={{ marginBottom: 24 }}>
        个人资料
      </Title>

      <Row gutter={[24, 24]}>
        <Col xs={24} lg={8}>
          <Card>
            <div style={{ textAlign: 'center' }}>
              <div style={{ position: 'relative', display: 'inline-block' }}>
                <Avatar
                  size={80}
                  src={user?.avatar}
                  icon={<UserOutlined />}
                  style={{ 
                    backgroundColor: '#1890ff',
                    marginBottom: 16,
                  }}
                />
                {isEditing && (
                  <Upload
                    beforeUpload={beforeUpload}
                    customRequest={({ file }) => handleAvatarUpload(file as File)}
                    showUploadList={false}
                    accept="image/*"
                  >
                    <Button
                      type="primary"
                      shape="circle"
                      icon={uploading ? <LoadingOutlined /> : <CameraOutlined />}
                      size="small"
                      style={{
                        position: 'absolute',
                        bottom: 20,
                        right: 0,
                        zIndex: 1,
                      }}
                      loading={uploading}
                    />
                  </Upload>
                )}
              </div>
              <Title level={4} style={{ margin: 0 }}>
                {user?.realName || user?.username}
              </Title>
              <div style={{ color: '#666', marginTop: 8 }}>
                {user?.username}
              </div>
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={16}>
          <Card
            title="基本信息"
            extra={
              !isEditing ? (
                <Button
                  type="primary"
                  icon={<EditOutlined />}
                  onClick={handleEdit}
                >
                  编辑
                </Button>
              ) : (
                <Space>
                  <Button 
                    size="middle"
                    style={{ minWidth: '80px', height: '32px' }}
                    onClick={handleCancel}
                  >
                    取消
                  </Button>
                  <Button
                    type="primary"
                    icon={<SaveOutlined />}
                    onClick={() => form.submit()}
                    loading={loading}
                  >
                    保存
                  </Button>
                </Space>
              )
            }
          >
            <Form
              form={form}
              layout="vertical"
              onFinish={handleSubmit}
              disabled={!isEditing}
            >
              <Row gutter={[16, 16]}>
                <Col xs={24} sm={12}>
                  <Form.Item
                    name="realName"
                    label="真实姓名"
                    rules={[
                      { required: true, message: '请输入真实姓名!' },
                      { max: 20, message: '真实姓名不能超过20个字符!' },
                    ]}
                  >
                    <Input
                      prefix={<UserOutlined />}
                      placeholder="请输入真实姓名"
                    />
                  </Form.Item>
                </Col>
                <Col xs={24} sm={12}>
                  <Form.Item
                    name="phone"
                    label="手机号"
                    rules={[
                      { required: true, message: '请输入手机号!' },
                      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号!' },
                    ]}
                  >
                    <Input
                      prefix={<PhoneOutlined />}
                      placeholder="请输入手机号"
                    />
                  </Form.Item>
                </Col>
                <Col xs={24}>
                  <Form.Item
                    name="mail"
                    label="邮箱"
                    rules={[
                      { required: true, message: '请输入邮箱!' },
                      { type: 'email', message: '请输入正确的邮箱!' },
                    ]}
                  >
                    <Input
                      prefix={<MailOutlined />}
                      placeholder="请输入邮箱"
                    />
                  </Form.Item>
                </Col>
              </Row>
            </Form>

            <Divider />

            <div>
              <Title level={5}>账户信息</Title>
              <Row gutter={[16, 16]}>
                <Col xs={24} sm={12}>
                  <div>
                    <div style={{ color: '#666', marginBottom: 4 }}>用户名</div>
                    <div style={{ fontWeight: 500 }}>{user?.username}</div>
                  </div>
                </Col>
                <Col xs={24} sm={12}>
                  <div>
                    <div style={{ color: '#666', marginBottom: 4 }}>注册时间</div>
                    <div style={{ fontWeight: 500 }}>
                      {user?.createTime ? new Date(user.createTime).toLocaleString() : '-'}
                    </div>
                  </div>
                </Col>
                <Col xs={24} sm={12}>
                  <div>
                    <div style={{ color: '#666', marginBottom: 4 }}>最后更新</div>
                    <div style={{ fontWeight: 500 }}>
                      {user?.updateTime ? new Date(user.updateTime).toLocaleString() : '-'}
                    </div>
                  </div>
                </Col>
                <Col xs={24} sm={12}>
                  <div>
                    <div style={{ color: '#666', marginBottom: 4 }}>账户状态</div>
                    <div style={{ fontWeight: 500, color: '#52c41a' }}>
                      正常
                    </div>
                  </div>
                </Col>
              </Row>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default UserProfile;
