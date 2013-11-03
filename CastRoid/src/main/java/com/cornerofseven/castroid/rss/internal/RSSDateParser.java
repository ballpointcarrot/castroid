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
package com.cornerofseven.castroid.rss.internal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;



/**
 * Converts an RSS date into a simpler form.
 * 
 * 
 * @author Sean Mooney
 *
 */
public class RSSDateParser {

	private final static DateFormat rssFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
	private final static DateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	
	public static String parse(String rssDate) throws ParseException{
		
		Date pubDate = rssFormat.parse(rssDate);
		return dbFormat.format(pubDate);
	}
}
