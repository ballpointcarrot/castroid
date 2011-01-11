package com.cornerofseven.castroid.data.test;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.data.PodcastDAO;
import com.cornerofseven.castroid.data.PodcastDataProvider;
import com.cornerofseven.castroid.rss.feed.RSSChannel;
import com.cornerofseven.castroid.rss.feed.RSSItem;

/**
 * Tests for the PodcastDataProvider.
 * The data provider is responsible for managing all
 * the data in system.  Namely, RSS/podcast feeds and
 * the items in those feeds.
 * @author Sean Mooney 
 *
 */
public class PodcastDataProviderTests extends AbstractPodcastDataProvider{

	@Override
	public void testAndroidTestCaseSetupProperly(){
		assertNotNull(getMockContentResolver());
		super.testAndroidTestCaseSetupProperly();
	}
	
	/* (non-Javadoc)
	 * @see android.test.AndroidTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testProvider(){
		assertNotNull(getProvider());
	}
	
	////////////////////MIME TYPES////////////////
	public void testFeedMimeType(){
		String feedMime = getMockContentResolver().getType(Feed.CONTENT_URI);
		assertNotNull(feedMime);
		fail("Is this the correct mime type? " + feedMime);
	}
	
	public void testItemMimeType(){
		String itemMime = getMockContentResolver().getType(Item.CONTENT_URI);
		assertNotNull(itemMime);
		fail("Is this the correct mime type? " + itemMime);
	}
	
	///////////FEED RELATED TESTS////////////////////////
	public void testAddFeed(){
		ContentValues values = new ContentValues();
		values.put(Feed.LINK, "http://www.something.com");
		values.put(Feed.DESCRIPTION, "This is a feed");
		values.put(Feed.TITLE, "Example Podcast");
		values.put(Feed.IMAGE, "nothing");
		
		Uri uri = getMockContentResolver().insert(Feed.CONTENT_URI, values);
		assertNotNull(uri);
	
		//TODO: anything else needed in this test?
	}
	
	public void testGetByItemID(){
		final String CHNL_TITLE = "Test1";
		final String CHNL_LINK = "http://www.twit.tv";
		final String CHNL_DESC = "Test Desc";
		RSSChannel channel = new RSSChannel(CHNL_TITLE, CHNL_LINK, CHNL_DESC, "");
		
		final String ITEM1_TITLE = "Item1";
		final String ITEM1_LINK = "http://www.twit1.tv";
		final String ITEM1_DESC = "Item1 desc";
		final String ITEM1_ENC = "http://www.twit1.tv/item1.mp3";
		final int 	 ITEM1_ENC_SIZE = 100;
		final String ITEM1_ENC_TYPE = "type/audio";

		final RSSItem item1 = new RSSItem(ITEM1_TITLE, ITEM1_LINK, ITEM1_DESC, ITEM1_ENC, ITEM1_ENC_SIZE, ITEM1_ENC_TYPE);
		channel.addItem(item1);
		
		ContentResolver contentResolver = getMockContentResolver();
		
		assertTrue(PodcastDAO.addRSS(contentResolver, channel));
		PodcastDataProvider dataProvider = 
			(PodcastDataProvider)getMockContentResolver()
			.acquireContentProviderClient(Feed.BASE_AUTH)
			.getLocalContentProvider();
		
		int fID = feedID(contentResolver, CHNL_TITLE);
		int itemID = itemID(contentResolver, CHNL_TITLE, ITEM1_TITLE);
		
		Cursor itemCursor;
		
		Uri itemSelectUri = ContentUris.withAppendedId(Item.CONTENT_URI, itemID);
		itemCursor = dataProvider.query(itemSelectUri, Item.PROJECTION, null, null, null);
	
		assertNotNull(itemCursor);
		
		itemCursor.moveToFirst();
		assertEquals(fID, itemCursor.getInt(itemCursor.getColumnIndex(Item.OWNER)));
		assertEquals(ITEM1_TITLE, itemCursor.getString(itemCursor.getColumnIndex(Item.TITLE)));
		assertEquals(ITEM1_LINK, itemCursor.getString(itemCursor.getColumnIndex(Item.LINK)));
		assertEquals(ITEM1_DESC, itemCursor.getString(itemCursor.getColumnIndex(Item.DESC)));
		assertEquals(ITEM1_ENC, itemCursor.getString(itemCursor.getColumnIndex(Item.ENC_LINK)));
		assertEquals(ITEM1_ENC_SIZE, itemCursor.getInt(itemCursor.getColumnIndex(Item.ENC_SIZE)));
		assertEquals(ITEM1_ENC_TYPE, itemCursor.getString(itemCursor.getColumnIndex(Item.ENC_TYPE)));
	}

	public void testDeleteFeed(){
		
		String feedTitle = "DeleteMe";
		ContentValues values = new ContentValues();
		values.put(Feed.TITLE, feedTitle);
		getMockContentResolver().insert(Feed.CONTENT_URI, values);
		
		String where = Feed.TITLE + "= ?";
		String[] selectionArgs = {feedTitle};
		int expectedNumber = 1;
		
		int itemsDeleted = getMockContentResolver().delete(Feed.CONTENT_URI, where, selectionArgs);
	
		
		assertEquals(expectedNumber, itemsDeleted);
	}

	/**
	 * Test delete feed by an ID on the end of the Feed URI
	 */
	public void testDeleteFeedById(){
		final String CHNL_TITLE = "Test1";
		final String CHNL_LINK = "http://www.twit.tv";
		final String CHNL_DESC = "Test Desc";
		RSSChannel channel = new RSSChannel(CHNL_TITLE, CHNL_LINK, CHNL_DESC, "");
		
		final String ITEM1_TITLE = "Item1";
		final String ITEM1_LINK = "http://www.twit1.tv";
		final String ITEM1_DESC = "Item1 desc";
		
		final String ITEM2_TITLE = "Item2";
		final String ITEM2_LINK = "http://www.twit2.tv";
		final String ITEM2_DESC = "Item2 desc";
		
		final RSSItem item1 = new RSSItem(ITEM1_TITLE, ITEM1_LINK, ITEM1_DESC);
		final RSSItem item2 = new RSSItem(ITEM2_TITLE, ITEM2_LINK, ITEM2_DESC);
		
		channel.addItem(item1);
		channel.addItem(item2);
		
		
		ContentResolver contentResolver = getMockContentResolver();
		
		assertTrue(PodcastDAO.addRSS(contentResolver, channel));
	
		Cursor feedId = contentResolver.query(Feed.CONTENT_URI, 
				new String[]{Feed._ID}, 
				Feed.TITLE + " = ?" , 
				new String[]{CHNL_TITLE}, null);
		feedId.moveToFirst();
		int fid = feedId.getInt(0);
		feedId.close();
		
		Uri delUri = ContentUris.withAppendedId(Feed.CONTENT_URI, fid);
		final int EXPECTED_DEL = 1;
		final int ACTUAL_DEL = contentResolver.delete(delUri, null, null);
		assertEquals(EXPECTED_DEL, ACTUAL_DEL);
		
		feedId = contentResolver.query(Feed.CONTENT_URI, 
				new String[]{Feed._ID}, 
				Feed.TITLE + " = ?" , 
				new String[]{CHNL_TITLE}, null);
		int feedCount = feedId.getCount();
		assertEquals("Should not have found a feed.", 0, feedCount);
	}
	
