package com.gnuc.thoth.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gnuc.thoth.framework.Thoth;

public class LogSyncReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(final Context cx, Intent intent)
	{
		Thoth.cx = cx;
		Thoth.Settings.read();
		if (!Thoth.Settings.APP_SETUP)
			Thoth.sendToService(Thoth.REQ.SYNC_CAPTURE, null, null);
	}
}
