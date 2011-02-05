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

import java.io.File;
import java.net.MalformedURLException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.Toast;

import com.cornerofseven.castroid.data.DatabaseQuery;
import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.data.PodcastDAO;
import com.cornerofseven.castroid.data.UpdateChannel;
import com.cornerofseven.castroid.dialogs.DownloadDialog;
import com.cornerofseven.castroid.handlers.ChannelItemClickHandler;
import com.cornerofseven.castroid.network.AsyncDownloadManager;
import com.cornerofseven.castroid.rss.MalformedRSSException;

public class Castroid extends Activity {
	public static final String TAG = "Castroid";

	// constants for the menus
	static final int MENU_FEED_DELETE = 1;
	
	/*COMMENT: Sean: Had borrowed the " = MENU_FEED_DELETE + 1"
	from an exaple or tutorial I saw. The advantage, is you can
	change a constant in the list (e.g. supoose for some reason
	 MENU_FEED_DELETE = 2), and not worry about duplicates.
	Everything increments properly. (Chris-you can drop this comment after reading)
	*/
	static final int MENU_ITEM_DOWNLOAD = 2;
	static final int MENU_FEED_UPDATE 	= 3;
	static final int MENU_FEED_VIEW 	= 4;
	static final int MENU_ITEM_PLAY		= 5;
	static final int MENU_ITEM_VIEW		= 6;
	
	// Referenced Widgets
	protected ExpandableListView mPodcastTree;

	// References to cursor columns
	static final String[] FEED_PROJECTION = { Feed._ID, Feed.TITLE,
			Feed.DESCRIPTION, Feed.LINK };
	static final String[] ITEM_PROJECTION = new String[] { Item._ID,
			Item.OWNER, Item.TITLE, Item.LINK, Item.DESC };

	// ExpandableListView formatting
	final int LAYOUT = android.R.layout.simple_expandable_list_item_1;
	final String[] FEED_FROM = { Feed.TITLE };
	final String[] CHILD_FROM = { Item.TITLE };
	final int[] TO = { android.R.id.text1 };

	// DIALOG IDs
	// TODO: Remove this, it shouldn't be needed.
	public static final int PROGRESS_DIALOG_ID = 1;
	public static final int UPDATE_PROGRESS_DIALOG_ID = 2;
	public static final int ABOUT_DIALOG_ID = 3;


	/**
	 * Reference to the dialog create on showDialog(PROGRESS_DIALOG_ID).
	 * Set when the dialog is created, and lets us get a hold of
	 * the dialog to give as a reference to other objects that need
	 * to update progress on the main screen.
	 */
	protected DownloadDialog mDownloadDialog;
	
	/**
	 * Dialog to use to display progress during feed updates.
	 */
	protected ProgressDialog mUpdateProgress;
	
	/**
	 * Used to download files on a seperate thread.
	 */
	protected AsyncDownloadManager mDownloadManager;
	
	/**
	 * Click handler for Channel items
	 */
	protected final ChannelItemClickHandler itemOnClickHandler = new ChannelItemClickHandler(this, MENU_ITEM_PLAY, MENU_ITEM_VIEW);
	
	//constants for any progress dialogs managed by handlers.
	public static final int WHAT_START = 1;
	public static final int WHAT_PREITEM = 2;
    public static final int WHAT_UPDATE = 3;
    public static final int WHAT_DONE = 4;
    public static final int WHAT_CANCELED = 5;
    
    //keys for the UpdateHandler data bundles
    public static final String PROGRESS_MAX = "MAX";
	public static final String PROGRESS_UPDATE = "UPDATE";
	public static final String PROGRESS_ITEMNAME = "NAME";
	
	// The media player to use for playing podcasts.
	// protected MediaPlayer mMediaPlayer;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mPodcastTree = ((ExpandableListView) findViewById(R.id.podcastList));

		Cursor c = managedQuery(Feed.CONTENT_URI, FEED_PROJECTION, null, null,
				null);
		c.setNotificationUri(getContentResolver(), Feed.CONTENT_URI);

		mPodcastTree.setAdapter(new SimpleCursorTreeAdapter(this, c, LAYOUT,
				FEED_FROM, TO, LAYOUT, CHILD_FROM, TO) {

			@Override
			protected Cursor getChildrenCursor(Cursor groupCursor) {

				final String SELECT_ITEMS = Item.OWNER + " = ?";
				int feedId = groupCursor.getInt(groupCursor
						.getColumnIndex(Feed._ID));
				String[] selectionArgs = new String[] { Integer
						.toString(feedId) };
				return managedQuery(Item.CONTENT_URI, ITEM_PROJECTION,
						SELECT_ITEMS, selectionArgs, Item.DEFAULT_SORT);
			}

		});

