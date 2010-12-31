package com.cornerofseven.castroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.PodcastDataProvider;
import com.cornerofseven.castroid.dialogs.DialogBuilder;
import com.cornerofseven.castroid.dialogs.DialogResult;


public class Castroid extends Activity {
	
	public static final String TAG = "Castroid";
	
	protected PodcastDataProvider mDataProvider;
	protected Button mBtnAdd;
	protected ListView mFeedList;
	
	/**
	 * ID Number for the input RSS dialog
	 */
	private static final int DLG_INPUT_RSS = 1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //mDataProvider = new PodcastDataProvider(this);
        mBtnAdd = (Button)findViewById(R.id.btn_add_podcast);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public final void onClick(View v) {
				addFeed();
				//showDialog(DLG_INPUT_RSS);
			}
		});
        
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Feed.CONTENT_URI);
        }
        
//        Cursor feedCursor = managedQuery(
//        		Feed.CONTENT_URI, 
//        		new String[]{
//        				Feed._ID,
//        				Feed.TITLE
//        		}, null, null, Feed.DEFAULT_SORT);
//        
//        ExpandableListView podcastList = (ExpandableListView) 
//        		findViewById(R.id.podcastList);
//       podcastList.setAdapter(new PodcastExpandableListAdapter(this, feedCursor));
        mFeedList = (ListView)findViewById(R.id.main_feedlist);
        final String[] FEED_PROJECTION ={
        		Feed._ID,
        		Feed.TITLE,
        		Feed.DESCRIPTION,
        		Feed.LINK
        };
        Cursor c = managedQuery(Feed.CONTENT_URI, 
        		FEED_PROJECTION, null, null, null);
        c.setNotificationUri(getContentResolver(), Feed.CONTENT_URI);
        /*ListAdapter feedListAdapter = new SimpleCursorAdapter(
        		this, R.layout.feed_information, c, new String[]{
        				Feed.TITLE,
        				Feed.DESCRIPTION,
        				Feed.LINK
        		}, new int[]{
        				R.id.feed_info_title,
        				R.id.feed_info_desc,
        				R.id.feed_info_link
        		});
        		
        
        mFeedList.setAdapter(feedListAdapter);*/
        PodcastExpandableListAdapter adapter =
        	new PodcastExpandableListAdapter(this, c);
        ((ExpandableListView)findViewById(R.id.podcastList)).setAdapter(adapter);
    }

    @Override
    protected Dialog onCreateDialog(int id){
    	Dialog d = null;
    	switch(id){
    	
    	case DLG_INPUT_RSS:
    		final DialogResult<String> input = new DialogResult<String>();
    		final DialogInterface.OnClickListener action = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(input.wasOkay()){
						addPodcastURL(input.getResult());
					}
				}
			};
    		d = DialogBuilder.makeInputDialog(this, "Add podcast", "URL", action, action, true, input);
    		break;
    		default: d = null; //just to be thorough...
    	}
    	
    	return d;
    }
    
    protected void addPodcastURL(String url){
    	Toast.makeText(this, "Should add " + url, Toast.LENGTH_SHORT).show();
    }
    
    protected void addFeed(){
    	Intent intent = new Intent(this, NewFeed.class);
    	startActivity(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case R.id.addFeed:
			addFeed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    

    
}
