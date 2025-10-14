import api from './index';

export interface RecycleBinShortLink {
  gid: string;
  originUrl: string;
  domain: string;
  shortUri: string;
  fullShortUrl: string;
  createdType: number;
  validDateType: number;
  validDate: string;
  describe: string;
  shortLinkCount: number;
  enableStatus: number;
  clickNum: number;
  todayPv: number;
  todayUv: number;
  totalPv: number;
  totalUv: number;
  totalUip: number;
  createTime: string;
  updateTime: string;
  delTime: number;
  delFlag: number;
}

export interface RecycleBinPageResponse {
  records: RecycleBinShortLink[];
  total: number;
  current: number;
  size: number;
}

export interface GetRecycleBinShortLinksRequest {
  gid?: string;
  current?: number;
  size?: number;
  orderTag?: string;
}

export interface SaveToRecycleBinRequest {
  gid: string;
  fullShortUrl: string;
}

export interface RecoverFromRecycleBinRequest {
  gid: string;
  fullShortUrl: string;
}

export interface RemoveFromRecycleBinRequest {
  gid: string;
  fullShortUrl: string;
}

export const recycleBinApi = {
  // 获取回收站短链接列表
  getRecycleBinShortLinks: (params: GetRecycleBinShortLinksRequest): Promise<RecycleBinPageResponse> => {
    return api.get('/api/short-link/admin/v1/recycle-bin/page', { params });
  },

  // 保存到回收站
  saveToRecycleBin: (data: SaveToRecycleBinRequest): Promise<void> => {
    return api.post('/api/short-link/admin/v1/recycle-bin/save', data);
  },

  // 从回收站恢复
  recoverFromRecycleBin: (data: RecoverFromRecycleBinRequest): Promise<void> => {
    return api.post('/api/short-link/admin/v1/recycle-bin/recover', data);
  },

  // 从回收站彻底删除
  removeFromRecycleBin: (data: RemoveFromRecycleBinRequest): Promise<void> => {
    return api.post('/api/short-link/admin/v1/recycle-bin/remove', data);
  },
};
