package com.cornerofseven.castroid.rss.internal;

import com.cornerofseven.castroid.rss.RSSFeedBuilder;
import com.cornerofseven.castroid.rss.feed.RSSChannel;
import com.cornerofseven.castroid.rss.feed.RSSItem;

public class SimpleRSSFeedBuilder implements RSSFeedBuilder {

	private RSSChannel mChannel = null;
	
	
	@Override
	public void newFeed() {
		mChannel = new RSSChannel();
	}

	@Override
	public void setChannelTitle(String title) {
		mChannel.setmTitle(title);
	}

	@Override
	public void setChannelSource(String rssSource){
	    mChannel.setRssUrl(rssSource);
	}
	
	@Override
	public void setChannelLink(String link) {
		mChannel.setmLink(link);
	}

	@Override
	public void setChannelDesc(String desc) {
		mChannel.setmDesc(desc);
	}
	
	@Override
	public RSSItem addItem() {
		RSSItem item = new RSSItem();
		mChannel.addItem(item);
		return item;
	}

	/**
	 * Nothing to be done.
	 */
	@Override
	public void finishFeed() {}
	
	@Override
	public RSSChannel getFeed(){
		return mChannel;
	}
}
