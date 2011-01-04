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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Context;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.cornerofseven.castroid.Castroid;
import com.cornerofseven.castroid.R;
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

	//constants for expected item indexes
	//name the indexes for easier reading.
	static final int TITLE_INDEX = 0;
	static final int LINK_INDEX = 1;
	static final int DESC_INDEX = 2;
	static final int ENC_INDEX = 3;
	
	
	static final String RSS2_0SampleFile = "rss2sample";
	static final String WaitWaitSampleFile = "waitwait";
	
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
		createSampleXML(R.raw.rss2sample, RSS2_0SampleFile);
		createSampleXML(R.raw.waitwait, WaitWaitSampleFile);
	}

	/**
	 * Copy the raw src to file we can load.
	 * Why? because I can't figure out another way todo this, such that
	 * that I can get a URI for a local datafile for the RSS Processor.
	 * TODO: Figure out a better way to push test data to the device!
	 * @param resourceID id of raw xml file.
	 * @param outfile
	 */
	protected void createSampleXML(int resourceID, String outfile){
		Context context = mContext;
		InputStream input = context.getResources().openRawResource(resourceID);

		Scanner in = new Scanner(input);

		final int FILE_MODE = 0;

		try {
			FileOutputStream output = context.openFileOutput(outfile, FILE_MODE);
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

		final String expectedTitle = "Liftoff News";
		final String expectedLink = "http://liftoff.msfc.nasa.gov/";
		final String expectedDesc = "Liftoff to Space Exploration.";

		
		String[][] expectedItemData = {
				{"Star City", "http://liftoff.msfc.nasa.gov/news/2003/news-starcity.asp", "How do Americans get ready to work with Russians aboard the International Space Station? They take a crash course in culture, language and protocol at Russia's <a href=\"http://howe.iki.rssi.ru/GCTC/gctc_e.htm\">Star City</a>.", ""},
				{"", "", "Sky watchers in Europe, Asia, and parts of Alaska and Canada will experience a &lt;a href=\"http://science.nasa.gov/headlines/y2003/30may_solareclipse.htm\"&gt;partial eclipse of the Sun&lt;/a&gt; on Saturday, May 31st.", ""},
				{"The Engine That Does More", "http://liftoff.msfc.nasa.gov/news/2003/news-VASIMR.asp", "Before man travels to Mars, NASA hopes to design new engines that will let us fly through the Solar System more quickly.  The proposed VASIMR engine would do that.", ""},
				{"Astronauts' Dirty Laundry", "http://liftoff.msfc.nasa.gov/news/2003/news-laundry.asp", "Compared to earlier spacecraft, the International Space Station has many luxuries, but laundry facilities are not one of them.  Instead, astronauts have other options.", ""}
		};

		doParsingTest(RSS2_0SampleFile, 
				expectedTitle, 
				expectedDesc, 
				expectedLink, expectedItemData);
	}
	
	/**
	 * Parse the example wait wait file.
	 * @throws MalformedRSSException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 */
	public void testParseWaitWait() throws ParserConfigurationException, IOException, SAXException, MalformedRSSException{
		final String expectedTitle = "NPR: Wait Wait... Don't Tell Me! Podcast";
		final String expectedLink = "http://www.npr.org/templates/rundowns/rundown.php?prgId=35";
		final String expectedDesc = "NPR's weekly current events quiz.  Have a laugh and test your news knowledge while figuring out what's real and what we've made up.";


		String[][] expectedItemData = new String[1][4];
		
		expectedItemData[0][TITLE_INDEX] = "NPR: 01-01-2011 Wait Wait... Don't Tell Me!";
		expectedItemData[0][DESC_INDEX] = "Stories:  1) Who's Carl This Time? 2) Opening Panel Round 3) Bluff The Listener 4) Not My Job Guest: Nora Ephron 5) Panel Round Two 6) Limericks 7) Lightning Fill In The Blank 8) Prediction"; 
		expectedItemData[0][LINK_INDEX] =	"http://www.npr.org/templates/rundowns/rundown.php?prgId=35";
		expectedItemData[0][ENC_INDEX] =	"http://podcastdownload.npr.org/anon.npr-podcasts/podcast/35/132562081/npr_132562081.mp3";

		doParsingTest(WaitWaitSampleFile, 
				expectedTitle, 
				expectedDesc, 
				expectedLink, expectedItemData);
	}
	
	/**
	 * Run a parsing test for a single XML file.
	 * 
	 * Parsing tests are basically the same, just
	 * with different parameters, which means it is abstracted
	 * out to reduce redundancy, and simplify creating new tests.
	 * 
	 * @param fileName
	 * @param expectedTitle
	 * @param expectedDesc
	 * @param expectedLink
	 * @param expectedItemData an array of expected item values.
	 * 			Item information is expected in 
	 * 			<ul><li>title</li><li>link</li><li>desc</li><li>enc_url</li> order.
	 * @throws MalformedRSSException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 */
	protected void doParsingTest(String fileName, String expectedTitle, 
			String expectedDesc, 
			String expectedLink,
			String[][] expectedItemData) throws ParserConfigurationException, IOException, SAXException, MalformedRSSException{
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

		assertEquals(expectedTitle, channel.getmTitle());
		assertEquals(expectedLink, channel.getmLink());
		assertEquals(expectedDesc, channel.getmDesc());

		RSSItem[] items = channel.itemsAsArray();

		
		/*
		 * this will array index out of bounds if too many items 
		 * are returned. 
		 */
		for(int i = 0; i<items.length; i++){
			RSSItem cur = items[i];
			assertEquals(expectedItemData[i][TITLE_INDEX], cur.getTitle());
			assertEquals(expectedItemData[i][LINK_INDEX], cur.getLink());
			assertEquals(expectedItemData[i][DESC_INDEX], cur.getDesc());
			assertEquals(expectedItemData[i][ENC_INDEX], cur.getEnclosure());
		}

		//assume the number of item data elements passed in
		//is how many total items are expected. 
		final int EXPECTED_ITEMS = expectedItemData.length;
		
		final String errorMsg = "Expected " + EXPECTED_ITEMS + " found " + items.length;
		assertEquals(errorMsg, EXPECTED_ITEMS, items.length);
	}
}
