package com.cornerofseven.castroid.rss;

import android.net.Uri;

import com.cornerofseven.castroid.rss.internal.SimpleFeedProcessor;
import com.cornerofseven.castroid.rss.internal.SimpleRSSFeedBuilder;
import com.cornerofseven.castroid.rss.internal.TestRSSProcessor;

/**
 * Factory to produce RSS processors, based on the type of feed
 * we want to process.
 * @author Sean Mooney
 *
 */
public class RSSProcessorFactory {
	//hide the constructor
	private RSSProcessorFactory(){}
	
	/**
	 * Get a processor for an RSS 2_0 spec RSS. The processor 
	 * has the builder set into it and is ready to process the feed.
	 * @param builder
	 * @return
	 */
	public static RSSProcessor getRSS2_0Processor(Uri feedLocation, RSSFeedBuilder builder){
		//SimpleFeedProcessor sfp = new SimpleFeedProcessor(feedLocation);
		TestRSSProcessor sfp = new TestRSSProcessor(feedLocation);
		sfp.setBuilder(builder);
		return sfp;
	}
	
	/**
	 * Process a feed with a SimpleRSSFeedBuilder.
	 * @param feedLocation
	 * @return
	 */
	public static RSSProcessor getRSS2_0Processor(Uri feedLocation){
		RSSFeedBuilder builder = new SimpleRSSFeedBuilder();
		return getRSS2_0Processor(feedLocation, builder);
	}
}
