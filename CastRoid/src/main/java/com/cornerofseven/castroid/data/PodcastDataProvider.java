package com.cornerofseven.castroid.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

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
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.cornerofseven.castroid.rss.MalformedRSSException;
import com.cornerofseven.castroid.rss.RSSProcessor;
import com.cornerofseven.castroid.rss.RSSProcessorFactory;
import com.cornerofseven.castroid.rss.feed.RSSChannel;

public class PodcastDataProvider extends ContentProvider{
	private static final String TAG = "PodcastDataProvider";
	private static final UriMatcher uriMatcher;
	private DbHelper helper;

	//Constants for choosing how to processes URI's
	private static final int FEED = 1;
	private static final int ITEM = FEED + 1;
	private static final int FEED_ID = ITEM + 1;
	private static final int ITEM_ID = FEED_ID + 1;
	
//	public PodcastDataProvider(Context context){
//		super();
//		helper = new DbHelper(context);
//	}

	private static class DbHelper extends SQLiteOpenHelper{
		private static final String DB_NAME = "podcast.db";
		private static final int DB_VERSION = 1;
        private final Context mContext;

		public DbHelper(Context context){
			super(context, DB_NAME, null, DB_VERSION);
		    this.mContext = context;
        }
		
		@Override
		public void onOpen(SQLiteDatabase db){
			super.onOpen(db);
			if(!db.isReadOnly()){
				//enable foreign keys
				db.execSQL("PRAGMA foreign_keys=ON;");
			}
		}

		@Override
		public void onCreate(SQLiteDatabase db){
			db.execSQL("CREATE TABLE " + Feed.TABLE_NAME + "(" + 
					Feed._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					Feed.LINK + " TEXT NOT NULL, " +
					Feed.TITLE + " TEXT NOT NULL, " +
					Feed.RSS_URL + " TEXT NOT NULL, " + 
					Feed.DESCRIPTION + " TEXT NOT NULL, " +
					Feed.IMAGE + " TEXT);");

			db.execSQL("CREATE TABLE " + Item.TABLE_NAME + "(" +
					Item._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					Item.OWNER + " INTEGER REFERENCES " + Feed.TABLE_NAME + " ON DELETE CASCADE, " +  //foreign key  to the feed table 
					Item.TITLE + " TEXT NOT NULL, " +
					Item.LINK  + " TEXT NOT NULL, " +
					Item.DESC  + " TEXT NOT NULL," +
					Item.NEW   + " INTEGER DEFAULT 1, " +
					Item.ENC_LINK + " TEXT,		" + //TODO: Defaults/not null for these fields?
					Item.ENC_SIZE + " INTEGER,	" +
					Item.ENC_TYPE + " TEXT, " +
					Item.PUB_DATE + " DATE DEFAULT NULL, " +
					Item.STREAM_POS + " INTEGER DEFAULT -1);");
			
			addDefaultPodcasts(db);
		}
		
