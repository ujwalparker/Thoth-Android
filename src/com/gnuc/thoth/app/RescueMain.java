package com.gnuc.thoth.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.gnuc.thoth.R;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.ThothMail;
import com.gnuc.thoth.framework.callbacks.ThothMailCallback;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class RescueMain extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Thoth.cx = this;
		//
		Thoth.TRACKER = GoogleAnalyticsTracker.getInstance();
		Thoth.TRACKER.setAnonymizeIp(true);
		Thoth.TRACKER.startNewSession("UA-24128171-1", RescueMain.this);
		//
		AlertDialog.Builder adb = new AlertDialog.Builder(RescueMain.this);
		adb.setIcon(R.drawable.icon);
		adb.setTitle("Oops!");
		adb.setMessage("Thoth has failed due to an error.\n\nWould you like to send the error logs to its developer's?\n\nThey will work towards avoiding such errors in the future.\n\nNo information identifying you is sent :)");
		adb.setCancelable(false);
		adb.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(final DialogInterface dialog, int id)
			{
				dialog.dismiss();
				final ProgressDialog pd = ProgressDialog.show(RescueMain.this, "", "Please wait while logs are uploaded.\n\nRestart Thoth after sometime.", true, false);
				new Thread()
				{
					@Override
					public void run()
					{
						new Runnable()
						{
							@Override
							public void run()
							{
								ThothMail.getInstance().sendMailToDev(new ThothMailCallback()
								{
									@Override
									public void progress(int resultCode, int progressValue)
									{}
									
									@Override
									public void handle(boolean result, int resultCode)
									{
										runOnUiThread(new Runnable()
										{
											@Override
											public void run()
											{
												pd.dismiss();
												Main.getInstance().mainHandler.sendEmptyMessage(666);
											}
										});
									}
								});
							}
						}.run();
					}
				}.start();
			}
		});
		adb.setNegativeButton("NO", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
				Main.getInstance().mainHandler.sendEmptyMessage(666);
			}
		});
		adb.create().show();
		//
		Thoth.TRACKER.trackPageView("/FAILURE");
		Thoth.TRACKER.dispatch();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
}