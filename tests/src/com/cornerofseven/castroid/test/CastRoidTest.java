package com.cornerofseven.castroid.test;

import com.cornerofseven.castroid.Castroid;

import android.test.ActivityInstrumentationTestCase2;

/**
 * 
 * @author Sean Mooney
 *
 */
public class CastRoidTest extends ActivityInstrumentationTestCase2<Castroid>{

	private Castroid mActivity;
	
	public CastRoidTest(){
		super("com.cornerofsever.castroid.Castroid", Castroid.class);
	}
	
	@Override
	public void setUp() throws Exception{
		mActivity = this.getActivity();
	}
}
