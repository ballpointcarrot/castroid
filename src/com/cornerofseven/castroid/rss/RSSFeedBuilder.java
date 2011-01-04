package com.cornerofseven.castroid.rss;

import com.cornerofseven.castroid.rss.feed.RSSChannel;
import com.cornerofseven.castroid.rss.feed.RSSItem;

/**
 * Interface defining what RSS/Podcast data can be
 * added.
 * @author sean
 *
 */
public interface RSSFeedBuilder {
	/**
	 * Start a new feed.
	 * @param feedName
	 * @param uri
	 */
	public void newFeed();
	
	/**
	 * Should only be called after {@link #newFeed}.
	 * @param title
	 */
	public void setChannelTitle(String title);
	
	/**
	 * Should only be called after {@link #newFeed}.
	 * @param link
	 */
	public void setChannelLink(String link);
	
	/**
	 * Should only be called after {@link #newFeed}
	 * @param desc
	 */
	public void setChannelDesc(String desc);
	
	/**
	 * Add an Item to the feed being built. It is incorrect to call addItem
	 * without first creating a new Feed.
	 * @param name
	 * @param desc
	 * @param encURI
	 * @return reference to the item.  This reference's should be mutated add information to the item.
	 */
	public RSSItem addItem();

	/**
	 * Do whatever clean/data commit/ect. that is needed for the feed.
	 */
	public void finishFeed();
	
	/**
	 * Get the object built by this builder.
	 * @return
	 */
	public RSSChannel getFeed();
}
