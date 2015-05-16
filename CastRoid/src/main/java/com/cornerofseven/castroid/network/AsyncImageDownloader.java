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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.cornerofseven.castroid.Castroid;

/**
 * Defines an AsyncTask that can be to download an image on a 
 * separate thread and bind data into an image view.
 * 
 * @author Sean Mooney
 * TODO: Test, especially failures (no network, corrupt file, can't find file).
 */
public class AsyncImageDownloader extends AsyncTask<String, Void, Bitmap>{

	private ImageView mImageView = null;
	
	static final String TAG = Castroid.TAG;
	
	/**
	 * 
	 * @param imageView the image view the downloaded image will be bound to.
	 */
	public AsyncImageDownloader(ImageView imageView){
		mImageView = imageView;
	}
	
	@Override
	protected Bitmap doInBackground(String... urls) {
		return loadImageFromNetwork(urls[0]);
	}

	/**
	 * Bind the downloaded image to the image view.
	 */
	protected void onPostExecute(Bitmap image){
		if(image != null && mImageView != null){
			mImageView.setImageBitmap(image);
		}
		
	}
	
	/**
	 * Retrieve the image at the other end of the url and
	 * create a bitmap from it.
	 * @param url
	 * @return
	 */
	private Bitmap loadImageFromNetwork(String url){
    	Bitmap bm = null;
    	BufferedInputStream bis = null;
    	InputStream is = null;
    	try{
    		URL aURL = new URL(url); 
            URLConnection conn = aURL.openConnection(); 
            conn.connect(); 
            is = conn.getInputStream(); 
            bis = new BufferedInputStream(is); 
            bm = BitmapFactory.decodeStream(bis); 
    	}catch(IOException ex){
    		Log.e(TAG, ex.getMessage());
    	}finally{
    		if(bis != null)
				try {
					bis.close();
				} catch (IOException e) {} 
            if(is != null)
				try {
					is.close();
				} catch (IOException e) {}    		
    	}
    	
    	return bm;
    }
}

