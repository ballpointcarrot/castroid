package com.cornerofseven.castroid;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cornerofseven.castroid.data.PodcastDAO;
import com.cornerofseven.castroid.data.UpdateChannel;
import com.cornerofseven.castroid.dialogs.DownloadDialog;
import com.cornerofseven.castroid.rss.MalformedRSSException;

import java.net.MalformedURLException;

/**
 * An asynchronous task for updating a feed.
 *
 * Currently in the castroid activity instead of its
 * own class (a la DownloadManager) to simplify some of the scoping
 * issues with an external class.
 * @author Sean Mooney
 *
 * TODO: KnownIssues
 * 1. Update dialog doesn't work right if this is triggered multiple times.
 */
public class AsyncFeedUpdater extends AsyncTask<Integer, Integer, Integer> {
    final Context mContext;
    final Handler mUpdateHandler;

    public AsyncFeedUpdater(Context context) {
        this(context, null);
    }

    public AsyncFeedUpdater(Context context, Handler handler) {
        this.mContext = context;
        this.mUpdateHandler = handler;
    }

    /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
    @Override
    protected Integer doInBackground(Integer... feedIds) {
        int numUpdated = 0;

        ContentResolver contentResolver = mContext.getContentResolver();
        UpdateChannel update = new UpdateChannel(contentResolver);
        Bundle startData = new Bundle();
        startData.putInt(Castroid.PROGRESS_MAX, feedIds.length);
        signalHandler(Castroid.WHAT_START, new Bundle());
        for(int currentFeed : feedIds){
            if(isCancelled()){
                break;
            }
            String feedName = PodcastDAO.getChannelTitle(contentResolver, currentFeed);
            Bundle data = new Bundle();
            data.putString(Castroid.PROGRESS_ITEMNAME, feedName);
            signalHandler(Castroid.WHAT_PREITEM, data);
            try {
                update.runUpdate(currentFeed);
            } catch (MalformedURLException e) {
                String msg = "Unable to update " + feedName + "\n" + e.getMessage();
                Log.w(Castroid.TAG, msg);
            } catch (MalformedRSSException e) {
                String msg = "Unable to update " + feedName + "\n" + e.getMessage();
                Log.w(Castroid.TAG, msg);
            } finally{
                publishProgress(++numUpdated);
            }
        }

        return numUpdated;
    }

    @Override
    public void onCancelled(){
        Bundle b = new Bundle();
        b.putBoolean(DownloadDialog.PROGRESS_DONE, false);
        signalHandler(DownloadDialog.WHAT_CANCELED, b);
    }

    @Override
    public void onProgressUpdate(Integer... progresses){
        Bundle b = new Bundle();
        b.putInt(DownloadDialog.PROGRESS_UPDATE, progresses[0]);
        signalHandler(DownloadDialog.WHAT_UPDATE, b);
    }

    @Override
    public void onPostExecute(Integer result){
        signalHandler(Castroid.WHAT_DONE, null);
    }

    private void signalHandler(int msgType, Bundle data){
        Handler handler = mUpdateHandler;
        if(handler != null) {
            Message msg = handler.obtainMessage(msgType);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }
}
