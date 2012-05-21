package com.gnuc.thoth.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.service.listeners.PhoneListener;

public class PhoneStateReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(final Context cx, Intent intent)
	{
		Thoth.cx = cx;
		Thoth.Settings.read();
		if (!Thoth.Settings.APP_SETUP)
		{
			/**
			 * PhoneState Receiver
			 */
			((TelephonyManager) cx.getSystemService(Context.TELEPHONY_SERVICE)).listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
			if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") || intent.getAction().equals("android.provider.Telephony.SMS_SENT"))
			{
				Thoth.showLogNotification("SMS logged");
				Thoth.sendToService(Thoth.REQ.MSGS_CAPTURE, intent.getExtras(), intent);
			}
			if (intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED") || (intent.getType() != null && intent.getType().equals("application/vnd.wap.mms-message")))
			{
				Thoth.showLogNotification("MMS logged");
				Thoth.sendToService(Thoth.REQ.MSGM_CAPTURE, null, intent);
			}
			if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL"))
				Thoth.Call.isOUTBOUND = true;
		}
	}
}
