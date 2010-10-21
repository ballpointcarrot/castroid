package com.cornerofseven.castroid.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SqliteOpenHelper;
import static android.provider.BaseColumns._ID;

public class PodcastDataProvider extends SQLiteOpenHelper{
	private static final String DB_NAME = "podcast.db";
	private static final int DB_VERSION = 1;

	public PodcastDataProvider(Context ctx){
		super(ctx, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL("CREATE TABLE PODCAST(" + _ID + 
		"INTEGER PRIMARY KEY AUTOINCREMENT, URL TEXT NOT NULL," +
		"TITLE TEXT NOT NULL

	}
}
