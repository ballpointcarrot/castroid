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
	public void setChannelLink(String link) {
		mChannel.setmLink(link);
	}

	@Override
	public void setChannelDesc(String desc) {
		mChannel.setmDesc(desc);
	}
	
	@Override
	public void addItem(String name, String desc, String encURI) {
		RSSItem item = new RSSItem(name, "", desc, encURI);
		mChannel.addItem(item);
	}

	@Override
	public void finishFeed() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public RSSChannel getFeed(){
		return mChannel;
	}
}
