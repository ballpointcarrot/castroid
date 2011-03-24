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
package com.cornerofseven.castroid.handlers;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.cornerofseven.castroid.ItemInformationView;
import com.cornerofseven.castroid.data.Item;

/**
 * A class to handle the click for any RSS item in a list.
 * 
 * 
 * Follows the chain of responsibility pattern the regular
 * onClickListeners implement. Gives us a common place
 * to create the item click logic, then any activity that
 * needs display channel items for click can use an instance
 * of this object to handle with the click.
 * 
 * @author Sean Mooney
 *
 */
public class ChannelItemClickHandler {

	
	private final int PLAY_ITEM_ID; 
	private final int VIEW_ITEM_ID;
	private final Context mContext;
	
	/**
	 * 
	 * @param itemClickId id to use to mark a channel item click.
	 */
	public ChannelItemClickHandler(Context context, int playItemId, int viewItemID){
		this.PLAY_ITEM_ID = playItemId;
		this.VIEW_ITEM_ID = viewItemID;
		this.mContext = context;
	}
	
	/**
	 * Check if the listener can handle the click type.
	 * @param clickType
	 * @return
	 */
	public boolean canHandle(final int clickType){
		if(clickType == PLAY_ITEM_ID || clickType == VIEW_ITEM_ID /*|| any other item id's*/){
			return true;
		}else return false;
	}
	
	/**
	 * Handle the click type
	 * @param clickType
	 * @param itemId
	 * @return
	 */
	public boolean onItemClick(final int clickType, final long itemId){
		if(clickType == PLAY_ITEM_ID){
			playStream(itemId);
			return true;
		}else if(clickType == VIEW_ITEM_ID){
			viewItem(itemId);
			return true;
		}
		return false;
	}
	
	/**
	 * Play the stream included in the inclosure.
	 * @param itemId
	 */
	protected void playStream(long itemId) {
		Uri itemUri = ContentUris.withAppendedId(Item.CONTENT_URI, itemId);
		Context context = mContext;
		Intent systemDefault = null;
		String type = null, dataUri = null;

		Cursor c = context.getContentResolver().query(itemUri, 
				new String[]{Item.ENC_LINK, Item.ENC_TYPE},
				null, null, null);
		
		
		if(c.getCount() > 0){
			c.moveToFirst();
			dataUri = c.getString(c.getColumnIndex(Item.ENC_LINK));
			type = c.getString(c.getColumnIndex(Item.ENC_TYPE));
		
			systemDefault = new Intent(Intent.ACTION_VIEW);
			systemDefault.setDataAndType(Uri.parse(dataUri), type);
		}
		
		//close the cursor before starting the intent.
		c.close();
		if(systemDefault != null){
			context.startActivity(systemDefault);
		}else{
			Toast.makeText(context, "No media found to play", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Fire an intent to view the item.
	 * @param itemId
	 */
	protected void viewItem(long itemId){
		mContext.startActivity(ItemInformationView.makeIntent(mContext, itemId));
	}
}
