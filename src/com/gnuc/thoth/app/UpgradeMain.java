package com.gnuc.thoth.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gnuc.thoth.R;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.Thoth.Settings;
import com.gnuc.thoth.framework.ThothLog;
import com.gnuc.thoth.framework.callbacks.ThothRequestCallback;
import com.gnuc.thoth.service.listeners.RequestListener;

public class UpgradeMain
{
	static UpgradeMain			instance					= null;
	Activity							p							= null;
	AlertDialog						ad							= null;
	//
	ProgressBar						pb							= null;
	//
	boolean							SETUP_CALL_CAPTURE	= true;
	boolean							SETUP_MSGS_CAPTURE	= true;
	boolean							SETUP_MSGM_CAPTURE	= true;
	boolean							SETUP_DATA_CAPTURE	= true;
	boolean							captureTimeout			= false;
	//
	public volatile static int	max						= 1;
	public volatile static int	current					= 0;
	
	//
	public UpgradeMain(Activity parent, AlertDialog dialog)
	{
		p = parent;
		ad = dialog;
		ad.findViewById(R.id.st_set_upgrade_2_1).setVisibility(View.VISIBLE);
		ad.findViewById(R.id.st_set_upgrade_2_2).setVisibility(View.VISIBLE);
		ad.findViewById(R.id.st_set_upgrade_2_3).setVisibility(View.VISIBLE);
		ad.findViewById(R.id.st_set_upgrade_2_4).setVisibility(View.VISIBLE);
		pb = (ProgressBar) ad.findViewById(R.id.st_set_upgrade_2_pb2);
	}
	
	public static UpgradeMain g(UpgradeMain um)
	{
		return instance = um;
	}
	
	public static UpgradeMain g()
	{
		return instance;
	}
	
	public Handler	UPGRADE	= new Handler()
									{
										@Override
										public void handleMessage(final Message msg)
										{
											try
											{
												switch (msg.what)
												{
													case 1 :
													{
														max = 1;
														current = 0;
														//
														captureLogs();
														break;
													}
													case 51 : // Call capture complete
													{
														SETUP_CALL_CAPTURE = false;
														ThothLog.dL("SETUP_CALL_CAPTURE COMPLETE -> " + System.currentTimeMillis());
														break;
													}
													case 52 : // SMS capture complete
													{
														SETUP_MSGS_CAPTURE = false;
														ThothLog.dL("SETUP_MSGS_CAPTURE COMPLETE -> " + System.currentTimeMillis());
														break;
													}
													case 53 : // MMS capture complete
													{
														SETUP_MSGM_CAPTURE = false;
														ThothLog.dL("SETUP_MSGM_CAPTURE COMPLETE -> " + System.currentTimeMillis());
														break;
													}
													case 54 : // Data capture complete
													{
														SETUP_DATA_CAPTURE = false;
														ThothLog.dL("SETUP_DATA_CAPTURE COMPLETE -> " + System.currentTimeMillis());
														break;
													}
													case 55 :
													{
														p.runOnUiThread(new Runnable()
														{
															@Override
															public void run()
															{
																try
																{
																	pb.setMax(max);
																	pb.setProgress(current);
																	//
																	TextView tv = null;
																	switch (msg.arg1)
																	{
																		case 0 :
																			tv = ((TextView) ad.findViewById(R.id.st_set_upgrade_2_1_title));
																			tv.setText("");
																			tv.setText((String) msg.obj);
																			break;
																		case 1 :
																			tv = ((TextView) ad.findViewById(R.id.st_set_upgrade_2_2_title));
																			tv.setText("");
																			tv.setText((String) msg.obj);
																			break;
																		case 2 :
																			tv = ((TextView) ad.findViewById(R.id.st_set_upgrade_2_3_title));
																			tv.setText("");
																			tv.setText((String) msg.obj);
																			break;
																		case 3 :
																			tv = ((TextView) ad.findViewById(R.id.st_set_upgrade_2_4_title));
																			tv.setText("");
																			tv.setText((String) msg.obj);
																			break;
																	}
																}
																catch (Exception e)
																{
																	ThothLog.e(e);
																}
															}
														});
														break;
													}
													case -888 :
													{
														synchronized (this)
														{
															captureTimeout = true;
														};
														break;
													}
													case -1 :
													{
														if (null != msg.obj) { throw new Exception((String) msg.obj); }
														break;
													}
												}
											}
											catch (Exception e)
											{
												ThothLog.e(e);
												Main.getInstance().mainHandler.sendEmptyMessage(-1);
											}
										}
									};
	
