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
import java.util.concurrent.atomic.AtomicBoolean;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cornerofseven.castroid.dialogs.DownloadDialog;

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
	
	/**
	 * This field will be accessed by multiple threads. 
	 */
	private AtomicBoolean isCanceled = new AtomicBoolean(false);
	
	/**
	 * Download the file pointed to by the item enclosure url field.
	 * @param c
	 * @param itemID
	 * @param progressDialogID
	 */
	public boolean downloadItem(Uri dlUri, String dlPath, Handler hand){
		
		File sdCardRoot = Environment.getExternalStorageDirectory();
		
		String fileName = dlUri.getLastPathSegment();
		File dataDir = new File(sdCardRoot, dlPath);
		
		if(!dataDir.canWrite()){
		    Log.e(TAG, "Cannot write to " + dataDir.getAbsolutePath());
            Bundle b = new Bundle();
            b.putBoolean(DownloadDialog.PROGRESS_DONE, false);
            signalHandler(hand, DownloadDialog.WHAT_DONE, b);
            return false;
		}
		
		//we only need to create the directory if it doesn't exist...how silly of me...
		if(!dataDir.exists()) {
		    if(!dataDir.mkdirs()) {
		        Log.e(TAG, "Unable to create " + dataDir.getAbsolutePath());
		        Bundle b = new Bundle();
		        b.putBoolean(DownloadDialog.PROGRESS_DONE, false);
		        signalHandler(hand, DownloadDialog.WHAT_DONE, b);
		        return false;
		    }
		}
		File dest = new File(dataDir, fileName);
		
		return downloadFile(dlUri.toString(), dest, hand);
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
				Message msg = handler.obtainMessage(DownloadDialog.WHAT_START);
				Bundle b = new Bundle();
				b.putInt(DownloadDialog.PROGRESS_MAX, totalSize);
				msg.setData(b);
				handler.sendMessage(msg);
			}

			//we'll do this the old fashioned way...copy buffered bytes from inputstream to output stream
			while( !isCanceled.get() && ( bytesRead = bInputStream.read(buffer)) > 0){
				bOutputStream.write(buffer, 0, bytesRead);
				downloadSize += bytesRead;
				bytesSinceLastLog += bytesRead;
				
				
				if(bytesSinceLastLog > K_PER_UPDATE){
					//float per = (float)downloadSize / (float)totalSize;
					//Log.i(TAG, "Downloaded " + downloadSize + " bytes " + per + "%");
					bytesSinceLastLog = 0;
					
					if(handler != null){
						Message msg = handler.obtainMessage(DownloadDialog.WHAT_UPDATE);
						Bundle b = new Bundle();
						b.putInt(DownloadDialog.PROGRESS_UPDATE, downloadSize);
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
			Message msg;
			Bundle b = new Bundle();
			boolean isCanceled = this.isCanceled.get();
			
			/*TODO: This could return false if
			* canceled at the last moment even thouh the download finished. 
			* That is, \exists a thread
			* interleaving such that isCanceled is set after the last time
			* the cancel flag is checked but before the while loop terminates.
			* 
			* I'm not sure if this is a problem or not...
			*/
			success = success && !isCanceled;
			
			if(isCanceled){
			    msg = handler.obtainMessage(DownloadDialog.WHAT_CANCELED);
			}else {
			    msg = handler.obtainMessage(DownloadDialog.WHAT_DONE);
			}
			    
			b.putBoolean(DownloadDialog.PROGRESS_DONE, success);
			msg.setData(b);
			handler.sendMessage(msg);
		}
		
		return success;
	}
	
	/**
	 * Tell the download it should cancel itself.
	 */
	public void cancelDownload(){
	    isCanceled.set(true);
	}
	
	public void signalHandler(Handler handler, int msgType, Bundle data){
	    if(handler != null) {
	        Message msg = handler.obtainMessage(msgType);
	        msg.setData(data);
	        handler.sendMessage(msg);
	    }
	}
	
	/**
     * Create a separate thread to run the down load on.
     * 
     * @author Sean Mooney
     * 
     */
    private static class DownloadThread extends Thread {

        /**
         * Track how many total instances of the object are constructed.
         * Convenient way to make a unique conter/threadname
         */
        private static int DLThreadCount = 1;
        
        private DownloadManager mDownloader;
        private Uri dlUri;
        private Handler hand = null;
        private String dlDir = "";
        public DownloadThread(DownloadManager downloader, Uri downloadUri) {
            super("DOWNLOAD-" + DLThreadCount++);
            this.mDownloader = downloader;
            dlUri = downloadUri;
        }

        public void setHandler(Handler hand){
            this.hand = hand;
        }
        
        public void setDownloadDir(String dlDir){
            this.dlDir = dlDir;
        }
        
        @Override
        public void run() {
            mDownloader.downloadItem(dlUri, dlDir, hand);
        }
    }
}
