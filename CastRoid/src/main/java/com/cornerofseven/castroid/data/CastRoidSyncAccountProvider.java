package com.cornerofseven.castroid.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * Singleton to represent the sync account. Will automatically
 * create an account object.
 */
public class CastRoidSyncAccountProvider {
    private static final Object sSyncAccountLock = new Object();
    private static Account sAccount;
    public static final String ACCOUNT_NAME = "com.cornerofseven.castroid";

    //Hidden Constructor
    private CastRoidSyncAccountProvider() {
    }

    /**
     * @param context
     * @return the sync accunt to use to sync podcasts.
     */
    public static Account getSyncAccount(final Context context) {
        synchronized (sSyncAccountLock) {
            if (sAccount == null) {
                createSyncAccount(context);
            }
            return sAccount;
        }
    }

    /**
     * Test utility method. Can be used to stub in mocks for testing.
     *
     * @param account
     */
    protected static void setSyncAccount(Account account) {
        synchronized (sSyncAccountLock) {
            sAccount = account;
        }
    }

    /**
     * Helper method to create a syncAccount. Assumes the {@link #sSyncAccountLock} has been acquired by the caller. (Relatively safe assumption since this class only has a handfull of methods.)
     */
    private static void createSyncAccount(final Context context) {
        final String authority = Feed.BASE_AUTH;
        final String accountType = CastRoidSyncAdapter.SYNC_ACCOUNT;
        final String account = "castroid";

        final Account newAccount = new Account(
                account, accountType
        );
        final AccountManager accountManager =
                (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);

        //FIXME: Check to see if needs to be added?
        accountManager.addAccountExplicitly(newAccount, null, null);

        sAccount = newAccount;
    }
}
