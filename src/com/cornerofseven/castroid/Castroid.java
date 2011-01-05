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


public class Castroid extends Activity {

    public static final String TAG = "Castroid";

    //constants for the menues
    static final int MENU_FEED_DELETE = 1;
    
    protected Button mBtnAdd;
    //protected ListView mFeedList;
    protected ExpandableListView mPodcastTree;
    
    
    //indexes for projection arrays for data adapter (e.g. FEED_PROJECTION in onCreat())
    static final int FEED_ID_COLUMN = 0;
    static final int FEED_TITLE_COLUMN = 1;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	mBtnAdd = (Button)findViewById(R.id.btn_add_podcast);
    	mBtnAdd.setOnClickListener(new View.OnClickListener() {
    		@Override
    		public final void onClick(View v) {
    			addFeed();
    		}
    	});

//    	mFeedList = (ListView)findViewById(R.id.main_feedlist);
    	final String[] FEED_PROJECTION ={
    			Feed._ID,
    			Feed.TITLE,
    			Feed.DESCRIPTION,
    			Feed.LINK
    	};
    	Cursor c = managedQuery(Feed.CONTENT_URI, 
    			FEED_PROJECTION, null, null, null);
    	c.setNotificationUri(getContentResolver(), Feed.CONTENT_URI);

    	final int GROUP_LAYOUT = android.R.layout.simple_expandable_list_item_1;
    	final String[] GROUP_FROM = {Feed.TITLE};
    	final int[] GROUP_TO = {android.R.id.text1};
    	final int CHILD_LAYOUT = android.R.layout.simple_expandable_list_item_1;
    	final String[] CHILD_FROM = {Item.TITLE};
    	final int[] CHILD_TO = {android.R.id.text1};
    	
    	mPodcastTree = ((ExpandableListView)findViewById(R.id.podcastList));
    	//pull a local ref for better performance
    	final ExpandableListView podcastTree = mPodcastTree;
    	
    	podcastTree.setAdapter(
    			new SimpleCursorTreeAdapter(this,c,
    					GROUP_LAYOUT, GROUP_FROM, GROUP_TO,
    					CHILD_LAYOUT, CHILD_FROM, CHILD_TO){

    				@Override
    				protected Cursor getChildrenCursor(Cursor groupCursor) {
    					final String[] PROJECTION = new String[]{
    							Item._ID,
    							Item.OWNER,
    							Item.TITLE,
    							Item.LINK,
    							Item.DESC
    					};
    					final String SELECT_ITEMS = Item.OWNER + " = ?";
    					int feedId = groupCursor.getInt(groupCursor.getColumnIndex(Feed._ID));
    					String[] selectionArgs = new String[]{
    							Integer.toString(feedId)	
    					};
    					return managedQuery(Item.CONTENT_URI, PROJECTION,
    							SELECT_ITEMS, selectionArgs, Item.DEFAULT_SORT);
    				}

    			});
    	
    	podcastTree.setClickable(true);
    	podcastTree.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			
			@Override
			public final void onCreateContextMenu(ContextMenu menu,
					View paramView, ContextMenuInfo paramContextMenuInfo) {
					
					ExpandableListView.ExpandableListContextMenuInfo info;
					
					try{
						info = (ExpandableListView.ExpandableListContextMenuInfo)paramContextMenuInfo;
					}catch (ClassCastException e) {
			            Log.e(TAG, "bad menuInfo", e);
			            return;
			        }
					
					int type =
						ExpandableListView.getPackedPositionType(info.packedPosition);
					
					//Toast.makeText(paramView.getContext(), "Selected " + info.id, Toast.LENGTH_SHORT).show();				
					
//					Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
//			        if (cursor == null) {
//			            // For some reason the requested item isn't available, do nothing
//			            return;
//			        }
					
					if(type == ExpandableListView.PACKED_POSITION_TYPE_CHILD){
						createItemContextMenu(paramView, menu, info);
					}else if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
						createChannelContextMenu(paramView, menu, info);
					}       
			}
			
			private final void createChannelContextMenu(View paramView, ContextMenu menu, ExpandableListView.ExpandableListContextMenuInfo info){
				// Setup the menu header
				ListAdapter list = podcastTree.getAdapter();
				int group =
					ExpandableListView.getPackedPositionGroup(info.packedPosition);
				Cursor cursor = (Cursor) list.getItem(group);
		        if(cursor == null){
		        	Log.d(TAG, "Null Group info cursor");
		        	return;
		        }
				menu.setHeaderTitle(cursor.getString(FEED_TITLE_COLUMN));

		        // Add a menu item to delete the note
		        menu.add(group, MENU_FEED_DELETE, 0, R.string.menu_delete);
			}
			
			private final void createItemContextMenu(View paramView, ContextMenu menu, ExpandableListView.ExpandableListContextMenuInfo info){
				Toast.makeText(paramView.getContext(), "Selected " + info.id, Toast.LENGTH_SHORT).show();				
			}
		});
    	
    }
    

    protected void addFeed(){
        Intent intent = new Intent(this, NewFeed.class);
        startActivity(intent);
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	switch(item.getItemId()){
    	case R.id.addFeed:
    		addFeed();
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		case MENU_FEED_DELETE: 
			ListAdapter list = mPodcastTree.getAdapter();
			Cursor cursor = (Cursor) list.getItem(item.getGroupId());
			
			if(cursor == null){
				return false;
			}
			
			int feedID = cursor.getInt(FEED_ID_COLUMN);
			//Toast.makeText(this, "Deleting channel " + feedID, Toast.LENGTH_SHORT).show();
			
			Uri delUri = ContentUris.withAppendedId(Feed.CONTENT_URI, feedID);
			getContentResolver().delete(delUri, null, null);
			
			return true;
		}
		return super.onContextItemSelected(item);
	}    

    
}
