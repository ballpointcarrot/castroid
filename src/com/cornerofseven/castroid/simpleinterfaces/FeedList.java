package com.cornerofseven.castroid.simpleinterfaces;


import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;

import com.cornerofseven.castroid.CastRoidListenerFactory;
import com.cornerofseven.castroid.R;
import com.cornerofseven.castroid.data.Feed;

public class FeedList extends ListActivity{

	private static final String TAG = "FeedList";
	
	private String[] FEED_PROJECTION = {
			Feed._ID,
			Feed.TITLE,
			Feed.LINK,
			Feed.DESCRIPTION
	};
	
	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
		
		Intent intent = getIntent();
		if(intent.getData() == null){
			intent.setData(Feed.CONTENT_URI);
		}
		
		Cursor cursor = managedQuery(
				intent.getData(), 
				FEED_PROJECTION, 
				null, 
				null, 
				Feed.DEFAULT_SORT);
	
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, 
				R.layout.feed_information, 
				cursor, 
				new String[]{Feed.TITLE, Feed.LINK, Feed.DESCRIPTION}, 
				new int[]{R.id.feed_info_title, R.id.feed_info_link, R.id.feed_info_desc});
//		
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.array.dummy, R.id.feed_info_title);
		setListAdapter(adapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.addFeed:
			Log.i(TAG, "Adding a feed.");
			CastRoidListenerFactory.getAddFeedListener().onClick(getListView());
			return true;
		//add other menu options here
		}
		
		//delegate to the parent.
		return super.onOptionsItemSelected(item);
	}
}
