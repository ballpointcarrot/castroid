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

    /**
     * Name of the owner field, foreign key.
     */
	public static final String OWNER = "OWNER";

	/**
	 * Title field for the item.
	 */
	public static final String TITLE = "TITLE";

	/**
	 * Link field for the item.
	 */
	public static final String LINK = "LINK";

	/**
	 * Description field for the item.
	 */
	public static final String DESC = "DESC";
    
	/**
	 * Link from the enclosure sub-element.
	 */
	public static final String ENC_LINK = "ENC_LINK";
	
	/**
	 * Type attribute from the enclosure element.
	 */
	public static final String ENC_TYPE = "ENC_TYPE";
	
	/**
	 * Size field for the enclosure file to download.
	 */
	public static final String ENC_SIZE = "ENC_SIZE";
	
	/**
	 * Projection map for all the elements of the Item
	 */
	public static final String[] PROJECTION = {_ID, OWNER, TITLE, LINK, DESC, ENC_LINK, ENC_TYPE, ENC_SIZE};
    
}
