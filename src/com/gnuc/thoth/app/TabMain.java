package com.gnuc.thoth.app;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.gnuc.thoth.R;
import com.gnuc.thoth.app.screens.CallMain;
import com.gnuc.thoth.app.screens.DataMain;
import com.gnuc.thoth.app.screens.MessagingMain;
import com.gnuc.thoth.app.screens.SyncMain;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.Thoth.Settings;
import com.gnuc.thoth.framework.ThothLog;
import com.gnuc.thoth.framework.ThothProcessor;
import com.gnuc.thoth.framework.ad.Advertisements;
import com.gnuc.thoth.framework.callbacks.ThothRequestCallback;
import com.gnuc.thoth.service.listeners.RequestListener;

public class TabMain extends TabActivity
{
	private static TabMain	instance	= null;
	public Spinner				spinner	= null;
	
	public static TabMain g()
	{
		if (instance == null)
			instance = new TabMain();
		return instance;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thoth_layout);
		//
		Thoth.cx = this;
		instance = this;
		Settings.read();
		//
		TABMAIN.sendEmptyMessage(0);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		Settings.write();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		if ((Minutes.minutesBetween(new DateTime(Settings.APP_LAST_SYNC_THOTH), new DateTime())).getMinutes() > 59 && !Settings.APP_UPGRADE)
			TABMAIN.sendEmptyMessageDelayed(20, 10000);
		if (Settings.APP_SHOW_UPDATE)
			TABMAIN.sendEmptyMessageDelayed(50, 5000);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.thoth_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.thoth_share :
			{
				shareThoth();
				return true;
			}
			case R.id.thoth_rate :
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
				return true;
			}
			case R.id.thoth_about :
			{
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.about_page_layout, null);
				final TextView apB = (TextView) layout.findViewById(R.id.ab_p_body);
				apB.setMovementMethod(LinkMovementMethod.getInstance());
				String text = "<p>Thoth is a logging application that logs all phone calls, messages and data activity on your Android device.</p><p>For further information, please visit the <a href=\"https://thoth-server.appspot.com\">Homepage.</a></p><p>Developed by <a href=\"http://gnuc.in\">Gnu Consultancy Pt. Ltd.</a></p><p>This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.</p><br />See the <a href=\"http://www.gnu.org/licenses/\">GNU General Public License</a> for more details.";
				apB.setText(Html.fromHtml(text));
				adb.setCancelable(true);
				adb.setView(layout);
				adb.setInverseBackgroundForced(true);
				final AlertDialog ad = adb.create();
				WindowManager.LayoutParams lp = ad.getWindow().getAttributes();
				lp.dimAmount = 0.1f;
				ad.getWindow().setAttributes(lp);
				ad.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
				ad.show();
				Thoth.TRACKER.trackPageView("/ABOUT_PAGE");
				return true;
			}
			case R.id.thoth_settings :
			{
				startActivity(new Intent(this, PreferencesMain.class));
				return true;
			}
			default :
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void shareThoth()
	{
		try
		{
			LayoutInflater inflater = (LayoutInflater) Thoth.cx.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.webloader_layout, (ViewGroup) findViewById(R.id.webloader_layout));
			final ProgressBar pbar = (ProgressBar) layout.findViewById(R.id.webloader_wait);
			final WebView webview = (WebView) layout.findViewById(R.id.webloader_web);
			//
			AlertDialog.Builder builder = new AlertDialog.Builder(Thoth.cx);
			builder.setView(layout);
			builder.setInverseBackgroundForced(true);
			final AlertDialog aldg = builder.create();
			//
			webview.loadUrl("https://thoth-server.appspot.com/share.html");
			webview.getSettings().setJavaScriptEnabled(true);
			webview.setWebViewClient(new WebViewClient()
			{
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url)
				{
					if (url.contains("thoth-server.appspot.com") == true)
						return false;
					else
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
				}
				
				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
				{
					aldg.cancel();
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://thoth-server.appspot.com/share.html")));
				}
				
				public void onPageFinished(WebView view, String url)
				{
					pbar.setVisibility(View.GONE);
					webview.setVisibility(View.VISIBLE);
				}
			});
			aldg.show();
		}
		catch (Exception e)
		{}
	}
	
	public Handler	TABMAIN	= new Handler()
									{
										public void handleMessage(final Message msg)
										{
											runOnUiThread(new Runnable()
											{
												@Override
												public void run()
												{
													try
													{
														switch (msg.what)
														{
															case 0 :
															{
																if (Settings.APP_UPGRADE)
																{
																	// Show progress bar where all logs are updated
																	// only once.
																	AlertDialog ad = null;
																	AlertDialog.Builder adb = new AlertDialog.Builder(Thoth.cx);
																	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
																	View layout = inflater.inflate(R.layout.upgrade_layout, null);
																	//
																	adb.setCancelable(false);
																	adb.setView(layout);
																	// adb.setInverseBackgroundForced(true);
																	ad = adb.create();
																	//
																	WindowManager.LayoutParams lp = ad.getWindow().getAttributes();
																	lp.dimAmount = 0.1f;
																	ad.getWindow().setAttributes(lp);
																	ad.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
																	ad.setTitle("Upgrading application files ...");
																	ad.show();
																	//
																	Thoth.sendToService(Thoth.REQ.UPGRADE_CAPTURE, null, null);
																	//
																	ThothProcessor.create();
																	//
																	UpgradeMain.g(new UpgradeMain(g(), ad)).UPGRADE.sendEmptyMessage(1);
																}
																else
																	TABMAIN.sendEmptyMessage(1);
																break;
															}
															case 1 :
															{
																final Resources res = getResources();
																final TabHost th = getTabHost();
																th.clearAllTabs();
																//
																th.addTab(th.newTabSpec("CallMain").setIndicator("Call Stats").setContent(new Intent().setClass(g(), CallMain.class)));
																th.addTab(th.newTabSpec("MessagingMain").setIndicator("Msg Stats").setContent(new Intent().setClass(g(), MessagingMain.class)));
																if (Settings.APP_ENABLE_DATA_CAPTURE)
																	th.addTab(th.newTabSpec("DataMain").setIndicator("Data Stats").setContent(new Intent().setClass(g(), DataMain.class)));
																th.addTab(th.newTabSpec("SyncMain").setIndicator("Sync").setContent(new Intent().setClass(g(), SyncMain.class)));
																//
																int iCnt = th.getTabWidget().getChildCount();
																for (int i = 0; i < iCnt; i++)
																	th.getTabWidget().getChildAt(i).getLayoutParams().height /= 1.5;
																//
																spinner = (Spinner) findViewById(R.id.log_filter_spinner);
																ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(g(), R.array.log_filter_choice_array, android.R.layout.simple_spinner_item);
																adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
																spinner.setAdapter(adapter);
																th.setOnTabChangedListener(new TabHost.OnTabChangeListener()
																{
																	@Override
																	public void onTabChanged(String tabId)
																	{
																		Advertisements.showAd(TabMain.this, findViewById(R.id.thoth_layout_id));
																		if (th.getCurrentTabTag().equalsIgnoreCase("syncmain"))
																			spinner.setVisibility(View.GONE);
																		else
																			spinner.setVisibility(View.VISIBLE);
																	}
																});
																//
																Advertisements.showAd(TabMain.this, findViewById(R.id.thoth_layout_id));
																// Check Thoth version.
																if (Thoth.isNetworkOK())
																{
																	RequestListener.onReceive(Thoth.REQ.HELLO, new ThothRequestCallback()
																	{
																		@Override
																		public void progress(int resultCode, int progressValue, final int r)
																		{}
																		
																		@Override
																		public void handle(int resultCode)
																		{
																			if (resultCode > 0)
																			{
																				AlertDialog.Builder adb = new AlertDialog.Builder(Thoth.cx);
																				adb.setIcon(R.drawable.icon);
																				adb.setTitle("Update Thoth");
																				adb.setMessage("A new update for Thoth is available on the Android Market.\n\nDo you want to update now?");
																				adb.setCancelable(false);
																				adb.setPositiveButton("Yes", new DialogInterface.OnClickListener()
																				{
																					public void onClick(DialogInterface dialog, int id)
																					{
																						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
																					}
																				});
																				adb.setNegativeButton("Later", new DialogInterface.OnClickListener()
																				{
																					public void onClick(DialogInterface dialog, int id)
																					{
																						dialog.cancel();
																					}
																				});
																			}
																		}
																	});
																}
																//
																TABMAIN.sendEmptyMessage(2);
																break;
															}
															case 2 :
															{
																final TabHost th = getTabHost();
																//
																spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
																{
																	@Override
																	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
																	{
																		Settings.APP_LOG_FILTER = pos;
																		Settings.write();
																		//
																		if (th.getCurrentTabTag().equalsIgnoreCase("callmain"))
																			CallMain.getInstance().updateStatistics();
																		else if (th.getCurrentTabTag().equalsIgnoreCase("messagingmain"))
																			MessagingMain.getInstance().updateStatistics();
																		else if (th.getCurrentTabTag().equalsIgnoreCase("datamain"))
																			DataMain.getInstance().updateStatistics();
																	}
																	
																	@Override
																	public void onNothingSelected(AdapterView<?> parent)
																	{}
																});
																try
																{
																	spinner.setSelection(Settings.APP_LOG_FILTER, true);
																}
																catch (IndexOutOfBoundsException e)
																{
																	Settings.APP_LOG_FILTER = 0;
																	Settings.write();
																	spinner.setSelection(Settings.APP_LOG_FILTER, true);
																}
																break;
															}
															case 3 :
															{
																((ProgressBar) findViewById(R.id.app_update_pb)).setVisibility(View.VISIBLE);
																((ImageView) findViewById(R.id.app_icon_id)).setVisibility(View.GONE);
																break;
															}
															case 4 :
															{
																((ProgressBar) findViewById(R.id.app_update_pb)).setVisibility(View.GONE);
																((ImageView) findViewById(R.id.app_icon_id)).setVisibility(View.VISIBLE);
																break;
															}
															case 10 :
															{
																ImageView gnucAd = (ImageView) findViewById(R.id.ad_gnuc);
																gnucAd.setVisibility(View.GONE);
																break;
															}
															case 20 :
															{
																TABMAIN.sendEmptyMessage(3);
																Thoth.sendToService(Thoth.REQ.LOG_UPDATE, null, null);
																break;
															}
															case 50 :
															{
																runOnUiThread(new Runnable()
																{
																	@Override
																	public void run()
																	{
																		AlertDialog.Builder adb = new AlertDialog.Builder(TabMain.this);
																		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
																		View layout = inflater.inflate(R.layout.about_page_layout, null);
																		final TextView apB = (TextView) layout.findViewById(R.id.ab_p_body);
																		apB.setMovementMethod(LinkMovementMethod.getInstance());
																		String text = "<h2>Changes in this version</h2><p>Now recover your old messages using Thoth (Go to Settings > Recover+).</p><p>More statistics such as calls missed, rejected and unanswered.</p><p>Now configure calls and messages to be logged to different Google Calendars. (Go to Settings > Connect+)</p><p>Your logs are now mailed to your inbox in a more organised manner.</p><p>For further information, please visit the <a href=\"https://thoth-server.appspot.com\">Homepage.</a></p><p>Mail your suggestions and queries to the <a href=\"mailto:uparker@gnuc.in\">developers</a></p>";
																		apB.setText(Html.fromHtml(text));
																		adb.setCancelable(true);
																		adb.setView(layout);
																		adb.setInverseBackgroundForced(true);
																		final AlertDialog ad = adb.create();
																		WindowManager.LayoutParams lp = ad.getWindow().getAttributes();
																		lp.dimAmount = 0.1f;
																		ad.getWindow().setAttributes(lp);
																		ad.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
																		ad.show();
																		Thoth.TRACKER.trackPageView("/FIRSTRUN_PAGE");
																		Settings.APP_SHOW_UPDATE = false;
																		Settings.write();
																	}
																});
																break;
															}
															case -10 :
															{
																ImageView gnucAd = (ImageView) findViewById(R.id.ad_gnuc);
																gnucAd.setVisibility(View.VISIBLE);
																gnucAd.setOnClickListener(new View.OnClickListener()
																{
																	@Override
																	public void onClick(View v)
																	{
																		Intent i = new Intent(Intent.ACTION_VIEW);
																		i.setData(Uri.parse("http://gnuconsultancy.mobify.me"));
																		Thoth.TRACKER.trackPageView("/GNUC_AD_CLICK");
																		startActivity(i);
																	}
																});
																break;
															}
														}
													}
													catch (Exception e)
													{
														ThothLog.e(e);
													}
												}
											});
										}
									};
}
