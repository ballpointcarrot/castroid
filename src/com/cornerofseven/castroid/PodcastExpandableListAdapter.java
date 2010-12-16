package com.cornerofseven.castroid;

import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.data.PodcastDataProvider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.SimpleCursorTreeAdapter;

public class PodcastExpandableListAdapter extends SimpleCursorTreeAdapter {

	private PodcastDataProvider dataProvider;
	
	private static final String SELECT_ITEMS = "";
	private static final String[] PROJECTION = new String[]{
		Item._ID
		
	};
	
	public PodcastExpandableListAdapter(Context context, Cursor cursor){
		super(context, cursor,
			-1, -1, null, null, -1, -1, null, null);
		dataProvider = new PodcastDataProvider(context);
		
	}
	

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		
		String[] selectionArgs = new String[]{
				
		};
		
		return dataProvider.query(Item.CONTENT_URI, 
				PROJECTION, SELECT_ITEMS, selectionArgs, Item.DEFAULT_SORT);
	}
}
