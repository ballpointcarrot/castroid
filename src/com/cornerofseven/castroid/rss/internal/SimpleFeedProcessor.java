package com.cornerofseven.castroid.rss.internal;

import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;

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
	private Uri feedLocation;
	
	public SimpleFeedProcessor(Uri feedLocation){
		this.feedLocation = feedLocation;
	}
	
	@Override
	public void process() throws ParserConfigurationException {
		Node root = null;
		
		DocumentBuilder builder 
			= DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document 
			=null;
//			builder.getDOMImplementation()
//			//.createDocument(namespaceURI, qualifiedName, doctype)
//			.createDocument(feedLocation.getAuthority(), feedLocation.getPath(), );
		
		root = document.getDocumentElement();
		
		findFeedInfo(root);
		findFeedItems(root);
		mFeedBuilder.finishFeed();
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
