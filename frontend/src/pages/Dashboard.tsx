import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Typography, Spin, Avatar } from 'antd';
import { LinkOutlined, TeamOutlined, EyeOutlined, FireOutlined, BarChartOutlined, TrophyOutlined, StarOutlined, ClockCircleOutlined, FolderOutlined, GlobalOutlined } from '@ant-design/icons';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store';
import { fetchGroups } from '../store/slices/groupSlice';
import { fetchShortLinks, getGroupShortLinkCount, batchFetchShortLinks } from '../store/slices/shortLinkSlice';

const { Title } = Typography;

// Favicon组件
const FaviconDisplay: React.FC<{ favicon?: string; size?: number }> = ({ favicon, size = 16 }) => {
  if (!favicon) {
    return <Avatar size={size} icon={<GlobalOutlined />} />;
  }

  return (
    <Avatar 
      size={size} 
      src={favicon}
      icon={<GlobalOutlined />}
    />
  );
};

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
      // 使用批量查询接口，一次性获取所有分组的短链接数据
      const fetchAllGroupShortLinks = async () => {
        try {
          const gids = groups.map(group => group.gid);
          const result = await dispatch(batchFetchShortLinks({ 
            gids,
            current: 1, 
            size: 50, // 总共获取50条
            orderTag: 'create_time' 
          }));
          
          if (result.payload && (result.payload as any).records) {
            const allShortLinks = (result.payload as any).records;
            
            // 按创建时间排序，取前10条
            const sortedLinks = allShortLinks
              .sort((a: any, b: any) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
              .slice(0, 10);
            
            // 更新Redux状态
            dispatch({
              type: 'shortLink/batchFetchShortLinks/fulfilled',
              payload: {
                records: sortedLinks,
                total: allShortLinks.length,
                current: 1,
                size: 10
              }
            });
          }
        } catch (error) {
          console.error('获取短链接数据失败:', error);
        }
      };
      
      fetchAllGroupShortLinks();
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
  const totalPv = (shortLinks || []).reduce((sum, link) => sum + (link.totalPv || 0), 0);
  const todayPv = (shortLinks || []).reduce((sum, link) => sum + (link.todayPv || 0), 0);
  const activeShortLinks = (shortLinks || []).filter(link => (link.totalPv || 0) > 0).length;
  const avgPvPerLink = shortLinkTotal > 0 ? Math.round(totalPv / shortLinkTotal) : 0;
  
  // 找出最热门的短链接
  const topShortLink = (shortLinks || []).reduce((max, link) => 
    (link.totalPv || 0) > (max.totalPv || 0) ? link : max, 
    { totalPv: 0, describe: '暂无', fullShortUrl: '', favicon: '' }
  );

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
              title="今日访问量"
              value={todayPv}
              prefix={<BarChartOutlined />}
              valueStyle={{ color: '#fff' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={8}>
          <Card className="stats-card" style={{ height: '100%' }}>
            <Statistic
              title="活跃短链接"
              value={activeShortLinks}
              prefix={<FireOutlined />}
              valueStyle={{ color: '#fff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card className="stats-card" style={{ height: '100%' }}>
            <Statistic
              title="平均访问量"
              value={avgPvPerLink}
              prefix={<TrophyOutlined />}
              valueStyle={{ color: '#fff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={24} lg={8}>
          <Card className="stats-card" style={{ height: '100%' }}>
            <div style={{ color: '#fff', height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
              <div style={{ fontSize: 14, marginBottom: 8, opacity: 0.8, display: 'flex', alignItems: 'center' }}>
                <StarOutlined style={{ marginRight: 6, color: '#ffd700' }} />
                最热门短链接
              </div>
              <div style={{ fontSize: 16, fontWeight: 500 }}>
                {topShortLink.totalPv > 0 ? (
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', marginBottom: 4 }}>
                      <FaviconDisplay favicon={topShortLink.favicon} size={20} />
                      <div style={{ marginLeft: 8, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', flex: 1 }}>
                        {topShortLink.describe || '无描述'}
                      </div>
                    </div>
                    <div style={{ fontSize: 12, opacity: 0.7, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', marginLeft: 28 }}>
                      {topShortLink.fullShortUrl} - {topShortLink.totalPv} 次访问
                    </div>
                  </div>
                ) : (
                  <div style={{ opacity: 0.7 }}>暂无访问数据</div>
                )}
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card 
            title={
              <span style={{ display: 'flex', alignItems: 'center' }}>
                <ClockCircleOutlined style={{ marginRight: 8, color: '#1890ff' }} />
                最近创建的短链接
              </span>
            } 
            className="chart-container"
          >
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
                    <div style={{ display: 'flex', alignItems: 'center', flex: 1 }}>
                      <FaviconDisplay favicon={link.favicon} size={20} />
                      <div style={{ marginLeft: 8, flex: 1 }}>
                        <div style={{ fontWeight: 500, marginBottom: 4 }}>
                          {link.describe || '无描述'}
                        </div>
                        <div style={{ color: '#666', fontSize: 12 }}>
                          {link.fullShortUrl}
                        </div>
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
          <Card 
            title={
              <span style={{ display: 'flex', alignItems: 'center' }}>
                <FolderOutlined style={{ marginRight: 8, color: '#52c41a' }} />
                分组概览（按短链接数量排序）
              </span>
            } 
            className="chart-container"
          >
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