	/**
	 * Test the delete context menu item for a feed.
	 * Delete should delete all the feed and all of it's items.
	 * 
	 */
	public void testDeleteOption(){
		final String CHNL_TITLE = "Test1";
		final String CHNL_LINK = "http://www.twit.tv";
		final String CHNL_DESC = "Test Desc";
		RSSChannel channel = new RSSChannel(CHNL_TITLE, CHNL_LINK, CHNL_DESC, "");
		
		final String ITEM1_TITLE = "Item1";
		final String ITEM1_LINK = "http://www.twit1.tv";
		final String ITEM1_DESC = "Item1 desc";
		
		final String ITEM2_TITLE = "Item2";
		final String ITEM2_LINK = "http://www.twit2.tv";
		final String ITEM2_DESC = "Item2 desc";
		
		final RSSItem item1 = new RSSItem(ITEM1_TITLE, ITEM1_LINK, ITEM1_DESC);
		final RSSItem item2 = new RSSItem(ITEM2_TITLE, ITEM2_LINK, ITEM2_DESC);
		
		channel.addItem(item1);
		channel.addItem(item2);
		
		
		ContentResolver contentResolver = getMockContentResolver();
		
		assertTrue(PodcastDAO.addRSS(contentResolver, channel));
	
		Cursor feedId = contentResolver.query(Feed.CONTENT_URI, 
				new String[]{Feed._ID}, 
				Feed.TITLE + " = ?" , 
				new String[]{CHNL_TITLE}, null);
		feedId.moveToFirst();
		int f1id = feedId.getInt(0);
		feedId.close();
		
		final String CHNL_TITLE2 = "Test2";
		final String CHNL_LINK2 = "http://www.twit2.tv";
		final String CHNL_DESC2 = "Test Desc2";
		RSSChannel channel2 = new RSSChannel(CHNL_TITLE2, CHNL_LINK2, CHNL_DESC2, "");
		channel2.addItem(item1);
		channel2.addItem(item2);
		assertTrue(PodcastDAO.addRSS(contentResolver, channel2));
		
		Cursor feed2Id = contentResolver.query(Feed.CONTENT_URI, 
				new String[]{Feed._ID}, 
				Feed.TITLE + " = ? " , 
				new String[]{CHNL_TITLE2}, null);
		feed2Id.moveToFirst();
		int f2id = feed2Id.getInt(0);
		feed2Id.close();
		
		Cursor f1items = contentResolver.query(Item.CONTENT_URI, new String[]{Item._ID}, 
				Item.OWNER + " = ?", new String[]{f1id + ""}, null);
		
		//check that we inserted everything correctly
		f1items.moveToFirst();
		int numF1Items = f1items.getCount();
		final int EX_NUM_F1_ITEMS = 2;
		f1items.close();
		assertEquals(EX_NUM_F1_ITEMS, numF1Items);
		
		Cursor f2items = contentResolver.query(Item.CONTENT_URI, new String[]{Item._ID}, 
				Item.OWNER + " = ?" , new String[]{f2id + ""}, null);
		int numF2Items = f2items.getCount();
		final int EX_NUM_F2_ITEMS = 2;
		f2items.close();
		assertEquals(EX_NUM_F2_ITEMS, numF2Items);
		
		int itemsDeleted = contentResolver.delete(Feed.CONTENT_URI, Feed.TITLE + " = ? ", new String[]{CHNL_TITLE});
		assertTrue(itemsDeleted > 0);
		
		f1items = contentResolver.query(Item.CONTENT_URI, new String[]{Item._ID}, 
				Item.OWNER + " = ?", new String[]{f1id + ""}, null);
		
		//check that dependencies where correctly deleted
		f1items.moveToFirst();
		numF1Items = f1items.getCount();
		f1items.close();
		//there should not be any F1 items left
		assertEquals(0, numF1Items);
		
		f2items = contentResolver.query(Item.CONTENT_URI, new String[]{Item._ID}, 
				Item.OWNER + " = ?", new String[]{f2id + ""}, null);
		numF2Items = f2items.getCount();
		f2items.close();
		//should still have the feed2 items
		assertEquals(EX_NUM_F2_ITEMS, numF2Items);
	}
	
