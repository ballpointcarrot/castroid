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
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.player.StreamingMediaPlayer;

/**
 * 
 * @author sean
 *
 */
public class MediaStreamer extends Activity{

    public static final String ITEM_ID = "itemid";

    public static final String TAG = "MediaStreamer";


    protected TextView mBanner;
    protected StreamingMediaPlayer mVideo;

    /////////////////////playing item information///////////////
    protected long mItemId = -1L;
    protected String mEncUri = "";
    protected String mItemName = null;
    protected int mPlaystart = -1;

    @Override	
    public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.player);
        mBanner = (TextView)findViewById(R.id.mediaplaying);
        mVideo = (StreamingMediaPlayer)findViewById(R.id.mediaplayer);


        Bundle extras = getIntent().getExtras();
        if(extras == null)return; //punch out if there are no extras, we don't know what to do.

        mItemId = extras.getLong(ITEM_ID);
        lookupItem();

        //set the background to something obnoxious until we load the stream;
        mVideo.setBackgroundColor(Color.CYAN);

        installOnPlayListeners();
        installPlayFinishedListeners();

        setTitle();
        startPlayback();
    }

    private void setTitle(){
        mBanner.setText(mItemName);
    }

    private void startPlayback(){
        String addr = mEncUri;
        StreamingMediaPlayer view = mVideo;
        if(addr!=null){
            Uri addrUri = Uri.parse(addr);
            view.setVideoURI(addrUri);

            if(mPlaystart > 0/* && view.canSeekForward()*/){
                Log.i(TAG, "Moving playback to " + mPlaystart);
                view.seekTo(mPlaystart);
            }

            view.start();
        }else{
            Toast.makeText(this, "Nothing to play", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Make sure the background is transparent when we play.
     */
    private void installOnPlayListeners(){
        mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public final void onPrepared(MediaPlayer mp) {
                mVideo.setBackgroundColor(Color.TRANSPARENT);
            }
        });
    }

    /**
     * Listeners for when playback ends
     */
    private void installPlayFinishedListeners(){
        mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            /**
             * When playback ends, reset the playhead for the item and finish the activity.
             */
            public void onCompletion(MediaPlayer paramMediaPlayer) {
                savePlaybackPosition(-1);
                finish();
            }
        });
    }

    /**
     * Set the fields pertaining to the item we are playing.
     * 
     * Assumes the {@link mItemId} has been set.
     * @return
     */
    private boolean lookupItem(){
        long itemId = mItemId;
        final String[] itemProject = {Item._ID, Item.TITLE, Item.ENC_LINK, Item.STREAM_POS};
        final String selection = Item._ID + " =?";
        final String[] selectionArgs = {Long.toString(itemId)};

        Uri itemInfoUri = ContentUris.withAppendedId(Item.CONTENT_URI, itemId);
        Cursor itemInformation = managedQuery(itemInfoUri, itemProject, 
                selection, selectionArgs, Item.DEFAULT_SORT);

        if(!itemInformation.moveToFirst()){
            return false; //didn't find anything
        }

        mItemName = itemInformation.getString(itemInformation.getColumnIndex(Item.TITLE));
        mEncUri= itemInformation.getString(itemInformation.getColumnIndex(Item.ENC_LINK));
        mPlaystart = itemInformation.getInt(itemInformation.getColumnIndex(Item.STREAM_POS));

        return true;
    }

    /**
     * Write the playback position back to the data provider.
     */
    protected void savePlaybackPosition(int currentPos){
        ContentValues values = new ContentValues();
        values.put(Item.STREAM_POS, currentPos);

        Uri updateUri = ContentUris.withAppendedId(Item.CONTENT_URI, mItemId);
        getContentResolver().update(updateUri, values, null, null);

    }

    ///////////////////////LIFE CYCLE MANAGEMENT////////////////////////////
    @Override
    protected void onPause(){
        super.onPause();
        pausePlayback();
    }



    @Override 
    protected void onResume(){
        super.onResume();
        resumePlayback();
    }

    /**
     * 
     */
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
    }

    /**
     * 
     */
    private void pausePlayback() {
        mVideo.pause();
        saveState();
    }

    /**
     * 
     */
    private void resumePlayback(){
        mVideo.resume();
    }

    /**
     * Save the state of the player, such that we can resume playback
     * at the same place we were.
     * 
     * If the video was playing, save the current playback head position back to the dataprovider.
     */
    private void saveState(){
        StreamingMediaPlayer video = mVideo;
        int currentPos = video.getCurrentPosition();
        savePlaybackPosition(currentPos);		
        Log.i(TAG, "Video is at: " + currentPos);
    }

    ////////////////////////END LIFE CYCLE//////////////////////////////////
}
