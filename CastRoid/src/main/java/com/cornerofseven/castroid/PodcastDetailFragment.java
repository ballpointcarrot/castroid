package com.cornerofseven.castroid;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.Item;
import com.cornerofseven.castroid.network.AsyncImageDownloader;

/**
 * A fragment representing a single Podcast detail screen.
 * This fragment is either contained in a {@link PodcastListActivity}
 * in two-pane mode (on tablets) or a {@link PodcastDetailActivity}
 * on handsets.
 */
public class PodcastDetailFragment extends Fragment {

    ////////////////Widgets we care about////////////
    private ImageView mChannelImage = null;
    private ListView mChannelItems = null;
    private TextView mChannelName = null;
    private TextView mChannelDesc = null;
    ///////////////END Widgets//////////////////////

    private SimpleCursorAdapter mItemAdapter;

    static final long NO_FEED_ID = Long.MIN_VALUE;

    private static final int LOADER_FEED = 0;
    private static final int LOADER_FEED_ITEMS = 1;

	private final int MAX_IMAGE_WIDTH = 75;
	private final int MAX_IMAGE_HEIGHT = 75;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_FEED_ID = "feed_id";

    private long mItem;
    private final LoaderManager.LoaderCallbacks<Cursor> mFeedLoaderCallbacks
            = new FeedInfoLoaderCallbacks();
    private final LoaderManager.LoaderCallbacks<Cursor> mItemLoaderCallbacks
            = new FeedItemLoadCallbacks();
    private final String[] itemProjection = new String[] {Item.DESC, Item.PUB_DATE};

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PodcastDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args.containsKey(ARG_FEED_ID)) {
            mItem = getArguments().getLong(ARG_FEED_ID);
        } else {
            mItem = NO_FEED_ID;
        }

        mItemAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.item_view,
                null,
                itemProjection,
                new int[]{R.id.item_textview, R.id.item_date},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.channel_view, container, false);
        collectWidgets(rootView);

        mChannelItems.setAdapter(mItemAdapter);

        Bundle args = new Bundle();
        args.putLong(FeedItemLoadCallbacks.KEY_FEED_ID, mItem);
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(LOADER_FEED, args, mFeedLoaderCallbacks);
        loaderManager.initLoader(LOADER_FEED_ITEMS, args, mItemLoaderCallbacks);



        //install the item listener. (Same listener from CastRoid)
        mChannelItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public final void onItemClick(final AdapterView<?> arg0, final View arg1,
                                          final int arg2, final long itemId) {
                //itemClickListener.onItemClick(VIEW_ITEM, itemId);
            }
        });

        return rootView;
    }
    /**
     * Set the widget fields with the widgets from the view.
     */
    protected void collectWidgets(View rootView){
        mChannelImage = (ImageView)(rootView.findViewById(R.id.cv_rssicon));
        mChannelItems = (ListView)(rootView.findViewById(R.id.cv_channel_items));
        mChannelName = (TextView)(rootView.findViewById(R.id.cv_channel_title));
        mChannelDesc = (TextView)(rootView.findViewById(R.id.cv_channel_desc));
    }

	/**
	 * Inflate, download, or supply default image for the channel.
	 * 
	 * Decision structure:
	 * either the channel listed an image link or it didn't
	 * 
	 * if no image link, return the default.
	 * 
	 * if \exists link,
	 * either image is cached or needs to be downloaded.
	 * 
	 * If cached, simply inflate/instaniate/load from storage.
	 * If needs download, fetch on separate thread, stick in the cache
	 * and notify a callback to change the image once it exists.
	 * 
	 *
	 * 
	 * @param channelId
	 * @return
	 */
	protected void loadImage(long channelId){
		final ImageView imageView = mChannelImage;
		//TODO: Logic for download/cache
		
		imageView.setAdjustViewBounds(true);
		
		//default image.
		//imageView.setImageResource(R.drawable.podcast_image);
		imageView.setMaxWidth(MAX_IMAGE_WIDTH);
		imageView.setMaxHeight(MAX_IMAGE_HEIGHT);
		imageView.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
		
		String imageUrl = channelImageUrl(channelId);
		if(imageUrl != null){
			new AsyncImageDownloader(imageView).execute(imageUrl);
		}
	}
		
	/////////////////end life cycle///////////////////
	
	/**
	 * Lookup the image uri for
	 * @return the Url stored in the content provider for the id, or null. 
	 */
	protected String channelImageUrl(long channelid){
		String imageUrl = null;
		
		Uri contentUri = ContentUris.withAppendedId(Feed.CONTENT_URI, channelid);
		Cursor c = new CursorLoader(getActivity(),
                contentUri, new String[]{Feed.IMAGE}, null, null, null).loadInBackground();
		
		if(c.moveToFirst()){
			imageUrl  = c.getString(c.getColumnIndex(Feed.IMAGE));
		}

        c.close();
		
		return imageUrl;
	}

    private class FeedItemLoadCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        static final String KEY_FEED_ID = "key.feed.id";

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            mItemAdapter.swapCursor(cursor);
        }

        /**
         *
         * @param i
         * @param bundle must set the feed's id into the bundle on KEY_FEED_ID
         * @return
         */
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

            long feedId = bundle.getLong(KEY_FEED_ID);

            //get the feed's items
            final String SELECT_ITEMS = Item.OWNER + " = ?";
            final String[] selectionArgs = new String[] { Long
                    .toString(feedId) };
            final CursorLoader cursorLoader = new CursorLoader(
                    getActivity(),
                    Item.CONTENT_URI, Item.PROJECTION,
                    SELECT_ITEMS, selectionArgs, Item.DEFAULT_SORT);
            return cursorLoader;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            mItemAdapter.swapCursor(null);
        }
    }

    private class FeedInfoLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>{
        static final String KEY_URI = "key_uri";

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            final String[] projection = {
                    Feed._ID,
                    Feed.IMAGE,
                    Feed.TITLE,
                    Feed.DESCRIPTION,
                    Feed.LINK
            };
            long feedId = bundle.getLong(FeedItemLoadCallbacks.KEY_FEED_ID);

            final String SELECT_ITEMS = Feed._ID + " = ?";
            final String[] selectionArgs = new String[] { Long
                    .toString(feedId) };
            final CursorLoader cursorLoader = new CursorLoader(
                    getActivity(),
                    Feed.CONTENT_URI, projection,
                    SELECT_ITEMS, selectionArgs, null);

            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            if(cursor.moveToFirst()){

                int feedId;

                //FIXME: Should already know the column idex
                final String channelTitle = cursor.getString(cursor.getColumnIndex(Feed.TITLE));
                final String channelDesc =  cursor.getString(cursor.getColumnIndex(Feed.DESCRIPTION));
                feedId = cursor.getInt(cursor.getColumnIndex(Feed._ID));

                loadImage(feedId);

                mChannelName.setText(channelTitle);
                mChannelDesc.setText(channelDesc);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            //Nothing to be done
        }
    }
}
