package com.gnuc.thoth.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.service.AppDataService;
import com.gnuc.thoth.service.MobileDataService;
import com.gnuc.thoth.service.WifiDataService;

public class DeviceStateReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context cx, Intent intent)
	{
		Thoth.cx = cx;
		Thoth.Data.read();
		if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
		{
			Thoth.Data.RESTART = true;
			Thoth.Data.DATA_RESET = Thoth.Data.MOBILE_NWK_RESET = Thoth.Data.WIFI_NWK_RESET = System.currentTimeMillis();
			Thoth.Data.write();
			Thoth.scheduleServices();
		}
		else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SHUTDOWN))
		{
			Thoth.Data.RESTART = false;
			Thoth.Data.write();
			NetworkInfo tni = ((ConnectivityManager) cx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			if (null == tni)
			{
				Thoth.Data.MOBILE_NWK_STATE_PREV = Thoth.Data.WIFI_NWK_STATE_PREV = TelephonyManager.DATA_DISCONNECTED;
				Thoth.Data.write();
				cx.startService(new Intent(cx, MobileDataService.class));
				cx.startService(new Intent(cx, WifiDataService.class));
			}
		}
		else if (intent.getAction().equalsIgnoreCase(WifiManager.WIFI_STATE_CHANGED_ACTION))
		{
			if (intent.getIntExtra("wifi_state", -1) == WifiManager.WIFI_STATE_DISABLING)
			{
				Thoth.Data.WIFI_INTRADAY_CORRECTION = true;
				Thoth.Data.write();
				cx.startService(new Intent(cx, WifiDataService.class));
			}
			else if (intent.getIntExtra("wifi_state", -1) == WifiManager.WIFI_STATE_DISABLED)
			{
				Thoth.Data.WIFI_DAILY_UL_CORRECTION = Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV = 0;
				Thoth.Data.WIFI_DAILY_DL_CORRECTION = Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV = 0;
				Thoth.Data.write();
				if (Thoth.Data.RESTART)
					cx.startService(new Intent(cx, AppDataService.class));
			}
			else if (intent.getIntExtra("wifi_state", -1) == WifiManager.WIFI_STATE_ENABLED)
			{
				Thoth.Data.WIFI_NWK_RESET = System.currentTimeMillis();
				Thoth.Data.write();
			}
		}
		else if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION))
		{
			NetworkInfo ni = intent.getParcelableExtra("networkInfo");
			if (ni.getType() == ConnectivityManager.TYPE_MOBILE)
			{
				if (ni.isConnected() && (Thoth.Data.MOBILE_NWK_STATE_PREV != TelephonyManager.DATA_CONNECTED || Thoth.Data.RESTART))
				{
					Thoth.Data.MOBILE_NWK_STATE_PREV = TelephonyManager.DATA_CONNECTED;
					Thoth.Data.write();
				}
				else if (!ni.isConnected() && Thoth.Data.MOBILE_NWK_STATE_PREV == TelephonyManager.DATA_CONNECTED && !Thoth.Data.RESTART)
				{
					Thoth.Data.MOBILE_NWK_STATE_PREV = TelephonyManager.DATA_DISCONNECTED;
					Thoth.Data.write();
					cx.startService(new Intent(cx, MobileDataService.class));
				}
				else if (!ni.isConnected() && Thoth.Data.RESTART)
				{
					cx.startService(new Intent(cx, AppDataService.class));
				}
			}
		}
	}
}
