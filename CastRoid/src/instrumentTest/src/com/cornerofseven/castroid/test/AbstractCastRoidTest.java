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

package com.cornerofseven.castroid.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ExpandableListView;

import com.cornerofseven.castroid.Castroid;
import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.PodcastDataProvider;


/**
 * Abstract class to take care of setting up the
 * common features of any tests of the CastRoid activity.
 * @author sean
 *
 */
public abstract class AbstractCastRoidTest extends ActivityInstrumentationTestCase2<Castroid>{

	protected Castroid mActivity = null;
	protected ExpandableListView mPodcastList = null;
	protected PodcastDataProvider mDataprovider = null;
	
	public AbstractCastRoidTest(){
		super("com.cornerofsever.castroid.Castroid", Castroid.class);
	}
	
	@Override
	public void setUp() throws Exception{
		super.setUp();
		mActivity = this.getActivity();
		//Get a reference to the PodcastDataProvider the test is using.
		mDataprovider = 
			(PodcastDataProvider)mActivity
			.getContentResolver()
			.acquireContentProviderClient(Feed.BASE_AUTH)
			.getLocalContentProvider();
		
		//clear the database before each test.
		mDataprovider.deleteAll();
	
		mPodcastList = (ExpandableListView)(mActivity.findViewById(com.cornerofseven.castroid.R.id.podcastList));
	}

	@Override
	public void tearDown() throws Exception{
		super.tearDown();
		mActivity = null;
		mPodcastList = null;
		mDataprovider = null;
	}
}
