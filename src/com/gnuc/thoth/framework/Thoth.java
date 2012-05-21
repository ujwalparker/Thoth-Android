package com.gnuc.thoth.framework;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;

import com.gnuc.thoth.R;
import com.gnuc.thoth.app.Main;
import com.gnuc.thoth.framework.auth.ThothAuth;
import com.gnuc.thoth.framework.callbacks.ThothAccountCallback;
import com.gnuc.thoth.framework.callbacks.ThothCallback;
import com.gnuc.thoth.framework.utils.date.ThothDate;
import com.gnuc.thoth.framework.utils.date.ThothDateMatch;
import com.gnuc.thoth.service.ThothService;
import com.gnuc.thoth.service.receivers.AlternativeNotificationReceiver;
import com.gnuc.thoth.service.receivers.LogSyncReceiver;
import com.gnuc.thoth.service.receivers.EndOfDayReceiver;
import com.gnuc.thoth.service.receivers.LogUpdateReceiver;
import com.gnuc.thoth.service.receivers.StartOfDayReceiver;
import com.gnuc.thoth.service.receivers.LogBackupReceiver;
import com.gnuc.thoth.service.receivers.ReportConstructor;
import com.gnuc.thoth.service.receivers.ReportMailer;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Thoth
{
	public static final int							APP		= R.drawable.thoth;
	public static final String						TAG		= "THOTH.PREF";
	public static Context							cx			= null;
	public static boolean							timeout	= false;
	public static volatile DefaultHttpClient	client	= null;
	public static String								auth		= null;
	public static AlertDialog						ad			= null;
	public static GoogleAnalyticsTracker		TRACKER	= null;
	
	public static class Call
	{
		public static boolean	isOUTBOUND	= false;
		public static boolean	isACTIVE		= false;
		public static int			count			= 0;
	}
	public static class Settings
	{
		public static long		ALARM_INTERVAL							= AlarmManager.INTERVAL_HOUR * 6;
		public static long		ALARM_TRIGGER_AT_TIME				= System.currentTimeMillis() + ALARM_INTERVAL;
		//
		public static String		USR_DEVICE_IMEI						= "";
		public static String		USR_DEVICE_PHONE_NUMBER				= "";
		public static String		USR_DEVICE_BUILD_MODEL				= Build.VERSION.RELEASE;
		public static String		USR_DEVICE_ACCOUNT_EMAIL			= "";
		public static int			USR_DEVICE_BUILD_FIRMWARE			= Build.VERSION.SDK_INT;
		public static int			USR_DEVICE_ACCOUNT_CALENDAR		= -1;
		public static int			USR_DEVICE_ACCOUNT_CALENDAR_CALL	= -1;
		public static int			USR_DEVICE_ACCOUNT_CALENDAR_MSG	= -1;
		public static int			USR_DEVICE_ACCOUNT					= 0;
		public static int			USR_DEVICE_NWK_TYPE					= 0;
		//
		public static boolean	APP_SETUP								= true;
		public static boolean	APP_UPGRADE								= true;
		public static boolean	APP_NETWORK_CONFIGURE				= false;
		public static boolean	APP_UPDATE_PENDING					= false;
		public static boolean	APP_UPDATE_IN_PROGRESS				= false;
		public static boolean	APP_SYNC_IN_PROGRESS					= false;
		//
		public static int			APP_VERSION								= 0;
		public static int			APP_LOG_FILTER							= 0;
		//
		public static boolean	APP_BACKUP_PENDING					= false;
		public static boolean	APP_BACKUP_IN_PROGRESS				= false;
		public static boolean	APP_RESTORE_PENDING					= false;
		public static boolean	APP_RESTORE_IN_PROGRESS				= false;
		//
		public static boolean	APP_GoMAIL								= false;
		public static boolean	APP_GoCAL								= false;
		public static boolean	APP_GoCAL_CALL							= false;
		public static boolean	APP_GoCAL_MSG							= false;
		//
		public static boolean	APP_REPORT_LOGS						= true;
		//
		public static boolean	APP_REPORT_CONSTRUCTION_PENDING	= true;
		public static boolean	APP_REPORT_MAIL_PENDING				= true;
		public static boolean	APP_TRACKING_PENDING					= true;
		//
		public static boolean	APP_ENABLE_DATA_CAPTURE				= true;
		//
		public static long		APP_FIRST_RUN_DATE					= System.currentTimeMillis();
		public static long		APP_LAST_SYNC_THOTH					= APP_FIRST_RUN_DATE;
		public static long		APP_LAST_CALL_CAPTURE				= APP_FIRST_RUN_DATE;
		public static long		APP_LAST_MSGS_CAPTURE				= APP_FIRST_RUN_DATE;
		public static long		APP_LAST_MSGM_CAPTURE				= APP_FIRST_RUN_DATE;
		public static long		APP_LAST_DATA_CAPTURE				= APP_FIRST_RUN_DATE;
		//
		public static long		APP_LAST_LOG_BACKUP					= -1l;
		public static long		APP_LAST_LOG_REPORT					= -1l;
		public static long		APP_LAST_NOTIFY						= -1l;
		public static long		APP_LAST_REPORT_CONSTRUCT			= -1l;
		public static long		APP_LAST_REPORT_MAILED				= -1l;
		//
		public static String		APP_BACKUP_ID							= null;
		public static boolean	APP_SHOW_UPDATE						= true;
		
		//
		public static void read()
		{
			try
			{
				SharedPreferences pref = cx.getSharedPreferences(TAG, Activity.MODE_PRIVATE);
				//
				USR_DEVICE_NWK_TYPE = pref.getInt("USR_DEVICE_NWK_TYPE", ((TelephonyManager) cx.getSystemService(Context.TELEPHONY_SERVICE)).getPhoneType());
				USR_DEVICE_IMEI = pref.getString("USR_DEVICE_IMEI", "");
				USR_DEVICE_PHONE_NUMBER = pref.getString("USR_DEVICE_PHONE_NUMBER", "");
				USR_DEVICE_BUILD_MODEL = pref.getString("USR_DEVICE_BUILD_MODEL", USR_DEVICE_BUILD_MODEL);
				USR_DEVICE_BUILD_FIRMWARE = pref.getInt("USR_DEVICE_BUILD_FIRMWARE", USR_DEVICE_BUILD_FIRMWARE);
				USR_DEVICE_ACCOUNT = pref.getInt("USR_DEVICE_ACCOUNT", USR_DEVICE_ACCOUNT);
				USR_DEVICE_ACCOUNT_EMAIL = pref.getString("USR_DEVICE_ACCOUNT_EMAIL", USR_DEVICE_ACCOUNT_EMAIL);
				USR_DEVICE_ACCOUNT_CALENDAR = pref.getInt("USR_DEVICE_ACCOUNT_CALENDAR", USR_DEVICE_ACCOUNT_CALENDAR);
				USR_DEVICE_ACCOUNT_CALENDAR_CALL = pref.getInt("USR_DEVICE_ACCOUNT_CALENDAR_CALL", USR_DEVICE_ACCOUNT_CALENDAR_CALL);
				USR_DEVICE_ACCOUNT_CALENDAR_MSG = pref.getInt("USR_DEVICE_ACCOUNT_CALENDAR_MSG", USR_DEVICE_ACCOUNT_CALENDAR_MSG);
				APP_LOG_FILTER = pref.getInt("APP_LOF_FILTER", APP_LOG_FILTER);
				APP_SETUP = pref.getBoolean("APP_SETUP", APP_SETUP);
				APP_UPGRADE = pref.getBoolean("APP_UPGRADE", APP_UPGRADE);
				//
				APP_VERSION = pref.getInt("APP_VERSION", APP_VERSION);
				APP_NETWORK_CONFIGURE = pref.getBoolean("APP_NETWORK_CONFIGURE", APP_NETWORK_CONFIGURE);
				APP_UPDATE_PENDING = pref.getBoolean("APP_UPDATE_PENDING", APP_UPDATE_PENDING);
				APP_UPDATE_IN_PROGRESS = pref.getBoolean("APP_UPDATE_IN_PROGRESS", APP_UPDATE_IN_PROGRESS);
				APP_SYNC_IN_PROGRESS = pref.getBoolean("APP_SYNC_IN_PROGRESS", APP_SYNC_IN_PROGRESS);
				//
				APP_BACKUP_ID = pref.getString("APP_BACKUP_ID", APP_BACKUP_ID);
				APP_BACKUP_PENDING = pref.getBoolean("APP_BACKUP_PENDING", APP_BACKUP_PENDING);
				APP_BACKUP_IN_PROGRESS = pref.getBoolean("APP_BACKUP_IN_PROGRESS", APP_BACKUP_IN_PROGRESS);
				APP_RESTORE_PENDING = pref.getBoolean("APP_RESTORE_PENDING", APP_RESTORE_PENDING);
				APP_RESTORE_IN_PROGRESS = pref.getBoolean("APP_RESTORE_IN_PROGRESS", APP_RESTORE_IN_PROGRESS);
				//
				APP_GoMAIL = pref.getBoolean("APP_GoMAIL", APP_GoMAIL);
				APP_GoCAL = pref.getBoolean("APP_GoCAL", APP_GoCAL);
				APP_GoCAL_CALL = pref.getBoolean("APP_GoCAL_CALL", APP_GoCAL_CALL);
				APP_GoCAL_MSG = pref.getBoolean("APP_GoCAL_MSG", APP_GoCAL_MSG);
				//
				APP_REPORT_LOGS = pref.getBoolean("APP_REPORT_LOGS", APP_REPORT_LOGS);
				APP_REPORT_CONSTRUCTION_PENDING = pref.getBoolean("APP_REPORT_CONSTRUCTION_PENDING", APP_REPORT_CONSTRUCTION_PENDING);
				APP_REPORT_MAIL_PENDING = pref.getBoolean("APP_REPORT_MAIL_PENDING", APP_REPORT_MAIL_PENDING);
				//
				APP_ENABLE_DATA_CAPTURE = pref.getBoolean("APP_ENABLE_DATA_CAPTURE", APP_ENABLE_DATA_CAPTURE);
				//
				APP_FIRST_RUN_DATE = pref.getLong("APP_FIRST_RUN_DATE", APP_FIRST_RUN_DATE);
				APP_LAST_SYNC_THOTH = pref.getLong("APP_LAST_SYNC_THOTH", APP_LAST_SYNC_THOTH);
				APP_LAST_CALL_CAPTURE = pref.getLong("APP_LAST_CALL_CAPTURE", APP_LAST_CALL_CAPTURE);
				APP_LAST_MSGS_CAPTURE = pref.getLong("APP_LAST_MSGS_CAPTURE", APP_LAST_MSGS_CAPTURE);
				APP_LAST_MSGM_CAPTURE = pref.getLong("APP_LAST_MSGM_CAPTURE", APP_LAST_MSGM_CAPTURE);
				APP_LAST_DATA_CAPTURE = pref.getLong("APP_LAST_DATA_CAPTURE", APP_LAST_DATA_CAPTURE);
				//
				APP_LAST_LOG_BACKUP = pref.getLong("APP_LAST_LOG_BACKUP", APP_LAST_LOG_BACKUP);
				APP_LAST_LOG_REPORT = pref.getLong("APP_LAST_LOG_REPORT", APP_LAST_LOG_REPORT);
				APP_LAST_NOTIFY = pref.getLong("APP_LAST_NOTIFY", APP_LAST_NOTIFY);
				APP_LAST_REPORT_CONSTRUCT = pref.getLong("APP_LAST_REPORT_CONSTRUCT", APP_LAST_REPORT_CONSTRUCT);
				APP_LAST_REPORT_MAILED = pref.getLong("APP_LAST_REPORT_MAILED", APP_LAST_REPORT_MAILED);
				//
				APP_TRACKING_PENDING = pref.getBoolean("APP_TRACKING_PENDING", APP_TRACKING_PENDING);
				APP_SHOW_UPDATE = pref.getBoolean("APP_SHOW_UPDATE", APP_SHOW_UPDATE);
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
		}
		
		public static void write()
		{
			try
			{
				SharedPreferences.Editor prefEditor = cx.getSharedPreferences(TAG, Activity.MODE_PRIVATE).edit();
				//
				prefEditor.putString("USR_DEVICE_IMEI", USR_DEVICE_IMEI);
				prefEditor.putInt("USR_DEVICE_NWK_TYPE", USR_DEVICE_NWK_TYPE);
				prefEditor.putString("USR_DEVICE_PHONE_NUMBER", USR_DEVICE_PHONE_NUMBER);
				prefEditor.putString("USR_DEVICE_BUILD_MODEL", USR_DEVICE_BUILD_MODEL);
				prefEditor.putInt("USR_DEVICE_BUILD_FIRMWARE", USR_DEVICE_BUILD_FIRMWARE);
				prefEditor.putInt("USR_DEVICE_ACCOUNT", USR_DEVICE_ACCOUNT);
				prefEditor.putString("USR_DEVICE_ACCOUNT_EMAIL", USR_DEVICE_ACCOUNT_EMAIL);
				prefEditor.putInt("USR_DEVICE_ACCOUNT_CALENDAR", USR_DEVICE_ACCOUNT_CALENDAR);
				prefEditor.putInt("USR_DEVICE_ACCOUNT_CALENDAR_CALL", USR_DEVICE_ACCOUNT_CALENDAR_CALL);
				prefEditor.putInt("USR_DEVICE_ACCOUNT_CALENDAR_MSG", USR_DEVICE_ACCOUNT_CALENDAR_MSG);
				prefEditor.putInt("APP_LOF_FILTER", APP_LOG_FILTER);
				prefEditor.putBoolean("APP_SETUP", APP_SETUP);
				prefEditor.putBoolean("APP_UPGRADE", APP_UPGRADE);
				//
				prefEditor.putInt("APP_VERSION", APP_VERSION);
				prefEditor.putBoolean("APP_NETWORK_CONFIGURE", APP_NETWORK_CONFIGURE);
				prefEditor.putBoolean("APP_UPDATE_PENDING", APP_UPDATE_PENDING);
				//
				prefEditor.putString("APP_BACKUP_ID", APP_BACKUP_ID);
				prefEditor.putBoolean("APP_BACKUP_PENDING", APP_BACKUP_PENDING);
				prefEditor.putBoolean("APP_BACKUP_IN_PROGRESS", APP_BACKUP_IN_PROGRESS);
				prefEditor.putBoolean("APP_RESTORE_PENDING", APP_RESTORE_PENDING);
				prefEditor.putBoolean("APP_RESTORE_IN_PROGRESS", APP_RESTORE_IN_PROGRESS);
				//
				prefEditor.putBoolean("APP_GoCAL", APP_GoCAL);
				prefEditor.putBoolean("APP_GoMAIL", APP_GoMAIL);
				prefEditor.putBoolean("APP_GoCAL_CALL", APP_GoCAL_CALL);
				prefEditor.putBoolean("APP_GoCAL_MSG", APP_GoCAL_MSG);
				//
				prefEditor.putBoolean("APP_REPORT_LOGS", APP_REPORT_LOGS);
				prefEditor.putBoolean("APP_REPORT_CONSTRUCTION_PENDING", APP_REPORT_CONSTRUCTION_PENDING);
				prefEditor.putBoolean("APP_REPORT_MAIL_PENDING", APP_REPORT_MAIL_PENDING);
				//
				prefEditor.putBoolean("APP_ENABLE_DATA_CAPTURE", APP_ENABLE_DATA_CAPTURE);
				//
				prefEditor.putLong("APP_FIRST_RUN_DATE", APP_FIRST_RUN_DATE);
				prefEditor.putLong("APP_LAST_SYNC_THOTH", APP_LAST_SYNC_THOTH);
				prefEditor.putLong("APP_LAST_CALL_CAPTURE", APP_LAST_CALL_CAPTURE);
				prefEditor.putLong("APP_LAST_MSGS_CAPTURE", APP_LAST_MSGS_CAPTURE);
				prefEditor.putLong("APP_LAST_MSGM_CAPTURE", APP_LAST_MSGM_CAPTURE);
				prefEditor.putLong("APP_LAST_DATA_CAPTURE", APP_LAST_DATA_CAPTURE);
				//
				prefEditor.putLong("APP_LAST_LOG_BACKUP", APP_LAST_LOG_BACKUP);
				prefEditor.putLong("APP_LAST_LOG_REPORT", APP_LAST_LOG_REPORT);
				prefEditor.putLong("APP_LAST_NOTIFY", APP_LAST_NOTIFY);
				prefEditor.putLong("APP_LAST_REPORT_CONSTRUCT", APP_LAST_REPORT_CONSTRUCT);
				prefEditor.putLong("APP_LAST_REPORT_MAILED", APP_LAST_REPORT_MAILED);
				//
				prefEditor.putBoolean("APP_TRACKING_PENDING", APP_TRACKING_PENDING);
				prefEditor.putBoolean("APP_SHOW_UPDATE", APP_SHOW_UPDATE);
				//
				prefEditor.commit();
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
		}
	}
	public static class Data
	{
		public static boolean	RESTART									= true;
		//
		public static boolean	NEW_DAY									= false;
		public static long		CURR_DAY_ID								= System.currentTimeMillis();
		public static long		PREV_DAY_ID								= CURR_DAY_ID;
		public static long		DATA_RESET								= CURR_DAY_ID;
		public static boolean	DAILY_CORRECTION						= false;
		//
		public static boolean	MOBILE_NEW_DAY							= false;
		public static long		MOBILE_CURR_DAY_ID					= CURR_DAY_ID;
		public static long		MOBILE_PREV_DAY_ID					= CURR_DAY_ID;
		public static long		MOBILE_NWK_STATE_PREV				= MOBILE_CURR_DAY_ID;
		public static long		MOBILE_NWK_RESET						= MOBILE_CURR_DAY_ID;
		public static boolean	MOBILE_DAILY_CORRECTION				= false;
		public static long		MOBILE_DAILY_UL_CORRECTION			= 0;
		public static long		MOBILE_DAILY_DL_CORRECTION			= 0;
		public static long		MOBILE_DAILY_UL_CORRECTION_PREV	= 0;
		public static long		MOBILE_DAILY_DL_CORRECTION_PREV	= 0;
		//
		public static boolean	WIFI_NEW_DAY							= false;
		public static long		WIFI_CURR_DAY_ID						= CURR_DAY_ID;
		public static long		WIFI_PREV_DAY_ID						= CURR_DAY_ID;
		public static long		WIFI_NWK_STATE_PREV					= WIFI_CURR_DAY_ID;
		public static long		WIFI_NWK_RESET							= WIFI_CURR_DAY_ID;
		public static boolean	WIFI_DAILY_CORRECTION				= false;
		public static boolean	WIFI_INTRADAY_CORRECTION			= false;
		public static long		WIFI_DAILY_UL_CORRECTION			= 0;
		public static long		WIFI_DAILY_DL_CORRECTION			= 0;
		public static long		WIFI_DAILY_UL_CORRECTION_PREV		= 0;
		public static long		WIFI_DAILY_DL_CORRECTION_PREV		= 0;
		public static long		WIFI_INTRADAY_UL_CORRECTION		= 0;
		public static long		WIFI_INTRADAY_DL_CORRECTION		= 0;
		public static long		WIFI_INTRADAY_UL_CORRECTION_PREV	= 0;
		public static long		WIFI_INTRADAY_DL_CORRECTION_PREV	= 0;
		
		public static void read()
		{
			try
			{
				SharedPreferences pref = cx.getSharedPreferences(TAG + "_DATA", Activity.MODE_PRIVATE);
				RESTART = pref.getBoolean("RESTART", RESTART);
				//
				NEW_DAY = pref.getBoolean("NEW_DAY", NEW_DAY);
				CURR_DAY_ID = pref.getLong("CURR_DAY_ID", CURR_DAY_ID);
				PREV_DAY_ID = pref.getLong("PREV_DAY_ID", PREV_DAY_ID);
				DATA_RESET = pref.getLong("DATA_RESET", DATA_RESET);
				DAILY_CORRECTION = pref.getBoolean("DAILY_CORRECTION", DAILY_CORRECTION);
				//
				MOBILE_NEW_DAY = pref.getBoolean("MOBILE_NEW_DAY", MOBILE_NEW_DAY);
				MOBILE_CURR_DAY_ID = pref.getLong("MOBILE_CURR_DAY_ID", MOBILE_CURR_DAY_ID);
				MOBILE_PREV_DAY_ID = pref.getLong("MOBILE_PREV_DAY_ID", MOBILE_PREV_DAY_ID);
				MOBILE_NWK_STATE_PREV = pref.getLong("MOBILE_NWK_STATE_PREV", MOBILE_NWK_STATE_PREV);
				MOBILE_NWK_RESET = pref.getLong("MOBILE_NWK_RESET", MOBILE_NWK_RESET);
				MOBILE_DAILY_CORRECTION = pref.getBoolean("MOBILE_DAILY_CORRECTION", MOBILE_DAILY_CORRECTION);
				MOBILE_DAILY_UL_CORRECTION = pref.getLong("MOBILE_DAILY_UL_CORRECTION", MOBILE_DAILY_UL_CORRECTION);
				MOBILE_DAILY_DL_CORRECTION = pref.getLong("MOBILE_DAILY_DL_CORRECTION", MOBILE_DAILY_DL_CORRECTION);
				MOBILE_DAILY_UL_CORRECTION_PREV = pref.getLong("MOBILE_DAILY_UL_CORRECTION_PREV", MOBILE_DAILY_UL_CORRECTION_PREV);
				MOBILE_DAILY_DL_CORRECTION_PREV = pref.getLong("MOBILE_DAILY_DL_CORRECTION_PREV", MOBILE_DAILY_DL_CORRECTION_PREV);
				//
				WIFI_NEW_DAY = pref.getBoolean("WIFI_NEW_DAY", WIFI_NEW_DAY);
				WIFI_CURR_DAY_ID = pref.getLong("WIFI_CURR_DAY_ID", WIFI_CURR_DAY_ID);
				WIFI_PREV_DAY_ID = pref.getLong("WIFI_PREV_DAY_ID", WIFI_PREV_DAY_ID);
				WIFI_NWK_STATE_PREV = pref.getLong("WIFI_NWK_STATE_PREV", WIFI_NWK_STATE_PREV);
				WIFI_NWK_RESET = pref.getLong("WIFI_NWK_RESET", WIFI_NWK_RESET);
				WIFI_DAILY_CORRECTION = pref.getBoolean("WIFI_DAILY_CORRECTION", WIFI_DAILY_CORRECTION);
				WIFI_INTRADAY_CORRECTION = pref.getBoolean("WIFI_INTRADAY_CORRECTION", WIFI_INTRADAY_CORRECTION);
				WIFI_DAILY_UL_CORRECTION = pref.getLong("WIFI_DAILY_UL_CORRECTION", WIFI_DAILY_UL_CORRECTION);
				WIFI_DAILY_DL_CORRECTION = pref.getLong("WIFI_DAILY_DL_CORRECTION", WIFI_DAILY_DL_CORRECTION);
				WIFI_DAILY_UL_CORRECTION_PREV = pref.getLong("WIFI_DAILY_UL_CORRECTION_PREV", WIFI_DAILY_UL_CORRECTION_PREV);
				WIFI_DAILY_DL_CORRECTION_PREV = pref.getLong("WIFI_DAILY_DL_CORRECTION_PREV", WIFI_DAILY_DL_CORRECTION_PREV);
				WIFI_INTRADAY_UL_CORRECTION = pref.getLong("WIFI_INTRADAY_UL_CORRECTION", WIFI_INTRADAY_UL_CORRECTION);
				WIFI_INTRADAY_DL_CORRECTION = pref.getLong("WIFI_INTRADAY_DL_CORRECTION", WIFI_INTRADAY_DL_CORRECTION);
				WIFI_INTRADAY_UL_CORRECTION_PREV = pref.getLong("WIFI_INTRADAY_UL_CORRECTION_PREV", WIFI_INTRADAY_UL_CORRECTION_PREV);
				WIFI_INTRADAY_DL_CORRECTION_PREV = pref.getLong("WIFI_INTRADAY_DL_CORRECTION_PREV", WIFI_INTRADAY_DL_CORRECTION_PREV);
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
		}
		
		public static void write()
		{
			try
			{
				SharedPreferences.Editor prefEditor = cx.getSharedPreferences(TAG, Activity.MODE_PRIVATE).edit();
				prefEditor.putBoolean("RESTART", RESTART);
				//
				prefEditor.putBoolean("NEW_DAY", NEW_DAY);
				prefEditor.putLong("CURR_DAY_ID", CURR_DAY_ID);
				prefEditor.putLong("PREV_DAY_ID", PREV_DAY_ID);
				prefEditor.putLong("DATA_RESET", DATA_RESET);
				prefEditor.putBoolean("DAILY_CORRECTION", DAILY_CORRECTION);
				//
				prefEditor.putBoolean("MOBILE_NEW_DAY", MOBILE_NEW_DAY);
				prefEditor.putLong("MOBILE_CURR_DAY_ID", MOBILE_CURR_DAY_ID);
				prefEditor.putLong("MOBILE_PREV_DAY_ID", MOBILE_PREV_DAY_ID);
				prefEditor.putLong("MOBILE_NWK_STATE_PREV", MOBILE_NWK_STATE_PREV);
				prefEditor.putLong("MOBILE_NWK_RESET", MOBILE_NWK_RESET);
				prefEditor.putBoolean("MOBILE_DAILY_CORRECTION", MOBILE_DAILY_CORRECTION);
				prefEditor.putLong("MOBILE_DAILY_UL_CORRECTION", MOBILE_DAILY_UL_CORRECTION);
				prefEditor.putLong("MOBILE_DAILY_DL_CORRECTION", MOBILE_DAILY_DL_CORRECTION);
				prefEditor.putLong("MOBILE_DAILY_UL_CORRECTION_PREV", MOBILE_DAILY_UL_CORRECTION_PREV);
				prefEditor.putLong("MOBILE_DAILY_DL_CORRECTION_PREV", MOBILE_DAILY_DL_CORRECTION_PREV);
				//
				prefEditor.putBoolean("WIFI_NEW_DAY", WIFI_NEW_DAY);
				prefEditor.putLong("WIFI_CURR_DAY_ID", WIFI_CURR_DAY_ID);
				prefEditor.putLong("WIFI_PREV_DAY_ID", WIFI_PREV_DAY_ID);
				prefEditor.putLong("WIFI_NWK_STATE_PREV", WIFI_NWK_STATE_PREV);
				prefEditor.putLong("WIFI_NWK_RESET", WIFI_NWK_RESET);
				prefEditor.putBoolean("WIFI_DAILY_CORRECTION", WIFI_DAILY_CORRECTION);
				prefEditor.putBoolean("WIFI_INTRADAY_CORRECTION", WIFI_INTRADAY_CORRECTION);
				prefEditor.putLong("WIFI_DAILY_UL_CORRECTION", WIFI_DAILY_UL_CORRECTION);
				prefEditor.putLong("WIFI_DAILY_DL_CORRECTION", WIFI_DAILY_DL_CORRECTION);
				prefEditor.putLong("WIFI_DAILY_UL_CORRECTION_PREV", WIFI_DAILY_UL_CORRECTION_PREV);
				prefEditor.putLong("WIFI_DAILY_DL_CORRECTION_PREV", WIFI_DAILY_DL_CORRECTION_PREV);
				prefEditor.putLong("WIFI_INTRADAY_UL_CORRECTION", WIFI_INTRADAY_UL_CORRECTION);
				prefEditor.putLong("WIFI_INTRADAY_DL_CORRECTION", WIFI_INTRADAY_DL_CORRECTION);
				prefEditor.putLong("WIFI_INTRADAY_UL_CORRECTION_PREV", WIFI_INTRADAY_UL_CORRECTION_PREV);
				prefEditor.putLong("WIFI_INTRADAY_DL_CORRECTION_PREV", WIFI_INTRADAY_DL_CORRECTION_PREV);
				prefEditor.commit();
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
		}
	}
	public final class REQ
	{
		public static final String	SERVER					= "https://thoth-server.appspot.com/app";
		//
		public static final int		HELLO						= 0;
		public static final int		NOTIFY					= 1;
		//
		public static final int		LOG_UPDATE				= 111;
		//
		public static final int		USR_CHECK				= 202;
		public static final int		USR_CREATE				= 203;
		public static final int		USR_EDIT					= 204;
		//
		public static final int		LOG_CREATE				= 205;
		//
		public static final int		LOG_BACKUP				= 300;
		public static final int		LOG_RESTORE				= 301;
		public static final int		LOG_MAIL					= 302;
		public static final int		REPORT_CONSTRUCTOR	= 600;
		public static final int		REPORT_MAILER			= 601;
		public static final int		REPORT_MAIL				= 602;
		//
		public static final int		MAIL_DEBUG_LOGS		= 900;
		//
		public static final int		CALL_CAPTURE			= 400;
		public static final int		MSGS_CAPTURE			= 401;
		public static final int		MSGM_CAPTURE			= 402;
		public static final int		DATA_CAPTURE			= 403;
		public static final int		SYNC_CAPTURE			= 500;
		//
		public static final int		UPGRADE_CAPTURE		= 405;
	}
	
	public static void scheduleServices()
	{
		/**
		 * Server Update Receiver
		 */
		AlarmManager aMgr = (AlarmManager) cx.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi = PendingIntent.getBroadcast(cx, 0, new Intent(cx, LogUpdateReceiver.class), 0);
		aMgr.cancel(pi);
		aMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, Thoth.Settings.ALARM_TRIGGER_AT_TIME, Thoth.Settings.ALARM_INTERVAL, pi);
		/**
		 * Start of Day Receiver
		 */
		pi = PendingIntent.getBroadcast(cx, 0, new Intent(cx, StartOfDayReceiver.class), 0);
		aMgr.cancel(pi);
		final DateTime tommorowS = (new DateTime()).plusDays(1).minuteOfDay().withMinimumValue().plusMinutes(1);
		aMgr.setRepeating(AlarmManager.RTC_WAKEUP, tommorowS.getMillis(), AlarmManager.INTERVAL_DAY, pi);
		/**
		 * End of Day Receiver
		 */
		pi = PendingIntent.getBroadcast(cx, 0, new Intent(cx, EndOfDayReceiver.class), 0);
		aMgr.cancel(pi);
		final DateTime todayE = (new DateTime()).minuteOfDay().withMaximumValue().minusMinutes(1);
		aMgr.setRepeating(AlarmManager.RTC_WAKEUP, todayE.getMillis(), AlarmManager.INTERVAL_DAY, pi);
		/**
		 * Daily Log Sync Receiver
		 */
		pi = PendingIntent.getBroadcast(cx, 0, new Intent(cx, LogSyncReceiver.class), 0);
		aMgr.cancel(pi);
		final DateTime todayE30 = (new DateTime()).minuteOfDay().withMaximumValue().minusMinutes(30);
		aMgr.setRepeating(AlarmManager.RTC_WAKEUP, todayE30.getMillis(), AlarmManager.INTERVAL_DAY, pi);
		/**
		 * Bi-weekly Receiver
		 */
		pi = PendingIntent.getBroadcast(cx, 0, new Intent(cx, AlternativeNotificationReceiver.class), 0);
		aMgr.cancel(pi);
		aMgr.setRepeating(AlarmManager.RTC_WAKEUP, (tommorowS.plusMinutes(120)).getMillis(), AlarmManager.INTERVAL_DAY, pi);
		/**
		 * Weekly Log Backup
		 */
		pi = PendingIntent.getBroadcast(cx, 0, new Intent(cx, LogBackupReceiver.class), 0);
		aMgr.cancel(pi);
		aMgr.setRepeating(AlarmManager.RTC_WAKEUP, (tommorowS.plusMinutes(60)).getMillis(), AlarmManager.INTERVAL_DAY, pi);
		/**
		 * Weekly Log Report Constructor
		 */
		pi = PendingIntent.getBroadcast(cx, 0, new Intent(cx, ReportConstructor.class), 0);
		aMgr.cancel(pi);
		aMgr.setRepeating(AlarmManager.RTC_WAKEUP, (tommorowS.plusMinutes(10)).getMillis(), AlarmManager.INTERVAL_DAY, pi);
		/**
		 * Weekly Log Report Mailer
		 */
		pi = PendingIntent.getBroadcast(cx, 0, new Intent(cx, ReportMailer.class), 0);
		aMgr.cancel(pi);
		aMgr.setRepeating(AlarmManager.RTC_WAKEUP, (tommorowS.plusMinutes(30)).getMillis(), AlarmManager.INTERVAL_DAY, pi);
	}
	
	public static void sendToService(int action, Bundle bundle, Intent intent)
	{
		Intent i = new Intent(cx, ThothService.class);
		i.setAction(String.valueOf(action));
		if (bundle != null)
			i.putExtras(bundle);
		if (intent != null)
			i.putExtra("data", intent.getByteArrayExtra("data"));
		cx.startService(i);
	}
	
	public static void createThothAsContact(ThothCallback callback)
	{
		for (Account account : AccountManager.get(Thoth.cx).getAccountsByType("com.google"))
		{
			if (account.hashCode() == Thoth.Settings.USR_DEVICE_ACCOUNT)
			{
				String name = "Thoth Android App";
				String email1 = "thoth@gnuc.in", email2 = "thoth.1@gnuc.in", email3 = "thoth.2@gnuc.in", email4 = "thoth.3@gnuc.in", email5 = "thoth.4@gnuc.in";
				int emailType = ContactsContract.CommonDataKinds.Email.TYPE_OTHER;
				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
				ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI).withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type).withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name).build());
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name).build());
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Email.DATA, email1).withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailType).build());
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Email.DATA, email2).withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailType).build());
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Email.DATA, email3).withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailType).build());
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Email.DATA, email4).withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailType).build());
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Email.DATA, email5).withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailType).build());
				ByteArrayOutputStream os = new ByteArrayOutputStream(256);
				Bitmap bmp = BitmapFactory.decodeResource(cx.getResources(), R.drawable.thoth);
				bmp.compress(CompressFormat.PNG, 100, os);
				ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, os.toByteArray()).build());
				try
				{
					Thoth.cx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
					callback.handle(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.e(TAG, "Exception encountered while inserting contact: " + e);
					callback.handle(false);
				}
				return;
			}
		}
	}
	
	public static void clearNotifications()
	{
		((NotificationManager) cx.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(Thoth.APP + 3);
		((NotificationManager) cx.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(Thoth.APP + 2);
		((NotificationManager) cx.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(Thoth.APP);
	}
	
	public static void showLogNotification(String nText)
	{
		final NotificationManager nfm = (NotificationManager) cx.getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification nf = new Notification(R.drawable.thoths, nText, System.currentTimeMillis());
		nf.ledARGB = android.graphics.Color.CYAN;
		nf.ledOnMS = 100;
		nf.ledOffMS = 100;
		nf.flags |= Notification.FLAG_SHOW_LIGHTS;
		nf.defaults |= Notification.DEFAULT_SOUND;
		Intent notificationIntent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(cx, 0, notificationIntent, 0);
		nf.setLatestEventInfo(cx, "Logged", nText, contentIntent);
		nfm.notify(Thoth.APP, nf);
		new Handler()
		{
			public void handleMessage(final Message msg)
			{
				nfm.cancel(Thoth.APP);
			}
		}.sendEmptyMessageDelayed(Thoth.APP, 5000);
	}
	
	public static void showNwkBackupNotification()
	{
		final NotificationManager nfm = (NotificationManager) cx.getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification nf = new Notification(R.drawable.thoths, "Thoth: logs were successfully backed up.", System.currentTimeMillis());
		nf.ledARGB = android.graphics.Color.MAGENTA;
		nf.ledOnMS = 100;
		nf.ledOffMS = 100;
		nf.flags |= Notification.FLAG_SHOW_LIGHTS;
		nf.defaults |= Notification.DEFAULT_SOUND;
		RemoteViews nfv = new RemoteViews(cx.getPackageName(), R.layout.notify_nwk_backup);
		nfv.setTextViewText(R.id.no_nw_up_time, DateFormat.format("h:mm AA on dd/MM/yy", System.currentTimeMillis()));
		nf.contentView = nfv;
		Intent i = new Intent(cx, Main.class);
		nf.contentIntent = PendingIntent.getActivity(cx, 0, i, 0);
		nfm.notify(Thoth.APP + 2, nf);
	}
	
	public static void showBiWeeklyNotification()
	{
		ThothDateMatch tdm = ThothDate.filter(new DateTime(Settings.APP_LAST_NOTIFY));
		if (!tdm.THIS_WEEK && (ThothDate.now.dayOfWeek().getAsText().equalsIgnoreCase("WEDNESDAY") || ThothDate.now.dayOfWeek().getAsText().equalsIgnoreCase("SATURDAY")))
		{
			final NotificationManager nfm = (NotificationManager) cx.getSystemService(Context.NOTIFICATION_SERVICE);
			final Notification nf = new Notification(R.drawable.thoths, "Thoth: Update Pending.", System.currentTimeMillis());
			nf.ledARGB = android.graphics.Color.RED;
			nf.ledOnMS = 100;
			nf.ledOffMS = 100;
			nf.flags |= Notification.FLAG_NO_CLEAR + Notification.FLAG_SHOW_LIGHTS;
			nf.defaults |= Notification.DEFAULT_SOUND;
			RemoteViews nfv = new RemoteViews(cx.getPackageName(), R.layout.notify_update_required);
			nfv.setTextViewText(R.id.last_update_id, DateFormat.format("h:mm AA on dd/MM/yy", System.currentTimeMillis()));
			nf.contentView = nfv;
			Intent i = new Intent(cx, Main.class);
			nf.contentIntent = PendingIntent.getActivity(cx, 0, i, 0);
			nfm.notify(Thoth.APP + 2, nf);
			//
			Settings.APP_LAST_NOTIFY = ThothDate.now.getMillis();
			Settings.write();
		}
	}
	
	public static boolean isNetworkOK()
	{
		ConnectivityManager cm = (ConnectivityManager) cx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected())
			return true;
		else if (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected())
			return true;
		return false;
	}
	
	public static boolean wasNetworkSwitchedOn()
	{
		ConnectivityManager cm = (ConnectivityManager) cx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected())
			return true;
		else if (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting())
			return true;
		return false;
	}
	
	public static void isSecureOK1(final ThothAccountCallback callback, final Handler hdlr)
	{
		ThothAuth.authenticate(new ThothAccountCallback()
		{
			@Override
			public void handle(int resultCode)
			{
				// 1 : OK
				// -1 : No default account is configured
				// -2 : No account matched
				// -3 : Timed out
				callback.handle(resultCode == 1 ? 1 : resultCode == -1 ? -1 : resultCode == -2 ? -2 : resultCode == -3 ? -3 : -3);
			}
		}, hdlr);
	}
	
	public static void switchOnNetwork(int alert, final ThothCallback callback)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(cx);
		adb.setIcon(R.drawable.icon);
		adb.setTitle("Network access is disabled");
		adb.setMessage("Thoth requires a network connection to update logs.\n\nWould you like to enable WiFi?");
		adb.setCancelable(false);
		adb.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				Settings.APP_NETWORK_CONFIGURE = true;
				Settings.write();
				Intent wifiOptionsIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
				wifiOptionsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				cx.startActivity(wifiOptionsIntent);
			}
		});
		adb.setNegativeButton("Not now", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
				Settings.APP_NETWORK_CONFIGURE = false;
				Settings.APP_UPDATE_PENDING = true;
				Settings.write();
				callback.handle(false);
			}
		});
		ad = adb.create();
		ad.show();
	}
	
	public static void showErrorDialog()
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(cx);
		adb.setIcon(R.drawable.icon);
		adb.setMessage("Fatal error. Exit?");
		adb.setCancelable(false);
		adb.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				Main.getInstance().mainHandler.sendEmptyMessage(666);
			}
		});
		ad = adb.create();
		ad.show();
	}
	
	public static boolean checkDbFile()
	{
		File currentDB = new File(Environment.getDataDirectory(), "/data/com.gnuc.thoth/databases/thoth.db");
		if (currentDB.exists())
			return currentDB.delete();
		else
			return true;
	}
	
	public static void showProgressDialog(final ThothCallback callback, final boolean timeoutSkip)
	{
		final ProgressDialog pd = ProgressDialog.show(cx, "", "Waiting for network to be operational...", true, false);
		final Handler ph = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case 0 :
					{
						pd.dismiss();
						Settings.APP_NETWORK_CONFIGURE = false;
						callback.handle(true);
						break;
					}
					case -999 :
					{
						synchronized (this)
						{
							timeout = true;
						};
						break;
					}
					case -1 : // Timeout
					{
						pd.dismiss();
						callback.handle(Settings.APP_NETWORK_CONFIGURE = timeout = false);
						break;
					}
				}
			}
		};
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				new Runnable()
				{
					@Override
					public void run()
					{
						if (timeoutSkip)
							ph.sendEmptyMessageDelayed(-999, 30000);
						else
							ph.sendEmptyMessageDelayed(-999, 5000);
						while (!timeout && !isNetworkOK())
						{
							try
							{
								Thread.sleep(500);
							}
							catch (Exception e)
							{
								ThothLog.e(e);
							}
						}
						if (timeout)
						{
							ph.sendEmptyMessage(-1);
							return;
						}
						ph.sendEmptyMessage(0);
					}
				}.run();
			}
		}).start();
	}
	
	public static void runOnBackground(final Runnable r)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				r.run();
			};
		}.start();
	}
}
