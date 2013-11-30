package com.cornerofseven.castroid.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by smooney on 11/29/13.
 */
public class CastRoidSyncService extends Service {
    private static CastRoidSyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();
    private static final boolean AUTO_INIT = true;
    private static final boolean ALLOW_PARALLEL_SYNCS = true;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new CastRoidSyncAdapter(getApplicationContext(), AUTO_INIT, ALLOW_PARALLEL_SYNCS);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

}
