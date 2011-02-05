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

import com.cornerofseven.castroid.data.DatabaseQuery;
import com.cornerofseven.castroid.data.Feed;
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
     * Sets up the database with some 'default' values to make
     * query tests a little more simple.
     * 
     * precondition: assumes the database is empty.
     * postcondition: Two separate feeds have been added to the database.
     * 	Feed1 is called Feed1 and has 2 items.
     *  Feed2 is called Feed2 and has 3 items.
     */
    protected void prepareDatabase(){
        String chnlTitle = "Test1";
        String chnlLink = "http://www.twit.tv";
        String chnlDesc = "Test Desc";
        RSSChannel channel = new RSSChannel(chnlTitle, chnlLink, chnlDesc, "");
        final ContentResolver mockResolver = getMockContentResolver();
        
        String itemTitle = "Item1";
        String itemLink = "http://www.twit1.tv";
        String itemDesc = "Item1 desc";
        String itemDate = "2011-01-03";
        String itemEnc = "http://www.twit1.tv/item1.mp3";
        int itemEncSize = 100;
        String itemEncType = "type/audio";

        channel.addItem(new RSSItem(itemTitle, itemLink, itemDesc, itemDate, itemEnc, itemEncSize, itemEncType));
        
        itemTitle = "Item2";
        itemLink = "http://www.twit1.tv";
        itemDesc = "Item2 desc";
        itemDate = "2011-01-03";
        itemEnc = "http://www.twit1.tv/item2.mp3";
        itemEncSize = 1000;
        itemEncType = "type/audio";

        channel.addItem(new RSSItem(itemTitle, itemLink, itemDesc, itemDate, itemEnc, itemEncSize, itemEncType));        
    
        PodcastDAO.addRSS(mockResolver, channel);
        
        chnlTitle = "Test2";
        chnlLink = "http://www.twit1.tv";
        chnlDesc = "Test 2 Desc";
        channel = new RSSChannel(chnlTitle, chnlLink, chnlDesc, "");
        
        
        itemTitle = "Item21";
        itemLink = "http://www.twit1.tv";
        itemDesc = "Item21 desc";
        itemDate = "2011-01-23";
        itemEnc = "http://www.twit21.tv/item21.mp3";
        itemEncSize = 100;
        itemEncType = "type/audio";

        channel.addItem(new RSSItem(itemTitle, itemLink, itemDesc, itemDate, itemEnc, itemEncSize, itemEncType));
        
        itemTitle = "Item22";
        itemLink = "http://www.twit21.tv";
        itemDesc = "Item22 desc";
        itemDate = "2011-01-25";
        itemEnc = "http://www.twit1.tv/item22.mp3";
        itemEncSize = 1000;
        itemEncType = "type/audio";

        channel.addItem(new RSSItem(itemTitle, itemLink, itemDesc, itemDate, itemEnc, itemEncSize, itemEncType));        
    
        itemTitle = "Item23";
        itemLink = "http://www.twit23.tv";
        itemDesc = "Item23 desc";
        itemDate = "2011-01-24";
        itemEnc = "http://www.twit1.tv/item23.mp3";
        itemEncSize = 10000;
        itemEncType = "type/audio";

        channel.addItem(new RSSItem(itemTitle, itemLink, itemDesc, itemDate, itemEnc, itemEncSize, itemEncType));        
    
        PodcastDAO.addRSS(mockResolver, channel);
    }


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


    /**
     * Test getting all of the feed 
     */
    public void testGetAllFeedIds(){
        prepareDatabase();
        DatabaseQuery q = PodcastDAO.getFeedIdsQuery();
        assertNotNull(q);
        ContentResolver mockContent = getMockContentResolver();

        Cursor c = mockContent.query(q.getContentUri(), 
                q.getProjection(), q.getSelection(), q.getSelectionArgs(), Feed._ID + " DESC");
    
        final int exNumItems = 2;
        assertEquals(exNumItems, c.getCount());
        
        
        c.close();
    }
}
