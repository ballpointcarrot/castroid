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

package com.cornerofseven.castroid.rss.test;

import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.cornerofseven.castroid.rss.MalformedRSSException;
import com.cornerofseven.castroid.rss.RSSFeedBuilder;
import com.cornerofseven.castroid.rss.RSSProcessor;
import com.cornerofseven.castroid.rss.RSSProcessorFactory;
import com.cornerofseven.castroid.rss.feed.RSSChannel;
import com.cornerofseven.castroid.rss.feed.RSSItem;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

/**
 * RSSParsing tests for the feeds that are
 * located remotely.  Requires an active Internet connection to work.
 * 
 * @author Sean Mooney
 */
public class RSSParsingTestRemote extends AndroidTestCase{

	private static final String TAG = "RSSRemoteTests";
	
	public void testParseRSS20Example() throws ParserConfigurationException, IOException, SAXException, MalformedRSSException{
		String address = "http://cyber.law.harvard.edu/rss/examples/rss2sample.xml";
		Uri uri = Uri.parse(address);
		URL url = new URL(uri.toString());
		
		RSSProcessor proc = RSSProcessorFactory.getRSS2_0Processor(url);


		RSSFeedBuilder builder = proc.getBuilder();
		assertNotNull(builder);

		proc.process();
		RSSChannel channel = builder.getFeed();
		assertNotNull(channel);

		final String expectedChannelTitle = "Liftoff News";
		final String expectedChannelLink = "http://liftoff.msfc.nasa.gov/";
		final String expectedChannelDesc = "Liftoff to Space Exploration.";

		final int EXPECTED_ITEMS = 4;
		//test data from the items
		//title, link, desc, enclosure

		//name the indexes for easier reading.
		final int TITLE_INDEX = 0;
		final int LINK_INDEX = 1;
		final int DESC_INDEX = 2;
		final int ENC_INDEX = 3;
		String[][] expectedItemData = {
				{"Star City", "http://liftoff.msfc.nasa.gov/news/2003/news-starcity.asp", "How do Americans get ready to work with Russians aboard the International Space Station? They take a crash course in culture, language and protocol at Russia's &lt;a href=\"http://howe.iki.rssi.ru/GCTC/gctc_e.htm\"&gt;Star City&lt;/a&gt;.", ""},
				{"", "", "Sky watchers in Europe, Asia, and parts of Alaska and Canada will experience a <a href=\"http://science.nasa.gov/headlines/y2003/30may_solareclipse.htm\">partial eclipse of the Sun</a> on Saturday, May 31st.", ""},
				{"The Engine That Does More", "http://liftoff.msfc.nasa.gov/news/2003/news-VASIMR.asp", "Before man travels to Mars, NASA hopes to design new engines that will let us fly through the Solar System more quickly.  The proposed VASIMR engine would do that.", ""},
				{"Astronauts' Dirty Laundry", "http://liftoff.msfc.nasa.gov/news/2003/news-laundry.asp", "Compared to earlier spacecraft, the International Space Station has many luxuries, but laundry facilities are not one of them.  Instead, astronauts have other options.", ""}
		};

		assertEquals(expectedChannelTitle, channel.getmTitle());
		assertEquals(expectedChannelLink, channel.getmLink());
		assertEquals(expectedChannelDesc, channel.getmDesc());

		RSSItem[] items = channel.itemsAsArray();

		/*
		 * this will array index out of bounds if too many items 
		 * are returned. 
		 */
		for(int i = 0; i<items.length; i++){
			RSSItem cur = items[i];
			assertEquals(expectedItemData[i][TITLE_INDEX], cur.getTitle());
			assertEquals(expectedItemData[i][LINK_INDEX], cur.getLink());
			assertEquals(expectedItemData[i][DESC_INDEX], cur.getDesc());
			assertEquals(expectedItemData[i][ENC_INDEX], cur.getEnclosure());
		}

		final String errorMsg = "Expected " + EXPECTED_ITEMS + " found " + items.length;
		assertEquals(errorMsg, EXPECTED_ITEMS, items.length);
	}
	
	/**
	 * Parse the Wait Wait Don't Tell me feed.
	 * @throws MalformedRSSException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 */
	public void testWaitWaitFeed() throws ParserConfigurationException, IOException, SAXException, MalformedRSSException{
		final String addr = "http://www.npr.org/rss/podcast.php?id=35";
		final URL wwURL = new URL(addr);
		
		RSSProcessor proc = RSSProcessorFactory.getRSS2_0Processor(wwURL);
		proc.process();
		
		RSSChannel channel = proc.getBuilder().getFeed();
		
		
		
		final String SUB_TAG = TAG + "_WaitWait";
		
		Log.i(SUB_TAG, channel.getmTitle());
		Log.i(SUB_TAG, channel.getmDesc());
		Log.i(SUB_TAG, channel.getmLink());
	
		assertEquals(addr, channel.getRssUrl());
		
		Log.i(SUB_TAG, "Items:");
		
		Iterator<RSSItem> items = channel.itemsIterator();
		while(items.hasNext()){
			RSSItem item = items.next();
			StringBuilder sb = new StringBuilder();
			sb.append(item.getTitle()); sb.append(":");
			sb.append(item.getDesc()); sb.append(":");
			sb.append(item.getLink()); sb.append(":");
			sb.append(item.getEnclosure()); sb.append(":");
		}
	}
	
	/**
	 * Test parsing a feed.
	 * 
	 * Feed from {@link
	 * http://www.faithwebsites.com/audio_rss.cfm?detailid=119486}
	 * 
	 * Test may break in the future, since goes for live data.
	 * Written for 2011-02-12
	 * 
	 * Problem submitted by Susan VanderPlas.  Crashes entire application.
	 * @throws MalformedRSSException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 */
	public void testParseFaithWebsite(){
	    try{
	        final String addr = "http://www.faithwebsites.com/audio_rss.cfm?detailid=119486";
	        final URL wwURL = new URL(addr);

	        RSSProcessor proc = RSSProcessorFactory.getRSS2_0Processor(wwURL);
	        proc.process();

	        RSSChannel channel = proc.getBuilder().getFeed();


	        final String SUB_TAG = TAG + "_FaitWebsite";

	        Log.i(SUB_TAG, channel.getmTitle());
	        Log.i(SUB_TAG, channel.getmDesc());
	        Log.i(SUB_TAG, channel.getmLink());


	        assertEquals(addr, channel.getRssUrl());

	        Log.i(SUB_TAG, "Items:");

	        //num item processed
	        int numProcItems = 0;
	        Iterator<RSSItem> items = channel.itemsIterator();
	        while(items.hasNext()){
	            RSSItem item = items.next();
	            StringBuilder sb = new StringBuilder();
	            sb.append(item.getTitle()); sb.append(":");
	            sb.append(item.getDesc()); sb.append(":");
	            sb.append(item.getLink()); sb.append(":");
	            sb.append(item.getEnclosure()); sb.append(":");

	            numProcItems++;
	        }

	        int exNumItems = 2;
	        assertEquals(exNumItems, numProcItems);
	    }catch(SAXException saxex){
	        fail(saxex.getMessage());
	    } catch (Exception e) {
	        e.printStackTrace();
	        fail("Exception " + e.getClass() + "\n" + e.getMessage());
	    } 
	}
}
