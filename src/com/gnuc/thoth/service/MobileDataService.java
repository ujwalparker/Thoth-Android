package com.gnuc.thoth.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.ThothLog;
import com.gnuc.thoth.framework.db.ThothDB;
import com.gnuc.thoth.framework.network.NetworkStatistics;

public class MobileDataService extends IntentService
{
	public MobileDataService()
	{
		super("MobileDataService - Service");
	}
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		Thoth.cx = getApplicationContext();
		Thoth.Data.read();
	}
	
	@Override
	protected void onHandleIntent(final Intent intent)
	{
		long _DAYID, _BOOTID, _UPLOAD, _DOWNLOAD, _TIME;
		_DAYID = Thoth.Data.MOBILE_CURR_DAY_ID;
		_BOOTID = Thoth.Data.MOBILE_NWK_RESET;
		_UPLOAD = NetworkStatistics.getMTBytes();
		_DOWNLOAD = NetworkStatistics.getMRBytes();
		_TIME = System.currentTimeMillis();
		if (Thoth.Data.MOBILE_DAILY_CORRECTION)
		{
			Thoth.Data.MOBILE_DAILY_CORRECTION = false;
			Thoth.Data.MOBILE_DAILY_UL_CORRECTION = _UPLOAD;
			Thoth.Data.MOBILE_DAILY_DL_CORRECTION = _DOWNLOAD;
			if (Thoth.Data.MOBILE_DAILY_UL_CORRECTION >= Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV)
			{
				_UPLOAD = Thoth.Data.MOBILE_DAILY_UL_CORRECTION - Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV;
				Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV += _UPLOAD;
			}
			else
				_UPLOAD = Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
			if (Thoth.Data.MOBILE_DAILY_DL_CORRECTION >= Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV)
			{
				_DOWNLOAD = Thoth.Data.MOBILE_DAILY_DL_CORRECTION - Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV;
				Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV += _DOWNLOAD;
			}
			else
				_DOWNLOAD = Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
			Thoth.Data.write();
		}
		if (Thoth.Data.MOBILE_NEW_DAY && Thoth.Data.MOBILE_CURR_DAY_ID != Thoth.Data.MOBILE_PREV_DAY_ID)
		{
			Thoth.Data.MOBILE_NEW_DAY = false;
			Thoth.Data.MOBILE_DAILY_UL_CORRECTION_PREV = Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
			Thoth.Data.MOBILE_DAILY_DL_CORRECTION_PREV = Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
			_UPLOAD -= Thoth.Data.MOBILE_DAILY_UL_CORRECTION;
			_DOWNLOAD -= Thoth.Data.MOBILE_DAILY_DL_CORRECTION;
			Thoth.Data.write();
		}
		_UPLOAD = _UPLOAD < 0 ? 0 : _UPLOAD;
		_DOWNLOAD = _DOWNLOAD < 0 ? 0 : _DOWNLOAD;
		//
		ThothDB dbA = new ThothDB(this);
		SQLiteDatabase db = dbA.getDb();
		//
		ContentValues newRow = new ContentValues();
		newRow.put(ThothDB.col_T_D_M_PKEY, _DAYID + _BOOTID);
		newRow.put(ThothDB.col_T_D_M_DAYID, _DAYID);
		newRow.put(ThothDB.col_T_D_M_BOOTID, _BOOTID);
		newRow.put(ThothDB.col_T_D_M_UPLOAD, _UPLOAD);
		newRow.put(ThothDB.col_T_D_M_DOWNLOAD, _DOWNLOAD);
		newRow.put(ThothDB.col_T_D_M_TIME, _TIME);
		try
		{
			db.insertOrThrow(ThothDB.table_TEMP_DATA_MOBL, "", newRow);
		}
		catch (SQLiteConstraintException e)
		{
			try
			{
				db.replaceOrThrow(ThothDB.table_TEMP_DATA_MOBL, "", newRow);
			}
			catch (Exception ee)
			{
				//ThothLog.e(ee);
			}
		}
		catch (Exception e)
		{
			ThothLog.e(e);
		}
		if (db.isOpen())
			db.close();
	}
}