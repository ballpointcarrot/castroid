package com.cornerofseven.castroid.data.test;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.data.PodcastDataProvider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.test.ProviderTestCase2;

/**
 * A base class to set up the mock content podcast data provider.
 * Any class testing the data provider somehow should extend this class.
 * 
 * @author Sean Mooney
 *
 */
public abstract class AbstractPodcastDataProvider extends ProviderTestCase2<PodcastDataProvider>{
	public AbstractPodcastDataProvider(){
		//this(PodcastDataProvider.class, "content://" + Feed.BASE_AUTH);
		this(PodcastDataProvider.class, Feed.BASE_AUTH);
	}
	
	public AbstractPodcastDataProvider(Class<PodcastDataProvider> providerClass,
			String providerAuthority) {
		super(providerClass, providerAuthority);
	}
	
	public void setUp() throws Exception{
		super.setUp();
		
		//clear out all the old data.
		PodcastDataProvider dataProvider = 
			(PodcastDataProvider)getMockContentResolver()
			.acquireContentProviderClient(Feed.BASE_AUTH)
			.getLocalContentProvider();
		dataProvider.deleteAll();
	}
	
	/**
	 * Look up the id of a feed from its title.
	 * @param feedTitle
	 * @return
	 */
	public static int feedID(ContentResolver content, String feedTitle){
		Cursor feedCursor = content.query(Feed.CONTENT_URI, 
				new String[]{Feed._ID}, Feed.TITLE + " = ? ", 
		new String[]{feedTitle}, 
		Feed.DEFAULT_SORT);
		
		int fid = -1;
		
		feedCursor.moveToFirst();
		fid = feedCursor.getInt(feedCursor.getColumnIndex(Feed._ID));
		
		feedCursor.close();
		return fid;
	}
	
	public static int itemID(ContentResolver content, String feedTitle, String itemTitle){
		Cursor feedCursor = content.query(Feed.CONTENT_URI, 
				new String[]{Feed._ID}, Feed.TITLE + " = ? ", 
		new String[]{feedTitle}, 
		Feed.DEFAULT_SORT);
		
		int fid = -1;
		
		feedCursor.moveToFirst();
		fid = feedCursor.getInt(feedCursor.getColumnIndex(Feed._ID));
		
		feedCursor.close();
		
		Cursor itemCursor = content.query(Item.CONTENT_URI, 
				new String[]{Item._ID}, 
				Item.OWNER + " = ? " , 
				new String[]{Integer.toString(fid)}, 
				Item.DEFAULT_SORT);
		
		itemCursor.moveToFirst();
		int itemID = itemCursor.getInt(itemCursor.getColumnIndex(Item._ID));
		itemCursor.close();
		
		return itemID;
	}
}
