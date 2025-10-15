import api from './index';

export interface ShortLink {
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
  favicon: string;
}

export interface ShortLinkPageResponse {
  records: ShortLink[];
  total: number;
  current: number;
  size: number;
}

export interface CreateShortLinkRequest {
  gid: string;
  originUrl: string;
  domain: string;
  createdType: number;
  validDateType: number;
  validDate: string;
  describe: string;
}

export interface CreateShortLinkResponse {
  gid: string;
  originUrl: string;
  domain: string;
  shortUri: string;
  fullShortUrl: string;
  createdType: number;
  validDateType: number;
  validDate: string;
  describe: string;
}

export interface BatchCreateShortLinkRequest {
  gid: string;
  originUrls: string[];
  domain: string;
  createdType: number;
  validDateType: number;
  validDate: string;
  describe: string;
}

export interface BatchCreateShortLinkResponse {
  baseLinkInfos: {
    gid: string;
    originUrl: string;
    domain: string;
    shortUri: string;
    fullShortUrl: string;
    createdType: number;
    validDateType: number;
    validDate: string;
    describe: string;
  }[];
}

export interface UpdateShortLinkRequest {
  gid: string;
  fullShortUrl: string;
  originUrl: string;
  validDateType: number;
  validDate: string;
  describe: string;
  originGid: string;
}


export interface GetShortLinksRequest {
  gid?: string;
  current?: number;
  size?: number;
  orderTag?: string;
  includeRecycle?: boolean;
}

export interface GroupCountResponse {
  gid: string;
  shortLinkCount: number;
}

export const shortLinkApi = {
  // 获取短链接列表
  getShortLinks: (params: GetShortLinksRequest): Promise<ShortLinkPageResponse> => {
    return api.get('/api/short-link/admin/v1/page', { params });
  },

  // 创建短链接
  createShortLink: (data: CreateShortLinkRequest): Promise<CreateShortLinkResponse> => {
    return api.post('/api/short-link/admin/v1/create', data);
  },

  // 批量创建短链接
  batchCreateShortLink: (data: BatchCreateShortLinkRequest): Promise<BatchCreateShortLinkResponse> => {
    return api.post('/api/short-link/admin/v1/create/batch', data);
  },

  // 更新短链接
  updateShortLink: (data: UpdateShortLinkRequest): Promise<void> => {
    return api.post('/api/short-link/admin/v1/update', data);
  },

  // 根据URL获取标题
  getTitleByUrl: (url: string): Promise<string> => {
    return api.get('/api/short-link/admin/v1/title', {
      params: { url },
    });
  },

  // 获取分组短链接数量
  getGroupShortLinkCount: (gids: string[]): Promise<GroupCountResponse[]> => {
    return api.get('/api/short-link/admin/v1/count', {
      params: { gids: gids.join(',') },
    });
  },

};
