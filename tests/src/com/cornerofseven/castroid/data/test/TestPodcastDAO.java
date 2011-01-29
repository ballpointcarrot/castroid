/**
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
package com.cornerofseven.castroid.data.test;

import android.content.ContentResolver;
import android.database.Cursor;

import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.data.PodcastDAO;
import com.cornerofseven.castroid.rss.feed.RSSChannel;
import com.cornerofseven.castroid.rss.feed.RSSItem;

/**
 * Test for the Podcast DataAccessObject (DAO).
 * 
 * @author Sean Mooney
 *
 */
public class TestPodcastDAO extends AbstractPodcastDataProvider{

	/**
	 * When a RSSChannel is added, all the items of the channel
	 * must appear in the database, and any items that 
	 * were not previously in the database must be marked as 
	 * "new".
	 */
	public void testAddChannel(){
		String pubDate = "2011-01-29";
		ContentResolver mockContent = getMockContentResolver();
		RSSChannel c = new RSSChannel("ASDF", "ASDF", "ASDF", "ASDF");
		
		final String itemTitle = "title";
		final String itemDesc = "desc";
		final String itemLink = "link";
		RSSItem item = new RSSItem(itemTitle, itemLink, itemDesc);
		item.setPubDate(pubDate);
		c.addItem(item);
		PodcastDAO.addRSS(mockContent, c);
	
		int fID = feedID(mockContent, "ASDF");
	
		Cursor cursor = mockContent.query(Item.CONTENT_URI, 
				Item.PROJECTION, Item.OWNER + " = ?", 
				new String[]{Integer.toString(fID)}, null);
		
		assertTrue(cursor.moveToFirst());
		
		assertEquals(itemTitle, getColumnAsString(cursor, Item.TITLE) );
		assertEquals(itemLink, getColumnAsString(cursor, Item.LINK) );
		assertEquals(itemDesc, getColumnAsString(cursor, Item.DESC) );
		assertEquals(pubDate, getColumnAsString(cursor, Item.PUB_DATE));
		assertEquals(1, getColumnAsInt(cursor, Item.NEW));
	}
}
