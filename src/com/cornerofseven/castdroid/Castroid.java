package com.cornerofseven.castdroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;


public class Castroid extends Activity {	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ExpandableListView podcastList = (ExpandableListView) 
        		findViewById(R.id.podcastList);
        ExpandableListAdapter podcastAdapter = new PodcastExpandableListAdapter();
        
    }
}