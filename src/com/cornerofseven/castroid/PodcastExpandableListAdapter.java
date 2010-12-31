package com.cornerofseven.castroid;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.data.PodcastDataProvider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.SimpleCursorTreeAdapter;

public class PodcastExpandableListAdapter extends SimpleCursorTreeAdapter {

	private PodcastDataProvider dataProvider;
	
	private static final String SELECT_ITEMS = Item.OWNER + " = ?";
	private static final String[] PROJECTION = new String[]{
		Item._ID,
		Item.OWNER,
		Item.TITLE,
		Item.LINK,
		Item.DESC
	};
	
	private static final int GROUP_LAYOUT = android.R.layout.simple_expandable_list_item_1;
	private static final String[] GROUP_FROM = {Feed.TITLE};
	private static final int[] GROUP_TO = {android.R.id.text1};
	private static final int CHILD_LAYOUT = android.R.layout.simple_expandable_list_item_1;
	private static final String[] CHILD_FROM = {Item.TITLE};
	private static final int[] CHILD_TO = {android.R.id.text1};
	
	public PodcastExpandableListAdapter(Context context, Cursor cursor){
		super(context, cursor, 
				GROUP_LAYOUT, GROUP_FROM, GROUP_TO,
				 CHILD_LAYOUT, CHILD_FROM, CHILD_TO
		);
		
	}

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		groupCursor.moveToNext();
		int feedId = groupCursor.getInt(groupCursor.getColumnIndex(Feed._ID));
		String[] selectionArgs = new String[]{
			Integer.toString(feedId)	
		};

		return dataProvider.query(Item.CONTENT_URI, 
				PROJECTION, SELECT_ITEMS, selectionArgs, Item.DEFAULT_SORT);
	}
}
