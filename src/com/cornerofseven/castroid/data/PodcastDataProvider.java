package com.cornerofseven.castroid.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class PodcastDataProvider extends ContentProvider{
	private static final String TAG = "PodcastDataProvider";
	private static final UriMatcher uriMatcher;
	private DbHelper helper;

	//Constants for choosing how to processes URI's
	private static final int CHANNEL = 1;
	private static final int ITEM = CHANNEL + 1;

//	public PodcastDataProvider(Context context){
//		super();
//		helper = new DbHelper(context);
//	}

	private static class DbHelper extends SQLiteOpenHelper{
		private static final String DB_NAME = "podcast.db";
		private static final int DB_VERSION = 1;

		public DbHelper(Context ctx){
			super(ctx, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db){
			db.execSQL("CREATE TABLE " + Feed.TABLE_NAME + "(" + 
					Feed._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					Feed.LINK + " TEXT NOT NULL, " +
					Feed.TITLE + " TEXT NOT NULL, " +
					Feed.DESCRIPTION + " TEXT NOT NULL, " +
					Feed.IMAGE + " TEXT);");

			db.execSQL("CREATE TABLE " + Item.TABLE_NAME + "(" +
					Item._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					Item.OWNER + " INTEGER, " +  //foreign key  to the feed table 
					Item.TITLE + " TEXT NOT NULL, " +
					Item.LINK  + " TEXT NOT NULL, " +
					Item.DESC  + " TEXT NOT NULL);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
			Log.w(TAG, "Upgrading database from version" + oldVersion +" to " +
					newVersion + ". Existing data will be deleted.");
			db.execSQL("DROP TABLE IF EXISTS "+Feed.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "+Item.TABLE_NAME);
			onCreate(db);
		}

		/**
		 * Convenience method to simplify adding a feed to the database
		 * @param db
		 * @param values
		 * @return
		 */
		public long insertFeed(final SQLiteDatabase db, ContentValues values){
			long newId = -1;

			if(values == null){
				values = new ContentValues();
			}
			
			//ensure all the columns of the table exist
			ensureValue(values, Feed.LINK, "NO LINK");
			ensureValue(values, Feed.TITLE, "NO TITLE");
			ensureValue(values, Feed.DESCRIPTION, "NO DESCRIPTION");
			ensureValue(values, Feed.IMAGE, null);
			
			newId = db.insert(Feed.TABLE_NAME, "", values);

			return newId;
		}

		/**
		 * Convenience method to simplify adding a item's feed to the database
		 * @param db
		 * @param values
		 * @return
		 */
		public int insertItem(SQLiteDatabase db, ContentValues values){
			int newId = -1;

			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

			qb.setTables(Item.TABLE_NAME);

			return newId;
		}
		
		/**
		 * Ensure the key is set in the value object.
		 * If the key already exists, do nothing, else set it with the default values.
		 * @param values
		 * @param field
		 * @param defaultValue
		 */
		private void ensureValue(ContentValues values, String field, String defaultValue){
			if(values.containsKey(field) == false){
				values.put(field, defaultValue);
			}
		}
	}

	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		switch(uriMatcher.match(uri)){
		case CHANNEL : return Feed.CONTENT_TYPE;
		case ITEM : return Feed.CONTENT_ITEM_TYPE;
		default:
			unknownURI(uri);
			return null;
		}

	}

	/**
	 * Insert a new item into the database.
	 * 
	 * @param uri
	 * @param values
	 * @returns the Uri of the inserted item, or {@link #Uri.EMPTY}
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db;
		long rowId;
		switch(uriMatcher.match(uri)){
		case CHANNEL: 
			db = helper.getWritableDatabase();
			rowId = helper.insertFeed(db,values);
			if(rowId > 0){
				Uri contentUri = ContentUris.withAppendedId(Feed.CONTENT_URI, rowId);
				return contentUri;
			}
			else break;
		case ITEM:
			db = helper.getWritableDatabase();
			rowId = helper.insertItem(db,values);
			if(rowId > 0){
				Uri contentUri = ContentUris.withAppendedId(Item.CONTENT_URI, rowId);
				return contentUri;
			}
			else break;
		default: unknownURI(uri); return Uri.EMPTY;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		helper = new DbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Utility method to make handling an unknown URI consistent.
	 * @param uri
	 */
	private static void unknownURI(Uri uri){
		throw new IllegalArgumentException("Unknown URI" + uri.toString());
	}

	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		//the podcast/rss data model has a table for feeds (channels in RSS parlance)

		uriMatcher.addURI(Feed.BASE_AUTH, "feed", CHANNEL);
		uriMatcher.addURI(Feed.BASE_AUTH, "item", ITEM);
	}


}
