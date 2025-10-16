import React, { useEffect, useState } from 'react';
import {
  Card,
  Table,
  Button,
  Modal,
  Form,
  Input,
  message,
  Popconfirm,
  Space,
  Typography,
  Tooltip,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  DragOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { AppDispatch, RootState } from '../store';
import {
  fetchGroups,
  createGroup,
  updateGroup,
  deleteGroup,
  orderGroups,
  updateGroupsOrder,
} from '../store/slices/groupSlice';

const { Title } = Typography;

interface GroupFormData {
  name: string;
}

// 可排序行组件
const SortableRow = ({ children, ...props }: any) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: props['data-row-key'],
  });

  const style = {
    ...props.style,
    transform: CSS.Transform.toString(transform),
    transition,
    ...(isDragging ? { position: 'relative', zIndex: 9999 } : {}),
  };

  return (
    <tr {...props} ref={setNodeRef} style={style} {...attributes}>
      {React.Children.map(children, (child, index) => {
        // 第一个单元格是排序列，添加拖拽监听器
        if (index === 0) {
          return React.cloneElement(child, {
            children: (
              <div {...listeners} style={{ cursor: 'move' }}>
                <DragOutlined />
              </div>
            ),
          });
        }
        return child;
      })}
    </tr>
  );
};

const GroupManagement: React.FC = () => {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingGroup, setEditingGroup] = useState<any>(null);
  const [form] = Form.useForm();
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { groups, loading } = useSelector((state: RootState) => state.group);

  // 拖拽传感器配置
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );


  useEffect(() => {
    dispatch(fetchGroups());
  }, [dispatch]);

  // 处理拖拽结束
  const handleDragEnd = async (event: any) => {
    console.log('拖拽结束事件:', event);
    const { active, over } = event;

    if (!over) {
      console.log('没有目标位置，取消拖拽');
      return;
    }

    if (active.id !== over.id) {
      console.log('开始排序:', { activeId: active.id, overId: over.id });
      const oldIndex = groups.findIndex((item) => item.gid === active.id);
      const newIndex = groups.findIndex((item) => item.gid === over.id);

      console.log('索引变化:', { oldIndex, newIndex });

      const newGroups = arrayMove(groups, oldIndex, newIndex);
      
      // 更新每个分组的 sortOrder 字段
      const updatedGroups = newGroups.map((group, index) => ({
        ...group,
        sortOrder: index + 1,
      }));
      
      // 立即更新本地状态以提供即时反馈
      dispatch(updateGroupsOrder(updatedGroups));
      
      // 更新排序
      const orderData = updatedGroups.map((group) => ({
        gid: group.gid,
        sortOrder: group.sortOrder,
      }));

      console.log('排序数据:', orderData);

      try {
        await dispatch(orderGroups(orderData)).unwrap();
        message.success('排序更新成功');
      } catch (error) {
        console.error('排序更新失败:', error);
        message.error('排序更新失败');
        // 如果API调用失败，重新获取数据以恢复正确状态
        dispatch(fetchGroups());
      }
    } else {
      console.log('位置没有变化，无需更新');
    }
  };

  const handleCreate = () => {
    setEditingGroup(null);
    form.resetFields();
    setIsModalVisible(true);
  };

  const handleEdit = (record: any) => {
    setEditingGroup(record);
    form.setFieldsValue({ name: record.name });
    setIsModalVisible(true);
  };

  const handleDelete = async (gid: string) => {
    try {
      await dispatch(deleteGroup(gid)).unwrap();
      message.success('删除成功');
      // Redux store 会自动更新本地状态，无需重新获取数据
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleViewShortLinks = (gid: string, groupName: string) => {
    navigate(`/groups/${gid}/shortlinks`, { state: { groupName } });
  };

  const handleSubmit = async (values: GroupFormData) => {
    try {
      if (editingGroup) {
        await dispatch(updateGroup({
          gid: editingGroup.gid,
          name: values.name,
        })).unwrap();
        message.success('更新成功');
        // 更新成功后重新获取分组列表
        dispatch(fetchGroups());
      } else {
        await dispatch(createGroup(values.name)).unwrap();
        message.success('创建成功');
        // 创建成功后重新获取分组列表
        dispatch(fetchGroups());
      }
      setIsModalVisible(false);
      form.resetFields();
    } catch (error) {
      message.error(editingGroup ? '更新失败' : '创建失败');
    }
  };


  const columns = [
    {
      title: '排序',
      dataIndex: 'sortOrder',
      key: 'sortOrder',
      width: 80,
      render: () => (
        <Tooltip title="拖拽排序">
          <DragOutlined style={{ cursor: 'move', color: '#999' }} />
        </Tooltip>
      ),
    },
    {
      title: '分组名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string) => (
        <span style={{ fontWeight: 500 }}>{text}</span>
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
            icon={<EyeOutlined />}
            onClick={() => handleViewShortLinks(record.gid, record.name)}
          >
            查看短链接
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个分组吗？"
            onConfirm={() => handleDelete(record.gid)}
            okText="确定"
            cancelText="取消"
            okButtonProps={{
              size: 'middle',
              style: { minWidth: '80px', height: '32px' }
            }}
            cancelButtonProps={{
              size: 'middle',
              style: { minWidth: '80px', height: '32px' }
            }}
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

  return (
    <div>
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: 24 
      }}>
        <Title level={2} style={{ margin: 0 }}>
          分组管理
        </Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleCreate}
        >
          新建分组
        </Button>
      </div>

      <Card className="table-container">
        {groups.length > 0 ? (
          <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
          >
            <SortableContext
              items={groups.map((group) => group.gid)}
              strategy={verticalListSortingStrategy}
            >
              <Table
                columns={columns}
                dataSource={groups}
                rowKey="gid"
                loading={loading}
                pagination={false}
                size="middle"
                components={{
                  body: {
                    row: SortableRow,
                  },
                }}
              />
            </SortableContext>
          </DndContext>
        ) : (
          <Table
            columns={columns}
            dataSource={[]}
            rowKey="gid"
            loading={loading}
            pagination={false}
            size="middle"
            locale={{
              emptyText: (
                <div style={{ 
                  padding: '40px 0', 
                  textAlign: 'center',
                  color: '#999'
                }}>
                  <div style={{ fontSize: '16px', marginBottom: '8px' }}>
                    暂无分组数据
                  </div>
                  <div style={{ fontSize: '14px' }}>
                    点击右上角"新建分组"按钮创建第一个分组
                  </div>
                </div>
              )
            }}
          />
        )}
      </Card>

      <Modal
        title={editingGroup ? '编辑分组' : '新建分组'}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
        }}
        footer={null}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            name="name"
            label="分组名称"
            rules={[
              { required: true, message: '请输入分组名称!' },
              { max: 20, message: '分组名称不能超过20个字符!' },
            ]}
          >
            <Input placeholder="请输入分组名称" />
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
                {editingGroup ? '更新' : '创建'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default GroupManagement;