	/**
	 * The the Podcast data access object addChannel method..
	 */
	public void testAddChannelDAO(){
		final String CHNL_TITLE = "Test1";
		final String CHNL_LINK = "http://www.twit.tv";
		final String CHNL_DESC = "Test Desc";
		final String CHNL_SRC = "http://www.nowhere.com";
		RSSChannel channel = new RSSChannel(CHNL_TITLE, CHNL_LINK, CHNL_DESC, CHNL_SRC);
		
		final String ITEM1_TITLE = "Item1";
		final String ITEM1_LINK = "http://www.twit1.tv";
		final String ITEM1_DESC = "Item1 desc";
		final String ITEM1_ENC = "http://www.twit1.tv/item1.mp3";
		final int 	 ITEM1_ENC_SIZE = 100;
		final String ITEM1_ENC_TYPE = "type/audio";
		
		final String ITEM2_TITLE = "Item2";
		final String ITEM2_LINK = "http://www.twit2.tv";
		final String ITEM2_DESC = "Item2 desc";
		//final String ITEM2_ENC = "http://www.twit2.tv/item2.mp3";
		
		final RSSItem item1 = new RSSItem(ITEM1_TITLE, ITEM1_LINK, ITEM1_DESC, ITEM1_ENC, ITEM1_ENC_SIZE, ITEM1_ENC_TYPE);
		final RSSItem item2 = new RSSItem(ITEM2_TITLE, ITEM2_LINK, ITEM2_DESC);
		
		channel.addItem(item1);
		channel.addItem(item2);
		
		ContentResolver contentResolver = getMockContentResolver();
		
		assertTrue(PodcastDAO.addRSS(contentResolver, channel));
		
		
		//lookup the Channel and its items.
		PodcastDataProvider dataProvider = 
			(PodcastDataProvider)getMockContentResolver()
			.acquireContentProviderClient(Feed.BASE_AUTH)
			.getLocalContentProvider();
		
		Cursor feedCursor = dataProvider.query(Feed.CONTENT_URI, 
				new String[]{
				Feed._ID, Feed.TITLE, Feed.LINK, Feed.DESCRIPTION,
				Feed.RSS_URL
		}, Feed.TITLE + " = ? ", 
		new String[]{CHNL_TITLE}, 
		Feed.DEFAULT_SORT);
	
		final int EX_FEED_NUM = 1;
		assertEquals(EX_FEED_NUM, feedCursor.getCount());
		
		
		feedCursor.moveToFirst();
		assertEquals(CHNL_TITLE, feedCursor.getString(1));
		assertEquals(CHNL_LINK, feedCursor.getString(2));
		assertEquals(CHNL_DESC, feedCursor.getString(3));
		assertEquals(CHNL_SRC, feedCursor.getString(4));
		
		int fID = feedCursor.getInt(0);
		
		feedCursor.close();
		feedCursor = null;
		
		Cursor itemCursor = dataProvider.query(Item.CONTENT_URI, 
				new String[]{
				Item._ID, Item.OWNER, Item.TITLE, Item.LINK, Item.DESC,
				Item.ENC_LINK, Item.ENC_SIZE, Item.ENC_TYPE
		}, 
				Item.OWNER + " = ? " , 
				new String[]{Integer.toString(fID)}, 
				Item.TITLE);
		
		final int EX_ITEM_NUM = 2;
		itemCursor.moveToFirst();
		assertEquals(EX_ITEM_NUM, itemCursor.getCount());
	
		assertEquals(fID, itemCursor.getInt(1));
		assertEquals(ITEM1_TITLE, itemCursor.getString(2));
		assertEquals(ITEM1_LINK, itemCursor.getString(3));
		assertEquals(ITEM1_DESC, itemCursor.getString(4));
		assertEquals(ITEM1_ENC, itemCursor.getString(5));
		assertEquals(ITEM1_ENC_SIZE, itemCursor.getInt(6));
		assertEquals(ITEM1_ENC_TYPE, itemCursor.getString(7));
		
		
		itemCursor.moveToNext();
		
		assertEquals(fID, itemCursor.getInt(1));
		assertEquals(ITEM2_TITLE, itemCursor.getString(2));
		assertEquals(ITEM2_LINK, itemCursor.getString(3));
		assertEquals(ITEM2_DESC, itemCursor.getString(4));
		
		itemCursor.close();
		itemCursor = null;
	}

