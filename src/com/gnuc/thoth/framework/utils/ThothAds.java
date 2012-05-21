package com.gnuc.thoth.framework.utils;

import com.gnuc.thoth.app.TabMain;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest.ErrorCode;

public class ThothAds implements AdListener
{
	@Override
	public void onDismissScreen(Ad ad)
	{}
	
	@Override
	public void onFailedToReceiveAd(Ad ad, ErrorCode ec)
	{
		TabMain.g().TABMAIN.sendEmptyMessage(-10);
	}
	
	@Override
	public void onLeaveApplication(Ad ad)
	{}
	
	@Override
	public void onPresentScreen(Ad ad)
	{}
	
	@Override
	public void onReceiveAd(Ad ad)
	{
		TabMain.g().TABMAIN.sendEmptyMessage(10);
	}
}
