package com.cornerofseven.castroid.rss.feed;

/**
 * Model the Item element in an RSS stream.
 * @author sean
 *
 */
public class RSSItem {
	private String mTitle, mLink, mDesc, mEnclosure;

	
	/**
	 * @param mTitle
	 * @param mLink
	 * @param mDesc
	 * @param mEnclosure
	 */
	public RSSItem(String mTitle, String mLink, String mDesc, String mEnclosure) {
		this.mTitle = mTitle;
		this.mLink = mLink;
		this.mDesc = mDesc;
		this.mEnclosure = mEnclosure;
	}

	/**
	 * @return the mTitle
	 */
	public final String getmTitle() {
		return mTitle;
	}

	/**
	 * @param mTitle the mTitle to set
	 */
	public final void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	/**
	 * @return the mLink
	 */
	public final String getmLink() {
		return mLink;
	}

	/**
	 * @param mLink the mLink to set
	 */
	public final void setmLink(String mLink) {
		this.mLink = mLink;
	}

	/**
	 * @return the mDesc
	 */
	public final String getmDesc() {
		return mDesc;
	}

	/**
	 * @param mDesc the mDesc to set
	 */
	public final void setmDesc(String mDesc) {
		this.mDesc = mDesc;
	}

	/**
	 * @return the mEnclosure
	 */
	public final String getmEnclosure() {
		return mEnclosure;
	}

	/**
	 * @param mEnclosure the mEnclosure to set
	 */
	public final void setmEnclosure(String mEnclosure) {
		this.mEnclosure = mEnclosure;
	}
}
