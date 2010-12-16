package com.cornerofseven.castroid.data;

import android.net.Uri;
import android.provider.BaseColumns;


public final class Item implements BaseColumns{
    public static final String BASE_AUTH = "com.cornerofseven.castroid.data.podcastdataprovider";

    //Non-instantiable class.
    private Item(){}

    /**
     * URI used to retrieve the content.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + BASE_AUTH + "/items");

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
    public static final String DEFAULT_SORT = "createdDt DESC";


    /******************************
     * Database Columns           *
     * Column Name - SQL Datatype *
     ******************************/
    /**
     * Name of the table.
     */
    public static final String TABLE_NAME = "ITEMS";

	public static final String OWNER = "OWNER";

	public static final String TITLE = "TITLE";

	public static final String LINK = "LINK";

	public static final String DESC = "DESC";
    

}
