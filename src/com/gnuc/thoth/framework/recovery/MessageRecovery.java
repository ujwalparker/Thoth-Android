package com.gnuc.thoth.framework.recovery;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gnuc.thoth.R;
import com.gnuc.thoth.framework.ThothLog;
import com.gnuc.thoth.framework.db.LoggerDB;

public class MessageRecovery extends ListActivity
{
	Context					cx			= null;
	ArrayList<Message>	msgList	= null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.msg_recover_list_layout);
		//
		cx = this;
		msgList = new ArrayList<Message>();
		//
		final View layout = findViewById(R.id.msg_list_layout);
		final ListView lv = getListView();
		final MessageAdapter lvA = new MessageAdapter(MessageRecovery.this, msgList);
		lv.setAdapter(lvA);
		lv.setClickable(true);
		//
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int i, long l)
			{
				MsgViewHolder mvh = (MsgViewHolder) v.getTag();
				if (mvh.selected.isChecked())
				{
					mvh.selected.setChecked(false);
					msgList.get(i).selected = false;
				}
				else
				{
					mvh.selected.setChecked(true);
					msgList.get(i).selected = true;
				}
			}
		});
		findViewById(R.id.msg_list_select_all).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				for (Message m : msgList)
					m.selected = true;
				lvA.notifyDataSetChanged();
			}
		});
		findViewById(R.id.msg_list_deselect_all).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				for (Message m : msgList)
					m.selected = false;
				lvA.notifyDataSetChanged();
			}
		});
		findViewById(R.id.msg_list_selected).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(MessageRecovery.this);
				builder.setMessage("Are you sure you want to recover the selected messages?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						boolean result = false;
						for (final Message m : msgList)
						{
							if (m.isSelected())
							{
								result = true;
								runOnBackground(new Runnable()
								{
									public void run()
									{
										ContentValues values = new ContentValues();
										values.put("address", m.getAddress());
										values.put("body", m.getBody());
										values.put("date", m.getDate());
										values.put("date_sent", m.getDate());
										values.put("read", 1);
										values.put("seen", 1);
										switch (m.getType())
										{
											case 0 :
											{
												values.put("type", 1);
												Uri uri = getContentResolver().insert(Uri.parse("content://sms"), values);
												//ThothLog.w("Recovered an SMS : " + uri.toString());
												break;
											}
											case 1 :
											{
												values.put("type", 2);
												Uri uri = getContentResolver().insert(Uri.parse("content://sms"), values);
												//ThothLog.w("Recovered an SMS : " + uri.toString());
												break;
											}
										}
										//
									}
								});
							}
						}
						//
						for (Message m : msgList)
							m.selected = false;
						lvA.notifyDataSetChanged();
						if (result)
							Toast.makeText(getApplicationContext(), "Selected messages have been recovered.", Toast.LENGTH_LONG).show();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						dialog.cancel();
					}
				});
				builder.create().show();
			}
		});
		//
		runOnBackground(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Intent intent = getIntent();
					if (null != intent)
					{
						Cursor cu = null;
						long T_M_DATETIME;
						String T_M_CONTACT, T_M_MSG;
						int T_M_TYPE;
						LoggerDB dbA = new LoggerDB(cx);
						SQLiteDatabase db = dbA.getDb();
						// 0-IN SMS, 1-OUT SMS, 2-IN MMS, 3-OUT MMS
						if (intent.getAction().equalsIgnoreCase("SMS_IN"))
							T_M_TYPE = 0;
						else if (intent.getAction().equalsIgnoreCase("SMS_OUT"))
							T_M_TYPE = 1;
						else if (intent.getAction().equalsIgnoreCase("MMS_IN"))
							T_M_TYPE = 2;
						else
							T_M_TYPE = 3;
						cu = db.rawQuery("SELECT * FROM " + LoggerDB.table_LOGMSGS + " WHERE " + LoggerDB.col_LM_Type + "=" + T_M_TYPE + " ORDER BY " + LoggerDB.col_LM_Time + " ASC", null);
						if (cu != null && cu.moveToFirst())
						{
							do
							{
								T_M_CONTACT = cu.getString(cu.getColumnIndexOrThrow(LoggerDB.col_LM_Contact));
								T_M_DATETIME = cu.getLong(cu.getColumnIndexOrThrow(LoggerDB.col_LM_Time));
								T_M_MSG = cu.getString(cu.getColumnIndexOrThrow(LoggerDB.col_LM_Body));
								T_M_TYPE = cu.getInt(cu.getColumnIndexOrThrow(LoggerDB.col_LM_Type));
								//
								Cursor tmp = db.rawQuery("SELECT contactName FROM t_UserContact WHERE _id= \"" + T_M_CONTACT + "\"", null);
								if (tmp.moveToFirst() && !tmp.getString(0).equalsIgnoreCase("UNKNOWN"))
									T_M_CONTACT = tmp.getString(0);
								tmp.close();
								//
								msgList.add(new Message(T_M_CONTACT, T_M_MSG, T_M_DATETIME, T_M_TYPE));
							}
							while (cu.moveToNext());
							//
							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									layout.findViewById(R.id.msg_list_wait).setVisibility(View.GONE);
									layout.findViewById(R.id.msg_list_buttons).setVisibility(View.VISIBLE);
									lv.setVisibility(View.VISIBLE);
									layout.invalidate();
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
									layout.findViewById(R.id.msg_list_wait).setVisibility(View.GONE);
									Toast.makeText(getApplicationContext(), "No message available to recover.", Toast.LENGTH_LONG).show();
									finish();
								}
							});
						}
						if (null != cu && !cu.isClosed())
							cu.close();
						if (db.isOpen())
							db.close();
					}
				}
				catch (Exception e)
				{
					ThothLog.e(e);
				}
			}
		});
	}
	
	private static void runOnBackground(final Runnable r)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				r.run();
			};
		}.start();
	}
	
	private static class MessageAdapter extends ArrayAdapter<Message>
	{
		private final List<Message>	list;
		private Context					context;
		
		public MessageAdapter(Context context, ArrayList<Message> list)
		{
			super(context, R.layout.msg_recover_row_layout, list);
			this.list = list;
			this.context = context;
		}
		
		@Override
		public View getView(int position, View cV, ViewGroup parent)
		{
			View v = null;
			if (cV == null)
			{
				LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflator.inflate(R.layout.msg_recover_row_layout, null);
				final MsgViewHolder vH = new MsgViewHolder();
				vH.address = (TextView) v.findViewById(R.id.msg_row_title);
				vH.body = (TextView) v.findViewById(R.id.msg_row_body);
				vH.datetime = (TextView) v.findViewById(R.id.msg_row_datetime);
				vH.selected = (CheckBox) v.findViewById(R.id.msg_row_select);
				vH.selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
				{
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						Message element = (Message) vH.selected.getTag();
						element.selected = buttonView.isChecked();
					}
				});
				v.setTag(vH);
				vH.selected.setTag(list.get(position));
			}
			else
			{
				v = cV;
				((MsgViewHolder) v.getTag()).selected.setTag(list.get(position));
			}
			MsgViewHolder holder = (MsgViewHolder) v.getTag();
			holder.address.setText(list.get(position).getAddress());
			holder.body.setText(list.get(position).getBody());
			holder.datetime.setText(DateFormat.format("h:mm AA on dd/MM/yy", list.get(position).getDate()));
			holder.selected.setChecked(list.get(position).isSelected());
			return v;
		}
	}
	private static class MsgViewHolder
	{
		protected TextView	address;
		protected TextView	body;
		protected TextView	datetime;
		protected CheckBox	selected;
	}
	private class Message
	{
		private String		address, body;
		private long		date;
		private int			type;
		private boolean	selected;
		
		public Message(String address, String body, long date, int type)
		{
			super();
			this.address = address;
			this.body = body;
			this.date = date;
			this.type = type;
			this.selected = false;
		}
		
		public String getAddress()
		{
			return address;
		}
		
		public String getBody()
		{
			return body;
		}
		
		public long getDate()
		{
			return date;
		}
		
		public int getType()
		{
			return type;
		}
		
		public boolean isSelected()
		{
			return selected;
		}
	}
}
