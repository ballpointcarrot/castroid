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
package com.cornerofseven.castroid.rss;

/**
 * Defines an exception for RSS/Podcast processing.
 * 
 * @author Sean Mooney
 *
 */
public class MalformedRSSException extends Exception{
	public MalformedRSSException(String msg){
		super(msg);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8492731847282518265L;

}
