package com.gnuc.thoth.app;

import android.app.ActivityGroup;
import android.app.AlarmManager;
import android.app.LocalActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import com.gnuc.thoth.R;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.Thoth.Settings;
import com.gnuc.thoth.framework.ThothLog;
import com.gnuc.thoth.framework.utils.date.ThothDate;
import com.gnuc.thoth.service.receivers.LogBackupReceiver;
import com.gnuc.thoth.service.receivers.ReportConstructor;
import com.gnuc.thoth.service.receivers.ReportMailer;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Main extends ActivityGroup
{
	public RelativeLayout				ml			= null;
	protected LocalActivityManager	mlam		= null;
	private View							mlv		= null;
	private static Main					instance	= null;
	
	public static Main getInstance()
	{
		if (instance == null)
			instance = new Main();
		return instance;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//
		Thoth.TRACKER = GoogleAnalyticsTracker.getInstance();
		Thoth.TRACKER.setAnonymizeIp(true);
		//
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		//
		Thoth.cx = this;
		instance = this;
		Settings.read();
		//
		Thoth.TRACKER.startNewSession("UA-24XXXX71-1", Thoth.cx);
		if (Settings.APP_TRACKING_PENDING)
		{
			Settings.APP_TRACKING_PENDING = Thoth.TRACKER.dispatch();
			Settings.write();
		}
		Thoth.clearNotifications();
		//
		init();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		if (null != Thoth.ad && Thoth.ad.isShowing())
			Thoth.ad.dismiss();
		if (Thoth.TRACKER.dispatch())
			Settings.APP_TRACKING_PENDING = false;
		Settings.write();
		Thoth.TRACKER.stopSession();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		super.onKeyDown(keyCode, event);
		try
		{
			if (keyCode == KeyEvent.KEYCODE_BACK && null != mlam && null != mlam.getCurrentId())
			{
				if (null != mlam && mlam.getCurrentId().equalsIgnoreCase("SETUP_MAIN"))
					return mlam.getActivity("SETUP_MAIN").dispatchKeyEvent(event);
				else
					return true;
			}
		}
		catch (Exception e)
		{
			ThothLog.e(e);
		}
		return false;
	}
	
	private void init()
	{
		try
		{
			ml = (RelativeLayout) findViewById(R.id.main_layout);
			ml.invalidate();
			mlam = (LocalActivityManager) getLocalActivityManager();
			mainHandler.sendEmptyMessage(0);
			Thoth.scheduleServices();
		}
		catch (Exception e)
		{
			ThothLog.e(e);
			mainHandler.sendEmptyMessage(-1);
		}
	}
	
	public Handler	mainHandler	= new Handler()
										{
											public void handleMessage(Message msg)
											{
												switch (msg.what)
												{
													case 0 :
													{
														mainHandler.sendEmptyMessage(Settings.APP_SETUP ? 1 : 2);
														break;
													}
													case 1 :
													{
														Intent i = new Intent(Thoth.cx, SetupMain.class);
														ml.removeAllViews();
														ml.addView(getContentView("SETUP_MAIN", i));
														Thoth.TRACKER.trackPageView("/SETUP_MAIN");
														break;
													}
													case 2 :
													{
														Intent i = new Intent(Thoth.cx, TabMain.class);
														ml.removeAllViews();
														ml.addView(getContentView("TAB_MAIN", i));
														Thoth.TRACKER.trackPageView("/TAB_MAIN");
														//
														if (Settings.APP_BACKUP_PENDING)
															mainHandler.sendEmptyMessageDelayed(5, 2000);
														if (Settings.APP_REPORT_CONSTRUCTION_PENDING)
															mainHandler.sendEmptyMessageDelayed(6, 3000);
														if (Settings.APP_REPORT_MAIL_PENDING)
															mainHandler.sendEmptyMessageDelayed(7, 4000);
														break;
													}
													case 5 :
													{
														/**
														 * Weekly Log Backup
														 */
														AlarmManager aMgr = (AlarmManager) Thoth.cx.getSystemService(Context.ALARM_SERVICE);
														PendingIntent pi = PendingIntent.getBroadcast(Thoth.cx, 0, new Intent(Thoth.cx, LogBackupReceiver.class), 0);
														aMgr.cancel(pi);
														aMgr.setRepeating(AlarmManager.RTC_WAKEUP, (ThothDate.now.plusMinutes(60)).getMillis(), AlarmManager.INTERVAL_DAY, pi);
														break;
													}
													case 6 :
													{
														/**
														 * Weekly Log Report Constructor
														 */
														AlarmManager aMgr = (AlarmManager) Thoth.cx.getSystemService(Context.ALARM_SERVICE);
														PendingIntent pi = PendingIntent.getBroadcast(Thoth.cx, 0, new Intent(Thoth.cx, ReportConstructor.class), 0);
														aMgr.cancel(pi);
														aMgr.setRepeating(AlarmManager.RTC_WAKEUP, (ThothDate.now.plusMinutes(20)).getMillis(), AlarmManager.INTERVAL_DAY, pi);
														break;
													}
													case 7 :
													{
														/**
														 * Weekly Log Report Mailer
														 */
														AlarmManager aMgr = (AlarmManager) Thoth.cx.getSystemService(Context.ALARM_SERVICE);
														PendingIntent pi = PendingIntent.getBroadcast(Thoth.cx, 0, new Intent(Thoth.cx, ReportMailer.class), 0);
														aMgr.cancel(pi);
														aMgr.setRepeating(AlarmManager.RTC_WAKEUP, (ThothDate.now.plusMinutes(10)).getMillis(), AlarmManager.INTERVAL_DAY, pi);
														break;
													}
													case -1 :
													{
														Intent i = new Intent(Thoth.cx, RescueMain.class);
														ml.removeAllViews();
														ml.addView(getContentView("RESCUE_MAIN", i));
														Thoth.TRACKER.trackPageView("/RESCUE_MAIN");
														break;
													}
													case 666 :
													{
														finish();
														break;
													}
												}
											}
										};
	
	public View getContentView(String tag, Intent intent)
	{
		if (mlam == null)
			throw new IllegalStateException("Did you forget to call 'public void setup(LocalActivityManager activityGroup)'?");
		final Window w = mlam.startActivity(tag, intent);
		final View wd = w != null ? w.getDecorView() : null;
		if (mlv != wd && mlv != null && mlv.getParent() != null)
			ml.removeView(mlv);
		mlv = wd;
		if (mlv != null)
		{
			mlv.setVisibility(View.VISIBLE);
			mlv.setFocusableInTouchMode(true);
			((ViewGroup) mlv).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		}
		return mlv;
	}
}
