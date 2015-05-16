package com.cornerofseven.castroid;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.cornerofseven.castroid.data.DatabaseQuery;
import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.PodcastDAO;


public class PodcastListActivity extends FragmentActivity
        implements PodcastListFragment.Callbacks
{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast_list);

        if (findViewById(R.id.podcast_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((PodcastListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.podcast_list))
                    .setActivateOnItemClick(true);
        }
    }

    /**
     * Callback method from {@link PodcastListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(long id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(PodcastDetailFragment.ARG_FEED_ID, id);
            PodcastDetailFragment fragment = new PodcastDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.podcast_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, PodcastDetailActivity.class);
            detailIntent.putExtra(PodcastDetailFragment.ARG_FEED_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.addFeed:
                addFeed();
                return true;
            case R.id.about:
                Toast.makeText(this, "About this app.", Toast.LENGTH_SHORT).show();
                //showDialog(ABOUT_DIALOG_ID);
                return true;
            case R.id.updateAll:
                updateAllChannels();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void addFeed() {
        Intent intent = new Intent(this, NewFeed.class);
        startActivity(intent);
    }

    /**
     * Update all the channels in the database.
     * Runs asynchronously.
     */
    protected void updateAllChannels(){
        final Activity activity = this; //bind the context for the thread.

        DatabaseQuery feedIdsQ = PodcastDAO.getFeedIdsQuery();
        Cursor c = activity.managedQuery(
                feedIdsQ.getContentUri(),
                feedIdsQ.getProjection(),
                feedIdsQ.getSelection(),
                feedIdsQ.getSelectionArgs(),
                feedIdsQ.getSortOrder());

        //marshal the data for the updateChannel method.
        Integer[] feedIds = new Integer[c.getCount()];
        int curIndex = 0;
        int feedCol = c.getColumnIndex(Feed._ID);
        while(c.moveToNext()){
            feedIds[curIndex++] = c.getInt(feedCol);
        }

        updateChannel(feedIds);
    }

    /**
     * Update the selected feed(s).
     *
     *  Can update multiple feeds on the same call.
     *  Use this if/when the "update all" feature is added.
     *
     * @param feedIds
     */
    protected void updateChannel(Integer... feedIds){
        new AsyncFeedUpdater(this).execute(feedIds);
    }

}
