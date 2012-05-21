package com.gnuc.thoth.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.service.AppDataService;
import com.gnuc.thoth.service.MobileDataService;
import com.gnuc.thoth.service.WifiDataService;

public class EndOfDayReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(final Context cx, Intent intent)
	{
		Thoth.cx = cx;
		Thoth.Settings.read();
		if (!Thoth.Settings.APP_SETUP)
		{
			// ThothLog.i("EndOfDayReceiver : Started");
			Thoth.Data.read();
			Thoth.Data.DAILY_CORRECTION = Thoth.Data.MOBILE_DAILY_CORRECTION = Thoth.Data.WIFI_DAILY_CORRECTION = true;
			Thoth.Data.write();
			cx.startService(new Intent(cx, MobileDataService.class));
			cx.startService(new Intent(cx, WifiDataService.class));
			cx.startService(new Intent(cx, AppDataService.class));
		}
	}
}
