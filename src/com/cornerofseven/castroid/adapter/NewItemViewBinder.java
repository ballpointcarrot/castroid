///*
//   Copyright 2010 Christopher Kruse and Sean Mooney
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License. 
// */
//package com.cornerofseven.castroid.adapter;
//
//import com.cornerofseven.castroid.data.Item;
//
//import android.database.Cursor;
//import android.util.Log;
//import android.view.View;
//import android.widget.SimpleCursorAdapter.ViewBinder;
//import android.widget.TextView;
//
///**
// * ViewBinder that will set the text of an item view
// * bold if the item is new.
// * 
// * TODO: Too tightly bound. Make more general somehow?
// * @author Sean Mooney
// *
// */
//public class NewItemViewBinder implements ViewBinder{
//
//	/* (non-Javadoc)
//	 * @see android.widget.SimpleCursorAdapter.ViewBinder#setViewValue(android.view.View, android.database.Cursor, int)
//	 */
//	@Override
//	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//		
//		if(view instanceof TextView){
//			String text = cursor.getString(columnIndex);
//			boolean isBold  = (Item.NEW_ITEM_FLAG == cursor.getInt(cursor.getColumnIndex(Item.NEW)));
//			bindHighlightingView((TextView)view, text, isBold);
//			return true;
//		}else{
//			return false;
//		}
//		
//	}
//	
//	protected void bindHighlightingView(TextView view, String text, boolean isBold){
//		view.setText(text);
//		//view.getTypeface().
//	}
//	
//}
