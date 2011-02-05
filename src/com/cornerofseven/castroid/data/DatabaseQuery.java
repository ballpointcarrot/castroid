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
package com.cornerofseven.castroid.data;

import java.net.URI;

import android.net.Uri;

/**
 * A abstract to define utility objects to help
 * make queries consistent.
 * 
 * One of the challenges I seem to have run into 
 * (especially with unit testing) is have to reproduce queries
 * in both the unit test and the client code.  Reproduction is 
 * both tedious and error prone.  The abstract class defines
 * an interface that can be used to make/store common query
 * objects.
 * 
 * @author Sean Mooney
 *
 */
public class DatabaseQuery {
	protected final String[] projection;
	protected final String selection;
	protected final String[] selectionArgs;
	protected final String sortOrder;
	protected final Uri contentUri;
	
	
	
	/**
	 * @param projection
	 * @param selection
	 * @param args
	 * @param sortOrder
	 * @param contentUri
	 */
	public DatabaseQuery(String[] projection, String selection, String[] selectionArgs,
			String sortOrder, Uri contentUri) {
		this.projection = projection;
		this.selection = selection;
		this.selectionArgs = selectionArgs;
		this.sortOrder = sortOrder;
		this.contentUri = contentUri;
	}
	/**
	 * @return the projection
	 */
	public String[] getProjection() {
		return projection;
	}
	/**
	 * @return the selection
	 */
	public String getSelection() {
		return selection;
	}
	/**
	 * @return the args
	 */
	public String[] getSelectionArgs() {
		return selectionArgs;
	}
	/**
	 * @return the sortOrder
	 */
	public String getSortOrder() {
		return sortOrder;
	}
	
	/**
	 * Get's the base content URI.  Clients may need to append an
	 * id to this get it work correctly.
	 * @return the contentUri
	 */
	public Uri getContentUri() {
		return contentUri;
	}
	
	
}
