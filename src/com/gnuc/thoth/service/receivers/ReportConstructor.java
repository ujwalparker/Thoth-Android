package com.gnuc.thoth.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.Thoth.Settings;

public class ReportConstructor extends BroadcastReceiver
{
	@Override
	public void onReceive(final Context cx, Intent intent)
	{
		Thoth.cx = cx;
		Settings.read();
		if (!Settings.APP_SETUP)
			Thoth.sendToService(Thoth.REQ.REPORT_CONSTRUCTOR, null, null);
	}
}
