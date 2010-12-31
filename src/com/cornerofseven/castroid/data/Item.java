package com.cornerofseven.castroid.data;

import android.net.Uri;
import android.provider.BaseColumns;


public final class Item implements BaseColumns{

    //Non-instantiable class.
    private Item(){}

    public static final String ITEM_PATH = "items";
    
    /**
     * URI used to retrieve the content.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + Feed.BASE_AUTH + "/" + ITEM_PATH);

    /**
     * Feed MIME type for Directory for Content Provider
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cornerofseven.item";

    /**
     * Feed MIME type for Item for Content Provider
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.dir/vnd.cornerofseven.item";

    /**
     * How items are sorted by default
     */
    public static final String DEFAULT_SORT = "TITLE";


    /******************************
     * Database Columns           *
     * Column Name - SQL Datatype *
     ******************************/
    /**
     * Name of the table.
     */
    public static final String TABLE_NAME = "items";

	public static final String OWNER = "OWNER";

	public static final String TITLE = "TITLE";

	public static final String LINK = "LINK";

	public static final String DESC = "DESC";
    

}
