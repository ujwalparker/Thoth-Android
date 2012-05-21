package com.gnuc.thoth.app.screens;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gnuc.thoth.R;
import com.gnuc.thoth.app.Main;
import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.ThothLog;
import com.gnuc.thoth.framework.callbacks.ThothCallback;
import com.gnuc.thoth.framework.db.LoggerDB;

public class CallMain extends ExpandableListActivity
{
	private ThothExpandableListAdapter	elAdapter	= null;
	private ExpandableListView				elView		= null;
	private Context							elContext;
	private static CallMain					instance		= null;
	
	public static CallMain getInstance()
	{
		if (instance == null)
			instance = new CallMain();
		return instance;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try
		{
			setContentView(R.layout.call_main);
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
					// Toast.makeText(elContext,
					// String.valueOf(v.getTag(Thoth.APP)),Toast.LENGTH_LONG).show();
					return true;
				}
			});
			elView.addHeaderView(View.inflate(this, R.layout.call_layout, null), null, false);
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
		//
		updateStatistics();
		//
		Thoth.TRACKER.trackPageView("/CALL_MAIN");
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
			explist_parents = new Integer[]{R.layout.explist_c_top_out, R.layout.explist_c_top_in, R.layout.explist_c_top_msd, R.layout.explist_c_top_noa, R.layout.explist_c_top_rej};
			explist_children = new String[][][]{{{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}, {{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}, {{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}, {{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}, {{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}};
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
			RelativeLayout child = (RelativeLayout) View.inflate(elContext, R.layout.explist_c_childrow, null);
			String cN = explist_children[groupPosition][childPosition][0];
			String cD = explist_children[groupPosition][childPosition][1];
			String cT = explist_children[groupPosition][childPosition][2];
			cN = (cN.equals("")) ? " - " : cN;
			if (!cD.equals(""))
			{
				Float seconds = Float.parseFloat(cD);
				cD = (seconds > 59) ? (Math.round(seconds / 60) + " min.") : (seconds + " sec.");
			}
			else
				cD = "( - )";
			cT = (cT.equals("")) ? "" : "[ " + cT + " nos. ]";
			TextView tv = ((TextView) child.findViewById(R.id.t_c_child_name_label_id));
			tv.setText(cN);
			tv.setVisibility(View.VISIBLE);
			tv = (TextView) child.findViewById(R.id.t_c_child_duration_label_id);
			tv.setText(cD);
			tv.setVisibility(View.VISIBLE);
			tv = (TextView) child.findViewById(R.id.t_c_child_quantity_label_id);
			tv.setText(cT);
			tv.setVisibility(View.VISIBLE);
			child.setTag(Thoth.APP, cN);
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
	
	String[]	IN			= {"t_LogCall_TODAY_IN", "t_LogCall_THISWEEK_IN", "t_LogCall_THISMONTH_IN", "t_LogCall_LASTWEEK_IN", "t_LogCall_LASTMONTH_IN"};
	String[]	MIS		= {"t_LogCall_TODAY_MIS", "t_LogCall_THISWEEK_MIS", "t_LogCall_THISMONTH_MIS", "t_LogCall_LASTWEEK_MIS", "t_LogCall_LASTMONTH_MIS"};
	String[]	NOA		= {"t_LogCall_TODAY_NOA", "t_LogCall_THISWEEK_NOA", "t_LogCall_THISMONTH_NOA", "t_LogCall_LASTWEEK_NOA", "t_LogCall_LASTMONTH_NOA"};
	String[]	OUT		= {"t_LogCall_TODAY_OUT", "t_LogCall_THISWEEK_OUT", "t_LogCall_THISMONTH_OUT", "t_LogCall_LASTWEEK_OUT", "t_LogCall_LASTMONTH_OUT"};
	String[]	REJ		= {"t_LogCall_TODAY_REJ", "t_LogCall_THISWEEK_REJ", "t_LogCall_THISMONTH_REJ", "t_LogCall_LASTWEEK_REJ", "t_LogCall_LASTMONTH_REJ"};
	String[]	TotalIN	= {"t_LogCall_TODAY_TotalIN", "t_LogCall_THISWEEK_TotalIN", "t_LogCall_THISMONTH_TotalIN", "t_LogCall_LASTWEEK_TotalIN", "t_LogCall_LASTMONTH_TotalIN"};
	String[]	TotalMIS	= {"t_LogCall_TODAY_TotalMIS", "t_LogCall_THISWEEK_TotalMIS", "t_LogCall_THISMONTH_TotalMIS", "t_LogCall_LASTWEEK_TotalMIS", "t_LogCall_LASTMONTH_TotalMIS"};
	String[]	TotalNOA	= {"t_LogCall_TODAY_TotalNOA", "t_LogCall_THISWEEK_TotalNOA", "t_LogCall_THISMONTH_TotalNOA", "t_LogCall_LASTWEEK_TotalNOA", "t_LogCall_LASTMONTH_TotalNOA"};
	String[]	TotalOUT	= {"t_LogCall_TODAY_TotalOUT", "t_LogCall_THISWEEK_TotalOUT", "t_LogCall_THISMONTH_TotalOUT", "t_LogCall_LASTWEEK_TotalOUT", "t_LogCall_LASTMONTH_TotalOUT"};
	String[]	TotalREJ	= {"t_LogCall_TODAY_TotalREJ", "t_LogCall_THISWEEK_TotalREJ", "t_LogCall_THISMONTH_TotalREJ", "t_LogCall_LASTWEEK_TotalREJ", "t_LogCall_LASTMONTH_TotalREJ"};
	String[]	TopIN		= {"t_LogCall_TODAY_TopIN", "t_LogCall_THISWEEK_TopIN", "t_LogCall_THISMONTH_TopIN", "t_LogCall_LASTWEEK_TopIN", "t_LogCall_LASTMONTH_TopIN"};
	String[]	TopMIS	= {"t_LogCall_TODAY_TopMIS", "t_LogCall_THISWEEK_TopMIS", "t_LogCall_THISMONTH_TopMIS", "t_LogCall_LASTWEEK_TopMIS", "t_LogCall_LASTMONTH_TopMIS"};
	String[]	TopNOA	= {"t_LogCall_TODAY_TopNOA", "t_LogCall_THISWEEK_TopNOA", "t_LogCall_THISMONTH_TopNOA", "t_LogCall_LASTWEEK_TopNOA", "t_LogCall_LASTMONTH_TopNOA"};
	String[]	TopOUT	= {"t_LogCall_TODAY_TopOUT", "t_LogCall_THISWEEK_TopOUT", "t_LogCall_THISMONTH_TopOUT", "t_LogCall_LASTWEEK_TopOUT", "t_LogCall_LASTMONTH_TopOUT"};
	String[]	TopREJ	= {"t_LogCall_TODAY_TopREJ", "t_LogCall_THISWEEK_TopREJ", "t_LogCall_THISMONTH_TopREJ", "t_LogCall_LASTWEEK_TopREJ", "t_LogCall_LASTMONTH_TopREJ"};
	//
	private static int	DURATION_INBOUND	= 0, DURATION_OUTBOUND = 0, DURATION_MISSED = 0, DURATION_NOANSWER = 0, DURATION_REJECTED = 0;
	private static int	TOTAL_INBOUND		= 0, TOTAL_OUTBOUND = 0, TOTAL_MISSED = 0, TOTAL_NOANSWER = 0, TOTAL_REJECTED = 0;
	
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
							 * DURATION
							 * ********************************************************************************************************************/
							cR = db.rawQuery("SELECT SUM(" + LoggerDB.col_LC_Duration + ") FROM " + IN[filter], null);
							DURATION_INBOUND = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : DURATION_INBOUND;
							cR.close();
							//
							cR = db.rawQuery("SELECT SUM(" + LoggerDB.col_LC_Duration + ") FROM " + OUT[filter], null);
							DURATION_OUTBOUND = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : DURATION_OUTBOUND;
							cR.close();
							//
							cR = db.rawQuery("SELECT SUM(" + LoggerDB.col_LC_Duration + ") FROM " + MIS[filter], null);
							DURATION_MISSED = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : DURATION_MISSED;
							cR.close();
							//
							cR = db.rawQuery("SELECT SUM(" + LoggerDB.col_LC_Duration + ") FROM " + NOA[filter], null);
							DURATION_NOANSWER = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : DURATION_NOANSWER;
							cR.close();
							//
							cR = db.rawQuery("SELECT SUM(" + LoggerDB.col_LC_Duration + ") FROM " + REJ[filter], null);
							DURATION_REJECTED = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : DURATION_REJECTED;
							cR.close();
							/**********************************************************************************************************************
							 * TOTAL
							 * ********************************************************************************************************************/
							cR = db.rawQuery("SELECT * FROM " + TotalIN[filter], null);
							TOTAL_INBOUND = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_INBOUND;
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TotalOUT[filter], null);
							TOTAL_OUTBOUND = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_OUTBOUND;
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TotalMIS[filter], null);
							TOTAL_MISSED = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_MISSED;
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TotalNOA[filter], null);
							TOTAL_NOANSWER = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_NOANSWER;
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TotalREJ[filter], null);
							TOTAL_REJECTED = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_REJECTED;
							cR.close();
							/********************************************************************************************************************** 
							 * ********************************************************************************************************************/
							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									((TextView) findViewById(R.id.t_c_duration_out_data_id)).setText(String.valueOf(DURATION_OUTBOUND / 60));
									((TextView) findViewById(R.id.t_c_duration_in_data_id)).setText(String.valueOf(DURATION_INBOUND / 60));
									((TextView) findViewById(R.id.t_c_total_out_data_id)).setText(String.valueOf(TOTAL_OUTBOUND));
									((TextView) findViewById(R.id.t_c_total_in_data_id)).setText(String.valueOf(TOTAL_INBOUND));
									((TextView) findViewById(R.id.t_c_total_missed_data_id)).setText(String.valueOf(TOTAL_MISSED));
									((TextView) findViewById(R.id.t_c_total_rejected_data_id)).setText(String.valueOf(TOTAL_REJECTED));
									((TextView) findViewById(R.id.t_c_total_noa_data_id)).setText(String.valueOf(TOTAL_NOANSWER));
								}
							});
							/**********************************************************************************************************************
							 * TOP
							 * ********************************************************************************************************************/
							int x;
							tela.explist_children = new String[][][]{{{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}, {{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}, {{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}, {{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}, {{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}}};
							//
							cR = db.rawQuery("SELECT * FROM " + TopOUT[filter] + " LIMIT 5", null);
							if (cR != null && cR.moveToFirst())
							{
								x = 0;
								do
								{
									Cursor tmp = db.rawQuery("SELECT contactName FROM t_UserContact WHERE _id= \"" + cR.getString(1) + "\"", null);
									if (tmp.moveToFirst() && !tmp.getString(0).equalsIgnoreCase("UNKNOWN"))
										tela.explist_children[0][x++] = new String[]{tmp.getString(0), cR.getString(2), cR.getString(0)};
									else
										tela.explist_children[0][x++] = new String[]{cR.getString(1), cR.getString(2), cR.getString(0)};
									tmp.close();
								}
								while (cR.moveToNext() && x < 5);
							}
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TopIN[filter] + " LIMIT 5", null);
							if (cR != null && cR.moveToFirst())
							{
								x = 0;
								do
								{
									Cursor tmp = db.rawQuery("SELECT contactName FROM t_UserContact WHERE _id= \"" + cR.getString(1) + "\"", null);
									if (tmp.moveToFirst() && !tmp.getString(0).equalsIgnoreCase("UNKNOWN"))
										tela.explist_children[1][x++] = new String[]{tmp.getString(0), cR.getString(2), cR.getString(0)};
									else
										tela.explist_children[1][x++] = new String[]{cR.getString(1), cR.getString(2), cR.getString(0)};
									tmp.close();
								}
								while (cR.moveToNext() && x < 5);
							}
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TopMIS[filter] + " LIMIT 5", null);
							if (cR != null && cR.moveToFirst())
							{
								x = 0;
								do
								{
									Cursor tmp = db.rawQuery("SELECT contactName FROM t_UserContact WHERE _id= \"" + cR.getString(1) + "\"", null);
									if (tmp.moveToFirst() && !tmp.getString(0).equalsIgnoreCase("UNKNOWN"))
										tela.explist_children[2][x++] = new String[]{tmp.getString(0), cR.getString(2), cR.getString(0)};
									else
										tela.explist_children[2][x++] = new String[]{cR.getString(1), cR.getString(2), cR.getString(0)};
									tmp.close();
								}
								while (cR.moveToNext() && x < 5);
							}
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TopNOA[filter] + " LIMIT 5", null);
							if (cR != null && cR.moveToFirst())
							{
								x = 0;
								do
								{
									Cursor tmp = db.rawQuery("SELECT contactName FROM t_UserContact WHERE _id= \"" + cR.getString(1) + "\"", null);
									if (tmp.moveToFirst() && !tmp.getString(0).equalsIgnoreCase("UNKNOWN"))
										tela.explist_children[3][x++] = new String[]{tmp.getString(0), cR.getString(2), cR.getString(0)};
									else
										tela.explist_children[3][x++] = new String[]{cR.getString(1), cR.getString(2), cR.getString(0)};
									tmp.close();
								}
								while (cR.moveToNext() && x < 5);
							}
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TopREJ[filter] + " LIMIT 5", null);
							if (cR != null && cR.moveToFirst())
							{
								x = 0;
								do
								{
									Cursor tmp = db.rawQuery("SELECT contactName FROM t_UserContact WHERE _id= \"" + cR.getString(1) + "\"", null);
									if (tmp.moveToFirst() && !tmp.getString(0).equalsIgnoreCase("UNKNOWN"))
										tela.explist_children[4][x++] = new String[]{tmp.getString(0), cR.getString(2), cR.getString(0)};
									else
										tela.explist_children[4][x++] = new String[]{cR.getString(1), cR.getString(2), cR.getString(0)};
									tmp.close();
								}
								while (cR.moveToNext() && x < 5);
							}
							cR.close();
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
}
