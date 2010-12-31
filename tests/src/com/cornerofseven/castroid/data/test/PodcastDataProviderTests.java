package com.cornerofseven.castroid.data.test;


import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.data.PodcastDataProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.test.ProviderTestCase2;

/**
 * Tests for the PodcastDataProvider.
 * The data provider is responsible for managing all
 * the data in system.  Namely, RSS/podcast feeds and
 * the items in those feeds.
 * @author Sean Mooney 
 *
 */
public class PodcastDataProviderTests extends ProviderTestCase2<PodcastDataProvider>{

	private ContentResolver mContentResolver;
	
	public PodcastDataProviderTests(){
		this(PodcastDataProvider.class, "content://" + Feed.BASE_AUTH);
	}
	
	public PodcastDataProviderTests(Class<PodcastDataProvider> providerClass,
			String providerAuthority) {
		super(providerClass, providerAuthority);
	}
	
	/* (non-Javadoc)
	 * @see android.test.AndroidTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		mContentResolver = getMockContentResolver();
		super.setUp();
	}

	@Override
	public void testAndroidTestCaseSetupProperly(){
		assertNotNull(mContentResolver);
		super.testAndroidTestCaseSetupProperly();
	}
	
	/* (non-Javadoc)
	 * @see android.test.AndroidTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	////////////////////MIME TYPES////////////////
	public void testFeedMimeType(){
		String feedMime = mContentResolver.getType(Feed.CONTENT_URI);
		fail("Is this the correct mime type? " + feedMime);
	}
	
	public void testItemMimeType(){
		String itemMime = mContentResolver.getType(Item.CONTENT_URI);
		fail("Is this the correct mime type? " + itemMime);
	}
	
	///////////FEED RELATED TESTS////////////////////////
	public void testAddFeed(){
		ContentValues values = new ContentValues();
		values.put(Feed.LINK, "http://www.something.com");
		values.put(Feed.DESCRIPTION, "This is a feed");
		values.put(Feed.TITLE, "Example Podcast");
		values.put(Feed.IMAGE, "nothing");
		
		Uri uri = mContentResolver.insert(Feed.CONTENT_URI, values);
		assertNotNull(uri);
	
		//TODO: anything else needed in this test?
	}

	public void testDeleteFeed(){
		
		String feedTitle = "DeleteMe";
		String where = Feed.TITLE + "=" + feedTitle;
		String[] selectionArgs = {};
		int expectedNumber = 1;
		
		int itemsDeleted = mContentResolver.delete(Feed.CONTENT_URI, where, selectionArgs);
	
		assertEquals(expectedNumber, itemsDeleted);
		//Currently this test should fail on the assertEquals
		//Fail here, in case the assertPasses when it shouldn't
		fail("Shouldn't reach here");
	}
	
	

	/////////////ITEM RELATED TESTS//////////////////////
	/*Any item must be associated with a Feed id.*/
	
	

}