		mPodcastTree.setClickable(true);
		mPodcastTree
				.setOnCreateContextMenuListener(new PodcastTreeContextMenuListener());

		// Add a click listener to download a clicked on podcast
		// TODO: Is this the control flow we want?
		mPodcastTree
				.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
					@Override
					public boolean onChildClick(
							ExpandableListView paramExpandableListView,
							View paramView, int groupPos, int childPos,
							long itemId) {
						return itemOnClickHandler.onItemClick(MENU_ITEM_VIEW, itemId);
					}
				});
	}

	//persistance for on pause/resume
	//TODO: Is onPause/onResume persistence model correct. Feels like a hack.
	//private int groupPos = 0, itemPos = 0;
	@Override
	public void onPause(){
	    super.onPause();
//	    groupPos = mPodcastTree.get
//	    
//	    Log.i(TAG, "Pausing");
//	    Log.i(TAG, "Selected item " + listPos);
	}
	
	@Override
	public void onResume(){
	    super.onResume();
//	    
//	    mPodcastTree.setSelectedChild(groupPos, childPosition, shouldExpandGroup)
//	    
//	    Log.i(TAG, "Resuming");
//	    Log.i(TAG, "Restoring selection to " + listPos);
	}
	
	@Override
	public void onStop(){
		super.onStop();
		Log.i(TAG, "Stoping castroid");
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState){
	    super.onSaveInstanceState(outState);
	    Log.i(TAG, "Saving Stating");
	    if(mDownloadManager != null && mDownloadManager.getStatus() != AsyncTask.Status.FINISHED)
	        Log.w(TAG, "A download is in progress");
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
		case R.id.about:
			showDialog(ABOUT_DIALOG_ID);
			return true;
		case R.id.updateAll:
		    updateAllChannels();
		    return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		    case MENU_FEED_DELETE:
		    {
		    	ListAdapter list = mPodcastTree.getAdapter();
		        //TODO: Is there a better way that doesn't use a cursor?
		        Cursor cursor = (Cursor) list.getItem(item.getGroupId());
		        if (cursor == null) {
		            return false;
		        }
		        int feedID = cursor.getInt(cursor.getColumnIndex(Feed._ID));
		        Uri queryUri = ContentUris.withAppendedId(Feed.CONTENT_URI, feedID);
		        getContentResolver().delete(queryUri, null, null);
		        return true;
		    }
		    case MENU_ITEM_PLAY:
		    {
		    	ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item
		        .getMenuInfo();
		        long itemId = info.id;
		        //re-dispatch to the item click handler.
		        return itemOnClickHandler.onItemClick(MENU_ITEM_PLAY, itemId);
		    }
		    case MENU_ITEM_DOWNLOAD:
		    {
		        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item
		        .getMenuInfo();
		        long itemID = info.id;
		        File dlFolder = new File(Environment.getExternalStorageDirectory(), "Podcasts");
		        mDownloadManager = new AsyncDownloadManager(this, dlFolder);
		        mDownloadManager.execute(Uri.parse(getDownloadLink(itemID)));
		        return true;
		    }
		    case MENU_FEED_UPDATE:
		    {
		        ListAdapter list = mPodcastTree.getAdapter();
		        Cursor cursor = (Cursor) list.getItem(item.getGroupId());
		        if (cursor == null) {
		            return false;
		        }
		        int feedID = cursor.getInt(cursor.getColumnIndex(Feed._ID));
		        updateChannel(feedID);
		        return true;
		    }
		    case MENU_FEED_VIEW:
		    {
		    	ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item
		        .getMenuInfo();
		    	long feedId = info.id;
		    	Uri contentURI = ContentUris.withAppendedId(Feed.CONTENT_URI, feedId);
		    	/*
		    	 * TODO: Dispatch as without binding directly to the FeedInformationView
		    	 * Crashes. Android reports cannot find an activity for the intent.
		    	 * Intent viewFeedIntent = new Intent(Intent.ACTION_VIEW, contentURI);
		    	 */
		    	
		    	Intent viewFeedIntent = new Intent(this, FeedInformationView.class);
		    	viewFeedIntent.setData(contentURI);
		    	startActivity(viewFeedIntent);
		    	return true;
		    }
		    
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * Retrieves the Item's Enclosure download link.
	 * 
	 * @param itemID
	 *            the id of the item.
	 * @return the enclosure link.
	 */
	private String getDownloadLink(long itemID) {
		Uri queryUri = ContentUris.withAppendedId(Item.CONTENT_URI, itemID);
		Cursor c = managedQuery(queryUri,
				new String[] { Item._ID, Item.ENC_LINK, Item.ENC_SIZE }, null,
				null, null);

		c.moveToFirst();
		String dlLnk = c.getString(c.getColumnIndex(Item.ENC_LINK));
		return dlLnk;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		
		switch (id) {
		case PROGRESS_DIALOG_ID:
		{
		    DownloadDialog pd;
			//Uri downloadUri = Uri.parse(args.getString("URI"));
			pd = new DownloadDialog(this, null);
			pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface paramDialogInterface) {
                    if(mDownloadManager != null){
                        mDownloadManager.cancel(true);
                        mDownloadManager = null;
                    }
                }
            });
			
            pd.setTitle(R.string.downloading);
			mDownloadDialog = pd; //log so the application can find it later.
			return pd;
		}
		case UPDATE_PROGRESS_DIALOG_ID:
		{
		    //stack ref to the type information.
		    ProgressDialog upProg = new ProgressDialog(this);
		    upProg.setCancelable(true);
		    upProg.setTitle("Updating Feeds");
		   
		    //assign the reference to other places 
		    //that need aliases to the new dialog.
		    mUpdateProgress = upProg; //field reference to access this dialog from other places in the activity.
		    return upProg;
		}
		case ABOUT_DIALOG_ID:
		{
			Dialog aboutDialog = new Dialog(this);
			aboutDialog.setContentView(R.layout.about_dialog);
			aboutDialog.setTitle(R.string.aboutLabel);
			return aboutDialog;
		}
		default:
			return super.onCreateDialog(id);
		}		
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dlg){
	    switch(id){
	        case PROGRESS_DIALOG_ID:
	            ((ProgressDialog)dlg).setProgress(0);
	            break;
	        default: super.onPrepareDialog(id, dlg);
	    }
	}
