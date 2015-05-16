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

import java.io.File;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.cornerofseven.castroid.ItemInformationView;
import com.cornerofseven.castroid.R;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.network.DownloadService;

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
	private final int DOWNLOAD_ITEM_ID;
	private final FragmentActivity mActivity;

	public ChannelItemClickHandler(FragmentActivity activity, int playItemId, int viewItemID, int downloadItemId){
		this.PLAY_ITEM_ID = playItemId;
		this.VIEW_ITEM_ID = viewItemID;
		this.DOWNLOAD_ITEM_ID = downloadItemId;
		this.mActivity = activity;
	}
	
	/**
	 * Check if the listener can handle the click type.
	 * @param clickType
	 * @return
	 */
	public boolean canHandle(final int clickType){
		if(clickType == PLAY_ITEM_ID || clickType == VIEW_ITEM_ID  || clickType == DOWNLOAD_ITEM_ID/*|| any other item id's*/){
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
		}else if(clickType == DOWNLOAD_ITEM_ID){
		    downloadItem(itemId);
		}
		return false;
	}
	
	/**
	 * Play the stream included in the inclosure.
	 * @param itemId
	 */
	protected void playStream(long itemId) {
		Uri itemUri = ContentUris.withAppendedId(Item.CONTENT_URI, itemId);
		Context context = mActivity;
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

        Bundle args = ItemInformationView.createArgs(itemId);
        ItemInformationView iiv = new ItemInformationView();
        iiv.setArguments(args);

        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
	    //FIXME: NO COMMIT
        transaction.replace(R.id.podcast_detail_container, iiv)
                .addToBackStack(null)
                .commit();
    }
	
	/**
	 * Start the item downloading in a seperate service.
	 * @param itemId
	 */
	@SuppressLint("NewApi")
    protected void downloadItem(long itemId){
	    
	    String[] dlInfo = getDownloadInfo(itemId);
	    
	    Object systemDM =  mActivity.getSystemService("download");
	    if(systemDM != null){ //for 2.3+ android systems.
	        DownloadManager sdm = (DownloadManager)systemDM;
	        DownloadManager.Request dmr = new DownloadManager.Request(Uri.parse(dlInfo[0]));
	        dmr.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS, "Podcasts");
	        dmr.setTitle(dlInfo[1]);
	        //TODO: Run media scanner when done? Docs shows this method exists,
	        // but it won't to compile
	        //dmr.allowScanningByMediaScanner();
	        sdm.enqueue(dmr);
	    }else{ //2.2 or less systems.
	        File dlFolder = new File(Environment.getExternalStorageDirectory(), "Podcasts");
	        Intent downloadIntent = new Intent(mActivity, DownloadService.class);
	        downloadIntent.setData(Uri.parse(dlInfo[0]));
	        downloadIntent.putExtra(DownloadService.INT_DOWNLOAD_FOLDER, dlFolder.getAbsolutePath());
	        mActivity.startService(downloadIntent);
	    }
	}
	
	/**
     * Retrieves the Item's Enclosure download link.
     * 
     * @param itemID
     *            the id of the item.
     * @return A list download information: {link, title}.
     */
    private String[] getDownloadInfo(long itemID) {
        Uri queryUri = ContentUris.withAppendedId(Item.CONTENT_URI, itemID);
        Cursor c = mActivity.managedQuery(queryUri,
                new String[] { Item._ID, Item.TITLE, Item.ENC_LINK, Item.ENC_SIZE }, null,
                null, null);

        c.moveToFirst();
        String dlLnk = c.getString(c.getColumnIndex(Item.ENC_LINK));
        String dlTitle = c.getString(c.getColumnIndex(Item.TITLE));
        return new String[]{dlLnk, dlTitle};
    }
}
