import api from './index';

export interface Group {
  gid: string;
  name: string;
  username: string;
  sortOrder: number;
  createTime: string;
  updateTime: string;
  delFlag: number;
}

export interface CreateGroupRequest {
  name: string;
}

export interface UpdateGroupRequest {
  gid: string;
  name: string;
}

export interface OrderGroupRequest {
  gid: string;
  sortOrder: number;
}

export const groupApi = {
  // 获取分组列表
  getGroups: (): Promise<Group[]> => {
    return api.get('/api/short-link/admin/v1/group');
  },

  // 创建分组
  createGroup: (data: CreateGroupRequest): Promise<void> => {
    return api.post('/api/short-link/admin/v1/group', data);
  },

  // 更新分组
  updateGroup: (data: UpdateGroupRequest): Promise<void> => {
    return api.put('/api/short-link/admin/v1/group', data);
  },

  // 删除分组
  deleteGroup: (gid: string): Promise<void> => {
    return api.delete('/api/short-link/admin/v1/group', {
      params: { gid },
    });
  },

  // 排序分组
  orderGroups: (data: OrderGroupRequest[]): Promise<void> => {
    return api.post('/api/short-link/admin/v1/group/sort', data);
  },
};
