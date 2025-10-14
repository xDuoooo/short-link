import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { groupApi } from '../../api/group';

export interface Group {
  gid: string;
  name: string;
  username: string;
  sortOrder: number;
  createTime: string;
  updateTime: string;
  delFlag: number;
}

export interface GroupState {
  groups: Group[];
  loading: boolean;
  error: string | null;
}

const initialState: GroupState = {
  groups: [],
  loading: false,
  error: null,
};

export const fetchGroups = createAsyncThunk(
  'group/fetchGroups',
  async () => {
    const response = await groupApi.getGroups();
    return response;
  }
);

export const createGroup = createAsyncThunk(
  'group/createGroup',
  async (name: string) => {
    await groupApi.createGroup({ name });
  }
);

export const updateGroup = createAsyncThunk(
  'group/updateGroup',
  async (groupData: { gid: string; name: string }) => {
    await groupApi.updateGroup(groupData);
  }
);

export const deleteGroup = createAsyncThunk(
  'group/deleteGroup',
  async (gid: string) => {
    await groupApi.deleteGroup(gid);
  }
);

export const orderGroups = createAsyncThunk(
  'group/orderGroups',
  async (groups: { gid: string; sortOrder: number }[]) => {
    await groupApi.orderGroups(groups);
  }
);

const groupSlice = createSlice({
  name: 'group',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    updateGroupsOrder: (state, action: PayloadAction<Group[]>) => {
      state.groups = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch Groups
      .addCase(fetchGroups.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchGroups.fulfilled, (state, action) => {
        state.loading = false;
        state.groups = action.payload;
      })
      .addCase(fetchGroups.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '获取分组列表失败';
      })
      // Create Group
      .addCase(createGroup.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createGroup.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(createGroup.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '创建分组失败';
      })
      // Update Group
      .addCase(updateGroup.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateGroup.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(updateGroup.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '更新分组失败';
      })
      // Delete Group
      .addCase(deleteGroup.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteGroup.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(deleteGroup.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '删除分组失败';
      })
      // Order Groups
      .addCase(orderGroups.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(orderGroups.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(orderGroups.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '排序分组失败';
      });
  },
});

export const { clearError, updateGroupsOrder } = groupSlice.actions;
export default groupSlice.reducer;
