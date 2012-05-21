package com.gnuc.thoth.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.service.AppDataService;
import com.gnuc.thoth.service.MobileDataService;
import com.gnuc.thoth.service.ThothService;
import com.gnuc.thoth.service.WifiDataService;

public class StartOfDayReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(final Context cx, Intent intent)
	{
		Thoth.cx = cx;
		Thoth.Settings.read();
		if (!Thoth.Settings.APP_SETUP)
		{
			// ThothLog.i("StartOfDayReceiver : Started");
			Thoth.Data.read();
			Thoth.Data.MOBILE_PREV_DAY_ID = Thoth.Data.MOBILE_CURR_DAY_ID;
			Thoth.Data.WIFI_PREV_DAY_ID = Thoth.Data.WIFI_CURR_DAY_ID;
			Thoth.Data.PREV_DAY_ID = Thoth.Data.CURR_DAY_ID;
			Thoth.Data.CURR_DAY_ID = Thoth.Data.MOBILE_CURR_DAY_ID = Thoth.Data.WIFI_CURR_DAY_ID = System.currentTimeMillis();
			Thoth.Data.NEW_DAY = Thoth.Data.MOBILE_NEW_DAY = Thoth.Data.WIFI_NEW_DAY = true;
			Thoth.Data.write();
			cx.startService(new Intent(cx, MobileDataService.class));
			cx.startService(new Intent(cx, WifiDataService.class));
			cx.startService(new Intent(cx, AppDataService.class));
			Intent i = new Intent(Thoth.cx, ThothService.class);
			i.setAction(String.valueOf("1000"));
			cx.startService(i);
		}
	}
}
