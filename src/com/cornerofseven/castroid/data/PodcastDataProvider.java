package com.cornerofseven.castroid.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class PodcastDataProvider extends ContentProvider{
    private static final String TAG = "PodcastDataProvider";
    //TODO: Instantiate with a real object.
    private static final UriMatcher uriMatcher = null; // the JavaDocs show
    												   // this exists, but  new UriMatcher();
    												   // doesn't actually exist in the API

    private final DbHelper helper;
    
    public PodcastDataProvider(Context context){
    	helper = new DbHelper(context);
    }
    
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
                    Feed.LINK + " TEXT NOT NULL," +
                    Feed.TITLE + " TEXT NOT NULL, " +
                    Feed.DESCRIPTION + " TEXT NOT NULL, " +
                    Feed.IMAGE + " TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            Log.w(TAG, "Upgrading database from version" + oldVersion +" to " +
                    newVersion + ". Existing data will be deleted.");
            db.execSQL("DROP TABLE IF EXISTS "+Feed.TABLE_NAME);
            /*redundant?
            db.execSQL("DROP TABLE IF EXISTS "+Entry.TABLE_NAME);*/
            onCreate(db);
        }
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
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

}
