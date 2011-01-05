/**
   Copyright 2010 Christopher Kruse and Sean Mooney

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. 
 */
package com.cornerofseven.castroid.network.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.cornerofseven.castroid.data.PodcastDAO;
import com.cornerofseven.castroid.data.test.AbstractPodcastDataProvider;
import com.cornerofseven.castroid.network.DownloadManager;
import com.cornerofseven.castroid.rss.MalformedRSSException;
import com.cornerofseven.castroid.rss.RSSFeedBuilder;
import com.cornerofseven.castroid.rss.RSSProcessor;
import com.cornerofseven.castroid.rss.RSSProcessorFactory;
import com.cornerofseven.castroid.rss.feed.RSSChannel;

/**
 * @author sean
 *
 */
public class DownloadTests extends AbstractPodcastDataProvider{

	//TODO: Put the download link somewhere else.
	//NPR could/will remove the file and then were will we be, with test broken for no reason.
	public void testDownloadItem() throws ParserConfigurationException, IOException, SAXException, MalformedRSSException{
		String fileName = "waitwait";
		
		final String feedName = "NPR: Wait Wait... Don't Tell Me! Podcast";
		final String itemName = "NPR: 01-01-2011 Wait Wait... Don't Tell Me!";
		
		File dataFolder = mContext.getFilesDir();
		File file = new File(dataFolder, fileName);

		Uri uri = Uri.fromFile(file);
		URL url = new URL(uri.toString());
		
		RSSProcessor proc = RSSProcessorFactory.getRSS2_0Processor(url);

		RSSFeedBuilder builder = proc.getBuilder();
		assertNotNull(builder);

		proc.process();
		RSSChannel channel = builder.getFeed();
		assertNotNull(channel);
		
		ContentResolver resolver = getMockContentResolver();
		
		PodcastDAO.addRSS(resolver, channel);
		
		int itemId = AbstractPodcastDataProvider.itemID(resolver, feedName, itemName);
		
		Context mockContext = getMockContext();
		assertNotNull(mockContext);
		assertTrue(DownloadManager.downloadItemEnc(mockContext, itemId, null));
	}
}
