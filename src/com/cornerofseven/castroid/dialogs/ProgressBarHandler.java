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

/**
 * Define some constants for sending messages to a progress bar handler.
 * 
 * @author Sean Mooney
 *
 */
public final class ProgressBarHandler {
	
	private ProgressBarHandler(){}
	
	//integers for the what field of a message
	public static final int WHAT_START = 1;
	public static final int WHAT_UPDATE = 2;
	public static final int WHAT_DONE = 3;
	
	public static final String PROGRESS_MAX = "max";
	public static final String PROGRESS_UPDATE = "total";
	public static final String PROGRESS_DONE = "done";

}
