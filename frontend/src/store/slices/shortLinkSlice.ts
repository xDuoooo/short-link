import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { shortLinkApi } from '../../api/shortLink';

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

export interface ShortLinkState {
  shortLinks: ShortLink[];
  total: number;
  loading: boolean;
  error: string | null;
  currentPage: number;
  pageSize: number;
  includeRecycle: boolean;
}

const initialState: ShortLinkState = {
  shortLinks: [],
  total: 0,
  loading: false,
  error: null,
  currentPage: 1,
  pageSize: 10,
  includeRecycle: false,
};

export const fetchShortLinks = createAsyncThunk(
  'shortLink/fetchShortLinks',
  async (params: {
    gid?: string;
    current?: number;
    size?: number;
    orderTag?: string;
    includeRecycle?: boolean;
  }) => {
    const response = await shortLinkApi.getShortLinks(params);
    return response;
  }
);

export const createShortLink = createAsyncThunk(
  'shortLink/createShortLink',
  async (data: {
    gid: string;
    originUrl: string;
    domain: string;
    createdType: number;
    validDateType: number;
    validDate: string;
    describe: string;
  }) => {
    const response = await shortLinkApi.createShortLink(data);
    return response;
  }
);

export const batchCreateShortLink = createAsyncThunk(
  'shortLink/batchCreateShortLink',
  async (data: {
    gid: string;
    originUrls: string[];
    domain: string;
    createdType: number;
    validDateType: number;
    validDate: string;
    describe: string;
  }) => {
    const response = await shortLinkApi.batchCreateShortLink(data);
    return response;
  }
);

export const updateShortLink = createAsyncThunk(
  'shortLink/updateShortLink',
  async (data: {
    gid: string;
    fullShortUrl: string;
    originUrl: string;
    validDateType: number;
    validDate: string;
    describe: string;
    originGid: string;
  }) => {
    await shortLinkApi.updateShortLink(data);
  }
);

export const getTitleByUrl = createAsyncThunk(
  'shortLink/getTitleByUrl',
  async (url: string) => {
    const response = await shortLinkApi.getTitleByUrl(url);
    return response;
  }
);

export const getGroupShortLinkCount = createAsyncThunk(
  'shortLink/getGroupShortLinkCount',
  async (gids: string[]) => {
    const response = await shortLinkApi.getGroupShortLinkCount(gids);
    return response;
  }
);

const shortLinkSlice = createSlice({
  name: 'shortLink',
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
    setIncludeRecycle: (state, action: PayloadAction<boolean>) => {
      state.includeRecycle = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch Short Links
      .addCase(fetchShortLinks.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchShortLinks.fulfilled, (state, action) => {
        state.loading = false;
        state.shortLinks = action.payload?.records || [];
        state.total = action.payload?.total || 0;
      })
      .addCase(fetchShortLinks.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '获取短链接列表失败';
      })
      // Create Short Link
      .addCase(createShortLink.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createShortLink.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(createShortLink.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '创建短链接失败';
      })
      // Batch Create Short Link
      .addCase(batchCreateShortLink.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(batchCreateShortLink.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(batchCreateShortLink.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '批量创建短链接失败';
      })
      // Update Short Link
      .addCase(updateShortLink.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateShortLink.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(updateShortLink.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '更新短链接失败';
      });
  },
});

export const { clearError, setCurrentPage, setPageSize, setIncludeRecycle } = shortLinkSlice.actions;
export default shortLinkSlice.reducer;
