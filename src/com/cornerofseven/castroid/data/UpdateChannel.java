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
package com.cornerofseven.castroid.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.cornerofseven.castroid.rss.MalformedRSSException;
import com.cornerofseven.castroid.rss.RSSProcessor;
import com.cornerofseven.castroid.rss.RSSProcessorFactory;
import com.cornerofseven.castroid.rss.feed.RSSItem;

/**
 * Update a series of RSS channels.
 * 
 * 
 * @author Sean Mooney
 *
 *  //TODO: Unit tests.
 */
public class UpdateChannel {

    private static final String DELETE_CLAUSE = Item.OWNER + " = ?";
    private static final String[] FEED_PROJ = new String[]{Feed._ID, Feed.RSS_URL};
    public static final String TAG = "UpdateChannel";
    
    private Context mContext;
    
    
    public UpdateChannel(Context context){
        mContext = context;
    }
    
    /**
     * Refresh the database for the given channel, by channel id.
     * @param channelId
     * @throws MalformedURLException
     * @throws MalformedRSSException 
     */
    public void runUpdate(int channelId) throws MalformedURLException, MalformedRSSException{
        ContentResolver contentResolver = mContext.getContentResolver();
        
        //clear the current items.
        Log.i(TAG, "Clearing out old items");
        clearItems(contentResolver, channelId);
        
        //get the feed url
        String url = getURL(contentResolver, channelId);
        Log.i(TAG, "Lookup feed at: " + url);
        
        //process the feed with the feed builder
        //re-add all the items.
        Log.i(TAG, "Loading the new items");
        loadNewItems(contentResolver, channelId, url);
    }
    
    private void loadNewItems(ContentResolver resolver, int channelId, String url) throws MalformedURLException, MalformedRSSException{
        URL feedLocation = new URL(url);
        RSSProcessor builder = RSSProcessorFactory.getRSS2_0Processor(feedLocation);
        try {
            builder.process();
        } catch (ParserConfigurationException e) { //rethrow the problem wrapped as a MalformedRSS
            throw new MalformedRSSException(e);
        } catch (IOException e) {
            throw new MalformedRSSException(e);
        } catch (SAXException e) {
            throw new MalformedRSSException(e);
        } 
        
        for(RSSItem item : builder.getBuilder().getFeed().itemsAsArray()){
            Log.i(TAG, "Loading item " + item.toString());
            PodcastDAO.addRSSItem(resolver, channelId, item);
        }
    }
    
    
    private void clearItems(ContentResolver contentResolver, int channelId){
        String[] deleteArgs = new String[]{Integer.toString(channelId)};
    
        contentResolver.delete(Item.CONTENT_URI, DELETE_CLAUSE, deleteArgs);
    }
    
    private String getURL(ContentResolver contentResolver, int channelId){
        String channelUrl = "";
        
        Uri feedIdUri = ContentUris.withAppendedId(Feed.CONTENT_URI, channelId);
        Cursor c = contentResolver.query(feedIdUri, FEED_PROJ, null, null, null);
        if(c.getCount() >= 0){
            c.moveToFirst();
            channelUrl = c.getString(c.getColumnIndex(Feed.RSS_URL));
        }
        
        c.close();
        
        return channelUrl;
    }
}
