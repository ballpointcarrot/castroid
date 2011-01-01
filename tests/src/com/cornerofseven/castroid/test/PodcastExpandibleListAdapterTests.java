package com.cornerofseven.castroid.test;

import com.cornerofseven.castroid.PodcastExpandableListAdapter;
import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.test.AbstractPodcastDataProvider;

import android.content.Context;
import android.database.Cursor;
import android.test.AndroidTestCase;


/**
 * Test the PodcastExpandableListAdapter.
 * 
 * @author Sean Mooney
 *
 */
public class PodcastExpandibleListAdapterTests extends AbstractPodcastDataProvider{

	public void testCreateProvider(){
		final String[] FEED_PROJECTION ={
        		Feed._ID,
        		Feed.TITLE,
        		Feed.DESCRIPTION,
        		Feed.LINK
        };
		
		Context context = getMockContext();
		Cursor cursor = getMockContentResolver().query(Feed.CONTENT_URI, 
        		FEED_PROJECTION, null, null, null);
		
		PodcastExpandableListAdapter adapter 
			= new PodcastExpandableListAdapter(context, cursor); 
	}
}
