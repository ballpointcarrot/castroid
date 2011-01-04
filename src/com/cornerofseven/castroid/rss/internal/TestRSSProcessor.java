/* Copyright 2010 Christopher Kruse and Sean Mooney

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.cornerofseven.castroid.rss.internal;

import javax.xml.parsers.ParserConfigurationException;

import android.net.Uri;

import com.cornerofseven.castroid.rss.RSSFeedBuilder;
import com.cornerofseven.castroid.rss.RSSProcessor;
import com.cornerofseven.castroid.rss.feed.RSSItem;

/**
 * RSS processor for test purposes.
 * Puts together pretend RSS feeds without having to process
 * an RSS document. This way we can test parts that rely on
 * having a feed before we write the RSS parser (which is going to be annoying...).
 * @author Sean Mooney
 *
 */
public class TestRSSProcessor implements RSSProcessor{

	private RSSFeedBuilder mBuilder = null;
	private Uri feedLocation;
	//keep track of total feeds processed to generate semi-different data.
	private static int numFeedsProcessed = 0;
	
	public TestRSSProcessor(Uri feedLocation){
		this.feedLocation = feedLocation;
	}
	
	@Override
	public void process() throws ParserConfigurationException {
		final RSSFeedBuilder builder = mBuilder;
	
		//inc the numFeeds
		final int feedIndex = ++numFeedsProcessed;
		
		final String feedTitle = "Feed" + feedIndex;
		final String feedLink = "http://localhost/rss" + feedIndex + ".html";
		final String feedDesc = "Feed number " + feedIndex;
		
		builder.newFeed();
		builder.setChannelTitle(feedTitle);
		builder.setChannelLink(feedLink);
		builder.setChannelDesc(feedDesc);
		
		for(int i = 0; i<feedIndex; i++){
			
			String itemName = "Item " + feedIndex + "_" + i;
			String itemDesc = "This is item " + feedIndex + "_" + i;
			String itemLink = "http://localhost/" + feedIndex + "_" + i + ".mp3";
			RSSItem item = builder.addItem();
			
			item.setmLink(itemLink);
			item.setmDesc(itemDesc);
			item.setmTitle(itemName);
		}
	}

	@Override
	public void setBuilder(RSSFeedBuilder builder) {
		this.mBuilder = builder;
	}
	
	@Override
	public RSSFeedBuilder getBuilder(){
		return mBuilder;
	}

}
