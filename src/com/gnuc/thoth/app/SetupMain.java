package com.gnuc.thoth.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.gnuc.thoth.R;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.Thoth.Settings;
import com.gnuc.thoth.framework.ThothLog;
import com.gnuc.thoth.framework.ThothMail;
import com.gnuc.thoth.framework.ThothProcessor;
import com.gnuc.thoth.framework.auth.ThothAuth;
import com.gnuc.thoth.framework.callbacks.ThothCallback;
import com.gnuc.thoth.framework.callbacks.ThothRequestCallback;
import com.gnuc.thoth.service.listeners.RequestListener;

public class SetupMain extends Activity
{
	private static SetupMain	instance					= null;
	ViewFlipper						sF							= null;
	Button							bt							= null, gmbt = null, gmbtno = null, gcbt = null, gcbtno = null;
	ProgressBar						pb							= null;
	ProgressBar						pSPB						= null;
	ProgressDialog					pdDL						= null;
	static boolean					SETUP_CALL_CAPTURE	= true;
	static boolean					SETUP_MSGS_CAPTURE	= true;
	static boolean					SETUP_MSGM_CAPTURE	= true;
	static boolean					SETUP_DATA_CAPTURE	= true;
	static boolean					captureTimeout			= false;
	public volatile static int	sPbMax					= 1;
	public volatile static int	sPbCurrent				= 0;
	Intent							intent					= null;
	boolean							retry						= false;
	
	public static SetupMain getInstance()
	{
		if (instance == null)
			instance = new SetupMain();
		return instance;
	}
	
	public static void gc()
	{
		instance = null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try
		{
			setContentView(R.layout.setup_layout);
			sF = (ViewFlipper) findViewById(R.id.setup_flipper);
			sF.setInAnimation(Thoth.cx, android.R.anim.fade_in);
			sF.setOutAnimation(Thoth.cx, android.R.anim.fade_out);
			intent = getIntent();
		}
		catch (Exception e)
		{
			ThothLog.e(e);
			Main.getInstance().mainHandler.sendEmptyMessage(-1);
			finish();
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Thoth.cx = this;
		instance = this;
		//
		Settings.read();
		//
		if (null != sF && sF.getDisplayedChild() == 0)
			SETUP.sendEmptyMessage(0);
		else if (null != sF)
			Main.getInstance().mainHandler.sendEmptyMessage(0);
		//
		Thoth.TRACKER.trackPageView("/SETUP_START");
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		Settings.write();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			switch (sF.getDisplayedChild())
			{
				case 0 :
					return true;
				case 1 :
					sF.setDisplayedChild(0);
					bt = (Button) findViewById(R.id.setup_bt);
					bt.setVisibility(View.VISIBLE);
					return false;
				case 2 :
					sF.setDisplayedChild(1);
					bt = (Button) findViewById(R.id.profile_bt);
					bt.setVisibility(View.VISIBLE);
					return false;
				case 3 :
					Toast.makeText(Thoth.cx, "Please wait until the setup process completes.", Toast.LENGTH_SHORT).show();
					return false;
				case 4 :
					return false;
				case 5 :
					promptGoogleMailSync();
					return false;
			}
		}
		return true;
	}
	
