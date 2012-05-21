package com.gnuc.thoth.framework;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.gnuc.thoth.framework.Thoth.Settings;
import com.gnuc.thoth.framework.callbacks.ThothRequestCallback;
import com.gnuc.thoth.framework.db.LoggerDB;

public class ThothProcessor
{
	public static void create()
	{
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
						LoggerDB ldb = null;
						try
						{
							//
							ldb = new LoggerDB(Thoth.cx);
							final SQLiteDatabase db = ldb.getDb();
							//
							ContentValues newRow = new ContentValues();
							newRow.put(LoggerDB.col_UD_PKEY, Settings.USR_DEVICE_PHONE_NUMBER);
							newRow.put(LoggerDB.col_UD_PhoneNumber, Settings.USR_DEVICE_PHONE_NUMBER);
							newRow.put(LoggerDB.col_UD_IMEI, Settings.USR_DEVICE_ACCOUNT_EMAIL);
							newRow.put(LoggerDB.col_UD_BuildModel, Settings.USR_DEVICE_BUILD_MODEL);
							newRow.put(LoggerDB.col_UD_BuildFirmware, Settings.USR_DEVICE_BUILD_FIRMWARE);
							try
							{
								db.insertOrThrow(LoggerDB.table_USERDEVICE, "", newRow);
							}
							catch (Exception e)
							{
								ThothLog.e(e);
							}
						}
						catch (Exception e)
						{
							ThothLog.e(e);
						}
						finally
						{
							if (null != ldb)
								ldb.close();
						}
					}
				}.run();
			};
		}.start();
	}
	
	public static void save(JSONObject req, final ThothRequestCallback reqCallback) throws Exception
	{
		JSONObject jo = req.getJSONObject("USER_DATA");
		Cursor cR = null;
		LoggerDB ldb = null;
		try
		{
			//
			ldb = new LoggerDB(Thoth.cx);
			final SQLiteDatabase db = ldb.getDb();
			//
			JSONObject logData = (JSONObject) req.get("LOG_DATA");
			if (logData.has("CALL"))
			{
				JSONArray callContacts = logData.getJSONArray("CALL");
				reqCallback.progress(-50, callContacts.length(), 0);
				for (int j = 0; j < callContacts.length(); ++j)
				{
					JSONObject contact = callContacts.getJSONObject(j);
					//
					reqCallback.progress(100, 1, 0);
					//
					int cI = 0, dI = 0, cO = 0, dO = 0, cRj = 0, cMd = 0, cNA = 0;
					//
					String UC_PKEY = contact.getString("cPN");
					cR = db.rawQuery("SELECT * FROM " + LoggerDB.table_USERCONTACT + " WHERE " + LoggerDB.col_UC_PKEY + "=\"" + UC_PKEY + "\"", null);
					if (cR != null && cR.moveToFirst())
					{
						cI = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UC_Count_Inbound));
						dI = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UC_Duration_Inbound));
						cO = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UC_Count_Outbound));
						dO = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UC_Duration_Outbound));
						cRj = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UC_Count_Rejected));
						cMd = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UC_Count_Missed));
						cNA = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UC_Count_NoAnswer));
						if (null != cR && !cR.isClosed())
							cR.close();
					}
					else
						db.insertOrThrow(LoggerDB.table_USERCONTACT, "", createNewContact(contact));
					if (contact.has("cL"))
					{
						JSONArray joc = contact.getJSONArray("cL");
						reqCallback.progress(-50, joc.length(), 0);
						for (int l = 0; l < joc.length(); ++l)
						{
							JSONObject c = joc.getJSONObject(l);
							//
							reqCallback.progress(100, 1, 0);
							//
							ContentValues newRow = new ContentValues();
							StringBuilder sb = new StringBuilder();
							sb.append(Thoth.Settings.USR_DEVICE_ACCOUNT_EMAIL.hashCode());
							sb.append(UC_PKEY);
							sb.append(c.getLong("tiOf"));
							sb.append(c.getLong("dOf"));
							//
							newRow.put(LoggerDB.col_LC_PKEY, sb.toString().hashCode());
							newRow.put(LoggerDB.col_LC_Type, c.getInt("tyOf"));
							newRow.put(LoggerDB.col_LC_Contact, UC_PKEY);
							newRow.put(LoggerDB.col_LC_Time, c.getLong("tiOf"));
							newRow.put(LoggerDB.col_LC_Timezone, c.getLong("tZOf"));
							newRow.put(LoggerDB.col_LC_Duration, c.getLong("dOf"));
							try
							{
								db.insertOrThrow(LoggerDB.table_LOGCALL, "", newRow);
							}
							catch (SQLiteConstraintException e)
							{
								db.replaceOrThrow(LoggerDB.table_LOGCALL, "", newRow);
							}
							switch (c.getInt("tyOf"))
							{
								case 0 :
									++cI;
									dI += c.getInt("dOf");
									break;
								case 1 :
									++cO;
									dO += c.getInt("dOf");
									break;
								case 2 :
									++cRj;
									break;
								case 3 :
									++cMd;
									break;
								case 4 :
									++cNA;
									break;
							}
						}
						//
						ContentValues newRow = new ContentValues();
						newRow.put(LoggerDB.col_UC_Count_Inbound, cI);
						newRow.put(LoggerDB.col_UC_Count_Outbound, cO);
						newRow.put(LoggerDB.col_UC_Count_Rejected, cRj);
						newRow.put(LoggerDB.col_UC_Count_Missed, cMd);
						newRow.put(LoggerDB.col_UC_Count_NoAnswer, cNA);
						newRow.put(LoggerDB.col_UC_Duration_Inbound, dI);
						newRow.put(LoggerDB.col_UC_Duration_Outbound, dO);
						//
						String where = LoggerDB.col_UC_PKEY + "=?";
						String[] whereArgs = {UC_PKEY};
						db.update(LoggerDB.table_USERCONTACT, newRow, where, whereArgs);
					}
				}
			}
			if (logData.has("MSGS"))
			{
				JSONArray msgContacts = logData.getJSONArray("MSGS");
				reqCallback.progress(-50, msgContacts.length(), 0);
				for (int j = 0; j < msgContacts.length(); ++j)
				{
					JSONObject contact = msgContacts.getJSONObject(j);
					//
					int cISMS = 0, cOSMS = 0, cIMMS = 0, cOMMS = 0;
					//
					String UC_PKEY = contact.getString("cPN");
					cR = db.rawQuery("SELECT * FROM " + LoggerDB.table_USERCONTACT + " WHERE " + LoggerDB.col_UC_PKEY + "=\"" + UC_PKEY + "\"", null);
					if (cR != null && cR.moveToFirst())
					{
						cISMS = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UC_Count_InboundSMS));
						cOSMS = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UC_Count_OutboundSMS));
						cIMMS = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UC_Count_InboundMMS));
						cOMMS = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UC_Count_OutboundMMS));
						if (null != cR && !cR.isClosed())
							cR.close();
					}
					else
						db.insertOrThrow(LoggerDB.table_USERCONTACT, "", createNewContact(contact));
					if (contact.has("mL"))
					{
						JSONArray jom = contact.getJSONArray("mL");
						reqCallback.progress(-50, jom.length(), 0);
						for (int l = 0; l < jom.length(); ++l)
						{
							JSONObject m = jom.getJSONObject(l);
							//
							reqCallback.progress(100, 1, 0);
							//
							ContentValues newRow = new ContentValues();
							StringBuilder sb = new StringBuilder();
							sb.append(Thoth.Settings.USR_DEVICE_ACCOUNT_EMAIL.hashCode());
							sb.append(UC_PKEY);
							sb.append(m.getLong("tiOf"));
							sb.append(m.getString("bOf").hashCode());
							//
							newRow.put(LoggerDB.col_LM_PKEY, sb.toString().hashCode());
							newRow.put(LoggerDB.col_LM_Type, m.getInt("tyOf"));
							newRow.put(LoggerDB.col_LM_Contact, UC_PKEY);
							newRow.put(LoggerDB.col_LM_Time, m.getLong("tiOf"));
							newRow.put(LoggerDB.col_LM_Timezone, m.getLong("tZOf"));
							newRow.put(LoggerDB.col_LM_Body, m.getString("bOf"));
							try
							{
								db.insertOrThrow(LoggerDB.table_LOGMSGS, "", newRow);
							}
							catch (SQLiteConstraintException e)
							{
								db.replaceOrThrow(LoggerDB.table_LOGMSGS, "", newRow);
							}
							switch (m.getInt("tyOf"))
							{
								case 0 :
									++cISMS;
									break;
								case 1 :
									++cOSMS;
									break;
								case 2 :
									++cIMMS;
									break;
								case 3 :
									++cOMMS;
									break;
							}
						}
						//
						ContentValues newRow = new ContentValues();
						newRow.put(LoggerDB.col_UC_Count_InboundSMS, cISMS);
						newRow.put(LoggerDB.col_UC_Count_OutboundSMS, cOSMS);
						newRow.put(LoggerDB.col_UC_Count_InboundMMS, cIMMS);
						newRow.put(LoggerDB.col_UC_Count_OutboundMMS, cOMMS);
						//
						String where = LoggerDB.col_UC_PKEY + "=?";
						String[] whereArgs = {UC_PKEY};
						db.update(LoggerDB.table_USERCONTACT, newRow, where, whereArgs);
					}
				}
			}
			if (logData.has("APPS"))
			{
				JSONArray apps = logData.getJSONArray("APPS");
				reqCallback.progress(-50, apps.length(), 0);
				for (int j = 0; j < apps.length(); ++j)
				{
					JSONObject app = apps.getJSONObject(j);
					//
					long tU = 0, tD = 0;
					//
					String UA_PKEY = app.getString("aP");
					cR = db.rawQuery("SELECT * FROM " + LoggerDB.table_USERAPPLICATION + " WHERE " + LoggerDB.col_UA_PKEY + "=\"" + UA_PKEY + "\"", null);
					if (cR != null && cR.moveToFirst())
					{
						tU = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UA_Total_Uploaded));
						tD = cR.getInt(cR.getColumnIndexOrThrow(LoggerDB.col_UA_Total_Downloaded));
						if (null != cR && !cR.isClosed())
							cR.close();
					}
					else
						db.insertOrThrow(LoggerDB.table_USERAPPLICATION, "", createNewApplication(app, jo.getString("DEVICE")));
					if (app.has("aL"))
					{
						JSONArray joa = app.getJSONArray("aL");
						reqCallback.progress(-50, joa.length(), 0);
						for (int l = 0; l < joa.length(); ++l)
						{
							JSONObject a = joa.getJSONObject(l);
							//
							reqCallback.progress(100, 1, 0);
							//
							ContentValues newRow = new ContentValues();
							StringBuilder sb = new StringBuilder();
							sb.append(Thoth.Settings.USR_DEVICE_ACCOUNT_EMAIL.hashCode());
							sb.append(UA_PKEY);
							sb.append(a.getLong("uL"));
							sb.append(a.getLong("dL"));
							//
							newRow.put(LoggerDB.col_LA_PKEY, sb.toString().hashCode());
							newRow.put(LoggerDB.col_LA_Application, UA_PKEY);
							newRow.put(LoggerDB.col_LA_Time, a.getLong("tiOf"));
							newRow.put(LoggerDB.col_LA_Uploaded, a.getLong("uL"));
							newRow.put(LoggerDB.col_LA_Downloaded, a.getLong("dL"));
							try
							{
								db.insertOrThrow(LoggerDB.table_LOGAPPS, "", newRow);
							}
							catch (SQLiteConstraintException e)
							{
								db.replaceOrThrow(LoggerDB.table_LOGAPPS, "", newRow);
							}
							tU += a.getLong("uL");
							tD += a.getLong("dL");
						}
						//
						ContentValues newRow = new ContentValues();
						newRow.put(LoggerDB.col_UA_Total_Uploaded, tU);
						newRow.put(LoggerDB.col_UA_Total_Downloaded, tD);
						//
						String where = LoggerDB.col_UA_PKEY + "=?";
						String[] whereArgs = {UA_PKEY};
						db.update(LoggerDB.table_USERAPPLICATION, newRow, where, whereArgs);
					}
				}
			}
			if (logData.has("WIFI"))
			{
				JSONArray wifi = logData.getJSONArray("WIFI");
				//
				reqCallback.progress(-50, 10, 0);
				//
				String UD_PKEY = jo.getString("DEVICE");
				cR = db.rawQuery("SELECT * FROM " + LoggerDB.table_USERDEVICE + " WHERE " + LoggerDB.col_UD_PKEY + "=\"" + UD_PKEY + "\"", null);
				if (cR != null && cR.moveToFirst())
				{
					reqCallback.progress(-50, wifi.length(), 0);
					//
					for (int j = 0; j < wifi.length(); ++j)
					{
						JSONObject w = wifi.getJSONObject(j);
						//
						reqCallback.progress(100, 1, 0);
						//
						ContentValues newRow = new ContentValues();
						StringBuilder sb = new StringBuilder();
						sb.append(Thoth.Settings.USR_DEVICE_ACCOUNT_EMAIL.hashCode());
						sb.append(UD_PKEY);
						sb.append(w.getLong("tiOf"));
						sb.append(w.getLong("uL"));
						sb.append(w.getLong("dL"));
						//
						newRow.put(LoggerDB.col_LDW_PKEY, sb.toString().hashCode());
						newRow.put(LoggerDB.col_LDW_Device, UD_PKEY);
						newRow.put(LoggerDB.col_LDW_Time, w.getLong("tiOf"));
						newRow.put(LoggerDB.col_LDW_Uploaded, w.getLong("uL"));
						newRow.put(LoggerDB.col_LDW_Downloaded, w.getLong("dL"));
						try
						{
							db.insertOrThrow(LoggerDB.table_LOGDATAWIFI, "", newRow);
						}
						catch (SQLiteConstraintException e)
						{
							db.replaceOrThrow(LoggerDB.table_LOGDATAWIFI, "", newRow);
						}
					}
					if (null != cR && !cR.isClosed())
						cR.close();
				}
			}
			if (logData.has("MOBL"))
			{
				JSONArray mobl = logData.getJSONArray("MOBL");
				//
				reqCallback.progress(-50, 10, 0);
				//
				String UD_PKEY = jo.getString("DEVICE");
				cR = db.rawQuery("SELECT * FROM " + LoggerDB.table_USERDEVICE + " WHERE " + LoggerDB.col_UD_PKEY + "=\"" + UD_PKEY + "\"", null);
				if (cR != null && cR.moveToFirst())
				{
					reqCallback.progress(-50, mobl.length(), 0);
					//
					for (int j = 0; j < mobl.length(); ++j)
					{
						JSONObject m = mobl.getJSONObject(j);
						//
						reqCallback.progress(100, 1, 0);
						//
						ContentValues newRow = new ContentValues();
						StringBuilder sb = new StringBuilder();
						sb.append(Thoth.Settings.USR_DEVICE_ACCOUNT_EMAIL.hashCode());
						sb.append(UD_PKEY);
						sb.append(m.getLong("tiOf"));
						sb.append(m.getLong("uL"));
						sb.append(m.getLong("dL"));
						//
						newRow.put(LoggerDB.col_LDM_PKEY, sb.toString().hashCode());
						newRow.put(LoggerDB.col_LDM_Device, UD_PKEY);
						newRow.put(LoggerDB.col_LDM_Time, m.getLong("tiOf"));
						newRow.put(LoggerDB.col_LDM_Uploaded, m.getLong("uL"));
						newRow.put(LoggerDB.col_LDM_Downloaded, m.getLong("dL"));
						try
						{
							db.insertOrThrow(LoggerDB.table_LOGDATAMOBILE, "", newRow);
						}
						catch (SQLiteConstraintException e)
						{
							db.replaceOrThrow(LoggerDB.table_LOGDATAMOBILE, "", newRow);
						}
					}
					if (null != cR && !cR.isClosed())
						cR.close();
				}
			}
		}
		catch (Exception e)
		{
			ThothLog.e(e);
		}
		finally
		{
			if (null != cR && !cR.isClosed())
				cR.close();
			if (null != ldb)
				ldb.close();
		}
	}
	
	private static ContentValues createNewContact(JSONObject contact) throws JSONException
	{
		ContentValues newRow = new ContentValues();
		newRow.put(LoggerDB.col_UC_PKEY, contact.getString("cPN"));
		if (contact.getString("cRId") == null)
		{
			newRow.put(LoggerDB.col_UC_Name, "UNKNOWN");
			newRow.put(LoggerDB.col_UC_Email, "UNKNOWN");
			newRow.put(LoggerDB.col_UC_RefId, "999999999");
		}
		else
		{
			newRow.put(LoggerDB.col_UC_Name, contact.getString(("cN")));
			newRow.put(LoggerDB.col_UC_Email, contact.getString("cE"));
			newRow.put(LoggerDB.col_UC_RefId, contact.getString("cRId"));
		}
		newRow.put(LoggerDB.col_UC_Count_Inbound, 0);
		newRow.put(LoggerDB.col_UC_Count_Outbound, 0);
		newRow.put(LoggerDB.col_UC_Count_Missed, 0);
		newRow.put(LoggerDB.col_UC_Count_Rejected, 0);
		newRow.put(LoggerDB.col_UC_Count_NoAnswer, 0);
		newRow.put(LoggerDB.col_UC_Duration_Inbound, 0);
		newRow.put(LoggerDB.col_UC_Duration_Outbound, 0);
		newRow.put(LoggerDB.col_UC_Count_InboundSMS, 0);
		newRow.put(LoggerDB.col_UC_Count_OutboundSMS, 0);
		newRow.put(LoggerDB.col_UC_Count_InboundMMS, 0);
		newRow.put(LoggerDB.col_UC_Count_OutboundMMS, 0);
		//
		return newRow;
	}
	
	private static ContentValues createNewApplication(JSONObject app, String device) throws JSONException
	{
		ContentValues newRow = new ContentValues();
		newRow.put(LoggerDB.col_UA_PKEY, app.getString("aP"));
		newRow.put(LoggerDB.col_UA_Device, device);
		newRow.put(LoggerDB.col_UA_Name, app.getString("aN"));
		newRow.put(LoggerDB.col_UA_Package, app.getString("aP"));
		newRow.put(LoggerDB.col_UA_Total_Uploaded, 0);
		newRow.put(LoggerDB.col_UA_Total_Downloaded, 0);
		//
		return newRow;
	}
}
