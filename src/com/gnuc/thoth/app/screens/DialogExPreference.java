package com.gnuc.thoth.app.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.DialogPreference;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.gnuc.thoth.R;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.Thoth.Settings;
import com.gnuc.thoth.framework.callbacks.ThothCallback;
import com.gnuc.thoth.framework.recovery.MessageRecovery;

public class DialogExPreference extends DialogPreference
{
	Context	cx;
	
	public DialogExPreference(Context oContext, AttributeSet attrs)
	{
		super(oContext, attrs);
		cx = oContext;
	}
	
	@Override
	protected void onBindDialogView(final View view)
	{
		super.onBindDialogView(view);
		if (view.getId() == R.id.setup_gmail)
		{
			if (Settings.APP_GoMAIL)
			{
				final Button button = (Button) view.findViewById(R.id.gmail_setup_bt);
				button.setText("Disconnect GMail");
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Settings.APP_GoMAIL = false;
						Settings.write();
						button.setText("Connect GMail");
						getDialog().cancel();
						((DialogExPreference) findPreferenceInHierarchy("APP_GoMAIL")).setSummary("Disabled");
					}
				});
			}
			else
			{
				final Button button = (Button) view.findViewById(R.id.gmail_setup_bt);
				button.setText("Connect GMail");
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Thoth.createThothAsContact(new ThothCallback()
						{
							@Override
							public void handle(boolean result)
							{
								if (result)
								{
									Settings.APP_GoMAIL = true;
									Settings.write();
									button.setText("Disconnect GMail");
									getDialog().cancel();
									((DialogExPreference) findPreferenceInHierarchy("APP_GoMAIL")).setSummary("Enabled");
								}
							}
						});
					}
				});
			}
		}
		else if (view.getId() == R.id.setup_gcal)
		{
			if (Settings.APP_GoCAL)
			{
				final Button button = (Button) view.findViewById(R.id.calendar_setup_bt);
				button.setText("Disconnect Calendar");
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Settings.APP_GoCAL = false;
						Settings.write();
						button.setText("Connect Calendar");
						getDialog().cancel();
						((DialogExPreference) findPreferenceInHierarchy("APP_GoCAL")).setSummary("Disabled");
					}
				});
			}
			else
			{
				final Button button = (Button) view.findViewById(R.id.calendar_setup_bt);
				final ListView calendarList = (ListView) view.findViewById(R.id.calendar_setup_list);
				button.setText("Connect Calendar");
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						button.setVisibility(View.INVISIBLE);
						((TextView) view.findViewById(R.id.calendar_setup_msg)).setText(R.string.st_calendar_msg_1_2);
						final HashMap<String, Integer> calendarMap = new HashMap<String, Integer>();
						final Uri CALENDAR = (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) ? CalendarContract.CONTENT_URI : (Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) ? Uri.parse("content://com.android.calendar") : Uri.parse("content://calendar");
						Cursor c = null;
						try
						{
							if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
							{
								c = Thoth.cx.getContentResolver().query(Uri.withAppendedPath(CALENDAR, "calendars"), null, null, null, null);
								while (c != null && c.moveToNext())
								{
									if (c.getString(c.getColumnIndex(CalendarContract.CalendarEntity.ACCOUNT_TYPE)).equalsIgnoreCase("com.google") && c.getString(c.getColumnIndex(CalendarContract.CalendarEntity.ACCOUNT_NAME)).equalsIgnoreCase(Settings.USR_DEVICE_ACCOUNT_EMAIL))
										calendarMap.put(c.getString(c.getColumnIndex(CalendarContract.CalendarEntity.CALENDAR_DISPLAY_NAME)), c.getInt(c.getColumnIndex(CalendarContract.CalendarEntity._ID)));
								}
							}
							else
							{
								c = Thoth.cx.getContentResolver().query(Uri.withAppendedPath(CALENDAR, "calendars"), new String[]{"_id", "displayname", "_sync_account", "_sync_account_type"}, null, null, "displayname ASC");
								while (c != null && c.moveToNext())
								{
									if (c.getString(3).equalsIgnoreCase("com.google") && c.getString(2).equalsIgnoreCase(Settings.USR_DEVICE_ACCOUNT_EMAIL))
										calendarMap.put(c.getString(1), c.getInt(0));
								}
							}
						}
						catch (IllegalArgumentException e)
						{
							Log.e(Thoth.TAG, "Calendars not available", e);
						}
						finally
						{
							if (c != null)
								c.close();
						}
						final List<String> calendarNames = new ArrayList<String>();
						for (String calendar : calendarMap.keySet())
							calendarNames.add(calendar);
						calendarList.setVisibility(View.VISIBLE);
						calendarList.setAdapter(new ArrayAdapter<String>(Thoth.cx, android.R.layout.simple_expandable_list_item_1, calendarNames));
						calendarList.setOnItemClickListener(new OnItemClickListener()
						{
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
							{
								if (calendarMap.containsKey(calendarNames.get(pos)))
								{
									Settings.USR_DEVICE_ACCOUNT_CALENDAR = calendarMap.get(calendarNames.get(pos));
									Settings.APP_GoCAL = true;
									Settings.write();
									calendarList.setVisibility(View.GONE);
									button.setVisibility(View.VISIBLE);
									button.setText("Disconnect Calendar");
									getDialog().cancel();
									((DialogExPreference) findPreferenceInHierarchy("APP_GoCAL")).setSummary("Enabled");
								}
							}
						});
					}
				});
			}
		}
		else if (view.getId() == R.id.setup_gcal_call)
		{
			if (Settings.APP_GoCAL_CALL)
			{
				final Button button = (Button) view.findViewById(R.id.calendar_setup_bt);
				button.setText("Disconnect Calendar");
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Settings.APP_GoCAL_CALL = false;
						Settings.write();
						button.setText("Connect Calendar | Calls");
						getDialog().cancel();
						((DialogExPreference) findPreferenceInHierarchy("APP_GoCAL_CALL")).setSummary("Disabled");
					}
				});
			}
			else
			{
				final Button button = (Button) view.findViewById(R.id.calendar_setup_bt);
				final ListView calendarList = (ListView) view.findViewById(R.id.calendar_setup_list);
				button.setText("Connect Calendar | Calls");
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						button.setVisibility(View.INVISIBLE);
						((TextView) view.findViewById(R.id.calendar_setup_msg)).setText(R.string.st_calendar_msg_2_2);
						final HashMap<String, Integer> calendarMap = new HashMap<String, Integer>();
						final Uri CALENDAR = (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) ? CalendarContract.CONTENT_URI : (Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) ? Uri.parse("content://com.android.calendar") : Uri.parse("content://calendar");
						Cursor c = null;
						try
						{
							if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
							{
								c = Thoth.cx.getContentResolver().query(Uri.withAppendedPath(CALENDAR, "calendars"), null, null, null, null);
								while (c != null && c.moveToNext())
								{
									if (c.getString(c.getColumnIndex(CalendarContract.CalendarEntity.ACCOUNT_TYPE)).equalsIgnoreCase("com.google") && c.getString(c.getColumnIndex(CalendarContract.CalendarEntity.ACCOUNT_NAME)).equalsIgnoreCase(Settings.USR_DEVICE_ACCOUNT_EMAIL))
										calendarMap.put(c.getString(c.getColumnIndex(CalendarContract.CalendarEntity.CALENDAR_DISPLAY_NAME)), c.getInt(c.getColumnIndex(CalendarContract.CalendarEntity._ID)));
								}
							}
							else
							{
								c = Thoth.cx.getContentResolver().query(Uri.withAppendedPath(CALENDAR, "calendars"), new String[]{"_id", "displayname", "_sync_account", "_sync_account_type"}, null, null, "displayname ASC");
								while (c != null && c.moveToNext())
								{
									if (c.getString(3).equalsIgnoreCase("com.google") && c.getString(2).equalsIgnoreCase(Settings.USR_DEVICE_ACCOUNT_EMAIL))
										calendarMap.put(c.getString(1), c.getInt(0));
								}
							}
						}
						catch (IllegalArgumentException e)
						{
							Log.e(Thoth.TAG, "Calendars not available", e);
						}
						finally
						{
							if (c != null)
								c.close();
						}
						final List<String> calendarNames = new ArrayList<String>();
						for (String calendar : calendarMap.keySet())
							calendarNames.add(calendar);
						calendarList.setVisibility(View.VISIBLE);
						calendarList.setAdapter(new ArrayAdapter<String>(Thoth.cx, android.R.layout.simple_expandable_list_item_1, calendarNames));
						calendarList.setOnItemClickListener(new OnItemClickListener()
						{
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
							{
								if (calendarMap.containsKey(calendarNames.get(pos)))
								{
									Settings.USR_DEVICE_ACCOUNT_CALENDAR_CALL = calendarMap.get(calendarNames.get(pos));
									Settings.APP_GoCAL_CALL = true;
									Settings.write();
									calendarList.setVisibility(View.GONE);
									button.setVisibility(View.VISIBLE);
									button.setText("Disconnect Calendar | Calls");
									getDialog().cancel();
									((DialogExPreference) findPreferenceInHierarchy("APP_GoCAL_CALL")).setSummary("Enabled");
								}
							}
						});
					}
				});
			}
		}
		else if (view.getId() == R.id.setup_gcal_msg)
		{
			if (Settings.APP_GoCAL_MSG)
			{
				final Button button = (Button) view.findViewById(R.id.calendar_setup_bt);
				button.setText("Disconnect Calendar");
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Settings.APP_GoCAL_MSG = false;
						Settings.write();
						button.setText("Connect Calendar | SMS - MMS");
						getDialog().cancel();
						((DialogExPreference) findPreferenceInHierarchy("APP_GoCAL_MSG")).setSummary("Disabled");
					}
				});
			}
			else
			{
				final Button button = (Button) view.findViewById(R.id.calendar_setup_bt);
				final ListView calendarList = (ListView) view.findViewById(R.id.calendar_setup_list);
				button.setText("Connect Calendar | SMS - MMS");
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						button.setVisibility(View.INVISIBLE);
						((TextView) view.findViewById(R.id.calendar_setup_msg)).setText(R.string.st_calendar_msg_3_2);
						final HashMap<String, Integer> calendarMap = new HashMap<String, Integer>();
						final Uri CALENDAR = (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) ? CalendarContract.CONTENT_URI : (Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) ? Uri.parse("content://com.android.calendar") : Uri.parse("content://calendar");
						Cursor c = null;
						try
						{
							if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
							{
								c = Thoth.cx.getContentResolver().query(Uri.withAppendedPath(CALENDAR, "calendars"), null, null, null, null);
								while (c != null && c.moveToNext())
								{
									if (c.getString(c.getColumnIndex(CalendarContract.CalendarEntity.ACCOUNT_TYPE)).equalsIgnoreCase("com.google") && c.getString(c.getColumnIndex(CalendarContract.CalendarEntity.ACCOUNT_NAME)).equalsIgnoreCase(Settings.USR_DEVICE_ACCOUNT_EMAIL))
										calendarMap.put(c.getString(c.getColumnIndex(CalendarContract.CalendarEntity.CALENDAR_DISPLAY_NAME)), c.getInt(c.getColumnIndex(CalendarContract.CalendarEntity._ID)));
								}
							}
							else
							{
								c = Thoth.cx.getContentResolver().query(Uri.withAppendedPath(CALENDAR, "calendars"), new String[]{"_id", "displayname", "_sync_account", "_sync_account_type"}, null, null, "displayname ASC");
								while (c != null && c.moveToNext())
								{
									if (c.getString(3).equalsIgnoreCase("com.google") && c.getString(2).equalsIgnoreCase(Settings.USR_DEVICE_ACCOUNT_EMAIL))
										calendarMap.put(c.getString(1), c.getInt(0));
								}
							}
						}
						catch (IllegalArgumentException e)
						{
							Log.e(Thoth.TAG, "Calendars not available", e);
						}
						finally
						{
							if (c != null)
								c.close();
						}
						final List<String> calendarNames = new ArrayList<String>();
						for (String calendar : calendarMap.keySet())
							calendarNames.add(calendar);
						calendarList.setVisibility(View.VISIBLE);
						calendarList.setAdapter(new ArrayAdapter<String>(Thoth.cx, android.R.layout.simple_expandable_list_item_1, calendarNames));
						calendarList.setOnItemClickListener(new OnItemClickListener()
						{
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
							{
								if (calendarMap.containsKey(calendarNames.get(pos)))
								{
									Settings.USR_DEVICE_ACCOUNT_CALENDAR_MSG = calendarMap.get(calendarNames.get(pos));
									Settings.APP_GoCAL_MSG = true;
									Settings.write();
									calendarList.setVisibility(View.GONE);
									button.setVisibility(View.VISIBLE);
									button.setText("Disconnect Calendar | SMS - MMS");
									getDialog().cancel();
									((DialogExPreference) findPreferenceInHierarchy("APP_GoCAL_MSG")).setSummary("Enabled");
								}
							}
						});
					}
				});
			}
		}
		else if (view.getId() == R.id.msg_recover)
		{
			((Button) view.findViewById(R.id.msg_recover_sms_in_button)).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent();
					intent.setClass(cx, MessageRecovery.class);
					intent.setAction("SMS_IN");
					cx.startActivity(intent);
				}
			});
			((Button) view.findViewById(R.id.msg_recover_sms_out_button)).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent();
					intent.setClass(cx, MessageRecovery.class);
					intent.setAction("SMS_OUT");
					cx.startActivity(intent);
				}
			});
		}
	}
}
