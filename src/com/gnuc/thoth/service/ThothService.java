package com.gnuc.thoth.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.gnuc.thoth.R;
import com.gnuc.thoth.app.SetupMain;
import com.gnuc.thoth.app.TabMain;
import com.gnuc.thoth.app.UpgradeMain;
import com.gnuc.thoth.app.screens.SyncMain;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.Thoth.REQ;
import com.gnuc.thoth.framework.Thoth.Settings;
import com.gnuc.thoth.framework.ThothLog;
import com.gnuc.thoth.framework.ThothMail;
import com.gnuc.thoth.framework.ThothReporter;
import com.gnuc.thoth.framework.auth.ThothAuth;
import com.gnuc.thoth.framework.callbacks.ThothMailCallback;
import com.gnuc.thoth.framework.callbacks.ThothRequestCallback;
import com.gnuc.thoth.framework.db.ThothDB;
import com.gnuc.thoth.framework.network.NetworkStatistics;
import com.gnuc.thoth.framework.utils.MD5;
import com.gnuc.thoth.framework.utils.date.ThothDate;
import com.gnuc.thoth.framework.utils.date.ThothDateMatch;
import com.gnuc.thoth.service.listeners.RequestListener;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ThothService extends IntentService
{
	static Context						cx				= null;
	int									pbMax			= 1;
	int									pbProgress	= 0;
	public static ExecutorService	service		= Executors.newSingleThreadExecutor();
	
	public ThothService()
	{
		super("Thoth Service");
	}
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
	}
	
	@Override
	protected void onHandleIntent(final Intent intent)
	{
		cx = getApplicationContext();
		Thoth.TRACKER = GoogleAnalyticsTracker.getInstance();
		Thoth.TRACKER.setAnonymizeIp(true);
		Thoth.TRACKER.startNewSession("UA-24XXXX71-1", cx);
		Settings.read();
		if (intent != null)
		{
			switch (Integer.parseInt(intent.getAction()))
			{
				case Thoth.REQ.CALL_CAPTURE :
				{
					if (Settings.APP_SETUP)
						setupCallCapture();
					else
					{
						sHandler.sendEmptyMessageDelayed(40, 3000);
						Thoth.TRACKER.trackPageView("/CALL_CAPTURE");
					}
					break;
				}
				case Thoth.REQ.MSGS_CAPTURE :
				{
					if (Settings.APP_SETUP)
						setupSMSCapture();
					else
					{
						handleSMSCapture(intent);
						Thoth.TRACKER.trackPageView("/MSGS_CAPTURE");
					}
					break;
				}
				case Thoth.REQ.MSGM_CAPTURE :
				{
					if (Settings.APP_SETUP)
						setupMMSCapture();
					else
					{
						handleMMSCapture();
						Thoth.TRACKER.trackPageView("/MSGM_CAPTURE");
					}
					break;
				}
				case Thoth.REQ.DATA_CAPTURE :
				{
					if (Settings.APP_ENABLE_DATA_CAPTURE)
					{
						if (Settings.APP_SETUP)
							setupDataCapture();
						else
						{
							handleDataCapture();
							Thoth.TRACKER.trackPageView("/DATA_CAPTURE");
						}
					}
					break;
				}
				case Thoth.REQ.UPGRADE_CAPTURE :
				{
					upgradeCallCapture();
					upgradeSMSCapture();
					upgradeMMSCapture();
					upgradeDataCapture();
					break;
				}
				case Thoth.REQ.LOG_UPDATE :
				{
					if (!Settings.APP_UPDATE_IN_PROGRESS && !Settings.APP_SETUP)
						sHandler.sendEmptyMessage(1);
					else
					{
						if (null != TabMain.g())
							TabMain.g().TABMAIN.sendEmptyMessage(4);
					}
					Thoth.TRACKER.trackPageView("/LOG_UPDATE");
					break;
				}
				case Thoth.REQ.SYNC_CAPTURE :
				{
					if (!Settings.APP_SYNC_IN_PROGRESS && !Settings.APP_SETUP)
					{
						Settings.APP_SYNC_IN_PROGRESS = true;
						Settings.write();
						//
						missedCallCapture();
						missedSMSCapture();
						missedMMSCapture();
					}
					break;
				}
				case Thoth.REQ.NOTIFY :
				{
					Thoth.showBiWeeklyNotification();
					break;
				}
				// Backup Logger.db to cloud
				case Thoth.REQ.LOG_BACKUP :
				{
					ThothDateMatch tdm = ThothDate.filter(new DateTime(Settings.APP_LAST_LOG_BACKUP));
					if ((!tdm.THIS_WEEK && ThothDate.now.dayOfWeek().getAsText().equalsIgnoreCase("SATURDAY")) || (Settings.APP_LAST_LOG_BACKUP == -1))
						sHandler.sendEmptyMessage(3);
					else
					{
						Settings.APP_BACKUP_PENDING = true;
						Settings.write();
					}
					//
					Thoth.TRACKER.trackPageView("/LOG_BACKUP");
					break;
				}
				// Restore Logger.db from cloud. Only called in setup or upgrade
				case Thoth.REQ.LOG_RESTORE :
				{
					if (!Settings.APP_SETUP)
						sHandler.sendEmptyMessage(4);
					Thoth.TRACKER.trackPageView("/LOG_RESTORE");
					break;
				}
				case Thoth.REQ.REPORT_CONSTRUCTOR :
				{
					ThothDateMatch tdm = ThothDate.filter(new DateTime(Settings.APP_LAST_REPORT_CONSTRUCT));
					if ((!tdm.THIS_WEEK && Settings.APP_REPORT_CONSTRUCTION_PENDING) || (Settings.APP_LAST_REPORT_CONSTRUCT == -1))
						sHandler.sendEmptyMessage(5);
					else
					{
						Settings.APP_REPORT_CONSTRUCTION_PENDING = true;
						Settings.write();
					}
					Thoth.TRACKER.trackPageView("/REPORT_CONSTRUCTOR");
					break;
				}
				case Thoth.REQ.REPORT_MAILER :
				{
					ThothDateMatch tdm1 = ThothDate.filter(new DateTime(Settings.APP_LAST_REPORT_CONSTRUCT));
					ThothDateMatch tdm2 = ThothDate.filter(new DateTime(Settings.APP_LAST_REPORT_MAILED));
					if (Settings.APP_REPORT_LOGS && ((tdm1.THIS_WEEK && !tdm2.THIS_WEEK && Settings.APP_REPORT_MAIL_PENDING) || (Settings.APP_LAST_REPORT_MAILED == -1)))
						sHandler.sendEmptyMessage(6);
					else
					{
						Settings.APP_REPORT_MAIL_PENDING = true;
						Settings.write();
					}
					Thoth.TRACKER.trackPageView("/REPORT_MAILER");
					break;
				}
				case Thoth.REQ.MAIL_DEBUG_LOGS :
				{
					sHandler.sendEmptyMessage(7);
					break;
				}
				case 1000 :
				{
					sHandler.sendEmptyMessageDelayed(1000, 120000);
					break;
				}
			}
			Thoth.clearNotifications();
		}
	}
	
	final Handler	sHandler	= new Handler()
									{
										@Override
										public void handleMessage(Message msg)
										{
											switch (msg.what)
											{
												case -1 : // Update failed
												{
													Settings.APP_UPDATE_PENDING = true;
													Settings.APP_UPDATE_IN_PROGRESS = false;
													Settings.write();
													//
													if (null != SyncMain.getInstance())
														SyncMain.getInstance().sHandler.sendEmptyMessage(1);
													if (null != TabMain.g())
														TabMain.g().TABMAIN.sendEmptyMessage(4);
													break;
												}
												case 1 :
												{
													Thoth.clearNotifications();
													//
													if (!Thoth.isNetworkOK())
													{
														Settings.APP_UPDATE_PENDING = true;
														Settings.APP_UPDATE_IN_PROGRESS = false;
														Settings.write();
														//
														if (null != SyncMain.getInstance())
															SyncMain.getInstance().sHandler.sendEmptyMessage(1);
														if (null != TabMain.g())
															TabMain.g().TABMAIN.sendEmptyMessage(4);
													}
													else
													{
														Settings.APP_UPDATE_PENDING = true;
														Settings.APP_UPDATE_IN_PROGRESS = true;
														Settings.write();
														//
														Toast.makeText(cx, "Thoth : An update is in progress", Toast.LENGTH_SHORT).show();
														updateLogs();
													}
													break;
												}
												case 2 :
												{
													Thoth.clearNotifications();
													//
													Settings.APP_LAST_SYNC_THOTH = ThothDate.now.getMillis();
													if (null != SyncMain.getInstance())
														SyncMain.getInstance().sHandler.sendEmptyMessage(1);
													if (null != TabMain.g())
														TabMain.g().TABMAIN.sendEmptyMessage(4);
													//
													Settings.APP_UPDATE_PENDING = false;
													Settings.APP_UPDATE_IN_PROGRESS = false;
													Settings.write();
													//
													Toast.makeText(cx, "Thoth : Update was successful", Toast.LENGTH_SHORT).show();
													break;
												}
												case 3 :
												{
													if (Thoth.isNetworkOK())
													{
														Settings.APP_BACKUP_IN_PROGRESS = true;
														Settings.write();
														//
														RequestListener.onReceive(Thoth.REQ.LOG_BACKUP, new JSONObject());
													}
													break;
												}
												case 4 :
												{
													if (Thoth.isNetworkOK())
													{
														Settings.APP_RESTORE_IN_PROGRESS = true;
														Settings.write();
														//
														RequestListener.onReceive(Thoth.REQ.LOG_RESTORE, new JSONObject());
													}
													break;
												}
												case 5 :
												{
													if (Thoth.isNetworkOK())
													{
														try
														{
															if (ThothDate.now.dayOfWeek().getAsText().equalsIgnoreCase("MONDAY") || Settings.APP_REPORT_CONSTRUCTION_PENDING)
															{
																Settings.APP_LAST_REPORT_CONSTRUCT = ThothDate.now.getMillis();
																Settings.APP_REPORT_MAIL_PENDING = ThothReporter.produce();
																Settings.APP_REPORT_CONSTRUCTION_PENDING = !Settings.APP_REPORT_MAIL_PENDING;
																Settings.write();
															}
															else
																ThothLog.w("Not generating report. Will check tommorrow :");
														}
														catch (Exception e)
														{
															Settings.APP_REPORT_CONSTRUCTION_PENDING = true;
															Settings.write();
															Thoth.clearNotifications();
														}
														Thoth.scheduleServices();
													}
													break;
												}
												case 6 :
												{
													if (Thoth.isNetworkOK())
													{
														try
														{
															if (ThothDate.now.dayOfWeek().getAsText().equalsIgnoreCase("MONDAY") || Settings.APP_REPORT_MAIL_PENDING)
															{
																RequestListener.onReceive(REQ.REPORT_MAIL, new ThothRequestCallback()
																{
																	@Override
																	public void progress(int resultCode, int progressValue, int reference)
																	{}
																	
																	@Override
																	public void handle(int resultCode)
																	{
																		if (resultCode > 0)
																		{
																			ThothMail.getInstance().sendReportMail(new ThothMailCallback()
																			{
																				@Override
																				public void progress(int resultCode, int progressValue)
																				{}
																				
																				@Override
																				public void handle(boolean result, int resultCode)
																				{
																					Settings.APP_LAST_REPORT_MAILED = ThothDate.now.getMillis();
																					Settings.APP_REPORT_MAIL_PENDING = false;
																					Settings.write();
																					//
																					ThothLog.clear();
																				}
																			});
																		}
																	}
																});
															}
															else
																ThothLog.w("Not mailing report. Will try tommorrow :");
														}
														catch (Exception e)
														{
															Settings.APP_REPORT_MAIL_PENDING = true;
															Settings.write();
															Thoth.clearNotifications();
														}
													}
													Thoth.scheduleServices();
													break;
												}
												case 7 :
												{
													if (Thoth.isNetworkOK())
													{
														try
														{
															ThothMail.getInstance().sendDebugMail("Debug : " + Settings.USR_DEVICE_ACCOUNT_EMAIL);
														}
														catch (Exception e)
														{
															ThothLog.e(e);
															Thoth.clearNotifications();
														}
													}
													break;
												}
												case 40 :
												{
													handleCallCapture();
													break;
												}
												case 1000 :
												{
													if (!Settings.APP_UPDATE_IN_PROGRESS && !Settings.APP_SETUP)
														sHandler.sendEmptyMessage(10);
													break;
												}
												case -999 :
												{
													synchronized (this)
													{
														ThothAuth.authTimeout = true;
													};
													break;
												}
											}
										}
									};
	
	public void updateLogs()
	{
		try
		{
			pbMax = pbProgress = 0;
			final NotificationManager nfm = (NotificationManager) cx.getSystemService(Context.NOTIFICATION_SERVICE);
			final Notification nf = new Notification(R.drawable.thoths, "Thoth: Log update", System.currentTimeMillis());
			nf.ledARGB = android.graphics.Color.BLUE;
			nf.ledOnMS = 300;
			nf.ledOffMS = 100;
			nf.flags |= Notification.FLAG_NO_CLEAR + Notification.FLAG_SHOW_LIGHTS;
			RemoteViews contentView = new RemoteViews(cx.getPackageName(), R.layout.notify_nwk_update);
			contentView.setProgressBar(R.id.no_nw_up_pb, pbMax, pbProgress, false);
			contentView.setTextViewText(R.id.no_nw_up_status, "Log update....");
			nf.contentView = contentView;
			Intent defaultIntent = new Intent();
			defaultIntent.setAction("Default");
			nf.contentIntent = PendingIntent.getActivity(cx, 0, defaultIntent, 0);
			nf.contentView.setProgressBar(R.id.no_nw_up_pb, 1, 0, false);
			nfm.notify(Thoth.APP + 2, nf);
			//
			RequestListener.onReceive(Thoth.REQ.LOG_CREATE, new ThothRequestCallback()
			{
				@Override
				public void handle(int resultCode)
				{
					if (resultCode == -1)
						sHandler.sendEmptyMessage(-1);
					else if (resultCode == 1)
						sHandler.sendEmptyMessage(2);
				}
				
				@Override
				public void progress(int resultCode, int progressValue, int reference)
				{
					if (resultCode == -50)
					{
						pbMax += progressValue;
						nf.contentView.setProgressBar(R.id.no_nw_up_pb, pbMax, 0, false);
						nfm.notify(Thoth.APP + 2, nf);
					}
					else
					{
						pbProgress += progressValue;
						nf.contentView.setProgressBar(R.id.no_nw_up_pb, pbMax, pbProgress, false);
						nfm.notify(Thoth.APP + 2, nf);
					}
				}
			});
		}
		catch (Exception e)
		{
			ThothLog.e(e);
		}
	}
	
	public static void updateGoogleCalendar(final long time, final long duration, final String title, final String description, final int callOrMessage)
	{
		if (callOrMessage == 1 && Settings.USR_DEVICE_ACCOUNT_CALENDAR_CALL > 0)
		{
			final Uri CALENDAR = (Integer.parseInt(Build.VERSION.SDK) > Build.VERSION_CODES.ECLAIR_MR1) ? Uri.parse("content://com.android.calendar") : Uri.parse("content://calendar");
			final ContentResolver cr = cx.getContentResolver();
			final ContentValues event = new ContentValues();
			Settings.read();
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
			{
				event.put("calendar_id", Settings.USR_DEVICE_ACCOUNT_CALENDAR_CALL);
				event.put("title", title);
				event.put("description", description);
				event.put("dtstart", time);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(time);
				cal.add(Calendar.SECOND, (int) duration);
				event.put("dtend", cal.getTimeInMillis());
				event.put("exdate", time);
				event.put("eventStatus", 0);
				event.put("organizer", Settings.USR_DEVICE_ACCOUNT_EMAIL);
				event.put("eventTimezone", TimeZone.getDefault().getID());
				event.put("hasAlarm", 0);
				event.put("allDay", 0);
			}
			else
			{
				event.put("calendar_id", Settings.USR_DEVICE_ACCOUNT_CALENDAR_CALL);
				event.put("title", title);
				event.put("description", description);
				event.put("dtstart", time);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(time);
				cal.add(Calendar.SECOND, (int) duration);
				event.put("dtend", cal.getTimeInMillis());
				event.put("exdate", time);
				event.put("duration", duration <= 0 ? 0 : duration);
				event.put("eventStatus", 0);
				event.put("visibility", 0);
				event.put("transparency", 0);
				event.put("organizer", Settings.USR_DEVICE_ACCOUNT_EMAIL);
				event.put("eventTimezone", TimeZone.getDefault().getID());
				event.put("_sync_account", Settings.USR_DEVICE_ACCOUNT_EMAIL);
				event.put("_sync_account_type", "com.google");
				event.put("hasAlarm", 0);
				event.put("allDay", 0);
			}
			try
			{
				cr.insert(Uri.withAppendedPath(CALENDAR, "events"), event).getLastPathSegment();
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
		}
		else if (callOrMessage == 2 && Settings.USR_DEVICE_ACCOUNT_CALENDAR_MSG > 0)
		{
			final Uri CALENDAR = (Integer.parseInt(Build.VERSION.SDK) > Build.VERSION_CODES.ECLAIR_MR1) ? Uri.parse("content://com.android.calendar") : Uri.parse("content://calendar");
			final ContentResolver cr = cx.getContentResolver();
			final ContentValues event = new ContentValues();
			Settings.read();
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
			{
				event.put("calendar_id", Settings.USR_DEVICE_ACCOUNT_CALENDAR_MSG);
				event.put("title", title);
				event.put("description", description);
				event.put("dtstart", time);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(time);
				cal.add(Calendar.SECOND, (int) duration);
				event.put("dtend", cal.getTimeInMillis());
				event.put("exdate", time);
				event.put("eventStatus", 0);
				event.put("organizer", Settings.USR_DEVICE_ACCOUNT_EMAIL);
				event.put("eventTimezone", TimeZone.getDefault().getID());
				event.put("hasAlarm", 0);
				event.put("allDay", 0);
			}
			else
			{
				event.put("calendar_id", Settings.USR_DEVICE_ACCOUNT_CALENDAR_MSG);
				event.put("title", title);
				event.put("description", description);
				event.put("dtstart", time);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(time);
				cal.add(Calendar.SECOND, (int) duration);
				event.put("dtend", cal.getTimeInMillis());
				event.put("exdate", time);
				event.put("duration", duration <= 0 ? 0 : duration);
				event.put("eventStatus", 0);
				event.put("visibility", 0);
				event.put("transparency", 0);
				event.put("organizer", Settings.USR_DEVICE_ACCOUNT_EMAIL);
				event.put("eventTimezone", TimeZone.getDefault().getID());
				event.put("_sync_account", Settings.USR_DEVICE_ACCOUNT_EMAIL);
				event.put("_sync_account_type", "com.google");
				event.put("hasAlarm", 0);
				event.put("allDay", 0);
			}
			try
			{
				cr.insert(Uri.withAppendedPath(CALENDAR, "events"), event).getLastPathSegment();
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
		}
		else if (Settings.USR_DEVICE_ACCOUNT_CALENDAR > 0)
		{
			final Uri CALENDAR = (Integer.parseInt(Build.VERSION.SDK) > Build.VERSION_CODES.ECLAIR_MR1) ? Uri.parse("content://com.android.calendar") : Uri.parse("content://calendar");
			final ContentResolver cr = cx.getContentResolver();
			final ContentValues event = new ContentValues();
			Settings.read();
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
			{
				event.put("calendar_id", Settings.USR_DEVICE_ACCOUNT_CALENDAR);
				event.put("title", title);
				event.put("description", description);
				event.put("dtstart", time);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(time);
				cal.add(Calendar.SECOND, (int) duration);
				event.put("dtend", cal.getTimeInMillis());
				event.put("exdate", time);
				event.put("eventStatus", 0);
				event.put("organizer", Settings.USR_DEVICE_ACCOUNT_EMAIL);
				event.put("eventTimezone", TimeZone.getDefault().getID());
				event.put("hasAlarm", 0);
				event.put("allDay", 0);
			}
			else
			{
				event.put("calendar_id", Settings.USR_DEVICE_ACCOUNT_CALENDAR);
				event.put("title", title);
				event.put("description", description);
				event.put("dtstart", time);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(time);
				cal.add(Calendar.SECOND, (int) duration);
				event.put("dtend", cal.getTimeInMillis());
				event.put("exdate", time);
				event.put("duration", duration <= 0 ? 0 : duration);
				event.put("eventStatus", 0);
				event.put("visibility", 0);
				event.put("transparency", 0);
				event.put("organizer", Settings.USR_DEVICE_ACCOUNT_EMAIL);
				event.put("eventTimezone", TimeZone.getDefault().getID());
				event.put("_sync_account", Settings.USR_DEVICE_ACCOUNT_EMAIL);
				event.put("_sync_account_type", "com.google");
				event.put("hasAlarm", 0);
				event.put("allDay", 0);
			}
			try
			{
				cr.insert(Uri.withAppendedPath(CALENDAR, "events"), event).getLastPathSegment();
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
		}
		else
			return;
	}
	
	void handleCallCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				ContentResolver cr = getContentResolver();
				Cursor callCur = cr.query(android.provider.CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");
				callCur.moveToFirst();
				int T_C_DURATION;
				long T_C_DATETIME;
				String H_C_PKEY;
				String T_C_NUMBER, T_C_CONTACT, T_C_CONTACTNAME, T_C_TYPE;
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				for (int rcdCnt = 0; rcdCnt < Thoth.Call.count && rcdCnt < callCur.getCount(); rcdCnt++)
				{
					T_C_NUMBER = callCur.getString(callCur.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
					T_C_CONTACT = T_C_NUMBER;
					T_C_CONTACTNAME = "UNKNOWN";
					T_C_DATETIME = callCur.getLong(callCur.getColumnIndex(android.provider.CallLog.Calls.DATE));
					T_C_DURATION = callCur.getInt(callCur.getColumnIndex(android.provider.CallLog.Calls.DURATION));
					T_C_TYPE = callCur.getString(callCur.getColumnIndex(android.provider.CallLog.Calls.TYPE));
					switch (callCur.getInt(callCur.getColumnIndex(android.provider.CallLog.Calls.TYPE)))
					{
						case android.provider.CallLog.Calls.INCOMING_TYPE :
							T_C_TYPE = (T_C_DURATION == 0) ? "2" : "0";
							break;
						case android.provider.CallLog.Calls.MISSED_TYPE :
							T_C_TYPE = "3";
							break;
						case android.provider.CallLog.Calls.OUTGOING_TYPE :
							T_C_TYPE = (T_C_DURATION == 0) ? "4" : "1";
							break;
					}
					H_C_PKEY = MD5.getMD5HEX(T_C_NUMBER + T_C_CONTACT + T_C_DATETIME + T_C_DURATION + T_C_TYPE);
					// Write to DB.
					// Insert HASH in h_Call
					ContentValues newRow = new ContentValues();
					newRow.put(ThothDB.col_H_C_PKEY, H_C_PKEY);
					try
					{
						db.insertOrThrow(ThothDB.table_HASH_CALL, "", newRow);
					}
					catch (SQLException exp)
					{
						if (callCur.moveToNext())
							continue;
						else
							break;
					}
					// Insert record into t_Call
					newRow = new ContentValues();
					newRow.put(ThothDB.col_T_C_FKEY, H_C_PKEY);
					newRow.put(ThothDB.col_T_C_NUMBER, T_C_NUMBER);
					Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_C_NUMBER));
					Cursor c = null;
					try
					{
						c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
						if (null != c && c.moveToFirst())
						{
							T_C_CONTACT = c.getString(0);
							T_C_CONTACTNAME = c.getString(1);
						}
					}
					catch (Exception e)
					{}
					finally
					{
						if (null != c)
							c.close();
					}
					newRow.put(ThothDB.col_T_C_CONTACT, T_C_CONTACT);
					newRow.put(ThothDB.col_T_C_CONTACTNAME, T_C_CONTACTNAME);
					newRow.put(ThothDB.col_T_C_DATETIME, T_C_DATETIME);
					newRow.put(ThothDB.col_T_C_DURATION, T_C_DURATION);
					newRow.put(ThothDB.col_T_C_TYPE, T_C_TYPE);
					try
					{
						db.insertOrThrow(ThothDB.table_TEMP_CALL, "", newRow);
						if (Settings.APP_GoCAL || Settings.APP_GoCAL_CALL)
						{
							StringBuilder title = new StringBuilder();
							StringBuilder description = new StringBuilder();
							String common = (T_C_CONTACTNAME.equalsIgnoreCase(T_C_NUMBER)) ? T_C_CONTACTNAME : T_C_CONTACTNAME + " [" + T_C_NUMBER + "]";
							int h = T_C_DURATION / 3600, r = T_C_DURATION % 3600, m = r / 60, s = r % 60;
							String hr = (h < 10 ? "0" : "") + h, min = (m < 10 ? "0" : "") + m, sec = (s < 10 ? "0" : "") + s;
							StringBuilder sb = new StringBuilder();
							sb.append((hr != "") ? hr + "h " : "");
							sb.append((min != "") ? min + "m " : "");
							sb.append((sec != "") ? sec + "s " : "");
							switch (Integer.valueOf(T_C_TYPE))
							{
								case 0 :
									title.append("IN ").append(common);
									description.append("Incoming call from ").append(common).append("\n" + "Duration : ").append(sb.toString());
									break;
								case 1 :
									title.append("OUT ").append(common);
									description.append("Outgoing call to  ").append(common).append("\n" + "Duration : ").append(sb.toString());
									break;
								case 2 :
									title.append("REJECTED ").append(common);
									description.append("Rejected call from ").append(common);
									break;
								case 3 :
									title.append("MISSED ").append(common);
									description.append("Missed call from ").append(common);
									break;
								case 4 :
									title.append("NO-ANSWER ").append(common);
									description.append("No-answer for call to ").append(common);
									break;
							}
							updateGoogleCalendar(T_C_DATETIME, T_C_DURATION, title.toString(), description.toString(), 1);
						}
					}
					catch (SQLException e)
					{
						// ThothLog.e(e);
					}
					callCur.moveToNext();
				}
				if (db.isOpen())
					db.close();
				callCur.close();
				Thoth.Call.count = 0;
				Thoth.Call.isACTIVE = Thoth.Call.isOUTBOUND = false;
				Settings.APP_LAST_CALL_CAPTURE = System.currentTimeMillis();
				Settings.write();
				Thoth.clearNotifications();
			}
		});
	}
	
	void handleSMSCapture(final Intent intent)
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ContentResolver cr = getContentResolver();
					if (null != intent)
					{
						Bundle bundle = intent.getExtras();
						SmsMessage[] msgs = null;
						if (null != bundle)
						{
							Object[] pdus = (Object[]) bundle.get("pdus");
							msgs = new SmsMessage[pdus.length];
							long T_M_DATETIME;
							String H_M_PKEY;
							String T_M_NUMBER, T_M_CONTACT, T_M_CONTACTNAME, T_M_MSG, T_M_TYPE;
							//
							ThothDB dbA = new ThothDB(cx);
							SQLiteDatabase db = dbA.getDb();
							//
							for (int i = 0; i < msgs.length; i++)
							{
								msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
								T_M_NUMBER = msgs[i].getOriginatingAddress();
								T_M_CONTACT = T_M_NUMBER;
								T_M_CONTACTNAME = "UNKNOWN";
								T_M_DATETIME = msgs[i].getTimestampMillis();
								T_M_MSG = msgs[i].getMessageBody().toString();
								T_M_TYPE = "0";
								H_M_PKEY = MD5.getMD5HEX(T_M_NUMBER + T_M_CONTACT + T_M_DATETIME + T_M_MSG + T_M_TYPE);
								// Write to DB.
								// Insert HASH in h_Msgs
								ContentValues newRow = new ContentValues();
								newRow.put(ThothDB.col_H_M_PKEY, H_M_PKEY);
								try
								{
									db.insertOrThrow(ThothDB.table_HASH_MSGS, "", newRow);
								}
								catch (SQLException e)
								{
									// ThothLog.e(e);
									continue;
								}
								// Insert record into t_Msgs
								newRow = new ContentValues();
								newRow.put(ThothDB.col_T_M_FKEY, H_M_PKEY);
								newRow.put(ThothDB.col_T_M_NUMBER, T_M_NUMBER);
								Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_M_NUMBER));
								Cursor c = null;
								try
								{
									c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
									if (null != c && c.moveToFirst())
									{
										T_M_CONTACT = c.getString(0);
										T_M_CONTACTNAME = c.getString(1);
									}
								}
								catch (Exception e)
								{}
								finally
								{
									if (null != c)
										c.close();
								}
								newRow.put(ThothDB.col_T_M_CONTACT, T_M_CONTACT);
								newRow.put(ThothDB.col_T_M_CONTACTNAME, T_M_CONTACTNAME);
								newRow.put(ThothDB.col_T_M_DATETIME, T_M_DATETIME);
								newRow.put(ThothDB.col_T_M_MSG, T_M_MSG);
								newRow.put(ThothDB.col_T_M_TYPE, T_M_TYPE);
								newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_NUMBER);
								newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_NUMBER.getBytes());
								newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, "text/plain");
								try
								{
									dbA.getDb().insertOrThrow(ThothDB.table_TEMP_MSGS, "", newRow);
									if (Settings.APP_GoCAL || Settings.APP_GoCAL_MSG)
									{
										StringBuilder title = new StringBuilder();
										StringBuilder description = new StringBuilder();
										String common = (T_M_CONTACTNAME.equalsIgnoreCase(T_M_NUMBER)) ? T_M_CONTACTNAME : T_M_CONTACTNAME + " [" + T_M_NUMBER + "]";
										//
										title.append("IN SMS ").append(common);
										description.append("Incoming SMS from ").append(common).append("\n").append(T_M_MSG);
										//
										updateGoogleCalendar(T_M_DATETIME, 0l, title.toString(), description.toString(), 2);
									}
								}
								catch (SQLException e)
								{
									// ThothLog.e(e);
								}
							}
							if (db.isOpen())
								db.close();
						}
					}
					// SENT MESSAGES
					long T_M_DATETIME;
					String H_M_PKEY;
					String T_M_NUMBER, T_M_CONTACT, T_M_CONTACTNAME, T_M_MSG, T_M_TYPE;
					Cursor smsCur = cr.query(Uri.parse("content://sms/sent"), null, null, null, null);
					if (null == smsCur || !smsCur.moveToFirst())
						return;
					if (smsCur.getCount() > 0)
					{
						ThothDB dbA = new ThothDB(cx);
						SQLiteDatabase db = dbA.getDb();
						//
						for (int i = 0; i < smsCur.getCount(); i++)
						{
							T_M_NUMBER = smsCur.getString(smsCur.getColumnIndexOrThrow("address")).toString();
							T_M_CONTACT = T_M_NUMBER;
							T_M_CONTACTNAME = "UNKNOWN";
							T_M_DATETIME = smsCur.getLong(smsCur.getColumnIndexOrThrow("date"));
							T_M_MSG = smsCur.getString(smsCur.getColumnIndexOrThrow("body")).toString();
							T_M_TYPE = "1";
							H_M_PKEY = MD5.getMD5HEX(T_M_NUMBER + (String) T_M_CONTACT + T_M_DATETIME + T_M_MSG + T_M_TYPE);
							// Write to DB.
							// Insert HASH in h_Msgs
							ContentValues newRow = new ContentValues();
							newRow.put(ThothDB.col_H_M_PKEY, H_M_PKEY);
							try
							{
								db.insertOrThrow(ThothDB.table_HASH_MSGS, "", newRow);
							}
							catch (SQLException e)
							{
								// ThothLog.e(e);
								if (smsCur.moveToNext())
									continue;
								else
									break;
							}
							// Insert record into t_Msgs
							newRow = new ContentValues();
							newRow.put(ThothDB.col_T_M_FKEY, H_M_PKEY);
							newRow.put(ThothDB.col_T_M_NUMBER, T_M_NUMBER);
							Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_M_NUMBER));
							Cursor c = null;
							try
							{
								c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
								if (null != c && c.moveToFirst())
								{
									T_M_CONTACT = c.getString(0);
									T_M_CONTACTNAME = c.getString(1);
								}
							}
							catch (Exception e)
							{}
							finally
							{
								if (null != c)
									c.close();
							}
							newRow.put(ThothDB.col_T_M_CONTACT, T_M_CONTACT);
							newRow.put(ThothDB.col_T_M_CONTACTNAME, T_M_CONTACTNAME);
							newRow.put(ThothDB.col_T_M_DATETIME, T_M_DATETIME);
							newRow.put(ThothDB.col_T_M_MSG, T_M_MSG);
							newRow.put(ThothDB.col_T_M_TYPE, T_M_TYPE);
							newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_NUMBER);
							newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_NUMBER.getBytes());
							newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, "text/plain");
							try
							{
								db.insertOrThrow(ThothDB.table_TEMP_MSGS, "", newRow);
								if (Settings.APP_GoCAL || Settings.APP_GoCAL_MSG)
								{
									StringBuilder title = new StringBuilder();
									StringBuilder description = new StringBuilder();
									String common = (T_M_CONTACTNAME.equalsIgnoreCase(T_M_NUMBER)) ? T_M_CONTACTNAME : T_M_CONTACTNAME + " [" + T_M_NUMBER + "]";
									//
									title.append("OUT SMS ").append(common);
									description.append("Outgoing SMS to ").append(common).append("\n").append(T_M_MSG);
									//
									updateGoogleCalendar(T_M_DATETIME, 0l, title.toString(), description.toString(), 2);
								}
							}
							catch (SQLException e)
							{
								// ThothLog.e(e);
							}
							smsCur.moveToNext();
						}
						if (db.isOpen())
							db.close();
					}
					smsCur.close();
					Settings.APP_LAST_MSGS_CAPTURE = System.currentTimeMillis();
					Settings.write();
					Thoth.clearNotifications();
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
					Thoth.clearNotifications();
				}
			}
		});
	}
	
	void handleMMSCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ContentResolver cr = getContentResolver();
					Cursor mmsCur = cr.query(Uri.parse("content://mms/"), null, null, null, "_id");
					if (null == mmsCur || !mmsCur.moveToFirst())
						return;
					if (mmsCur.getCount() > 0)
					{
						ThothDB dbA = new ThothDB(cx);
						SQLiteDatabase db = dbA.getDb();
						//
						for (int z = 0; z < mmsCur.getCount(); z++)
						{
							long T_M_DATETIME = 0;
							String H_M_PKEY = "", T_M_NUMBER = "", T_M_CONTACT = "", T_M_CONTACTNAME = "", T_M_MSG = "", T_M_TYPE = "", T_M_BLOB_TEXT = "";
							byte[] T_M_BLOB_DATA = null;
							String T_M_BLOB_DATA_MIME = "";
							int id = Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("_id")));
							T_M_DATETIME = 1000 * mmsCur.getLong(mmsCur.getColumnIndex("date"));
							Cursor addrCur = cr.query(Uri.parse("content://mms/" + id + "/addr"), null, "type=151", null, "_id");
							if (addrCur != null && addrCur.getCount() > 0 && addrCur.moveToFirst())
								T_M_NUMBER = addrCur.getString(addrCur.getColumnIndex("address"));
							if (T_M_NUMBER != "")
								T_M_CONTACT = T_M_NUMBER;
							else
								T_M_CONTACT = T_M_NUMBER = Settings.USR_DEVICE_PHONE_NUMBER;
							addrCur.close();
							T_M_CONTACTNAME = "UNKNOWN";
							T_M_MSG = mmsCur.getString(mmsCur.getColumnIndex("sub"));
							T_M_TYPE = (Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("m_type"))) == 128) ? "3" : "2";
							H_M_PKEY = MD5.getMD5HEX(id + T_M_NUMBER + T_M_CONTACT + T_M_DATETIME + T_M_MSG + T_M_TYPE);
							// Write to DB.
							// Insert HASH in h_Msgs
							ContentValues newRow = new ContentValues();
							newRow.put(ThothDB.col_H_M_PKEY, H_M_PKEY);
							try
							{
								db.insertOrThrow(ThothDB.table_HASH_MSGS, "", newRow);
							}
							catch (SQLException e)
							{
								// ThothLog.e(e);
								if (mmsCur.moveToNext())
									continue;
								else
									break;
							}
							// Get MMS message parts
							Cursor curPart = cr.query(Uri.parse("content://mms/part"), null, "mid = " + id, null, "_id");
							if (curPart.getCount() > 0 && curPart.moveToFirst())
							{
								for (int y = 0; y < curPart.getCount(); y++)
								{
									String mime = curPart.getString(curPart.getColumnIndex("ct"));
									String prtId = curPart.getString(curPart.getColumnIndex("_id"));
									if (mime.equalsIgnoreCase("text/plain"))
									{
										byte[] messageData = readMMSPart(prtId);
										if (messageData != null && messageData.length > 0)
											T_M_BLOB_TEXT = new String(messageData);
										if (T_M_BLOB_TEXT == "")
										{
											Cursor txtPartCur = cr.query(Uri.parse("content://mms/part"), null, "mid = " + id + " and _id =" + prtId, null, "_id");
											txtPartCur.moveToLast();
											T_M_BLOB_TEXT = txtPartCur.getString(txtPartCur.getColumnIndex("text"));
											if (T_M_MSG.length() == 0 && T_M_BLOB_TEXT != null)
												T_M_MSG = T_M_BLOB_TEXT;
										}
									}
									else
									{
										T_M_BLOB_DATA = readMMSPart(prtId);
										T_M_BLOB_DATA_MIME = mime;
									}
									curPart.moveToNext();
								}
							}
							newRow = new ContentValues();
							newRow.put(ThothDB.col_T_M_FKEY, H_M_PKEY);
							newRow.put(ThothDB.col_T_M_NUMBER, T_M_NUMBER);
							Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_M_NUMBER));
							Cursor c = null;
							try
							{
								c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
								if (null != c && c.moveToFirst())
								{
									T_M_CONTACT = c.getString(0);
									T_M_CONTACTNAME = c.getString(1);
								}
							}
							catch (Exception e)
							{}
							finally
							{
								if (null != c)
									c.close();
							}
							newRow.put(ThothDB.col_T_M_CONTACT, T_M_CONTACT);
							newRow.put(ThothDB.col_T_M_CONTACTNAME, T_M_CONTACTNAME);
							newRow.put(ThothDB.col_T_M_DATETIME, T_M_DATETIME);
							if (T_M_MSG != null)
								newRow.put(ThothDB.col_T_M_MSG, T_M_MSG);
							else
								newRow.put(ThothDB.col_T_M_MSG, T_M_NUMBER);
							newRow.put(ThothDB.col_T_M_TYPE, T_M_TYPE);
							if (T_M_BLOB_TEXT != null)
								newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_BLOB_TEXT);
							else
								newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_NUMBER);
							if (T_M_BLOB_DATA != null)
								newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_BLOB_DATA);
							else
								newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_NUMBER.getBytes());
							if (T_M_BLOB_DATA_MIME != "")
								newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, T_M_BLOB_DATA_MIME);
							else
								newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, "text/plain");
							try
							{
								db.insertOrThrow(ThothDB.table_TEMP_MSGS, "", newRow);
								if (Settings.APP_GoCAL || Settings.APP_GoCAL_MSG)
								{
									StringBuilder title = new StringBuilder();
									StringBuilder description = new StringBuilder();
									String common = (T_M_CONTACTNAME.equalsIgnoreCase(T_M_NUMBER)) ? T_M_CONTACTNAME : T_M_CONTACTNAME + " [" + T_M_NUMBER + "]";
									//
									if (T_M_TYPE.equalsIgnoreCase("2"))
									{
										title.append("IN MMS ").append(common);
										description.append("Incoming MMS from ").append(common).append("\n").append(T_M_MSG);
									}
									else
									{
										title.append("OUT MMS ").append(common);
										description.append("Outgoing MMS to ").append(common).append("\n").append(T_M_MSG);
									}
									//
									updateGoogleCalendar(T_M_DATETIME, 0l, title.toString(), description.toString(), 2);
								}
							}
							catch (SQLException e)
							{
								// ThothLog.e(e);
							}
							mmsCur.moveToNext();
						}
						if (db.isOpen())
							db.close();
					}
					mmsCur.close();
					Settings.APP_LAST_MSGM_CAPTURE = System.currentTimeMillis();
					Settings.write();
					Thoth.clearNotifications();
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
					Thoth.clearNotifications();
				}
			}
		});
	}
	
	void handleDataCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				long _DAYID, _BOOTID, _UPLOAD, _DOWNLOAD, _TIME;
				_DAYID = Thoth.Data.MOBILE_CURR_DAY_ID;
				_BOOTID = Thoth.Data.MOBILE_NWK_RESET;
				_UPLOAD = NetworkStatistics.getMTBytes();
				_DOWNLOAD = NetworkStatistics.getMRBytes();
				_TIME = System.currentTimeMillis();
				if (Thoth.Data.MOBILE_DAILY_CORRECTION)
				{
					Thoth.Data.MOBILE_DAILY_CORRECTION = false;
					Thoth.Data.MOBILE_DAILY_UL_CORRECTION = _UPLOAD;
					Thoth.Data.MOBILE_DAILY_DL_CORRECTION = _DOWNLOAD;
					if (Thoth.Data.MOBILE_DAILY_UL_CORRECTION >= Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV)
					{
						_UPLOAD = Thoth.Data.MOBILE_DAILY_UL_CORRECTION - Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV;
						Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV += _UPLOAD;
					}
					else
						_UPLOAD = Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
					if (Thoth.Data.MOBILE_DAILY_DL_CORRECTION >= Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV)
					{
						_DOWNLOAD = Thoth.Data.MOBILE_DAILY_DL_CORRECTION - Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV;
						Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV += _DOWNLOAD;
					}
					else
						_DOWNLOAD = Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
					Thoth.Data.write();
				}
				if (Thoth.Data.MOBILE_NEW_DAY && Thoth.Data.MOBILE_CURR_DAY_ID != Thoth.Data.MOBILE_PREV_DAY_ID)
				{
					Thoth.Data.MOBILE_NEW_DAY = false;
					Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV = Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
					Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV = Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
					_UPLOAD -= Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
					_DOWNLOAD -= Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
				}
				_UPLOAD = _UPLOAD < 0 ? 0 : _UPLOAD;
				_DOWNLOAD = _DOWNLOAD < 0 ? 0 : _DOWNLOAD;
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				ContentValues newRow = new ContentValues();
				newRow.put(ThothDB.col_T_D_M_PKEY, _DAYID + _BOOTID);
				newRow.put(ThothDB.col_T_D_M_DAYID, _DAYID);
				newRow.put(ThothDB.col_T_D_M_BOOTID, _BOOTID);
				newRow.put(ThothDB.col_T_D_M_UPLOAD, _UPLOAD);
				newRow.put(ThothDB.col_T_D_M_DOWNLOAD, _DOWNLOAD);
				newRow.put(ThothDB.col_T_D_M_TIME, _TIME);
				try
				{
					db.insertOrThrow(ThothDB.table_TEMP_DATA_MOBL, "", newRow);
				}
				catch (SQLiteConstraintException e)
				{
					try
					{
						db.replaceOrThrow(ThothDB.table_TEMP_DATA_MOBL, "", newRow);
					}
					catch (Exception ee)
					{
						// ThothLog.e(ee);
						Thoth.clearNotifications();
					}
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
					Thoth.clearNotifications();
				}
				if (db.isOpen())
					db.close();
			}
		});
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				long _DAYID, _BOOTID, _UPLOAD, _DOWNLOAD, _TIME;
				_DAYID = Thoth.Data.WIFI_CURR_DAY_ID;
				_BOOTID = Thoth.Data.WIFI_NWK_RESET;
				_UPLOAD = NetworkStatistics.getTTBytes() - NetworkStatistics.getMTBytes();
				_DOWNLOAD = NetworkStatistics.getTRBytes() - NetworkStatistics.getMRBytes();
				_TIME = System.currentTimeMillis();
				if (Thoth.Data.WIFI_DAILY_CORRECTION)
				{
					Thoth.Data.WIFI_DAILY_CORRECTION = false;
					Thoth.Data.WIFI_DAILY_UL_CORRECTION = _UPLOAD;
					Thoth.Data.WIFI_DAILY_DL_CORRECTION = _DOWNLOAD;
					if (Thoth.Data.WIFI_DAILY_UL_CORRECTION >= Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV)
					{
						_UPLOAD = Thoth.Data.WIFI_DAILY_UL_CORRECTION - Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV;
						Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV += _UPLOAD;
					}
					else
						_UPLOAD = Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					if (Thoth.Data.WIFI_DAILY_DL_CORRECTION >= Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV)
					{
						_DOWNLOAD = Thoth.Data.WIFI_DAILY_DL_CORRECTION - Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV;
						Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV += _DOWNLOAD;
					}
					else
						_DOWNLOAD = Thoth.Data.WIFI_DAILY_DL_CORRECTION;
					Thoth.Data.WIFI_INTRADAY_UL_CORRECTION = Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV = 0;
					Thoth.Data.WIFI_INTRADAY_DL_CORRECTION = Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV = 0;
					Thoth.Data.write();
				}
				if (Thoth.Data.WIFI_INTRADAY_CORRECTION)
				{
					Thoth.Data.WIFI_INTRADAY_CORRECTION = false;
					Thoth.Data.WIFI_INTRADAY_UL_CORRECTION = _UPLOAD - Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					Thoth.Data.WIFI_INTRADAY_DL_CORRECTION = _DOWNLOAD - Thoth.Data.WIFI_DAILY_DL_CORRECTION;
					if (Thoth.Data.WIFI_INTRADAY_UL_CORRECTION > Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV)
					{
						_UPLOAD = Thoth.Data.WIFI_INTRADAY_UL_CORRECTION - Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV;
						Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV += _UPLOAD;
					}
					else
						_UPLOAD = 0;
					if (Thoth.Data.WIFI_INTRADAY_DL_CORRECTION > Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV)
					{
						_DOWNLOAD = Thoth.Data.WIFI_INTRADAY_DL_CORRECTION - Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV;
						Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV += _DOWNLOAD;
					}
					else
						_DOWNLOAD = 0;
					Thoth.Data.write();
				}
				if (Thoth.Data.WIFI_NEW_DAY && Thoth.Data.WIFI_CURR_DAY_ID != Thoth.Data.WIFI_PREV_DAY_ID)
				{
					Thoth.Data.WIFI_NEW_DAY = false;
					Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV = Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV = Thoth.Data.WIFI_DAILY_DL_CORRECTION;
					_UPLOAD -= Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					_DOWNLOAD -= Thoth.Data.WIFI_DAILY_DL_CORRECTION;
				}
				_UPLOAD = _UPLOAD < 0 ? 0 : _UPLOAD;
				_DOWNLOAD = _DOWNLOAD < 0 ? 0 : _DOWNLOAD;
				if (_UPLOAD < 1 || _DOWNLOAD < 1)
					_BOOTID = Thoth.Data.WIFI_NWK_RESET = System.currentTimeMillis();
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				ContentValues newRow = new ContentValues();
				newRow.put(ThothDB.col_T_D_W_PKEY, _DAYID + _BOOTID);
				newRow.put(ThothDB.col_T_D_W_DAYID, _DAYID);
				newRow.put(ThothDB.col_T_D_W_BOOTID, _BOOTID);
				newRow.put(ThothDB.col_T_D_W_UPLOAD, _UPLOAD);
				newRow.put(ThothDB.col_T_D_W_DOWNLOAD, _DOWNLOAD);
				newRow.put(ThothDB.col_T_D_W_TIME, _TIME);
				try
				{
					db.insertOrThrow(ThothDB.table_TEMP_DATA_WIFI, "", newRow);
				}
				catch (SQLiteConstraintException e)
				{
					try
					{
						db.replaceOrThrow(ThothDB.table_TEMP_DATA_WIFI, "", newRow);
					}
					catch (Exception ee)
					{
						// ThothLog.e(ee);
						Thoth.clearNotifications();
					}
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
					Thoth.clearNotifications();
				}
				if (db.isOpen())
					db.close();
			}
		});
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final PackageManager pkgMgr = getPackageManager();
					//
					ThothDB dbA = new ThothDB(cx);
					SQLiteDatabase db = dbA.getDb();
					//
					List<PackageInfo> appList = pkgMgr.getInstalledPackages(0);
					for (PackageInfo app : appList)
					{
						if (!((app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0))
						{
							int appUID = app.applicationInfo.uid;
							String appName = app.applicationInfo.loadLabel(getPackageManager()).toString();
							String appPackage = app.packageName;
							String appUIDName = pkgMgr.getNameForUid(appUID);
							long appUpload = NetworkStatistics.getATBytes(appUID);
							long appDownload = NetworkStatistics.getARBytes(appUID);
							long appDayId = Thoth.Data.CURR_DAY_ID;
							long appBootId = Thoth.Data.DATA_RESET;
							ContentValues newRow = new ContentValues();
							newRow.put(ThothDB.col_H_A_PKEY, appPackage);
							try
							{
								db.insertOrThrow(ThothDB.table_HASH_APPS, "", newRow);
							}
							catch (SQLiteConstraintException e)
							{}
							catch (Exception e)
							{
								// ThothLog.e(e);
							}
							long UL_CORRECTION = appUpload, UL_CORRECTION_PREV = 0, DL_CORRECTION = appDownload, DL_CORRECTION_PREV = 0;
							Cursor cR = db.rawQuery("SELECT SUM(" + ThothDB.col_T_A_UPLOAD + "), SUM(" + ThothDB.col_T_A_DOWNLOAD + ") FROM " + ThothDB.table_TEMP_APPS + " WHERE " + ThothDB.col_T_A_PKG + "='" + appPackage + "' AND " + ThothDB.col_T_A_DAYID + "=" + Thoth.Data.PREV_DAY_ID + " AND " + ThothDB.col_T_A_BOOTID + "=" + appBootId + " GROUP BY " + ThothDB.col_T_A_DAYID, null);
							if (null != cR && cR.moveToFirst())
							{
								UL_CORRECTION_PREV = cR.getLong(0);
								DL_CORRECTION_PREV = cR.getLong(1);
								cR.close();
							}
							appUpload = (UL_CORRECTION >= UL_CORRECTION_PREV) ? (UL_CORRECTION - UL_CORRECTION_PREV) : UL_CORRECTION;
							appDownload = (DL_CORRECTION >= DL_CORRECTION_PREV) ? (DL_CORRECTION - DL_CORRECTION_PREV) : DL_CORRECTION;
							if (appUpload < 1 && appDownload < 1)
								continue;
							newRow = new ContentValues();
							newRow.put(ThothDB.col_T_A_PKG, appPackage);
							newRow.put(ThothDB.col_T_A_NAME, appName);
							newRow.put(ThothDB.col_T_A_UID, appUID);
							newRow.put(ThothDB.col_T_A_UID_NAME, appUIDName);
							newRow.put(ThothDB.col_T_A_DAYID, appDayId);
							newRow.put(ThothDB.col_T_A_BOOTID, appBootId);
							newRow.put(ThothDB.col_T_A_UPLOAD, appUpload);
							newRow.put(ThothDB.col_T_A_DOWNLOAD, appDownload);
							try
							{
								db.insertOrThrow(ThothDB.table_TEMP_APPS, "", newRow);
							}
							catch (Exception e)
							{
								// ThothLog.e(e);
							}
						}
					}
					if (db.isOpen())
						db.close();
					Settings.APP_LAST_DATA_CAPTURE = System.currentTimeMillis();
					Settings.write();
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
					Thoth.clearNotifications();
				}
			}
		});
	}
	
	void setupCallCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				ContentResolver cr = getContentResolver();
				Cursor callCur = cr.query(android.provider.CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");
				if (callCur == null || !callCur.moveToFirst())
				{
					Message msg = new Message();
					msg.what = 666;
					msg.arg1 = Thoth.REQ.CALL_CAPTURE;
					sHandler.sendMessage(msg);
					Settings.APP_LAST_CALL_CAPTURE = System.currentTimeMillis();
					Settings.write();
					SetupMain.getInstance().SETUP.sendEmptyMessage(51);
					return;
				}
				int T_C_DURATION;
				long T_C_DATETIME;
				String H_C_PKEY;
				String T_C_NUMBER, T_C_CONTACT, T_C_CONTACTNAME, T_C_TYPE;
				SetupMain.sPbMax += callCur.getCount();
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				// for (int k = 0; k < callCur.getCount(); k++)
				for (int k = 0; k < ((callCur.getCount()) > 100 ? 100 : callCur.getCount()); k++)
				{
					SetupMain.sPbCurrent++;
					Message msg = new Message();
					msg.what = 55;
					msg.arg1 = 0;
					msg.obj = new String("CALL logs (" + (k + 1) + "/" + ((callCur.getCount()) > 100 ? 100 : callCur.getCount()) + ")");
					// msg.obj = new String("CALL logs (" + (k + 1) + "/" + callCur.getCount() + ")");
					SetupMain.getInstance().SETUP.sendMessage(msg);
					T_C_NUMBER = callCur.getString(callCur.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
					T_C_CONTACT = T_C_NUMBER;
					T_C_CONTACTNAME = "UNKNOWN";
					T_C_DATETIME = callCur.getLong(callCur.getColumnIndex(android.provider.CallLog.Calls.DATE));
					T_C_DURATION = callCur.getInt(callCur.getColumnIndex(android.provider.CallLog.Calls.DURATION));
					T_C_TYPE = callCur.getString(callCur.getColumnIndex(android.provider.CallLog.Calls.TYPE));
					switch (callCur.getInt(callCur.getColumnIndex(android.provider.CallLog.Calls.TYPE)))
					{
						case android.provider.CallLog.Calls.INCOMING_TYPE :
							T_C_TYPE = (T_C_DURATION == 0) ? "2" : "0";
							break;
						case android.provider.CallLog.Calls.MISSED_TYPE :
							T_C_TYPE = "3";
							break;
						case android.provider.CallLog.Calls.OUTGOING_TYPE :
							T_C_TYPE = (T_C_DURATION == 0) ? "4" : "1";
							break;
					}
					H_C_PKEY = T_C_NUMBER + T_C_CONTACT + T_C_DATETIME + T_C_DURATION + T_C_TYPE;
					// Write to DB.
					// Insert HASH in h_Call
					ContentValues newRow = new ContentValues();
					newRow.put(ThothDB.col_H_C_PKEY, H_C_PKEY);
					try
					{
						db.insertOrThrow(ThothDB.table_HASH_CALL, "", newRow);
					}
					catch (SQLException exp)
					{
						// ThothLog.e(e);
						if (callCur.moveToNext())
							continue;
						else
							break;
					}
					// Insert record into t_Call
					newRow = new ContentValues();
					newRow.put(ThothDB.col_T_C_FKEY, H_C_PKEY);
					newRow.put(ThothDB.col_T_C_NUMBER, T_C_NUMBER);
					Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_C_NUMBER));
					Cursor c = null;
					try
					{
						c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
						if (null != c && c.moveToFirst())
						{
							T_C_CONTACT = c.getString(0);
							T_C_CONTACTNAME = c.getString(1);
						}
					}
					catch (Exception e)
					{}
					finally
					{
						if (null != c)
							c.close();
					}
					newRow.put(ThothDB.col_T_C_CONTACT, T_C_CONTACT);
					newRow.put(ThothDB.col_T_C_CONTACTNAME, T_C_CONTACTNAME);
					newRow.put(ThothDB.col_T_C_DATETIME, T_C_DATETIME);
					newRow.put(ThothDB.col_T_C_DURATION, T_C_DURATION);
					newRow.put(ThothDB.col_T_C_TYPE, T_C_TYPE);
					try
					{
						db.insertOrThrow(ThothDB.table_TEMP_CALL, "", newRow);
					}
					catch (SQLException e)
					{
						// ThothLog.e(e);
					}
					callCur.moveToNext();
				}
				if (db.isOpen())
					db.close();
				callCur.close();
				Settings.APP_LAST_CALL_CAPTURE = System.currentTimeMillis();
				Settings.write();
				SetupMain.getInstance().SETUP.sendEmptyMessage(51);
			}
		});
	}
	
	void setupSMSCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				float T_M_DATETIME;
				String H_M_PKEY;
				String T_M_NUMBER, T_M_CONTACT, T_M_CONTACTNAME, T_M_MSG, T_M_TYPE;
				ContentResolver cr = getContentResolver();
				Cursor smsCur = cr.query(Uri.parse("content://sms"), null, null, null, null);
				if (!smsCur.moveToFirst())
				{
					Message msg = new Message();
					msg.what = 666;
					msg.arg1 = Thoth.REQ.MSGS_CAPTURE;
					sHandler.sendMessage(msg);
					Settings.APP_LAST_MSGS_CAPTURE = System.currentTimeMillis();
					Settings.write();
					SetupMain.getInstance().SETUP.sendEmptyMessage(52);
					return;
				}
				SetupMain.sPbMax += smsCur.getCount();
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				for (int i = 0; i < smsCur.getCount(); i++)
				// for (int i = 0; i < ((smsCur.getCount() > 30) ? 30 : smsCur.getCount()); i++)
				{
					SetupMain.sPbCurrent++;
					Message msg = new Message();
					msg.what = 55;
					msg.arg1 = 1;
					msg.obj = new String("SMS logs (" + (i + 1) + "/" + ((smsCur.getCount() > 30) ? 30 : smsCur.getCount()) + ")");
					// msg.obj = new String("SMS logs (" + (i + 1) + "/" + smsCur.getCount() + ")");
					SetupMain.getInstance().SETUP.sendMessage(msg);
					T_M_NUMBER = smsCur.getString(smsCur.getColumnIndexOrThrow("address")).toString();
					T_M_CONTACT = T_M_NUMBER;
					T_M_CONTACTNAME = "UNKNOWN";
					T_M_DATETIME = smsCur.getFloat(smsCur.getColumnIndexOrThrow("date"));
					T_M_MSG = smsCur.getString(smsCur.getColumnIndexOrThrow("body")).toString();
					T_M_TYPE = smsCur.getString(smsCur.getColumnIndexOrThrow("type")).toString();
					if (Integer.parseInt(T_M_TYPE) == 1 || Integer.parseInt(T_M_TYPE) == 2)
					{
						T_M_TYPE = (Integer.parseInt(T_M_TYPE) == 1) ? "0" : "1";
						H_M_PKEY = MD5.getMD5HEX(T_M_NUMBER + T_M_CONTACT + T_M_DATETIME + T_M_MSG + T_M_TYPE);
						// Write to DB.
						// Insert HASH in h_Msgs
						ContentValues newRow = new ContentValues();
						newRow.put(ThothDB.col_H_M_PKEY, H_M_PKEY);
						try
						{
							db.insertOrThrow(ThothDB.table_HASH_MSGS, "", newRow);
						}
						catch (SQLException exp)
						{
							// ThothLog.e(e);
							if (smsCur.moveToNext())
								continue;
							else
								break;
						}
						// Insert record into t_Msgs
						newRow = new ContentValues();
						newRow.put(ThothDB.col_T_M_FKEY, H_M_PKEY);
						newRow.put(ThothDB.col_T_M_NUMBER, T_M_NUMBER);
						Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_M_NUMBER));
						Cursor c = null;
						try
						{
							c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
							if (null != c && c.moveToFirst())
							{
								T_M_CONTACT = c.getString(0);
								T_M_CONTACTNAME = c.getString(1);
							}
						}
						catch (Exception e)
						{}
						finally
						{
							if (null != c)
								c.close();
						}
						newRow.put(ThothDB.col_T_M_CONTACT, T_M_CONTACT);
						newRow.put(ThothDB.col_T_M_CONTACTNAME, T_M_CONTACTNAME);
						newRow.put(ThothDB.col_T_M_DATETIME, T_M_DATETIME);
						newRow.put(ThothDB.col_T_M_MSG, T_M_MSG);
						newRow.put(ThothDB.col_T_M_TYPE, T_M_TYPE);
						newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_NUMBER);
						newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_NUMBER.getBytes());
						newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, "text/plain");
						try
						{
							db.insertOrThrow(ThothDB.table_TEMP_MSGS, "", newRow);
						}
						catch (SQLException e)
						{
							// ThothLog.e(e);
						}
						catch (Exception e)
						{
							// ThothLog.e(e);
						}
						smsCur.moveToNext();
					}
					else
					{
						smsCur.moveToNext();
						continue;
					}
				}
				if (db.isOpen())
					db.close();
				smsCur.close();
				Settings.APP_LAST_MSGS_CAPTURE = System.currentTimeMillis();
				Settings.write();
				SetupMain.getInstance().SETUP.sendEmptyMessage(52);
			}
		});
	}
	
	void setupMMSCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ContentResolver cr = getContentResolver();
					Cursor mmsCur = cr.query(Uri.parse("content://mms/"), null, null, null, "_id");
					if (!mmsCur.moveToFirst())
					{
						Message msg = new Message();
						msg.what = 666;
						msg.arg1 = Thoth.REQ.MSGM_CAPTURE;
						sHandler.sendMessage(msg);
						Settings.APP_LAST_MSGM_CAPTURE = System.currentTimeMillis();
						Settings.write();
						SetupMain.getInstance().SETUP.sendEmptyMessage(53);
						return;
					}
					SetupMain.sPbMax += mmsCur.getCount();
					//
					ThothDB dbA = new ThothDB(cx);
					SQLiteDatabase db = dbA.getDb();
					//
					// for (int z = 0; z < mmsCur.getCount(); z++)
					for (int z = 0; z < ((mmsCur.getCount() > 5) ? 5 : mmsCur.getCount()); z++)
					{
						SetupMain.sPbCurrent++;
						Message msg = new Message();
						msg.what = 55;
						msg.arg1 = 2;
						msg.obj = new String("MMS logs (" + (z + 1) + "/" + ((mmsCur.getCount()) > 100 ? 100 : mmsCur.getCount()) + ")");
						// msg.obj = new String("MMS logs (" + (z + 1) + "/" + mmsCur.getCount() + ")");
						SetupMain.getInstance().SETUP.sendMessage(msg);
						float T_M_DATETIME = 0;
						String H_M_PKEY = "", T_M_NUMBER = "", T_M_CONTACT = "", T_M_CONTACTNAME = "", T_M_MSG = "", T_M_TYPE = "", T_M_BLOB_TEXT = "";
						byte[] T_M_BLOB_DATA = new byte[0];
						String T_M_BLOB_DATA_MIME = "";
						int id = Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("_id")));
						T_M_DATETIME = 1000 * mmsCur.getLong(mmsCur.getColumnIndex("date"));
						Cursor addrCur = cr.query(Uri.parse("content://mms/" + id + "/addr"), null, "type=151", null, "_id");
						if (addrCur != null && addrCur.getCount() > 0 && addrCur.moveToFirst())
							T_M_NUMBER = addrCur.getString(addrCur.getColumnIndex("address"));
						if (T_M_NUMBER != "")
							T_M_CONTACT = T_M_NUMBER;
						else
							T_M_CONTACT = T_M_NUMBER = Settings.USR_DEVICE_PHONE_NUMBER;
						addrCur.close();
						T_M_CONTACTNAME = "UNKNOWN";
						T_M_MSG = mmsCur.getString(mmsCur.getColumnIndex("sub"));
						T_M_TYPE = (Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("m_type"))) == 128) ? "3" : "2";
						H_M_PKEY = MD5.getMD5HEX(id + T_M_NUMBER + T_M_CONTACT + T_M_DATETIME + T_M_MSG + T_M_TYPE);
						// Write to DB.
						// Insert HASH in h_Msgs
						ContentValues newRow = new ContentValues();
						newRow.put(ThothDB.col_H_M_PKEY, H_M_PKEY);
						try
						{
							db.insertOrThrow(ThothDB.table_HASH_MSGS, "", newRow);
						}
						catch (SQLException e)
						{
							// ThothLog.e(e);
							if (mmsCur.moveToNext())
								continue;
							else
								break;
						}
						// Get MMS message parts
						Cursor curPart = cr.query(Uri.parse("content://mms/part"), null, "mid = " + id, null, "_id");
						if (curPart.getCount() > 0 && curPart.moveToFirst())
						{
							for (int y = 0; y < curPart.getCount(); y++)
							{
								String mime = curPart.getString(curPart.getColumnIndex("ct"));
								String prtId = curPart.getString(curPart.getColumnIndex("_id"));
								if (mime.equalsIgnoreCase("text/plain"))
								{
									byte[] messageData = readMMSPart(prtId);
									if (messageData != null && messageData.length > 0)
										T_M_BLOB_TEXT = new String(messageData);
									if (T_M_BLOB_TEXT == "")
									{
										Cursor txtPartCur = cr.query(Uri.parse("content://mms/part"), null, "mid = " + id + " and _id =" + prtId, null, "_id");
										txtPartCur.moveToLast();
										T_M_BLOB_TEXT = txtPartCur.getString(txtPartCur.getColumnIndex("text"));
										if (T_M_MSG.length() == 0 && T_M_BLOB_TEXT != null)
											T_M_MSG = T_M_BLOB_TEXT;
									}
								}
								else
								{
									T_M_BLOB_DATA = readMMSPart(prtId);
									T_M_BLOB_DATA_MIME = mime;
								}
								curPart.moveToNext();
							}
						}
						newRow = new ContentValues();
						newRow.put(ThothDB.col_T_M_FKEY, H_M_PKEY);
						newRow.put(ThothDB.col_T_M_NUMBER, T_M_NUMBER);
						Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_M_NUMBER));
						Cursor c = null;
						try
						{
							c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
							if (null != c && c.moveToFirst())
							{
								T_M_CONTACT = c.getString(0);
								T_M_CONTACTNAME = c.getString(1);
							}
						}
						catch (Exception e)
						{}
						finally
						{
							if (null != c)
								c.close();
						}
						newRow.put(ThothDB.col_T_M_CONTACT, T_M_CONTACT);
						newRow.put(ThothDB.col_T_M_CONTACTNAME, T_M_CONTACTNAME);
						newRow.put(ThothDB.col_T_M_DATETIME, T_M_DATETIME);
						if (T_M_MSG != null)
							newRow.put(ThothDB.col_T_M_MSG, T_M_MSG);
						else
							newRow.put(ThothDB.col_T_M_MSG, T_M_NUMBER);
						newRow.put(ThothDB.col_T_M_TYPE, T_M_TYPE);
						if (T_M_BLOB_TEXT != null)
							newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_BLOB_TEXT);
						else
							newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_NUMBER);
						if (T_M_BLOB_DATA != null)
							newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_BLOB_DATA);
						else
							newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_NUMBER.getBytes());
						if (T_M_BLOB_DATA_MIME != "")
							newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, T_M_BLOB_DATA_MIME);
						else
							newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, "text/plain");
						try
						{
							db.insertOrThrow(ThothDB.table_TEMP_MSGS, "", newRow);
						}
						catch (SQLException e)
						{
							// ThothLog.e(e);
						}
						mmsCur.moveToNext();
					}
					if (db.isOpen())
						db.close();
					mmsCur.close();
					Settings.APP_LAST_MSGM_CAPTURE = System.currentTimeMillis();
					Settings.write();
					SetupMain.getInstance().SETUP.sendEmptyMessage(53);
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
				}
			}
		});
	}
	
	void setupDataCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				long _DAYID, _BOOTID, _UPLOAD, _DOWNLOAD, _TIME;
				_DAYID = Thoth.Data.MOBILE_CURR_DAY_ID;
				_BOOTID = Thoth.Data.MOBILE_NWK_RESET;
				_UPLOAD = NetworkStatistics.getMTBytes();
				_DOWNLOAD = NetworkStatistics.getMRBytes();
				_TIME = System.currentTimeMillis();
				if (Thoth.Data.MOBILE_DAILY_CORRECTION)
				{
					Thoth.Data.MOBILE_DAILY_CORRECTION = false;
					Thoth.Data.MOBILE_DAILY_UL_CORRECTION = _UPLOAD;
					Thoth.Data.MOBILE_DAILY_DL_CORRECTION = _DOWNLOAD;
					if (Thoth.Data.MOBILE_DAILY_UL_CORRECTION >= Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV)
					{
						_UPLOAD = Thoth.Data.MOBILE_DAILY_UL_CORRECTION - Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV;
						Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV += _UPLOAD;
					}
					else
						_UPLOAD = Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
					if (Thoth.Data.MOBILE_DAILY_DL_CORRECTION >= Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV)
					{
						_DOWNLOAD = Thoth.Data.MOBILE_DAILY_DL_CORRECTION - Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV;
						Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV += _DOWNLOAD;
					}
					else
						_DOWNLOAD = Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
					Thoth.Data.write();
				}
				if (Thoth.Data.MOBILE_NEW_DAY && Thoth.Data.MOBILE_CURR_DAY_ID != Thoth.Data.MOBILE_PREV_DAY_ID)
				{
					Thoth.Data.MOBILE_NEW_DAY = false;
					Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV = Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
					Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV = Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
					_UPLOAD -= Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
					_DOWNLOAD -= Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
				}
				_UPLOAD = _UPLOAD < 0 ? 0 : _UPLOAD;
				_DOWNLOAD = _DOWNLOAD < 0 ? 0 : _DOWNLOAD;
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				ContentValues newRow = new ContentValues();
				newRow.put(ThothDB.col_T_D_M_PKEY, _DAYID + _BOOTID);
				newRow.put(ThothDB.col_T_D_M_DAYID, _DAYID);
				newRow.put(ThothDB.col_T_D_M_BOOTID, _BOOTID);
				newRow.put(ThothDB.col_T_D_M_UPLOAD, _UPLOAD);
				newRow.put(ThothDB.col_T_D_M_DOWNLOAD, _DOWNLOAD);
				newRow.put(ThothDB.col_T_D_M_TIME, _TIME);
				try
				{
					db.insertOrThrow(ThothDB.table_TEMP_DATA_MOBL, "", newRow);
				}
				catch (SQLiteConstraintException e)
				{
					try
					{
						db.replaceOrThrow(ThothDB.table_TEMP_DATA_MOBL, "", newRow);
					}
					catch (Exception ee)
					{
						// ThothLog.e(ee);
					}
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
				}
				if (db.isOpen())
					db.close();
			}
		});
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				long _DAYID, _BOOTID, _UPLOAD, _DOWNLOAD, _TIME;
				_DAYID = Thoth.Data.WIFI_CURR_DAY_ID;
				_BOOTID = Thoth.Data.WIFI_NWK_RESET;
				_UPLOAD = NetworkStatistics.getTTBytes() - NetworkStatistics.getMTBytes();
				_DOWNLOAD = NetworkStatistics.getTRBytes() - NetworkStatistics.getMRBytes();
				_TIME = System.currentTimeMillis();
				if (Thoth.Data.WIFI_DAILY_CORRECTION)
				{
					Thoth.Data.WIFI_DAILY_CORRECTION = false;
					Thoth.Data.WIFI_DAILY_UL_CORRECTION = _UPLOAD;
					Thoth.Data.WIFI_DAILY_DL_CORRECTION = _DOWNLOAD;
					if (Thoth.Data.WIFI_DAILY_UL_CORRECTION >= Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV)
					{
						_UPLOAD = Thoth.Data.WIFI_DAILY_UL_CORRECTION - Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV;
						Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV += _UPLOAD;
					}
					else
						_UPLOAD = Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					if (Thoth.Data.WIFI_DAILY_DL_CORRECTION >= Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV)
					{
						_DOWNLOAD = Thoth.Data.WIFI_DAILY_DL_CORRECTION - Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV;
						Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV += _DOWNLOAD;
					}
					else
						_DOWNLOAD = Thoth.Data.WIFI_DAILY_DL_CORRECTION;
					Thoth.Data.WIFI_INTRADAY_UL_CORRECTION = Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV = 0;
					Thoth.Data.WIFI_INTRADAY_DL_CORRECTION = Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV = 0;
					Thoth.Data.write();
				}
				if (Thoth.Data.WIFI_INTRADAY_CORRECTION)
				{
					Thoth.Data.WIFI_INTRADAY_CORRECTION = false;
					Thoth.Data.WIFI_INTRADAY_UL_CORRECTION = _UPLOAD - Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					Thoth.Data.WIFI_INTRADAY_DL_CORRECTION = _DOWNLOAD - Thoth.Data.WIFI_DAILY_DL_CORRECTION;
					if (Thoth.Data.WIFI_INTRADAY_UL_CORRECTION > Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV)
					{
						_UPLOAD = Thoth.Data.WIFI_INTRADAY_UL_CORRECTION - Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV;
						Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV += _UPLOAD;
					}
					else
						_UPLOAD = 0;
					if (Thoth.Data.WIFI_INTRADAY_DL_CORRECTION > Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV)
					{
						_DOWNLOAD = Thoth.Data.WIFI_INTRADAY_DL_CORRECTION - Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV;
						Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV += _DOWNLOAD;
					}
					else
						_DOWNLOAD = 0;
					Thoth.Data.write();
				}
				if (Thoth.Data.WIFI_NEW_DAY && Thoth.Data.WIFI_CURR_DAY_ID != Thoth.Data.WIFI_PREV_DAY_ID)
				{
					Thoth.Data.WIFI_NEW_DAY = false;
					Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV = Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV = Thoth.Data.WIFI_DAILY_DL_CORRECTION;
					_UPLOAD -= Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					_DOWNLOAD -= Thoth.Data.WIFI_DAILY_DL_CORRECTION;
				}
				_UPLOAD = _UPLOAD < 0 ? 0 : _UPLOAD;
				_DOWNLOAD = _DOWNLOAD < 0 ? 0 : _DOWNLOAD;
				if (_UPLOAD < 1 || _DOWNLOAD < 1)
					_BOOTID = Thoth.Data.WIFI_NWK_RESET = System.currentTimeMillis();
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				ContentValues newRow = new ContentValues();
				newRow.put(ThothDB.col_T_D_W_PKEY, _DAYID + _BOOTID);
				newRow.put(ThothDB.col_T_D_W_DAYID, _DAYID);
				newRow.put(ThothDB.col_T_D_W_BOOTID, _BOOTID);
				newRow.put(ThothDB.col_T_D_W_UPLOAD, _UPLOAD);
				newRow.put(ThothDB.col_T_D_W_DOWNLOAD, _DOWNLOAD);
				newRow.put(ThothDB.col_T_D_W_TIME, _TIME);
				try
				{
					db.insertOrThrow(ThothDB.table_TEMP_DATA_WIFI, "", newRow);
				}
				catch (SQLiteConstraintException e)
				{
					try
					{
						db.replaceOrThrow(ThothDB.table_TEMP_DATA_WIFI, "", newRow);
					}
					catch (Exception ee)
					{
						// ThothLog.e(ee);
					}
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
				}
				if (db.isOpen())
					db.close();
			}
		});
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				final PackageManager pkgMgr = getPackageManager();
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				List<PackageInfo> appList = pkgMgr.getInstalledPackages(0);
				SetupMain.sPbMax += appList.size();
				int i = 0;
				for (PackageInfo app : appList)
				{
					SetupMain.sPbCurrent++;
					Message msg = new Message();
					msg.what = 55;
					msg.arg1 = 3;
					msg.obj = new String("Data logs (" + (++i) + "/" + appList.size() + ")");
					SetupMain.getInstance().SETUP.sendMessage(msg);
					if (!((app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0))
					{
						int appUID = app.applicationInfo.uid;
						String appName = app.applicationInfo.loadLabel(getPackageManager()).toString();
						String appPackage = app.packageName;
						String appUIDName = pkgMgr.getNameForUid(appUID);
						long appUpload = NetworkStatistics.getATBytes(appUID);
						long appDownload = NetworkStatistics.getARBytes(appUID);
						long appDayId = Thoth.Data.CURR_DAY_ID;
						long appBootId = Thoth.Data.DATA_RESET;
						ContentValues newRow = new ContentValues();
						newRow.put(ThothDB.col_H_A_PKEY, appPackage);
						try
						{
							db.insertOrThrow(ThothDB.table_HASH_APPS, "", newRow);
						}
						catch (Exception e)
						{
							// ThothLog.e(e);
						}
						long UL_CORRECTION = appUpload, UL_CORRECTION_PREV = 0, DL_CORRECTION = appDownload, DL_CORRECTION_PREV = 0;
						Cursor cR = db.rawQuery("SELECT SUM(" + ThothDB.col_T_A_UPLOAD + "), SUM(" + ThothDB.col_T_A_DOWNLOAD + ") FROM " + ThothDB.table_TEMP_APPS + " WHERE " + ThothDB.col_T_A_PKG + "='" + appPackage + "' AND " + ThothDB.col_T_A_DAYID + "=" + Thoth.Data.PREV_DAY_ID + " AND " + ThothDB.col_T_A_BOOTID + "=" + appBootId + " GROUP BY " + ThothDB.col_T_A_DAYID, null);
						if (null != cR && cR.moveToFirst())
						{
							UL_CORRECTION_PREV = cR.getLong(0);
							DL_CORRECTION_PREV = cR.getLong(1);
							cR.close();
						}
						appUpload = (UL_CORRECTION >= UL_CORRECTION_PREV) ? (UL_CORRECTION - UL_CORRECTION_PREV) : UL_CORRECTION;
						appDownload = (DL_CORRECTION >= DL_CORRECTION_PREV) ? (DL_CORRECTION - DL_CORRECTION_PREV) : DL_CORRECTION;
						if (appUpload < 1 && appDownload < 1)
							continue;
						newRow = new ContentValues();
						newRow.put(ThothDB.col_T_A_PKG, appPackage);
						newRow.put(ThothDB.col_T_A_NAME, appName);
						newRow.put(ThothDB.col_T_A_UID, appUID);
						newRow.put(ThothDB.col_T_A_UID_NAME, appUIDName);
						newRow.put(ThothDB.col_T_A_DAYID, appDayId);
						newRow.put(ThothDB.col_T_A_BOOTID, appBootId);
						newRow.put(ThothDB.col_T_A_UPLOAD, appUpload);
						newRow.put(ThothDB.col_T_A_DOWNLOAD, appDownload);
						try
						{
							db.insertOrThrow(ThothDB.table_TEMP_APPS, "", newRow);
						}
						catch (Exception e)
						{
							// ThothLog.e(e);
						}
					}
				}
				if (db.isOpen())
					db.close();
			}
		});
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				Settings.APP_LAST_DATA_CAPTURE = System.currentTimeMillis();
				Settings.write();
				SetupMain.getInstance().SETUP.sendEmptyMessage(54);
			}
		});
	}
	
	void missedCallCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				ContentResolver cr = getContentResolver();
				Cursor callCur = cr.query(android.provider.CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");
				if (null == callCur || !callCur.moveToFirst())
					return;
				int T_C_DURATION;
				long T_C_DATETIME;
				String H_C_PKEY;
				String T_C_NUMBER, T_C_CONTACT, T_C_CONTACTNAME, T_C_TYPE;
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				for (int k = 0; k < callCur.getCount(); k++)
				{
					T_C_NUMBER = callCur.getString(callCur.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
					T_C_CONTACT = T_C_NUMBER;
					T_C_CONTACTNAME = "UNKNOWN";
					T_C_DATETIME = callCur.getLong(callCur.getColumnIndex(android.provider.CallLog.Calls.DATE));
					T_C_DURATION = callCur.getInt(callCur.getColumnIndex(android.provider.CallLog.Calls.DURATION));
					T_C_TYPE = callCur.getString(callCur.getColumnIndex(android.provider.CallLog.Calls.TYPE));
					switch (callCur.getInt(callCur.getColumnIndex(android.provider.CallLog.Calls.TYPE)))
					{
						case android.provider.CallLog.Calls.INCOMING_TYPE :
							T_C_TYPE = (T_C_DURATION == 0) ? "2" : "0";
							break;
						case android.provider.CallLog.Calls.MISSED_TYPE :
							T_C_TYPE = "3";
							break;
						case android.provider.CallLog.Calls.OUTGOING_TYPE :
							T_C_TYPE = (T_C_DURATION == 0) ? "4" : "1";
							break;
					}
					H_C_PKEY = T_C_NUMBER + T_C_CONTACT + T_C_DATETIME + T_C_DURATION + T_C_TYPE;
					// Write to DB.
					// Insert HASH in h_Call
					ContentValues newRow = new ContentValues();
					newRow.put(ThothDB.col_H_C_PKEY, H_C_PKEY);
					try
					{
						db.insertOrThrow(ThothDB.table_HASH_CALL, "", newRow);
					}
					catch (SQLException exp)
					{
						// ThothLog.e(e);
						if (callCur.moveToNext())
							continue;
						else
							break;
					}
					// Insert record into t_Call
					newRow = new ContentValues();
					newRow.put(ThothDB.col_T_C_FKEY, H_C_PKEY);
					newRow.put(ThothDB.col_T_C_NUMBER, T_C_NUMBER);
					Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_C_NUMBER));
					Cursor c = null;
					try
					{
						c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
						if (null != c && c.moveToFirst())
						{
							T_C_CONTACT = c.getString(0);
							T_C_CONTACTNAME = c.getString(1);
						}
					}
					catch (Exception e)
					{}
					finally
					{
						if (null != c)
							c.close();
					}
					newRow.put(ThothDB.col_T_C_CONTACT, T_C_CONTACT);
					newRow.put(ThothDB.col_T_C_CONTACTNAME, T_C_CONTACTNAME);
					newRow.put(ThothDB.col_T_C_DATETIME, T_C_DATETIME);
					newRow.put(ThothDB.col_T_C_DURATION, T_C_DURATION);
					newRow.put(ThothDB.col_T_C_TYPE, T_C_TYPE);
					try
					{
						db.insertOrThrow(ThothDB.table_TEMP_CALL, "", newRow);
					}
					catch (SQLException e)
					{
						// ThothLog.e(e);
					}
					callCur.moveToNext();
				}
				if (db.isOpen())
					db.close();
				callCur.close();
				Settings.APP_LAST_CALL_CAPTURE = System.currentTimeMillis();
				Settings.write();
				ThothLog.dL("RESCAN_CALL_CAPTURE COMPLETE -> " + Settings.APP_LAST_CALL_CAPTURE);
			}
		});
	}
	
	void missedSMSCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				float T_M_DATETIME;
				String H_M_PKEY;
				String T_M_NUMBER, T_M_CONTACT, T_M_CONTACTNAME, T_M_MSG, T_M_TYPE;
				ContentResolver cr = getContentResolver();
				Cursor smsCur = cr.query(Uri.parse("content://sms"), null, null, null, null);
				if (null == smsCur || !smsCur.moveToFirst())
					return;
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				for (int i = 0; i < smsCur.getCount(); i++)
				{
					T_M_NUMBER = smsCur.getString(smsCur.getColumnIndexOrThrow("address")).toString();
					T_M_CONTACT = T_M_NUMBER;
					T_M_CONTACTNAME = "UNKNOWN";
					T_M_DATETIME = smsCur.getFloat(smsCur.getColumnIndexOrThrow("date"));
					T_M_MSG = smsCur.getString(smsCur.getColumnIndexOrThrow("body")).toString();
					T_M_TYPE = smsCur.getString(smsCur.getColumnIndexOrThrow("type")).toString();
					if (Integer.parseInt(T_M_TYPE) == 1 || Integer.parseInt(T_M_TYPE) == 2)
					{
						T_M_TYPE = (Integer.parseInt(T_M_TYPE) == 1) ? "0" : "1";
						H_M_PKEY = MD5.getMD5HEX(T_M_NUMBER + T_M_CONTACT + T_M_DATETIME + T_M_MSG + T_M_TYPE);
						// Write to DB.
						// Insert HASH in h_Msgs
						ContentValues newRow = new ContentValues();
						newRow.put(ThothDB.col_H_M_PKEY, H_M_PKEY);
						try
						{
							db.insertOrThrow(ThothDB.table_HASH_MSGS, "", newRow);
						}
						catch (SQLException exp)
						{
							// ThothLog.e(e);
							if (smsCur.moveToNext())
								continue;
							else
								break;
						}
						// Insert record into t_Msgs
						newRow = new ContentValues();
						newRow.put(ThothDB.col_T_M_FKEY, H_M_PKEY);
						newRow.put(ThothDB.col_T_M_NUMBER, T_M_NUMBER);
						Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_M_NUMBER));
						Cursor c = null;
						try
						{
							c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
							if (null != c && c.moveToFirst())
							{
								T_M_CONTACT = c.getString(0);
								T_M_CONTACTNAME = c.getString(1);
							}
						}
						catch (Exception e)
						{}
						finally
						{
							if (null != c)
								c.close();
						}
						newRow.put(ThothDB.col_T_M_CONTACT, T_M_CONTACT);
						newRow.put(ThothDB.col_T_M_CONTACTNAME, T_M_CONTACTNAME);
						newRow.put(ThothDB.col_T_M_DATETIME, T_M_DATETIME);
						newRow.put(ThothDB.col_T_M_MSG, T_M_MSG);
						newRow.put(ThothDB.col_T_M_TYPE, T_M_TYPE);
						newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_NUMBER);
						newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_NUMBER.getBytes());
						newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, "text/plain");
						try
						{
							db.insertOrThrow(ThothDB.table_TEMP_MSGS, "", newRow);
						}
						catch (SQLException e)
						{
							// ThothLog.e(e);
						}
						catch (Exception e)
						{
							// ThothLog.e(e);
						}
						smsCur.moveToNext();
					}
					else
					{
						smsCur.moveToNext();
						continue;
					}
				}
				if (db.isOpen())
					db.close();
				smsCur.close();
				Settings.APP_LAST_MSGS_CAPTURE = System.currentTimeMillis();
				Settings.write();
				ThothLog.dL("RESCAN_MSGS_CAPTURE COMPLETE -> " + Settings.APP_LAST_MSGS_CAPTURE);
			}
		});
	}
	
	void missedMMSCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ContentResolver cr = getContentResolver();
					Cursor mmsCur = cr.query(Uri.parse("content://mms/"), null, null, null, "_id");
					if (null == mmsCur || !mmsCur.moveToFirst())
						return;
					//
					ThothDB dbA = new ThothDB(cx);
					SQLiteDatabase db = dbA.getDb();
					//
					for (int z = 0; z < mmsCur.getCount(); z++)
					{
						float T_M_DATETIME = 0;
						String H_M_PKEY = "", T_M_NUMBER = "", T_M_CONTACT = "", T_M_CONTACTNAME = "", T_M_MSG = "", T_M_TYPE = "", T_M_BLOB_TEXT = "";
						byte[] T_M_BLOB_DATA = null;
						String T_M_BLOB_DATA_MIME = "";
						int id = Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("_id")));
						T_M_DATETIME = 1000 * mmsCur.getLong(mmsCur.getColumnIndex("date"));
						Cursor addrCur = cr.query(Uri.parse("content://mms/" + id + "/addr"), null, "type=151", null, "_id");
						if (addrCur != null && addrCur.getCount() > 0 && addrCur.moveToFirst())
							T_M_NUMBER = addrCur.getString(addrCur.getColumnIndex("address"));
						if (T_M_NUMBER != "")
							T_M_CONTACT = T_M_NUMBER;
						else
							T_M_CONTACT = T_M_NUMBER = Settings.USR_DEVICE_PHONE_NUMBER;
						addrCur.close();
						T_M_CONTACTNAME = "UNKNOWN";
						T_M_MSG = mmsCur.getString(mmsCur.getColumnIndex("sub"));
						T_M_TYPE = (Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("m_type"))) == 128) ? "3" : "2";
						H_M_PKEY = MD5.getMD5HEX(id + T_M_NUMBER + T_M_CONTACT + T_M_DATETIME + T_M_MSG + T_M_TYPE);
						// Write to DB.
						// Insert HASH in h_Msgs
						ContentValues newRow = new ContentValues();
						newRow.put(ThothDB.col_H_M_PKEY, H_M_PKEY);
						try
						{
							db.insertOrThrow(ThothDB.table_HASH_MSGS, "", newRow);
						}
						catch (SQLException e)
						{
							// ThothLog.e(e);
							if (mmsCur.moveToNext())
								continue;
							else
								break;
						}
						// Get MMS message parts
						Cursor curPart = cr.query(Uri.parse("content://mms/part"), null, "mid = " + id, null, "_id");
						if (curPart.getCount() > 0 && curPart.moveToFirst())
						{
							for (int y = 0; y < curPart.getCount(); y++)
							{
								String mime = curPart.getString(curPart.getColumnIndex("ct"));
								String prtId = curPart.getString(curPart.getColumnIndex("_id"));
								if (mime.equalsIgnoreCase("text/plain"))
								{
									byte[] messageData = readMMSPart(prtId);
									if (messageData != null && messageData.length > 0)
										T_M_BLOB_TEXT = new String(messageData);
									if (T_M_BLOB_TEXT == "")
									{
										Cursor txtPartCur = cr.query(Uri.parse("content://mms/part"), null, "mid = " + id + " and _id =" + prtId, null, "_id");
										txtPartCur.moveToLast();
										T_M_BLOB_TEXT = txtPartCur.getString(txtPartCur.getColumnIndex("text"));
										if (T_M_MSG.length() == 0 && T_M_BLOB_TEXT != null)
											T_M_MSG = T_M_BLOB_TEXT;
									}
								}
								else
								{
									T_M_BLOB_DATA = readMMSPart(prtId);
									T_M_BLOB_DATA_MIME = mime;
								}
								curPart.moveToNext();
							}
						}
						newRow = new ContentValues();
						newRow.put(ThothDB.col_T_M_FKEY, H_M_PKEY);
						newRow.put(ThothDB.col_T_M_NUMBER, T_M_NUMBER);
						Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_M_NUMBER));
						Cursor c = null;
						try
						{
							c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
							if (null != c && c.moveToFirst())
							{
								T_M_CONTACT = c.getString(0);
								T_M_CONTACTNAME = c.getString(1);
							}
						}
						catch (Exception e)
						{
							// ThothLog.e(e);
						}
						finally
						{
							if (null != c)
								c.close();
						}
						newRow.put(ThothDB.col_T_M_CONTACT, T_M_CONTACT);
						newRow.put(ThothDB.col_T_M_CONTACTNAME, T_M_CONTACTNAME);
						newRow.put(ThothDB.col_T_M_DATETIME, T_M_DATETIME);
						if (T_M_MSG != null)
							newRow.put(ThothDB.col_T_M_MSG, T_M_MSG);
						else
							newRow.put(ThothDB.col_T_M_MSG, T_M_NUMBER);
						newRow.put(ThothDB.col_T_M_TYPE, T_M_TYPE);
						if (T_M_BLOB_TEXT != null)
							newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_BLOB_TEXT);
						else
							newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_NUMBER);
						if (T_M_BLOB_DATA != null)
							newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_BLOB_DATA);
						else
							newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_NUMBER.getBytes());
						if (T_M_BLOB_DATA_MIME != "")
							newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, T_M_BLOB_DATA_MIME);
						else
							newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, "text/plain");
						try
						{
							db.insertOrThrow(ThothDB.table_TEMP_MSGS, "", newRow);
						}
						catch (SQLException e)
						{
							// ThothLog.e(e);
						}
						mmsCur.moveToNext();
					}
					if (db.isOpen())
						db.close();
					mmsCur.close();
					Settings.APP_LAST_MSGM_CAPTURE = System.currentTimeMillis();
					Settings.write();
					ThothLog.dL("RESCAN_MSGM_CAPTURE COMPLETE -> " + Settings.APP_LAST_MSGM_CAPTURE);
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
				}
			}
		});
	}
	
	byte[] readMMSPart(String partId)
	{
		byte[] partData = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try
		{
			is = getContentResolver().openInputStream(Uri.parse("content://mms/part/" + partId));
			byte[] buffer = new byte[256];
			int len = is.read(buffer);
			while (len >= 0)
			{
				baos.write(buffer, 0, len);
				len = is.read(buffer);
			}
			partData = baos.toByteArray();
		}
		catch (IOException e)
		{}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					// ThothLog.e(e);
				}
			}
		}
		return partData;
	}
	
	void upgradeCallCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				ContentResolver cr = getContentResolver();
				Cursor callCur = cr.query(android.provider.CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");
				if (callCur == null || !callCur.moveToFirst())
				{
					Message msg = new Message();
					msg.what = 666;
					msg.arg1 = Thoth.REQ.CALL_CAPTURE;
					sHandler.sendMessage(msg);
					Settings.APP_LAST_CALL_CAPTURE = System.currentTimeMillis();
					Settings.write();
					UpgradeMain.g().UPGRADE.sendEmptyMessage(51);
					return;
				}
				int T_C_DURATION;
				long T_C_DATETIME;
				String H_C_PKEY;
				String T_C_NUMBER, T_C_CONTACT, T_C_CONTACTNAME, T_C_TYPE;
				UpgradeMain.max += callCur.getCount();
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				for (int k = 0; k < callCur.getCount(); k++)
				{
					UpgradeMain.current++;
					Message msg = new Message();
					msg.what = 55;
					msg.arg1 = 0;
					msg.obj = new String("CALL logs (" + (k + 1) + "/" + callCur.getCount() + ")");
					UpgradeMain.g().UPGRADE.sendMessage(msg);
					T_C_NUMBER = callCur.getString(callCur.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
					T_C_CONTACT = T_C_NUMBER;
					T_C_CONTACTNAME = "UNKNOWN";
					T_C_DATETIME = callCur.getLong(callCur.getColumnIndex(android.provider.CallLog.Calls.DATE));
					T_C_DURATION = callCur.getInt(callCur.getColumnIndex(android.provider.CallLog.Calls.DURATION));
					T_C_TYPE = callCur.getString(callCur.getColumnIndex(android.provider.CallLog.Calls.TYPE));
					switch (callCur.getInt(callCur.getColumnIndex(android.provider.CallLog.Calls.TYPE)))
					{
						case android.provider.CallLog.Calls.INCOMING_TYPE :
							T_C_TYPE = (T_C_DURATION == 0) ? "2" : "0";
							break;
						case android.provider.CallLog.Calls.MISSED_TYPE :
							T_C_TYPE = "3";
							break;
						case android.provider.CallLog.Calls.OUTGOING_TYPE :
							T_C_TYPE = (T_C_DURATION == 0) ? "4" : "1";
							break;
					}
					H_C_PKEY = T_C_NUMBER + T_C_CONTACT + T_C_DATETIME + T_C_DURATION + T_C_TYPE;
					// Write to DB.
					// Insert HASH in h_Call
					ContentValues newRow = new ContentValues();
					newRow.put(ThothDB.col_H_C_PKEY, H_C_PKEY);
					try
					{
						db.insertOrThrow(ThothDB.table_HASH_CALL, "", newRow);
					}
					catch (SQLException exp)
					{
						// ThothLog.e(e);
						if (callCur.moveToNext())
							continue;
						else
							break;
					}
					// Insert record into t_Call
					newRow = new ContentValues();
					newRow.put(ThothDB.col_T_C_FKEY, H_C_PKEY);
					newRow.put(ThothDB.col_T_C_NUMBER, T_C_NUMBER);
					Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_C_NUMBER));
					Cursor c = null;
					try
					{
						c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
						if (null != c && c.moveToFirst())
						{
							T_C_CONTACT = c.getString(0);
							T_C_CONTACTNAME = c.getString(1);
						}
					}
					catch (Exception e)
					{}
					finally
					{
						if (null != c)
							c.close();
					}
					newRow.put(ThothDB.col_T_C_CONTACT, T_C_CONTACT);
					newRow.put(ThothDB.col_T_C_CONTACTNAME, T_C_CONTACTNAME);
					newRow.put(ThothDB.col_T_C_DATETIME, T_C_DATETIME);
					newRow.put(ThothDB.col_T_C_DURATION, T_C_DURATION);
					newRow.put(ThothDB.col_T_C_TYPE, T_C_TYPE);
					try
					{
						db.insertOrThrow(ThothDB.table_TEMP_CALL, "", newRow);
					}
					catch (SQLException e)
					{
						// ThothLog.e(e);
					}
					callCur.moveToNext();
				}
				if (db.isOpen())
					db.close();
				callCur.close();
				Settings.APP_LAST_CALL_CAPTURE = System.currentTimeMillis();
				Settings.write();
				UpgradeMain.g().UPGRADE.sendEmptyMessage(51);
			}
		});
	}
	
	void upgradeSMSCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				float T_M_DATETIME;
				String H_M_PKEY;
				String T_M_NUMBER, T_M_CONTACT, T_M_CONTACTNAME, T_M_MSG, T_M_TYPE;
				ContentResolver cr = getContentResolver();
				Cursor smsCur = cr.query(Uri.parse("content://sms"), null, null, null, null);
				if (!smsCur.moveToFirst())
				{
					Message msg = new Message();
					msg.what = 666;
					msg.arg1 = Thoth.REQ.MSGS_CAPTURE;
					sHandler.sendMessage(msg);
					Settings.APP_LAST_MSGS_CAPTURE = System.currentTimeMillis();
					Settings.write();
					UpgradeMain.g().UPGRADE.sendEmptyMessage(52);
					return;
				}
				UpgradeMain.max += smsCur.getCount();
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				for (int i = 0; i < smsCur.getCount(); i++)
				// for (int i = 0; i < ((smsCur.getCount() > 30) ? 30 : smsCur.getCount()); i++)
				{
					UpgradeMain.current++;
					Message msg = new Message();
					msg.what = 55;
					msg.arg1 = 1;
					// msg.obj = new String("SMS logs (" + (i + 1) + "/" + ((smsCur.getCount() > 30) ? 30
					// : smsCur.getCount()) + ")");
					msg.obj = new String("SMS logs (" + (i + 1) + "/" + smsCur.getCount() + ")");
					UpgradeMain.g().UPGRADE.sendMessage(msg);
					T_M_NUMBER = smsCur.getString(smsCur.getColumnIndexOrThrow("address")).toString();
					T_M_CONTACT = T_M_NUMBER;
					T_M_CONTACTNAME = "UNKNOWN";
					T_M_DATETIME = smsCur.getFloat(smsCur.getColumnIndexOrThrow("date"));
					T_M_MSG = smsCur.getString(smsCur.getColumnIndexOrThrow("body")).toString();
					T_M_TYPE = smsCur.getString(smsCur.getColumnIndexOrThrow("type")).toString();
					if (Integer.parseInt(T_M_TYPE) == 1 || Integer.parseInt(T_M_TYPE) == 2)
					{
						T_M_TYPE = (Integer.parseInt(T_M_TYPE) == 1) ? "0" : "1";
						H_M_PKEY = MD5.getMD5HEX(T_M_NUMBER + T_M_CONTACT + T_M_DATETIME + T_M_MSG + T_M_TYPE);
						// Write to DB.
						// Insert HASH in h_Msgs
						ContentValues newRow = new ContentValues();
						newRow.put(ThothDB.col_H_M_PKEY, H_M_PKEY);
						try
						{
							db.insertOrThrow(ThothDB.table_HASH_MSGS, "", newRow);
						}
						catch (SQLException exp)
						{
							// ThothLog.e(e);
							if (smsCur.moveToNext())
								continue;
							else
								break;
						}
						// Insert record into t_Msgs
						newRow = new ContentValues();
						newRow.put(ThothDB.col_T_M_FKEY, H_M_PKEY);
						newRow.put(ThothDB.col_T_M_NUMBER, T_M_NUMBER);
						Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_M_NUMBER));
						Cursor c = null;
						try
						{
							c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
							if (null != c && c.moveToFirst())
							{
								T_M_CONTACT = c.getString(0);
								T_M_CONTACTNAME = c.getString(1);
							}
						}
						catch (Exception e)
						{}
						finally
						{
							if (null != c)
								c.close();
						}
						newRow.put(ThothDB.col_T_M_CONTACT, T_M_CONTACT);
						newRow.put(ThothDB.col_T_M_CONTACTNAME, T_M_CONTACTNAME);
						newRow.put(ThothDB.col_T_M_DATETIME, T_M_DATETIME);
						newRow.put(ThothDB.col_T_M_MSG, T_M_MSG);
						newRow.put(ThothDB.col_T_M_TYPE, T_M_TYPE);
						newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_NUMBER);
						newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_NUMBER.getBytes());
						newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, "text/plain");
						try
						{
							db.insertOrThrow(ThothDB.table_TEMP_MSGS, "", newRow);
						}
						catch (SQLException e)
						{
							// ThothLog.e(e);
						}
						catch (Exception e)
						{
							// ThothLog.e(e);
						}
						smsCur.moveToNext();
					}
					else
					{
						smsCur.moveToNext();
						continue;
					}
				}
				if (db.isOpen())
					db.close();
				smsCur.close();
				Settings.APP_LAST_MSGS_CAPTURE = System.currentTimeMillis();
				Settings.write();
				UpgradeMain.g().UPGRADE.sendEmptyMessage(52);
			}
		});
	}
	
	void upgradeMMSCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ContentResolver cr = getContentResolver();
					Cursor mmsCur = cr.query(Uri.parse("content://mms/"), null, null, null, "_id");
					if (!mmsCur.moveToFirst())
					{
						Message msg = new Message();
						msg.what = 666;
						msg.arg1 = Thoth.REQ.MSGM_CAPTURE;
						sHandler.sendMessage(msg);
						Settings.APP_LAST_MSGM_CAPTURE = System.currentTimeMillis();
						Settings.write();
						UpgradeMain.g().UPGRADE.sendEmptyMessage(53);
						return;
					}
					UpgradeMain.max += mmsCur.getCount();
					//
					ThothDB dbA = new ThothDB(cx);
					SQLiteDatabase db = dbA.getDb();
					//
					for (int z = 0; z < mmsCur.getCount(); z++)
					// for (int z = 0; z < ((mmsCur.getCount() > 5) ? 5 : mmsCur.getCount()); z++)
					{
						UpgradeMain.current++;
						Message msg = new Message();
						msg.what = 55;
						msg.arg1 = 2;
						// msg.obj = new String("MMS logs (" + (z + 1) + "/" + ((mmsCur.getCount()) > 100
						// ? 100 : mmsCur.getCount()) + ")");
						msg.obj = new String("MMS logs (" + (z + 1) + "/" + mmsCur.getCount() + ")");
						//
						UpgradeMain.g().UPGRADE.sendMessage(msg);
						//
						float T_M_DATETIME = 0;
						String H_M_PKEY = "", T_M_NUMBER = "", T_M_CONTACT = "", T_M_CONTACTNAME = "", T_M_MSG = "", T_M_TYPE = "", T_M_BLOB_TEXT = "";
						byte[] T_M_BLOB_DATA = new byte[0];
						String T_M_BLOB_DATA_MIME = "";
						int id = Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("_id")));
						T_M_DATETIME = 1000 * mmsCur.getLong(mmsCur.getColumnIndex("date"));
						Cursor addrCur = cr.query(Uri.parse("content://mms/" + id + "/addr"), null, "type=151", null, "_id");
						if (addrCur != null && addrCur.getCount() > 0 && addrCur.moveToFirst())
							T_M_NUMBER = addrCur.getString(addrCur.getColumnIndex("address"));
						if (T_M_NUMBER != "")
							T_M_CONTACT = T_M_NUMBER;
						else
							T_M_CONTACT = T_M_NUMBER = Settings.USR_DEVICE_PHONE_NUMBER;
						addrCur.close();
						T_M_CONTACTNAME = "UNKNOWN";
						T_M_MSG = mmsCur.getString(mmsCur.getColumnIndex("sub"));
						T_M_TYPE = (Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("m_type"))) == 128) ? "3" : "2";
						H_M_PKEY = MD5.getMD5HEX(id + T_M_NUMBER + T_M_CONTACT + T_M_DATETIME + T_M_MSG + T_M_TYPE);
						// Write to DB.
						// Insert HASH in h_Msgs
						ContentValues newRow = new ContentValues();
						newRow.put(ThothDB.col_H_M_PKEY, H_M_PKEY);
						try
						{
							db.insertOrThrow(ThothDB.table_HASH_MSGS, "", newRow);
						}
						catch (SQLException e)
						{
							// ThothLog.e(e);
							if (mmsCur.moveToNext())
								continue;
							else
								break;
						}
						// Get MMS message parts
						Cursor curPart = cr.query(Uri.parse("content://mms/part"), null, "mid = " + id, null, "_id");
						if (curPart.getCount() > 0 && curPart.moveToFirst())
						{
							for (int y = 0; y < curPart.getCount(); y++)
							{
								String mime = curPart.getString(curPart.getColumnIndex("ct"));
								String prtId = curPart.getString(curPart.getColumnIndex("_id"));
								if (mime.equalsIgnoreCase("text/plain"))
								{
									byte[] messageData = readMMSPart(prtId);
									if (messageData != null && messageData.length > 0)
										T_M_BLOB_TEXT = new String(messageData);
									if (T_M_BLOB_TEXT == "")
									{
										Cursor txtPartCur = cr.query(Uri.parse("content://mms/part"), null, "mid = " + id + " and _id =" + prtId, null, "_id");
										txtPartCur.moveToLast();
										T_M_BLOB_TEXT = txtPartCur.getString(txtPartCur.getColumnIndex("text"));
										if (T_M_MSG.length() == 0 && T_M_BLOB_TEXT != null)
											T_M_MSG = T_M_BLOB_TEXT;
									}
								}
								else
								{
									T_M_BLOB_DATA = readMMSPart(prtId);
									T_M_BLOB_DATA_MIME = mime;
								}
								curPart.moveToNext();
							}
						}
						newRow = new ContentValues();
						newRow.put(ThothDB.col_T_M_FKEY, H_M_PKEY);
						newRow.put(ThothDB.col_T_M_NUMBER, T_M_NUMBER);
						Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(T_M_NUMBER));
						Cursor c = null;
						try
						{
							c = cr.query(uri, new String[]{PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME}, null, null, null);
							if (null != c && c.moveToFirst())
							{
								T_M_CONTACT = c.getString(0);
								T_M_CONTACTNAME = c.getString(1);
							}
						}
						catch (Exception e)
						{}
						finally
						{
							if (null != c)
								c.close();
						}
						newRow.put(ThothDB.col_T_M_CONTACT, T_M_CONTACT);
						newRow.put(ThothDB.col_T_M_CONTACTNAME, T_M_CONTACTNAME);
						newRow.put(ThothDB.col_T_M_DATETIME, T_M_DATETIME);
						if (T_M_MSG != null)
							newRow.put(ThothDB.col_T_M_MSG, T_M_MSG);
						else
							newRow.put(ThothDB.col_T_M_MSG, T_M_NUMBER);
						newRow.put(ThothDB.col_T_M_TYPE, T_M_TYPE);
						if (T_M_BLOB_TEXT != null)
							newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_BLOB_TEXT);
						else
							newRow.put(ThothDB.col_T_M_BLOB_TEXT, T_M_NUMBER);
						if (T_M_BLOB_DATA != null)
							newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_BLOB_DATA);
						else
							newRow.put(ThothDB.col_T_M_BLOB_DATA, T_M_NUMBER.getBytes());
						if (T_M_BLOB_DATA_MIME != "")
							newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, T_M_BLOB_DATA_MIME);
						else
							newRow.put(ThothDB.col_T_M_BLOB_DATA_MIME, "text/plain");
						try
						{
							db.insertOrThrow(ThothDB.table_TEMP_MSGS, "", newRow);
						}
						catch (SQLException e)
						{
							// ThothLog.e(e);
						}
						mmsCur.moveToNext();
					}
					if (db.isOpen())
						db.close();
					mmsCur.close();
					Settings.APP_LAST_MSGM_CAPTURE = System.currentTimeMillis();
					Settings.write();
					UpgradeMain.g().UPGRADE.sendEmptyMessage(53);
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
				}
			}
		});
	}
	
	void upgradeDataCapture()
	{
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				long _DAYID, _BOOTID, _UPLOAD, _DOWNLOAD, _TIME;
				_DAYID = Thoth.Data.MOBILE_CURR_DAY_ID;
				_BOOTID = Thoth.Data.MOBILE_NWK_RESET;
				_UPLOAD = NetworkStatistics.getMTBytes();
				_DOWNLOAD = NetworkStatistics.getMRBytes();
				_TIME = System.currentTimeMillis();
				if (Thoth.Data.MOBILE_DAILY_CORRECTION)
				{
					Thoth.Data.MOBILE_DAILY_CORRECTION = false;
					Thoth.Data.MOBILE_DAILY_UL_CORRECTION = _UPLOAD;
					Thoth.Data.MOBILE_DAILY_DL_CORRECTION = _DOWNLOAD;
					if (Thoth.Data.MOBILE_DAILY_UL_CORRECTION >= Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV)
					{
						_UPLOAD = Thoth.Data.MOBILE_DAILY_UL_CORRECTION - Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV;
						Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV += _UPLOAD;
					}
					else
						_UPLOAD = Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
					if (Thoth.Data.MOBILE_DAILY_DL_CORRECTION >= Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV)
					{
						_DOWNLOAD = Thoth.Data.MOBILE_DAILY_DL_CORRECTION - Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV;
						Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV += _DOWNLOAD;
					}
					else
						_DOWNLOAD = Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
					Thoth.Data.write();
				}
				if (Thoth.Data.MOBILE_NEW_DAY && Thoth.Data.MOBILE_CURR_DAY_ID != Thoth.Data.MOBILE_PREV_DAY_ID)
				{
					Thoth.Data.MOBILE_NEW_DAY = false;
					Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV = Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
					Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV = Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
					_UPLOAD -= Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
					_DOWNLOAD -= Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
				}
				_UPLOAD = _UPLOAD < 0 ? 0 : _UPLOAD;
				_DOWNLOAD = _DOWNLOAD < 0 ? 0 : _DOWNLOAD;
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				ContentValues newRow = new ContentValues();
				newRow.put(ThothDB.col_T_D_M_PKEY, _DAYID + _BOOTID);
				newRow.put(ThothDB.col_T_D_M_DAYID, _DAYID);
				newRow.put(ThothDB.col_T_D_M_BOOTID, _BOOTID);
				newRow.put(ThothDB.col_T_D_M_UPLOAD, _UPLOAD);
				newRow.put(ThothDB.col_T_D_M_DOWNLOAD, _DOWNLOAD);
				newRow.put(ThothDB.col_T_D_M_TIME, _TIME);
				try
				{
					db.insertOrThrow(ThothDB.table_TEMP_DATA_MOBL, "", newRow);
				}
				catch (SQLiteConstraintException e)
				{
					try
					{
						db.replaceOrThrow(ThothDB.table_TEMP_DATA_MOBL, "", newRow);
					}
					catch (Exception ee)
					{
						// ThothLog.e(ee);
					}
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
				}
				if (db.isOpen())
					db.close();
			}
		});
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				long _DAYID, _BOOTID, _UPLOAD, _DOWNLOAD, _TIME;
				_DAYID = Thoth.Data.WIFI_CURR_DAY_ID;
				_BOOTID = Thoth.Data.WIFI_NWK_RESET;
				_UPLOAD = NetworkStatistics.getTTBytes() - NetworkStatistics.getMTBytes();
				_DOWNLOAD = NetworkStatistics.getTRBytes() - NetworkStatistics.getMRBytes();
				_TIME = System.currentTimeMillis();
				if (Thoth.Data.WIFI_DAILY_CORRECTION)
				{
					Thoth.Data.WIFI_DAILY_CORRECTION = false;
					Thoth.Data.WIFI_DAILY_UL_CORRECTION = _UPLOAD;
					Thoth.Data.WIFI_DAILY_DL_CORRECTION = _DOWNLOAD;
					if (Thoth.Data.WIFI_DAILY_UL_CORRECTION >= Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV)
					{
						_UPLOAD = Thoth.Data.WIFI_DAILY_UL_CORRECTION - Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV;
						Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV += _UPLOAD;
					}
					else
						_UPLOAD = Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					if (Thoth.Data.WIFI_DAILY_DL_CORRECTION >= Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV)
					{
						_DOWNLOAD = Thoth.Data.WIFI_DAILY_DL_CORRECTION - Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV;
						Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV += _DOWNLOAD;
					}
					else
						_DOWNLOAD = Thoth.Data.WIFI_DAILY_DL_CORRECTION;
					Thoth.Data.WIFI_INTRADAY_UL_CORRECTION = Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV = 0;
					Thoth.Data.WIFI_INTRADAY_DL_CORRECTION = Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV = 0;
					Thoth.Data.write();
				}
				if (Thoth.Data.WIFI_INTRADAY_CORRECTION)
				{
					Thoth.Data.WIFI_INTRADAY_CORRECTION = false;
					Thoth.Data.WIFI_INTRADAY_UL_CORRECTION = _UPLOAD - Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					Thoth.Data.WIFI_INTRADAY_DL_CORRECTION = _DOWNLOAD - Thoth.Data.WIFI_DAILY_DL_CORRECTION;
					if (Thoth.Data.WIFI_INTRADAY_UL_CORRECTION > Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV)
					{
						_UPLOAD = Thoth.Data.WIFI_INTRADAY_UL_CORRECTION - Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV;
						Thoth.Data.WIFI_INTRADAY_UL_CORRECTION_PREV += _UPLOAD;
					}
					else
						_UPLOAD = 0;
					if (Thoth.Data.WIFI_INTRADAY_DL_CORRECTION > Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV)
					{
						_DOWNLOAD = Thoth.Data.WIFI_INTRADAY_DL_CORRECTION - Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV;
						Thoth.Data.WIFI_INTRADAY_DL_CORRECTION_PREV += _DOWNLOAD;
					}
					else
						_DOWNLOAD = 0;
					Thoth.Data.write();
				}
				if (Thoth.Data.WIFI_NEW_DAY && Thoth.Data.WIFI_CURR_DAY_ID != Thoth.Data.WIFI_PREV_DAY_ID)
				{
					Thoth.Data.WIFI_NEW_DAY = false;
					Thoth.Data.WIFI_DAILY_UL_CORRECTION_PREV = Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					Thoth.Data.WIFI_DAILY_DL_CORRECTION_PREV = Thoth.Data.WIFI_DAILY_DL_CORRECTION;
					_UPLOAD -= Thoth.Data.WIFI_DAILY_UL_CORRECTION;
					_DOWNLOAD -= Thoth.Data.WIFI_DAILY_DL_CORRECTION;
				}
				_UPLOAD = _UPLOAD < 0 ? 0 : _UPLOAD;
				_DOWNLOAD = _DOWNLOAD < 0 ? 0 : _DOWNLOAD;
				if (_UPLOAD < 1 || _DOWNLOAD < 1)
					_BOOTID = Thoth.Data.WIFI_NWK_RESET = System.currentTimeMillis();
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				ContentValues newRow = new ContentValues();
				newRow.put(ThothDB.col_T_D_W_PKEY, _DAYID + _BOOTID);
				newRow.put(ThothDB.col_T_D_W_DAYID, _DAYID);
				newRow.put(ThothDB.col_T_D_W_BOOTID, _BOOTID);
				newRow.put(ThothDB.col_T_D_W_UPLOAD, _UPLOAD);
				newRow.put(ThothDB.col_T_D_W_DOWNLOAD, _DOWNLOAD);
				newRow.put(ThothDB.col_T_D_W_TIME, _TIME);
				try
				{
					db.insertOrThrow(ThothDB.table_TEMP_DATA_WIFI, "", newRow);
				}
				catch (SQLiteConstraintException e)
				{
					try
					{
						db.replaceOrThrow(ThothDB.table_TEMP_DATA_WIFI, "", newRow);
					}
					catch (Exception ee)
					{
						// ThothLog.e(ee);
					}
				}
				catch (Exception e)
				{
					// ThothLog.e(e);
				}
				if (db.isOpen())
					db.close();
			}
		});
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				final PackageManager pkgMgr = getPackageManager();
				//
				ThothDB dbA = new ThothDB(cx);
				SQLiteDatabase db = dbA.getDb();
				//
				List<PackageInfo> appList = pkgMgr.getInstalledPackages(0);
				UpgradeMain.max += appList.size();
				int i = 0;
				for (PackageInfo app : appList)
				{
					UpgradeMain.current++;
					Message msg = new Message();
					msg.what = 55;
					msg.arg1 = 3;
					msg.obj = new String("Data logs (" + (++i) + "/" + appList.size() + ")");
					UpgradeMain.g().UPGRADE.sendMessage(msg);
					if (!((app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0))
					{
						int appUID = app.applicationInfo.uid;
						String appName = app.applicationInfo.loadLabel(getPackageManager()).toString();
						String appPackage = app.packageName;
						String appUIDName = pkgMgr.getNameForUid(appUID);
						long appUpload = NetworkStatistics.getATBytes(appUID);
						long appDownload = NetworkStatistics.getARBytes(appUID);
						long appDayId = Thoth.Data.CURR_DAY_ID;
						long appBootId = Thoth.Data.DATA_RESET;
						ContentValues newRow = new ContentValues();
						newRow.put(ThothDB.col_H_A_PKEY, appPackage);
						try
						{
							db.insertOrThrow(ThothDB.table_HASH_APPS, "", newRow);
						}
						catch (Exception e)
						{
							// ThothLog.e(e);
						}
						long UL_CORRECTION = appUpload, UL_CORRECTION_PREV = 0, DL_CORRECTION = appDownload, DL_CORRECTION_PREV = 0;
						Cursor cR = db.rawQuery("SELECT SUM(" + ThothDB.col_T_A_UPLOAD + "), SUM(" + ThothDB.col_T_A_DOWNLOAD + ") FROM " + ThothDB.table_TEMP_APPS + " WHERE " + ThothDB.col_T_A_PKG + "='" + appPackage + "' AND " + ThothDB.col_T_A_DAYID + "=" + Thoth.Data.PREV_DAY_ID + " AND " + ThothDB.col_T_A_BOOTID + "=" + appBootId + " GROUP BY " + ThothDB.col_T_A_DAYID, null);
						if (null != cR && cR.moveToFirst())
						{
							UL_CORRECTION_PREV = cR.getLong(0);
							DL_CORRECTION_PREV = cR.getLong(1);
							cR.close();
						}
						appUpload = (UL_CORRECTION >= UL_CORRECTION_PREV) ? (UL_CORRECTION - UL_CORRECTION_PREV) : UL_CORRECTION;
						appDownload = (DL_CORRECTION >= DL_CORRECTION_PREV) ? (DL_CORRECTION - DL_CORRECTION_PREV) : DL_CORRECTION;
						if (appUpload < 1 && appDownload < 1)
							continue;
						newRow = new ContentValues();
						newRow.put(ThothDB.col_T_A_PKG, appPackage);
						newRow.put(ThothDB.col_T_A_NAME, appName);
						newRow.put(ThothDB.col_T_A_UID, appUID);
						newRow.put(ThothDB.col_T_A_UID_NAME, appUIDName);
						newRow.put(ThothDB.col_T_A_DAYID, appDayId);
						newRow.put(ThothDB.col_T_A_BOOTID, appBootId);
						newRow.put(ThothDB.col_T_A_UPLOAD, appUpload);
						newRow.put(ThothDB.col_T_A_DOWNLOAD, appDownload);
						try
						{
							db.insertOrThrow(ThothDB.table_TEMP_APPS, "", newRow);
						}
						catch (Exception e)
						{
							// ThothLog.e(e);
						}
					}
				}
				if (db.isOpen())
					db.close();
			}
		});
		service.submit(new Runnable()
		{
			@Override
			public void run()
			{
				Settings.APP_LAST_DATA_CAPTURE = System.currentTimeMillis();
				Settings.write();
				UpgradeMain.g().UPGRADE.sendEmptyMessage(54);
			}
		});
	}
}
