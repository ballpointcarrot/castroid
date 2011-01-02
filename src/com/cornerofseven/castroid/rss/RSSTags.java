package com.cornerofseven.castroid.rss;

/**
 * Defines all of the RSS tags that we care about.
 * Tags are divided into 3 levels, 1.0, 1.1 and 2.0
 * specs.  The specs extend each other, and the class
 * hierarchy here does the same.
 * @author Sean Mooney
 *
 */
public final class RSSTags {
	
	private RSSTags(){}
	
	public static final String RSS = "rss";
	public static final String RSS_VERSION = "version";
	
	public static final String CHANNEL = "channel";
	public static final String CHNL_TITLE = "title";
	public static final String CHNL_LINK = "link";
	public static final String CHNL_DESC = "description";
	
	public static final String ITEM = "item";
	public static final String ITEM_TITLE = "title";
	public static final String ITEM_LINK = "link";
	public static final String ITEM_DESC = "description";
	public static final String ITEM_ENC = "enclosure";
	
	//////////////////ENCLOSURE ATTRIBUTES/////////////////////
	public static final String ENC_URL = "url";
	public static final String ENC_TYPE = "type";
	public static final String ENC_LEN = "length";
}
