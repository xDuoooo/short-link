import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { statsApi } from '../../api/stats';

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
}

export interface StatsState {
  statsData: StatsData | null;
  accessRecords: AccessRecord[];
  total: number;
  loading: boolean;
  error: string | null;
  currentPage: number;
  pageSize: number;
}

const initialState: StatsState = {
  statsData: null,
  accessRecords: [],
  total: 0,
  loading: false,
  error: null,
  currentPage: 1,
  pageSize: 10,
};

export const getShortLinkStats = createAsyncThunk(
  'stats/getShortLinkStats',
  async (params: {
    fullShortUrl: string;
    gid: string;
    startDate: string;
    endDate: string;
  }) => {
    const response = await statsApi.getShortLinkStats(params);
    return response;
  }
);

export const getGroupShortLinkStats = createAsyncThunk(
  'stats/getGroupShortLinkStats',
  async (params: {
    gid: string;
    startDate: string;
    endDate: string;
  }) => {
    const response = await statsApi.getGroupShortLinkStats(params);
    return response;
  }
);

export const getShortLinkAccessRecords = createAsyncThunk(
  'stats/getShortLinkAccessRecords',
  async (params: {
    fullShortUrl: string;
    gid: string;
    startDate: string;
    endDate: string;
    current?: number;
    size?: number;
  }) => {
    const response = await statsApi.getShortLinkAccessRecords(params);
    return response;
  }
);

export const getGroupShortLinkAccessRecords = createAsyncThunk(
  'stats/getGroupShortLinkAccessRecords',
  async (params: {
    gid: string;
    startDate: string;
    endDate: string;
    current?: number;
    size?: number;
  }) => {
    const response = await statsApi.getGroupShortLinkAccessRecords(params);
    return response;
  }
);

const statsSlice = createSlice({
  name: 'stats',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setCurrentPage: (state, action: PayloadAction<number>) => {
      state.currentPage = action.payload;
    },
    setPageSize: (state, action: PayloadAction<number>) => {
      state.pageSize = action.payload;
    },
    clearStatsData: (state) => {
      state.statsData = null;
      state.accessRecords = [];
      state.total = 0;
    },
  },
  extraReducers: (builder) => {
    builder
      // Get Short Link Stats
      .addCase(getShortLinkStats.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getShortLinkStats.fulfilled, (state, action) => {
        state.loading = false;
        state.statsData = action.payload || null;
      })
      .addCase(getShortLinkStats.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '获取统计数据失败';
      })
      // Get Group Short Link Stats
      .addCase(getGroupShortLinkStats.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getGroupShortLinkStats.fulfilled, (state, action) => {
        state.loading = false;
        state.statsData = action.payload || null;
      })
      .addCase(getGroupShortLinkStats.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '获取分组统计数据失败';
      })
      // Get Short Link Access Records
      .addCase(getShortLinkAccessRecords.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getShortLinkAccessRecords.fulfilled, (state, action) => {
        state.loading = false;
        state.accessRecords = action.payload?.records || [];
        state.total = action.payload?.total || 0;
      })
      .addCase(getShortLinkAccessRecords.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '获取访问记录失败';
      })
      // Get Group Short Link Access Records
      .addCase(getGroupShortLinkAccessRecords.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getGroupShortLinkAccessRecords.fulfilled, (state, action) => {
        state.loading = false;
        state.accessRecords = action.payload?.records || [];
        state.total = action.payload?.total || 0;
      })
      .addCase(getGroupShortLinkAccessRecords.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '获取分组访问记录失败';
      });
  },
});

export const { clearError, setCurrentPage, setPageSize, clearStatsData } = statsSlice.actions;
export default statsSlice.reducer;