	void captureLogs()
	{
		try
		{
			p.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					ad.findViewById(R.id.st_set_upgrade_2).setVisibility(View.VISIBLE);
					ad.findViewById(R.id.st_set_upgrade_2_1).setVisibility(View.VISIBLE);
					ad.findViewById(R.id.st_set_upgrade_2_2).setVisibility(View.VISIBLE);
					ad.findViewById(R.id.st_set_upgrade_2_3).setVisibility(View.VISIBLE);
					ad.findViewById(R.id.st_set_upgrade_2_4).setVisibility(View.VISIBLE);
					pb = (ProgressBar) ad.findViewById(R.id.st_set_upgrade_2_pb2);
				}
			});
			waitForLogCapture(new ThothRequestCallback()
			{
				@Override
				public void handle(final int resultCode)
				{
					if (resultCode != 1)
						ThothLog.e(new Exception("Exception -> during upgrade in captureLogs. Continuing"));
					p.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							ad.findViewById(R.id.st_set_upgrade_2_2_pb).setVisibility(View.GONE);
							ad.findViewById(R.id.st_set_upgrade_2_2_ok).setVisibility(View.VISIBLE);
							((TextView) ad.findViewById(R.id.st_set_upgrade_2_title)).setTextColor(Color.rgb(153, 255, 102));
							ad.findViewById(R.id.st_set_upgrade_2_pb1).setVisibility(View.GONE);
							ad.findViewById(R.id.st_set_upgrade_2_pb2).setVisibility(View.GONE);
							ad.findViewById(R.id.st_set_upgrade_2_ok).setVisibility(View.VISIBLE);
							ad.findViewById(R.id.st_set_upgrade_2_4).setVisibility(View.GONE);
							ad.findViewById(R.id.st_set_upgrade_2_3).setVisibility(View.GONE);
							ad.findViewById(R.id.st_set_upgrade_2_2).setVisibility(View.GONE);
							ad.findViewById(R.id.st_set_upgrade_2_1).setVisibility(View.GONE);
							ad.findViewById(R.id.st_set_upgrade_3).setVisibility(View.VISIBLE);
							ad.findViewById(R.id.st_set_upgrade_3_1).setVisibility(View.VISIBLE);
							pb = (ProgressBar) ad.findViewById(R.id.st_set_upgrade_3_pb2);
							pb.setMax(0);
							pb.setProgress(0);
						}
					});
					RequestListener.onReceive(Thoth.REQ.LOG_CREATE, new ThothRequestCallback()
					{
						@Override
						public void handle(int rC)
						{
							switch (rC)
							{
								case 1 :
								case 4 :
								{
									p.runOnUiThread(new Runnable()
									{
										@Override
										public void run()
										{
											ad.findViewById(R.id.st_set_upgrade_3_1_pb).setVisibility(View.GONE);
											ad.findViewById(R.id.st_set_upgrade_3_1_ok).setVisibility(View.VISIBLE);
											TextView tvs = (TextView) ad.findViewById(R.id.st_set_upgrade_3_1_title);
											tvs.setText("");
											tvs.setText("Log Process (100/100)");
										}
									});
									collectionComplete();
									break;
								}
								case -1 :
								{
									Exception e = new Exception("Exception during upgrade in captureLogs. Continuing");
									ThothLog.e(e);
									//
									p.runOnUiThread(new Runnable()
									{
										@Override
										public void run()
										{
											ad.findViewById(R.id.st_set_upgrade_3_1_pb).setVisibility(View.GONE);
											ad.findViewById(R.id.st_set_upgrade_3_1_ok).setVisibility(View.VISIBLE);
										}
									});
									//
									collectionComplete();
									break;
								}
							}
						}
						
						@Override
						public void progress(final int rC, final int rV, final int r)
						{
							p.runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									if (rC == -50)
										pb.setMax(pb.getMax() + rV);
									else
									{
										pb.setProgress(pb.getProgress() + rV);
										TextView tv = (TextView) ad.findViewById(R.id.st_set_upgrade_3_1_title);
										tv.setText("");
										tv.setText("Log Process (" + pb.getProgress() + "/" + pb.getMax() + ")");
									}
								}
							});
						}
					});
				}
				
				@Override
				public void progress(int resultCode, int progressValue, final int r)
				{}
			});
		}
		catch (Exception e)
		{
			ThothLog.e(e);
		}
	}
	
	void waitForLogCapture(final ThothRequestCallback callback)
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
						UPGRADE.sendEmptyMessageDelayed(-888, 600000);
						while (!captureTimeout)
						{
							try
							{
								p.runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										if (!SETUP_CALL_CAPTURE)
										{
											((TextView) ad.findViewById(R.id.st_set_upgrade_2_1_title)).setTextColor(Color.rgb(153, 255, 102));
											ad.findViewById(R.id.st_set_upgrade_2_1_pb).setVisibility(View.GONE);
											ad.findViewById(R.id.st_set_upgrade_2_1_ok).setVisibility(View.VISIBLE);
											ad.findViewById(R.id.st_set_upgrade_2_2_pb).setVisibility(View.VISIBLE);
										}
										if (!SETUP_MSGS_CAPTURE)
										{
											((TextView) ad.findViewById(R.id.st_set_upgrade_2_2_title)).setTextColor(Color.rgb(153, 255, 102));
											ad.findViewById(R.id.st_set_upgrade_2_2_pb).setVisibility(View.GONE);
											ad.findViewById(R.id.st_set_upgrade_2_2_ok).setVisibility(View.VISIBLE);
											ad.findViewById(R.id.st_set_upgrade_2_3_pb).setVisibility(View.VISIBLE);
										}
										if (!SETUP_MSGM_CAPTURE)
										{
											((TextView) ad.findViewById(R.id.st_set_upgrade_2_3_title)).setTextColor(Color.rgb(153, 255, 102));
											ad.findViewById(R.id.st_set_upgrade_2_3_pb).setVisibility(View.GONE);
											ad.findViewById(R.id.st_set_upgrade_2_3_ok).setVisibility(View.VISIBLE);
											ad.findViewById(R.id.st_set_upgrade_2_4_pb).setVisibility(View.VISIBLE);
										}
										if (!SETUP_DATA_CAPTURE)
										{
											((TextView) ad.findViewById(R.id.st_set_upgrade_2_4_title)).setTextColor(Color.rgb(153, 255, 102));
											ad.findViewById(R.id.st_set_upgrade_2_4_pb).setVisibility(View.GONE);
											ad.findViewById(R.id.st_set_upgrade_2_4_ok).setVisibility(View.VISIBLE);
										}
									}
								});
								//
								if (!SETUP_CALL_CAPTURE && !SETUP_MSGS_CAPTURE && !SETUP_DATA_CAPTURE)
									captureTimeout = true;
								else
									Thread.sleep(500);
							}
							catch (InterruptedException e)
							{
								// ThothLog.e(e);
							}
						}
						if (!SETUP_CALL_CAPTURE && !SETUP_MSGS_CAPTURE && !SETUP_MSGM_CAPTURE && !SETUP_DATA_CAPTURE)
						{
							captureTimeout = false;
							UPGRADE.removeMessages(-888);
							callback.handle(1);
							return;
						}
						else if (captureTimeout) // Timeout
						{
							captureTimeout = false;
							callback.handle(-1);
							return;
						}
					}
				}.run();
			};
		}.start();
	}
	
	void collectionComplete()
	{
		captureTimeout = false;
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
						p.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								ad.findViewById(R.id.st_set_upgrade_3_1).setVisibility(View.GONE);
								pb.setMax(0);
								pb.setProgress(0);
								ad.findViewById(R.id.st_set_upgrade_3_2).setVisibility(View.GONE);
								ad.findViewById(R.id.st_set_upgrade_3_1).setVisibility(View.GONE);
								ad.findViewById(R.id.st_set_upgrade_3_pb1).setVisibility(View.GONE);
								ad.findViewById(R.id.st_set_upgrade_3_pb2).setVisibility(View.GONE);
								((TextView) ad.findViewById(R.id.st_set_upgrade_3_title)).setTextColor(Color.rgb(153, 255, 102));
								//
								try
								{
									Settings.APP_VERSION = (p.getPackageManager().getPackageInfo("com.gnuc.thoth", 0)).versionCode;
								}
								catch (NameNotFoundException e)
								{}
								//
								Settings.APP_SETUP = Settings.APP_UPGRADE = false; // Setup is complete.
								Settings.APP_LOG_FILTER = 0;
								Settings.write();
								//
								Thoth.TRACKER.trackPageView("/UPGRADE_COMPLETE");
								//
								ad.dismiss();
								//
								TabMain.g().TABMAIN.sendEmptyMessage(1);
							}
						});
					}
				}.run();
			};
		}.start();
	}
}
