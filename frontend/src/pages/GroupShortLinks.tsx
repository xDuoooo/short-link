import React, { useEffect, useState } from 'react';
import {
  Card,
  Table,
  Button,
  Modal,
  Form,
  Input,
  Select,
  DatePicker,
  message,
  Popconfirm,
  Space,
  Typography,
  Tag,
  Tooltip,
  Tabs,
  Breadcrumb,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CopyOutlined,
  EyeOutlined,
  DownloadOutlined,
  ArrowLeftOutlined,
} from '@ant-design/icons';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { AppDispatch, RootState } from '../store';
import {
  fetchShortLinks,
  createShortLink,
  batchCreateShortLink,
  updateShortLink,
  getTitleByUrl,
} from '../store/slices/shortLinkSlice';
import { saveToRecycleBin } from '../store/slices/recycleBinSlice';
import dayjs from 'dayjs';

const { Title } = Typography;
const { TextArea } = Input;
const { Option } = Select;

interface ShortLinkFormData {
  gid: string;
  originUrl: string;
  domain: string;
  createdType: number;
  validDateType: number;
  validDate: string;
  describe: string;
}

interface BatchCreateFormData {
  gid: string;
  originUrls: string[];
  domain: string;
  createdType: number;
  validDateType: number;
  validDate: string;
  describe: string;
}

