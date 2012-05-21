package com.gnuc.thoth.framework.ad;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlTargeting;
import com.gnuc.thoth.R;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.ThothLog;

public class Advertisements implements AdWhirlLayout.AdWhirlInterface
{
	private static AdWhirlLayout								adWhirl	= null;
	private static View											adParent	= null;
	private static final RelativeLayout.LayoutParams	params	= new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	private static Advertisements								ad			= null;
	
	public Advertisements()
	{}
	
	@Override
	public void adWhirlGeneric()
	{}
	
	public void onFailedToReceiveAd()
	{
		try
		{
			Thoth.TRACKER.trackPageView("/adFailed");
			(adParent.findViewById(R.id.ad_default)).setVisibility(View.GONE);
			(adParent.findViewById(R.id.ad_gnuc)).setVisibility(View.VISIBLE);
			(adParent.findViewById(R.id.ad_gnuc)).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						Intent i = new Intent(Intent.ACTION_VIEW);
						// i.setData(Uri.parse("http://gnuconsultancy.mobify.me"));
						//
						i.setData(Uri.parse("market://details?id=com.gnuc.alphabetfive.lite"));
						Thoth.cx.startActivity(i);
					}
					catch (Exception e)
					{
						ThothLog.e(e);
					}
				}
			});
			adWhirl.adWhirlManager.resetRollover();
			adWhirl.rotateThreadedDelayed();
		}
		catch (Exception ex)
		{
			adWhirl.rollover();
			ThothLog.e(ex);
		}
	}
	
	public static Advertisements g()
	{
		if (null == ad)
			ad = new Advertisements();
		return ad;
	}
	
	public static void showAd(Activity activity, View parent)
	{
		try
		{
			adWhirl = new AdWhirlLayout(activity, activity.getResources().getString(R.string.ADWHIRL_KEY));
			adWhirl.setAdWhirlInterface(g());
			AdWhirlTargeting.setTestMode(false);
			adParent = parent;
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			LinearLayout adDefault = (LinearLayout) adParent.findViewById(R.id.ad_default);
			adDefault.addView(adWhirl, params);
			adDefault.setVisibility(View.VISIBLE);
			(adParent.findViewById(R.id.ad_gnuc)).setVisibility(View.GONE);
			//
			Thoth.TRACKER.trackPageView("/adRequested");
			//
			adParent.forceLayout();
			adParent.invalidate();
		}
		catch (Exception e)
		{
			ThothLog.e(e);
		}
	}
}
