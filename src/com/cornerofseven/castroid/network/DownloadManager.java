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
package com.cornerofseven.castroid.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cornerofseven.castroid.R;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.dialogs.ProgressBarHandler;

/**
 * Download manager for download podcast items.
 * 
 * Hmm. design check point. We had talked about creating a play list
 * and giving it to the system player, which would let the system manage, stream
 * whatever, the media.  Is this unnecessary?
 * 
 * TODO: cancelibility
 * 
 * @author Sean Mooney
 *
 */
public class DownloadManager {

	private static String TAG = "DownloadManager";
	
	//TODO: Is this a memory leak?
	//Do we need to use a context for the backing?
	//Overall, this feels like bad/uninformed design right now.
	private Context mContext;
	public DownloadManager(Context context){
		this.mContext = context;
	}
	
	/**
	 * Download the file pointed to by the item enclosure url field.
	 * @param c
	 * @param itemID
	 * @param progressDialogID
	 */
	public boolean downloadItemEnc(long itemID, Handler handler){
		String downloadLink;
		
		Uri queryUri = ContentUris.withAppendedId(Item.CONTENT_URI, itemID);
		Cursor c = mContext.getContentResolver().query(
				queryUri, 
				new String[]{Item._ID, Item.ENC_LINK, Item.ENC_SIZE}, 
				null, null, null);
		
		c.moveToFirst();
		downloadLink = c.getString(c.getColumnIndex(Item.ENC_LINK));
		c.close();
		
		File sdCardRoot = Environment.getExternalStorageDirectory();
		//TODO: Don't clobber existing files!
		String fileName = "";
		
		Uri fileUri = Uri.parse(downloadLink);
		fileName = fileUri.getLastPathSegment();
		File dataDir = new File(sdCardRoot, mContext.getString(R.string.download_root_dir));
		if(!dataDir.mkdirs()){
			Log.e(TAG, "Unable to create " + dataDir.getAbsolutePath());
		}
		File dest = new File(dataDir, fileName);
		
		return downloadFile(downloadLink, dest, handler);
		//Toast.makeText(context, "Download " + downloadLink, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Download a given URL to a file.
	 * based on stack overflow example: @{link http://stackoverflow.com/questions/3287795/android-download-service}
	 */
	public boolean downloadFile( String link, File dest, Handler handler)
	{
		BufferedInputStream bInputStream = null;
		BufferedOutputStream bOutputStream = null;
		boolean success = false;
		
		try{
			URL url = new URL(link);
			FileOutputStream fileOutput = new FileOutputStream(dest);
			
			HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoInput(true);
			
			urlConnection.connect();
			
			InputStream inputStream = urlConnection.getInputStream();
			
			//TODO: Tune buffer sizes
			final int BUFFER_SIZE = 100 * 1024; //100K buffer;
			
			//buffer both streams.
			bInputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);
			bOutputStream = new BufferedOutputStream(fileOutput, BUFFER_SIZE);
			
			int totalSize = urlConnection.getContentLength();
			//TODO: How big of a file before this rolls over and is meaningless, or do we need a long?
			int downloadSize = 0;
			int bytesSinceLastLog = 0;
			
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			//update every 1 percent of total download size
			final int K_PER_UPDATE = (1024 * 100); //update every tenth meg downloaded
			
			if(handler != null){
				Message msg = handler.obtainMessage(ProgressBarHandler.WHAT_START);
				Bundle b = new Bundle();
				b.putInt(ProgressBarHandler.PROGRESS_MAX, totalSize);
				msg.setData(b);
				handler.sendMessage(msg);
			}

			//we'll do this the old fashioned way...copy buffered bytes from inputstream to output stream
			while( ( bytesRead = bInputStream.read(buffer)) > 0){
				
				
				bOutputStream.write(buffer, 0, bytesRead);
				downloadSize += bytesRead;
				bytesSinceLastLog += bytesRead;
				
				
				if(bytesSinceLastLog > K_PER_UPDATE){
					//float per = (float)downloadSize / (float)totalSize;
					//Log.i(TAG, "Downloaded " + downloadSize + " bytes " + per + "%");
					bytesSinceLastLog = 0;
					
					if(handler != null){
						Message msg = handler.obtainMessage(ProgressBarHandler.WHAT_UPDATE);
						Bundle b = new Bundle();
						b.putInt(ProgressBarHandler.PROGRESS_UPDATE, downloadSize);
						msg.setData(b);
						handler.sendMessage(msg);
					}
					
					//sleep briefly to let interrupts happen
					//FIXME: WRONG PLACE TO PUT THIS!
					Thread.sleep(10);
				}
			}
			success = true;
		} catch (MalformedURLException e) {  
			//TODO: Delete me
			e.printStackTrace();
		} catch (IOException e) { 
			//TODO: Delete me
			e.printStackTrace();
		} catch( InterruptedException ex){
			ex.printStackTrace();
		}
		finally{
			if(bOutputStream != null)
				try {
					bOutputStream.close();
				} catch (IOException e) {}//ignore, grumble grumble grumble
			
			if(bInputStream != null)
				try {
					bInputStream.close();
				} catch (IOException e) {}
		}
		

		if(handler != null){
			Message msg = handler.obtainMessage(ProgressBarHandler.WHAT_DONE);
			Bundle b = new Bundle();
			b.putBoolean(ProgressBarHandler.PROGRESS_DONE, success);
			msg.setData(b);
			handler.sendMessage(msg);
		}
		
		return success;
	}
}
