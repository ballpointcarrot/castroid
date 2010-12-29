package com.cornerofseven.castroid.rss.feed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Model the RSS xml Channel element.
 * @author sean
 *
 */
public class RSSChannel {
	private String mTitle;
	private String mLink;
	private String mDesc;
	private List<RSSItem> items;
	
	/**
	 * Delegate to the full constructor, with empty strings for paramters.
	 */
	public RSSChannel(){
		this("", "", "");
	}
	
	/**
	 * 
	 * @param title
	 * @param link
	 * @param desc
	 */
	public RSSChannel(String title, String link, String desc){
		this.mTitle = title;
		this.mLink = link;
		this.mDesc = desc;
		items = new ArrayList<RSSItem>();
	}
	
	public void addItem(RSSItem item){
		items.add(item);
	}
	
	public Iterator<RSSItem> itemsIterator(){
		return items.iterator();
	}
	
	/**
	 * Get a new array representing all of the items.
	 * @return
	 */
	public RSSItem[] itemsAsArray(){
		RSSItem[] items = new RSSItem[this.items.size()];
		return this.items.toArray(items);
	}

	public String getmTitle() {
		return mTitle;
	}

	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getmLink() {
		return mLink;
	}

	public void setmLink(String mLink) {
		this.mLink = mLink;
	}

	public String getmDesc() {
		return mDesc;
	}

	public void setmDesc(String mDesc) {
		this.mDesc = mDesc;
	}
}
