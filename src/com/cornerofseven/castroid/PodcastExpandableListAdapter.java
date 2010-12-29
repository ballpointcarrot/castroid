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
	
	public PodcastExpandableListAdapter(Context context, Cursor cursor){
		/*
		(Context context, 
		Cursor cursor, 
		int collapsedGroupLayout, 
		int expandedGroupLayout, 
		String[] groupFrom, int[] groupTo, 
		int childLayout, int lastChildLayout, 
		String[] childFrom, int[] childTo)
		*/
		super(context, cursor,
			-1, -1, null, null, -1, -1, null, null);
		//dataProvider = new PodcastDataProvider(context);
		
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
