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

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.data.PodcastDAO;
import com.cornerofseven.castroid.rss.RSSFeedBuilder;
import com.cornerofseven.castroid.rss.RSSProcessor;
import com.cornerofseven.castroid.rss.RSSProcessorFactory;
import com.cornerofseven.castroid.rss.feed.RSSChannel;
import com.cornerofseven.castroid.rss.feed.RSSItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * An activity for adding a new feed to the system.
 * 
 * The activity allows a user to enter the URL for a podcast and
 * then display the information for the feed, or tells the user
 * the URL was invalid.
 *
 * @author Sean Mooney
 */
public class NewFeed extends Activity{

    private static final String TAG = "NewFeed"; 

    private static final int DIALOG_PROGRESS_ID = 1;
    
    //The controls we will need from the activity's view//
    private Button mCreate = null;
    private EditText mInputText;
    private TextView mFeedTitle; 
    private TextView mFeedDesc;
    private TextView mFeedLink;
    private ListView mFeedItems;
    private Button mSaveFeed;
    ///////////////////////////////////////////////////////

    private RSSChannel mFeed = null;

    private AsyncFeedCheck mFeedCheck = null;
    
    private ProgressDialog mProgressDialog = null;
    //An object we can lock on when accessing the dialog.
    private final Object pdLock = new Object();
    
    
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        //find the NewFeed interface.
        setContentView(R.layout.add_feed_screen); 

        //find all the controls in the view
        mCreate = (Button)findViewById(R.id.afs_check);
        mInputText = (EditText)findViewById(R.id.afs_url);
        mFeedTitle = (TextView)findViewById(R.id.feed_info_title);
        mFeedLink = (TextView)findViewById(R.id.feed_info_link);
        mFeedDesc = (TextView)findViewById(R.id.feed_info_desc);
        mFeedItems = (ListView)findViewById(R.id.afs_items);
        mSaveFeed = (Button)findViewById(R.id.afs_savefeed);

        final EditText input = mInputText;

        //connect the CheckFeed button on click action.
        mCreate.setOnClickListener(new View.OnClickListener() {
            /**
             * Redirect to the class method to handel checking,
             * parsing, display, etc. a new feed.
             */
            @Override
            public final void onClick(View v) {
                loadFeedOrError(input.getText().toString());
            }
        });

        mSaveFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRSSFeed();
            }
        });

        //all done!
    }

    /**
     * Create a dialog object for the activity.
     * 
     * {@link DIALOG_PROGRESS_ID} create the progress dialog.
     * 
     * @param dlgId
     * @return the dialog matching the id.
     */
    @Override
    public Dialog onCreateDialog(int dlgId){
        Dialog dlg = null;
        
        switch(dlgId){
            case DIALOG_PROGRESS_ID:
                ProgressDialog pd = new ProgressDialog(this);
                dlg = pd;
                setProgressDialog(pd);
                pd.setMessage("Checking Feed...");
                pd.setCancelable(true);
                pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                        if(mFeedCheck != null && !(mFeedCheck.getStatus() == AsyncTask.Status.FINISHED)){
                            mFeedCheck.cancel(true);
                            mFeedCheck = null; //get rid of the reference when we cancel it.
                        }
                    }
                });
                break;
