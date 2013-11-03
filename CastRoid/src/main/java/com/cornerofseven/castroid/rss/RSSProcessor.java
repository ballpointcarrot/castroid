package com.cornerofseven.castroid.rss;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public interface RSSProcessor {
	public void process() throws ParserConfigurationException, IOException, SAXException, MalformedRSSException;
	public void setBuilder(RSSFeedBuilder builder);
	public RSSFeedBuilder getBuilder();
}
