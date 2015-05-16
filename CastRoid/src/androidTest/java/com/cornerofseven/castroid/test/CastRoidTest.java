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

import android.test.UiThreadTest;
import android.widget.ExpandableListView;

import com.cornerofseven.castroid.data.PodcastDAO;
import com.cornerofseven.castroid.rss.feed.RSSChannel;
import com.cornerofseven.castroid.rss.feed.RSSItem;

/**
 * 
 * @author Sean Mooney
 *
 */
public class CastRoidTest extends AbstractCastRoidTest {
	
	
	/**
	 * A test for bug #1.
	 * 
	 * The first time a podcast is added to the system
	 * when the title is clicked on, the program crashes
	 * with an NPE instead of expanding the child tree view.
	 */
	@UiThreadTest
	public void testFirstPodcastAdded(){
		add1Podcast();
		
		ExpandableListView podcastList = mPodcastList;
		assertEquals(1, podcastList.getChildCount());
		podcastList.performClick();
		//if this doesn't crash, it passes.
	}
	
	private void add1Podcast(){
		RSSChannel c = new RSSChannel("ASDF", "ASDF", "ASDF", "ASDF");
		c.addItem(new RSSItem("ASDF", "ASDF", "ASDF"));
		
		PodcastDAO.addRSS(mActivity.getContentResolver(), c);
	}
	
}