//            case ERROR_DIALOG:
//            {
//            	AlertDialog
//            }
            default: dlg = super.onCreateDialog(dlgId);
        }
        
        return dlg;
    }
    
    public void setProgressDialog(ProgressDialog pd){
        synchronized (pdLock) {
            this.mProgressDialog = pd;
        }
    }
    
    public ProgressDialog getProgressDialog(){
        synchronized (pdLock) {
           return this.mProgressDialog;
        }
    }
    
    /**
     * Reset the dialog each time its shown.
     * @param dlgId
     * @param dlg
     */
    @Override
    public void onPrepareDialog(int dlgId, Dialog dlg){
        switch(dlgId){
            case DIALOG_PROGRESS_ID:
                ProgressDialog pd = (ProgressDialog)dlg;
                pd.setProgress(0);
                break;
            default: super.onPrepareDialog(dlgId, dlg);
        }
    }
    
    /**
     * Check the url given to see if it contains a valid
     * rss feed.  
     * @param uriString string representation of the Uri where the feed is located.
     */
    protected void loadFeedOrError(String urlString){
        if(mFeedCheck == null || mFeedCheck.getStatus() == AsyncTask.Status.FINISHED){
            mFeedCheck = (AsyncFeedCheck)new AsyncFeedCheck().execute(urlString);
        }else{
            Toast.makeText(this, "Already checking a feed.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Bind the rss feed information into the correct view elements
     */
    protected void bindFeedInfo(){
        final RSSChannel feed = mFeed;

        if(feed != null){
            mFeedTitle.setText(feed.getmTitle());
            mFeedDesc.setText(feed.getmDesc());
            mFeedLink.setText(feed.getmLink());

            final ListView itemView = mFeedItems;
            final ListAdapter adapter 
           = new ArrayAdapter<RSSItem>(this, android.R.layout.simple_list_item_1, feed.itemsAsArray());
            itemView.setAdapter(adapter);
            
            
        }
    }

    /**
     * Save the feed into the dataprovider
     */
    protected void saveRSSFeed(){
        if(mFeed == null){ //check the feed if the user didn't
            //try to load the feed on save.
            loadFeedOrError(mInputText.getText().toString());
        }

        //TODO: Need to wait until the async worker finishes if we are loading the 
        //podcast.  Need to do so in such a way that isn't a busy wait, or hangs the GUI thread
        //call back, somewhere?
        
        
        //only save and finish the activity if something was loaded.
        //preserve the activity if a feed couldn't be processed.
        if(mFeed != null){
            ContentResolver content = getContentResolver();
            if(!PodcastDAO.addRSS(content, mFeed)){
                Toast.makeText(this, "Unable to add the feed", Toast.LENGTH_SHORT).show();
            }else{
                finish(); //only finish if an error didn't happen
            }
        }else{
            Toast.makeText(this, "No feed to save", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Utility method to show a toast message.
     * 
     * Having an extra method serves two purpsoses. 
     * The first is so that showing the messaging is automatic.
     * The second, and more important one, is to let the worker
     * thread send messages back the main context and not crash
     * the program. 
     * TODO: Except, it doesn't currently do that. Still crashing...
     * @param msg
     * @param length
     */
    protected void showMessage(String msg){
    	
    }
    
    
    /**
     * An asynchronous method to connect to/download/parse a feed.
     * @author sean
     *
     */
    private class AsyncFeedCheck extends AsyncTask<String, String, RSSChannel> {
    	
        @Override
        protected void onPreExecute(){
            showDialog(DIALOG_PROGRESS_ID);
        }
        
        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected RSSChannel doInBackground(String... args) {
            /*General idea
             * -> Convert the String to a URI object.
             * -> Pass the URI as an argument to the RSS feed creator
             * -> Let the Feed creator run. It will signal any problems with the url
             * -> If no problems, keep track of the feed, so we don't have to reprossess and display its information
             */

            String urlString = args[0];
            
            try{
                //TODO: Delete me!
                Log.d(TAG, "Checking " + urlString);

                URL feedLocation = new URL(urlString);
                Log.i(TAG, feedLocation.toString());
                //TODO: Make strings into resources
                publishProgress("Connecting to RSS feed");
                RSSProcessor processor = RSSProcessorFactory.getRSS2_0Processor(feedLocation);
                Log.d(TAG, "Using processor " + processor.getClass().toString());
                
                publishProgress("Reading RSS feed");
                processor.process();
                RSSFeedBuilder builder = processor.getBuilder();

                publishProgress("Done!");
               return builder.getFeed();
                
            }catch(UnknownHostException uhe){
            	//TODO: Crashes on call.
                showMessage("Unknown host ");
            }
            catch(Exception ex){
                Log.e(TAG, ex.getClass().toString());
                Log.e(TAG, ex.getMessage());
                //TODO: Crashes on call. Issues with second thread.
                showMessage("Unable to parse the feed\n " 
                        + ex.getMessage());
            }
            
            return null;
        }
        
        @Override
        protected void onProgressUpdate(String... msgs){
            ProgressDialog pd = getProgressDialog();
            if(pd != null){
                pd.setMessage(msgs[0]);
            }
        }
        
        protected void onPostExecute(RSSChannel channel){
            mFeed = channel;
            bindFeedInfo();
            dismissDialog(DIALOG_PROGRESS_ID);
        }
    }
}
