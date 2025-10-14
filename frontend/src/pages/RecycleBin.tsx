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
} from 'antd';
import {
  RestOutlined,
  DeleteOutlined,
  EyeOutlined,
  CopyOutlined,
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
  const [selectedGroup, setSelectedGroup] = useState<string>('');
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
        gid: selectedGroup || undefined,
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
        gid: selectedGroup || undefined,
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
      gid: value || undefined,
    }));
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
      title: '删除时间',
      dataIndex: 'delTime',
      key: 'delTime',
      render: (text: number) => text ? new Date(text).toLocaleString() : '-',
    },
    {
      title: '状态',
      dataIndex: 'delFlag',
      key: 'delFlag',
      render: (delFlag: number) => (
        <Tag color={delFlag === 1 ? 'red' : 'green'}>
          {delFlag === 1 ? '已删除' : '正常'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: any) => (
        <Space>
          <Button
            type="link"
            icon={<RestOutlined />}
            onClick={() => handleRecover(record)}
          >
            恢复
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
            >
              彻底删除
            </Button>
          </Popconfirm>
        </Space>
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
          回收站
        </Title>
        <Space>
          <Select
            style={{ width: 200 }}
            placeholder="按分组筛选"
            value={selectedGroup}
            onChange={handleGroupFilter}
            allowClear
          >
            {groups.map(group => (
              <Option key={group.gid} value={group.gid}>
                {group.name}
              </Option>
            ))}
          </Select>
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
    </div>
  );
};

export default RecycleBin;
