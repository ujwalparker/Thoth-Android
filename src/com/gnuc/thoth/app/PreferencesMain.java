package com.gnuc.thoth.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.gnuc.thoth.R;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.Thoth.Settings;

public class PreferencesMain extends PreferenceActivity
{
	Preference	dataCapture				= null;
	Preference	smsCalendarCapture	= null;
	Preference	reportLogs				= null;
	Preference	THOTH_PRO				= null;
	//
	Preference	LOG_MAIL_LOGS			= null;
	// Preference REPORT_CONSTRUCT = null;
	// Preference REPORT_MAIL = null;
	// Preference LOG_RESTORE = null;
	//
	int			counter					= 0;
	
	//
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Thoth.cx = getBaseContext();
		Settings.read();
		addPreferencesFromResource(R.xml.thoth_preferences);
		//
		((Preference) findPreference("APP_GoMAIL")).setSummary(Settings.APP_GoMAIL ? "Enabled" : "Disabled");
		((Preference) findPreference("APP_GoCAL_CALL")).setSummary(Settings.APP_GoCAL_CALL ? "Enabled" : "Disabled");
		((Preference) findPreference("APP_GoCAL_MSG")).setSummary(Settings.APP_GoCAL_MSG ? "Enabled" : "Disabled");
		//
		dataCapture = (Preference) findPreference("APP_LOG_DATA");
		dataCapture.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference preference)
			{
				Settings.APP_ENABLE_DATA_CAPTURE = !Settings.APP_ENABLE_DATA_CAPTURE;
				if (Settings.APP_ENABLE_DATA_CAPTURE)
					dataCapture.setSummary("Data uploads and downloads will be monitored");
				else
					dataCapture.setSummary("Data uploads and downloads is not monitored");
				Toast.makeText(Thoth.cx, "Data uploads and downloads monitoring : " + (Settings.APP_ENABLE_DATA_CAPTURE ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
				Settings.write();
				return true;
			}
		});
		reportLogs = (Preference) findPreference("APP_REPORT_LOGS");
		reportLogs.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference preference)
			{
				Settings.APP_REPORT_LOGS = !Settings.APP_REPORT_LOGS;
				if (Settings.APP_REPORT_LOGS)
				{
					reportLogs.setSummary("Thoth will email you all the logs it collects every week");
					Toast.makeText(Thoth.cx, "Thoth will email you all the logs it collects every week", Toast.LENGTH_SHORT).show();
				}
				else
				{
					reportLogs.setSummary("Thoth will not email you logs.");
					Toast.makeText(Thoth.cx, "Thoth will not email you logs", Toast.LENGTH_SHORT).show();
				}
				Settings.write();
				return true;
			}
		});
		LOG_MAIL_LOGS = (Preference) findPreference("LOG_MAIL_LOGS");
		LOG_MAIL_LOGS.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference preference)
			{
				if (!Settings.APP_SETUP && counter == 4)
				{
					counter = 0;
					Thoth.sendToService(Thoth.REQ.MAIL_DEBUG_LOGS, null, null);
					Toast.makeText(Thoth.cx, "Mailing your logs to the developer :)", Toast.LENGTH_SHORT).show();
				}
				else
					counter += 1;
				return true;
			}
		});
		// REPORT_CONSTRUCT = (Preference) findPreference("REPORT_CONSTRUCT");
		// REPORT_CONSTRUCT.setOnPreferenceClickListener(new OnPreferenceClickListener()
		// {
		// public boolean onPreferenceClick(Preference preference)
		// {
		// if (!Settings.APP_SETUP)
		// {
		// Thoth.sendToService(Thoth.REQ.REPORT_CONSTRUCTOR, null, null);
		// Toast.makeText(Thoth.cx, "REPORT_CONSTRUCTOR started", Toast.LENGTH_SHORT).show();
		// }
		// return true;
		// }
		// });
		// REPORT_MAIL = (Preference) findPreference("REPORT_MAIL");
		// REPORT_MAIL.setOnPreferenceClickListener(new OnPreferenceClickListener()
		// {
		// public boolean onPreferenceClick(Preference preference)
		// {
		// if (!Settings.APP_SETUP)
		// {
		// Thoth.sendToService(Thoth.REQ.REPORT_MAILER, null, null);
		// Toast.makeText(Thoth.cx, "REPORT_MAILER started", Toast.LENGTH_SHORT).show();
		// }
		// return true;
		// }
		// });
		// LOG_RESTORE = (Preference) findPreference("LOG_RESTORE");
		// LOG_RESTORE.setOnPreferenceClickListener(new OnPreferenceClickListener()
		// {
		// public boolean onPreferenceClick(Preference preference)
		// {
		// if (!Settings.APP_SETUP)
		// {
		// Thoth.sendToService(Thoth.REQ.LOG_RESTORE, null, null);
		// Toast.makeText(Thoth.cx, "LOG_RESTORE started", Toast.LENGTH_SHORT).show();
		// }
		// return true;
		// }
		// });
		//
		THOTH_PRO = (Preference) findPreference("THOTH_PRO");
		THOTH_PRO.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference preference)
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.gnuc.thoth.pro")));
				return true;
			}
		});
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Settings.read();
		//
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Settings.APP_GoMAIL = prefs.getBoolean("APP_GoMAIL", Settings.APP_GoMAIL);
		Settings.APP_GoCAL = prefs.getBoolean("APP_GoCAL", Settings.APP_GoCAL);
		Settings.APP_GoCAL_CALL = prefs.getBoolean("APP_GoCAL_CALL", Settings.APP_GoCAL_CALL);
		Settings.APP_GoCAL_MSG = prefs.getBoolean("APP_GoCAL_MSG", Settings.APP_GoCAL_MSG);
		Settings.APP_ENABLE_DATA_CAPTURE = prefs.getBoolean("APP_ENABLE_DATA_CAPTURE", Settings.APP_ENABLE_DATA_CAPTURE);
		Settings.APP_REPORT_LOGS = prefs.getBoolean("APP_REPORT_LOGS", Settings.APP_REPORT_LOGS);
	}
}