const GroupShortLinks: React.FC = () => {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingLink, setEditingLink] = useState<any>(null);
  const [activeTab, setActiveTab] = useState('single');
  const [form] = Form.useForm();
  const [batchForm] = Form.useForm();
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { gid } = useParams<{ gid: string }>();
  const location = useLocation();
  const { shortLinks, total, loading, currentPage, pageSize } = useSelector((state: RootState) => state.shortLink);
  
  // 从路由状态中获取分组名称
  const groupName = location.state?.groupName || '未知分组';

  useEffect(() => {
    if (gid) {
      dispatch(fetchShortLinks({ current: 1, size: 10, gid }));
    }
  }, [dispatch, gid]);

  const handleCreate = () => {
    setEditingLink(null);
    form.resetFields();
    form.setFieldsValue({ gid });
    setActiveTab('single');
    setIsModalVisible(true);
  };

  const handleBatchCreate = () => {
    batchForm.resetFields();
    batchForm.setFieldsValue({ gid });
    setActiveTab('batch');
    setIsModalVisible(true);
  };

  const handleEdit = (record: any) => {
    setEditingLink(record);
    form.setFieldsValue({
      gid: record.gid,
      originUrl: record.originUrl,
      domain: record.domain,
      createdType: record.createdType,
      validDateType: record.validDateType,
      validDate: record.validDate ? dayjs(record.validDate) : null,
      describe: record.describe,
    });
    setIsModalVisible(true);
  };

  const handleDelete = async (record: any) => {
    try {
      await dispatch(saveToRecycleBin({
        gid: record.gid,
        fullShortUrl: record.fullShortUrl,
      })).unwrap();
      message.success('已移至回收站');
      dispatch(fetchShortLinks({ current: currentPage, size: pageSize, gid }));
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    message.success('已复制到剪贴板');
  };

  const handleSubmit = async (values: ShortLinkFormData) => {
    try {
      const formData = {
        ...values,
        createdType: 1, // 控制台创建
        validDate: values.validDate ? dayjs(values.validDate).format('YYYY-MM-DD HH:mm:ss') : '',
      };

      if (editingLink) {
        await dispatch(updateShortLink({
          ...formData,
          fullShortUrl: editingLink.fullShortUrl,
        })).unwrap();
        message.success('更新成功');
      } else {
        await dispatch(createShortLink(formData)).unwrap();
        message.success('创建成功');
      }
      setIsModalVisible(false);
      form.resetFields();
      dispatch(fetchShortLinks({ current: currentPage, size: pageSize, gid }));
    } catch (error) {
      message.error(editingLink ? '更新失败' : '创建失败');
    }
  };

  const handleBatchSubmit = async (values: BatchCreateFormData) => {
    try {
      const formData = {
        ...values,
        createdType: 1, // 控制台创建
        validDate: values.validDate ? dayjs(values.validDate).format('YYYY-MM-DD HH:mm:ss') : '',
      };

      await dispatch(batchCreateShortLink(formData)).unwrap();
      message.success('批量创建成功');
      setIsModalVisible(false);
      batchForm.resetFields();
      dispatch(fetchShortLinks({ current: currentPage, size: pageSize, gid }));
    } catch (error) {
      message.error('批量创建失败');
    }
  };

  const handleGetTitle = async (url: string) => {
    if (!url) return;
    try {
      const title = await dispatch(getTitleByUrl(url)).unwrap();
      form.setFieldsValue({ describe: title });
    } catch (error) {
      message.error('获取标题失败');
    }
  };

  const columns = [
    {
      title: '短链接',
      dataIndex: 'fullShortUrl',
      key: 'fullShortUrl',
      render: (text: string) => (
        <div>
          <div style={{ fontWeight: 500, marginBottom: 4 }}>
            {text}
          </div>
          <Button
            type="link"
            size="small"
            icon={<CopyOutlined />}
            onClick={() => handleCopy(text)}
          >
            复制
          </Button>
        </div>
      ),
    },
    {
      title: '原始链接',
      dataIndex: 'originUrl',
      key: 'originUrl',
      ellipsis: true,
      render: (text: string) => (
        <Tooltip title={text}>
          <a href={text} target="_blank" rel="noopener noreferrer">
            {text}
          </a>
        </Tooltip>
      ),
    },
    {
      title: '描述',
      dataIndex: 'describe',
      key: 'describe',
      ellipsis: true,
      render: (text: string) => text || '-',
    },
    {
      title: '访问量',
      dataIndex: 'clickNum',
      key: 'clickNum',
      render: (text: number) => (
        <Tag color="blue">{text}</Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'enableStatus',
      key: 'enableStatus',
      render: (status: number) => (
        <Tag color={status === 0 ? 'green' : 'red'}>
          {status === 0 ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (text: string) => new Date(text).toLocaleString(),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: any) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            icon={<EyeOutlined />}
            href={record.fullShortUrl}
            target="_blank"
          >
            访问
          </Button>
          <Popconfirm
            title="确定要删除这个短链接吗？"
            onConfirm={() => handleDelete(record)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const tabItems = [
    {
      key: 'single',
      label: '单个创建',
      children: (
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            name="originUrl"
            label="原始链接"
            rules={[
              { required: true, message: '请输入原始链接!' },
              { type: 'url', message: '请输入正确的URL!' },
            ]}
          >
            <Input
              placeholder="请输入原始链接"
              addonAfter={
                <Button
                  type="link"
                  onClick={() => handleGetTitle(form.getFieldValue('originUrl'))}
                >
                  获取标题
                </Button>
              }
            />
          </Form.Item>

          <Form.Item
            name="domain"
            label="域名"
            rules={[{ required: true, message: '请输入域名!' }]}
          >
            <Select placeholder="请选择域名">
              <Option value="http://localhost:8001">本地域名</Option>
              <Option value="http://xDuo.top">短链接域名</Option>
            </Select>
          </Form.Item>


          <Form.Item
            name="validDateType"
            label="有效期类型"
            rules={[{ required: true, message: '请选择有效期类型!' }]}
          >
            <Select placeholder="请选择有效期类型">
              <Option value={0}>永久有效</Option>
              <Option value={1}>自定义</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="validDate"
            label="有效期"
            dependencies={['validDateType']}
          >
            <Form.Item shouldUpdate={(prevValues, currentValues) => prevValues.validDateType !== currentValues.validDateType} noStyle>
              {({ getFieldValue }) => (
                <DatePicker
                  showTime
                  placeholder="请选择有效期"
                  style={{ width: '100%' }}
                  disabled={getFieldValue('validDateType') === 0}
                />
              )}
            </Form.Item>
          </Form.Item>

          <Form.Item
            name="describe"
            label="描述"
          >
            <TextArea
              placeholder="请输入描述"
              rows={3}
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button 
                size="middle"
                style={{ minWidth: '80px', height: '32px' }}
                onClick={() => setIsModalVisible(false)}
              >
                取消
              </Button>
              <Button
                type="primary"
                size="middle"
                style={{ minWidth: '80px', height: '32px' }}
                htmlType="submit"
                loading={loading}
              >
                {editingLink ? '更新' : '创建'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      ),
    },
    {
      key: 'batch',
      label: '批量创建',
      children: (
        <Form
          form={batchForm}
          layout="vertical"
          onFinish={handleBatchSubmit}
        >
          <Form.Item
            name="originUrls"
            label="原始链接列表"
            rules={[{ required: true, message: '请输入原始链接!' }]}
          >
            <TextArea
              placeholder="每行一个链接"
              rows={6}
            />
          </Form.Item>

          <Form.Item
            name="domain"
            label="域名"
            rules={[{ required: true, message: '请输入域名!' }]}
          >
            <Select placeholder="请选择域名">
              <Option value="http://localhost:8001">本地域名</Option>
              <Option value="http://xDuo.top">短链接域名</Option>
            </Select>
          </Form.Item>


          <Form.Item
            name="validDateType"
            label="有效期类型"
            rules={[{ required: true, message: '请选择有效期类型!' }]}
          >
            <Select placeholder="请选择有效期类型">
              <Option value={0}>永久有效</Option>
              <Option value={1}>自定义</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="validDate"
            label="有效期"
            dependencies={['validDateType']}
          >
            <DatePicker
              showTime
              placeholder="请选择有效期"
              style={{ width: '100%' }}
              disabled={batchForm.getFieldValue('validDateType') === 0}
            />
          </Form.Item>

          <Form.Item
            name="describe"
            label="描述"
          >
            <TextArea
              placeholder="请输入描述"
              rows={3}
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button 
                size="middle"
                style={{ minWidth: '80px', height: '32px' }}
                onClick={() => setIsModalVisible(false)}
              >
                取消
              </Button>
              <Button 
                type="primary" 
                size="middle"
                style={{ minWidth: '80px', height: '32px' }}
                htmlType="submit" 
                loading={loading}
              >
                批量创建
              </Button>
            </Space>
          </Form.Item>
        </Form>
      ),
    },
  ];

  return (
    <div>
      <Breadcrumb style={{ marginBottom: 16, fontSize: 14 }}>
        <Breadcrumb.Item>
          <Button 
            type="link" 
            icon={<ArrowLeftOutlined />} 
            onClick={() => navigate('/groups')}
            style={{ 
              padding: 0, 
              fontSize: '14px !important',
              height: 'auto',
              lineHeight: '1.4'
            }}
          >
            分组管理
          </Button>
        </Breadcrumb.Item>
        <Breadcrumb.Item>
          <span style={{ fontSize: '14px' }}>{groupName}</span>
        </Breadcrumb.Item>
      </Breadcrumb>

      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: 24 
      }}>
        <Title level={2} style={{ margin: 0 }}>
          {groupName} - 短链接管理
        </Title>
        <Space>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleCreate}
          >
            创建短链接
          </Button>
          <Button
            type="primary"
            icon={<DownloadOutlined />}
            onClick={handleBatchCreate}
          >
            批量创建
          </Button>
        </Space>
      </div>

      <Card className="table-container">
        <Table
          columns={columns}
          dataSource={shortLinks}
          rowKey="gid"
          loading={loading}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
          size="middle"
        />
      </Card>

      <Modal
        title={activeTab === 'single' ? '创建短链接' : '批量创建短链接'}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
          batchForm.resetFields();
        }}
        footer={null}
        destroyOnClose
        width={800}
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={tabItems}
        />
      </Modal>

    </div>
  );
};

export default GroupShortLinks;
