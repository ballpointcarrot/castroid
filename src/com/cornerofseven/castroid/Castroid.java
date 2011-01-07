/**
  Copyright 2010 Christopher Kruse and Sean Mooney

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License
*/

package com.cornerofseven.castroid;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorTreeAdapter;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.dialogs.ProgressBarHandler;
import com.cornerofseven.castroid.network.DownloadManager;

public class Castroid extends Activity {

    public static final String TAG = "Castroid";

    // constants for the menus
    static final int MENU_FEED_DELETE = 1;
    static final int MENU_ITEM_DOWNLOAD = MENU_FEED_DELETE + 1;

    protected Button mBtnAdd;
    // protected ListView mFeedList;e
    protected ExpandableListView mPodcastTree;

    // indexes for projection arrays for data adapter (e.g. FEED_PROJECTION in
    // onCreate())
    static final int FEED_ID_COLUMN = 0;
    static final int FEED_TITLE_COLUMN = 1;

    // DIALOG IDs
    public static final int PROGRESS_DIALOG_ID = 1;

    protected ProgressDialog mProgressDialog = null;

    // The media player to use for playing podcasts.
    // protected MediaPlayer mMediaPlayer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mBtnAdd = (Button) findViewById(R.id.btn_add_podcast);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View v) {
                addFeed();
            }
        });

        final String[] FEED_PROJECTION = { Feed._ID, Feed.TITLE,
            Feed.DESCRIPTION, Feed.LINK };
        Cursor c = managedQuery(Feed.CONTENT_URI, FEED_PROJECTION, null, null,
                null);
        c.setNotificationUri(getContentResolver(), Feed.CONTENT_URI);

        final int GROUP_LAYOUT = android.R.layout.simple_expandable_list_item_1;
        final String[] GROUP_FROM = { Feed.TITLE };
        final int[] GROUP_TO = { android.R.id.text1 };
        final int CHILD_LAYOUT = android.R.layout.simple_expandable_list_item_1;
        final String[] CHILD_FROM = { Item.TITLE };
        final int[] CHILD_TO = { android.R.id.text1 };

        mPodcastTree = ((ExpandableListView) findViewById(R.id.podcastList));
        // pull a local ref for better performance
        final ExpandableListView podcastTree = mPodcastTree;
        podcastTree.setAdapter(new SimpleCursorTreeAdapter(this, c,
                    GROUP_LAYOUT, GROUP_FROM, GROUP_TO, CHILD_LAYOUT, CHILD_FROM,
                    CHILD_TO) {

            @Override
            protected Cursor getChildrenCursor(Cursor groupCursor) {
                final String[] PROJECTION = new String[] { Item._ID,
                        Item.OWNER, Item.TITLE, Item.LINK, Item.DESC };
                final String SELECT_ITEMS = Item.OWNER + " = ?";
                int feedId = groupCursor.getInt(groupCursor
                    .getColumnIndex(Feed._ID));
                String[] selectionArgs = new String[] { 
                    Integer.toString(feedId) };
                return managedQuery(Item.CONTENT_URI, PROJECTION, SELECT_ITEMS,
                    selectionArgs, Item.DEFAULT_SORT);
            }

        });

        podcastTree.setClickable(true);
        podcastTree.setOnCreateContextMenuListener(
            new View.OnCreateContextMenuListener() {

            @Override
            public final void onCreateContextMenu(ContextMenu menu,
                View paramView, ContextMenuInfo paramContextMenuInfo) {

                    ExpandableListView.ExpandableListContextMenuInfo info;

                    try {
                        info = (ExpandableListView.ExpandableListContextMenuInfo) paramContextMenuInfo;
                    } catch (ClassCastException e) {
                        Log.e(TAG, "bad menuInfo", e);
                        return;
                    }

                    int type = ExpandableListView.getPackedPositionType(
                        info.packedPosition);

                    if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                        createItemContextMenu(paramView, menu, info);
                    } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                        createChannelContextMenu(paramView, menu, info);
                    }
                }

            private final void createChannelContextMenu(View paramView,ContextMenu menu,
                ExpandableListView.ExpandableListContextMenuInfo info) 
            {
                // Setup the menu header
                ListAdapter list = podcastTree.getAdapter();
                int group = ExpandableListView.getPackedPositionGroup(
                    info.packedPosition);
                Cursor cursor = (Cursor) list.getItem(group);
                if (cursor == null) {
                    Log.d(TAG, "Null Group info cursor");
                    return;
                }
                menu.setHeaderTitle(cursor.getString(FEED_TITLE_COLUMN));

                // Add a menu item to delete the note
                menu.add(group, MENU_FEED_DELETE, 0,
                    R.string.menu_delete);
            }

            private final void createItemContextMenu(View paramView,ContextMenu menu,
                ExpandableListView.ExpandableListContextMenuInfo info) 
            {
                menu.setHeaderTitle("Item Options");
                menu.add(0, MENU_ITEM_DOWNLOAD, 0,R.string.menu_download);
            }
        });

        // Add a click listener to download a clicked on podcast
        // TODO: Is this the control flow we want?
        podcastTree
            .setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(
                    ExpandableListView paramExpandableListView,
                    View paramView, int paramInt1, int paramInt2,
                    long paramLong) {
                    // TODO Auto-generated method stub
                    // downloadItem(paramLong);
                    playStream(paramLong);
                    return true;
                    }
            });
    }

    protected void addFeed() {
        Intent intent = new Intent(this, NewFeed.class);
        startActivity(intent);
    }

    @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            super.onCreateOptionsMenu(menu);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
        }

    @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.addFeed:
                    addFeed();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

    @Override
        public boolean onContextItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_FEED_DELETE:
                    ListAdapter list = mPodcastTree.getAdapter();
                    Cursor cursor = (Cursor) list.getItem(item.getGroupId());
                    if (cursor == null) {
                        return false;
                    }
                    int feedID = cursor.getInt(FEED_ID_COLUMN);
                    Uri queryUri = ContentUris.withAppendedId(Feed.CONTENT_URI, feedID);
                    getContentResolver().delete(queryUri, null, null);
                    return true;
                case MENU_ITEM_DOWNLOAD:
                    ExpandableListView.ExpandableListContextMenuInfo info = 
                        (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
                    long itemID = info.id;
                    downloadItem(itemID);
            }
            return super.onContextItemSelected(item);
        }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
        protected Dialog onCreateDialog(int id) {
            Dialog dialog = null;
            ProgressDialog pd;
            switch (id) {
                case PROGRESS_DIALOG_ID:
                    pd = new ProgressDialog(this);

                    pd.setTitle(R.string.downloading);
                    pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pd.setCancelable(true);

                    pd.setOnCancelListener(new Dialog.OnCancelListener() {
                        @Override
                        public final void onCancel(DialogInterface paramDialogInterface) {
                            Thread dlt = mDownloadThread;
                            if (dlt != null) {
                                dlt.interrupt();
                                try {
                                    dlt.join(); // wait for the download thread to
                                    // really die
                                    // will cause a UI "wait for finish" if it takes too
                                    // long.
                                } catch (InterruptedException e) {
                                }
                                mDownloadThread = null;
                            }
                        }
                    });

                    mProgressDialog = pd;
                    dialog = pd;
                    break;
                default:
                    dialog = super.onCreateDialog(id);
            }
            return dialog;
        }

    /**
     * Maintain a reference to the thread used for downloading. This field will
     * be changed everytime a download is started.
     */
    private DownloadThread mDownloadThread = null;

    /**
     * Download the selected item in a seperate thread.
     * 
     * @param itemId
     */
    protected void downloadItem(long itemId) {
        DownloadManager manager = new DownloadManager(this);
        DownloadThread dt = new DownloadThread(manager, itemId, progressHandler);
        mDownloadThread = dt;
        dt.start(); // spawn the thread off. raw threading, I feel a little
        // dirty...
    }

    protected void playStream(long itemId) {
        Intent intent = new Intent(this, MediaStreamer.class);
        intent.putExtra(MediaStreamer.ITEM_ID, itemId);
        startActivity(intent);
    }

    /**
     * A handler for updating/showing a progress.
     * 
     * The msg should set it's what field to any of the WHAT_* constants in
     * @see{com.cornerofsever.castroid.dialogs.ProgressBarHandler}.
     */
    final Handler progressHandler = new Handler() {
        @Override
            public final void handleMessage(Message msg) {
                switch (msg.what) {
                    case ProgressBarHandler.WHAT_START:
                        showDialog(PROGRESS_DIALOG_ID);
                        int max = msg.getData().getInt(ProgressBarHandler.PROGRESS_MAX);
                        mProgressDialog.setMax(max);
                        mProgressDialog.setProgress(0);
                        break;
                    case ProgressBarHandler.WHAT_UPDATE:
                        int total = msg.getData().getInt(
                                ProgressBarHandler.PROGRESS_UPDATE);
                        mProgressDialog.setProgress(total);
                        break;
                    case ProgressBarHandler.WHAT_DONE:
                        mProgressDialog.dismiss();
                        break;
                    default:
                        Log.e(TAG, "No case to for " + msg.what + " in handler.");
                }
            }
    };

    /**
     * Create a separate thread to run the down load on.
     * 
     * @author Sean Mooney
     * 
     */
    private class DownloadThread extends Thread {

        private DownloadManager mDownloader;
        private long mItemId;
        private Handler mHandler;

        public DownloadThread(DownloadManager downloader, long itemId,
                Handler handler) {
            super("DOWNLOAD-" + itemId);
            this.mDownloader = downloader;
            mItemId = itemId;
            mHandler = handler;
        }

        @Override
            public void run() {
                mDownloader.downloadItemEnc(mItemId, mHandler);
            }
    }
}