		private void addDefaultPodcasts(final SQLiteDatabase db){

            String[] urls = {
                    "http://www.npr.org/rss/podcast.php?id=35",
                    "http://feeds.twit.tv/twit_video_small",
                    "http://feeds.twit.tv/aaa_video_small",
                    "http://feeds.twit.tv/sn_video_small",
                    "http://feeds.feedburner.com/se-radio"
            };

            new AsyncTask<String, Void, Void>() {
                @Override
                protected void onProgressUpdate(Void... values) {

                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                }

                @Override
                protected Void doInBackground(String[] urls) {
                    for(String url : urls){
                        try {
                            RSSProcessor rp = RSSProcessorFactory.getRSS2_0Processor(new URL(url));
                            rp.process();
                            RSSChannel channel = rp.getBuilder().getFeed();
                            ContentValues values = new ContentValues();
                            values.put(Feed.TITLE, channel.getmTitle());
                            values.put(Feed.LINK, channel.getmLink());
                            values.put(Feed.DESCRIPTION, channel.getmDesc());
                            values.put(Feed.RSS_URL, channel.getRssUrl());
                            values.put(Feed.IMAGE, channel.getImageLink());
                            long fid = insertFeed(db, values);

                            mContext.getContentResolver().notifyChange(
                                    ContentUris.withAppendedId(Feed.CONTENT_URI, fid),
                                    null
                            );
                        } catch (MalformedURLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (SAXException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (MalformedRSSException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
            }.execute(urls);
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
		 * Clear out all the data from the database.
		 * @param db
		 */
		public void clearDatabase(SQLiteDatabase db){
			db.delete(Feed.TABLE_NAME, null, null);
			db.delete(Item.TABLE_NAME, null, null);
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
			ensureValue(values, Feed.RSS_URL, "NO SOURCE");
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
		public long insertItem(SQLiteDatabase db, ContentValues values){
			long newId = -1;

			ensureValue(values, Item.TITLE, "No title");
			ensureValue(values, Item.LINK, "No link");
			ensureValue(values, Item.DESC, "No Description");
			ensureValue(values, Item.ENC_LINK, "");
			ensureValue(values, Item.ENC_TYPE, "");
			ensureValue(values, Item.ENC_SIZE, -1);
			if(values.get(Item.OWNER) == null){
				values.put(Item.OWNER, -1);
			}
			newId = db.insert(Item.TABLE_NAME, "", values);
			
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
			if(!(values.containsKey(field))){
				values.put(field, defaultValue);
			}
		}
		
		private void ensureValue(ContentValues values, String field, int defaultValue){
			if(!(values.containsKey(field))){
				values.put(field, defaultValue);
			}
		}
	}

	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int numDel = 0;
		switch(uriMatcher.match(uri)){
		case FEED : 
			numDel = db.delete(Feed.TABLE_NAME, where, whereArgs); 
			break;
		case FEED_ID:
			//Adapted from the NotePadProvider example.
			String feedId = uri.getPathSegments().get(1);
			numDel = db.delete(Feed.TABLE_NAME, Feed._ID + "=" + feedId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		case ITEM : 
			numDel = db.delete(Item.TABLE_NAME, where, whereArgs);
			break;
		case ITEM_ID:
			String itemId = uri.getPathSegments().get(1);
			numDel =  db.delete(Item.TABLE_NAME, Item._ID + "=" + itemId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
		default:
			unknownURI(uri);
			return -1;
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return numDel;
	}

	@Override
	public String getType(Uri uri) {
		switch(uriMatcher.match(uri)){
		case FEED_ID: return Feed.CONTENT_TYPE;
		case FEED : return Feed.CONTENT_TYPE;
		case ITEM : return Feed.CONTENT_ITEM_TYPE;
		case ITEM_ID : return Feed.CONTENT_ITEM_TYPE;
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
	 * @returns the Uri of the inserted item, or {@link Uri#EMPTY}
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db;
		long rowId;
		switch(uriMatcher.match(uri)){
		case FEED: 
			db = helper.getWritableDatabase();
			rowId = helper.insertFeed(db,values);
			Log.d(TAG, "Feed id " + rowId);
			if(rowId > 0){
				Uri contentUri = ContentUris.withAppendedId(Feed.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(contentUri, null);
				return contentUri;
			}
			else break;
		case ITEM:
			db = helper.getWritableDatabase();
			rowId = helper.insertItem(db,values);
			Log.d(TAG, "Item id " + rowId);
			if(rowId > 0){
				Uri contentUri = ContentUris.withAppendedId(Item.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(contentUri, null);
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
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		//set the default sort order in the switch.
		//Then we can use a single check for setting the orderby claus.
		String defaultSortOrder ="";
		switch(uriMatcher.match(uri)){
		case FEED:
			qb.setTables(Feed.TABLE_NAME);
			defaultSortOrder = Feed.DEFAULT_SORT;
			break;
		//TODO: Unit Test
		case FEED_ID:
		    qb.setTables(Feed.TABLE_NAME);
		    defaultSortOrder = Feed.DEFAULT_SORT;
		    qb.appendWhere(Feed._ID + " = " + uri.getPathSegments().get(1));
		    break;
		case ITEM:
			qb.setTables(Item.TABLE_NAME);
			defaultSortOrder = Item.DEFAULT_SORT;
			break;
		case ITEM_ID:
			qb.setTables(Item.TABLE_NAME);
			qb.appendWhere(Item._ID + " = " + uri.getPathSegments().get(1));
			defaultSortOrder = Item.DEFAULT_SORT;
			break;
		default: unknownURI(uri); 
		}
		
		String orderBy;
		if(TextUtils.isEmpty(sortOrder)){
			//should be set in the URI switch above
			orderBy = defaultSortOrder;
		}else{
			orderBy = sortOrder;
		}
		
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String whereClause,
			String[] whereArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int rowsUpdated = 0;
		String id = "";

		switch(uriMatcher.match(uri)){
		case ITEM_ID:
			id = uri.getPathSegments().get(1);
			String where = Item._ID + "=?";
			String selectionArgs[] = new String[]{id};
			rowsUpdated = db.update(Item.TABLE_NAME, values, where, selectionArgs);
			break;
		default: unknownURI(uri);
		}
		
		return rowsUpdated;
	}

	/**
	 * Delete all the information in the DB tables, but leave the tables intact.
	 */
	public void deleteAll(){
		helper.clearDatabase(helper.getWritableDatabase());
	}
	
	/**
	 * Utility method to make handling an unknown URI consistent.
	 * @param uri
	 */
	private static void unknownURI(Uri uri){
		throw new IllegalArgumentException("Unknown URI " + uri.toString());
	}

	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		//the podcast/rss data model has a table for feeds (channels in RSS parlance)

		uriMatcher.addURI(Feed.BASE_AUTH, Feed.FEED_PATH, FEED);
		uriMatcher.addURI(Feed.BASE_AUTH, Feed.FEED_PATH + "/#", FEED_ID);
		uriMatcher.addURI(Feed.BASE_AUTH, Item.ITEM_PATH, ITEM);
		uriMatcher.addURI(Feed.BASE_AUTH, Item.ITEM_PATH + "/#", ITEM_ID);
	}


}
