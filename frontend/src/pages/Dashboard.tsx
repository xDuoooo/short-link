import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Typography, Spin } from 'antd';
import { LinkOutlined, TeamOutlined, EyeOutlined, UserOutlined } from '@ant-design/icons';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store';
import { fetchGroups } from '../store/slices/groupSlice';
import { fetchShortLinks, getGroupShortLinkCount } from '../store/slices/shortLinkSlice';

const { Title } = Typography;

const Dashboard: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { groups, loading: groupLoading } = useSelector((state: RootState) => state.group);
  const { shortLinks, total: shortLinkTotal, loading: shortLinkLoading } = useSelector((state: RootState) => state.shortLink);
  const [groupCounts, setGroupCounts] = useState<{ [gid: string]: number }>({});

  useEffect(() => {
    dispatch(fetchGroups());
  }, [dispatch]);

  // 当分组加载完成后，获取所有分组的短链接
  useEffect(() => {
    if (groups.length > 0) {
      // 获取所有分组的短链接（不传gid参数），按创建时间排序
      dispatch(fetchShortLinks({ current: 1, size: 10, orderTag: 'create_time' }));
    }
  }, [groups, dispatch]);

  // 获取分组短链接数量
  useEffect(() => {
    if (groups.length > 0) {
      const gids = groups.map(group => group.gid);
      dispatch(getGroupShortLinkCount(gids)).then((result) => {
        if (result.payload) {
          const counts: { [gid: string]: number } = {};
          (result.payload as any[]).forEach((item: any) => {
            counts[item.gid] = item.shortLinkCount;
          });
          setGroupCounts(counts);
        }
      });
    }
  }, [groups, dispatch]);

  // 计算统计数据
  // const totalClicks = (shortLinks || []).reduce((sum, link) => sum + (link.clickNum || 0), 0);
  const totalPv = (shortLinks || []).reduce((sum, link) => sum + (link.totalPv || 0), 0);
  const totalUv = (shortLinks || []).reduce((sum, link) => sum + (link.totalUv || 0), 0);

  // 按短链接数量排序分组
  const sortedGroups = [...groups].sort((a, b) => {
    const countA = groupCounts[a.gid] || 0;
    const countB = groupCounts[b.gid] || 0;
    return countB - countA; // 降序排列
  });

  if (groupLoading || shortLinkLoading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <Title level={2} style={{ marginBottom: 24 }}>
        仪表盘
      </Title>
      
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card className="stats-card">
            <Statistic
              title="分组数量"
              value={groups.length}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#fff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="stats-card">
            <Statistic
              title="短链接数量"
              value={shortLinkTotal}
              prefix={<LinkOutlined />}
              valueStyle={{ color: '#fff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="stats-card">
            <Statistic
              title="总访问量"
              value={totalPv}
              prefix={<EyeOutlined />}
              valueStyle={{ color: '#fff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="stats-card">
            <Statistic
              title="独立访客"
              value={totalUv}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#fff' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="最近创建的短链接" className="chart-container">
            {shortLinks.length > 0 ? (
              <div>
                {shortLinks.slice(0, 5).map((link) => (
                  <div key={link.gid} style={{ 
                    padding: '12px 0', 
                    borderBottom: '1px solid #f0f0f0',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}>
                    <div>
                      <div style={{ fontWeight: 500, marginBottom: 4 }}>
                        {link.describe || '无描述'}
                      </div>
                      <div style={{ color: '#666', fontSize: 12 }}>
                        {link.fullShortUrl}
                      </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ color: '#1890ff', fontWeight: 500 }}>
                        {link.totalPv > 0 ? (
                          <div>
                            <div>{link.totalPv} 次访问</div>
                            {link.todayPv > 0 && (
                              <div style={{ fontSize: 12, color: '#52c41a' }}>
                                今日 {link.todayPv} 次
                              </div>
                            )}
                          </div>
                        ) : (
                          <div style={{ color: '#999' }}>暂无访问</div>
                        )}
                      </div>
                      <div style={{ color: '#999', fontSize: 12 }}>
                        {new Date(link.createTime).toLocaleDateString()}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div style={{ textAlign: 'center', color: '#999', padding: '20px' }}>
                暂无短链接
              </div>
            )}
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="分组概览（按短链接数量排序）" className="chart-container">
            {sortedGroups.length > 0 ? (
              <div>
                {sortedGroups.slice(0, 5).map((group) => (
                  <div key={group.gid} style={{ 
                    padding: '12px 0', 
                    borderBottom: '1px solid #f0f0f0',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}>
                    <div>
                      <div style={{ fontWeight: 500, marginBottom: 4 }}>
                        {group.name}
                      </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ color: '#1890ff', fontWeight: 500 }}>
                        {groupCounts[group.gid] || 0} 个短链接
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div style={{ textAlign: 'center', color: '#999', padding: '20px' }}>
                暂无分组
              </div>
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
