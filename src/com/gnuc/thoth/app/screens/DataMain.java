package com.gnuc.thoth.app.screens;

import java.text.DecimalFormat;
import java.util.Date;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gnuc.thoth.R;
import com.gnuc.thoth.app.Main;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.ThothLog;
import com.gnuc.thoth.framework.callbacks.ThothCallback;
import com.gnuc.thoth.framework.db.LoggerDB;
import com.gnuc.thoth.service.ThothService;

public class DataMain extends ExpandableListActivity
{
	private ThothExpandableListAdapter	elAdapter;
	private ExpandableListView				elView;
	private Context							elContext;
	private static DataMain					instance	= null;
	
	public static DataMain getInstance()
	{
		if (instance == null)
			instance = new DataMain();
		return instance;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try
		{
			setContentView(R.layout.data_main);
			elContext = instance = this;
			elView = getExpandableListView();
			elAdapter = new ThothExpandableListAdapter();
			elView.setOnGroupClickListener(new OnGroupClickListener()
			{
				@Override
				public boolean onGroupClick(ExpandableListView elv, View view, int pos, long row)
				{
					return false;
				}
			});
			elView.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
			{
				@Override
				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
				{
					// Toast.makeText(elContext, "" + v.getTag(Thoth.APP), Toast.LENGTH_LONG).show();
					return true;
				}
			});
			elView.addHeaderView(View.inflate(this, R.layout.data_layout, null), null, false);
			elView.setAdapter(elAdapter);
			elView.setDivider(getResources().getDrawable(R.drawable.row_gradient1));
			elView.setDividerHeight(1);
			elView.setChildDivider(getResources().getDrawable(R.drawable.row_gradient2));
			elView.setFooterDividersEnabled(true);
		}
		catch (Exception e)
		{
			ThothLog.e(e);
			Main.getInstance().mainHandler.sendEmptyMessage(-1);
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Thoth.cx = this;
		updateStatistics();
		Thoth.TRACKER.trackPageView("/DATA_MAIN");
	}
	
