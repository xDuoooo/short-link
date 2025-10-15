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
} from 'antd';
import {
  RestOutlined,
  DeleteOutlined,
  CopyOutlined,
  ReloadOutlined,
  ClearOutlined,
  LinkOutlined,
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


  const handleTableChange = (pagination: any) => {
    dispatch(fetchRecycleBinShortLinks({ 
      current: pagination.current, 
      size: pagination.pageSize,
      gidList: selectedGroup === 'all' ? undefined : [selectedGroup],
    }));
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
          <Tag color="blue">总访问: {record.totalPv || 0}</Tag>
        </div>
      ),
    },
    {
      title: '删除时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 150,
      render: (text: string) => text ? new Date(text).toLocaleString() : '-',
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
      fixed: 'right' as const,
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
  const totalLinks = total;
  const totalPv = shortLinks.reduce((sum, link) => sum + (link.totalPv || 0), 0);
  const totalUv = shortLinks.reduce((sum, link) => sum + (link.totalUv || 0), 0);
  
  // 找到访问量最高的短链接
  const topPvLink = shortLinks.length > 0 
    ? shortLinks.reduce((max, link) => (link.totalPv || 0) > (max.totalPv || 0) ? link : max)
    : null;

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
        <Col span={6}>
          <Card style={{ height: '100%' }}>
            <Statistic
              title="回收站链接数"
              value={totalLinks}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ height: '100%' }}>
            <Statistic
              title="总访问量"
              value={totalPv}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ height: '100%' }}>
            <Statistic
              title="总独立访客"
              value={totalUv}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ height: '100%' }}>
            <div style={{ 
              textAlign: 'center', 
              padding: '8px 0',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'center',
              height: '100%'
            }}>
              <div style={{ 
                fontSize: '14px', 
                color: '#666',
                marginBottom: '8px',
                fontWeight: '500'
              }}>
                访问量最高的短链接
              </div>
              {topPvLink && (
                <div style={{
                  background: '#f8f9fa',
                  borderRadius: '6px',
                  padding: '6px',
                  border: '1px solid #e9ecef',
                  maxHeight: '60px',
                  overflow: 'hidden'
                }}>
                  <div style={{ 
                    fontSize: '12px', 
                    fontWeight: '600',
                    color: '#495057',
                    marginBottom: '2px',
                    fontFamily: 'monospace'
                  }}>
                    {topPvLink.shortUri}
                  </div>
                  {topPvLink.describe && (
                    <div style={{ 
                      fontSize: '11px', 
                      color: '#6c757d',
                      lineHeight: '1.2',
                      maxHeight: '20px',
                      overflow: 'hidden',
                      display: '-webkit-box',
                      WebkitLineClamp: 1,
                      WebkitBoxOrient: 'vertical'
                    }}>
                      {topPvLink.describe}
                    </div>
                  )}
                </div>
              )}
            </div>
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
        </Row>
      </Card>

      {/* 数据表格 */}
      <Card className="table-container">
        <Table
          columns={columns}
          dataSource={shortLinks}
          rowKey="fullShortUrl"
          loading={loading}
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

    </div>
  );
};

export default RecycleBin;
