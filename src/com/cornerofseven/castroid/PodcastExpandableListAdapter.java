package com.cornerofseven.castroid;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.SimpleCursorTreeAdapter;

public class PodcastExpandableListAdapter extends SimpleCursorTreeAdapter {

	public PodcastExpandableListAdapter(Context context){
		super(context, null, /*TODO: get the actual data cursor*/
			-1, -1, null, null, -1, -1, null, null);
		
		
	}
	

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		// TODO Auto-generated method stub
		return null;
	}
}
