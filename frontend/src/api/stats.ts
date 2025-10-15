import api from './index';

export interface StatsData {
  pv: number;
  uv: number;
  uip: number;
  todayPv?: number;
  todayUv?: number;
  todayUip?: number;
  daily?: Array<{
    date: string;
    pv: number;
    uv: number;
    uip: number;
  }>;
  hourStats?: number[];
  weekdayStats?: number[];
  deviceStats?: Array<{
    device: string;
    cnt: number;
    ratio: number;
  }>;
  browserStats?: Array<{
    browser: string;
    cnt: number;
    ratio: number;
  }>;
  osStats?: Array<{
    os: string;
    cnt: number;
    ratio: number;
  }>;
  networkStats?: Array<{
    network: string;
    cnt: number;
    ratio: number;
  }>;
  uvTypeStats?: Array<{
    uvType: string;
    cnt: number;
  }>;
  localeCnStats?: Array<{
    locale: string;
    cnt: number;
    ratio: number;
  }>;
}

export interface AccessRecord {
  user: string;
  ip: string;
  locale: string;
  os: string;
  browser: string;
  device: string;
  network: string;
  createTime: string;
  describe?: string;
  originUrl?: string;
}

export interface AccessRecordPageResponse {
  records: AccessRecord[];
  total: number;
  current: number;
  size: number;
}

export interface GetShortLinkStatsRequest {
  fullShortUrl: string;
  gid: string;
  startDate: string;
  endDate: string;
  includeRecycle?: boolean;
}

export interface GetGroupShortLinkStatsRequest {
  gid: string;
  startDate: string;
  endDate: string;
  includeRecycle?: boolean;
}

export interface GetShortLinkAccessRecordsRequest {
  fullShortUrl: string;
  gid: string;
  startDate: string;
  endDate: string;
  current?: number;
  size?: number;
  includeRecycle?: boolean;
}

export interface GetGroupShortLinkAccessRecordsRequest {
  gid: string;
  startDate: string;
  endDate: string;
  current?: number;
  size?: number;
  includeRecycle?: boolean;
}

export const statsApi = {
  // 获取单个短链接统计数据
  getShortLinkStats: (params: GetShortLinkStatsRequest): Promise<StatsData> => {
    return api.get('/api/short-link/admin/v1/stats', { params });
  },

  // 获取分组短链接统计数据
  getGroupShortLinkStats: (params: GetGroupShortLinkStatsRequest): Promise<StatsData> => {
    return api.get('/api/short-link/admin/v1/stats/group', { params });
  },

  // 获取单个短链接访问记录
  getShortLinkAccessRecords: (params: GetShortLinkAccessRecordsRequest): Promise<AccessRecordPageResponse> => {
    return api.get('/api/short-link/admin/v1/stats/access-record', { params });
  },

  // 获取分组短链接访问记录
  getGroupShortLinkAccessRecords: (params: GetGroupShortLinkAccessRecordsRequest): Promise<AccessRecordPageResponse> => {
    return api.get('/api/short-link/admin/v1/stats/access-record/group', { params });
  },
};
