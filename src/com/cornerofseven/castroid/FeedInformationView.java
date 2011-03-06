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
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.handlers.ChannelItemClickHandler;
import com.cornerofseven.castroid.network.AsyncImageDownloader;

/**
 * An activity for displaying information specifc to an rss channel.
 * 
 * Things we should be able to do here:
 * 
 * 1. TODO: Update rss feed.
 * 2. TODO: Click on item to view information
 * 3. TODO: Download item
 * 4. Stream item (works, but probably on wrong action.  Default action of item tap)
 * 
 * @author Sean Mooney
 * @since Jan 23, 2011
 */
public class FeedInformationView extends Activity{

	
	////////////////Widgets we care about////////////
	private ImageView mChannelImage = null;
	private ListView mChannelItems = null;
	private TextView mChannelName = null;
	private TextView mChannelDesc = null;
	///////////////END Widgets//////////////////////
	
	static final int PLAY_ITEM = 1;
	static final int VIEW_ITEM = 2;
	
	private final int MAX_IMAGE_WIDTH = 75;
	private final int MAX_IMAGE_HEIGHT = 75;
	
	protected final ChannelItemClickHandler itemClickListener 
		= new ChannelItemClickHandler(this, PLAY_ITEM, VIEW_ITEM); 
	
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
	
	/**
	 * Set the widget fields with the widgets from the view.
	 */
	protected void collectWidgets(){
		mChannelImage = (ImageView)findViewById(R.id.cv_rssicon);
		mChannelItems = (ListView)findViewById(R.id.cv_channel_items);
		mChannelName = (TextView)findViewById(R.id.cv_channel_title);
		mChannelDesc = (TextView)findViewById(R.id.cv_channel_desc);
	}
	
	/**
	 * Populate the view with the channel's information.
	 * @param channelURI content URI for the channel information.
	 */
	//TODO: Finish populating view.
	protected void populateView(Uri channelURI){
		final String[] projection = {
				Feed._ID,
				Feed.IMAGE,
				Feed.TITLE,
				Feed.DESCRIPTION,
				Feed.LINK
		};
		final String[] ITEM_PROJECTION = new String[] { Item._ID,
				/*Item.OWNER,*/ Item.TITLE, /*Item.LINK, Item.DESC,*/ Item.PUB_DATE };
		
		Cursor c = managedQuery(channelURI, projection, null, null, null);
		
		if(c.moveToFirst()){

			int feedId;
			
			//TODO: install the image in the image view, if exists.
			final String channelTitle = c.getString(c.getColumnIndex(Feed.TITLE));
			final String channelDesc =  c.getString(c.getColumnIndex(Feed.DESCRIPTION));
			feedId = c.getInt(c.getColumnIndex(Feed._ID));
			
			loadImage(feedId);
			
			mChannelName.setText(channelTitle);
			mChannelDesc.setText(channelDesc);
			
			//get the feed's items
			final String SELECT_ITEMS = Item.OWNER + " = ?";
			final String[] selectionArgs = new String[] { Integer
					.toString(feedId) };
			final Cursor itemCursor = managedQuery(Item.CONTENT_URI, ITEM_PROJECTION,
					SELECT_ITEMS, selectionArgs, Item.DEFAULT_SORT);
			final SimpleCursorAdapter itemAdapter = new SimpleCursorAdapter(this,
					R.layout.item_view, 
					itemCursor,
					new String[]{Item.TITLE, Item.PUB_DATE},
					new int[]{R.id.item_textview, R.id.item_date}
			);
			mChannelItems.setAdapter(itemAdapter);
			
			//install the item listener. (Same listener from CastRoid)
			mChannelItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public final void onItemClick(final AdapterView<?> arg0, final View arg1,
						final int arg2, final long itemId) {
					itemClickListener.onItemClick(VIEW_ITEM, itemId);
				}
			});
		}
	}
	
	/**
	 * Inflate, download, or supply default image for the channel.
	 * 
	 * Decision structure:
	 * either the channel listed an image link or it didn't
	 * 
	 * if no image link, return the default.
	 * 
	 * if \exists link,
	 * either image is cached or needs to be downloaded.
	 * 
	 * If cached, simply inflate/instaniate/load from storage.
	 * If needs download, fetch on separate thread, stick in the cache
	 * and notify a callback to change the image once it exists.
	 * 
	 *
	 * 
	 * @param feedId
	 * @return
	 */
	protected void loadImage(long channelId){
		final ImageView imageView = mChannelImage;
		//TODO: Logic for download/cache
		
		imageView.setAdjustViewBounds(true);
		
		//default image.
		//imageView.setImageResource(R.drawable.podcast_image);
		imageView.setMaxWidth(MAX_IMAGE_WIDTH);
		imageView.setMaxHeight(MAX_IMAGE_HEIGHT);
		imageView.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		
		String imageUrl = channelImageUrl(channelId);
		if(imageUrl != null){
			new AsyncImageDownloader(imageView).execute(imageUrl);
		}
	}
		
	/////////////////end life cycle///////////////////
	
	/**
	 * Lookup the image uri for
	 * @return the Url stored in the content provider for the id, or null. 
	 */
	protected String channelImageUrl(long channelid){
		String imageUrl = null;
		
		Uri contentUri = ContentUris.withAppendedId(Feed.CONTENT_URI, channelid);
		Cursor c = managedQuery(contentUri, new String[]{Feed.IMAGE}, null, null, null);
		
		if(c.moveToFirst()){
			imageUrl  = c.getString(c.getColumnIndex(Feed.IMAGE));
		}
		
		return imageUrl;
	}
}
