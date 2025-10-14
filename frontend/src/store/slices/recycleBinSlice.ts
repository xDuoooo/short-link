import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { recycleBinApi } from '../../api/recycleBin';

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

export interface RecycleBinState {
  shortLinks: RecycleBinShortLink[];
  total: number;
  loading: boolean;
  error: string | null;
  currentPage: number;
  pageSize: number;
}

const initialState: RecycleBinState = {
  shortLinks: [],
  total: 0,
  loading: false,
  error: null,
  currentPage: 1,
  pageSize: 10,
};

export const fetchRecycleBinShortLinks = createAsyncThunk(
  'recycleBin/fetchRecycleBinShortLinks',
  async (params: {
    gid?: string;
    current?: number;
    size?: number;
    orderTag?: string;
  }) => {
    const response = await recycleBinApi.getRecycleBinShortLinks(params);
    return response;
  }
);

export const saveToRecycleBin = createAsyncThunk(
  'recycleBin/saveToRecycleBin',
  async (data: { gid: string; fullShortUrl: string }) => {
    await recycleBinApi.saveToRecycleBin(data);
  }
);

export const recoverFromRecycleBin = createAsyncThunk(
  'recycleBin/recoverFromRecycleBin',
  async (data: { gid: string; fullShortUrl: string }) => {
    await recycleBinApi.recoverFromRecycleBin(data);
  }
);

export const removeFromRecycleBin = createAsyncThunk(
  'recycleBin/removeFromRecycleBin',
  async (data: { gid: string; fullShortUrl: string }) => {
    await recycleBinApi.removeFromRecycleBin(data);
  }
);

const recycleBinSlice = createSlice({
  name: 'recycleBin',
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
  },
  extraReducers: (builder) => {
    builder
      // Fetch Recycle Bin Short Links
      .addCase(fetchRecycleBinShortLinks.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchRecycleBinShortLinks.fulfilled, (state, action) => {
        state.loading = false;
        state.shortLinks = action.payload?.records || [];
        state.total = action.payload?.total || 0;
      })
      .addCase(fetchRecycleBinShortLinks.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '获取回收站短链接列表失败';
      })
      // Save to Recycle Bin
      .addCase(saveToRecycleBin.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(saveToRecycleBin.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(saveToRecycleBin.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '移至回收站失败';
      })
      // Recover from Recycle Bin
      .addCase(recoverFromRecycleBin.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(recoverFromRecycleBin.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(recoverFromRecycleBin.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '恢复短链接失败';
      })
      // Remove from Recycle Bin
      .addCase(removeFromRecycleBin.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(removeFromRecycleBin.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(removeFromRecycleBin.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '彻底删除短链接失败';
      });
  },
});

export const { clearError, setCurrentPage, setPageSize } = recycleBinSlice.actions;
export default recycleBinSlice.reducer;
