package com.gnuc.thoth.framework.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.gnuc.thoth.framework.ThothLog;

public class LoggerDB
{
	/**
	 * ThothProcessor Database
	 * 
	 * Database name & Database version
	 */
	private static final String	DATABASE_NAME										= "logger.db";
	private static final int		DATABASE_VERSION									= 5;
	/**
	 ****************************** table_USERDEVICE ******************************
	 */
	public static final String		table_USERDEVICE									= "t_UserDevice";
	/* COLUMNS */
	public static final String		col_UD_PKEY											= "_id";
	//
	public static final String		col_UD_PhoneNumber								= "PhoneNumber";
	public static final String		col_UD_IMEI											= "IMEI";
	public static final String		col_UD_BuildModel									= "BuildModel";
	public static final String		col_UD_BuildFirmware								= "BuildFirmware";
	/**
	 ****************************** table_USERCONTACT ******************************
	 */
	public static final String		table_USERCONTACT									= "t_UserContact";
	/* COLUMNS */
	public static final String		col_UC_PKEY											= "_id";
	public static final String		col_UC_Name											= "contactName";
	public static final String		col_UC_Email										= "contactEmail";
	public static final String		col_UC_RefId										= "contactRefId";
	public static final String		col_UC_Count_Inbound								= "countInbound";
	public static final String		col_UC_Count_Outbound							= "countOutbound";
	public static final String		col_UC_Count_Missed								= "countMissed";
	public static final String		col_UC_Count_Rejected							= "countRejected";
	public static final String		col_UC_Count_NoAnswer							= "countNoAnswer";
	public static final String		col_UC_Duration_Inbound							= "durationInbound";
	public static final String		col_UC_Duration_Outbound						= "durationOutbound";
	public static final String		col_UC_Count_InboundSMS							= "countInboundSMS";
	public static final String		col_UC_Count_OutboundSMS						= "countOutboundSMS";
	public static final String		col_UC_Count_InboundMMS							= "countInboundMMS";
	public static final String		col_UC_Count_OutboundMMS						= "countOutboundMMS";
	/**
	 ****************************** table_USERAPPLICATION ******************************
	 */
	public static final String		table_USERAPPLICATION							= "t_UserApplication";
	/* COLUMNS */
	public static final String		col_UA_PKEY											= "_id";
	//
	public static final String		col_UA_Device										= "device";
	//
	public static final String		col_UA_Name											= "appName";
	public static final String		col_UA_Package										= "appPackage";
	public static final String		col_UA_Total_Uploaded							= "totalUploaded";
	public static final String		col_UA_Total_Downloaded							= "totalDownloaded";
	/**
	 ****************************** table_LOGCALL ******************************
	 */
	public static final String		table_LOGCALL										= "t_LogCall";
	/* COLUMNS */
	public static final String		col_LC_PKEY											= "_id";
	//
	public static final String		col_LC_Contact										= "contact";
	// 0-INBOUND, 1-OUTBOUND, 2-REJECTED, 3-MISSED, 4-NOANSWER
	public static final String		col_LC_Type											= "type";
	public static final String		col_LC_Time											= "time";
	public static final String		col_LC_Timezone									= "timezone";
	public static final String		col_LC_Duration									= "duration";
	/**
	 ****************************** table_LOGMSGS ******************************
	 */
	public static final String		table_LOGMSGS										= "t_LogMsgs";
	/* COLUMNS */
	public static final String		col_LM_PKEY											= "_id";
	//
	public static final String		col_LM_Contact										= "contact";
	// 0-IN SMS, 1-OUT SMS, 2-IN MMS, 3-OUT MMS
	public static final String		col_LM_Type											= "type";
	public static final String		col_LM_Time											= "time";
	public static final String		col_LM_Timezone									= "timezone";
	public static final String		col_LM_Body											= "body";
	/**
	 ****************************** table_LOGAPPS ******************************
	 */
	public static final String		table_LOGAPPS										= "t_LogApps";
	/* COLUMNS */
	public static final String		col_LA_PKEY											= "_id";
	//
	public static final String		col_LA_Application								= "application";
	public static final String		col_LA_Time											= "time";
	public static final String		col_LA_Uploaded									= "uploaded";
	public static final String		col_LA_Downloaded									= "downloaded";
	/**
	 ****************************** table_LOGDATAWIFI ******************************
	 */
	public static final String		table_LOGDATAWIFI									= "t_LogDataWifi";
	/* COLUMNS */
	public static final String		col_LDW_PKEY										= "_id";
	//
	public static final String		col_LDW_Device										= "device";
	public static final String		col_LDW_Time										= "time";
	public static final String		col_LDW_Uploaded									= "uploaded";
	public static final String		col_LDW_Downloaded								= "downloaded";
	/**
	 ****************************** table_LOGDATAMOBILE******************************
	 */
	public static final String		table_LOGDATAMOBILE								= "t_LogDataMobile";
	/* COLUMNS */
	public static final String		col_LDM_PKEY										= "_id";
	//
	public static final String		col_LDM_Device										= "device";
	public static final String		col_LDM_Time										= "time";
	public static final String		col_LDM_Uploaded									= "uploaded";
	public static final String		col_LDM_Downloaded								= "downloaded";
	/**
	 * Table create scripts
	 */
	private static final String	CREATE_table_USERDEVICE							= "CREATE TABLE " + table_USERDEVICE + " (" + col_UD_PKEY + " TEXT PRIMARY KEY," + col_UD_PhoneNumber + " TEXT," + col_UD_IMEI + " TEXT," + col_UD_BuildModel + " TEXT," + col_UD_BuildFirmware + " TEXT);";
	private static final String	CREATE_table_USERCONTACT						= "CREATE TABLE " + table_USERCONTACT + " (" + col_UC_PKEY + " TEXT PRIMARY KEY," + col_UC_Name + " TEXT," + col_UC_Email + " TEXT," + col_UC_RefId + " TEXT," + col_UC_Count_Inbound + " INTEGER," + col_UC_Count_Outbound + " INTEGER," + col_UC_Count_Missed + " INTEGER," + col_UC_Count_Rejected + " INTEGER," + col_UC_Count_NoAnswer + " INTEGER," + col_UC_Duration_Inbound + " INTEGER," + col_UC_Duration_Outbound + " INTEGER," + col_UC_Count_InboundSMS + " INTEGER," + col_UC_Count_OutboundSMS + " INTEGER," + col_UC_Count_InboundMMS + " INTEGER," + col_UC_Count_OutboundMMS + " INTEGER);";
	private static final String	CREATE_table_USERAPPLICATION					= "CREATE TABLE " + table_USERAPPLICATION + " (" + col_UA_PKEY + " TEXT PRIMARY KEY," + col_UA_Device + " TEXT REFERENCES " + table_USERDEVICE + "(" + col_UD_PKEY + ")," + col_UA_Name + " TEXT," + col_UA_Package + " TEXT," + col_UA_Total_Uploaded + " INTEGER," + col_UA_Total_Downloaded + " INTEGER);";
	private static final String	CREATE_table_LOGCALL								= "CREATE TABLE " + table_LOGCALL + " (" + col_LC_PKEY + " TEXT PRIMARY KEY," + col_LC_Contact + " TEXT REFERENCES " + table_USERCONTACT + "(" + col_UC_PKEY + ")," + col_LC_Type + " TEXT," + col_LC_Time + " TEXT," + col_LC_Timezone + " INTEGER," + col_LC_Duration + " INTEGER);";
	private static final String	CREATE_table_LOGMSGS								= "CREATE TABLE " + table_LOGMSGS + " (" + col_LM_PKEY + " TEXT PRIMARY KEY," + col_LM_Contact + " TEXT REFERENCES " + table_USERCONTACT + "(" + col_UC_PKEY + ")," + col_LM_Type + " TEXT," + col_LM_Time + " TEXT," + col_LM_Timezone + " INTEGER," + col_LM_Body + " TEXT);";
	private static final String	CREATE_table_LOGAPPS								= "CREATE TABLE " + table_LOGAPPS + " (" + col_LA_PKEY + " TEXT PRIMARY KEY," + col_LA_Application + " TEXT REFERENCES " + table_USERAPPLICATION + "(" + col_UA_PKEY + ")," + col_LA_Time + " TEXT," + col_LA_Uploaded + " INTEGER," + col_LA_Downloaded + " INTEGER);";
	private static final String	CREATE_table_LOGDATAWIFI						= "CREATE TABLE " + table_LOGDATAWIFI + " (" + col_LDW_PKEY + " TEXT PRIMARY KEY," + col_LDW_Device + " TEXT REFERENCES " + table_USERDEVICE + "(" + col_UD_PKEY + ")," + col_LDW_Time + " TEXT," + col_LDW_Uploaded + " INTEGER," + col_LDW_Downloaded + " INTEGER);";
	private static final String	CREATE_table_LOGDATAMOBILE						= "CREATE TABLE " + table_LOGDATAMOBILE + " (" + col_LDM_PKEY + " TEXT PRIMARY KEY," + col_LDM_Device + " TEXT REFERENCES " + table_USERDEVICE + "(" + col_UD_PKEY + ")," + col_LDM_Time + " TEXT," + col_LDM_Uploaded + " INTEGER," + col_LDM_Downloaded + " INTEGER);";
	/**
	 * Table clear scripts
	 */
	public static final String		CLEAR_table_USERDEVICE							= "DELETE FROM " + table_USERDEVICE + ";";
	public static final String		CLEAR_table_USERCONTACT							= "DELETE FROM " + table_USERCONTACT + ";";
	public static final String		CLEAR_table_USERAPPLICATION					= "DELETE FROM " + table_USERAPPLICATION + ";";
	public static final String		CLEAR_table_LOGCALL								= "DELETE FROM " + table_LOGCALL + ";";
	public static final String		CLEAR_table_LOGMSGS								= "DELETE FROM " + table_LOGMSGS + ";";
	public static final String		CLEAR_table_LOGAPPS								= "DELETE FROM " + table_LOGAPPS + ";";
	public static final String		CLEAR_table_LOGDATAWIFI							= "DELETE FROM " + table_USERDEVICE + ";";
	public static final String		CLEAR_table_LOGDATAMOBILE						= "DELETE FROM " + table_LOGDATAMOBILE + ";";
	/**
	 * Table drop scripts
	 */
	// private static final String DROP_table_USERDEVICE = "DROP TABLE IF EXISTS " + table_USERDEVICE
	// + ";";
	// private static final String DROP_table_USERCONTACT = "DROP TABLE IF EXISTS " +
	// table_USERCONTACT + ";";
	// private static final String DROP_table_USERAPPLICATION = "DROP TABLE IF EXISTS " +
	// table_USERAPPLICATION + ";";
	// private static final String DROP_table_LOGCALL = "DROP TABLE IF EXISTS " + table_LOGCALL +
	// ";";
	// private static final String DROP_table_LOGMSGS = "DROP TABLE IF EXISTS " + table_LOGMSGS +
	// ";";
	// private static final String DROP_table_LOGAPPS = "DROP TABLE IF EXISTS " + table_LOGAPPS +
	// ";";
	// private static final String DROP_table_LOGDATAWIFI = "DROP TABLE IF EXISTS " +
	// table_LOGDATAWIFI + ";";
	// private static final String DROP_table_LOGDATAMOBILE = "DROP TABLE IF EXISTS " +
	// table_LOGDATAMOBILE + ";";
	/**
	 * View create scripts
	 */
	private static final String	CREATE_view_LOGCALL_THISYEAR					= "CREATE VIEW t_LogCall_THISYEAR AS SELECT * FROM " + table_LOGCALL + " T WHERE ( (strftime('%Y',datetime('now', 'localtime'))) == (strftime('%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime'))) )==1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGMSGS_THISYEAR					= "CREATE VIEW t_LogMsgs_THISYEAR AS SELECT * FROM " + table_LOGMSGS + " T WHERE ( (strftime('%Y',datetime('now', 'localtime'))) == (strftime('%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime'))) )==1 ORDER BY T." + col_LM_Time + " ASC;";
	private static final String	CREATE_view_LOGDATAWIFI_THISYEAR				= "CREATE VIEW t_LogDataWifi_THISYEAR AS SELECT * FROM " + table_LOGDATAWIFI + " T WHERE ( (strftime('%Y',datetime('now', 'localtime'))) == (strftime('%Y',datetime((T." + col_LDW_Time + "/1000), 'unixepoch', 'localtime'))) )==1 ORDER BY T." + col_LDW_Time + " ASC;";
	private static final String	CREATE_view_LOGDATAMOBILE_THISYEAR			= "CREATE VIEW t_LogDataMobile_THISYEAR AS SELECT * FROM " + table_LOGDATAMOBILE + " T WHERE ( (strftime('%Y',datetime('now', 'localtime'))) == (strftime('%Y',datetime((T." + col_LDM_Time + "/1000), 'unixepoch', 'localtime'))) )==1 ORDER BY T." + col_LDM_Time + " ASC;";
	private static final String	CREATE_view_LOGAPPS_THISYEAR					= "CREATE VIEW t_LogApps_THISYEAR AS SELECT * FROM " + table_LOGAPPS + " T WHERE ( (strftime('%Y',datetime('now', 'localtime'))) == (strftime('%Y',datetime((T." + col_LA_Time + "/1000), 'unixepoch', 'localtime'))) )==1 ORDER BY T." + col_LA_Time + " ASC;";
	/**
	 ****************************** LOGCALL ******************************
	 */
	//
	private static final String	CREATE_view_LOGCALL_LASTMONTH_IN				= "CREATE VIEW t_LogCall_LASTMONTH_IN AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==0 ) & ( ( ( strftime('%Y',datetime('now', 'localtime')) == strftime('%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%m',datetime('now', 'localtime'))-1 ) == round ( strftime('%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( ( round ( strftime('%m',datetime('now', 'localtime')) ) == 1 ) & ( round ( strftime('%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) == 12 ) ) ) )==1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_MIS			= "CREATE VIEW t_LogCall_LASTMONTH_MIS AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==3 ) & ( ( ( strftime('%Y',datetime('now', 'localtime')) == strftime('%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%m',datetime('now', 'localtime'))-1 ) == round ( strftime('%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( ( round ( strftime('%m',datetime('now', 'localtime')) ) == 1 ) & ( round ( strftime('%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) == 12 ) ) ) )==1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_NOA			= "CREATE VIEW t_LogCall_LASTMONTH_NOA AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==4 ) & ( ( ( strftime('%Y',datetime('now', 'localtime')) == strftime('%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%m',datetime('now', 'localtime'))-1 ) == round ( strftime('%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( ( round ( strftime('%m',datetime('now', 'localtime')) ) == 1 ) & ( round ( strftime('%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) == 12 ) ) ) )==1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_OUT			= "CREATE VIEW t_LogCall_LASTMONTH_OUT AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==1 ) & ( ( ( strftime('%Y',datetime('now', 'localtime')) == strftime('%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%m',datetime('now', 'localtime'))-1 ) == round ( strftime('%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( ( round ( strftime('%m',datetime('now', 'localtime')) ) == 1 ) & ( round ( strftime('%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) == 12 ) ) ) )==1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_REJ			= "CREATE VIEW t_LogCall_LASTMONTH_REJ AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==2 ) & ( ( ( strftime('%Y',datetime('now', 'localtime')) == strftime('%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%m',datetime('now', 'localtime'))-1 ) == round ( strftime('%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( ( round ( strftime('%m',datetime('now', 'localtime')) ) == 1 ) & ( round ( strftime('%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) == 12 ) ) ) )==1 ORDER BY T." + col_LC_Time + " ASC;";
	//
	private static final String	CREATE_view_LOGCALL_LASTWEEK_IN				= "CREATE VIEW t_LogCall_LASTWEEK_IN AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==0 ) & ( ( ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%m-%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%W%m',datetime('now', 'localtime')) )==11 & round ( strftime('%W%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )==5212 ) ) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_MIS				= "CREATE VIEW t_LogCall_LASTWEEK_MIS AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==3 ) & ( ( ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%m-%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%W%m',datetime('now', 'localtime')) )==11 & round ( strftime('%W%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )==5212 ) ) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_NOA				= "CREATE VIEW t_LogCall_LASTWEEK_NOA AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==4 ) & ( ( ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%m-%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%W%m',datetime('now', 'localtime')) )==11 & round ( strftime('%W%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )==5212 ) ) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_OUT				= "CREATE VIEW t_LogCall_LASTWEEK_OUT AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==1 ) & ( ( ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%m-%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%W%m',datetime('now', 'localtime')) )==11 & round ( strftime('%W%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )==5212 ) ) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_REJ				= "CREATE VIEW t_LogCall_LASTWEEK_REJ AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==2 ) & ( ( ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%m-%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%W%m',datetime('now', 'localtime')) )==11 & round ( strftime('%W%m',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )==5212 ) ) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	//
	private static final String	CREATE_view_LOGCALL_THISMONTH_IN				= "CREATE VIEW t_LogCall_THISMONTH_IN AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==0 ) & ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_THISMONTH_MIS			= "CREATE VIEW t_LogCall_THISMONTH_MIS AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==3 ) & ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_THISMONTH_NOA			= "CREATE VIEW t_LogCall_THISMONTH_NOA AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==4 ) & ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_THISMONTH_OUT			= "CREATE VIEW t_LogCall_THISMONTH_OUT AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==1 ) & ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_THISMONTH_REJ			= "CREATE VIEW t_LogCall_THISMONTH_REJ AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==2 ) & ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	//
	private static final String	CREATE_view_LOGCALL_THISWEEK_IN				= "CREATE VIEW t_LogCall_THISWEEK_IN AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==0 ) & ( strftime('%W-%m-%Y',datetime('now', 'localtime')) == strftime('%W-%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_THISWEEK_MIS				= "CREATE VIEW t_LogCall_THISWEEK_MIS AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==3 ) & ( strftime('%W-%m-%Y',datetime('now', 'localtime')) == strftime('%W-%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_THISWEEK_NOA				= "CREATE VIEW t_LogCall_THISWEEK_NOA AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==4 ) & ( strftime('%W-%m-%Y',datetime('now', 'localtime')) == strftime('%W-%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_THISWEEK_OUT				= "CREATE VIEW t_LogCall_THISWEEK_OUT AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==1 ) & ( strftime('%W-%m-%Y',datetime('now', 'localtime')) == strftime('%W-%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_THISWEEK_REJ				= "CREATE VIEW t_LogCall_THISWEEK_REJ AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==2 ) & ( strftime('%W-%m-%Y',datetime('now', 'localtime')) == strftime('%W-%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	//
	private static final String	CREATE_view_LOGCALL_TODAY_IN					= "CREATE VIEW t_LogCall_TODAY_IN AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==0 ) & ( strftime('%d-%m-%Y',datetime('now', 'localtime')) == strftime('%d-%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_TODAY_MIS					= "CREATE VIEW t_LogCall_TODAY_MIS AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==3 ) & ( strftime('%d-%m-%Y',datetime('now', 'localtime')) == strftime('%d-%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_TODAY_NOA					= "CREATE VIEW t_LogCall_TODAY_NOA AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==4 ) & ( strftime('%d-%m-%Y',datetime('now', 'localtime')) == strftime('%d-%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_TODAY_OUT					= "CREATE VIEW t_LogCall_TODAY_OUT AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==1 ) & ( strftime('%d-%m-%Y',datetime('now', 'localtime')) == strftime('%d-%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	private static final String	CREATE_view_LOGCALL_TODAY_REJ					= "CREATE VIEW t_LogCall_TODAY_REJ AS SELECT * FROM " + table_LOGCALL + " T WHERE ( T." + col_LC_Type + "==2 ) & ( strftime('%d-%m-%Y',datetime('now', 'localtime')) == strftime('%d-%m-%Y',datetime((T." + col_LC_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LC_Time + " ASC;";
	//
	private static final String	CREATE_view_LOGCALL_LASTMONTH_TOPIN			= "CREATE VIEW t_LogCall_LASTMONTH_TopIN AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_LASTMONTH_IN WHERE " + col_LC_Type + "='0' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_TOPMIS		= "CREATE VIEW t_LogCall_LASTMONTH_TopMIS AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_LASTMONTH_MIS WHERE " + col_LC_Type + "='3' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_TOPNOA		= "CREATE VIEW t_LogCall_LASTMONTH_TopNOA AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_LASTMONTH_NOA WHERE " + col_LC_Type + "='4' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_TOPOUT		= "CREATE VIEW t_LogCall_LASTMONTH_TopOUT AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_LASTMONTH_OUT WHERE " + col_LC_Type + "='1' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_TOPREJ		= "CREATE VIEW t_LogCall_LASTMONTH_TopREJ AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_LASTMONTH_REJ WHERE " + col_LC_Type + "='2' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_TOTALIN		= "CREATE VIEW t_LogCall_LASTMONTH_TotalIN AS SELECT COUNT(*) AS COUNT FROM t_LogCall_LASTMONTH_IN WHERE " + col_LC_Type + "='0';";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_TOTALMIS		= "CREATE VIEW t_LogCall_LASTMONTH_TotalMIS AS SELECT COUNT(*) AS COUNT FROM t_LogCall_LASTMONTH_MIS WHERE " + col_LC_Type + "='3';";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_TOTALNOA		= "CREATE VIEW t_LogCall_LASTMONTH_TotalNOA AS SELECT COUNT(*) AS COUNT FROM t_LogCall_LASTMONTH_NOA WHERE " + col_LC_Type + "='4';";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_TOTALOUT		= "CREATE VIEW t_LogCall_LASTMONTH_TotalOUT AS SELECT COUNT(*) AS COUNT FROM t_LogCall_LASTMONTH_OUT WHERE " + col_LC_Type + "='1';";
	private static final String	CREATE_view_LOGCALL_LASTMONTH_TOTALREJ		= "CREATE VIEW t_LogCall_LASTMONTH_TotalREJ AS SELECT COUNT(*) AS COUNT FROM t_LogCall_LASTMONTH_REJ WHERE " + col_LC_Type + "='2';";
	//
	private static final String	CREATE_view_LOGCALL_LASTWEEK_TOPIN			= "CREATE VIEW t_LogCall_LASTWEEK_TopIN AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_LASTWEEK_IN WHERE " + col_LC_Type + "='0' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_TOPMIS			= "CREATE VIEW t_LogCall_LASTWEEK_TopMIS AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_LASTWEEK_MIS WHERE " + col_LC_Type + "='3' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_TOPNOA			= "CREATE VIEW t_LogCall_LASTWEEK_TopNOA AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_LASTWEEK_NOA WHERE " + col_LC_Type + "='4' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_TOPOUT			= "CREATE VIEW t_LogCall_LASTWEEK_TopOUT AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_LASTWEEK_OUT WHERE " + col_LC_Type + "='1' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_TOPREJ			= "CREATE VIEW t_LogCall_LASTWEEK_TopREJ AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_LASTWEEK_REJ WHERE " + col_LC_Type + "='2' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_TOTALIN		= "CREATE VIEW t_LogCall_LASTWEEK_TotalIN AS SELECT COUNT(*) AS COUNT FROM t_LogCall_LASTWEEK_IN WHERE " + col_LC_Type + "='0';";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_TOTALMIS		= "CREATE VIEW t_LogCall_LASTWEEK_TotalMIS AS SELECT COUNT(*) AS COUNT FROM t_LogCall_LASTWEEK_MIS WHERE " + col_LC_Type + "='3';";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_TOTALNOA		= "CREATE VIEW t_LogCall_LASTWEEK_TotalNOA AS SELECT COUNT(*) AS COUNT FROM t_LogCall_LASTWEEK_NOA WHERE " + col_LC_Type + "='4';";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_TOTALOUT		= "CREATE VIEW t_LogCall_LASTWEEK_TotalOUT AS SELECT COUNT(*) AS COUNT FROM t_LogCall_LASTWEEK_OUT WHERE " + col_LC_Type + "='1';";
	private static final String	CREATE_view_LOGCALL_LASTWEEK_TOTALREJ		= "CREATE VIEW t_LogCall_LASTWEEK_TotalREJ AS SELECT COUNT(*) AS COUNT FROM t_LogCall_LASTWEEK_REJ WHERE " + col_LC_Type + "='2';";
	//
	private static final String	CREATE_view_LOGCALL_THISMONTH_TOPIN			= "CREATE VIEW t_LogCall_THISMONTH_TopIN AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_THISMONTH_IN WHERE " + col_LC_Type + "='0' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_THISMONTH_TOPMIS		= "CREATE VIEW t_LogCall_THISMONTH_TopMIS AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_THISMONTH_MIS WHERE " + col_LC_Type + "='3' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_THISMONTH_TOPNOA		= "CREATE VIEW t_LogCall_THISMONTH_TopNOA AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_THISMONTH_NOA WHERE " + col_LC_Type + "='4' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_THISMONTH_TOPOUT		= "CREATE VIEW t_LogCall_THISMONTH_TopOUT AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_THISMONTH_OUT WHERE " + col_LC_Type + "='1' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_THISMONTH_TOPREJ		= "CREATE VIEW t_LogCall_THISMONTH_TopREJ AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_THISMONTH_REJ WHERE " + col_LC_Type + "='2' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_THISMONTH_TOTALIN		= "CREATE VIEW t_LogCall_THISMONTH_TotalIN AS SELECT COUNT(*) AS COUNT FROM t_LogCall_THISMONTH_IN WHERE " + col_LC_Type + "='0';";
	private static final String	CREATE_view_LOGCALL_THISMONTH_TOTALMIS		= "CREATE VIEW t_LogCall_THISMONTH_TotalMIS AS SELECT COUNT(*) AS COUNT FROM t_LogCall_THISMONTH_MIS WHERE " + col_LC_Type + "='3';";
	private static final String	CREATE_view_LOGCALL_THISMONTH_TOTALNOA		= "CREATE VIEW t_LogCall_THISMONTH_TotalNOA AS SELECT COUNT(*) AS COUNT FROM t_LogCall_THISMONTH_NOA WHERE " + col_LC_Type + "='4';";
	private static final String	CREATE_view_LOGCALL_THISMONTH_TOTALOUT		= "CREATE VIEW t_LogCall_THISMONTH_TotalOUT AS SELECT COUNT(*) AS COUNT FROM t_LogCall_THISMONTH_OUT WHERE " + col_LC_Type + "='1';";
	private static final String	CREATE_view_LOGCALL_THISMONTH_TOTALREJ		= "CREATE VIEW t_LogCall_THISMONTH_TotalREJ AS SELECT COUNT(*) AS COUNT FROM t_LogCall_THISMONTH_REJ WHERE " + col_LC_Type + "='2';";
	//
	private static final String	CREATE_view_LOGCALL_THISWEEK_TOPIN			= "CREATE VIEW t_LogCall_THISWEEK_TopIN AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_THISWEEK_IN WHERE " + col_LC_Type + "='0' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_THISWEEK_TOPMIS			= "CREATE VIEW t_LogCall_THISWEEK_TopMIS AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_THISWEEK_MIS WHERE " + col_LC_Type + "='3' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_THISWEEK_TOPNOA			= "CREATE VIEW t_LogCall_THISWEEK_TopNOA AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_THISWEEK_NOA WHERE " + col_LC_Type + "='4' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_THISWEEK_TOPOUT			= "CREATE VIEW t_LogCall_THISWEEK_TopOUT AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_THISWEEK_OUT WHERE " + col_LC_Type + "='1' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_THISWEEK_TOPREJ			= "CREATE VIEW t_LogCall_THISWEEK_TopREJ AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_THISWEEK_REJ WHERE " + col_LC_Type + "='2' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_THISWEEK_TOTALIN		= "CREATE VIEW t_LogCall_THISWEEK_TotalIN AS SELECT COUNT(*) AS COUNT FROM t_LogCall_THISWEEK_IN WHERE " + col_LC_Type + "='0';";
	private static final String	CREATE_view_LOGCALL_THISWEEK_TOTALMIS		= "CREATE VIEW t_LogCall_THISWEEK_TotalMIS AS SELECT COUNT(*) AS COUNT FROM t_LogCall_THISWEEK_MIS WHERE " + col_LC_Type + "='3';";
	private static final String	CREATE_view_LOGCALL_THISWEEK_TOTALNOA		= "CREATE VIEW t_LogCall_THISWEEK_TotalNOA AS SELECT COUNT(*) AS COUNT FROM t_LogCall_THISWEEK_NOA WHERE " + col_LC_Type + "='4';";
	private static final String	CREATE_view_LOGCALL_THISWEEK_TOTALOUT		= "CREATE VIEW t_LogCall_THISWEEK_TotalOUT AS SELECT COUNT(*) AS COUNT FROM t_LogCall_THISWEEK_OUT WHERE " + col_LC_Type + "='1';";
	private static final String	CREATE_view_LOGCALL_THISWEEK_TOTALREJ		= "CREATE VIEW t_LogCall_THISWEEK_TotalREJ AS SELECT COUNT(*) AS COUNT FROM t_LogCall_THISWEEK_REJ WHERE " + col_LC_Type + "='2';";
	//
	private static final String	CREATE_view_LOGCALL_TODAY_TOPIN				= "CREATE VIEW t_LogCall_TODAY_TopIN AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_TODAY_IN WHERE " + col_LC_Type + "='0' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_TODAY_TOPMIS				= "CREATE VIEW t_LogCall_TODAY_TopMIS AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_TODAY_MIS WHERE " + col_LC_Type + "='3' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_TODAY_TOPNOA				= "CREATE VIEW t_LogCall_TODAY_TopNOA AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_TODAY_NOA WHERE " + col_LC_Type + "='4' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_TODAY_TOPOUT				= "CREATE VIEW t_LogCall_TODAY_TopOUT AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_TODAY_OUT WHERE " + col_LC_Type + "='1' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_TODAY_TOPREJ				= "CREATE VIEW t_LogCall_TODAY_TopREJ AS SELECT COUNT(*) AS COUNT," + col_LC_Contact + " CONTACT, SUM(" + col_LC_Duration + ") DURATION FROM t_LogCall_TODAY_REJ WHERE " + col_LC_Type + "='2' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LC_Time + " DESC;";
	private static final String	CREATE_view_LOGCALL_TODAY_TOTALIN			= "CREATE VIEW t_LogCall_TODAY_TotalIN AS SELECT COUNT(*) AS COUNT FROM t_LogCall_TODAY_IN WHERE " + col_LC_Type + "='0';";
	private static final String	CREATE_view_LOGCALL_TODAY_TOTALMIS			= "CREATE VIEW t_LogCall_TODAY_TotalMIS AS SELECT COUNT(*) AS COUNT FROM t_LogCall_TODAY_MIS WHERE " + col_LC_Type + "='3';";
	private static final String	CREATE_view_LOGCALL_TODAY_TOTALNOA			= "CREATE VIEW t_LogCall_TODAY_TotalNOA AS SELECT COUNT(*) AS COUNT FROM t_LogCall_TODAY_NOA WHERE " + col_LC_Type + "='4';";
	private static final String	CREATE_view_LOGCALL_TODAY_TOTALOUT			= "CREATE VIEW t_LogCall_TODAY_TotalOUT AS SELECT COUNT(*) AS COUNT FROM t_LogCall_TODAY_OUT WHERE " + col_LC_Type + "='1';";
	private static final String	CREATE_view_LOGCALL_TODAY_TOTALREJ			= "CREATE VIEW t_LogCall_TODAY_TotalREJ AS SELECT COUNT(*) AS COUNT FROM t_LogCall_TODAY_REJ WHERE " + col_LC_Type + "='2';";
	/**
	 ****************************** LOGMSGS ******************************
	 */
	private static final String	CREATE_view_LOGMSGS_LASTMONTH_MMS			= "CREATE VIEW t_LogMsgs_LASTMONTH_MMS AS SELECT * FROM " + table_LOGMSGS + " T WHERE ( T." + col_LM_Type + "==2 | T." + col_LM_Type + "==3 ) & ( ( ( strftime('%Y',datetime('now', 'localtime')) == strftime('%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%m',datetime('now', 'localtime'))-1 ) == round ( strftime('%m',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( ( round ( strftime('%m',datetime('now', 'localtime')) ) == 1 ) & ( round ( strftime('%m',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) == 12 ) ) ) )==1 ORDER BY T." + col_LM_Time + " ASC;";
	private static final String	CREATE_view_LOGMSGS_LASTMONTH_SMS			= "CREATE VIEW t_LogMsgs_LASTMONTH_SMS AS SELECT * FROM " + table_LOGMSGS + " T WHERE ( T." + col_LM_Type + "==0 | T." + col_LM_Type + "==1 ) & ( ( ( strftime('%Y',datetime('now', 'localtime')) == strftime('%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%m',datetime('now', 'localtime'))-1 ) == round ( strftime('%m',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( ( round ( strftime('%m',datetime('now', 'localtime')) ) == 1 ) & ( round ( strftime('%m',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) == 12 ) ) ) )==1 ORDER BY T." + col_LM_Time + " ASC;";
	private static final String	CREATE_view_LOGMSGS_LASTWEEK_MMS				= "CREATE VIEW t_LogMsgs_LASTWEEK_MMS AS SELECT * FROM " + table_LOGMSGS + " T WHERE ( T." + col_LM_Type + "==2 | T." + col_LM_Type + "==3 ) & ( ( ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%m-%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%m-%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%W%m',datetime('now', 'localtime')) )==11 & round ( strftime('%W%m',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) )==5212 ) ) )=1 ORDER BY T." + col_LM_Time + " ASC;";
	private static final String	CREATE_view_LOGMSGS_LASTWEEK_SMS				= "CREATE VIEW t_LogMsgs_LASTWEEK_SMS AS SELECT * FROM " + table_LOGMSGS + " T WHERE ( T." + col_LM_Type + "==0 | T." + col_LM_Type + "==1 ) & ( ( ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%m-%Y',datetime('now', 'localtime'))-1 ) == round ( strftime('%m-%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) ) & ( round ( strftime('%W',datetime('now', 'localtime'))-1 ) == round ( strftime('%W',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) ) ) ) | ( ( round ( strftime('%W%m',datetime('now', 'localtime')) )==11 & round ( strftime('%W%m',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) )==5212 ) ) )=1 ORDER BY T." + col_LM_Time + " ASC;";
	private static final String	CREATE_view_LOGMSGS_THISMONTH_MMS			= "CREATE VIEW t_LogMsgs_THISMONTH_MMS AS SELECT * FROM " + table_LOGMSGS + " T WHERE ( T." + col_LM_Type + "==2 | T." + col_LM_Type + "==3 ) & ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LM_Time + " ASC;";
	private static final String	CREATE_view_LOGMSGS_THISMONTH_SMS			= "CREATE VIEW t_LogMsgs_THISMONTH_SMS AS SELECT * FROM " + table_LOGMSGS + " T WHERE ( T." + col_LM_Type + "==0 | T." + col_LM_Type + "==1 ) & ( strftime('%m-%Y',datetime('now', 'localtime')) == strftime('%m-%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LM_Time + " ASC;";
	private static final String	CREATE_view_LOGMSGS_THISWEEK_MMS				= "CREATE VIEW t_LogMsgs_THISWEEK_MMS AS SELECT * FROM " + table_LOGMSGS + " T WHERE ( T." + col_LM_Type + "==2 | T." + col_LM_Type + "==3 ) & ( strftime('%W-%m-%Y',datetime('now', 'localtime')) == strftime('%W-%m-%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LM_Time + " ASC;";
	private static final String	CREATE_view_LOGMSGS_THISWEEK_SMS				= "CREATE VIEW t_LogMsgs_THISWEEK_SMS AS SELECT * FROM " + table_LOGMSGS + " T WHERE ( T." + col_LM_Type + "==0 | T." + col_LM_Type + "==1 ) & ( strftime('%W-%m-%Y',datetime('now', 'localtime')) == strftime('%W-%m-%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LM_Time + " ASC;";
	private static final String	CREATE_view_LOGMSGS_TODAY_MMS					= "CREATE VIEW t_LogMsgs_TODAY_MMS AS SELECT * FROM " + table_LOGMSGS + " T WHERE ( T." + col_LM_Type + "==2 | T." + col_LM_Type + "==3 ) & ( strftime('%d-%m-%Y',datetime('now', 'localtime')) == strftime('%d-%m-%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LM_Time + " ASC;";
	private static final String	CREATE_view_LOGMSGS_TODAY_SMS					= "CREATE VIEW t_LogMsgs_TODAY_SMS AS SELECT * FROM " + table_LOGMSGS + " T WHERE ( T." + col_LM_Type + "==0 | T." + col_LM_Type + "==1 ) & ( strftime('%d-%m-%Y',datetime('now', 'localtime')) == strftime('%d-%m-%Y',datetime((T." + col_LM_Time + "/1000), 'unixepoch', 'localtime')) )=1 ORDER BY T." + col_LM_Time + " ASC;";
	//
	private static final String	CREATE_view_LOGMSGS_LASTMONTH_TOPMMSIN		= "CREATE VIEW t_LogMsgs_LASTMONTH_TopMMSIN AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_LASTMONTH_MMS WHERE " + col_LM_Type + "='2' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_LASTMONTH_TOPMMSOUT	= "CREATE VIEW t_LogMsgs_LASTMONTH_TopMMSOUT AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_LASTMONTH_MMS WHERE " + col_LM_Type + "='3' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_LASTMONTH_TOPSMSIN		= "CREATE VIEW t_LogMsgs_LASTMONTH_TopSMSIN AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_LASTMONTH_SMS WHERE " + col_LM_Type + "='0' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_LASTMONTH_TOPSMSOUT	= "CREATE VIEW t_LogMsgs_LASTMONTH_TopSMSOUT AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_LASTMONTH_SMS WHERE " + col_LM_Type + "='1' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_LASTMONTH_TOTALMMSIN	= "CREATE VIEW t_LogMsgs_LASTMONTH_TotalMMSIN AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_LASTMONTH_MMS WHERE " + col_LM_Type + "='2';";
	private static final String	CREATE_view_LOGMSGS_LASTMONTH_TOTALMMSOUT	= "CREATE VIEW t_LogMsgs_LASTMONTH_TotalMMSOUT AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_LASTMONTH_MMS WHERE " + col_LM_Type + "='3';";
	private static final String	CREATE_view_LOGMSGS_LASTMONTH_TOTALSMSIN	= "CREATE VIEW t_LogMsgs_LASTMONTH_TotalSMSIN AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_LASTMONTH_SMS WHERE " + col_LM_Type + "='0';";
	private static final String	CREATE_view_LOGMSGS_LASTMONTH_TOTALSMSOUT	= "CREATE VIEW t_LogMsgs_LASTMONTH_TotalSMSOUT AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_LASTMONTH_SMS WHERE " + col_LM_Type + "='1';";
	//
	private static final String	CREATE_view_LOGMSGS_LASTWEEK_TOPMMSIN		= "CREATE VIEW t_LogMsgs_LASTWEEK_TopMMSIN AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_LASTWEEK_MMS WHERE " + col_LM_Type + "='2' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_LASTWEEK_TOPMMSOUT		= "CREATE VIEW t_LogMsgs_LASTWEEK_TopMMSOUT AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_LASTWEEK_MMS WHERE " + col_LM_Type + "='3' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_LASTWEEK_TOPSMSIN		= "CREATE VIEW t_LogMsgs_LASTWEEK_TopSMSIN AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_LASTWEEK_SMS WHERE " + col_LM_Type + "='0' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_LASTWEEK_TOPSMSOUT		= "CREATE VIEW t_LogMsgs_LASTWEEK_TopSMSOUT AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_LASTWEEK_SMS WHERE " + col_LM_Type + "='1' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_LASTWEEK_TOTALMMSIN	= "CREATE VIEW t_LogMsgs_LASTWEEK_TotalMMSIN AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_LASTWEEK_MMS WHERE " + col_LM_Type + "='2';";
	private static final String	CREATE_view_LOGMSGS_LASTWEEK_TOTALMMSOUT	= "CREATE VIEW t_LogMsgs_LASTWEEK_TotalMMSOUT AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_LASTWEEK_MMS WHERE " + col_LM_Type + "='3';";
	private static final String	CREATE_view_LOGMSGS_LASTWEEK_TOTALSMSIN	= "CREATE VIEW t_LogMsgs_LASTWEEK_TotalSMSIN AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_LASTWEEK_SMS WHERE " + col_LM_Type + "='0';";
	private static final String	CREATE_view_LOGMSGS_LASTWEEK_TOTALSMSOUT	= "CREATE VIEW t_LogMsgs_LASTWEEK_TotalSMSOUT AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_LASTWEEK_SMS WHERE " + col_LM_Type + "='1';";
	//
	private static final String	CREATE_view_LOGMSGS_THISMONTH_TOPMMSIN		= "CREATE VIEW t_LogMsgs_THISMONTH_TopMMSIN AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_THISMONTH_MMS WHERE " + col_LM_Type + "='2' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_THISMONTH_TOPMMSOUT	= "CREATE VIEW t_LogMsgs_THISMONTH_TopMMSOUT AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_THISMONTH_MMS WHERE " + col_LM_Type + "='3' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_THISMONTH_TOPSMSIN		= "CREATE VIEW t_LogMsgs_THISMONTH_TopSMSIN AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_THISMONTH_SMS WHERE " + col_LM_Type + "='0' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_THISMONTH_TOPSMSOUT	= "CREATE VIEW t_LogMsgs_THISMONTH_TopSMSOUT AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_THISMONTH_SMS WHERE " + col_LM_Type + "='1' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_THISMONTH_TOTALMMSIN	= "CREATE VIEW t_LogMsgs_THISMONTH_TotalMMSIN AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_THISMONTH_MMS WHERE " + col_LM_Type + "='2';";
	private static final String	CREATE_view_LOGMSGS_THISMONTH_TOTALMMSOUT	= "CREATE VIEW t_LogMsgs_THISMONTH_TotalMMSOUT AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_THISMONTH_MMS WHERE " + col_LM_Type + "='3';";
	private static final String	CREATE_view_LOGMSGS_THISMONTH_TOTALSMSIN	= "CREATE VIEW t_LogMsgs_THISMONTH_TotalSMSIN AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_THISMONTH_SMS WHERE " + col_LM_Type + "='0';";
	private static final String	CREATE_view_LOGMSGS_THISMONTH_TOTALSMSOUT	= "CREATE VIEW t_LogMsgs_THISMONTH_TotalSMSOUT AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_THISMONTH_SMS WHERE " + col_LM_Type + "='1';";
	//
	private static final String	CREATE_view_LOGMSGS_THISWEEK_TOPMMSIN		= "CREATE VIEW t_LogMsgs_THISWEEK_TopMMSIN AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_THISWEEK_MMS WHERE " + col_LM_Type + "='2' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_THISWEEK_TOPMMSOUT		= "CREATE VIEW t_LogMsgs_THISWEEK_TopMMSOUT AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_THISWEEK_MMS WHERE " + col_LM_Type + "='3' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_THISWEEK_TOPSMSIN		= "CREATE VIEW t_LogMsgs_THISWEEK_TopSMSIN AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_THISWEEK_SMS WHERE " + col_LM_Type + "='0' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_THISWEEK_TOPSMSOUT		= "CREATE VIEW t_LogMsgs_THISWEEK_TopSMSOUT AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_THISWEEK_SMS WHERE " + col_LM_Type + "='1' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_THISWEEK_TOTALMMSIN	= "CREATE VIEW t_LogMsgs_THISWEEK_TotalMMSIN AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_THISWEEK_MMS WHERE " + col_LM_Type + "='2';";
	private static final String	CREATE_view_LOGMSGS_THISWEEK_TOTALMMSOUT	= "CREATE VIEW t_LogMsgs_THISWEEK_TotalMMSOUT AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_THISWEEK_MMS WHERE " + col_LM_Type + "='3';";
	private static final String	CREATE_view_LOGMSGS_THISWEEK_TOTALSMSIN	= "CREATE VIEW t_LogMsgs_THISWEEK_TotalSMSIN AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_THISWEEK_SMS WHERE " + col_LM_Type + "='0';";
	private static final String	CREATE_view_LOGMSGS_THISWEEK_TOTALSMSOUT	= "CREATE VIEW t_LogMsgs_THISWEEK_TotalSMSOUT AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_THISWEEK_SMS WHERE " + col_LM_Type + "='1';";
	//
	private static final String	CREATE_view_LOGMSGS_TODAY_TOPMMSIN			= "CREATE VIEW t_LogMsgs_TODAY_TopMMSIN AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_TODAY_MMS WHERE " + col_LM_Type + "='2' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_TODAY_TOPMMSOUT			= "CREATE VIEW t_LogMsgs_TODAY_TopMMSOUT AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_TODAY_MMS WHERE " + col_LM_Type + "='3' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_TODAY_TOPSMSIN			= "CREATE VIEW t_LogMsgs_TODAY_TopSMSIN AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_TODAY_SMS WHERE " + col_LM_Type + "='0' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_TODAY_TOPSMSOUT			= "CREATE VIEW t_LogMsgs_TODAY_TopSMSOUT AS SELECT COUNT(*) AS COUNT," + col_LM_Contact + " CONTACT FROM t_LogMsgs_TODAY_SMS WHERE " + col_LM_Type + "='1' GROUP BY CONTACT ORDER BY COUNT DESC," + col_LM_Time + " DESC;";
	private static final String	CREATE_view_LOGMSGS_TODAY_TOTALMMSIN		= "CREATE VIEW t_LogMsgs_TODAY_TotalMMSIN AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_TODAY_MMS WHERE " + col_LM_Type + "='2';";
	private static final String	CREATE_view_LOGMSGS_TODAY_TOTALMMSOUT		= "CREATE VIEW t_LogMsgs_TODAY_TotalMMSOUT AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_TODAY_MMS WHERE " + col_LM_Type + "='3';";
	private static final String	CREATE_view_LOGMSGS_TODAY_TOTALSMSIN		= "CREATE VIEW t_LogMsgs_TODAY_TotalSMSIN AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_TODAY_SMS WHERE " + col_LM_Type + "='0';";
	private static final String	CREATE_view_LOGMSGS_TODAY_TOTALSMSOUT		= "CREATE VIEW t_LogMsgs_TODAY_TotalSMSOUT AS SELECT COUNT(*) AS COUNT FROM t_LogMsgs_TODAY_SMS WHERE " + col_LM_Type + "='1';";
	/**
	 ****************************** LOGAPPS ******************************
	 */
	private static final String	CREATE_view_LOGAPPS_LASTMONTH					= "CREATE VIEW t_LogApps_LASTMONTH AS SELECT * FROM " + table_LOGAPPS + " T WHERE ( ( ( strftime('%Y',datetime('now')) == strftime('%Y',datetime((T." + col_LA_Time + "/1000), 'unixepoch')) ) & ( round ( strftime('%m',datetime('now'))-1 ) == round ( strftime('%m',datetime((T." + col_LA_Time + "/1000), 'unixepoch')) ) ) ) | ( ( round ( strftime('%Y',datetime('now'))-1 ) == round ( strftime('%Y',datetime((T." + col_LA_Time + "/1000), 'unixepoch')) ) ) & ( ( round ( strftime('%m',datetime('now')) ) == 1 ) & ( round ( strftime('%m',datetime((T." + col_LA_Time + "/1000), 'unixepoch')) ) == 12 ) ) ) )==1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LA_Time + "/1000), 'unixepoch','localtime')) ORDER BY T." + col_LA_Time + " ASC;";
	private static final String	CREATE_view_LOGAPPS_LASTMONTH_TOPDL			= "CREATE VIEW t_LogApps_LASTMONTH_TopDL AS SELECT SUM(" + col_LA_Downloaded + ") AS DOWNLOADED," + col_LA_Application + " APPLICATION FROM t_LogApps_LASTMONTH GROUP BY APPLICATION ORDER BY DOWNLOADED DESC," + col_LA_Time + " DESC;";
	private static final String	CREATE_view_LOGAPPS_LASTMONTH_TOPUL			= "CREATE VIEW t_LogApps_LASTMONTH_TopUL AS SELECT SUM(" + col_LA_Uploaded + ") AS UPLOADED," + col_LA_Application + " APPLICATION FROM t_LogApps_LASTMONTH GROUP BY APPLICATION ORDER BY UPLOADED DESC," + col_LA_Time + " DESC;";
	//
	private static final String	CREATE_view_LOGAPPS_LASTWEEK					= "CREATE VIEW t_LogApps_LASTWEEK AS SELECT * FROM " + table_LOGAPPS + " T WHERE ( ( ( strftime('%m-%Y',datetime('now')) == strftime('%m-%Y',datetime((T." + col_LA_Time + "/1000), 'unixepoch')) ) & ( round ( strftime('%W',datetime('now'))-1 ) == round ( strftime('%W',datetime((T." + col_LA_Time + "/1000), 'unixepoch')) ) ) ) | ( ( round ( strftime('%m-%Y',datetime('now'))-1 ) == round ( strftime('%m-%Y',datetime((T." + col_LA_Time + "/1000), 'unixepoch')) ) ) & ( round ( strftime('%W',datetime('now'))-1 ) == round ( strftime('%W',datetime((T." + col_LA_Time + "/1000), 'unixepoch')) ) ) ) | ( ( round ( strftime('%W%m',datetime('now')) )==11 & round ( strftime('%W%m',datetime((T." + col_LA_Time + "/1000), 'unixepoch')) )==5212 ) ) )=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LA_Time + "/1000), 'unixepoch','localtime')) ORDER BY T." + col_LA_Time + " ASC;";
	private static final String	CREATE_view_LOGAPPS_LASTWEEK_TOPDL			= "CREATE VIEW t_LogApps_LASTWEEK_TopDL AS SELECT SUM(" + col_LA_Downloaded + ") AS DOWNLOADED," + col_LA_Application + " APPLICATION FROM t_LogApps_LASTWEEK GROUP BY APPLICATION ORDER BY DOWNLOADED DESC," + col_LA_Time + " DESC;";
	private static final String	CREATE_view_LOGAPPS_LASTWEEK_TOPUL			= "CREATE VIEW t_LogApps_LASTWEEK_TopUL AS SELECT SUM(" + col_LA_Uploaded + ") AS UPLOADED," + col_LA_Application + " APPLICATION FROM t_LogApps_LASTWEEK GROUP BY APPLICATION ORDER BY UPLOADED DESC, " + col_LA_Time + " DESC;";
	//
	private static final String	CREATE_view_LOGAPPS_THISMONTH					= "CREATE VIEW t_LogApps_THISMONTH AS SELECT * FROM " + table_LOGAPPS + " T WHERE (strftime('%m-%Y',datetime('now','localtime'))==strftime('%m-%Y',datetime((T." + col_LA_Time + "/1000), 'unixepoch'))	)=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LA_Time + "/1000), 'unixepoch','localtime')) ORDER BY T." + col_LA_Time + " ASC;";
	private static final String	CREATE_view_LOGAPPS_THISMONTH_TOPDL			= "CREATE VIEW t_LogApps_THISMONTH_TopDL AS SELECT SUM(" + col_LA_Downloaded + ") AS DOWNLOADED," + col_LA_Application + " APPLICATION FROM t_LogApps_THISMONTH GROUP BY APPLICATION ORDER BY DOWNLOADED DESC," + col_LA_Time + " DESC;";
	private static final String	CREATE_view_LOGAPPS_THISMONTH_TOPUL			= "CREATE VIEW t_LogApps_THISMONTH_TopUL AS SELECT SUM(" + col_LA_Uploaded + ") AS UPLOADED," + col_LA_Application + " APPLICATION FROM t_LogApps_THISMONTH GROUP BY APPLICATION ORDER BY UPLOADED DESC, " + col_LA_Time + " DESC;";
	//
	private static final String	CREATE_view_LOGAPPS_THISWEEK					= "CREATE VIEW t_LogApps_THISWEEK AS SELECT * FROM " + table_LOGAPPS + " T WHERE ( strftime('%W-%m-%Y',datetime('now')) == strftime('%W-%m-%Y',datetime((T." + col_LA_Time + "/1000), 'unixepoch')) )=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LA_Time + "/1000), 'unixepoch','localtime')) ORDER BY T." + col_LA_Time + " ASC;";
	private static final String	CREATE_view_LOGAPPS_THISWEEK_TOPDL			= "CREATE VIEW t_LogApps_THISWEEK_TopDL AS SELECT SUM(" + col_LA_Downloaded + ") AS DOWNLOADED," + col_LA_Application + " APPLICATION FROM t_LogApps_THISWEEK GROUP BY APPLICATION ORDER BY DOWNLOADED DESC," + col_LA_Time + " DESC;";
	private static final String	CREATE_view_LOGAPPS_THISWEEK_TOPUL			= "CREATE VIEW t_LogApps_THISWEEK_TopUL AS SELECT SUM(" + col_LA_Uploaded + ") AS UPLOADED," + col_LA_Application + " APPLICATION FROM t_LogApps_THISWEEK GROUP BY APPLICATION ORDER BY UPLOADED DESC, " + col_LA_Time + " DESC;";
	//
	private static final String	CREATE_view_LOGAPPS_TODAY						= "CREATE VIEW t_LogApps_TODAY AS SELECT * FROM " + table_LOGAPPS + " T WHERE ( strftime('%d-%m-%Y',datetime('now')) == strftime('%d-%m-%Y',datetime((T." + col_LA_Time + "/1000), 'unixepoch')) )=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LA_Time + "/1000), 'unixepoch','localtime')) ORDER BY T." + col_LA_Time + " ASC;";
	private static final String	CREATE_view_LOGAPPS_TODAY_TOPDL				= "CREATE VIEW t_LogApps_TODAY_TopDL AS SELECT SUM(" + col_LA_Downloaded + ") AS DOWNLOADED," + col_LA_Application + " APPLICATION FROM t_LogApps_TODAY GROUP BY APPLICATION ORDER BY DOWNLOADED DESC," + col_LA_Time + " DESC;";
	private static final String	CREATE_view_LOGAPPS_TODAY_TOPUL				= "CREATE VIEW t_LogApps_TODAY_TopUL AS SELECT SUM(" + col_LA_Uploaded + ") AS UPLOADED," + col_LA_Application + " APPLICATION FROM t_LogApps_TODAY GROUP BY APPLICATION ORDER BY UPLOADED DESC," + col_LA_Time + " DESC;";
	/**
	 ****************************** LOGMOBILE ******************************
	 */
	private static final String	CREATE_view_LOGDATAMOBILE_LASTMONTH			= "CREATE VIEW t_LogDataMobile_LASTMONTH AS SELECT * FROM " + table_LOGDATAMOBILE + " T WHERE ( ( ( strftime('%Y',datetime('now')) == strftime('%Y',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ) & ( round ( strftime('%m',datetime('now'))-1 ) == round ( strftime('%m',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ) ) ) | ( ( round ( strftime('%Y',datetime('now'))-1 ) == round ( strftime('%Y',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ) ) & ( ( round ( strftime('%m',datetime('now')) ) == 1 ) & ( round ( strftime('%m',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ) == 12 ) ) ) )==1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ORDER BY T." + col_LDM_Time + " ASC;";
	private static final String	CREATE_view_LOGDATAMOBILE_LASTWEEK			= "CREATE VIEW t_LogDataMobile_LASTWEEK AS SELECT * FROM " + table_LOGDATAMOBILE + " T WHERE ( ( ( strftime('%m-%Y',datetime('now')) == strftime('%m-%Y',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ) & ( round ( strftime('%W',datetime('now'))-1 ) == round ( strftime('%W',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ) ) ) | ( ( round ( strftime('%m-%Y',datetime('now'))-1 ) == round ( strftime('%m-%Y',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ) ) & ( round ( strftime('%W',datetime('now'))-1 ) == round ( strftime('%W',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ) ) ) | ( ( round ( strftime('%W%m',datetime('now')) )==11 & round ( strftime('%W%m',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) )==5212 ) ) )=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ORDER BY T." + col_LDM_Time + " ASC;";
	private static final String	CREATE_view_LOGDATAMOBILE_THISMONTH			= "CREATE VIEW t_LogDataMobile_THISMONTH AS SELECT * FROM " + table_LOGDATAMOBILE + " T WHERE ( strftime('%m-%Y',datetime('now')) == strftime('%m-%Y',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) )=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ORDER BY T." + col_LDM_Time + " ASC;";
	private static final String	CREATE_view_LOGDATAMOBILE_THISWEEK			= "CREATE VIEW t_LogDataMobile_THISWEEK AS SELECT * FROM " + table_LOGDATAMOBILE + " T WHERE ( strftime('%W-%m-%Y',datetime('now')) == strftime('%W-%m-%Y',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) )=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ORDER BY T." + col_LDM_Time + " ASC;";
	private static final String	CREATE_view_LOGDATAMOBILE_TODAY				= "CREATE VIEW t_LogDataMobile_TODAY AS SELECT * FROM " + table_LOGDATAMOBILE + " T WHERE ( strftime('%d-%m-%Y',datetime('now')) == strftime('%d-%m-%Y',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) )=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LDM_Time + "/1000), 'unixepoch')) ORDER BY T." + col_LDM_Time + " ASC;";
	/**
	 ****************************** LOGWIFI ******************************
	 */
	private static final String	CREATE_view_LOGDATAWIFI_LASTMONTH			= "CREATE VIEW t_LogDataWifi_LASTMONTH AS SELECT * FROM " + table_LOGDATAWIFI + " T WHERE ( ( ( strftime('%Y',datetime('now')) == strftime('%Y',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ) & ( round ( strftime('%m',datetime('now'))-1 ) == round ( strftime('%m',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ) ) ) | ( ( round ( strftime('%Y',datetime('now'))-1 ) == round ( strftime('%Y',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ) ) & ( ( round ( strftime('%m',datetime('now')) ) == 1 ) & ( round ( strftime('%m',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ) == 12 ) ) ) )==1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ORDER BY T." + col_LDW_Time + " ASC;";
	private static final String	CREATE_view_LOGDATAWIFI_LASTWEEK				= "CREATE VIEW t_LogDataWifi_LASTWEEK AS SELECT * FROM " + table_LOGDATAWIFI + " T WHERE ( ( ( strftime('%m-%Y',datetime('now')) == strftime('%m-%Y',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ) & ( round ( strftime('%W',datetime('now'))-1 ) == round ( strftime('%W',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ) ) ) | ( ( round ( strftime('%m-%Y',datetime('now'))-1 ) == round ( strftime('%m-%Y',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ) ) & ( round ( strftime('%W',datetime('now'))-1 ) == round ( strftime('%W',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ) ) ) | ( ( round ( strftime('%W%m',datetime('now')) )==11 & round ( strftime('%W%m',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) )==5212 ) ) )=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ORDER BY T." + col_LDW_Time + " ASC;";
	private static final String	CREATE_view_LOGDATAWIFI_THISMONTH			= "CREATE VIEW t_LogDataWifi_THISMONTH AS SELECT * FROM " + table_LOGDATAWIFI + " T WHERE ( strftime('%m-%Y',datetime('now')) == strftime('%m-%Y',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) )=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ORDER BY T." + col_LDW_Time + " ASC;";
	private static final String	CREATE_view_LOGDATAWIFI_THISWEEK				= "CREATE VIEW t_LogDataWifi_THISWEEK AS SELECT * FROM " + table_LOGDATAWIFI + " T WHERE ( strftime('%W-%m-%Y',datetime('now')) == strftime('%W-%m-%Y',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) )=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ORDER BY T." + col_LDW_Time + " ASC;";
	private static final String	CREATE_view_LOGDATAWIFI_TODAY					= "CREATE VIEW t_LogDataWifi_TODAY AS SELECT * FROM " + table_LOGDATAWIFI + " T WHERE ( strftime('%d-%m-%Y',datetime('now')) == strftime('%d-%m-%Y',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) )=1 GROUP BY strftime('%Y%m%d',datetime((T." + col_LDW_Time + "/1000), 'unixepoch')) ORDER BY T." + col_LDW_Time + " ASC;";
	//
	private SQLiteDatabase			db;
	
	public SQLiteDatabase getDb()
	{
		return db;
	}
	
	public void setDb(SQLiteDatabase db)
	{
		this.db = db;
	}
	
	private final Context	dbContext;
	private DBHelper			dbHelper	= null;
	
	public LoggerDB(Context ctx)
	{
		dbContext = ctx;
		if (null == dbHelper)
		{
			dbHelper = new DBHelper(dbContext, DATABASE_NAME, null, DATABASE_VERSION);
			try
			{
				db = dbHelper.getWritableDatabase();
			}
			catch (SQLiteException ex)
			{
				// db = dbHelper.getReadableDatabase();
				ThothLog.e(ex);
			}
		}
	}
	
	public void close()
	{
		if (dbHelper != null)
			dbHelper.close();
	}
	
	public LoggerDB refreshAllTables()
	{
		ThothLog.i("All TABLE data will be refreshed.");
		//
		db.execSQL(CLEAR_table_USERDEVICE);
		db.execSQL(CLEAR_table_USERCONTACT);
		db.execSQL(CLEAR_table_USERAPPLICATION);
		//
		db.execSQL(CLEAR_table_LOGCALL);
		db.execSQL(CLEAR_table_LOGMSGS);
		db.execSQL(CLEAR_table_LOGAPPS);
		db.execSQL(CLEAR_table_LOGDATAWIFI);
		db.execSQL(CLEAR_table_LOGDATAMOBILE);
		return this;
	}
	
	private static class DBHelper extends SQLiteOpenHelper
	{
		public DBHelper(Context context, String name, CursorFactory factory, int version)
		{
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(CREATE_table_USERDEVICE);
			db.execSQL(CREATE_table_USERCONTACT);
			db.execSQL(CREATE_table_USERAPPLICATION);
			//
			db.execSQL(CREATE_table_LOGCALL);
			db.execSQL(CREATE_table_LOGMSGS);
			db.execSQL(CREATE_table_LOGAPPS);
			db.execSQL(CREATE_table_LOGDATAWIFI);
			db.execSQL(CREATE_table_LOGDATAMOBILE);
			//
			db.execSQL(CREATE_view_LOGCALL_THISYEAR);
			db.execSQL(CREATE_view_LOGMSGS_THISYEAR);
			db.execSQL(CREATE_view_LOGDATAWIFI_THISYEAR);
			db.execSQL(CREATE_view_LOGDATAMOBILE_THISYEAR);
			db.execSQL(CREATE_view_LOGAPPS_THISYEAR);
			//
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_IN);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_MIS);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_NOA);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_OUT);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_REJ);
			//
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_IN);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_MIS);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_NOA);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_OUT);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_REJ);
			//
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_IN);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_MIS);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_NOA);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_OUT);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_REJ);
			//
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_IN);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_MIS);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_NOA);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_OUT);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_REJ);
			//
			db.execSQL(CREATE_view_LOGCALL_TODAY_IN);
			db.execSQL(CREATE_view_LOGCALL_TODAY_MIS);
			db.execSQL(CREATE_view_LOGCALL_TODAY_NOA);
			db.execSQL(CREATE_view_LOGCALL_TODAY_OUT);
			db.execSQL(CREATE_view_LOGCALL_TODAY_REJ);
			//
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOPIN);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOPMIS);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOPNOA);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOPOUT);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOPREJ);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOTALIN);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOTALMIS);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOTALNOA);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOTALOUT);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOTALREJ);
			//
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOPIN);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOPMIS);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOPNOA);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOPOUT);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOPREJ);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOTALIN);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOTALMIS);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOTALNOA);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOTALOUT);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOTALREJ);
			//
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOPIN);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOPMIS);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOPNOA);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOPOUT);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOPREJ);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOTALIN);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOTALMIS);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOTALNOA);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOTALOUT);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOTALREJ);
			//
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOPIN);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOPMIS);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOPNOA);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOPOUT);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOPREJ);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOTALIN);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOTALMIS);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOTALNOA);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOTALOUT);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOTALREJ);
			//
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOPIN);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOPMIS);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOPNOA);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOPOUT);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOPREJ);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOTALIN);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOTALMIS);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOTALNOA);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOTALOUT);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOTALREJ);
			//
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_MMS);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_SMS);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_MMS);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_SMS);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_MMS);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_SMS);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_MMS);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_SMS);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_MMS);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_SMS);
			//
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOPMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOPMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOPSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOPSMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOTALMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOTALMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOTALSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOTALSMSOUT);
			//
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOPMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOPMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOPSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOPSMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOTALMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOTALMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOTALSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOTALSMSOUT);
			//
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOPMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOPMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOPSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOPSMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOTALMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOTALMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOTALSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOTALSMSOUT);
			//
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOPMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOPMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOPSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOPSMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOTALMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOTALMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOTALSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOTALSMSOUT);
			//
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOPMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOPMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOPSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOPSMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOTALMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOTALMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOTALSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOTALSMSOUT);
			//
			db.execSQL(CREATE_view_LOGAPPS_LASTMONTH);
			db.execSQL(CREATE_view_LOGAPPS_LASTMONTH_TOPDL);
			db.execSQL(CREATE_view_LOGAPPS_LASTMONTH_TOPUL);
			//
			db.execSQL(CREATE_view_LOGAPPS_LASTWEEK);
			db.execSQL(CREATE_view_LOGAPPS_LASTWEEK_TOPDL);
			db.execSQL(CREATE_view_LOGAPPS_LASTWEEK_TOPUL);
			//
			db.execSQL(CREATE_view_LOGAPPS_THISMONTH);
			db.execSQL(CREATE_view_LOGAPPS_THISMONTH_TOPDL);
			db.execSQL(CREATE_view_LOGAPPS_THISMONTH_TOPUL);
			//
			db.execSQL(CREATE_view_LOGAPPS_THISWEEK);
			db.execSQL(CREATE_view_LOGAPPS_THISWEEK_TOPDL);
			db.execSQL(CREATE_view_LOGAPPS_THISWEEK_TOPUL);
			//
			db.execSQL(CREATE_view_LOGAPPS_TODAY);
			db.execSQL(CREATE_view_LOGAPPS_TODAY_TOPDL);
			db.execSQL(CREATE_view_LOGAPPS_TODAY_TOPUL);
			//
			db.execSQL(CREATE_view_LOGDATAWIFI_LASTMONTH);
			db.execSQL(CREATE_view_LOGDATAWIFI_LASTWEEK);
			db.execSQL(CREATE_view_LOGDATAWIFI_THISMONTH);
			db.execSQL(CREATE_view_LOGDATAWIFI_THISWEEK);
			db.execSQL(CREATE_view_LOGDATAWIFI_TODAY);
			//
			db.execSQL(CREATE_view_LOGDATAMOBILE_LASTMONTH);
			db.execSQL(CREATE_view_LOGDATAMOBILE_LASTWEEK);
			db.execSQL(CREATE_view_LOGDATAMOBILE_THISMONTH);
			db.execSQL(CREATE_view_LOGDATAMOBILE_THISWEEK);
			db.execSQL(CREATE_view_LOGDATAMOBILE_TODAY);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			ThothLog.w("Upgrading " + this.getClass().getName() + " from version " + oldVersion + " to " + newVersion + ". All TABLE data will be deleted.");
			//
			// db.execSQL(DROP_table_USERDEVICE);
			// db.execSQL(DROP_table_USERCONTACT);
			// db.execSQL(DROP_table_USERAPPLICATION);
			// //
			// db.execSQL(DROP_table_LOGCALL);
			// db.execSQL(DROP_table_LOGMSGS);
			// db.execSQL(DROP_table_LOGAPPS);
			// db.execSQL(DROP_table_LOGDATAWIFI);
			// db.execSQL(DROP_table_LOGDATAMOBILE);
			//
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISYEAR;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISYEAR;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataWifi_THISYEAR;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataMobile_THISYEAR;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_THISYEAR;");
			//
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_IN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_MIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_NOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_OUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_REJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_IN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_MIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_NOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_OUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_REJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_IN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_MIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_NOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_OUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_REJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_IN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_MIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_NOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_OUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_REJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_IN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_MIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_NOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_OUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_REJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_TopIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_TopMIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_TopNOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_TopOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_TopREJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_TotalIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_TotalMIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_TotalNOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_TotalOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTMONTH_TotalREJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_TopIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_TopMIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_TopNOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_TopOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_TopREJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_TotalIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_TotalMIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_TotalNOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_TotalOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_LASTWEEK_TotalREJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_TopMIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_TopNOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_TopOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_TopREJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_TotalIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_TotalMIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_TotalNOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_TotalOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_TotalREJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_TopIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_TopMIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_TopNOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_TopOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_TopREJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_TotalIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_TotalMIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_TotalNOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_TotalOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISWEEK_TotalREJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_TopIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_TopMIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_TopNOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_TopOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_TopREJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_TotalIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_TotalMIS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_TotalNOA;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_TotalOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_TODAY_TotalREJ;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTMONTH_MMS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTMONTH_SMS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTWEEK_MMS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTWEEK_SMS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISMONTH_MMS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISMONTH_SMS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISWEEK_MMS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISWEEK_SMS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_TODAY_MMS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_TODAY_SMS;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTMONTH_TopMMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTMONTH_TopMMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTMONTH_TopSMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTMONTH_TopSMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTMONTH_TotalMMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTMONTH_TotalMMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTMONTH_TotalSMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTMONTH_TotalSMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTWEEK_TopMMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTWEEK_TopMMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTWEEK_TopSMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTWEEK_TopSMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTWEEK_TotalMMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTWEEK_TotalMMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTWEEK_TotalSMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_LASTWEEK_TotalSMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISMONTH_TopMMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISMONTH_TopMMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISMONTH_TopSMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISMONTH_TopSMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISMONTH_TotalMMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISMONTH_TotalMMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISMONTH_TotalSMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISMONTH_TotalSMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISWEEK_TopMMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISWEEK_TopMMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISWEEK_TopSMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISWEEK_TopSMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISWEEK_TotalMMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISWEEK_TotalMMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISWEEK_TotalSMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_THISWEEK_TotalSMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_TODAY_TopMMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_TODAY_TopMMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_TODAY_TopSMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_TODAY_TopSMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_TODAY_TotalMMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_TODAY_TotalMMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_TODAY_TotalSMSIN;");
			db.execSQL("DROP VIEW IF EXISTS t_LogMsgs_TODAY_TotalSMSOUT;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_LASTMONTH;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_LASTMONTH_TopDL;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_LASTMONTH_TopUL;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_LASTWEEK;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_LASTWEEK_TopDL;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_LASTWEEK_TopUL;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_THISMONTH;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_THISMONTH_TopDL;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_THISMONTH_TopUL;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_THISWEEK;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_THISWEEK_TopDL;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_THISWEEK_TopUL;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_TODAY;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_TODAY_TopDL;");
			db.execSQL("DROP VIEW IF EXISTS t_LogApps_TODAY_TopUL;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataWifi_LASTMONTH;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataWifi_LASTWEEK;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataWifi_THISMONTH;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataWifi_THISWEEK;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataWifi_TODAY;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataMobile_LASTMONTH;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataMobile_LASTWEEK;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataMobile_THISMONTH;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataMobile_THISWEEK;");
			db.execSQL("DROP VIEW IF EXISTS t_LogDataMobile_TODAY;");
			db.execSQL("DROP VIEW IF EXISTS t_LogCall_THISMONTH_TopIN;");
			//
			//
			db.execSQL(CREATE_view_LOGCALL_THISYEAR);
			db.execSQL(CREATE_view_LOGMSGS_THISYEAR);
			db.execSQL(CREATE_view_LOGDATAWIFI_THISYEAR);
			db.execSQL(CREATE_view_LOGDATAMOBILE_THISYEAR);
			db.execSQL(CREATE_view_LOGAPPS_THISYEAR);
			//
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_IN);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_MIS);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_NOA);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_OUT);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_REJ);
			//
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_IN);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_MIS);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_NOA);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_OUT);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_REJ);
			//
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_IN);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_MIS);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_NOA);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_OUT);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_REJ);
			//
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_IN);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_MIS);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_NOA);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_OUT);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_REJ);
			//
			db.execSQL(CREATE_view_LOGCALL_TODAY_IN);
			db.execSQL(CREATE_view_LOGCALL_TODAY_MIS);
			db.execSQL(CREATE_view_LOGCALL_TODAY_NOA);
			db.execSQL(CREATE_view_LOGCALL_TODAY_OUT);
			db.execSQL(CREATE_view_LOGCALL_TODAY_REJ);
			//
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOPIN);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOPMIS);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOPNOA);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOPOUT);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOPREJ);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOTALIN);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOTALMIS);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOTALNOA);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOTALOUT);
			db.execSQL(CREATE_view_LOGCALL_LASTMONTH_TOTALREJ);
			//
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOPIN);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOPMIS);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOPNOA);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOPOUT);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOPREJ);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOTALIN);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOTALMIS);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOTALNOA);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOTALOUT);
			db.execSQL(CREATE_view_LOGCALL_LASTWEEK_TOTALREJ);
			//
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOPIN);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOPMIS);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOPNOA);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOPOUT);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOPREJ);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOTALIN);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOTALMIS);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOTALNOA);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOTALOUT);
			db.execSQL(CREATE_view_LOGCALL_THISMONTH_TOTALREJ);
			//
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOPIN);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOPMIS);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOPNOA);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOPOUT);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOPREJ);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOTALIN);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOTALMIS);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOTALNOA);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOTALOUT);
			db.execSQL(CREATE_view_LOGCALL_THISWEEK_TOTALREJ);
			//
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOPIN);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOPMIS);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOPNOA);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOPOUT);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOPREJ);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOTALIN);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOTALMIS);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOTALNOA);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOTALOUT);
			db.execSQL(CREATE_view_LOGCALL_TODAY_TOTALREJ);
			//
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_MMS);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_SMS);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_MMS);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_SMS);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_MMS);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_SMS);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_MMS);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_SMS);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_MMS);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_SMS);
			//
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOPMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOPMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOPSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOPSMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOTALMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOTALMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOTALSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTMONTH_TOTALSMSOUT);
			//
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOPMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOPMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOPSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOPSMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOTALMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOTALMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOTALSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_LASTWEEK_TOTALSMSOUT);
			//
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOPMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOPMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOPSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOPSMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOTALMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOTALMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOTALSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISMONTH_TOTALSMSOUT);
			//
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOPMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOPMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOPSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOPSMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOTALMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOTALMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOTALSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_THISWEEK_TOTALSMSOUT);
			//
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOPMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOPMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOPSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOPSMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOTALMMSIN);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOTALMMSOUT);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOTALSMSIN);
			db.execSQL(CREATE_view_LOGMSGS_TODAY_TOTALSMSOUT);
			//
			db.execSQL(CREATE_view_LOGAPPS_LASTMONTH);
			db.execSQL(CREATE_view_LOGAPPS_LASTMONTH_TOPDL);
			db.execSQL(CREATE_view_LOGAPPS_LASTMONTH_TOPUL);
			//
			db.execSQL(CREATE_view_LOGAPPS_LASTWEEK);
			db.execSQL(CREATE_view_LOGAPPS_LASTWEEK_TOPDL);
			db.execSQL(CREATE_view_LOGAPPS_LASTWEEK_TOPUL);
			//
			db.execSQL(CREATE_view_LOGAPPS_THISMONTH);
			db.execSQL(CREATE_view_LOGAPPS_THISMONTH_TOPDL);
			db.execSQL(CREATE_view_LOGAPPS_THISMONTH_TOPUL);
			//
			db.execSQL(CREATE_view_LOGAPPS_THISWEEK);
			db.execSQL(CREATE_view_LOGAPPS_THISWEEK_TOPDL);
			db.execSQL(CREATE_view_LOGAPPS_THISWEEK_TOPUL);
			//
			db.execSQL(CREATE_view_LOGAPPS_TODAY);
			db.execSQL(CREATE_view_LOGAPPS_TODAY_TOPDL);
			db.execSQL(CREATE_view_LOGAPPS_TODAY_TOPUL);
			//
			db.execSQL(CREATE_view_LOGDATAWIFI_LASTMONTH);
			db.execSQL(CREATE_view_LOGDATAWIFI_LASTWEEK);
			db.execSQL(CREATE_view_LOGDATAWIFI_THISMONTH);
			db.execSQL(CREATE_view_LOGDATAWIFI_THISWEEK);
			db.execSQL(CREATE_view_LOGDATAWIFI_TODAY);
			//
			db.execSQL(CREATE_view_LOGDATAMOBILE_LASTMONTH);
			db.execSQL(CREATE_view_LOGDATAMOBILE_LASTWEEK);
			db.execSQL(CREATE_view_LOGDATAMOBILE_THISMONTH);
			db.execSQL(CREATE_view_LOGDATAMOBILE_THISWEEK);
			db.execSQL(CREATE_view_LOGDATAMOBILE_TODAY);
		}
	}
}
