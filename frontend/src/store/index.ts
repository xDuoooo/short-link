import { configureStore } from '@reduxjs/toolkit';
import authSlice from './slices/authSlice';
import groupSlice from './slices/groupSlice';
import shortLinkSlice from './slices/shortLinkSlice';
import statsSlice from './slices/statsSlice';
import recycleBinSlice from './slices/recycleBinSlice';

export const store = configureStore({
  reducer: {
    auth: authSlice,
    group: groupSlice,
    shortLink: shortLinkSlice,
    stats: statsSlice,
    recycleBin: recycleBinSlice,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
