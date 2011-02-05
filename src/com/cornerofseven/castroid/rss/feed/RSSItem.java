package com.cornerofseven.castroid.rss.feed;

/**
 * Model the Item element in an RSS stream.
 * @author sean
 *
 */
public class RSSItem {
	private String mTitle, mLink, mDesc, mEnclosure, mEnclosureType;
	private String mPubDate;
	private long mEncSize;
	
	/**
	 * 
	 */
	public RSSItem(){
		this("", "", "", "", "", -1, "");
	}
	
	/**
	 * @param title
	 * @param link
	 * @param desc
	 */
	public RSSItem(String title, String link, String desc) {
		this(title, link, desc, "", "", -1, "");
	}

	/**
	 * 
	 * @param title
	 * @param link
	 * @param desc
	 * @param pubDate
	 * @param enclosure link for the enclosure data file
	 * @param encSize
	 * @param encType
	 */
	public RSSItem(String title, String link, String desc, String pubDate, String enclosure, long encSize, String encType )
	{
		this.mTitle = title;
		this.mLink = link;
		this.mDesc = desc;
		this.mPubDate = pubDate;
		this.mEnclosure = enclosure;
		this.mEncSize = encSize;
		this.mEnclosureType = encType;
	}
	/**
	 * @return the mTitle
	 */
	public final String getTitle() {
		return mTitle;
	}

	/**
	 * @param mTitle the mTitle to set
	 */
	public final void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	/**
	 * @return the mLink
	 */
	public final String getLink() {
		return mLink;
	}

	/**
	 * @param mLink the mLink to set
	 */
	public final void setLink(String mLink) {
		this.mLink = mLink;
	}

	/**
	 * @return the mDesc
	 */
	public final String getDesc() {
		return mDesc;
	}

	/**
	 * @param mDesc the mDesc to set
	 */
	public final void setDesc(String mDesc) {
		this.mDesc = mDesc;
	}

	/**
	 * @return the mEnclosure
	 */
	public final String getEnclosure() {
		return mEnclosure;
	}

	/**
	 * @param mEnclosure the mEnclosure to set
	 */
	public final void setEnclosure(String mEnclosure) {
		this.mEnclosure = mEnclosure;
	}
	
	/**
	 * 
	 * @param type enclusure type
	 */
	public final void setEnclosureType(String type){
		this.mEnclosureType = type;
	}
	
	/**
	 * 
	 * @return 
	 */
	public final String getEnclosureType(){
		return this.mEnclosureType;
	}
	
	
	
	/**
	 * @return the mEncSize
	 */
	public final long getEnclosureLength() {
		return mEncSize;
	}

	/**
	 * @param mEncSize the mEncSize to set
	 */
	public final void setEnclosureLength(long mEncSize) {
		this.mEncSize = mEncSize;
	}

	/**
	 * Sets the date the item was published.
	 * @param date
	 */
	public void setPubDate(String date){
		this.mPubDate = date;
	}
	
	/**
	 * 
	 * @return the date the item was published.
	 */
	public String getPubDate(){
		return this.mPubDate;
	}
	
	@Override
	public String toString(){
		return this.mTitle;
	}
}
