package com.cornerofseven.castroid.data;

import android.content.ContentProvider;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SqliteOpenHelper;

public class PodcastDataProvider extends ContentProvider{
    private static final String TAG = "PodcastDataProvider";
    private static final UriMatcher uriMatcher;

    private static class DbHelper extends SQLiteOpenHelper{
        private static final String DB_NAME = "podcast.db";
        private static final int DB_VERSION = 1;

        public PodcastDataProvider(Context ctx){
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
            db.execSQL("DROP TABLE IF EXISTS "+Entry.TABLE_NAME);
            onCreate(db);
        }
    }

}
