package com.gnuc.thoth.app.screens;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

public class MessagingMain extends ExpandableListActivity
{
	private ThothExpandableListAdapter	elAdapter;
	private ExpandableListView				elView;
	private Context							elContext;
	private static MessagingMain			instance	= null;
	
	public static MessagingMain getInstance()
	{
		if (instance == null)
			instance = new MessagingMain();
		return instance;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try
		{
			setContentView(R.layout.msg_main);
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
			elView.addHeaderView(View.inflate(this, R.layout.msg_layout, null), null, false);
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
		Thoth.TRACKER.trackPageView("/MSGS_MAIN");
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
			explist_parents = new Integer[]{R.layout.explist_m_s_top_out, R.layout.explist_m_s_top_in, R.layout.explist_m_m_top_out, R.layout.explist_m_m_top_in};
			explist_children = new String[][][]{{{"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}}, {{"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}}, {{"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}}, {{"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}}};
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
			RelativeLayout child = (RelativeLayout) View.inflate(elContext, R.layout.explist_m_childrow, null);
			String cPN = explist_children[groupPosition][childPosition][0];
			String cPQ = explist_children[groupPosition][childPosition][1];
			cPN = (cPN.equals("")) ? " - " : cPN;
			cPQ = (cPQ.equals("")) ? "( - )" : "(" + cPQ + " times.)";
			TextView tv = ((TextView) child.findViewById(R.id.t_m_child_name_label_id));
			tv.setText(cPN);
			tv.setVisibility(View.VISIBLE);
			tv = (TextView) child.findViewById(R.id.t_m_child_quantity_label_id);
			tv.setText(cPQ);
			tv.setVisibility(View.VISIBLE);
			child.findViewById(R.id.t_m_child_name_label_pb).setVisibility(View.INVISIBLE);
			child.findViewById(R.id.t_m_child_quantity_label_pb).setVisibility(View.INVISIBLE);
			child.setTag(Thoth.APP, cPN);
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
	String[]	SMS			= {"t_LogMsgs_TODAY_SMS", "t_LogMsgs_THISWEEK_SMS", "t_LogMsgs_THISMONTH_SMS", "t_LogMsgs_LASTWEEK_SMS", "t_LogMsgs_LASTMONTH_SMS"};
	String[]	MMS			= {"t_LogMsgs_TODAY_MMS", "t_LogMsgs_THISWEEK_MMS", "t_LogMsgs_THISMONTH_MMS", "t_LogMsgs_LASTWEEK_MMS", "t_LogMsgs_LASTMONTH_MMS"};
	//
	String[]	TotalSMSIN	= {"t_LogMsgs_TODAY_TotalSMSIN", "t_LogMsgs_THISWEEK_TotalSMSIN", "t_LogMsgs_THISMONTH_TotalSMSIN", "t_LogMsgs_LASTWEEK_TotalSMSIN", "t_LogMsgs_LASTMONTH_TotalSMSIN"};
	String[]	TotalSMSOUT	= {"t_LogMsgs_TODAY_TotalSMSOUT", "t_LogMsgs_THISWEEK_TotalSMSOUT", "t_LogMsgs_THISMONTH_TotalSMSOUT", "t_LogMsgs_LASTWEEK_TotalSMSOUT", "t_LogMsgs_LASTMONTH_TotalSMSOUT"};
	String[]	TotalMMSIN	= {"t_LogMsgs_TODAY_TotalMMSIN", "t_LogMsgs_THISWEEK_TotalMMSIN", "t_LogMsgs_THISMONTH_TotalMMSIN", "t_LogMsgs_LASTWEEK_TotalMMSIN", "t_LogMsgs_LASTMONTH_TotalMMSIN"};
	String[]	TotalMMSOUT	= {"t_LogMsgs_TODAY_TotalMMSOUT", "t_LogMsgs_THISWEEK_TotalMMSOUT", "t_LogMsgs_THISMONTH_TotalMMSOUT", "t_LogMsgs_LASTWEEK_TotalMMSOUT", "t_LogMsgs_LASTMONTH_TotalMMSOUT"};
	//
	String[]	TopSMSIN		= {"t_LogMsgs_TODAY_TopSMSIN", "t_LogMsgs_THISWEEK_TopSMSIN", "t_LogMsgs_THISMONTH_TopSMSIN", "t_LogMsgs_LASTWEEK_TopSMSIN", "t_LogMsgs_LASTMONTH_TopSMSIN"};
	String[]	TopSMSOUT	= {"t_LogMsgs_TODAY_TopSMSOUT", "t_LogMsgs_THISWEEK_TopSMSOUT", "t_LogMsgs_THISMONTH_TopSMSOUT", "t_LogMsgs_LASTWEEK_TopSMSOUT", "t_LogMsgs_LASTMONTH_TopSMSOUT"};
	String[]	TopMMSIN		= {"t_LogMsgs_TODAY_TopMMSIN", "t_LogMsgs_THISWEEK_TopMMSIN", "t_LogMsgs_THISMONTH_TopMMSIN", "t_LogMsgs_LASTWEEK_TopMMSIN", "t_LogMsgs_LASTMONTH_TopMMSIN"};
	String[]	TopMMSOUT	= {"t_LogMsgs_TODAY_TopMMSOUT", "t_LogMsgs_THISWEEK_TopMMSOUT", "t_LogMsgs_THISMONTH_TopMMSOUT", "t_LogMsgs_LASTWEEK_TopMMSOUT", "t_LogMsgs_LASTMONTH_TopMMSOUT"};
	//
	private static int	TOTAL_SMS_INBOUND	= 0, TOTAL_SMS_OUTBOUND = 0, TOTAL_MMS_INBOUND = 0, TOTAL_MMS_OUTBOUND = 0;
	
	//
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
						LoggerDB ldb = new LoggerDB(Thoth.cx);
						try
						{
							SQLiteDatabase db = ldb.getDb();
							/**********************************************************************************************************************
							 * TOTAL
							 * ********************************************************************************************************************/
							cR = db.rawQuery("SELECT * FROM " + TotalSMSIN[filter], null);
							TOTAL_SMS_INBOUND = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_SMS_INBOUND;
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TotalSMSOUT[filter], null);
							TOTAL_SMS_OUTBOUND = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_SMS_OUTBOUND;
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TotalMMSIN[filter], null);
							TOTAL_MMS_INBOUND = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_MMS_INBOUND;
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TotalMMSOUT[filter], null);
							TOTAL_MMS_OUTBOUND = (cR != null && cR.moveToFirst()) ? cR.getInt(0) : TOTAL_MMS_OUTBOUND;
							cR.close();
							/********************************************************************************************************************** 
							 * ********************************************************************************************************************/
							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									((TextView) findViewById(R.id.t_m_s_total_out_data_id)).setText(String.valueOf(TOTAL_SMS_OUTBOUND));
									((TextView) findViewById(R.id.t_m_s_total_in_data_id)).setText(String.valueOf(TOTAL_SMS_INBOUND));
									((TextView) findViewById(R.id.t_m_m_total_out_data_id)).setText(String.valueOf(TOTAL_MMS_OUTBOUND));
									((TextView) findViewById(R.id.t_m_m_total_in_data_id)).setText(String.valueOf(TOTAL_MMS_INBOUND));
								}
							});
							/**********************************************************************************************************************
							 * TOP
							 * ********************************************************************************************************************/
							int x;
							tela.explist_children = new String[][][]{{{"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}}, {{"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}}, {{"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}}, {{"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}}};
							//
							cR = db.rawQuery("SELECT * FROM " + TopSMSOUT[filter] + " LIMIT 5", null);
							if (cR != null && cR.moveToFirst())
							{
								x = 0;
								do
								{
									Cursor tmp = db.rawQuery("SELECT contactName FROM t_UserContact WHERE _id= \"" + cR.getString(1) + "\"", null);
									if (tmp.moveToFirst() && !tmp.getString(0).equalsIgnoreCase("UNKNOWN"))
										tela.explist_children[0][x++] = new String[]{tmp.getString(0), cR.getString(0)};
									else
										tela.explist_children[0][x++] = new String[]{cR.getString(1), cR.getString(0)};
									tmp.close();
								}
								while (cR.moveToNext() && x < 5);
							}
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TopSMSIN[filter] + " LIMIT 5", null);
							if (cR != null && cR.moveToFirst())
							{
								x = 0;
								do
								{
									Cursor tmp = db.rawQuery("SELECT contactName FROM t_UserContact WHERE _id= \"" + cR.getString(1) + "\"", null);
									if (tmp.moveToFirst() && !tmp.getString(0).equalsIgnoreCase("UNKNOWN"))
										tela.explist_children[1][x++] = new String[]{tmp.getString(0), cR.getString(0)};
									else
										tela.explist_children[1][x++] = new String[]{cR.getString(1), cR.getString(0)};
									tmp.close();
								}
								while (cR.moveToNext() && x < 5);
							}
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TopMMSOUT[filter] + " LIMIT 5", null);
							if (cR != null && cR.moveToFirst())
							{
								x = 0;
								do
								{
									Cursor tmp = db.rawQuery("SELECT contactName FROM t_UserContact WHERE _id= \"" + cR.getString(1) + "\"", null);
									if (tmp.moveToFirst() && !tmp.getString(0).equalsIgnoreCase("UNKNOWN"))
										tela.explist_children[2][x++] = new String[]{tmp.getString(0), cR.getString(0)};
									else
										tela.explist_children[2][x++] = new String[]{cR.getString(1), cR.getString(0)};
									tmp.close();
								}
								while (cR.moveToNext() && x < 5);
							}
							cR.close();
							//
							cR = db.rawQuery("SELECT * FROM " + TopMMSIN[filter] + " LIMIT 5", null);
							if (cR != null && cR.moveToFirst())
							{
								x = 0;
								do
								{
									Cursor tmp = db.rawQuery("SELECT contactName FROM t_UserContact WHERE _id= \"" + cR.getString(1) + "\"", null);
									if (tmp.moveToFirst() && !tmp.getString(0).equalsIgnoreCase("UNKNOWN"))
										tela.explist_children[3][x++] = new String[]{tmp.getString(0), cR.getString(0)};
									else
										tela.explist_children[3][x++] = new String[]{cR.getString(1), cR.getString(0)};
									tmp.close();
								}
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
}
