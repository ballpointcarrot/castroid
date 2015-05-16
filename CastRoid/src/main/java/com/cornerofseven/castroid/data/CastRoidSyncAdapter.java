package com.cornerofseven.castroid.data;


import android.accounts.Account;
import android.content.*;
import android.os.Bundle;
import android.util.Log;

import com.cornerofseven.castroid.Castroid;
import com.cornerofseven.castroid.rss.MalformedRSSException;

import java.net.MalformedURLException;

public class CastRoidSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String KEY_FEED_ID = "key.feed.id";
    public static final String SYNC_ACCOUNT = "com.cornerofsever.castroid";

    private final ContentResolver mResolver;

    private UpdateChannel mUpdateHelper;

    public CastRoidSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mResolver = context.getContentResolver();
        mUpdateHelper = new UpdateChannel(mResolver);
    }

    public CastRoidSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mResolver = context.getContentResolver();
        mUpdateHelper = new UpdateChannel(mResolver);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final int unknownId = -1;
        final int id;
        id = extras.getInt(KEY_FEED_ID, unknownId);

        if (id != unknownId) {
            try {
                mUpdateHelper.runUpdate(id);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (MalformedRSSException e) {
                e.printStackTrace();
            }

        } else {
            Log.e(Castroid.TAG, "No feed id found to sync!");
        }
    }

    public static Bundle createManualUpdateExtras(int feedId) {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        settingsBundle.putInt(KEY_FEED_ID, feedId);

        return settingsBundle;
    }
}