	@Override
	public void onStop()
	{
		super.onPause();
		Intent i = new Intent(Thoth.cx, ThothService.class);
		i.setAction(String.valueOf(Thoth.REQ.DATA_CAPTURE));
		Thoth.cx.startService(i);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	public class ThothExpandableListAdapter extends BaseExpandableListAdapter
	{
		private Integer[]		explist_parents;
		private String[][][]	explist_children;
		
		public ThothExpandableListAdapter()
		{
			explist_parents = new Integer[]{R.layout.explist_d_app_out, R.layout.explist_d_app_in};
			explist_children = new String[][][]{{{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}, {{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}};
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition)
		{
			return explist_children[groupPosition][childPosition];
		}
		
		@Override
		public long getChildId(int groupPosition, int childPosition)
		{
			return childPosition;
		}
		
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
		{
			RelativeLayout child = (RelativeLayout) View.inflate(elContext, R.layout.explist_d_childrow, null);
			String cAN = explist_children[groupPosition][childPosition][0];
			String cAQ = explist_children[groupPosition][childPosition][1];
			String cAD = "";
			cAN = (cAN.equals("")) ? " - " : cAN;
			cAQ = (cAQ.equals("")) ? "( - )" : kbSizer(Double.parseDouble(cAQ));
			cAD = (cAQ.equalsIgnoreCase("( - )")) ? " - " : "since " + (DateFormat.format("dd/MM/yy", new Date(Thoth.Settings.APP_FIRST_RUN_DATE)));
			TextView tv = ((TextView) child.findViewById(R.id.t_d_child_name_label_id));
			tv.setText(cAN);
			tv.setVisibility(View.VISIBLE);
			tv = (TextView) child.findViewById(R.id.t_d_child_quantity_label_id);
			tv.setText(cAQ);
			tv.setVisibility(View.VISIBLE);
			tv = (TextView) child.findViewById(R.id.t_d_child_duration_label_id);
			tv.setText(cAD);
			tv.setVisibility(View.INVISIBLE);
			child.findViewById(R.id.t_d_child_name_label_pb).setVisibility(View.INVISIBLE);
			child.findViewById(R.id.t_d_child_quantity_label_pb).setVisibility(View.INVISIBLE);
			child.findViewById(R.id.t_d_child_duration_label_pb).setVisibility(View.INVISIBLE);
			child.setTag(Thoth.APP, cAN);
			return child;
		}
		
		@Override
		public int getChildrenCount(int groupPosition)
		{
			return explist_children[groupPosition].length;
		}
		
		@Override
		public Object getGroup(int groupPosition)
		{
			return explist_parents[groupPosition];
		}
		
		@Override
		public int getGroupCount()
		{
			return explist_parents.length;
		}
		
		@Override
		public long getGroupId(int groupPosition)
		{
			return groupPosition;
		}
		
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
		{
			return (RelativeLayout) View.inflate(elContext, (Integer) getGroup(groupPosition), null);
		}
		
		@Override
		public boolean hasStableIds()
		{
			return true;
		}
		
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition)
		{
			return true;
		}
	}
	
	public void updateStatistics()
	{
		updateDataSet(elAdapter, Thoth.Settings.APP_LOG_FILTER, new ThothCallback()
		{
			@Override
			public void handle(boolean result)
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						elAdapter.notifyDataSetChanged();
					}
				});
			}
		});
	}
	
	//
	String[]	TopDL			= {"t_LogApps_TODAY_TopDL", "t_LogApps_THISWEEK_TopDL", "t_LogApps_THISMONTH_TopDL", "t_LogApps_LASTWEEK_TopDL", "t_LogApps_LASTMONTH_TopDL"};
	String[]	TopUL			= {"t_LogApps_TODAY_TopUL", "t_LogApps_THISWEEK_TopUL", "t_LogApps_THISMONTH_TopUL", "t_LogApps_LASTWEEK_TopUL", "t_LogApps_LASTMONTH_TopUL"};
	String[]	DataMOBILE	= {"t_LogDataMobile_TODAY", "t_LogDataMobile_THISWEEK", "t_LogDataMobile_THISMONTH", "t_LogDataMobile_LASTWEEK", "t_LogDataMobile_LASTMONTH"};
	String[]	DataWIFI		= {"t_LogDataWifi_TODAY", "t_LogDataWifi_THISWEEK", "t_LogDataWifi_THISMONTH", "t_LogDataWifi_LASTWEEK", "t_LogDataWifi_LASTMONTH"};
	//
	private static int	TOTAL_MOBILE_UL	= 0, TOTAL_MOBILE_DL = 0, TOTAL_WIFI_UL = 0, TOTAL_WIFI_DL = 0;
	
	void updateDataSet(final ThothExpandableListAdapter tela, final int filter, final ThothCallback callback)
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
						boolean result = false;
						Cursor cR = null;
						LoggerDB ldb = new LoggerDB(elContext);
						try
						{
							SQLiteDatabase db = ldb.getDb();
							/**********************************************************************************************************************
							 * TOTAL
							 * ********************************************************************************************************************/
							cR = db.rawQuery("SELECT SUM(" + LoggerDB.col_LDW_Uploaded + ") FROM " + DataWIFI[filter], null);
							TOTAL_WIFI_UL = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_WIFI_UL;
							cR.close();
							//
							cR = db.rawQuery("SELECT SUM(" + LoggerDB.col_LDW_Downloaded + ") FROM " + DataWIFI[filter], null);
							TOTAL_WIFI_DL = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_WIFI_DL;
							cR.close();
							//
							cR = db.rawQuery("SELECT SUM(" + LoggerDB.col_LDM_Uploaded + ") FROM " + DataMOBILE[filter], null);
							TOTAL_MOBILE_UL = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_MOBILE_UL;
							cR.close();
							//
							cR = db.rawQuery("SELECT SUM(" + LoggerDB.col_LDM_Downloaded + ") FROM " + DataMOBILE[filter], null);
							TOTAL_MOBILE_DL = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_MOBILE_DL;
							cR.close();
							//
							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									((TextView) findViewById(R.id.t_d_total_out_data_id)).setText(kbSizer(TOTAL_WIFI_UL));
									((TextView) findViewById(R.id.t_d_total_in_data_id)).setText(kbSizer(TOTAL_WIFI_DL));
									((TextView) findViewById(R.id.t_d_mobile_out_data_id)).setText(kbSizer(TOTAL_MOBILE_UL));
									((TextView) findViewById(R.id.t_d_mobile_in_data_id)).setText(kbSizer(TOTAL_MOBILE_DL));
								}
							});
							/**********************************************************************************************************************
							 * TOP
							 * ********************************************************************************************************************/
							int x;
							tela.explist_children = new String[][][]{{{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}, {{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}};
							//
							cR = db.rawQuery("SELECT * FROM " + TopUL[filter] + " LIMIT 5", null);
							if (cR != null && cR.moveToFirst())
							{
								x = 0;
								do
									tela.explist_children[0][x++] = new String[]{lookupApplication(cR.getString(1)), cR.getString(0), ""};
								while (cR.moveToNext() && x < 5);
							}
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TopDL[filter] + " LIMIT 5", null);
							if (cR != null && cR.moveToFirst())
							{
								x = 0;
								do
									tela.explist_children[1][x++] = new String[]{lookupApplication(cR.getString(1)), cR.getString(0), ""};
								while (cR.moveToNext() && x < 5);
							}
							cR.close();
							//
							result = true;
						}
						catch (Exception e)
						{
							ThothLog.e(e);
						}
						finally
						{
							if (null != ldb)
								ldb.getDb().close();
							callback.handle(result);
						}
					}
				}.run();
			}
		}.start();
	}
	
	public String kbSizer(double size)
	{
		final double BASE = 1024, KB = BASE, MB = KB * BASE, GB = MB * BASE;
		final DecimalFormat df = new DecimalFormat("#.##");
		return (size >= GB) ? df.format(size / GB) + " GB" : (size >= MB) ? df.format(size / MB) + " MB" : (size >= KB) ? df.format(size / KB) + " KB" : (int) size + " bytes";
	}
	
	public String lookupApplication(String packageName)
	{
		final PackageManager pm = Thoth.cx.getPackageManager();
		ApplicationInfo ai;
		try
		{
			ai = pm.getApplicationInfo(packageName, 0);
		}
		catch (final NameNotFoundException e)
		{
			ai = null;
		}
		return (String) (ai != null ? pm.getApplicationLabel(ai) : "(" + packageName + ")");
	}
}
