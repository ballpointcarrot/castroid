package com.cornerofseven.castroid.data;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

/**
 * Controller for requests to the sync service.
 */
public class CastRoidSyncController {

    private Context mContext;

    public CastRoidSyncController(Context context) {
        this.mContext = context;
    }

    public void onManualSync(int feedId) {
        Account syncAccount = CastRoidSyncAccountProvider.getSyncAccount(mContext);
        Bundle settingsBundle = CastRoidSyncAdapter.createManualUpdateExtras(feedId);

        //Start a sync request on a separate thread.
        ContentResolver.requestSync(syncAccount, Feed.BASE_AUTH, settingsBundle);
    }
}