//	
//	protected void playStream(long itemId) {
//		Uri itemUri = ContentUris.withAppendedId(Item.CONTENT_URI, itemId);
//		Cursor c = managedQuery(itemUri, 
//				new String[]{Item.ENC_LINK, Item.ENC_TYPE},
//				null, null, null);
//		
//		if(c.getCount() > 0){
//			String type, dataUri;
//
//			c.moveToFirst();
//			dataUri = c.getString(c.getColumnIndex(Item.ENC_LINK));
//			type = c.getString(c.getColumnIndex(Item.ENC_TYPE));
//			
//			Intent systemDefault = new Intent(Intent.ACTION_VIEW);
//			systemDefault.setDataAndType(Uri.parse(dataUri), type);
//			startActivity(systemDefault);
//		}else{
//			Toast.makeText(this, "No media found to play", Toast.LENGTH_LONG).show();
//		}
//	}
//	
	/**
	 * Update all the channels in the database.
	 * Runs asynchronously.
	 */
	protected void updateAllChannels(){
	    final Activity activity = this; //bind the context for the thread.

	    DatabaseQuery feedIdsQ = PodcastDAO.getFeedIdsQuery();
	    Cursor c = activity.managedQuery(
	            feedIdsQ.getContentUri(), 
	            feedIdsQ.getProjection(), 
	            feedIdsQ.getSelection(), 
	            feedIdsQ.getSelectionArgs(), 
	            feedIdsQ.getSortOrder());

	    //marshal the data for the updateChannel method.
	    Integer[] feedIds = new Integer[c.getCount()];
	    int curIndex = 0;
	    int feedCol = c.getColumnIndex(Feed._ID);
	    while(c.moveToNext()){
	        feedIds[curIndex++] = c.getInt(feedCol);
	    }

	    updateChannel(feedIds);
	}
	
	/**
     * Update the selected feed(s).
     * 
     *  Can update multiple feeds on the same call.
     *  Use this if/when the "update all" feature is added.
     *  
     * @param feedId
     */
    protected void updateChannel(Integer... feedId){
        new AsyncFeedUpdater().execute(feedId);
    }

    /**
     * Assuming the download dialog has been created, return its reference.
     * @return reference to the download dialog object.
     */
    public DownloadDialog getDownloadDialog(){
        return mDownloadDialog;
    }
    
    
    private class PodcastTreeContextMenuListener implements
			View.OnCreateContextMenuListener {
		/**
		 * Default Constructor
		 */
		public PodcastTreeContextMenuListener() {
		}

		/**
		 * @see android.view.View.OnCreateContextMenuListener#onCreateContextMenu(android.view.ContextMenu,
		 *      android.view.View, android.view.ContextMenu.ContextMenuInfo)
		 */
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			// Recover PodcastTree Menu Info.
			ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

			switch (ExpandableListView
					.getPackedPositionType(info.packedPosition)) {
			case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
				menu.setHeaderTitle("Item Options");
				menu.add(0, MENU_ITEM_DOWNLOAD, 0, R.string.menu_download);
				menu.add(0, MENU_ITEM_PLAY, 0, R.string.menu_play);
				break;
			case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
				int group = ExpandableListView
						.getPackedPositionGroup(info.packedPosition);
				ExpandableListAdapter ela = ((ExpandableListView) v)
						.getExpandableListAdapter();
				Cursor groupCursor = (Cursor) ela.getGroup(group);
				if (groupCursor != null) {
					menu.setHeaderTitle(groupCursor.getString(groupCursor
							.getColumnIndex(Feed.TITLE)));
					menu.add(group, MENU_FEED_VIEW, 0, R.string.menu_view_feed);
					menu.add(group, MENU_FEED_UPDATE, 0, R.string.menu_update);
					menu.add(group, MENU_FEED_DELETE, 0, R.string.menu_delete);
					
				}
				break;
			default:
				Log.e(TAG, "Error in selecting packed position.");
			}
		}
	}
    
    private Handler mUpdateHandler = new Handler(){
        /*
         * (non-Javadoc)
         * 
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            
            switch (msg.what) {
                case WHAT_START:
                {
                    showDialog(UPDATE_PROGRESS_DIALOG_ID);
                    
                    int max = msg.getData().getInt(PROGRESS_MAX);
                    mUpdateProgress.setMax(max);
                    mUpdateProgress.setProgress(0);
                    break;
                }
                case WHAT_PREITEM:
                {
                    Bundle data = msg.getData();
                    String currentItem = (data != null) ? data.getString(PROGRESS_ITEMNAME) : "";
                    mUpdateProgress.setMessage(currentItem);
                    break;
                }
                case WHAT_UPDATE:
                {   
                    int total = msg.getData().getInt(PROGRESS_UPDATE);
                    mUpdateProgress.setProgress(total);
                    break;
                }
                case WHAT_DONE:
                {  
                    dismissDialog(UPDATE_PROGRESS_DIALOG_ID);
                    break;
                }
            }
        }    
    };
    
    /**
     * An asynchronous task for updating a feed.
     * 
     * Currently in the castroid activity instead of its 
     * own class (ala DownloadManager) to simplify some of the scoping
     * issues with an external class.
     * @author Sean Mooney
     *
     */
    private class AsyncFeedUpdater extends AsyncTask<Integer, Integer, Integer>{

        
        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Integer doInBackground(Integer... feedIds) {
            int numUpdated = 0;
            
            
            UpdateChannel update = new UpdateChannel(getContentResolver());
            Bundle startData = new Bundle();
            startData.putInt(PROGRESS_MAX, feedIds.length);
            signalHandler(WHAT_START, new Bundle());
            for(int currentFeed : feedIds){
                if(isCancelled())break;
                    
                try {
                    
                    Bundle data = new Bundle();
                    data.putString(PROGRESS_ITEMNAME, "Feed " + currentFeed);
                    signalHandler(WHAT_PREITEM, data);
                    
                    update.runUpdate(currentFeed);
                } catch (MalformedURLException e) {
                    //TODO: Put the podcast title instead of id in the error message
                    String msg = "Unable to update " + currentFeed + "\n" + e.getMessage();
                    Log.w(TAG, msg);
                    //TODO: Warn someone
                    //Toast.makeText(this, msg , Toast.LENGTH_LONG);
                } catch (MalformedRSSException e) {
                  //TODO: Put the podcast title instead of id in the error message
                    String msg = "Unable to update " + currentFeed + "\n" + e.getMessage();
                    Log.w(TAG, msg);
                    //TODO: Warn someone
                    //Toast.makeText(this, msg , Toast.LENGTH_LONG);
                } finally{
                    publishProgress(++numUpdated);
                }
            }
            
            return numUpdated;
        }
        
        @Override
        public void onCancelled(){
            Bundle b = new Bundle();
            b.putBoolean(DownloadDialog.PROGRESS_DONE, false);
            signalHandler(DownloadDialog.WHAT_CANCELED, b);
        }
        
        @Override
        public void onProgressUpdate(Integer... progresses){
            Bundle b = new Bundle();
            b.putInt(DownloadDialog.PROGRESS_UPDATE, progresses[0]);
            signalHandler(DownloadDialog.WHAT_UPDATE, b);
        }
        
        @Override
        public void onPostExecute(Integer result){
            signalHandler(WHAT_DONE, null);
        }
        
        private void signalHandler(int msgType, Bundle data){
            Handler handler = mUpdateHandler;
            if(handler != null) {
                Message msg = handler.obtainMessage(msgType);
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }
    }
}
