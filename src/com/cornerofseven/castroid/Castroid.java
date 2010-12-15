package com.cornerofseven.castroid;

import com.cornerofseven.castroid.data.PodcastDataProvider;
import com.cornerofseven.castroid.dialogs.DialogBuilder;
import com.cornerofseven.castroid.dialogs.DialogHelpers;
import com.cornerofseven.castroid.dialogs.DialogResult;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;


public class Castroid extends Activity {
	
	protected PodcastDataProvider mDataProvider;
	protected Button mBtnAdd;
	
	/**
	 * ID Number for the input RSS dialog
	 */
	private static final int DLG_INPUT_RSS = 1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDataProvider = new PodcastDataProvider(this);
        mBtnAdd = (Button)findViewById(R.id.btn_add_podcast);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public final void onClick(View v) {
				showDialog(DLG_INPUT_RSS);
			}
		});
        
//        ExpandableListView podcastList = (ExpandableListView) 
//        		findViewById(R.id.podcastList);
//        podcastList.setAdapter(new PodcastExpandableListAdapter(this));
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
}
