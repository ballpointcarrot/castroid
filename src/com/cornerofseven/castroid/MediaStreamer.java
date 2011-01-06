/*
 * Copyright 2010 Christopher Kruse and Sean Mooney

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
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.VideoView;

public class MediaStreamer extends Activity{

	public static final String STREAM_ADDRESS = "streamaddr";
	public static final String TITLE = "title";
	
	protected TextView mBanner;
	protected VideoView mVideo;
	
	@Override	
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.player);
		mBanner = (TextView)findViewById(R.id.mediaplaying);
		mVideo = (VideoView)findViewById(R.id.mediaplayer);
		
		
		//Local Refs
		TextView banner = mBanner;
		VideoView view = mVideo;
		
		view.setBackgroundColor(Color.CYAN);
		installOnPlayListeners();
		
		Bundle extras = getIntent().getExtras();
		
		String addr = extras.getString(STREAM_ADDRESS);
		String title = extras.getString(TITLE);
		
		if(addr!=null){
			if(title!=null){
				banner.setText(title);
			}else{
				banner.setText(addr);
			}
			
			Uri addrUri = Uri.parse(addr);
			view.setVideoURI(addrUri);
			view.start();
		}else{
			banner.setText("Nothing to stream");
		}
	}
	
	private void installOnPlayListeners(){
		mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			
			@Override
			public final void onPrepared(MediaPlayer mp) {
				mVideo.setBackgroundColor(Color.TRANSPARENT);
			}
		});
	}
}
