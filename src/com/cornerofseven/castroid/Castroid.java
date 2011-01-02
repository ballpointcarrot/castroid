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
import android.widget.ListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.Toast;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.data.PodcastDataProvider;
import com.cornerofseven.castroid.dialogs.DialogBuilder;
import com.cornerofseven.castroid.dialogs.DialogResult;


public class Castroid extends Activity {

    public static final String TAG = "Castroid";

    protected Button mBtnAdd;
    protected ListView mFeedList;

    /** Called when the activity is first created. */
    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            mBtnAdd = (Button)findViewById(R.id.btn_add_podcast);
            mBtnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(View v) {
                    addFeed();
                }
            });

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
            
            final int GROUP_LAYOUT = android.R.layout.simple_expandable_list_item_1;
            final String[] GROUP_FROM = {Feed.TITLE};
            final int[] GROUP_TO = {android.R.id.text1};
            final int CHILD_LAYOUT = android.R.layout.simple_expandable_list_item_1;
            final String[] CHILD_FROM = {Item.TITLE};
            final int[] CHILD_TO = {android.R.id.text1};

            ((ExpandableListView)findViewById(R.id.podcastList)).setAdapter(
            new SimpleCursorTreeAdapter(this,c,
                GROUP_LAYOUT, GROUP_FROM, GROUP_TO,
                CHILD_LAYOUT, CHILD_FROM, CHILD_TO){

                @Override
                protected Cursor getChildrenCursor(Cursor groupCursor) {
                    final String[] PROJECTION = new String[]{
                        Item._ID,
                Item.OWNER,
                Item.TITLE,
                Item.LINK,
                Item.DESC
                    };
                    final String SELECT_ITEMS = Item.OWNER + " = ?";
                    int feedId = groupCursor.getInt(groupCursor.getColumnIndex(Feed._ID));
                    String[] selectionArgs = new String[]{
                        Integer.toString(feedId)	
                    };
                    return managedQuery(Item.CONTENT_URI, PROJECTION,
                        SELECT_ITEMS, selectionArgs, Item.DEFAULT_SORT);
                }

            });
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


    //TODO: Context menu to delete feeds/items from the database

}
