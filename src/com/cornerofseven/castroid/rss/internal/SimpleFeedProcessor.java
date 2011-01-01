package com.cornerofseven.castroid.rss.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.net.Uri;

import com.cornerofseven.castroid.rss.RSSFeedBuilder;
import com.cornerofseven.castroid.rss.RSSProcessor;

/**
 * An example RSS file:
 * 
 * <rss version="2.0">
 * <channel>
 * 	<title/>
 * 	<item></item>*
 * </channel>
 * </rss>
 * 
 * 
 * @author sean
 *
 */

public class SimpleFeedProcessor implements RSSProcessor{

	private RSSFeedBuilder mFeedBuilder = null;
	private Uri mFeedLocation;
	
	public SimpleFeedProcessor(Uri feedLocation){
		this.mFeedLocation = feedLocation;
	}
	
	@Override
	public void process() throws ParserConfigurationException, IOException, SAXException {
		Node root = null;
		
		DocumentBuilder builder 
			= DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputStream feedstream = getIntputStream();
		Document document = builder.parse(feedstream);
		root = document.getDocumentElement();
		feedstream.close();
		
		findFeedInfo(root);
		findFeedItems(root);
		mFeedBuilder.finishFeed();
	}

	/**
	 * Convert the feed URI to an inputstream.
	 * The client is responsible for closing the stream.
	 * @return An InputStream from the URI for the feed location.
	 * @throws IOException 
	 */
	protected InputStream getIntputStream() throws IOException{
		URL url = new URL(mFeedLocation.toString());
		return url.openConnection().getInputStream();
	}
	
	@Override
	public void setBuilder(RSSFeedBuilder builder) {
		mFeedBuilder = builder;
	}
	
	@Override
	public RSSFeedBuilder getBuilder(){
		return mFeedBuilder;
	}
	
	/**
	 * 
	 * @param root 
	 */
	private void findFeedInfo(Node root){
		RSSFeedBuilder builder = mFeedBuilder;
	
		String feedName = "";
		String feedDesc = "";
		String feedLink = "";
		builder.newFeed();
		builder.setChannelTitle(feedName);
		builder.setChannelLink(feedLink);
		builder.setChannelDesc(feedDesc);
	
	}
	
	/**
	 * 
	 * @param root
	 */
	private void findFeedItems(Node root){
		
	}
	
	/**
	 * 
	 * @param item
	 */
	private void processItem(Node item){
		RSSFeedBuilder builder = mFeedBuilder;
		
		String name = "", desc = "", encURI = "";
		
		builder.addItem(name, desc, encURI);
	}
}
