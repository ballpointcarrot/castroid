package com.cornerofseven.castroid.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.cornerofseven.castroid.Castroid;
import com.cornerofseven.castroid.dialogs.DownloadDialog;

public class AsyncDownloadManager extends AsyncTask<Uri, Integer, Long>{
    static final String TAG = "Download";
    
    File dlDir;
    Castroid mActivity;
    Handler mHandler;
    //ProgressDialog mProgressDialog;
    public AsyncDownloadManager(Castroid activity, File dlDir){
        this.dlDir = dlDir;
        this.mActivity = activity;
//        this.mProgressDialog = progress;
    }
    
    /* (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Long doInBackground(Uri... uris) {
   
        Long totalDownloadSize = 0L;
        for(Uri uri : uris ){
            if(isCancelled())break;
            
            totalDownloadSize += downloadUri(uri);
        }
        
        
        return totalDownloadSize;
    }
    
    @Override
    public void onPreExecute(){
        mActivity.showDialog(Castroid.PROGRESS_DIALOG_ID);
        
        DownloadDialog dd = mActivity.getDownloadDialog();
        if(dd != null) {
            mHandler = dd.getUpdateHandler();
        }
        
    }
    
    @Override
    public void onCancelled(){
        Bundle b = new Bundle();
        b.putBoolean(DownloadDialog.PROGRESS_DONE, false);
        signalHandler(mHandler, DownloadDialog.WHAT_CANCELED, b);
        
        cleanup();
    }
    
    @Override
    public void onProgressUpdate(Integer... progresses){
        Bundle b = new Bundle();
        b.putInt(DownloadDialog.PROGRESS_UPDATE, progresses[0]);
        signalHandler(mHandler, DownloadDialog.WHAT_UPDATE, b);
    }
    
    @Override
    public void onPostExecute(Long result){
        Toast.makeText(mActivity, "Downloaded " + result, Toast.LENGTH_SHORT).show();
        mActivity.dismissDialog(Castroid.PROGRESS_DIALOG_ID);
        cleanup();
    }
    
    /**
     * Display a Toast message describing the problem.
     * @param e
     */
    private void onError(Exception e){
        Toast.makeText(mActivity, "Error during download!\n" + e.getMessage(), Toast.LENGTH_LONG).show();
    }
    
    private void signalHandler(Handler handler, int msgType, Bundle data){
        if(handler != null) {
            Message msg = handler.obtainMessage(msgType);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }
    
    /**
     * Null out all the refs, we won't need them again.
     * Should only be called by onPostExecute or onCancelled.
     * Semantics of AsyncTask are such that after either of these
     * methods are called, the task should not run again.
     */
    private void cleanup(){
        mActivity = null;
        mHandler = null;
        dlDir = null;
    }
    
    /**
     * Download a single file
     * @param dlUrl
     * @return
     */
    public Long downloadUri(Uri dlUri){
        String fileName = dlUri.getLastPathSegment();
        File dataDir = dlDir;
        
        if(dlDir == null) return 0L;
        
        if(!dataDir.canWrite()){
            Log.e(TAG, "Cannot write to " + dataDir.getAbsolutePath());
            return 0L;
        }
        
        //we only need to create the directory if it doesn't exist...how silly of me...
        if(!dataDir.exists()) {
            if(!dataDir.mkdirs()) {
                Log.e(TAG, "Unable to create " + dataDir.getAbsolutePath());
                return 0L;
            }
        }
        
        return  downloadFile(dlUri.toString(), new File(dataDir, fileName));
    }
    
    
    /**
     * Download a given URL to a file.
     * based on stack overflow example: @{link http://stackoverflow.com/questions/3287795/android-download-service}
     */
    private Long downloadFile( String link, File dest)
    {
        BufferedInputStream bInputStream = null;
        BufferedOutputStream bOutputStream = null;
        int downloadSize = 0;
        
        try{
            URL url = new URL(link);
            FileOutputStream fileOutput = new FileOutputStream(dest);
            
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            
            urlConnection.connect();
            
            InputStream inputStream = urlConnection.getInputStream();
            
            //TODO: Tune buffer sizes
            final int BUFFER_SIZE = 100 * 1024; //100K buffer;
            
            //buffer both streams.
            bInputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);
            bOutputStream = new BufferedOutputStream(fileOutput, BUFFER_SIZE);
            
            int totalSize = urlConnection.getContentLength();
            //TODO: How big of a file before this rolls over and is meaningless, or do we need a long?
            
            int bytesSinceLastLog = 0;
            
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            final int K_PER_UPDATE = (1024 * 100); //update every tenth meg downloaded
            
            //signal the max download size
            Bundle b = new Bundle();
            b.putInt(DownloadDialog.PROGRESS_MAX, totalSize);
            signalHandler(mHandler, DownloadDialog.WHAT_START, b);

            //we'll do this the old fashioned way...copy buffered bytes from inputstream to output stream
            while(!isCancelled() && ( bytesRead = bInputStream.read(buffer)) > 0){
                bOutputStream.write(buffer, 0, bytesRead);
                downloadSize += bytesRead;
                bytesSinceLastLog += bytesRead;
                
                
                if(bytesSinceLastLog > K_PER_UPDATE){
                    float per = (float)downloadSize / (float)totalSize;
                    //Log.i(TAG, "Downloaded " + downloadSize + " bytes " + per + "%");
                    bytesSinceLastLog = 0;
                    //int msgArg = (int)(per * 100);
                    publishProgress(downloadSize);
                }
            }
        } catch (MalformedURLException e) {  
            onError(e);
        } catch (IOException e) { 
            onError(e);
        }
        finally{
            if(bOutputStream != null)
                try {
                    bOutputStream.close();
                } catch (IOException e) {}//ignore, grumble grumble grumble
            
            if(bInputStream != null)
                try {
                    bInputStream.close();
                } catch (IOException e) {}
        }
        return new Long(downloadSize);
    }
}