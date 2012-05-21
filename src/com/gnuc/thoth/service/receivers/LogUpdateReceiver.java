package com.gnuc.thoth.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.service.ThothService;

public class LogUpdateReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context cx, Intent intent)
	{
		Thoth.cx = cx;
		Intent i = new Intent(cx, ThothService.class);
		i.setAction(String.valueOf(Thoth.REQ.LOG_UPDATE));
		cx.startService(i);
	}
}
