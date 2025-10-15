import React, { useEffect, useState } from 'react';
import {
  Card,
  Table,
  Button,
  message,
  Popconfirm,
  Space,
  Typography,
  Tag,
  Tooltip,
  Select,
  Input,
  Row,
  Col,
  Statistic,
  Empty,
  Modal,
} from 'antd';
import {
  RestOutlined,
  DeleteOutlined,
  CopyOutlined,
  ReloadOutlined,
  ClearOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store';
import {
  fetchRecycleBinShortLinks,
  recoverFromRecycleBin,
  removeFromRecycleBin,
} from '../store/slices/recycleBinSlice';
import { fetchGroups } from '../store/slices/groupSlice';

const { Title } = Typography;
const { Option } = Select;

const RecycleBin: React.FC = () => {
  const [selectedGroup, setSelectedGroup] = useState<string>('all');
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [batchModalVisible, setBatchModalVisible] = useState<boolean>(false);
  const dispatch = useDispatch<AppDispatch>();
  const { shortLinks, total, loading, currentPage, pageSize } = useSelector((state: RootState) => state.recycleBin);
  const { groups } = useSelector((state: RootState) => state.group);

  useEffect(() => {
    dispatch(fetchGroups());
    dispatch(fetchRecycleBinShortLinks({ current: 1, size: 10 }));
  }, [dispatch]);

  const handleRecover = async (record: any) => {
    try {
      await dispatch(recoverFromRecycleBin({
        gid: record.gid,
        fullShortUrl: record.fullShortUrl,
      })).unwrap();
      message.success('恢复成功');
      dispatch(fetchRecycleBinShortLinks({ 
        current: currentPage, 
        size: pageSize,
        gidList: selectedGroup === 'all' ? undefined : [selectedGroup],
      }));
    } catch (error) {
      message.error('恢复失败');
    }
  };

  const handleRemove = async (record: any) => {
    try {
      await dispatch(removeFromRecycleBin({
        gid: record.gid,
        fullShortUrl: record.fullShortUrl,
      })).unwrap();
      message.success('彻底删除成功');
      dispatch(fetchRecycleBinShortLinks({ 
        current: currentPage, 
        size: pageSize,
        gidList: selectedGroup === 'all' ? undefined : [selectedGroup],
      }));
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    message.success('已复制到剪贴板');
  };

  const handleGroupFilter = (value: string) => {
    setSelectedGroup(value);
    dispatch(fetchRecycleBinShortLinks({ 
      current: 1, 
      size: pageSize,
      gidList: value === 'all' ? undefined : [value],
    }));
  };


  const handleRefresh = () => {
    dispatch(fetchRecycleBinShortLinks({ 
      current: currentPage, 
      size: pageSize,
      gidList: selectedGroup === 'all' ? undefined : [selectedGroup],
    }));
  };

  const handleClearFilters = () => {
    setSelectedGroup('all');
    dispatch(fetchRecycleBinShortLinks({ current: 1, size: pageSize }));
  };

  const handleBatchRecover = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要恢复的短链接');
      return;
    }
    
    try {
      const selectedRecords = shortLinks.filter(link => selectedRowKeys.includes(link.gid));
      for (const record of selectedRecords) {
        await dispatch(recoverFromRecycleBin({
          gid: record.gid,
          fullShortUrl: record.fullShortUrl,
        })).unwrap();
      }
      message.success(`成功恢复 ${selectedRecords.length} 个短链接`);
      setSelectedRowKeys([]);
      setBatchModalVisible(false);
      handleRefresh();
    } catch (error) {
      message.error('批量恢复失败');
    }
  };

  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的短链接');
      return;
    }
    
    Modal.confirm({
      title: '确认批量删除',
      icon: <ExclamationCircleOutlined />,
      content: `确定要彻底删除选中的 ${selectedRowKeys.length} 个短链接吗？此操作不可恢复！`,
      okText: '确定',
      cancelText: '取消',
      okType: 'danger',
      onOk: async () => {
        try {
          const selectedRecords = shortLinks.filter(link => selectedRowKeys.includes(link.gid));
          for (const record of selectedRecords) {
            await dispatch(removeFromRecycleBin({
              gid: record.gid,
              fullShortUrl: record.fullShortUrl,
            })).unwrap();
          }
          message.success(`成功删除 ${selectedRecords.length} 个短链接`);
          setSelectedRowKeys([]);
          setBatchModalVisible(false);
          handleRefresh();
        } catch (error) {
          message.error('批量删除失败');
        }
      },
    });
  };

  const handleTableChange = (pagination: any) => {
    dispatch(fetchRecycleBinShortLinks({ 
      current: pagination.current, 
      size: pagination.pageSize,
      gidList: selectedGroup === 'all' ? undefined : [selectedGroup],
    }));
  };

  const rowSelection = {
    selectedRowKeys,
    onChange: setSelectedRowKeys,
    getCheckboxProps: (record: any) => ({
      disabled: record.delFlag === 1, // 已删除的不允许选择
    }),
  };

  const columns = [
    {
      title: '短链接',
      dataIndex: 'fullShortUrl',
      key: 'fullShortUrl',
      width: 200,
      render: (text: string) => (
        <div>
          <div style={{ fontWeight: 500, marginBottom: 4, wordBreak: 'break-all' }}>
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
      width: 250,
      render: (text: string) => (
        <Tooltip title={text}>
          <a href={text} target="_blank" rel="noopener noreferrer" style={{ wordBreak: 'break-all' }}>
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
      width: 150,
      render: (text: string) => text || '-',
    },
    {
      title: '访问统计',
      key: 'stats',
      width: 120,
      render: (_: any, record: any) => (
        <div>
          <div style={{ marginBottom: 4 }}>
            <Tag color="blue">总访问: {record.totalPv || 0}</Tag>
          </div>
          <div>
            <Tag color="green">今日: {record.todayPv || 0}</Tag>
          </div>
        </div>
      ),
    },
    {
      title: '删除时间',
      dataIndex: 'delTime',
      key: 'delTime',
      width: 150,
      render: (text: number) => text ? new Date(text).toLocaleString() : '-',
    },
    {
      title: '状态',
      dataIndex: 'delFlag',
      key: 'delFlag',
      width: 80,
      render: (delFlag: number) => (
        <Tag color={delFlag === 1 ? 'red' : 'orange'}>
          {delFlag === 1 ? '已删除' : '回收站'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      fixed: 'right',
      render: (_: any, record: any) => (
        <Space>
          {record.delFlag !== 1 && (
            <Button
              type="link"
              icon={<RestOutlined />}
              onClick={() => handleRecover(record)}
              size="small"
            >
              恢复
            </Button>
          )}
          {record.delFlag !== 1 && (
            <Popconfirm
              title="确定要彻底删除这个短链接吗？此操作不可恢复！"
              onConfirm={() => handleRemove(record)}
              okText="确定"
              cancelText="取消"
              okType="danger"
            >
              <Button
                type="link"
                danger
                icon={<DeleteOutlined />}
                size="small"
              >
                彻底删除
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  // 计算统计数据
  const totalLinks = shortLinks.length;
  const recoverableLinks = shortLinks.filter(link => link.delFlag !== 1).length;

  return (
    <div>
      {/* 页面标题和操作栏 */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: 24 
      }}>
        <Title level={2} style={{ margin: 0 }}>
          回收站
        </Title>
        <Space>
          <Button
            icon={<ReloadOutlined />}
            onClick={handleRefresh}
            loading={loading}
          >
            刷新
          </Button>
          <Button
            icon={<ClearOutlined />}
            onClick={handleClearFilters}
          >
            清空筛选
          </Button>
        </Space>
      </div>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card>
            <Statistic
              title="总数量"
              value={totalLinks}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={12}>
          <Card>
            <Statistic
              title="可恢复"
              value={recoverableLinks}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 筛选和搜索栏 */}
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col span={6}>
            <Select
              style={{ width: '100%' }}
              placeholder="按分组筛选"
              value={selectedGroup}
              onChange={handleGroupFilter}
            >
              <Option value="all">全部</Option>
              {groups.map(group => (
                <Option key={group.gid} value={group.gid}>
                  {group.name}
                </Option>
              ))}
            </Select>
          </Col>
          <Col span={6}>
            <Space>
              {selectedRowKeys.length > 0 && (
                <>
                  <Button
                    type="primary"
                    icon={<RestOutlined />}
                    onClick={() => setBatchModalVisible(true)}
                  >
                    批量恢复 ({selectedRowKeys.length})
                  </Button>
                  <Button
                    danger
                    icon={<DeleteOutlined />}
                    onClick={handleBatchDelete}
                  >
                    批量删除 ({selectedRowKeys.length})
                  </Button>
                </>
              )}
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 数据表格 */}
      <Card className="table-container">
        <Table
          columns={columns}
          dataSource={shortLinks}
          rowKey="gid"
          loading={loading}
          rowSelection={rowSelection}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条记录`,
            pageSizeOptions: ['10', '20', '50', '100'],
          }}
          onChange={handleTableChange}
          size="middle"
          scroll={{ x: 1200 }}
          locale={{
            emptyText: (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="回收站暂无数据"
              />
            )
          }}
        />
      </Card>

      {/* 批量操作确认弹窗 */}
      <Modal
        title="批量操作确认"
        open={batchModalVisible}
        onCancel={() => setBatchModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setBatchModalVisible(false)}>
            取消
          </Button>,
          <Button key="recover" type="primary" onClick={handleBatchRecover}>
            确认恢复
          </Button>,
        ]}
      >
        <p>确定要恢复选中的 {selectedRowKeys.length} 个短链接吗？</p>
        <p style={{ color: '#666', fontSize: '12px' }}>
          恢复后的短链接将重新变为可用状态
        </p>
      </Modal>
    </div>
  );
};

export default RecycleBin;
