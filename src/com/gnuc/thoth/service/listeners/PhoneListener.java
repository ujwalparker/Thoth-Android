package com.gnuc.thoth.service.listeners;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.gnuc.thoth.framework.Thoth;

public class PhoneListener extends PhoneStateListener
{
	private static int	lastCallState	= TelephonyManager.CALL_STATE_IDLE;
	
	@Override
	public void onCallStateChanged(int state, String incomingNumber)
	{
		super.onCallStateChanged(state, incomingNumber);
		switch (state)
		{
			case TelephonyManager.CALL_STATE_IDLE :
			{
				if (state != lastCallState)
				{
					if (lastCallState == TelephonyManager.CALL_STATE_OFFHOOK || lastCallState == TelephonyManager.CALL_STATE_RINGING)
					{
						// INBOUND & OUTBOUND CALL SUCCESS || INBOUND CALL CUT & MISSED
						Thoth.sendToService(Thoth.REQ.CALL_CAPTURE, null, null);
						Thoth.showLogNotification("CALL logged");
					}
					lastCallState = state;
				}
				break;
			}
			case TelephonyManager.CALL_STATE_RINGING :
			{
				if (state != lastCallState)
				{
					Thoth.Call.count++;
					lastCallState = state;
				}
				break;
			}
			case TelephonyManager.CALL_STATE_OFFHOOK :
			{
				if (state != lastCallState)
				{
					if (!Thoth.Call.isACTIVE)
						Thoth.Call.count++;
					if (Thoth.Call.isOUTBOUND && Thoth.Call.isACTIVE)
						Thoth.Call.count++;
					else
						Thoth.Call.isACTIVE = true;
					lastCallState = state;
				}
				break;
			}
		}
	}
}
