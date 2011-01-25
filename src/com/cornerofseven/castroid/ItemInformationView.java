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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.handlers.ChannelItemClickHandler;

/**
 * @author Sean Mooney
 *
 */
public class ItemInformationView extends Activity{

	protected TextView mItemName, mItemDesc;
	protected Button mPlay, mDownload;
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_information_view);
		
		collectWidgets();
		
		Intent intent = getIntent();
		if(intent != null){
			Uri dataUri = intent.getData();
			if(dataUri != null){
				bindData(dataUri);
				
				try{
					long itemId = Long.parseLong(dataUri.getPathSegments().get(1));
					bindListeners(itemId);
				}catch(NumberFormatException nfe){
					Toast.makeText(this, "Unknown item id in uri " 
							+ dataUri.toString(), Toast.LENGTH_LONG).show();
				}
				
			}
		}
		
	}
	

	/**
	 * Bind the elements from the view to the proper fields.
	 */
	protected void collectWidgets(){
		mItemName = (TextView)findViewById(R.id.iiv_item_name);
		mItemDesc = (TextView)findViewById(R.id.iiv_item_desc);
		
		mPlay = (Button)findViewById(R.id.iiv_play);
		mDownload = (Button)findViewById(R.id.iiv_download);
	}
	
	/**
	 * Bind the listeners to the buttons.
	 * @param itemId, the database identifier for the item.
	 */
	protected void bindListeners(final long itemId) {
		final Context context = this;
		mPlay.setOnClickListener(new View.OnClickListener() {
			
			static final int ITEM_CLICK_PLAY = 1;
			
			/**
			 * Handler for ChannelItem clicks.  We don't need
			 * to view this item (we already are) so make the VIEW_ID -1;
			 * 
			 * In this activity, the play on click handler is the
			 * only thing that will dispatch events to the 
			 * ChannelItemClickHandler. This means we can make the 
			 * object a field of the inner class, and not contribute
			 * to polluting the state of the overall activity. Locality FTW. 
			 */
			final ChannelItemClickHandler itemClickHandler 
				= new ChannelItemClickHandler(context, ITEM_CLICK_PLAY, -1);
			
			@Override
			public final void onClick(View v) {
				itemClickHandler.onItemClick(ITEM_CLICK_PLAY, itemId);
			}
		});
	}

	/**
	 * Bind the data we want to show into the text views.
	 * 
	 * @param dataLocation Uri of item to display.
	 */
	protected void bindData(final Uri dataLocation) {
		final String[] projection = {
				Item.TITLE,
				Item.DESC,
				Item.PUB_DATE,
		};
		Cursor c = managedQuery(dataLocation, projection, null, null, null);
	
		if(c.moveToFirst()){
			String title = c.getString(c.getColumnIndex(Item.TITLE));
			String desc = c.getString(c.getColumnIndex(Item.DESC));
			
			//TODO: Display the publication date.
			String pubDate = c.getString(c.getColumnIndex(Item.PUB_DATE));
			
			mItemName.setText(title);
			mItemDesc.setText(desc);
		}/*
		  * else, didn't return anything, which implies the data location
		  * was invalid. TODO: Do we warn/inform or just not display anything?
		  */
	}
}
