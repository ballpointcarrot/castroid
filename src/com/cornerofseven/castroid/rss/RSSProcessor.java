package com.cornerofseven.castroid.rss;

import javax.xml.parsers.ParserConfigurationException;

public interface RSSProcessor {

	public void process() throws ParserConfigurationException;
	public void setBuilder(RSSFeedBuilder builder);
	public RSSFeedBuilder getBuilder();
	
}
