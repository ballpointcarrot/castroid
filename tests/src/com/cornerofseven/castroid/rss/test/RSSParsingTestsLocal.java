/*
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
package com.cornerofseven.castroid.rss.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Context;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.cornerofseven.castroid.Castroid;
import com.cornerofseven.castroid.rss.MalformedRSSException;
import com.cornerofseven.castroid.rss.RSSFeedBuilder;
import com.cornerofseven.castroid.rss.RSSProcessor;
import com.cornerofseven.castroid.rss.RSSProcessorFactory;
import com.cornerofseven.castroid.rss.feed.RSSChannel;
import com.cornerofseven.castroid.rss.feed.RSSItem;

/**
 * Test cases for parsing an RSS file that is located locally on the device.
 * 
 * Currently runs a plain junit3 set of tests for the parsing. This will
 * be switched to an Android test once some of the logic is in place, 
 * and I figure out how to get eclipse to copy the test files automatically.
 * 
 * @author Sean Mooney
 *
 */
public class RSSParsingTestsLocal extends ActivityInstrumentationTestCase2<Castroid>{

	static final String RSS2_0SampleFile = "rss2sample";

	private Castroid mActivity;
	private Context mContext;
	
	public RSSParsingTestsLocal()
	{
		super("com.cornerofsever.castroid.Castroid", Castroid.class);
	}


	@Override
	public void setUp() throws Exception{
		super.setUp();
		mActivity = this.getActivity();
		mContext = mActivity.getApplication().getApplicationContext();// getInstrumentation().getContext();
		create_rss2samplexml();
	}

	/**
	 * Copy the raw src to file we can load.
	 * Why? because I can't figure out another way todo this, such that
	 * that I can get a URI for a local datafile for the RSS Processor.
	 * TODO: Figure out a better way to push test data to the device!
	 */
	protected void create_rss2samplexml(){
		Context context = mContext;
		InputStream input = context.getResources().openRawResource(
				com.cornerofseven.castroid.R.raw.rss2sample);

		Scanner in = new Scanner(input);

		final int FILE_MODE = 0;

		try {
			FileOutputStream output = context.openFileOutput(RSS2_0SampleFile, FILE_MODE);
			Log.i(TAG, "Output file" + output.getFD().toString());
			PrintStream outstream = new PrintStream(output);
			while(in.hasNext()){
				String xmldata = in.nextLine();
				outstream.println(xmldata);
			}
			outstream.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final String TAG = "RSSParsingLocalTests";

	public void testParseRSS20Example() throws ParserConfigurationException, IOException, SAXException, MalformedRSSException{

		//getContext().ge
		
		File dataFolder = mContext.getFilesDir();
		File file = new File(dataFolder, RSS2_0SampleFile);

		Uri uri = Uri.fromFile(file);
		URL url = new URL(uri.toString());
		
		RSSProcessor proc = RSSProcessorFactory.getRSS2_0Processor(url);

		RSSFeedBuilder builder = proc.getBuilder();
		assertNotNull(builder);

		proc.process();
		RSSChannel channel = builder.getFeed();
		assertNotNull(channel);

		final String expectedChannelTitle = "Liftoff News";
		final String expectedChannelLink = "http://liftoff.msfc.nasa.gov/";
		final String expectedChannelDesc = "Liftoff to Space Exploration.";

		final int EXPECTED_ITEMS = 4;
		//test data from the items
		//title, link, desc, enclosure

		//name the indexes for easier reading.
		final int TITLE_INDEX = 0;
		final int LINK_INDEX = 1;
		final int DESC_INDEX = 2;
		final int ENC_INDEX = 3;
		String[][] expectedItemData = {
				{"Star City", "http://liftoff.msfc.nasa.gov/news/2003/news-starcity.asp", "How do Americans get ready to work with Russians aboard the International Space Station? They take a crash course in culture, language and protocol at Russia's &lt;a href=\"http://howe.iki.rssi.ru/GCTC/gctc_e.htm\"&gt;Star City&lt;/a&gt;.", ""},
				{"", "", "Sky watchers in Europe, Asia, and parts of Alaska and Canada will experience a &lt;a href=\"http://science.nasa.gov/headlines/y2003/30may_solareclipse.htm\"&gt;partial eclipse of the Sun&lt;/a&gt; on Saturday, May 31st.", ""},
				{"The Engine That Does More", "http://liftoff.msfc.nasa.gov/news/2003/news-VASIMR.asp", "Before man travels to Mars, NASA hopes to design new engines that will let us fly through the Solar System more quickly.  The proposed VASIMR engine would do that.", ""},
				{"Astronauts' Dirty Laundry", "http://liftoff.msfc.nasa.gov/news/2003/news-laundry.asp", "Compared to earlier spacecraft, the International Space Station has many luxuries, but laundry facilities are not one of them.  Instead, astronauts have other options.", ""}
		};

		assertEquals(expectedChannelTitle, channel.getmTitle());
		assertEquals(expectedChannelLink, channel.getmLink());
		assertEquals(expectedChannelDesc, channel.getmDesc());

		RSSItem[] items = channel.itemsAsArray();

		/*
		 * this will array index out of bounds if too many items 
		 * are returned. 
		 */
		for(int i = 0; i<items.length; i++){
			RSSItem cur = items[i];
			assertEquals(expectedItemData[i][TITLE_INDEX], cur.getmTitle());
			assertEquals(expectedItemData[i][LINK_INDEX], cur.getmLink());
			assertEquals(expectedItemData[i][DESC_INDEX], cur.getmDesc());
			assertEquals(expectedItemData[i][ENC_INDEX], cur.getmEnclosure());
		}

		final String errorMsg = "Expected " + EXPECTED_ITEMS + " found " + items.length;
		assertEquals(errorMsg, EXPECTED_ITEMS, items.length);
	}
}
