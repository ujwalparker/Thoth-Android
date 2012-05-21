package com.gnuc.thoth.service.listeners;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.Thoth.REQ;
import com.gnuc.thoth.framework.Thoth.Settings;
import com.gnuc.thoth.framework.ThothLog;
import com.gnuc.thoth.framework.ThothMail;
import com.gnuc.thoth.framework.ThothProcessor;
import com.gnuc.thoth.framework.callbacks.ThothMailCallback;
import com.gnuc.thoth.framework.callbacks.ThothRequestCallback;
import com.gnuc.thoth.framework.db.ThothDB;
import com.gnuc.thoth.framework.network.ThothSSLSocketFactory;
import com.gnuc.thoth.framework.utils.date.ThothDate;

public class RequestListener
{
	static DefaultHttpClient	CLIENT	= null;
	
	static DefaultHttpClient c() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException
	{
		if (null == CLIENT)
		{
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			SSLSocketFactory sf = new ThothSSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			//
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			//
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
			return CLIENT = new DefaultHttpClient(ccm, params);
		}
		else
			return CLIENT;
	}
	
	public static void onReceive(final int reqCode, final JSONObject obj)
	{
		performOnBackgroundThread(new Runnable()
		{
			@Override
			public void run()
			{
				switch (reqCode)
				{
					case REQ.LOG_MAIL :
					{
						try
						{
							HttpPost req = new HttpPost("https://thoth-server.appspot.com/jobs/rms");
							//
							JSONObject jo = new JSONObject();
							jo.put("ACTION", "LOG_MAIL");
							jo.put("ACCOUNT", Thoth.Settings.USR_DEVICE_ACCOUNT_EMAIL);
							jo.put("DATA", obj);
							//
							req.setEntity(new StringEntity(jo.toString(), "UTF-8"));
							req.setHeader("Accept", "application/json");
							req.setHeader("Content-type", "application/json");
							req.setHeader("Accept-Encoding", "gzip");
							//
							HttpResponse resp = c().execute(req);
							//
							if (resp.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK)
								throw new Exception(EntityUtils.toString(resp.getEntity()));
						}
						catch (Exception e)
						{
							ThothLog.e(e);
						}
						break;
					}
					case REQ.LOG_BACKUP :
					{
						try
						{
							HttpPost req = new HttpPost(REQ.SERVER + "/logger");
							//
							Settings.APP_BACKUP_ID = Settings.USR_DEVICE_ACCOUNT_EMAIL + "_" + Settings.USR_DEVICE_PHONE_NUMBER;
							Settings.write();
							//
							JSONObject jo = new JSONObject();
							jo.put("ACTION", "LOG_BACKUP");
							jo.put("ACCOUNT", Settings.USR_DEVICE_ACCOUNT_EMAIL);
							jo.put("DEVICE", Settings.USR_DEVICE_PHONE_NUMBER);
							jo.put("BACKUP", Settings.APP_BACKUP_ID);
							//
							req.setEntity(new StringEntity(jo.toString(), "UTF-8"));
							req.setHeader("Accept", "application/json");
							req.setHeader("Content-type", "application/json");
							req.setHeader("Accept-Encoding", "gzip");
							//
							HttpResponse resp = c().execute(req);
							//
							String respStr = EntityUtils.toString(resp.getEntity());
							int respCode = resp.getStatusLine().getStatusCode();
							if (respCode == HttpURLConnection.HTTP_OK)
							{
								jo = new JSONObject(respStr);
								if (null != jo)
								{
									File data = Environment.getDataDirectory();
									String currentDBPath = "/data/com.gnuc.thoth/databases/logger.db";
									File currentDB = new File(data, currentDBPath);
									if (currentDB.exists())
									{
										InputStreamBody streamBody = new InputStreamBody(new FileInputStream(currentDB), "application/x-sqlite3", Settings.APP_BACKUP_ID);
										MultipartEntity reqEntity = new MultipartEntity();
										reqEntity.addPart(Settings.APP_BACKUP_ID, streamBody);
										//
										req = new HttpPost(jo.getJSONObject("LOG_DATA").getString("UPLOADURL"));
										req.setEntity(reqEntity);
										resp = c().execute(req);
										respCode = resp.getStatusLine().getStatusCode();
										respStr = EntityUtils.toString(resp.getEntity());
										//
										if (respCode == HttpURLConnection.HTTP_OK)
										{
											Settings.APP_LAST_LOG_BACKUP = ThothDate.now.getMillis();
											Settings.APP_BACKUP_PENDING = false;
											Settings.APP_BACKUP_IN_PROGRESS = false;
											Settings.write();
											//
											Thoth.showNwkBackupNotification();
											Thoth.scheduleServices();
										}
										else
											throw new Exception(respCode + " -> " + respStr);
									}
								}
							}
							else
								throw new Exception(respStr);
						}
						catch (Exception e)
						{
							ThothLog.e(e);
						}
						break;
					}
				}
			}
		});
	}
	
