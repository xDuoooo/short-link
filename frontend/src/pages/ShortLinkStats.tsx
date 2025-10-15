import React, { useEffect, useState } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Select,
  DatePicker,
  Button,
  Table,
  Typography,
  Space,
  Tabs,
  Spin,
  Switch,
  Tooltip,
} from 'antd';
import {
  EyeOutlined,
  UserOutlined,
  GlobalOutlined,
  BarChartOutlined,
} from '@ant-design/icons';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell } from 'recharts';
import { Tooltip as RechartsTooltip } from 'recharts';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store';
import {
  getShortLinkStats,
  getGroupShortLinkStats,
  getShortLinkAccessRecords,
  getGroupShortLinkAccessRecords,
  clearStatsData,
} from '../store/slices/statsSlice';
import { fetchShortLinks, clearShortLinks } from '../store/slices/shortLinkSlice';
import { fetchGroups } from '../store/slices/groupSlice';
import dayjs from 'dayjs';

const { Title } = Typography;
const { RangePicker } = DatePicker;
const { Option } = Select;

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

const ShortLinkStats: React.FC = () => {
  const [selectedGroup, setSelectedGroup] = useState<string>('');
  const [selectedShortLink, setSelectedShortLink] = useState<string>('');
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([
    dayjs().subtract(7, 'day'),
    dayjs(),
  ]);
  const [activeTab, setActiveTab] = useState('overview');
  const [includeRecycle, setIncludeRecycle] = useState<boolean>(false);

  const dispatch = useDispatch<AppDispatch>();
  const { statsData, accessRecords, loading } = useSelector((state: RootState) => state.stats);
  const { shortLinks } = useSelector((state: RootState) => state.shortLink);
  const { groups } = useSelector((state: RootState) => state.group);

  // 添加调试信息
  useEffect(() => {
    console.log('统计数据更新:', statsData);
    console.log('访问记录更新:', accessRecords);
    console.log('加载状态:', loading);
  }, [statsData, accessRecords, loading]);

  useEffect(() => {
    dispatch(fetchGroups());
  }, [dispatch]);

  // 当分组数据加载完成后，自动选择第一个分组
  useEffect(() => {
    if (groups.length > 0 && !selectedGroup) {
      setSelectedGroup(groups[0].gid);
    }
  }, [groups, selectedGroup]);

  // 当选中分组时，获取该分组的短链接数据
  useEffect(() => {
    if (selectedGroup) {
      dispatch(fetchShortLinks({ gid: selectedGroup, current: 1, size: 100 }));
    }
  }, [dispatch, selectedGroup]);

  useEffect(() => {
    if (selectedGroup && dateRange[0] && dateRange[1]) {
      console.log('获取统计数据，参数:', {
        selectedGroup,
        selectedShortLink,
        startDate: dateRange[0].format('YYYY-MM-DD'),
        endDate: dateRange[1].format('YYYY-MM-DD'),
        includeRecycle
      });
      
      if (selectedShortLink) {
        // 获取单个短链接统计
        dispatch(getShortLinkStats({
          fullShortUrl: selectedShortLink,
          gid: selectedGroup,
          startDate: dateRange[0].format('YYYY-MM-DD'),
          endDate: dateRange[1].format('YYYY-MM-DD'),
          includeRecycle: includeRecycle,
        }));
        // 获取单个短链接访问记录
        dispatch(getShortLinkAccessRecords({
          fullShortUrl: selectedShortLink,
          gid: selectedGroup,
          startDate: dateRange[0].format('YYYY-MM-DD'),
          endDate: dateRange[1].format('YYYY-MM-DD'),
          current: 1,
          size: 10,
          includeRecycle: includeRecycle,
        }));
      } else {
        // 获取分组统计
        dispatch(getGroupShortLinkStats({
          gid: selectedGroup,
          startDate: dateRange[0].format('YYYY-MM-DD'),
          endDate: dateRange[1].format('YYYY-MM-DD'),
          includeRecycle: includeRecycle,
        }));
        // 获取分组访问记录
        dispatch(getGroupShortLinkAccessRecords({
          gid: selectedGroup,
          startDate: dateRange[0].format('YYYY-MM-DD'),
          endDate: dateRange[1].format('YYYY-MM-DD'),
          current: 1,
          size: 10,
          includeRecycle: includeRecycle,
        }));
      }
    }
  }, [dispatch, selectedGroup, selectedShortLink, dateRange, includeRecycle]);

  const handleGroupChange = (value: string) => {
    setSelectedGroup(value);
    setSelectedShortLink('');
    dispatch(clearStatsData());
    // 切换分组时先清空短链接数据，然后重新获取该分组的短链接数据
    dispatch(clearShortLinks());
    dispatch(fetchShortLinks({ gid: value, current: 1, size: 100 }));
  };

  const handleShortLinkChange = (value: string) => {
    setSelectedShortLink(value);
    dispatch(clearStatsData());
  };

  const handleDateRangeChange = (dates: any, dateStrings: [string, string]) => {
    if (dates) {
      setDateRange(dates);
      dispatch(clearStatsData());
    }
  };

  const handleRefresh = () => {
    if (selectedGroup && dateRange[0] && dateRange[1]) {
      if (selectedShortLink) {
        // 刷新单个短链接统计数据
        dispatch(getShortLinkStats({
          fullShortUrl: selectedShortLink,
          gid: selectedGroup,
          startDate: dateRange[0].format('YYYY-MM-DD'),
          endDate: dateRange[1].format('YYYY-MM-DD'),
          includeRecycle: includeRecycle,
        }));
        // 刷新单个短链接访问记录
        dispatch(getShortLinkAccessRecords({
          fullShortUrl: selectedShortLink,
          gid: selectedGroup,
          startDate: dateRange[0].format('YYYY-MM-DD'),
          endDate: dateRange[1].format('YYYY-MM-DD'),
          current: 1,
          size: 10,
          includeRecycle: includeRecycle,
        }));
      } else {
        // 刷新分组统计数据
        dispatch(getGroupShortLinkStats({
          gid: selectedGroup,
          startDate: dateRange[0].format('YYYY-MM-DD'),
          endDate: dateRange[1].format('YYYY-MM-DD'),
          includeRecycle: includeRecycle,
        }));
        // 刷新分组访问记录
        dispatch(getGroupShortLinkAccessRecords({
          gid: selectedGroup,
          startDate: dateRange[0].format('YYYY-MM-DD'),
          endDate: dateRange[1].format('YYYY-MM-DD'),
          current: 1,
          size: 10,
          includeRecycle: includeRecycle,
        }));
      }
    }
  };

  const filteredShortLinks = (shortLinks || []).filter(link => link.gid === selectedGroup);

  const accessRecordColumns = [
    {
      title: '用户',
      dataIndex: 'user',
      key: 'user',
      render: (text: string) => text || '匿名用户',
    },
    {
      title: 'IP地址',
      dataIndex: 'ip',
      key: 'ip',
    },
    {
      title: '地区',
      dataIndex: 'locale',
      key: 'locale',
    },
    {
      title: '操作系统',
      dataIndex: 'os',
      key: 'os',
    },
    {
      title: '浏览器',
      dataIndex: 'browser',
      key: 'browser',
    },
    {
      title: '设备',
      dataIndex: 'device',
      key: 'device',
    },
    {
      title: '网络',
      dataIndex: 'network',
      key: 'network',
    },
    {
      title: '短链描述',
      dataIndex: 'describe',
      key: 'describe',
      ellipsis: true,
      render: (text: string) => text || '-',
    },
    {
      title: '短链接',
      dataIndex: 'fullShortUrl',
      key: 'fullShortUrl',
      ellipsis: true,
      render: (text: string) => text ? (
        <Tooltip title={text}>
          <a href={text} target="_blank" rel="noopener noreferrer" style={{ wordBreak: 'break-all' }}>
            {text}
          </a>
        </Tooltip>
      ) : '-',
    },
    {
      title: '访问时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (text: string) => new Date(text).toLocaleString(),
    },
  ];

  const tabItems = [
    {
      key: 'overview',
      label: '概览',
      children: (
        <div>
          <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="总访问量"
                  value={statsData?.pv || 0}
                  prefix={<EyeOutlined />}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="独立访客"
                  value={statsData?.uv || 0}
                  prefix={<UserOutlined />}
                  valueStyle={{ color: '#52c41a' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="独立IP"
                  value={statsData?.uip || 0}
                  prefix={<GlobalOutlined />}
                  valueStyle={{ color: '#fa8c16' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="今日访问"
                  value={statsData?.todayPv || statsData?.pv || 0}
                  prefix={<BarChartOutlined />}
                  valueStyle={{ color: '#eb2f96' }}
                />
              </Card>
            </Col>
          </Row>

          <Row gutter={[16, 16]}>
            <Col xs={24} lg={12}>
              <Card title="访问趋势" className="chart-container">
                {statsData?.daily && statsData.daily.length > 0 ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={statsData.daily}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis />
                      <RechartsTooltip />
                      <Line type="monotone" dataKey="pv" stroke="#1890ff" name="访问量" />
                      <Line type="monotone" dataKey="uv" stroke="#52c41a" name="独立访客" />
                    </LineChart>
                  </ResponsiveContainer>
                ) : (
                  <div style={{ textAlign: 'center', padding: '50px', color: '#999' }}>
                    <div style={{ fontSize: '16px', marginBottom: '8px' }}>暂无访问趋势数据</div>
                    <div style={{ fontSize: '14px' }}>请访问短链接后查看访问趋势</div>
                  </div>
                )}
              </Card>
            </Col>
            <Col xs={24} lg={12}>
              <Card title="设备分布" className="chart-container">
                {statsData?.deviceStats && statsData.deviceStats.length > 0 ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={statsData.deviceStats}
                        cx="50%"
                        cy="50%"
                        labelLine={true}
                        label={({ device, ratio }) => {
                          // 截断过长的设备名称
                          const shortDevice = device.length > 10 ? device.substring(0, 10) + '...' : device;
                          return `${shortDevice} ${(ratio * 100).toFixed(0)}%`;
                        }}
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="cnt"
                      >
                        {statsData.deviceStats.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <RechartsTooltip 
                        formatter={(value, name, props) => [
                          `${props.payload.device}: ${value} (${(props.payload.ratio * 100).toFixed(1)}%)`,
                          '访问量'
                        ]}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                ) : (
                  <div style={{ textAlign: 'center', padding: '50px', color: '#999' }}>
                    <div style={{ fontSize: '16px', marginBottom: '8px' }}>暂无设备数据</div>
                    <div style={{ fontSize: '14px' }}>请访问短链接后查看设备分布</div>
                  </div>
                )}
              </Card>
            </Col>
          </Row>
          <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
            <Col xs={24} lg={8}>
              <Card title="浏览器分布" className="chart-container">
                {statsData?.browserStats && statsData.browserStats.length > 0 ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={statsData.browserStats}
                        cx="50%"
                        cy="50%"
                        labelLine={true}
                        label={({ browser, ratio }) => {
                          // 截断过长的浏览器名称
                          const shortBrowser = browser.length > 12 ? browser.substring(0, 12) + '...' : browser;
                          return `${shortBrowser} ${(ratio * 100).toFixed(0)}%`;
                        }}
                        outerRadius={70}
                        fill="#8884d8"
                        dataKey="cnt"
                      >
                        {statsData.browserStats.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <RechartsTooltip 
                        formatter={(value, name, props) => [
                          `${props.payload.browser}: ${value} (${(props.payload.ratio * 100).toFixed(1)}%)`,
                          '访问量'
                        ]}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                ) : (
                  <div style={{ textAlign: 'center', padding: '50px', color: '#999' }}>
                    <div style={{ fontSize: '16px', marginBottom: '8px' }}>暂无浏览器数据</div>
                    <div style={{ fontSize: '14px' }}>请访问短链接后查看浏览器分布</div>
                  </div>
                )}
              </Card>
            </Col>
            <Col xs={24} lg={8}>
              <Card title="操作系统分布" className="chart-container">
                {statsData?.osStats && statsData.osStats.length > 0 ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={statsData.osStats}
                        cx="50%"
                        cy="50%"
                        labelLine={true}
                        label={({ os, ratio }) => {
                          // 截断过长的操作系统名称
                          const shortOs = os.length > 12 ? os.substring(0, 12) + '...' : os;
                          return `${shortOs} ${(ratio * 100).toFixed(0)}%`;
                        }}
                        outerRadius={70}
                        fill="#8884d8"
                        dataKey="cnt"
                      >
                        {statsData.osStats.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <RechartsTooltip 
                        formatter={(value, name, props) => [
                          `${props.payload.os}: ${value} (${(props.payload.ratio * 100).toFixed(1)}%)`,
                          '访问量'
                        ]}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                ) : (
                  <div style={{ textAlign: 'center', padding: '50px', color: '#999' }}>
                    <div style={{ fontSize: '16px', marginBottom: '8px' }}>暂无操作系统数据</div>
                    <div style={{ fontSize: '14px' }}>请访问短链接后查看操作系统分布</div>
                  </div>
                )}
              </Card>
            </Col>
            <Col xs={24} lg={8}>
              <Card title="网络类型分布" className="chart-container">
                {statsData?.networkStats && statsData.networkStats.length > 0 ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={statsData.networkStats}
                        cx="50%"
                        cy="50%"
                        labelLine={true}
                        label={({ network, ratio }) => {
                          // 截断过长的网络类型名称
                          const shortNetwork = network.length > 12 ? network.substring(0, 12) + '...' : network;
                          return `${shortNetwork} ${(ratio * 100).toFixed(0)}%`;
                        }}
                        outerRadius={70}
                        fill="#8884d8"
                        dataKey="cnt"
                      >
                        {statsData.networkStats.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <RechartsTooltip 
                        formatter={(value, name, props) => [
                          `${props.payload.network}: ${value} (${(props.payload.ratio * 100).toFixed(1)}%)`,
                          '访问量'
                        ]}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                ) : (
                  <div style={{ textAlign: 'center', padding: '50px', color: '#999' }}>
                    <div style={{ fontSize: '16px', marginBottom: '8px' }}>暂无网络数据</div>
                    <div style={{ fontSize: '14px' }}>请访问短链接后查看网络类型分布</div>
                  </div>
                )}
              </Card>
            </Col>
          </Row>

          <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
            <Col xs={24} lg={12}>
              <Card title="24小时访问分布" className="chart-container">
                {statsData?.hourStats && statsData.hourStats.some(cnt => cnt > 0) ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={statsData.hourStats.map((cnt, hour) => ({ hour: `${hour}:00`, cnt }))}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="hour" />
                      <YAxis />
                      <RechartsTooltip 
                        formatter={(value, name, props) => [
                          `${value} 次访问`,
                          `${props.payload.hour}`
                        ]}
                      />
                      <Bar dataKey="cnt" fill="#1890ff" />
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <div style={{ textAlign: 'center', padding: '50px', color: '#999' }}>
                    <div style={{ fontSize: '16px', marginBottom: '8px' }}>暂无24小时数据</div>
                    <div style={{ fontSize: '14px' }}>请访问短链接后查看24小时访问分布</div>
                  </div>
                )}
              </Card>
            </Col>
            <Col xs={24} lg={12}>
              <Card title="星期访问分布" className="chart-container">
                {statsData?.weekdayStats && statsData.weekdayStats.some(cnt => cnt > 0) ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={statsData.weekdayStats.map((cnt, day) => ({ 
                      day: ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][day], 
                      cnt 
                    }))}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="day" />
                      <YAxis />
                      <RechartsTooltip 
                        formatter={(value, name, props) => [
                          `${value} 次访问`,
                          `${props.payload.day}`
                        ]}
                      />
                      <Bar dataKey="cnt" fill="#52c41a" />
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <div style={{ textAlign: 'center', padding: '50px', color: '#999' }}>
                    <div style={{ fontSize: '16px', marginBottom: '8px' }}>暂无星期数据</div>
                    <div style={{ fontSize: '14px' }}>请访问短链接后查看星期访问分布</div>
                  </div>
                )}
              </Card>
            </Col>
          </Row>

          <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
            <Col xs={24} lg={8}>
              <Card title="新老访客统计">
                <div style={{ textAlign: 'center' }}>
                  <div style={{ marginBottom: 16 }}>
                    <Statistic
                      title="新访客"
                      value={statsData?.uvTypeStats?.find(item => item.uvType === 'newUser')?.cnt || 0}
                      valueStyle={{ color: '#52c41a' }}
                    />
                  </div>
                  <div>
                    <Statistic
                      title="老访客"
                      value={statsData?.uvTypeStats?.find(item => item.uvType === 'oldUser')?.cnt || 0}
                      valueStyle={{ color: '#1890ff' }}
                    />
                  </div>
                </div>
              </Card>
            </Col>
            <Col xs={24} lg={8}>
              <Card title="地区分布">
                <div style={{ textAlign: 'center' }}>
                  {statsData?.localeCnStats && statsData.localeCnStats.length > 0 ? (
                    statsData.localeCnStats.map((item, index) => (
                      <div key={index} style={{ marginBottom: 12, padding: '8px', backgroundColor: '#f5f5f5', borderRadius: '6px' }}>
                        <div style={{ fontSize: '16px', fontWeight: 'bold', color: COLORS[index % COLORS.length], marginBottom: '4px' }}>
                          {item.locale || '未知地区'}
                        </div>
                        <div style={{ fontSize: '14px', color: '#666' }}>
                          访问量: <span style={{ fontWeight: 'bold', color: '#1890ff' }}>{item.cnt}</span> 次
                        </div>
                        <div style={{ fontSize: '14px', color: '#666' }}>
                          占比: <span style={{ fontWeight: 'bold', color: '#52c41a' }}>{(item.ratio * 100).toFixed(1)}%</span>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div style={{ padding: '20px', color: '#999' }}>
                      <div style={{ fontSize: '16px', marginBottom: '8px' }}>暂无地区数据</div>
                      <div style={{ fontSize: '14px' }}>请访问短链接后查看地区分布</div>
                    </div>
                  )}
                </div>
              </Card>
            </Col>
            <Col xs={24} lg={8}>
              <Card title="访问质量">
                <div style={{ textAlign: 'center' }}>
                  <div style={{ marginBottom: 16 }}>
                    <Statistic
                      title="平均每IP访问量"
                      value={statsData?.uip ? (statsData.pv / statsData.uip).toFixed(2) : 0}
                      valueStyle={{ color: '#fa8c16' }}
                    />
                  </div>
                  <div>
                    <Statistic
                      title="平均每用户访问量"
                      value={statsData?.uv ? (statsData.pv / statsData.uv).toFixed(2) : 0}
                      valueStyle={{ color: '#eb2f96' }}
                    />
                  </div>
                </div>
              </Card>
            </Col>
          </Row>
        </div>
      ),
    },
    {
      key: 'records',
      label: '访问记录',
      children: (
        <Card className="table-container">
          <Table
            columns={accessRecordColumns}
            dataSource={accessRecords}
            rowKey="createTime"
            loading={loading}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total) => `共 ${total} 条记录`,
            }}
            size="middle"
          />
        </Card>
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
          统计分析
        </Title>
        <Button
          type="primary"
          onClick={handleRefresh}
          loading={loading}
        >
          刷新数据
        </Button>
      </div>

      <Card style={{ marginBottom: 24 }}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={8} md={6}>
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
                选择分组
              </label>
              <Select
                style={{ width: '100%' }}
                placeholder="请选择分组"
                value={selectedGroup}
                onChange={handleGroupChange}
              >
                {groups.map(group => (
                  <Option key={group.gid} value={group.gid}>
                    {group.name}
                  </Option>
                ))}
              </Select>
            </div>
          </Col>
          <Col xs={24} sm={8} md={6}>
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
                选择短链接
              </label>
              <Select
                style={{ width: '100%' }}
                placeholder="请选择短链接"
                value={selectedShortLink || ""}
                onChange={handleShortLinkChange}
                disabled={!selectedGroup}
              >
                <Option key="all" value="">
                  全部
                </Option>
                {filteredShortLinks.map(link => (
                  <Option key={link.fullShortUrl} value={link.fullShortUrl}>
                    {link.describe || link.shortUri}
                  </Option>
                ))}
              </Select>
            </div>
          </Col>
          <Col xs={24} sm={8} md={6}>
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
                时间范围
              </label>
              <RangePicker
                style={{ width: '100%' }}
                value={dateRange}
                onChange={handleDateRangeChange}
                format="YYYY-MM-DD"
                placeholder={['开始日期', '结束日期']}
              />
            </div>
          </Col>
          <Col xs={24} sm={8} md={6}>
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
                包含回收站
              </label>
              <Switch
                checked={includeRecycle}
                onChange={setIncludeRecycle}
                checkedChildren="是"
                unCheckedChildren="否"
              />
              <div style={{ fontSize: 12, color: '#666', marginTop: 4 }}>
                是否包含已移入回收站的短链接
              </div>
            </div>
          </Col>
        </Row>
      </Card>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
        </div>
      ) : (
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={tabItems}
        />
      )}
    </div>
  );
};

export default ShortLinkStats;
