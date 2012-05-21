package com.gnuc.thoth.framework;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gnuc.thoth.framework.Thoth.Settings;
import com.gnuc.thoth.framework.db.LoggerDB;
import com.gnuc.thoth.framework.utils.date.ThothDate;
import com.gnuc.thoth.service.listeners.RequestListener;

public class ThothReporter
{
	public static boolean produce() throws JSONException
	{
		Map<Integer, String> CALL_TYPE = new HashMap<Integer, String>();
		CALL_TYPE.put(0, "IN");
		CALL_TYPE.put(1, "OUT");
		CALL_TYPE.put(2, "REJ");
		CALL_TYPE.put(3, "MIS");
		CALL_TYPE.put(4, "NOA");
		//
		Map<Integer, String> MSGS_TYPE = new HashMap<Integer, String>();
		MSGS_TYPE.put(0, "SMS");
		MSGS_TYPE.put(1, "SMS");
		MSGS_TYPE.put(2, "MMS");
		MSGS_TYPE.put(3, "MMS");
		//
		JSONObject jo = new JSONObject();
		//
		jo.put("USR", Settings.USR_DEVICE_ACCOUNT_EMAIL);
		jo.put("PROFILE", Settings.USR_DEVICE_PHONE_NUMBER);
		jo.put("SINCE", (new DateTime(Settings.APP_FIRST_RUN_DATE)).toString(DateTimeFormat.fullDate()));
		//
		String STMT_FOR = (Settings.APP_LAST_LOG_REPORT == -1l) ? "Until - " + ThothDate.now.toString(DateTimeFormat.shortDate()) : ThothDate.lastWeekS.toString(DateTimeFormat.shortDate()) + " - " + ThothDate.lastWeekE.toString(DateTimeFormat.shortDate());
		jo.put("STMT_FOR", STMT_FOR);
		//
		Cursor cR = null;
		LoggerDB ldb = new LoggerDB(Thoth.cx);
		try
		{
			SQLiteDatabase db = ldb.getDb();
			// CALLS
			JSONObject callLogs = new JSONObject();
			for (int callType : CALL_TYPE.keySet())
			{
				JSONArray calls = new JSONArray();
				//
				String target = (Settings.APP_LAST_LOG_REPORT > -1l) ? "t_LogCall_LASTWEEK_" + CALL_TYPE.get(callType) : "t_LogCall_THISYEAR";
				cR = db.rawQuery("SELECT A.contact,B.contactName,A.duration,A.time FROM " + target + " A,t_UserContact B WHERE A.type=" + callType + " AND A.contact=B._id ORDER BY A.time ASC", null);
				if (cR != null)
				{
					if (cR.moveToFirst())
					{
						do
						{
							JSONObject call = new JSONObject();
							call.put("CN", cR.getString(1).equalsIgnoreCase("UNKNOWN") ? cR.getString(0) : cR.getString(0) + " [" + cR.getString(1) + "]");
							call.put("DU", cR.getLong(2));
							call.put("DA", cR.getLong(3));
							//
							calls.put(call);
						}
						while (cR.moveToNext());
					}
					cR.close();
				}
				//
				callLogs.put(CALL_TYPE.get(callType), calls);
			}
			//
			// MSGS
			JSONObject msgsLogs = new JSONObject();
			for (int msgsType : MSGS_TYPE.keySet())
			{
				JSONArray msgs = new JSONArray();
				//
				String target = (Settings.APP_LAST_LOG_REPORT > -1l) ? "t_LogMsgs_LASTWEEK_" + MSGS_TYPE.get(msgsType) : "t_LogMsgs_THISYEAR";
				cR = db.rawQuery("SELECT A.contact,B.contactName,A.body,A.time FROM " + target + " A,t_UserContact B WHERE A.type=" + msgsType + " AND A.contact=B._id ORDER BY A.time ASC", null);
				if (cR != null)
				{
					if (cR.moveToFirst())
					{
						do
						{
							JSONObject msg = new JSONObject();
							msg.put("CN", cR.getString(1).equalsIgnoreCase("UNKNOWN") ? cR.getString(0) : cR.getString(0) + " [" + cR.getString(1) + "]");
							msg.put("BD", cR.getString(2));
							msg.put("DA", cR.getLong(3));
							//
							msgs.put(msg);
						}
						while (cR.moveToNext());
					}
					cR.close();
				}
				//
				msgsLogs.put(MSGS_TYPE.get(msgsType) + ((msgsType == 0 || msgsType == 2) ? "IN" : "OUT"), msgs);
			}
			//
			// DATA - WIFI
			JSONObject dataLogs = new JSONObject();
			JSONArray datas = new JSONArray();
			//
			String target = (Settings.APP_LAST_LOG_REPORT > -1l) ? "t_LogDataWifi_LASTWEEK" : "t_LogDataWifi_THISYEAR";
			cR = db.rawQuery("SELECT time,uploaded,downloaded FROM " + target + " ORDER BY time ASC", null);
			if (cR != null)
			{
				if (cR.moveToFirst())
				{
					do
					{
						JSONObject data = new JSONObject();
						data.put("DA", cR.getLong(0));
						data.put("UL", cR.getLong(1));
						data.put("DL", cR.getLong(2));
						//
						datas.put(data);
					}
					while (cR.moveToNext());
				}
				cR.close();
			}
			dataLogs.put("WIFI", datas);
			//
			//
			// DATA - MOBILE
			datas = new JSONArray();
			//
			target = (Settings.APP_LAST_LOG_REPORT > -1l) ? "t_LogDataMobile_LASTWEEK" : "t_LogDataMobile_THISYEAR";
			cR = db.rawQuery("SELECT time,uploaded,downloaded FROM " + target + " ORDER BY time ASC", null);
			if (cR != null)
			{
				if (cR.moveToFirst())
				{
					do
					{
						JSONObject data = new JSONObject();
						data.put("DA", cR.getLong(0));
						data.put("UL", cR.getLong(1));
						data.put("DL", cR.getLong(2));
						//
						datas.put(data);
					}
					while (cR.moveToNext());
				}
				cR.close();
			}
			//
			dataLogs.put("MOBILE", datas);
			//
			// DATA - APPS
			datas = new JSONArray();
			//
			target = (Settings.APP_LAST_LOG_REPORT > -1l) ? "t_LogApps_LASTWEEK" : "t_LogApps_THISYEAR";
			cR = db.rawQuery("SELECT B.appName,B.appPackage,A.time,A.uploaded,A.downloaded FROM " + target + " A, t_UserApplication B WHERE A.application=B.appPackage ORDER BY A.time ASC", null);
			if (cR != null)
			{
				if (cR.moveToFirst())
				{
					do
					{
						JSONObject data = new JSONObject();
						data.put("AN", cR.getString(0) + " [" + cR.getString(1) + "]");
						data.put("DA", cR.getLong(2));
						data.put("UL", cR.getLong(3));
						data.put("DL", cR.getLong(4));
						//
						datas.put(data);
					}
					while (cR.moveToNext());
				}
				cR.close();
			}
			//
			dataLogs.put("APPS", datas);
			//
			JSONObject joo = new JSONObject();
			joo.put("CALL", callLogs);
			joo.put("MSGS", msgsLogs);
			joo.put("DATA", dataLogs);
			jo.put("LOG_DATA", joo);
			//
			RequestListener.onReceive(Thoth.REQ.LOG_MAIL, jo);
			Settings.APP_LAST_LOG_REPORT = ThothDate.now.getMillis();
			Settings.write();
			//
			return true;
		}
		catch (Exception e)
		{
			ThothLog.e(e);
			return false;
		}
		finally
		{
			ldb.getDb().close();
		}
	}
}
