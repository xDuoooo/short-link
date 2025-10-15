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
  Switch,
  Row,
  Col,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CopyOutlined,
  EyeOutlined,
  DownloadOutlined,
  ArrowLeftOutlined,
  LinkOutlined,
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
  setIncludeRecycle,
} from '../store/slices/shortLinkSlice';
import { shortLinkApi } from '../api/shortLink';
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



  const handleTableChange = (pagination: any) => {
    dispatch(fetchShortLinks({ 
      current: pagination.current, 
      size: pagination.pageSize, 
      gid
    }));
  };


  const handleSubmit = async (values: ShortLinkFormData) => {
    try {
      console.log('表单提交值:', values);
      console.log('validDateType:', values.validDateType);
      console.log('validDate:', values.validDate);
      
      // 验证有效期
      if (values.validDateType === 1 && !values.validDate) {
        message.error('请选择有效期!');
        return;
      }

      const formData = {
        ...values,
        createdType: 1, // 控制台创建
        // 如果是永久有效（validDateType = 0），则设置 validDate 为 null
        // 如果是自定义有效期（validDateType = 1），则格式化日期
        validDate: values.validDateType === 0 ? null : (values.validDate ? dayjs(values.validDate).format('YYYY-MM-DD HH:mm:ss') : ''),
      };

      if (editingLink) {
        await dispatch(updateShortLink({
          ...formData,
          fullShortUrl: editingLink.fullShortUrl,
          originGid: editingLink.gid, // 添加原始分组ID
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
      console.log('批量表单提交值:', values);
      console.log('validDateType:', values.validDateType);
      console.log('validDate:', values.validDate);
      
      // 验证有效期
      if (values.validDateType === 1 && !values.validDate) {
        message.error('请选择有效期!');
        return;
      }

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
      title: (
        <span>
          <LinkOutlined style={{ marginRight: 8 }} />
          短链接
        </span>
      ),
      dataIndex: 'fullShortUrl',
      key: 'fullShortUrl',
      width: 300,
      render: (text: string, record: any) => (
        <div>
          <div style={{ fontWeight: 500, marginBottom: 4, display: 'flex', alignItems: 'center' }}>
            {record.favicon && (
              <img 
                src={record.favicon} 
                alt="favicon" 
                style={{ 
                  width: 16, 
                  height: 16, 
                  marginRight: 8,
                  borderRadius: 2
                }}
                onError={(e) => {
                  e.currentTarget.style.display = 'none';
                }}
              />
            )}
            <a 
              href={text} 
              target="_blank" 
              rel="noopener noreferrer"
              style={{ color: '#1890ff', textDecoration: 'none' }}
              onMouseEnter={(e) => (e.target as HTMLElement).style.textDecoration = 'underline'}
              onMouseLeave={(e) => (e.target as HTMLElement).style.textDecoration = 'none'}
            >
              {text}
            </a>
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
      title: '状态',
      dataIndex: 'enableStatus',
      key: 'enableStatus',
      width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '正常' : '回收站'}
        </Tag>
      ),
    },
    {
      title: '访问量',
      dataIndex: 'totalPv',
      key: 'totalPv',
      width: 80,
      render: (text: number) => (
        <Tag color="blue">{text || 0}</Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (text: string) => new Date(text).toLocaleString(),
    },
    {
      title: '过期时间',
      dataIndex: 'validDate',
      key: 'validDate',
      render: (text: string) => {
        if (!text || text === '2099-12-31 23:59:59') {
          return '永久';
        }
        return new Date(text).toLocaleString();
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
      render: (_: any, record: any) => (
        <Space>
          {record.enableStatus === 1 ? (
            <>
              <Button
                type="link"
                icon={<EditOutlined />}
                onClick={() => handleEdit(record)}
              >
                编辑
              </Button>
              <Popconfirm
                title="确定要将这个短链接移到回收站吗？"
                onConfirm={() => handleDelete(record)}
                okText="确定"
                cancelText="取消"
              >
                <Button
                  type="link"
                  danger
                  icon={<DeleteOutlined />}
                >
                  移到回收站
                </Button>
              </Popconfirm>
            </>
          ) : (
            <Tag color="red">已移入回收站</Tag>
          )}
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
          initialValues={{ validDateType: 0 }}
        >
          <Form.Item name="gid" style={{ display: 'none' }}>
            <Input />
          </Form.Item>
          
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

          <Form.Item shouldUpdate={(prevValues, currentValues) => prevValues.validDateType !== currentValues.validDateType} noStyle>
            {({ getFieldValue }) => (
              <Form.Item
                name="validDate"
                label="有效期"
                dependencies={['validDateType']}
              >
                <DatePicker
                  showTime
                  placeholder="请选择有效期"
                  style={{ width: '100%' }}
                  disabled={getFieldValue('validDateType') === 0}
                />
              </Form.Item>
            )}
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
          initialValues={{ validDateType: 0 }}
        >
          <Form.Item name="gid" style={{ display: 'none' }}>
            <Input />
          </Form.Item>
          
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

          <Form.Item shouldUpdate={(prevValues, currentValues) => prevValues.validDateType !== currentValues.validDateType} noStyle>
            {({ getFieldValue }) => (
              <Form.Item
                name="validDate"
                label="有效期"
                dependencies={['validDateType']}
              >
                <DatePicker
                  showTime
                  placeholder="请选择有效期"
                  style={{ width: '100%' }}
                  disabled={getFieldValue('validDateType') === 0}
                />
              </Form.Item>
            )}
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
        <div>
          <Title level={2} style={{ margin: 0 }}>
            {groupName} - 短链接管理
          </Title>
          <div style={{ 
            marginTop: 8, 
            color: '#666', 
            fontSize: '14px',
            display: 'flex',
            alignItems: 'center',
            gap: '16px'
          }}>
            <span>
              <LinkOutlined style={{ marginRight: 4 }} />
              共 {total} 个短链接
            </span>
            <span>
              <EyeOutlined style={{ marginRight: 4 }} />
              总访问量: {(shortLinks || []).reduce((sum, link) => sum + (link.totalPv || 0), 0)}
            </span>
          </div>
        </div>
        <Space>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleCreate}
          >
            创建短链接
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
          onChange={handleTableChange}
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
