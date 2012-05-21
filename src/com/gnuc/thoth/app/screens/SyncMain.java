package com.gnuc.thoth.app.screens;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gnuc.thoth.R;
import com.gnuc.thoth.app.TabMain;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.service.ThothService;

public class SyncMain extends Activity
{
	private static SyncMain	instance	= null;
	
	public static SyncMain getInstance()
	{
		return instance;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_main);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Thoth.TRACKER.trackPageView("/SYNC_MAIN");
		Thoth.cx = instance = this;
		Thoth.Settings.read();
		((TextView) findViewById(R.id.t_s_web_b_datetime_id)).setText(DateFormat.format("h:mm AA on dd/MM/yy", Thoth.Settings.APP_LAST_SYNC_THOTH));
		((Button) findViewById(R.id.t_s_web_b_button_id)).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startWebSync();
			}
		});
		// ((Button) findViewById(R.id.t_s_thoth_b_button_id)).setVisibility(View.VISIBLE);
		// ((Button) findViewById(R.id.t_s_thoth_b_button_id)).setOnClickListener(new
		// View.OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// ((Button) findViewById(R.id.t_s_thoth_b_button_id)).setVisibility(View.INVISIBLE);
		// Intent browserIntent = new Intent("android.intent.action.VIEW",
		// Uri.parse("https://thoth-server.appspot.com"));
		// Thoth.cx.startActivity(browserIntent);
		// }
		// });
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		Thoth.Settings.write();
	}
	
	public void startWebSync()
	{
		TabMain.g().TABMAIN.sendEmptyMessage(2);
		((Button) findViewById(R.id.t_s_web_b_button_id)).setVisibility(View.GONE);
		((ProgressBar) findViewById(R.id.t_s_web_b_progressbar)).setVisibility(View.VISIBLE);
		TextView tvv = (TextView) findViewById(R.id.t_s_web_b_status);
		tvv.setVisibility(View.VISIBLE);
		tvv.setText("Log update in progress.");
		//
		Intent i = new Intent(Thoth.cx, ThothService.class);
		i.setAction(String.valueOf(Thoth.REQ.LOG_UPDATE));
		Thoth.cx.startService(i);
	}
	
	public Handler	sHandler	= new Handler()
									{
										public void handleMessage(android.os.Message msg)
										{
											if (msg.what == 1)
											{
												runOnUiThread(new Runnable()
												{
													@Override
													public void run()
													{
														((Button) instance.findViewById(R.id.t_s_web_b_button_id)).setVisibility(View.VISIBLE);
														((ProgressBar) instance.findViewById(R.id.t_s_web_b_progressbar)).setVisibility(View.GONE);
														((TextView) instance.findViewById(R.id.t_s_web_b_datetime_id)).setText(DateFormat.format("h:mm AA on dd/MM/yy", Thoth.Settings.APP_LAST_SYNC_THOTH));
														((TextView) instance.findViewById(R.id.t_s_web_b_status)).setVisibility(View.GONE);
													}
												});
											}
										};
									};
}
