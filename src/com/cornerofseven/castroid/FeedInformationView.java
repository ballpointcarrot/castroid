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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cornerofseven.castroid.data.Feed;

/**
 * An activity for displaying information specifc to an rss channel.
 * 
 * @author Sean Mooney
 * @since Jan 23, 2011
 */
public class FeedInformationView extends Activity{

	
	////////////////Widgets we care about////////////
	private ImageView mChannelImage = null;
	private ListView mChannelItems = null;
	private TextView mChannelName = null;
	 
	
	//////////////////life cycle/////////////////////
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_view);
	
		collectWidgets();
		
		Intent intent = getIntent();
		if(intent != null){
			populateView(intent.getData());
		}else{
			//TODO: Case for when there is no intent.
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		//TODO: Save the feed view state? or is it enough to get it from the intent	
	}
	
	/**
	 * Set the widget fields with the widgets from the view.
	 */
	protected void collectWidgets(){
		mChannelImage = (ImageView)findViewById(R.id.cv_rssicon);
		mChannelItems = (ListView)findViewById(R.id.cv_channel_items);
		mChannelName = (TextView)findViewById(R.id.cv_channel_title);
	}
	
	/**
	 * Populate the view with the channel's information.
	 * @param channelURI content URI for the channel information.
	 */
	//TODO: Finish populating view.
	protected void populateView(Uri channelURI){
		final String[] projection = {
				Feed.IMAGE,
				Feed.TITLE,
				Feed.DESCRIPTION,
				Feed.LINK
		};
		Cursor c = managedQuery(channelURI, projection, null, null, null);
		
		if(c.moveToFirst()){

			//TODO: install the image in the image view, if exists.
			String channelTitle, channelDesc;
			channelTitle = c.getString(c.getColumnIndex(Feed.TITLE));
			mChannelName.setText(channelTitle);
			
			//get the feed's items
			
			//install the item listener. (Same listener from CastRoid)
		}
		
	}
	/////////////////end life cycle///////////////////
}