	public static void onReceive(final int reqCode, final ThothRequestCallback reqCallback)
	{
		performOnBackgroundThread(new Runnable()
		{
			@Override
			public void run()
			{
				switch (reqCode)
				{
					case REQ.HELLO :
					{
						try
						{
							HttpResponse resp = c().execute(new HttpGet(REQ.SERVER + "/logger"));
							String respStr = EntityUtils.toString(resp.getEntity());
							//
							if (resp.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK)
								reqCallback.handle(Integer.valueOf(respStr) > Settings.APP_VERSION ? 0 : 1);
							else
								throw new Exception(respStr);
						}
						catch (Exception e)
						{
							ThothLog.e(e);
							reqCallback.handle(-1);
						}
						break;
					}
					case REQ.USR_CHECK :
					{
						try
						{
							HttpPost req = new HttpPost(REQ.SERVER + "/logger");
							JSONObject jo = new JSONObject();
							JSONObject joo = new JSONObject();
							joo.put("ACCOUNT", Thoth.Settings.USR_DEVICE_ACCOUNT_EMAIL);
							jo.put("USER_DATA", joo);
							jo.put("AUTH", Thoth.auth);
							jo.put("ACTION", "USR_CHECK");
							req.setEntity(new StringEntity(jo.toString(), "UTF-8"));
							req.setHeader("Accept", "application/json");
							req.setHeader("Content-type", "application/json");
							req.setHeader("Accept-Encoding", "gzip");
							HttpResponse resp = c().execute(req);
							String respStr = EntityUtils.toString(resp.getEntity());
							int respCode = resp.getStatusLine().getStatusCode();
							if (respCode == HttpURLConnection.HTTP_OK)
								reqCallback.handle(1); // User exists
							else if (respCode == HttpURLConnection.HTTP_NOT_FOUND)
								reqCallback.handle(2); // User does not exist
							else
								throw new Exception(respStr);
						}
						catch (Exception e)
						{
							ThothLog.e(e);
							reqCallback.handle(-1);
						}
						break;
					}
					case REQ.USR_CREATE :
					{
						try
						{
							HttpPost req = new HttpPost(REQ.SERVER + "/logger");
							// Build response
							JSONObject jo = new JSONObject();
							JSONObject joo = new JSONObject();
							JSONObject jooo = new JSONObject();
							jooo.put("dBM", Thoth.Settings.USR_DEVICE_BUILD_MODEL);
							jooo.put("dIMEI", Thoth.Settings.USR_DEVICE_IMEI);
							jooo.put("dBF", Thoth.Settings.USR_DEVICE_BUILD_FIRMWARE);
							jooo.put("dPN", Thoth.Settings.USR_DEVICE_PHONE_NUMBER);
							joo.put("DEVICE_DATA", jooo);
							joo.put("ACCOUNT", Thoth.Settings.USR_DEVICE_ACCOUNT_EMAIL);
							jo.put("USER_DATA", joo);
							jo.put("AUTH", Thoth.auth);
							jo.put("ACTION", "USR_CREATE");
							// Set response
							req.setEntity(new StringEntity(jo.toString(), "UTF-8"));
							req.setHeader("Accept", "application/json");
							req.setHeader("Content-type", "application/json");
							req.setHeader("Accept-Encoding", "gzip");
							HttpResponse resp = c().execute(req);
							String respStr = EntityUtils.toString(resp.getEntity());
							int respCode = resp.getStatusLine().getStatusCode();
							if (respCode == HttpURLConnection.HTTP_OK)
								reqCallback.handle(1); // User create OK
							else if (respCode == HttpURLConnection.HTTP_FORBIDDEN)
								reqCallback.handle(2); // User already exists
							else
								throw new Exception(respStr);
						}
						catch (Exception e)
						{
							ThothLog.e(e);
							reqCallback.handle(-1);
						}
						break;
					}
					case REQ.USR_EDIT :
					{
						try
						{
							HttpPost req = new HttpPost(REQ.SERVER + "/logger");
							// Build response
							JSONObject jo = new JSONObject();
							JSONObject joo = new JSONObject();
							JSONObject jooo = new JSONObject();
							jooo.put("dBM", Thoth.Settings.USR_DEVICE_BUILD_MODEL);
							jooo.put("dIMEI", Thoth.Settings.USR_DEVICE_IMEI);
							jooo.put("dBF", Thoth.Settings.USR_DEVICE_BUILD_FIRMWARE);
							jooo.put("dPN", Thoth.Settings.USR_DEVICE_PHONE_NUMBER);
							joo.put("DEVICE_DATA", jooo);
							joo.put("ACCOUNT", Thoth.Settings.USR_DEVICE_ACCOUNT_EMAIL);
							jo.put("USER_DATA", joo);
							jo.put("AUTH", Thoth.auth);
							jo.put("ACTION", "USR_EDIT");
							//
							req.setEntity(new StringEntity(jo.toString(), "UTF-8"));
							req.setHeader("Accept", "application/json");
							req.setHeader("Content-type", "application/json");
							req.setHeader("Accept-Encoding", "gzip");
							HttpResponse resp = c().execute(req);
							String respStr = EntityUtils.toString(resp.getEntity());
							int respCode = resp.getStatusLine().getStatusCode();
							if (respCode == HttpURLConnection.HTTP_OK)
								reqCallback.handle(1); // Added new device to user
							else if (respCode == HttpURLConnection.HTTP_CONFLICT)
								reqCallback.handle(2); // Device already exists
							else
								throw new Exception(respStr);
						}
						catch (Exception e)
						{
							ThothLog.e(e);
							reqCallback.handle(-1);
						}
						break;
					}
					case REQ.LOG_CREATE :
					{
						boolean isCallLog = false, isMsgsLog = false, isDataLog = false, isWifiLog = false, isMoblLog = false;
						Cursor cR = null;
						//
						ThothDB dbA = new ThothDB(Thoth.cx);
						SQLiteDatabase db = dbA.getDb();
						//
						HashMap<String, JSONArray> map = new HashMap<String, JSONArray>();
						try
						{
							int timeZone = TimeZone.getDefault().getRawOffset();
							JSONArray callL = new JSONArray();
							try
							{
								cR = db.rawQuery("SELECT * FROM " + ThothDB.table_TEMP_CALL + " ORDER BY " + ThothDB.col_T_C_CONTACT + " ASC", null);
								reqCallback.progress(-50, 100, 0);
								if (cR != null && cR.moveToFirst())
								{
									JSONObject ci = null;
									JSONArray ci_cl = null;
									String prevContact = "THOTHANDROIDAPP";
									for (int x = 0; x < cR.getCount(); x++)
									{
										try
										{
											String currContact = cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_C_CONTACT));
											if (prevContact.equalsIgnoreCase(currContact))
											{
												JSONObject cli = new JSONObject();
												cli.put("tyOf", cR.getInt(cR.getColumnIndexOrThrow(ThothDB.col_T_C_TYPE)));
												cli.put("tiOf", cR.getLong(cR.getColumnIndexOrThrow(ThothDB.col_T_C_DATETIME)));
												cli.put("tZOf", timeZone);
												cli.put("dOf", cR.getLong(cR.getColumnIndexOrThrow(ThothDB.col_T_C_DURATION)));
												ci_cl.put(cli);
											}
											else
											{
												if (null == ci && null == ci_cl)
												{
													ci = new JSONObject();
													ci_cl = new JSONArray();
												}
												else
												{
													ci.put("cL", ci_cl);
													callL.put(ci);
													ci = new JSONObject();
													ci_cl = new JSONArray();
												}
												ci.put("cRId", currContact);
												ci.put("cN", cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_C_CONTACTNAME)));
												ci.put("cPN", cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_C_NUMBER)));
												ci.put("cE", cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_C_CONTACTNAME)) + "@" + Thoth.Settings.USR_DEVICE_PHONE_NUMBER);
												JSONObject cli = new JSONObject();
												cli.put("tyOf", cR.getInt(cR.getColumnIndexOrThrow(ThothDB.col_T_C_TYPE)));
												cli.put("tiOf", cR.getLong(cR.getColumnIndexOrThrow(ThothDB.col_T_C_DATETIME)));
												cli.put("tZOf", timeZone);
												cli.put("dOf", cR.getLong(cR.getColumnIndexOrThrow(ThothDB.col_T_C_DURATION)));
												ci_cl.put(cli);
											}
											prevContact = currContact;
											cR.moveToNext();
											if (cR.getCount() > 0 && cR.getCount() < 2)
											{
												ci.put("cL", ci_cl);
												callL.put(ci);
											}
										}
										catch (Exception e)
										{
											ThothLog.e(e);
										}
									}
									map.put("CALL", callL);
									isCallLog = true;
								}
								else
									isCallLog = false;
								if (null != cR && !cR.isClosed())
									cR.close();
							}
							catch (Exception e)
							{
								ThothLog.e(e);
							}
							reqCallback.progress(100, 12, 0);
							// SMS and MMS messages to JSON
							JSONArray msgsL = new JSONArray();
							try
							{
								cR = db.rawQuery("SELECT * FROM " + ThothDB.table_TEMP_MSGS + " ORDER BY " + ThothDB.col_T_M_CONTACT + " ASC", null);
								if (cR != null && cR.moveToFirst())
								{
									JSONObject mi = null;
									JSONArray mi_ml = null;
									String prevContact = "THOTHANDROIDAPP";
									for (int x = 0; x < cR.getCount(); x++)
									{
										try
										{
											String currContact = cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_M_CONTACT));
											if (prevContact.equalsIgnoreCase(currContact))
											{
												JSONObject mli = new JSONObject();
												mli.put("tyOf", cR.getInt(cR.getColumnIndexOrThrow(ThothDB.col_T_M_TYPE)));
												mli.put("tiOf", cR.getLong(cR.getColumnIndexOrThrow(ThothDB.col_T_M_DATETIME)));
												mli.put("tZOf", timeZone);
												mli.put("bOf", cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_M_MSG)));
												mi_ml.put(mli);
											}
											else
											{
												if (null == mi && null == mi_ml)
												{
													mi = new JSONObject();
													mi_ml = new JSONArray();
												}
												else
												{
													mi.put("mL", mi_ml);
													msgsL.put(mi);
													mi = new JSONObject();
													mi_ml = new JSONArray();
												}
												mi = new JSONObject();
												mi_ml = new JSONArray();
												mi.put("cRId", currContact);
												mi.put("cN", cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_M_CONTACTNAME)));
												mi.put("cPN", cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_M_NUMBER)));
												mi.put("cE", cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_M_CONTACTNAME)) + "@" + Thoth.Settings.USR_DEVICE_PHONE_NUMBER);
												JSONObject mli = new JSONObject();
												mli.put("tyOf", cR.getInt(cR.getColumnIndexOrThrow(ThothDB.col_T_M_TYPE)));
												mli.put("tiOf", cR.getLong(cR.getColumnIndexOrThrow(ThothDB.col_T_M_DATETIME)));
												mli.put("tZOf", timeZone);
												mli.put("bOf", cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_M_MSG)));
												mi_ml.put(mli);
											}
											prevContact = currContact;
											cR.moveToNext();
											if (cR.getCount() > 0 && cR.getCount() < 2)
											{
												mi.put("mL", mi_ml);
												msgsL.put(mi);
											}
										}
										catch (Exception e)
										{
											ThothLog.e(e);
										}
									}
									map.put("MSGS", msgsL);
									isMsgsLog = true;
								}
								else
									isMsgsLog = false;
								if (null != cR && !cR.isClosed())
									cR.close();
							}
							catch (Exception e)
							{
								ThothLog.e(e);
							}
							reqCallback.progress(100, 12, 0);
							// Data logs
							JSONArray appsL = new JSONArray();
							if (Settings.APP_ENABLE_DATA_CAPTURE)
							{
								try
								{
									cR = db.rawQuery("SELECT SUM(" + ThothDB.col_T_A_UPLOAD + "),SUM(" + ThothDB.col_T_A_DOWNLOAD + ")," + ThothDB.col_T_A_DAYID + ", " + ThothDB.col_T_A_PKG + ", " + ThothDB.col_T_A_NAME + ", " + ThothDB.col_T_A_UID + " FROM " + ThothDB.table_TEMP_APPS + " GROUP BY " + ThothDB.col_T_A_PKG + ", " + ThothDB.col_T_A_DAYID, null);
									if (cR != null && cR.moveToFirst())
									{
										JSONObject ai = null;
										JSONArray ai_al = null;
										String prevApp = "THOTHANDROIDAPP";
										for (int x = 0; x < cR.getCount(); x++)
										{
											try
											{
												String currApp = cR.getString(3);
												if (prevApp.equalsIgnoreCase(currApp))
												{
													JSONObject ali = new JSONObject();
													ali.put("tiOf", cR.getLong(2));
													ali.put("uL", cR.getLong(0));
													ali.put("dL", cR.getLong(1));
													ai_al.put(ali);
												}
												else
												{
													if (null == ai && null == ai_al)
													{
														ai = new JSONObject();
														ai_al = new JSONArray();
													}
													else
													{
														ai.put("aL", ai_al);
														appsL.put(ai);
														ai = new JSONObject();
														ai_al = new JSONArray();
													}
													ai = new JSONObject();
													ai_al = new JSONArray();
													ai.put("aP", currApp);
													ai.put("aN", cR.getString(4));
													ai.put("aUID", cR.getInt(5));
													//
													JSONObject ali = new JSONObject();
													ali.put("tiOf", cR.getLong(2));
													ali.put("uL", cR.getLong(0));
													ali.put("dL", cR.getLong(1));
													ai_al.put(ali);
												}
												prevApp = currApp;
												cR.moveToNext();
												if (cR.getCount() > 0 && cR.getCount() < 2)
												{
													ai.put("aL", ai_al);
													appsL.put(ai);
												}
											}
											catch (Exception e)
											{
												ThothLog.e(e);
											}
										}
										isDataLog = true;
									}
									else
										isDataLog = false;
									if (null != cR && !cR.isClosed())
										cR.close();
								}
								catch (Exception e)
								{
									ThothLog.e(e);
								}
							}
							reqCallback.progress(100, 12, 0);
							//
							// Device Data logs
							JSONArray wifiL = new JSONArray();
							if (Settings.APP_ENABLE_DATA_CAPTURE)
							{
								try
								{
									cR = db.rawQuery("SELECT " + ThothDB.col_T_D_W_BOOTID + ", " + ThothDB.col_T_D_W_UPLOAD + ", " + ThothDB.col_T_D_W_DOWNLOAD + " FROM " + ThothDB.table_TEMP_DATA_WIFI + " WHERE (" + ThothDB.col_T_D_W_UPLOAD + "+" + ThothDB.col_T_D_W_DOWNLOAD + ")>0", null);
									if (cR != null && cR.moveToFirst())
									{
										for (int x = 0; x < cR.getCount(); x++)
										{
											try
											{
												JSONObject wli = new JSONObject();
												wli.put("tiOf", cR.getLong(0));
												wli.put("uL", cR.getLong(1));
												wli.put("dL", cR.getLong(2));
												wifiL.put(wli);
												cR.moveToNext();
											}
											catch (Exception e)
											{
												ThothLog.e(e);
											}
										}
									}
									else
										isWifiLog = false;
									if (null != cR && !cR.isClosed())
										cR.close();
								}
								catch (Exception e)
								{
									ThothLog.e(e);
								}
							}
							reqCallback.progress(100, 12, 0);
							JSONArray moblL = new JSONArray();
							//
							if (Settings.APP_ENABLE_DATA_CAPTURE)
							{
								try
								{
									cR = db.rawQuery("SELECT " + ThothDB.col_T_D_M_BOOTID + ", " + ThothDB.col_T_D_M_UPLOAD + ", " + ThothDB.col_T_D_M_DOWNLOAD + " FROM " + ThothDB.table_TEMP_DATA_MOBL + " WHERE (" + ThothDB.col_T_D_M_UPLOAD + "+" + ThothDB.col_T_D_M_DOWNLOAD + ")>0", null);
									if (cR != null && cR.moveToFirst())
									{
										for (int x = 0; x < cR.getCount(); x++)
										{
											try
											{
												JSONObject mli = new JSONObject();
												mli.put("tiOf", cR.getLong(0));
												mli.put("uL", cR.getLong(1));
												mli.put("dL", cR.getLong(2));
												moblL.put(mli);
												cR.moveToNext();
											}
											catch (Exception e)
											{
												ThothLog.e(e);
											}
										}
										isMoblLog = true;
									}
									else
										isMoblLog = false;
									if (null != cR && !cR.isClosed())
										cR.close();
								}
								catch (Exception e)
								{
									ThothLog.e(e);
								}
							}
							reqCallback.progress(100, 12, 0);
							// ///////////////////////////
							if (isCallLog || isMsgsLog || isDataLog || isWifiLog || isMoblLog)
							{
								JSONObject jo = new JSONObject();
								JSONObject log_data = new JSONObject();
								JSONObject user_data = new JSONObject();
								log_data.put("CALL", callL);
								log_data.put("MSGS", msgsL);
								log_data.put("APPS", appsL);
								log_data.put("MOBL", moblL);
								log_data.put("WIFI", wifiL);
								//
								user_data.put("DEVICE", Thoth.Settings.USR_DEVICE_PHONE_NUMBER);
								user_data.put("ACCOUNT", Thoth.Settings.USR_DEVICE_ACCOUNT_EMAIL);
								jo.put("USER_DATA", user_data);
								jo.put("LOG_DATA", log_data);
								jo.put("AUTH", Thoth.auth);
								jo.put("ACTION", "LOG_CREATE");
								//
								try
								{
									ThothProcessor.save(jo, reqCallback);
								}
								catch (Exception e)
								{
									ThothLog.e(e);
								}
							}
							if (db.isOpen())
								db.close();
							reqCallback.progress(100, 10, 0);
						}
						catch (Exception e)
						{
							ThothLog.e(e);
							reqCallback.handle(-1);
						}
						finally
						{
							if (null != cR && !cR.isClosed())
								cR.close();
							reqCallback.progress(100, 5, 0);
							if (isMsgsLog && Thoth.Settings.APP_GoMAIL)
							{
								ThothMail.getInstance().sendMail(new ThothMailCallback()
								{
									@Override
									public void handle(boolean result, int resultCode)
									{
										if (result)
										{
											ThothDB dbA = new ThothDB(Thoth.cx);
											dbA.refreshTempTables(dbA.getDb());
											if (dbA.getDb().isOpen())
												dbA.getDb().close();
										}
										//
										reqCallback.progress(100, 5, 0);
										reqCallback.handle(1);
									}
									
									@Override
									public void progress(int resultCode, int progressValue)
									{}
								});
							}
							else
							{
								dbA = new ThothDB(Thoth.cx);
								dbA.refreshTempTables(dbA.getDb());
								if (dbA.getDb().isOpen())
									dbA.getDb().close();
								//
								reqCallback.progress(100, 5, 0);
								reqCallback.handle(1);
							}
						}
						break;
					}
					case REQ.LOG_RESTORE :
					{
						try
						{
							File dir = Environment.getDataDirectory();
							String dbFile = "/data/com.gnuc.thoth/databases/logger.db";
							File current = new File(dir, dbFile);
							if (current.exists())
								current.renameTo(new File(dir, "/data/com.gnuc.thoth/databases/logger_BACKUP.db"));
							//
							InputStream in = null;
							OutputStream out = null;
							HttpResponse resp = c().execute(new HttpGet("https://thoth-server.appspot.com/restore?user=" + Settings.USR_DEVICE_ACCOUNT_EMAIL + "&device=" + Settings.USR_DEVICE_PHONE_NUMBER));
							if (resp.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK)
							{
								try
								{
									in = resp.getEntity().getContent();
									out = new FileOutputStream(new File(dir, dbFile));
									try
									{
										byte[] buffer = new byte[2048];
										while (true)
										{
											int n = in.read(buffer);
											if (n < 0)
												break;
											out.write(buffer, 0, n);
										}
										out.flush();
									}
									finally
									{
										out.close();
									}
									File backup = new File(dir, "/data/com.gnuc.thoth/databases/logger_BACKUP.db");
									backup.delete();
								}
								catch (Exception e)
								{
									File backup = new File(dir, "/data/com.gnuc.thoth/databases/logger_BACKUP.db");
									backup.renameTo(new File(dir, "/data/com.gnuc.thoth/databases/logger.db"));
								}
								finally
								{
									if (null != in)
										in.close();
									Settings.APP_RESTORE_PENDING = false;
									Settings.APP_RESTORE_IN_PROGRESS = false;
									Settings.write();
									//
									reqCallback.handle(1);
								}
							}
							else
								reqCallback.handle(-1);
						}
						catch (Exception e)
						{
							ThothLog.e(e);
							reqCallback.handle(-1);
						}
						break;
					}
					case REQ.REPORT_MAIL :
					{
						try
						{
							String reportFile = "Thoth-Log.xlsx";
							InputStream in = null;
							FileOutputStream out = null;
							HttpResponse resp = c().execute(new HttpGet("https://thoth-server.appspot.com/report?user=" + Settings.USR_DEVICE_ACCOUNT_EMAIL + "&device=" + Settings.USR_DEVICE_PHONE_NUMBER));
							if (resp.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK)
							{
								in = resp.getEntity().getContent();
								out = Thoth.cx.openFileOutput(reportFile, Context.MODE_PRIVATE);
								try
								{
									byte[] buffer = new byte[2048];
									while (true)
									{
										int n = in.read(buffer);
										if (n < 0)
											break;
										out.write(buffer, 0, n);
									}
									out.flush();
								}
								finally
								{
									out.close();
								}
								if (null != in)
									in.close();
								reqCallback.handle(1);
							}
							else
								throw new Exception("FAILED - Status : " + resp.getStatusLine().getStatusCode());
						}
						catch (Exception e)
						{
							ThothLog.e(e);
							reqCallback.handle(-1);
						}
						break;
					}
				}
			}
		});
	}
	
	private static Thread performOnBackgroundThread(final Runnable runnable)
	{
		final Thread t = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					runnable.run();
				}
				finally
				{}
			}
		};
		t.start();
		return t;
	}
	
	public static String kbSizer(double size)
	{
		final double BASE = 1024, KB = BASE, MB = KB * BASE, GB = MB * BASE;
		final DecimalFormat df = new DecimalFormat("#.##");
		size = Math.abs(size);
		return (size >= GB) ? df.format(size / GB) + " GB" : (size >= MB) ? df.format(size / MB) + " MB" : (size >= KB) ? df.format(size / KB) + " KB" : df.format(size) + " bytes";
	}
}
