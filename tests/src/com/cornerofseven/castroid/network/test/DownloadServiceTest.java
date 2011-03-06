/**
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
package com.cornerofseven.castroid.network.test;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.test.ServiceTestCase;

import com.cornerofseven.castroid.network.DownloadService;

/**
 * @author sean
 *
 */
public class DownloadServiceTest extends ServiceTestCase<DownloadService>{

    /**
     * @param serviceClass
     */
    public DownloadServiceTest(){
        super(DownloadService.class);
    }

    public void testStartDownload(){
        Intent intent = new Intent();
        intent.setData(Uri.parse("http://www.podtrac.com/pts/redirect.mp3/twit.cachefly.net/tnt0192.mp3"));
        
        File downloadDir = new File("/mnt/sdcard/Podcasts");
        
        intent.putExtra(DownloadService.INT_DOWNLOAD_FOLDER, downloadDir.getAbsolutePath());
        startService(intent);
   
        assertTrue(getService() != null);
    }
}
