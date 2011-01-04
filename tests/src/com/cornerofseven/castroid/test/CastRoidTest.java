/*
   Copyright 2010 Christopher Kruse and Sean Mooney

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.cornerofseven.castroid.test;

import android.content.ContentResolver;
import android.database.Cursor;

import com.cornerofseven.castroid.Castroid;
import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.data.PodcastDAO;
import com.cornerofseven.castroid.data.PodcastDataProvider;
import com.cornerofseven.castroid.rss.feed.RSSChannel;
import com.cornerofseven.castroid.rss.feed.RSSItem;

/**
 * 
 * @author Sean Mooney
 *
 */
public class CastRoidTest extends AbstractCastRoidTest {
	
	/**
	 * Test the delete context menu item for a feed.
	 * Delete should delete all the feed and all of it's items.
	 */
	public void testDeleteOption(){
		final String CHNL_TITLE = "Test1";
		final String CHNL_LINK = "http://www.twit.tv";
		final String CHNL_DESC = "Test Desc";
		RSSChannel channel = new RSSChannel(CHNL_TITLE, CHNL_LINK, CHNL_DESC);
		
		final String ITEM1_TITLE = "Item1";
		final String ITEM1_LINK = "http://www.twit1.tv";
		final String ITEM1_DESC = "Item1 desc";
		final String ITEM1_ENC = "http://www.twit1.tv/item1.mp3";
		
		final String ITEM2_TITLE = "Item2";
		final String ITEM2_LINK = "http://www.twit2.tv";
		final String ITEM2_DESC = "Item2 desc";
		final String ITEM2_ENC = "http://www.twit2.tv/item2.mp3";
		
		final RSSItem item1 = new RSSItem(ITEM1_TITLE, ITEM1_LINK, ITEM1_DESC, ITEM1_ENC);
		final RSSItem item2 = new RSSItem(ITEM2_TITLE, ITEM2_LINK, ITEM2_DESC, ITEM2_ENC);
		
		channel.addItem(item1);
		channel.addItem(item2);
		
		Castroid activity = mActivity;
		ContentResolver contentResolver = activity.getContentResolver();
		
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
		RSSChannel channel2 = new RSSChannel(CHNL_TITLE2, CHNL_LINK2, CHNL_DESC2);
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
		RSSChannel channel = new RSSChannel(CHNL_TITLE, CHNL_LINK, CHNL_DESC);
		
		final String ITEM1_TITLE = "Item1";
		final String ITEM1_LINK = "http://www.twit1.tv";
		final String ITEM1_DESC = "Item1 desc";
		final String ITEM1_ENC = "http://www.twit1.tv/item1.mp3";
		
		final String ITEM2_TITLE = "Item2";
		final String ITEM2_LINK = "http://www.twit2.tv";
		final String ITEM2_DESC = "Item2 desc";
		final String ITEM2_ENC = "http://www.twit2.tv/item2.mp3";
		
		final RSSItem item1 = new RSSItem(ITEM1_TITLE, ITEM1_LINK, ITEM1_DESC, ITEM1_ENC);
		final RSSItem item2 = new RSSItem(ITEM2_TITLE, ITEM2_LINK, ITEM2_DESC, ITEM2_ENC);
		
		channel.addItem(item1);
		channel.addItem(item2);
		
		Castroid activity = mActivity;
		ContentResolver contentResolver = activity.getContentResolver();
		
		assertTrue(PodcastDAO.addRSS(contentResolver, channel));
		
		
		//lookup the Channel and its items.
		PodcastDataProvider dataProvider = mDataprovider;
		
		Cursor feedCursor = dataProvider.query(Feed.CONTENT_URI, 
				new String[]{
				Feed._ID, Feed.TITLE, Feed.LINK, Feed.DESCRIPTION
		}, Feed.TITLE + " = ? ", 
		new String[]{CHNL_TITLE}, 
		Feed.DEFAULT_SORT);
	
		final int EX_FEED_NUM = 1;
		assertEquals(EX_FEED_NUM, feedCursor.getCount());
		
		
		feedCursor.moveToFirst();
		assertEquals(CHNL_TITLE, feedCursor.getString(1));
		assertEquals(CHNL_LINK, feedCursor.getString(2));
		assertEquals(CHNL_DESC, feedCursor.getString(3));
		
		int fID = feedCursor.getInt(0);
		
		feedCursor.close();
		feedCursor = null;
		
		Cursor itemCursor = dataProvider.query(Item.CONTENT_URI, 
				new String[]{
				Item._ID, Item.OWNER, Item.TITLE, Item.LINK, Item.DESC
		}, 
				Item.OWNER + " = ? " , 
				new String[]{Integer.toString(fID)}, 
				Item.DEFAULT_SORT);
		
		final int EX_ITEM_NUM = 2;
		itemCursor.moveToFirst();
		assertEquals(EX_ITEM_NUM, itemCursor.getCount());
	
		assertEquals(fID, itemCursor.getInt(1));
		assertEquals(ITEM1_TITLE, itemCursor.getString(2));
		assertEquals(ITEM1_LINK, itemCursor.getString(3));
		assertEquals(ITEM1_DESC, itemCursor.getString(4));
		
		itemCursor.moveToNext();
		
		assertEquals(fID, itemCursor.getInt(1));
		assertEquals(ITEM2_TITLE, itemCursor.getString(2));
		assertEquals(ITEM2_LINK, itemCursor.getString(3));
		assertEquals(ITEM2_DESC, itemCursor.getString(4));
		
		itemCursor.close();
		itemCursor = null;
	}
	
	
}
