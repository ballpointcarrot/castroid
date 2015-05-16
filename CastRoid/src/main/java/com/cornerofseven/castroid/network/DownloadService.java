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
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * A service to allow asynchronous downloads to run
 * and not block the main thread.
 * 
 * <h3>Lifecycle</h3>
 * <p>
 * Both {@see #onStart(Intent, int)} and {@see #onStartCommand(Intent, int, int)}
 * delegate to handleCommand, which takes care of starting a new download on
 * a seperate thread.
 * </p>
 * 
 * 
 * <h3>Synchronization Policy</h3>
 * <p>The download manager has multiple threads that must interact
 * to operate the Service properly.  Any method accesses the {@see #runningDownloads} 
 * field must be synchronized, otherwise a potential race condition exists
 * between dispatching a new download and shutting down the service 
 * when the last active download finishes. 
 * </p>
 * <p> The service shutdown policy is to shutdown when a download finishes
 * and the set of running downloads is also empty. The synchronization policy 
 * is that any method, which directly or
 * indirectly access the {@see #runningDownloads} field must be synchronized.
 * </p>
 * 
 * <h3>TODO's</h3>
 * <ul>
 * <li> Add ability to cancel a download</li>
 * <li> Change where the notifaction intent dispatches.
 * Currently goes to the main Castroid activity. Would
 * probable make sense to dispatch to a "download manager"
 * activity, or to at least dispatch ItemInformationView.
 * </ul>
 * @author Sean Mooney
 * @since v0.1
 */
public class DownloadService extends Service{

    /**
     * Intent data for where to download the link.
     */
    public static final String INT_DOWNLOAD_FOLDER = "download_dest";
    
    static final String TAG = "DownloadService";

    /**
     * List of running downloads. When this is empty the 
     * service can be shutdown.
     */
    ArrayList<AsyncDownload> runningDownloads;

    /**
     * Handles all the messages from running downloads.
     */
    ServiceMsgHandler mMessageHandler;

    /**
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public synchronized void onCreate(){
        runningDownloads = new ArrayList<DownloadService.AsyncDownload>();
        mMessageHandler  = new ServiceMsgHandler(this);
        super.onCreate();
    }

    @Override
    /**
     * Synchronize the startService method to make
     * sure that the service is restarted if the
     * last running download finishes and stops
     * the service when startService is called.
     */
    public synchronized ComponentName startService(Intent intent){
        return super.startService(intent);
    }
    
    @Override
    public synchronized void onDestroy(){
        if(!runningDownloads.isEmpty()){
            Log.w(TAG, "Destroying the download service with downloads pending");
        }
        runningDownloads = null;
        mMessageHandler = null;
    }

    //For pre-2.0 platforms
    /*
     * (non-Javadoc)
     * @see android.app.Service#onStart(android.content.Intent, int)
     */
    @Override
    public synchronized void onStart(Intent intent, int startId){
        handleCommand(intent, startId);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    /**
     * Download the file. Intent caller is responsible for
     * setting the link URI as the intent data, the destination folder/dir
     * as an extra, with key {@see #INT_DOWNLOAD_FOLDER}
     */
    public synchronized int onStartCommand(Intent intent, int flags, int startId){
        handleCommand(intent, startId);
        return START_STICKY;
    }

    /**
     * Handle a download intent.
     * 
     * To handle a download intent, start a new async download object,
     * give it the URL from the the intent, start the download thread.
     * 
     * If the there is no URL in the intent, does nothing.
     * 
     * @param intent
     */
    protected synchronized void handleCommand(Intent intent, int startId){

        File dlDir;
        Uri dlUri;
        
        //pull the download dir from the intent.
        Bundle bundle = intent.getExtras();
        String filePath = bundle.getString(INT_DOWNLOAD_FOLDER);
        Log.i(TAG, "Save to " + filePath);
        dlDir = new File(filePath);
        
        dlUri = intent.getData();
        
        //pull the download URL from the intent.
        
        if(!dlDir.exists()){
            if(!dlDir.mkdirs()){
                Toast.makeText(this, dlDir.getAbsoluteFile() + " does not exist.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if(!dlDir.canWrite()){
            Toast.makeText(this, dlDir.getAbsoluteFile() + " is not writtable.", Toast.LENGTH_LONG).show();
            return;
        }

        AsyncDownload download = new AsyncDownload(dlDir, mMessageHandler, startId);
        runningDownloads.add(download);

        //TODO: What happens if the downloadLink is invalid?
        if(dlUri == null){
            Toast.makeText(this, "Download uri is null", Toast.LENGTH_LONG).show();
            return;
        }
        
        download.execute(dlUri);
    }

    /**
     * Finish up the download.
     * 
     * <p>
     * When a download finishes, remove from the list of
     * running tracked downloads.
     * </p>
     * <p>
     * Accesses the download list.
     * </p>
     * @param download
     */
    protected synchronized void finishDownload(AsyncDownload download){
        runningDownloads.remove(download);
        if(runningDownloads.isEmpty()){
            Log.i(TAG, "Shutting down the download service.");
            stopSelf();
        }
    }

    /* (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    /**
     * BINDING NOT SUPPORTED.
     * @return null.
     */
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Simple container to hold download progress information. 
     *
     */
    private static class DownloadProgress{
        int numBytes;
        int totalBytes;
        
        public DownloadProgress(int numBytes, int totalBytes){
            this.numBytes = numBytes;
            this.totalBytes = totalBytes;
        }
    }
    
    private class AsyncDownload extends AsyncTask<Uri, DownloadProgress, Long>{
        static final String TAG = "Download";

        File dlDir;
        Handler mHandler;
        final int downloadId;
        File saveFile = null; //handle to the file being written to.
        
        //ProgressDialog mProgressDialog;
        public AsyncDownload(File dlDir, Handler handler, int downloadId){
            this.dlDir = dlDir;
            this.mHandler = handler;
            this.downloadId = downloadId;
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
        public void onCancelled(){
            Bundle b = new Bundle();
            b.putBoolean(ServiceMsgHandler.PROGRESS_DONE, false);
            b.putInt(ServiceMsgHandler.SEND_ID, downloadId);
            signalHandler(mHandler, ServiceMsgHandler.WHAT_CANCELED, b);

            cleanup();
        }

        @Override
        public void onProgressUpdate(DownloadProgress... progresses){
            Bundle b = new Bundle();
            DownloadProgress dp = progresses[0];
            b.putInt(ServiceMsgHandler.PROGRESS_UPDATE, dp.numBytes);
            
            b.putInt(ServiceMsgHandler.PROGRESS_MAX, dp.totalBytes);
            
            b.putInt(ServiceMsgHandler.SEND_ID, downloadId);
            signalHandler(mHandler, ServiceMsgHandler.WHAT_UPDATE, b);
        }

        @Override
        public void onPostExecute(Long result){
            cleanup();
        }

        /**
         * Display cleanup, with a failed message.
         * @param e
         */
        private void onError(Exception e){
            cleanup(false);
        }

        
        
        private void signalHandler(Handler handler, int msgType, Bundle data){
            if(handler != null) {
                Message msg = handler.obtainMessage(msgType);
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }

        
        /**
         * Deletes to cleanup(boolean), with a parmater of true.
         */
        private void cleanup(){
            cleanup(true);
        }
        
        /**
         * Null out all the refs, we won't need them again.
         * Should only be called by onPostExecute or onCancelled.
         * Semantics of AsyncTask are such that after either of these
         * methods are called, the task should not run again.
         * 
         * Calls finishDownload in the parent context to remove itself
         * from the list of running tasks.
         */
        private void cleanup(boolean success){
            Log.i(TAG, "SIGNALING HANDLER DONE");
            Bundle b = new Bundle();
            b.putBoolean(ServiceMsgHandler.PROGRESS_DONE, success);
            b.putInt(ServiceMsgHandler.SEND_ID, downloadId);
            
            if(saveFile != null){
                String fileUri = saveFile.getAbsolutePath();
                b.putString(ServiceMsgHandler.SEND_FILENAME, fileUri);
            }
            signalHandler(mHandler, ServiceMsgHandler.WHAT_DONE, b);
            
            mHandler = null;
            dlDir = null;
            saveFile = null;
            
            finishDownload(this);
        }

        /**
         * Download a single file
         * @param dlUri
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

            //hold onto the save file, to report when done.
            saveFile = dest;
            
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
                b.putInt(ServiceMsgHandler.PROGRESS_MAX, totalSize);
                b.putInt(ServiceMsgHandler.SEND_ID, downloadId);
                signalHandler(mHandler, ServiceMsgHandler.WHAT_START, b);

                final DownloadProgress downProgress = new DownloadProgress(0, totalSize);
                
                //we'll do this the old fashioned way...copy buffered bytes from inputstream to output stream
                while(!isCancelled() && ( bytesRead = bInputStream.read(buffer)) > 0){
                    bOutputStream.write(buffer, 0, bytesRead);
                    downloadSize += bytesRead;
                    bytesSinceLastLog += bytesRead;


                    if(bytesSinceLastLog > K_PER_UPDATE){
                        //reset the byte count for the next update count.
                        bytesSinceLastLog = 0;
                        
                        downProgress.numBytes = downloadSize;
                        publishProgress(downProgress);
                        
                        //flush to stream occasionally to make sure things are written
                        //even if the download is interrupted.
                        bOutputStream.flush();
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
            return Long.valueOf(downloadSize);
        }
    }

    static class ServiceMsgHandler extends Handler{
        public static final int WHAT_START = 1;
        public static final int WHAT_UPDATE = 2;
        public static final int WHAT_DONE = 3;
        public static final int WHAT_CANCELED = 4;

        public static final String PROGRESS_MAX = "max";
        public static final String PROGRESS_UPDATE = "total";
        public static final String PROGRESS_DONE = "done";
        public static final String SEND_ID = "sender";
        public static final String SEND_FILENAME = "filename";

         Context mContext;

         NotificationManager mNotificationManager;

         final float BYTES_PER_MEG = 1048576f;
         /**
          * Format a decimal to two decimal places, with a megabyte label at the end.
          */
         final DecimalFormat decFormMB = new DecimalFormat("####.00 MB");
         /**
          * Format a decimal with no decimal places and a percent sign on the end.
          */
         final DecimalFormat decFormPer = new DecimalFormat("###0%");
         
        public ServiceMsgHandler(Context context){
            this.mContext = context;
            
            String ns = Context.NOTIFICATION_SERVICE;
            mNotificationManager = (NotificationManager)(context.getSystemService(ns));
        }
        
        

        /*
         * (non-Javadoc)
         * 
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        /**
         * Sends a system notifaction with the SEND_ID and SEND_FILENAME
         * in the bundle.
         * 
         * Notifaction uses the SEND_ID, which must be put into the extras
         * by the sender as the notification id. This id should be the same
         * as the start id from {@see #onStart(Intent, int)} or 
         * {@see #onStartCommand(Intent, int, int)} to keep the notifications
         * synced with the async downloaders sending them.
         */
        public void handleMessage(Message msg) {

            Bundle data = msg.getData();
            
            String fileName = data.getString(SEND_FILENAME);
            if(fileName == null) 
                fileName = "";
            
            int senderId = data.getInt(SEND_ID, -1);
            
            if(senderId == -1){
                Log.w(TAG, "NO SENDER ID IN handleMessage");
                return;
            }
            
            
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_START:
                {
                    int totalBytes = data.getInt(PROGRESS_MAX);
                    notifyProgressStatus(senderId, fileName, 0, totalBytes);
                }
                    break;
                case WHAT_UPDATE:
                {
                    int bytesDown = data.getInt(PROGRESS_UPDATE);
                    int totalBytes = data.getInt(PROGRESS_MAX);
                    notifyProgressStatus(senderId, fileName, bytesDown, totalBytes);
                }
                    break;
                case WHAT_DONE:
                {
                    boolean success = data.getBoolean(PROGRESS_DONE);
                    if(!success){
                        //Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Download Failed");
                    }
                    
                    if("".equals(fileName)){
                        notifyDone(senderId,null);
                    }else{
                        notifyDone(senderId, new File(fileName));
                    }   
                }
                    break;
                case WHAT_CANCELED:
                    break;
                default:
                    Log.w(TAG, "Unknown code " + msg.what + "sent to handler");
            }
        }
        
        /**
         * Publish a update to the notification bar of how much is downloaded.
         * 
         * @param senderId
         * @param fileName
         * @param numBytes bytes download
         * @param totalBytes total to download
         */
        void notifyProgressStatus(int senderId, String fileName, int numBytes, int totalBytes){
            Notification notification;
            
            int icon = android.R.drawable.stat_sys_download;
            String msg = "Downloading" ;
            long when = System.currentTimeMillis();
            
            notification = new Notification(icon, msg, when);
            
            Context context = mContext;
            CharSequence contentTitle = "Downloading " + fileName;
            
            CharSequence contentText;
            /* on the off chance that this is called with 0 total size,
             * just disply the total number of bytes down. Otherwise, display the percentage
             */
            if(totalBytes >0){
                float per = ((float)numBytes/(float)totalBytes);
                contentText = decFormPer.format(per);
            }else{
                float megsDown = numBytes/BYTES_PER_MEG;
                contentText = decFormMB.format(megsDown);
            }

            int flags = Notification.FLAG_ONGOING_EVENT;
            notification.flags = flags;
            
            mNotificationManager.notify(senderId, notification);
        }
        
        /**
         * 
         * @param senderId
         * @param downloadedFile file uri, or null if the uri is not known. A null fileUri will result in the intent going to castroid, instead of a system intent to view.
         */
        void notifyDone(int senderId, File downloadedFile){
            Notification notification;
            
            int icon = android.R.drawable.stat_sys_download_done;
            String msg = "Download Finished";
            long when = System.currentTimeMillis();
            
            notification = new Notification(icon, msg, when);
            
            Context context = mContext;
            CharSequence contentTitle = "Download Finished";
            
            Intent notificationIntent = null;
            if(downloadedFile != null){
                
                Uri contentUri = Uri.fromFile(downloadedFile);
                notificationIntent = new Intent(Intent.ACTION_VIEW,contentUri);
                Log.d(TAG, "Content URI: " + contentUri);
                Log.d(TAG, "Downloaded type " + notificationIntent.getType());
            
                Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                mediaScanIntent.setData(contentUri);
                Log.i(TAG, "Broadcasting media scan for " + contentUri);
                mContext.sendBroadcast(mediaScanIntent);
            }else{
                Log.i(TAG, "No file, cannot broadcast update.");
            }

            if (notificationIntent != null) {
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                //remove the notification as soon as clicked.
                int flags = Notification.FLAG_AUTO_CANCEL;

                notification.setLatestEventInfo(context, contentTitle, "", contentIntent);
                notification.flags = flags;
            }

            mNotificationManager.notify(senderId, notification);
        }
    }
}
