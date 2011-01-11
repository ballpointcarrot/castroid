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

import java.net.MalformedURLException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.Toast;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.dialogs.DownloadDialog;

public class Castroid extends Activity {
	public static final String TAG = "Castroid";

	// constants for the menus
	static final int MENU_FEED_DELETE = 1;
	// TODO: This notation is weird... I'd rather see
	// static final int MENU_ITEM_DOWNLOAD = 2;
	static final int MENU_ITEM_DOWNLOAD = MENU_FEED_DELETE + 1;

	// Referenced Widgets
	protected Button mBtnAdd;
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
			int feedID = cursor.getInt(cursor.getColumnIndex(Feed._ID));
			Uri queryUri = ContentUris.withAppendedId(Feed.CONTENT_URI, feedID);
			getContentResolver().delete(queryUri, null, null);
			return true;
		case MENU_ITEM_DOWNLOAD:
			ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item
					.getMenuInfo();
			long itemID = info.id;
			Bundle bdl = new Bundle();
			bdl.putString("URI", getDownloadLink(itemID));
			showDialog(PROGRESS_DIALOG_ID, bdl);
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
		Cursor c = getContentResolver().query(queryUri,
				new String[] { Item._ID, Item.ENC_LINK, Item.ENC_SIZE }, null,
				null, null);

		c.moveToFirst();
		String dlLnk = c.getString(c.getColumnIndex(Item.ENC_LINK));
		c.close();
		return dlLnk;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dialog = null;
		ProgressDialog pd;
		switch (id) {
		case PROGRESS_DIALOG_ID:
			Uri downloadUri = Uri.parse(args.getString("URI"));
			pd = new DownloadDialog(this, downloadUri);
			dialog = pd;
			break;
		default:
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}

	protected void playStream(long itemId) {
		Intent intent = new Intent(this, MediaStreamer.class);
		intent.putExtra(MediaStreamer.ITEM_ID, itemId);
		startActivity(intent);
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
					menu.add(group, MENU_FEED_DELETE, 0, R.string.menu_delete);
				}
				break;
			default:
				Log.e(TAG, "Error in selecting packed position.");
			}
		}
	}
}
