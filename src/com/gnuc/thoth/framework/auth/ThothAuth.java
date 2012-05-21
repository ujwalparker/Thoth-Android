package com.gnuc.thoth.framework.auth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.ThothLog;
import com.gnuc.thoth.framework.callbacks.ThothAccountCallback;
import com.gnuc.thoth.framework.utils.MD5;

public class ThothAuth
{
	private static final String	baseUrl		= "https://thoth-server.appspot.com";
	private static final String	loginUrl		= baseUrl + "/_ah/login";
	private static final String	checkUrl		= baseUrl + "/app/hello";
	private static String			authToken	= null;
	private static Cookie			authCookie	= null;
	public static boolean			authTimeout	= false;
	
	public static String getAuthToken()
	{
		return authToken;
	}
	
	public static Cookie getAuthCookie()
	{
		return authCookie;
	}
	
	public static void setAuthToken(Account account, boolean invalidate)
	{
		AccountManager am = AccountManager.get(Thoth.cx);
		AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>()
		{
			@Override
			public void run(AccountManagerFuture<Bundle> future)
			{
				try
				{
					authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
				}
				catch (Exception e)
				{
					ThothLog.e(e);
				}
			}
		};
		am.getAuthToken(account, "ah", false, callback, null);
		if (invalidate && authToken != null)
		{
			am.invalidateAuthToken(account.type, authToken);
			setAuthToken(account, false);
		}
	}
	
	public static void setAuthCookie()
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
						try
						{
							HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
							BasicHttpParams params = new BasicHttpParams();
							SchemeRegistry schemeRegistry = new SchemeRegistry();
							schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
							SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
							schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
							ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
							//
							HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
							DefaultHttpClient client = new DefaultHttpClient(cm, params);
							//
							String cookieUrl = loginUrl + "?continue=" + URLEncoder.encode(checkUrl, "UTF-8") + "&auth=" + URLEncoder.encode(authToken, "UTF-8");
							HttpGet req = new HttpGet(cookieUrl);
							client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
							HttpResponse resp = client.execute(req);
							String respStr = EntityUtils.toString(resp.getEntity());
							int respCode = resp.getStatusLine().getStatusCode();
							if (respCode == HttpURLConnection.HTTP_OK || respCode == HttpURLConnection.HTTP_MOVED_TEMP)
							{
								for (Cookie cookie : client.getCookieStore().getCookies())
								{
									if (cookie.getName().equalsIgnoreCase("SACSID"))
									{
										authCookie = cookie;
										Thoth.client = null;
										Thoth.client = client;
										Thoth.auth = MD5.getMD5HEX(cookie.getValue());
										break;
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
					}
				}.run();
			}
		}.start();
	}
	
	public static void authenticate(final ThothAccountCallback authCallback, final Handler hdlr)
	{
		if (Thoth.Settings.USR_DEVICE_ACCOUNT != 0)
		{
			authToken = null;
			authCookie = null;
			for (Account account : AccountManager.get(Thoth.cx).getAccounts())
			{
				if (account.hashCode() == Thoth.Settings.USR_DEVICE_ACCOUNT)
				{
					setAuthToken(account, true);
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
									hdlr.sendEmptyMessageDelayed(-999, 15000);
									while (!authTimeout && authToken == null)
									{
										try
										{
											Thread.sleep(500);
										}
										catch (InterruptedException e)
										{
											e.printStackTrace();
										}
									}
									if (authToken == null && authTimeout)
									{
										authTimeout = false;
										authCallback.handle(-3);
										return;
									}
									else if (authToken != null)
									{
										hdlr.removeMessages(-999);
										authTimeout = false;
										setAuthCookie();
										hdlr.sendEmptyMessageDelayed(-999, 15000);
										while (!authTimeout && authCookie == null)
										{
											try
											{
												Thread.sleep(500);
											}
											catch (InterruptedException e)
											{
												e.printStackTrace();
											}
										}
										if (authCookie == null && authTimeout)
										{
											authTimeout = false;
											authCallback.handle(-3);
											return;
										}
										hdlr.removeMessages(-999);
										authCallback.handle(1);
									}
								}
							}.run();
						}
					}.start();
				}
			}
		}
		else
			authCallback.handle(-1); // Account not set
	}
	
	public static void authenticate(Account account, final ThothAccountCallback authCallback, final Handler hdlr)
	{
		setAuthToken(account, true);
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
						if (authToken != null)
						{
							setAuthCookie();
							hdlr.sendEmptyMessageDelayed(-999, 30000);
							while (!authTimeout && null == authCookie)
							{
								try
								{
									Thread.sleep(500);
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
							}
							if (authCookie == null && authTimeout)
							{
								authTimeout = false;
								authCallback.handle(-2);
								return;
							}
							hdlr.removeMessages(-999);
							authCallback.handle(1);
						}
						else
						{
							ThothLog.e(new Exception("authToken was null"));
							authCallback.handle(-1);
						}
					}
				}.run();
			}
		}.start();
	}
	
	/**
	 * Method checks if the passed account has been granted access to be used by Thoth
	 * 
	 * @param account
	 *           is the account chosen from account list during setup.
	 */
	public static void checkAccount(final Account account, final Activity activity, final ThothAccountCallback checkCallback)
	{
		AccountManager am = AccountManager.get(Thoth.cx);
		AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>()
		{
			@Override
			public void run(AccountManagerFuture<Bundle> future)
			{
				try
				{
					authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
					if (authToken != null)
					{
						setAuthToken(account, true);
						checkCallback.handle(0);
					}
				}
				catch (OperationCanceledException e)
				{
					ThothLog.e(e);
					checkCallback.handle(-1);
				}
				catch (AuthenticatorException e)
				{
					ThothLog.e(e);
					checkCallback.handle(-1);
				}
				catch (IOException e)
				{
					ThothLog.e(e);
					checkCallback.handle(-1);
				}
				catch (Exception e)
				{
					ThothLog.e(e);
					checkCallback.handle(-1);
				}
			}
		};
		am.getAuthToken(account, "ah", null, activity, callback, null);
	}
}