	/**
	 * Test updating an item with the full item id in the uri.
	 */
	public void testUpdateItemById(){
		final String CHNL_TITLE = "Test1";
		final String CHNL_LINK = "http://www.twit.tv";
		final String CHNL_DESC = "Test Desc";
		RSSChannel channel = new RSSChannel(CHNL_TITLE, CHNL_LINK, CHNL_DESC, "");
		
		final String ITEM1_TITLE = "Item1";
		final String ITEM1_LINK = "http://www.twit1.tv";
		final String ITEM1_DESC = "Item1 desc";
		final String ITEM1_ENC = "http://www.twit1.tv/item1.mp3";
		final int 	 ITEM1_ENC_SIZE = 100;
		final String ITEM1_ENC_TYPE = "type/audio";

		final RSSItem item1 = new RSSItem(ITEM1_TITLE, ITEM1_LINK, ITEM1_DESC, ITEM1_ENC, ITEM1_ENC_SIZE, ITEM1_ENC_TYPE);
		channel.addItem(item1);
		
		ContentResolver contentResolver = getMockContentResolver();
		
		assertTrue(PodcastDAO.addRSS(contentResolver, channel));
		
		int itemId = itemID(contentResolver, CHNL_TITLE, ITEM1_TITLE);
	
		Uri updateUri = ContentUris.withAppendedId(Item.CONTENT_URI, itemId);
		//should update exactly 1 row
		ContentValues values = new ContentValues();
		values.put(Item.STREAM_POS, 10);
		assertEquals(1, contentResolver.update(updateUri, values, null, null));
	}
	
	/**
	 * Test updating an item in the database
	 */
	public void testUpdateItem(){
		
		ContentResolver contentResolver = getMockContentResolver();
		contentResolver.update(Item.CONTENT_URI, null, null, null);
		
		notImplemented();
	}
	
	public void testUpdateFeed(){

		ContentResolver contentResolver = getMockContentResolver();
		contentResolver.update(Feed.CONTENT_URI, null, null, null);
		
		notImplemented();
	}
	
	public void testUpdateFeedById(){

		Uri fidURI = ContentUris.withAppendedId(Feed.CONTENT_URI, 1);
		
		ContentResolver contentResolver = getMockContentResolver();
		contentResolver.update(fidURI, null, null, null);
		
		notImplemented();
	}

	/////////////ITEM RELATED TESTS//////////////////////
	/*Any item must be associated with a Feed id.*/
	
	

}