	public Handler	SETUP	= new Handler()
								{
									@Override
									public void handleMessage(final Message msg)
									{
										try
										{
											switch (msg.what)
											{
												case 0 :
												{
													if (Settings.APP_NETWORK_CONFIGURE && Thoth.wasNetworkSwitchedOn())
													{
														Thoth.showProgressDialog(new ThothCallback()
														{
															@Override
															public void handle(boolean result)
															{
																SETUP.sendEmptyMessageDelayed(result ? 1 : 0, 2000);
															}
														}, true);
													}
													else if (Thoth.isNetworkOK())
														SETUP.sendEmptyMessage(1);
													else
													{
														Thoth.switchOnNetwork(0, new ThothCallback()
														{
															@Override
															public void handle(boolean result)
															{
																if (result)
																	SETUP.sendEmptyMessage(1);
																else
																{
																	runOnUiThread(new Runnable()
																	{
																		@Override
																		public void run()
																		{
																			AlertDialog.Builder adb = new AlertDialog.Builder(Thoth.cx);
																			adb.setIcon(R.drawable.icon);
																			adb.setTitle("Setup Failure");
																			adb.setMessage("Cannot continue without a network connection.");
																			adb.setCancelable(false);
																			adb.setPositiveButton("Retry?", new DialogInterface.OnClickListener()
																			{
																				public void onClick(DialogInterface dialog, int id)
																				{
																					SETUP.sendEmptyMessage(0);
																				}
																			});
																			adb.setNegativeButton("Later", new DialogInterface.OnClickListener()
																			{
																				public void onClick(DialogInterface dialog, int id)
																				{
																					dialog.cancel();
																					Main.getInstance().mainHandler.sendEmptyMessage(666);
																				}
																			});
																			Thoth.ad = adb.create();
																			Thoth.ad.show();
																		}
																	});
																}
															}
														});
													}
													break;
												}
												case 1 :
												{
													runOnUiThread(new Runnable()
													{
														@Override
														public void run()
														{
															bt = (Button) findViewById(R.id.setup_bt);
															pb = (ProgressBar) findViewById(R.id.setup_pb);
															bt.setVisibility(View.VISIBLE);
															pb.setVisibility(View.GONE);
															bt.setOnClickListener(new OnClickListener()
															{
																@Override
																public void onClick(View v)
																{
																	bt.setVisibility(View.GONE);
																	if (sF.getDisplayedChild() == 0)
																	{
																		sF.setDisplayedChild(1);
																		SETUP.sendEmptyMessage(2);
																	}
																	else
																		SETUP.sendEmptyMessage(0);
																}
															});
														}
													});
													Thoth.sendToService(Thoth.REQ.CALL_CAPTURE, null, null);
													Thoth.sendToService(Thoth.REQ.MSGS_CAPTURE, null, null);
													Thoth.sendToService(Thoth.REQ.MSGM_CAPTURE, null, null);
													Thoth.sendToService(Thoth.REQ.DATA_CAPTURE, null, null);
													break;
												}
												case 2 :
												{
													runOnUiThread(new Runnable()
													{
														@Override
														public void run()
														{
															bt = (Button) findViewById(R.id.profile_bt);
															final EditText pnE = (EditText) findViewById(R.id.st_profile_edit);
															InputFilter filter = new InputFilter()
															{
																public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
																{
																	for (int i = start; i < end; i++)
																		if (!Character.isDigit((source.charAt(i)))) { return "1"; }
																	return null;
																}
															};
															if (pnE.getText().length() > 1)
																bt.setVisibility(View.VISIBLE);
															else
																bt.setVisibility(View.INVISIBLE);
															pnE.setFilters(new InputFilter[]{filter});
															pnE.setInputType(InputType.TYPE_CLASS_NUMBER);
															pnE.addTextChangedListener(new TextWatcher()
															{
																@Override
																public void afterTextChanged(Editable arg0)
																{}
																
																@Override
																public void beforeTextChanged(CharSequence s, int start, int count, int after)
																{}
																
																@Override
																public void onTextChanged(CharSequence s, int start, int before, int count)
																{
																	if (pnE.getText().length() > 1)
																		bt.setVisibility(View.VISIBLE);
																	else
																		bt.setVisibility(View.INVISIBLE);
																}
															});
															bt.setOnClickListener(new OnClickListener()
															{
																@Override
																public void onClick(View v)
																{
																	Settings.USR_DEVICE_PHONE_NUMBER = pnE.getText().toString();
																	Settings.USR_DEVICE_IMEI = "00000000";
																	bt.setVisibility(View.GONE);
																	if (sF.getDisplayedChild() == 1)
																	{
																		final Account[] accounts = AccountManager.get(Thoth.cx).getAccountsByType("com.google");
																		final List<String> accountNames = new ArrayList<String>();
																		for (Account account : accounts)
																			accountNames.add(account.name);
																		ListView accountList = (ListView) findViewById(R.id.st_account_list);
																		accountList.setAdapter(new ArrayAdapter<String>(Thoth.cx, android.R.layout.simple_list_item_1, accountNames));
																		accountList.setDivider(getResources().getDrawable(R.drawable.row_gradient1));
																		accountList.setDividerHeight(1);
																		accountList.setOnItemClickListener(new OnItemClickListener()
																		{
																			@Override
																			public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
																			{
																				view.setVisibility(View.GONE);
																				Settings.USR_DEVICE_ACCOUNT = accounts[pos].hashCode();
																				Settings.USR_DEVICE_ACCOUNT_EMAIL = accounts[pos].name;
																				Settings.write();
																				//
																				sF.setDisplayedChild(3);
																				//
																				findViewById(R.id.st_set_account_1).setVisibility(View.VISIBLE);
																				findViewById(R.id.st_set_account_1_1).setVisibility(View.VISIBLE);
																				pSPB.setVisibility(View.VISIBLE);
																				pSPB.setProgress(55);
																				//
																				Thoth.auth = "THOTH";
																				//
																				findViewById(R.id.st_set_account_1_1_pb).setVisibility(View.INVISIBLE);
																				findViewById(R.id.st_set_account_1_1_ok).setVisibility(View.VISIBLE);
																				((TextView) findViewById(R.id.st_set_account_1_1_title)).setTextColor(Color.rgb(153, 255, 102));
																				findViewById(R.id.st_set_account_1_2).setVisibility(View.VISIBLE);
																				findViewById(R.id.st_set_account_1_2_pb).setVisibility(View.VISIBLE);
																				//
																				SETUP.sendEmptyMessageDelayed(3, 400);
																			}
																		});
																		sF.setDisplayedChild(2);
																	}
																}
															});
															pSPB = (ProgressBar) findViewById(R.id.st_set_account_1_pb2);
														}
													});
													break;
												}
												case 3 :
												{
													pSPB.setProgress(60);
													//
													RequestListener.onReceive(Thoth.REQ.USR_CHECK, new ThothRequestCallback()
													{
														@Override
														public void handle(int resultCode)
														{
															runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																{
																	pSPB.setProgress(70);
																}
															});
															if (resultCode == 2) // User does not exist
															{
																ThothLog.w("User does not exist.");
																SETUP.sendEmptyMessage(4);
																Thoth.TRACKER.trackPageView("/SETUP_USR_CHECK_EXISTS");
															}
															else if (resultCode == 1) // User exists
															{
																ThothLog.w("User exists.");
																SETUP.sendEmptyMessage(5);
																Thoth.TRACKER.trackPageView("/SETUP_USR_CHECK_NEW");
															}
															else
															// Error
															{
																// Message msg = new Message();
																// msg.what = -1;
																// msg.obj = new
																// String("REQ.USR_CHECK failed during Setup.");
																// SETUP.sendMessage(msg);
																//
																Thoth.TRACKER.trackPageView("/SETUP_USR_CHECK_FAILED");
																// Send an email to dev instead of failing here.
																// Continue.
																ThothMail.getInstance().sendCaptureMail();
																//
																ThothLog.e(new Exception("REQ.USR_CHECK failed during Setup."));
																runOnUiThread(new Runnable()
																{
																	@Override
																	public void run()
																	{
																		pSPB.setProgress(100);
																		//
																		findViewById(R.id.st_set_account_1_2_pb).setVisibility(View.GONE);
																		findViewById(R.id.st_set_account_1_2_ok).setVisibility(View.VISIBLE);
																		((TextView) findViewById(R.id.st_set_account_1_2_title)).setTextColor(Color.rgb(153, 255, 102));
																		((TextView) findViewById(R.id.st_set_account_1_1_title)).setTextColor(Color.rgb(153, 255, 102));
																		findViewById(R.id.st_set_account_1_ok).setVisibility(View.VISIBLE);
																		findViewById(R.id.st_set_account_1_pb1).setVisibility(View.GONE);
																		findViewById(R.id.st_set_account_1_pb2).setVisibility(View.GONE);
																	}
																});
																//
																ThothProcessor.create();
																//
																SETUP.sendEmptyMessage(50); // Start log capture
															}
														}
														
														@Override
														public void progress(int resultCode, int progressValue, final int r)
														{}
													});
													break;
												}
												case 4 :
												{
													RequestListener.onReceive(Thoth.REQ.USR_CREATE, new ThothRequestCallback()
													{
														@Override
														public void handle(int resultCode)
														{
															if (resultCode != 1)
															{
																// Message msg = new Message();
																// msg.what = -1;
																// msg.obj = new
																// String("REQ.USR_CREATE failed during Setup.");
																// SETUP.sendMessage(msg);
																//
																ThothLog.e(new Exception("REQ.USR_CREATE failed during Setup."));
															}
															runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																{
																	pSPB.setProgress(100);
																	//
																	findViewById(R.id.st_set_account_1_2_pb).setVisibility(View.GONE);
																	findViewById(R.id.st_set_account_1_2_ok).setVisibility(View.VISIBLE);
																	((TextView) findViewById(R.id.st_set_account_1_2_title)).setTextColor(Color.rgb(153, 255, 102));
																	((TextView) findViewById(R.id.st_set_account_1_1_title)).setTextColor(Color.rgb(153, 255, 102));
																	findViewById(R.id.st_set_account_1_ok).setVisibility(View.VISIBLE);
																	findViewById(R.id.st_set_account_1_pb1).setVisibility(View.GONE);
																	findViewById(R.id.st_set_account_1_pb2).setVisibility(View.GONE);
																}
															});
															//
															ThothProcessor.create();
															//
															SETUP.sendEmptyMessage(50); // Start log capture
														}
														
														@Override
														public void progress(int resultCode, int progressValue, final int r)
														{}
													});
													break;
												}
												case 5 :
												{
													RequestListener.onReceive(Thoth.REQ.USR_EDIT, new ThothRequestCallback()
													{
														@Override
														public void handle(int resultCode)
														{
															if (resultCode == 2) // Device already exists
															{
																runOnUiThread(new Runnable()
																{
																	@Override
																	public void run()
																	{
																		pSPB.setProgress(100);
																		findViewById(R.id.st_set_account_1_2_pb).setVisibility(View.GONE);
																		findViewById(R.id.st_set_account_1_2_ok).setVisibility(View.VISIBLE);
																		((TextView) findViewById(R.id.st_set_account_1_2_title)).setTextColor(Color.rgb(153, 255, 102));
																		((TextView) findViewById(R.id.st_set_account_1_1_title)).setTextColor(Color.rgb(153, 255, 102));
																		findViewById(R.id.st_set_account_1_ok).setVisibility(View.VISIBLE);
																		findViewById(R.id.st_set_account_1_pb1).setVisibility(View.GONE);
																		findViewById(R.id.st_set_account_1_pb2).setVisibility(View.GONE);
																		//
																		pdDL = new ProgressDialog(Thoth.cx);
																		pdDL.setCancelable(false);
																		pdDL.setIndeterminate(true);
																		pdDL.setTitle("Checking for your device logs on server :)");
																		pdDL.show();
																		//
																		ThothLog.w("Checking for your device logs on server :)");
																	}
																});
																//
																RequestListener.onReceive(Thoth.REQ.LOG_RESTORE, new ThothRequestCallback()
																{
																	@Override
																	public void progress(int resultCode, int progressValue, int reference)
																	{}
																	
																	@Override
																	public void handle(final int resultCode)
																	{
																		runOnUiThread(new Runnable()
																		{
																			@Override
																			public void run()
																			{
																				if (null != pdDL)
																					pdDL.dismiss();
																				Toast.makeText(Thoth.cx, "Continuing with configuration ...", Toast.LENGTH_SHORT).show();
																				ThothLog.w("Continuing with configuration ...");
																			}
																		});
																		SETUP.sendEmptyMessage(50); // Start log
																												// capture
																	}
																});
																//
															}
															else
															// 1 - User edit OK
															{
																if (resultCode < 1)// failed to edit earlier
																{
																	// Message msg = new Message();
																	// msg.what = -1;
																	// msg.obj = new
																	// String("REQ.USR_EDIT failed during Setup.");
																	// SETUP.sendMessage(msg);
																	//
																	ThothLog.e(new Exception("REQ.USR_EDIT failed during Setup."));
																}
																runOnUiThread(new Runnable()
																{
																	@Override
																	public void run()
																	{
																		pSPB.setProgress(100);
																		findViewById(R.id.st_set_account_1_2_pb).setVisibility(View.GONE);
																		findViewById(R.id.st_set_account_1_2_ok).setVisibility(View.VISIBLE);
																		((TextView) findViewById(R.id.st_set_account_1_2_title)).setTextColor(Color.rgb(153, 255, 102));
																		((TextView) findViewById(R.id.st_set_account_1_1_title)).setTextColor(Color.rgb(153, 255, 102));
																		findViewById(R.id.st_set_account_1_ok).setVisibility(View.VISIBLE);
																		findViewById(R.id.st_set_account_1_pb1).setVisibility(View.GONE);
																		findViewById(R.id.st_set_account_1_pb2).setVisibility(View.GONE);
																		//
																		ThothProcessor.create();
																		//
																		SETUP.sendEmptyMessage(50); // Start log
																												// capture
																	}
																});
															}
														}
														
														@Override
														public void progress(int resultCode, int progressValue, final int r)
														{}
													});
													break;
												}
												case 6 :
												{
													promptGoogleMailSync();
													break;
												}
												case 7 :
												{
													promptGoogleCalendarSync();
													break;
												}
												case 50 :
												{
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
													runOnUiThread(new Runnable()
													{
														@Override
														public void run()
														{
															try
															{
																pSPB.setMax(sPbMax);
																pSPB.setProgress(sPbCurrent);
																if (msg.arg1 == 0)
																{
																	TextView ttv = ((TextView) findViewById(R.id.st_set_account_2_1_title));
																	ttv.setText("");
																	ttv.setText((String) msg.obj);
																}
																else if (msg.arg1 == 1)
																{
																	TextView ttv = ((TextView) findViewById(R.id.st_set_account_2_2_title));
																	ttv.setText("");
																	ttv.setText((String) msg.obj);
																}
																else if (msg.arg1 == 2)
																{
																	TextView ttv = ((TextView) findViewById(R.id.st_set_account_2_3_title));
																	ttv.setText("");
																	ttv.setText((String) msg.obj);
																}
																else if (msg.arg1 == 3)
																{
																	TextView ttv = ((TextView) findViewById(R.id.st_set_account_2_4_title));
																	ttv.setText("");
																	ttv.setText((String) msg.obj);
																}
															}
															catch (Exception e)
															{}
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
												case -999 :
												{
													synchronized (this)
													{
														ThothAuth.authTimeout = true;
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
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					((TextView) findViewById(R.id.st_set_account_1_title)).setTextColor(Color.rgb(153, 255, 102));
					findViewById(R.id.st_set_account_1_2).setVisibility(View.GONE);
					findViewById(R.id.st_set_account_1_1).setVisibility(View.GONE);
					findViewById(R.id.st_set_account_2).setVisibility(View.VISIBLE);
					findViewById(R.id.st_set_account_2_1).setVisibility(View.VISIBLE);
					findViewById(R.id.st_set_account_2_2).setVisibility(View.VISIBLE);
					findViewById(R.id.st_set_account_2_3).setVisibility(View.VISIBLE);
					findViewById(R.id.st_set_account_2_4).setVisibility(View.VISIBLE);
					pSPB = (ProgressBar) findViewById(R.id.st_set_account_2_pb2);
				}
			});
			waitForLogCapture(new ThothRequestCallback()
			{
				@Override
				public void handle(final int resultCode)
				{
					if (resultCode != 1)
						ThothLog.e(new Exception("Exception -> REQ.LOG_CREATE failed during Setup in captureLogs. Continuing"));
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							findViewById(R.id.st_set_account_2_2_pb).setVisibility(View.GONE);
							findViewById(R.id.st_set_account_2_2_ok).setVisibility(View.VISIBLE);
							((TextView) findViewById(R.id.st_set_account_2_title)).setTextColor(Color.rgb(153, 255, 102));
							findViewById(R.id.st_set_account_2_pb1).setVisibility(View.GONE);
							findViewById(R.id.st_set_account_2_pb2).setVisibility(View.GONE);
							findViewById(R.id.st_set_account_2_ok).setVisibility(View.VISIBLE);
							findViewById(R.id.st_set_account_2_4).setVisibility(View.GONE);
							findViewById(R.id.st_set_account_2_3).setVisibility(View.GONE);
							findViewById(R.id.st_set_account_2_2).setVisibility(View.GONE);
							findViewById(R.id.st_set_account_2_1).setVisibility(View.GONE);
							findViewById(R.id.st_set_account_3).setVisibility(View.VISIBLE);
							findViewById(R.id.st_set_account_3_1).setVisibility(View.VISIBLE);
							pSPB = (ProgressBar) findViewById(R.id.st_set_account_3_pb2);
							pSPB.setMax(0);
							pSPB.setProgress(0);
						}
					});
					RequestListener.onReceive(Thoth.REQ.LOG_CREATE, new ThothRequestCallback()
					{
						@Override
						public void handle(int rC)
						{
							if (rC < 1)
								ThothLog.e(new Exception("Exception during REQ.LOG_CREATE. Continuing"));
							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									findViewById(R.id.st_set_account_3_1_pb).setVisibility(View.GONE);
									findViewById(R.id.st_set_account_3_1_ok).setVisibility(View.VISIBLE);
									TextView tvs = (TextView) findViewById(R.id.st_set_account_3_1_title);
									tvs.setText("");
									tvs.setText("Log Upload (100/100)");
								}
							});
							collectionComplete();
						}
						
						@Override
						public void progress(final int rC, final int rV, final int r)
						{
							if (rC == -50)
							{
								runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										pSPB.setMax(pSPB.getMax() + rV);
									}
								});
							}
							else
							{
								runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										pSPB.setProgress(pSPB.getProgress() + rV);
										TextView tvs = (TextView) findViewById(R.id.st_set_account_3_1_title);
										tvs.setText("");
										tvs.setText("Log Process (" + pSPB.getProgress() + "/" + pSPB.getMax() + ")");
									}
								});
							}
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
						SETUP.sendEmptyMessageDelayed(-888, 300000);
						while (!captureTimeout)
						{
							try
							{
								runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										if (!SETUP_CALL_CAPTURE)
										{
											((TextView) findViewById(R.id.st_set_account_2_1_title)).setTextColor(Color.rgb(153, 255, 102));
											findViewById(R.id.st_set_account_2_1_pb).setVisibility(View.GONE);
											findViewById(R.id.st_set_account_2_1_ok).setVisibility(View.VISIBLE);
											findViewById(R.id.st_set_account_2_2_pb).setVisibility(View.VISIBLE);
										}
										if (!SETUP_MSGS_CAPTURE)
										{
											((TextView) findViewById(R.id.st_set_account_2_2_title)).setTextColor(Color.rgb(153, 255, 102));
											findViewById(R.id.st_set_account_2_2_pb).setVisibility(View.GONE);
											findViewById(R.id.st_set_account_2_2_ok).setVisibility(View.VISIBLE);
											findViewById(R.id.st_set_account_2_3_pb).setVisibility(View.VISIBLE);
										}
										if (!SETUP_MSGM_CAPTURE)
										{
											((TextView) findViewById(R.id.st_set_account_2_3_title)).setTextColor(Color.rgb(153, 255, 102));
											findViewById(R.id.st_set_account_2_3_pb).setVisibility(View.GONE);
											findViewById(R.id.st_set_account_2_3_ok).setVisibility(View.VISIBLE);
											findViewById(R.id.st_set_account_2_4_pb).setVisibility(View.VISIBLE);
										}
										if (!SETUP_DATA_CAPTURE)
										{
											((TextView) findViewById(R.id.st_set_account_2_4_title)).setTextColor(Color.rgb(153, 255, 102));
											findViewById(R.id.st_set_account_2_4_pb).setVisibility(View.GONE);
											findViewById(R.id.st_set_account_2_4_ok).setVisibility(View.VISIBLE);
										}
									}
								});
								if (!SETUP_CALL_CAPTURE && !SETUP_MSGS_CAPTURE && !SETUP_DATA_CAPTURE)
									captureTimeout = true;
								else
									Thread.sleep(500);
							}
							catch (InterruptedException e)
							{
								ThothLog.e(e);
							}
						}
						if (!SETUP_CALL_CAPTURE && !SETUP_MSGS_CAPTURE && !SETUP_MSGM_CAPTURE && !SETUP_DATA_CAPTURE)
						{
							captureTimeout = false;
							SETUP.removeMessages(-888);
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
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				findViewById(R.id.st_set_account_3_1).setVisibility(View.GONE);
				pSPB.setMax(0);
				pSPB.setProgress(0);
				findViewById(R.id.st_set_account_3_2).setVisibility(View.GONE);
				findViewById(R.id.st_set_account_3_1).setVisibility(View.GONE);
				findViewById(R.id.st_set_account_3_pb1).setVisibility(View.GONE);
				findViewById(R.id.st_set_account_3_pb2).setVisibility(View.GONE);
				((TextView) findViewById(R.id.st_set_account_3_title)).setTextColor(Color.rgb(153, 255, 102));
				try
				{
					Settings.APP_VERSION = (getPackageManager().getPackageInfo("com.gnuc.thoth", 0)).versionCode;
				}
				catch (NameNotFoundException e)
				{}
				Settings.APP_SETUP = Settings.APP_UPGRADE = false; // Setup is complete.
				Settings.write();
				promptGoogleMailSync();
				Thoth.TRACKER.trackPageView("/SETUP_COMPLETE");
			}
		});
	}
	
	// Ask user if they want to enable GMail sync
	void promptGoogleMailSync()
	{
		bt.setVisibility(View.GONE);
		pb.setVisibility(View.GONE);
		sF.setDisplayedChild(4);
		gmbt = (Button) findViewById(R.id.gmail_setup_bt);
		gmbtno = (Button) findViewById(R.id.gmail_setup_btno);
		gmbt.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				gmbt.setVisibility(View.GONE);
				gmbtno.setVisibility(View.GONE);
				Thoth.createThothAsContact(new ThothCallback()
				{
					@Override
					public void handle(boolean result)
					{
						if (result)
						{
							Settings.APP_GoMAIL = true;
							Settings.write();
							gmbt.setVisibility(View.VISIBLE);
							gmbt.setText("Connected");
							Thoth.TRACKER.trackPageView("/SETUP_GoMail");
							if (!Settings.APP_GoCAL)
								promptGoogleCalendarSync();
							else
								Main.getInstance().mainHandler.sendEmptyMessage(2);
						}
						else
							promptGoogleCalendarSync();
					}
				});
			}
		});
		gmbtno.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (!Settings.APP_GoCAL)
					promptGoogleCalendarSync();
				else
					Main.getInstance().mainHandler.sendEmptyMessage(2);
			}
		});
	}
	
	void promptGoogleCalendarSync()
	{
		bt.setVisibility(View.GONE);
		pb.setVisibility(View.GONE);
		sF.setDisplayedChild(5);
		gcbt = (Button) findViewById(R.id.calendar_setup_bt);
		gcbtno = (Button) findViewById(R.id.calendar_setup_btno);
		gcbt.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				gcbt.setVisibility(View.GONE);
				gcbtno.setVisibility(View.GONE);
				((TextView) findViewById(R.id.st_calendar_msg)).setText(R.string.st_calendar_msg_1_2);
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
				catch (Exception e)
				{
					ThothLog.e(e);
					Main.getInstance().mainHandler.sendEmptyMessage(2);
				}
				finally
				{
					if (c != null)
						c.close();
				}
				if (!calendarMap.isEmpty())
				{
					final List<String> calendarNames = new ArrayList<String>();
					for (String calendar : calendarMap.keySet())
						calendarNames.add(calendar);
					ListView calendarList = (ListView) findViewById(R.id.st_calendar_list);
					calendarList.setVisibility(View.VISIBLE);
					calendarList.setAdapter(new ArrayAdapter<String>(Thoth.cx, android.R.layout.simple_expandable_list_item_1, calendarNames));
					calendarList.setDivider(getResources().getDrawable(R.drawable.row_gradient1));
					calendarList.setDividerHeight(1);
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
								Main.getInstance().mainHandler.sendEmptyMessage(2);
								Thoth.TRACKER.trackPageView("/SETUP_GoCal");
							}
						}
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							Toast.makeText(Thoth.cx, "No calendar found on device. Please create a new calendar on Google calendar and configure later. :)", Toast.LENGTH_LONG).show();
						}
					});
					Settings.write();
					Main.getInstance().mainHandler.sendEmptyMessage(2);
				}
			}
		});
		gcbtno.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Main.getInstance().mainHandler.sendEmptyMessage(2);
			}
		});
	}
}
