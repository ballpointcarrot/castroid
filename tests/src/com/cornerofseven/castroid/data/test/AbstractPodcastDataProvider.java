package com.cornerofseven.castroid.data.test;

import com.cornerofseven.castroid.data.Feed;
import com.cornerofseven.castroid.data.PodcastDataProvider;

import android.test.ProviderTestCase2;

/**
 * A base class to set up the mock content podcast data provider.
 * Any class testing the data provider somehow should extend this class.
 * 
 * @author Sean Mooney
 *
 */
public abstract class AbstractPodcastDataProvider extends ProviderTestCase2<PodcastDataProvider>{
	public AbstractPodcastDataProvider(){
		//this(PodcastDataProvider.class, "content://" + Feed.BASE_AUTH);
		this(PodcastDataProvider.class, Feed.BASE_AUTH);
	}
	
	public AbstractPodcastDataProvider(Class<PodcastDataProvider> providerClass,
			String providerAuthority) {
		super(providerClass, providerAuthority);
	}
	
	/**
	 * Initialize the simple data test provider
	 */
	public void initDatabase(){
		
	}
}
