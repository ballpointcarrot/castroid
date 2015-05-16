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

import android.net.Uri;
import android.test.AndroidTestCase;

import java.net.URL;
import java.net.UnknownHostException;

/**
 * Tests for misc. network based issues
 * 
 * @author sean
 *
 */
public class NetworkTests extends AndroidTestCase{
	
	/**
	 * Test resolving a URL that is complete.
	 * 
	 * Google's always up!
	 * @throws UnknownHostException 
	 */
	public void testURLResolve1() throws UnknownHostException{
		final String host = "http://www.google.com";
		//InetAddress addr = java.net.InetAddress.getByName(host);
		
		Uri addr = android.net.Uri.parse(host);
		
		assertNotNull(addr);
	}
	
	/**
	 * Test resolving a URL that is complete.
	 * 
	 * Google's always up!
	 * @throws UnknownHostException 
	 */
	public void testURLResolve2() throws Exception{
		final String host = "www.google.com";
		//InetAddress addr = java.net.InetAddress.getByName(host);
		
		Uri addr = android.net.Uri.parse(host);
		
		assertNotNull(addr);
		System.out.println(addr);
		
		System.out.println(addr.getAuthority());
		System.out.println(addr.getHost());
		
		URL url = new URL(addr.getAuthority() + addr.getHost());
		
	}
	
	/**
	 * Test resolving a URL that is complete.
	 * 
	 * Google's always up!
	 * @throws UnknownHostException 
	 */
	public void testURLResolve3() throws UnknownHostException{
		final String host = "google.com";
		//InetAddress addr = java.net.InetAddress.getByName(host);
		
		Uri addr = android.net.Uri.parse(host);
		
		assertNotNull(addr);
	}
	
}
