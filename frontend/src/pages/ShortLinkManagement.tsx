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
  InputNumber,
  Upload,
  Tabs,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CopyOutlined,
  EyeOutlined,
  DownloadOutlined,
  LinkOutlined,
} from '@ant-design/icons';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store';
import {
  fetchShortLinks,
  createShortLink,
  batchCreateShortLink,
  updateShortLink,
  getTitleByUrl,
  getGroupShortLinkCount,
} from '../store/slices/shortLinkSlice';
import { fetchGroups } from '../store/slices/groupSlice';
import { saveToRecycleBin } from '../store/slices/recycleBinSlice';
import dayjs from 'dayjs';

const { Title } = Typography;
const { TextArea } = Input;
const { Option } = Select;
const { RangePicker } = DatePicker;

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
  originUrls: string;
  domain: string;
  createdType: number;
  validDateType: number;
  validDate: string;
  describe: string;
}

const ShortLinkManagement: React.FC = () => {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isBatchModalVisible, setIsBatchModalVisible] = useState(false);
  const [editingLink, setEditingLink] = useState<any>(null);
  const [activeTab, setActiveTab] = useState('single');
  const [form] = Form.useForm();
  const [batchForm] = Form.useForm();
  const dispatch = useDispatch<AppDispatch>();
  const { shortLinks, total, loading, currentPage, pageSize } = useSelector((state: RootState) => state.shortLink);
  const { groups } = useSelector((state: RootState) => state.group);

  useEffect(() => {
    console.log('组件初始化，开始加载分组数据');
    dispatch(fetchGroups());
    dispatch(fetchShortLinks({ current: 1, size: 10 }));
  }, [dispatch]);

  // 当分组数据加载完成后，如果模态框是打开的，自动设置默认分组
  useEffect(() => {
    console.log('分组数据变化:', groups);
    if (isModalVisible && groups.length > 0) {
      console.log('模态框打开，自动设置默认分组:', groups[0].gid);
      form.setFieldsValue({ gid: groups[0].gid });
      // 强制触发表单验证
      form.validateFields(['gid']);
    }
  }, [isModalVisible, groups, form]);

  // 当批量创建模态框打开时，自动设置默认分组
  useEffect(() => {
    if (isBatchModalVisible && groups.length > 0) {
      console.log('批量创建模态框打开，自动设置默认分组:', groups[0].gid);
      batchForm.setFieldsValue({ gid: groups[0].gid });
      // 强制触发表单验证
      batchForm.validateFields(['gid']);
    }
  }, [isBatchModalVisible, groups, batchForm]);

  const handleCreate = () => {
    setEditingLink(null);
    setActiveTab('single');
    setIsModalVisible(true);
    
    // 重置表单并设置默认值
    form.resetFields();
    
    // 使用 setTimeout 确保表单重置完成后再设置默认值
    setTimeout(() => {
      if (groups.length > 0) {
        console.log('立即设置默认分组:', groups[0].gid);
        form.setFieldsValue({ 
          gid: groups[0].gid,
          validDateType: 0 
        });
        // 验证设置是否成功
        setTimeout(() => {
          const currentValues = form.getFieldsValue();
          console.log('表单设置后的值:', currentValues);
        }, 50);
      } else {
        console.warn('没有可用的分组数据');
      }
    }, 100);
  };

  const handleBatchCreate = () => {
    setIsBatchModalVisible(true);
    
    // 重置表单并设置默认值
    batchForm.resetFields();
    
    // 使用 setTimeout 确保表单重置完成后再设置默认值
    setTimeout(() => {
      if (groups.length > 0) {
        console.log('批量创建立即设置默认分组:', groups[0].gid);
        batchForm.setFieldsValue({ 
          gid: groups[0].gid,
          validDateType: 0 
        });
      }
    }, 100);
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
      dispatch(fetchShortLinks({ current: currentPage, size: pageSize }));
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
      console.log('=== 表单提交调试信息 ===');
      console.log('表单提交数据:', values);
      console.log('gid字段值:', values.gid);
      console.log('gid字段类型:', typeof values.gid);
      console.log('表单当前值:', form.getFieldsValue());
      console.log('分组数据:', groups);
      console.log('当前选中的分组:', groups.find(g => g.gid === values.gid));
      
      // 验证gid是否存在，如果不存在则使用第一个分组
      let gid = values.gid;
      if (!gid && groups.length > 0) {
        gid = groups[0].gid;
        console.log('gid为空，使用默认分组:', gid);
      }
      
      if (!gid) {
        console.error('gid字段为空且没有可用分组，无法提交');
        message.error('请先创建分组');
        return;
      }
      
      // 确保gid字段存在且有效
      if (!gid || gid.trim() === '') {
        message.error('请选择分组');
        return;
      }
      
      const formData = {
        ...values,
        gid: gid, // 确保gid字段存在
        createdType: 1, // 控制台创建
        validDate: values.validDate ? dayjs(values.validDate).format('YYYY-MM-DD HH:mm:ss') : '',
      };
      console.log('处理后的数据:', formData);
      console.log('最终发送的数据包含gid:', !!formData.gid);
      console.log('gid值:', formData.gid);
      console.log('=== 调试信息结束 ===');

      if (editingLink) {
        await dispatch(updateShortLink({
          ...formData,
          fullShortUrl: editingLink.fullShortUrl,
          originGid: editingLink.gid,
        })).unwrap();
        message.success('更新成功');
      } else {
        await dispatch(createShortLink(formData)).unwrap();
        message.success('创建成功');
      }
      setIsModalVisible(false);
      form.resetFields();
    } catch (error) {
      console.error('提交错误:', error); // 调试信息
      message.error(editingLink ? '更新失败' : '创建失败');
    }
  };

  const handleBatchSubmit = async (values: BatchCreateFormData) => {
    try {
      console.log('=== 批量创建表单提交调试信息 ===');
      console.log('表单提交数据:', values);
      console.log('gid字段值:', values.gid);
      console.log('分组数据:', groups);
      
      // 验证gid是否存在
      if (!values.gid || values.gid.trim() === '') {
        message.error('请选择分组');
        return;
      }
      
      // 处理原始链接列表
      const originUrls = values.originUrls
        .split('\n')
        .map((url: string) => url.trim())
        .filter((url: string) => url.length > 0);
      
      if (originUrls.length === 0) {
        message.error('请输入至少一个原始链接');
        return;
      }
      
      const formData = {
        ...values,
        originUrls,
        createdType: 1, // 控制台创建
        validDate: values.validDate ? dayjs(values.validDate).format('YYYY-MM-DD HH:mm:ss') : '',
      };
      
      console.log('批量创建处理后的数据:', formData);
      console.log('gid值:', formData.gid);
      console.log('=== 批量创建调试信息结束 ===');

      await dispatch(batchCreateShortLink(formData)).unwrap();
      message.success('批量创建成功');
      setIsBatchModalVisible(false);
      batchForm.resetFields();
    } catch (error) {
      console.error('批量创建错误:', error);
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
      dataIndex: 'totalPv',
      key: 'totalPv',
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
          initialValues={{
            validDateType: 0,
          }}
        >
          <Form.Item
            name="gid"
            label="分组"
            rules={[{ required: true, message: '请选择分组!' }]}
          >
            <Select placeholder="请选择分组">
              {groups.map(group => (
                <Option key={group.gid} value={group.gid}>
                  {group.name}
                </Option>
              ))}
            </Select>
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

          <Form.Item
            name="validDate"
            label="有效期"
            dependencies={['validDateType']}
            rules={[
              ({ getFieldValue }) => ({
                validator(_, value) {
                  const validDateType = getFieldValue('validDateType');
                  if (validDateType === 1 && !value) {
                    return Promise.reject(new Error('请选择有效期!'));
                  }
                  return Promise.resolve();
                },
              }),
            ]}
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
          initialValues={{
            validDateType: 0,
          }}
        >
          <Form.Item
            name="gid"
            label="分组"
            rules={[{ required: true, message: '请选择分组!' }]}
          >
            <Select placeholder="请选择分组">
              {groups.map(group => (
                <Option key={group.gid} value={group.gid}>
                  {group.name}
                </Option>
              ))}
            </Select>
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

          <Form.Item
            name="validDate"
            label="有效期"
            dependencies={['validDateType']}
            rules={[
              ({ getFieldValue }) => ({
                validator(_, value) {
                  const validDateType = getFieldValue('validDateType');
                  if (validDateType === 1 && !value) {
                    return Promise.reject(new Error('请选择有效期!'));
                  }
                  return Promise.resolve();
                },
              }),
            ]}
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
        </Form>
      ),
    },
  ];

  return (
    <div>
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: 24 
      }}>
        <Title level={2} style={{ margin: 0 }}>
          短链接管理
        </Title>
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
          size="middle"
        />
      </Card>

      <Modal
        title="创建短链接"
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
        }}
        footer={null}
        destroyOnClose
        width={600}
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={tabItems}
        />
        <div style={{ textAlign: 'right', marginTop: 16 }}>
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
              onClick={() => form.submit()}
              loading={loading}
            >
              {editingLink ? '更新' : '创建'}
            </Button>
          </Space>
        </div>
      </Modal>

      <Modal
        title="批量创建短链接"
        open={isBatchModalVisible}
        onCancel={() => {
          setIsBatchModalVisible(false);
          batchForm.resetFields();
        }}
        footer={null}
        destroyOnClose
        width={600}
      >
        <Form
          form={batchForm}
          layout="vertical"
          onFinish={handleBatchSubmit}
          initialValues={{
            validDateType: 0,
          }}
        >
          <Form.Item
            name="gid"
            label="分组"
            rules={[{ required: true, message: '请选择分组!' }]}
          >
            <Select placeholder="请选择分组">
              {groups.map(group => (
                <Option key={group.gid} value={group.gid}>
                  {group.name}
                </Option>
              ))}
            </Select>
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

          <Form.Item
            name="validDate"
            label="有效期"
            dependencies={['validDateType']}
            rules={[
              ({ getFieldValue }) => ({
                validator(_, value) {
                  const validDateType = getFieldValue('validDateType');
                  if (validDateType === 1 && !value) {
                    return Promise.reject(new Error('请选择有效期!'));
                  }
                  return Promise.resolve();
                },
              }),
            ]}
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
                onClick={() => setIsBatchModalVisible(false)}
              >
                取消
              </Button>
              <Button type="primary" htmlType="submit" loading={loading}>
                批量创建
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ShortLinkManagement;
