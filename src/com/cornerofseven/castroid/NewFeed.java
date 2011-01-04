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

package com.cornerofseven.castroid;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.data.PodcastDAO;
import com.cornerofseven.castroid.rss.RSSFeedBuilder;
import com.cornerofseven.castroid.rss.RSSProcessor;
import com.cornerofseven.castroid.rss.RSSProcessorFactory;
import com.cornerofseven.castroid.rss.feed.RSSChannel;
import com.cornerofseven.castroid.rss.feed.RSSItem;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * An activity for adding a new feed to the system.
 * 
 * The activity allows a user to enter the URL for a podcast and
 * then display the information for the feed, or tells the user
 * the URL was invalid.
 *
 * @author Sean Mooney
 */
public class NewFeed extends Activity{

	private static final String TAG = "NewFeed"; 
	
	//The controls we will need from the activity's view//
	private Button mCreate = null;
	private EditText mInputText;
	private TextView mFeedTitle; 
	private TextView mFeedDesc;
	private TextView mFeedLink;
	private ListView mFeedItems;
	private Button mSaveFeed;
	///////////////////////////////////////////////////////

	private RSSChannel mFeed = null;
	
	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);

		//find the NewFeed interface.
		setContentView(R.layout.add_feed_screen); 

		//find all the controls in the view
		mCreate = (Button)findViewById(R.id.afs_check);
		mInputText = (EditText)findViewById(R.id.afs_url);
		mFeedTitle = (TextView)findViewById(R.id.feed_info_title);
		mFeedLink = (TextView)findViewById(R.id.feed_info_link);
		mFeedDesc = (TextView)findViewById(R.id.feed_info_desc);
		mFeedItems = (ListView)findViewById(R.id.afs_items);
		mSaveFeed = (Button)findViewById(R.id.afs_savefeed);
		
		final EditText input = mInputText;

		//connect the CheckFeed button on click action.
		mCreate.setOnClickListener(new View.OnClickListener() {
			/**
			 * Redirect to the class method to handel checking,
			 * parsing, display, etc. a new feed.
			 */
			@Override
			public final void onClick(View v) {
				loadFeedOrError(input.getText().toString());
			}
		});
		
		mSaveFeed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveRSSFeed();
			}
		});

		//all done!
	}

	/**
	 * Check the url given to see if it contains a valid
	 * rss feed.  
	 * @param uriString string representation of the Uri where the feed is located.
	 */
	protected void loadFeedOrError(String urlString){
		/*General idea
		 * -> Convert the String to a URI object.
		 * -> Pass the URI as an argument to the RSS feed creator
		 * -> Let the Feed creator run. It will signal any problems with the url
		 * -> If no problems, keep track of the feed, so we don't have to reprossess and display its information
		 */

		try{
			//TODO: Delete me!
			Log.d(TAG, "Checking " + urlString);
			URL feedLocation = new URL(urlString);
			Log.i(TAG, feedLocation.toString());
			RSSProcessor processor = RSSProcessorFactory.getRSS2_0Processor(feedLocation);
			Log.d(TAG, "Using processor " + processor.getClass().toString());
			
			processor.process();
			RSSFeedBuilder builder = processor.getBuilder();
		
			mFeed = builder.getFeed();
			Log.d(TAG, mFeed.toString());
			bindFeedInfo();
		}catch(Exception ex){
			Log.e(TAG, ex.getMessage());
			Toast.makeText(this, "Unable to parse the feed\n " 
					+ ex.getMessage()
					, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Bind the rss feed information into the correct view elements
	 */
	protected void bindFeedInfo(){
		final RSSChannel feed = mFeed;
		
		//TODO: Delete me
		Log.d(TAG, "Binding feed information");
		if(feed != null){
			mFeedTitle.setText(feed.getmTitle());
			mFeedDesc.setText(feed.getmDesc());
			mFeedLink.setText(feed.getmLink());
		
			final ListView itemView = mFeedItems;
			final ListAdapter adapter 
				= new ArrayAdapter<RSSItem>(this, R.layout.item_view, feed.itemsAsArray());
			itemView.setAdapter(adapter);
			
			Log.d(Castroid.TAG, "Items in " + feed.getmTitle());
			Iterator<RSSItem> itemsIter = feed.itemsIterator();
			while(itemsIter.hasNext())
				Log.d(Castroid.TAG, itemsIter.next().toString());
		}
	}
	
	/**
	 * Save the feed into the dataprovider
	 */
	protected void saveRSSFeed(){
		final RSSChannel feed = mFeed;
		if(feed != null){
			ContentResolver content = getContentResolver();
			
			if(!PodcastDAO.addRSS(content, feed)){
				Toast.makeText(this, "Unable to add the feed", Toast.LENGTH_SHORT).show();
			}
			
		}else{
			Toast.makeText(this, "No feed to save", Toast.LENGTH_SHORT).show();
		}
		
		finish();
	}
}
