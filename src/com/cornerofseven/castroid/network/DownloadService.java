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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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
 * @author Sean Mooney
 * @since v0.1
 */
public class DownloadService extends Service{
    
    //For pre-2.0 platforms
    /*
     * (non-Javadoc)
     * @see android.app.Service#onStart(android.content.Intent, int)
     */
    @Override
    public void onStart(Intent intent, int startId){
        handleCommand(intent);
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        handleCommand(intent);
        
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
    protected void handleCommand(Intent intent){
        
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

}
