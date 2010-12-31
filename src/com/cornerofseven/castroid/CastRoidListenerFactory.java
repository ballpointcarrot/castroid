package com.cornerofseven.castroid;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.PodcastDataProvider;
import com.cornerofseven.castroid.dialogs.DialogBuilder;
import com.cornerofseven.castroid.dialogs.DialogResult;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * A common place to define and provide access methods to
 * all the listeners in the system.
 * 
 * All the methods return singletons.  If the same event needs to
 * be handeled in multiple places, the getter can be called multiple
 * times and always returns the same object.
 * @author Sean Mooney
 *
 */
public class CastRoidListenerFactory {

	/**
	 * Get the click listener instance for adding a new feed to the database.
	 * @return
	 */
	public static View.OnClickListener getAddFeedListener(){
		return AddFeedListener.instance;
	}
	
	/**
	 * Get the click listener for downloading a podcast.
	 * @return
	 */
	public static View.OnClickListener getDownloadItemListener(){
		return DownloadItemListener.instance;
	}
	
	private static class AddFeedListener implements View.OnClickListener{
		static final View.OnClickListener instance;
		static final String TAG = "AddFeedListener";
		
		static{
			//load the singleton the first time the instance is accessed.
			//saves us a null check,  auto inits the field, and allows us
			//to make the field final
			instance = new AddFeedListener();
		}
		

		@Override
		public void onClick(final View v) {
			
			final Context context = v.getContext();
			
			//Toast.makeText(context, "Somebody told me, to add a podcast", Toast.LENGTH_LONG).show();
		
			final DialogResult<String> dialogResult = new DialogResult<String>();
			
			/**
			 * Create the okayListener to be decorated by the default
			 * listener in the Dialog builder.
			 * It uses the dialogResult object to communicate between
			 * the 'built-in' listener and this on that actually does some work.
			 */
			final DialogInterface.OnClickListener okayListener = 
				new DialogInterface.OnClickListener() {
				
				@Override
				public final void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "Adding a new feed to the database");
					String url = dialogResult.getResult().trim();
					
					ContentResolver resolver = v.getContext().getContentResolver();
					ContentValues values = new ContentValues();
					values.put(Feed.TITLE, url);
					
					Uri result = resolver.insert(Feed.CONTENT_URI, values);
					Toast.makeText(context, "Added " + result.toString(), Toast.LENGTH_SHORT).show();
				}
			};
			
			final DialogInterface.OnClickListener cancelListener =
				new DialogInterface.OnClickListener(){
					@Override
					public final void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "Canceled input");
					}
			};
			
			Dialog input = DialogBuilder.makeInputDialog(v.getContext(), 
					"Add Feed", "Feed URL", okayListener, cancelListener, 
					false, dialogResult);
			
			input.show();
		}
		
		//Hide the constructor.  This is a singleton.
		private AddFeedListener(){}
	}

	private static class DownloadItemListener implements View.OnClickListener{
		private static final DownloadItemListener instance;
		
		static{
			instance = new DownloadItemListener();
		}

		/* (non-Javadoc)
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View arg0) {
			final Context context = arg0.getContext();
			String itemToDownload = "?";
			Toast.makeText(context, "Download " + itemToDownload, Toast.LENGTH_SHORT).show();
		}
	}
}
