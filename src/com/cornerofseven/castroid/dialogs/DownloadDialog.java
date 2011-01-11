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
package com.cornerofseven.castroid.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cornerofseven.castroid.R;
import com.cornerofseven.castroid.network.DownloadManager;

/**
 * Download Dialog - Presents a dialog which shows the progress of a file
 * download.
 * 
 * @author Christopher Kruse
 * 
 */
public class DownloadDialog extends ProgressDialog {
	public static final String TAG = "DownloadDialog";
	// integers for the what field of a message
	public static final int WHAT_START = 1;
	public static final int WHAT_UPDATE = 2;
	public static final int WHAT_DONE = 3;

	public static final String PROGRESS_MAX = "max";
	public static final String PROGRESS_UPDATE = "total";
	public static final String PROGRESS_DONE = "done";

	/*
	 * There are weird things happening here with the download thread. It's
	 * probably just my misunderstanding of threading. TODO: make it so the
	 * download thread only runs once; tilting the device will start the
	 * download again, and that causes problems.
	 */
	static Handler hand;
	DownloadThread thread;
	DownloadManager dlMgr;
	static String dlDir;

	/**
	 * {@inheritDoc}
	 */
	public DownloadDialog(Context context, Uri downloadUri) {
		super(context);
		hand = new Handler() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case WHAT_START:
					int max = msg.getData().getInt(PROGRESS_MAX);
					setMax(max);
					setProgress(0);
					break;
				case WHAT_UPDATE:
					int total = msg.getData().getInt(PROGRESS_UPDATE);
					setProgress(total);
					break;
				case WHAT_DONE:
					hand.removeCallbacks(thread);
					dismiss();
				}
			}
		};
		dlMgr = new DownloadManager();
		thread = new DownloadThread(dlMgr, downloadUri);
		dlDir = context.getString(R.string.download_root_dir);
		setTitle(R.string.downloading);
		setProgressStyle(STYLE_HORIZONTAL);
		setCancelable(true);
		thread.start();
		setOnCancelListener(new Dialog.OnCancelListener() {
			@Override
			public final void onCancel(DialogInterface dIface) {
				hand.removeCallbacks(thread);
				thread.interrupt();
				while (thread.isAlive()) {
					try {
						thread.join();
					} catch (InterruptedException e) {
						Log.e(TAG, "Error interrupting thread.");
						e.printStackTrace();
					}
				}
				dismiss();
			}
		});
	}

	/**
	 * Create a separate thread to run the down load on.
	 * 
	 * @author Sean Mooney
	 * 
	 */
	private static class DownloadThread extends Thread {

		private DownloadManager mDownloader;
		private Uri dlUri;
		public DownloadThread(DownloadManager downloader, Uri downloadUri) {
			// super("DOWNLOAD-" + downloadUri);
			this.mDownloader = downloader;
			dlUri = downloadUri;
		}

		@Override
		public void run() {
			mDownloader.downloadItem(dlUri, dlDir, hand);
		}
	}
}
