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
package com.cornerofseven.castroid.player;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * A simple video player view for playing media, as supported by the device.
 * 
 * Bundles the video window and controls into 
 * a single view that can be used.
 * @author Sean Mooney
 *
 */
public class StreamingMediaPlayer extends VideoView {

	private MediaController mMediaController;
	
	
	
	public StreamingMediaPlayer(Context context) {
		super(context);
		setupController(context);
	}
	
	public StreamingMediaPlayer(Context context, AttributeSet attributes){
		super(context, attributes);
		setupController(context);
	}
	
	private void setupController(Context context){
		mMediaController = new MediaController(context);
		setMediaController(mMediaController);
	}

}
