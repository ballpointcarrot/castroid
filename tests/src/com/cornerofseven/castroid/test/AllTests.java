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
package com.cornerofseven.castroid.test;

import com.cornerofseven.castroid.data.test.PodcastDataProviderTests;
import com.cornerofseven.castroid.network.test.DownloadTests;
import com.cornerofseven.castroid.rss.test.RSSParsingTestRemote;
import com.cornerofseven.castroid.rss.test.RSSParsingTestsLocal;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author sean
 *
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(CastRoidTest.class);
		suite.addTestSuite(PodcastDataProviderTests.class);
		suite.addTestSuite(RSSParsingTestsLocal.class);
		suite.addTestSuite(RSSParsingTestRemote.class);
		suite.addTestSuite(DownloadTests.class);
		//$JUnit-END$
		return suite;
	}

}
