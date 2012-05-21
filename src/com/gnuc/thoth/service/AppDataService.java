package com.gnuc.thoth.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.gnuc.thoth.framework.Thoth;
import com.gnuc.thoth.framework.db.ThothDB;
import com.gnuc.thoth.framework.network.NetworkStatistics;

public class AppDataService extends IntentService
{
	public AppDataService()
	{
		super("AppDataService - Service");
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
		if (Thoth.Data.NEW_DAY && Thoth.Data.PREV_DAY_ID != Thoth.Data.CURR_DAY_ID)
		{
			Thoth.Data.NEW_DAY = false;
			Thoth.Data.write();
			startOfDayParse();
		}
		else if (Thoth.Data.DAILY_CORRECTION)
		{
			Thoth.Data.DAILY_CORRECTION = false;
			Thoth.Data.write();
			endOfDayParse();
		}
		else
			regularParse();
	}
	
	public boolean startOfDayParse()
	{
		final PackageManager pkgMgr = getPackageManager();
		//
		ThothDB dbA = new ThothDB(this);
		SQLiteDatabase db = dbA.getDb();
		//
		for (PackageInfo app : pkgMgr.getInstalledPackages(0))
		{
			if (!((app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0))
			{
				int appUID = app.applicationInfo.uid;
				String appName = app.applicationInfo.loadLabel(getPackageManager()).toString();
				String appPackage = app.packageName;
				String appUIDName = pkgMgr.getNameForUid(appUID);
				long appUpload = NetworkStatistics.getATBytes(appUID);
				long appDownload = NetworkStatistics.getARBytes(appUID);
				long appDayId = Thoth.Data.CURR_DAY_ID;
				long appBootId = Thoth.Data.DATA_RESET;
				ContentValues newRow = new ContentValues();
				newRow.put(ThothDB.col_H_A_PKEY, appPackage);
				try
				{
					db.insertOrThrow(ThothDB.table_HASH_APPS, "", newRow);
				}
				catch (Exception e)
				{
					//ThothLog.e(e);
				}
				long UL_CORRECTION = appUpload, UL_CORRECTION_PREV = 0, DL_CORRECTION = appDownload, DL_CORRECTION_PREV = 0;
				Cursor cR = db.rawQuery("SELECT SUM(" + ThothDB.col_T_A_UPLOAD + "), SUM(" + ThothDB.col_T_A_DOWNLOAD + ") FROM " + ThothDB.table_TEMP_APPS + " WHERE " + ThothDB.col_T_A_PKG + "='" + appPackage + "' AND " + ThothDB.col_T_A_DAYID + "=" + Thoth.Data.PREV_DAY_ID + " AND " + ThothDB.col_T_A_BOOTID + "=" + appBootId + " GROUP BY " + ThothDB.col_T_A_DAYID, null);
				if (null != cR && cR.moveToFirst())
				{
					UL_CORRECTION_PREV = cR.getLong(0);
					DL_CORRECTION_PREV = cR.getLong(1);
					cR.close();
				}
				appUpload = (UL_CORRECTION >= UL_CORRECTION_PREV) ? (UL_CORRECTION - UL_CORRECTION_PREV) : UL_CORRECTION;
				appDownload = (DL_CORRECTION >= DL_CORRECTION_PREV) ? (DL_CORRECTION - DL_CORRECTION_PREV) : DL_CORRECTION;
				if (appUpload < 1 && appDownload < 1)
					continue;
				newRow = new ContentValues();
				newRow.put(ThothDB.col_T_A_PKG, appPackage);
				newRow.put(ThothDB.col_T_A_NAME, appName);
				newRow.put(ThothDB.col_T_A_UID, appUID);
				newRow.put(ThothDB.col_T_A_UID_NAME, appUIDName);
				newRow.put(ThothDB.col_T_A_DAYID, appDayId);
				newRow.put(ThothDB.col_T_A_BOOTID, appBootId);
				newRow.put(ThothDB.col_T_A_UPLOAD, appUpload);
				newRow.put(ThothDB.col_T_A_DOWNLOAD, appDownload);
				try
				{
					db.insertOrThrow(ThothDB.table_TEMP_APPS, "", newRow);
				}
				catch (Exception e)
				{
					//ThothLog.e(e);
				}
			}
		}
		if (db.isOpen())
			db.close();
		return true;
	}
	
	public boolean endOfDayParse()
	{
		final PackageManager pkgMgr = getPackageManager();
		//
		ThothDB dbA = new ThothDB(this);
		SQLiteDatabase db = dbA.getDb();
		//
		for (PackageInfo app : pkgMgr.getInstalledPackages(0))
		{
			if (!((app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0))
			{
				int appUID = app.applicationInfo.uid;
				String appName = app.applicationInfo.loadLabel(getPackageManager()).toString();
				String appPackage = app.packageName;
				String appUIDName = pkgMgr.getNameForUid(appUID);
				long appUpload = NetworkStatistics.getATBytes(appUID);
				long appDownload = NetworkStatistics.getARBytes(appUID);
				long appDayId = Thoth.Data.CURR_DAY_ID;
				long appBootId = Thoth.Data.DATA_RESET;
				ContentValues newRow = new ContentValues();
				newRow.put(ThothDB.col_H_A_PKEY, appPackage);
				try
				{
					db.insertOrThrow(ThothDB.table_HASH_APPS, "", newRow);
				}
				catch (Exception e)
				{
					//ThothLog.e(e);
				}
				long UL_CORRECTION = appUpload, UL_CORRECTION_PREV = 0, DL_CORRECTION = appDownload, DL_CORRECTION_PREV = 0;
				Cursor cR = db.rawQuery("SELECT " + ThothDB.col_T_A_UPLOAD + ", " + ThothDB.col_T_A_DOWNLOAD + " FROM " + ThothDB.table_TEMP_APPS + " WHERE " + ThothDB.col_T_A_PKG + "='" + appPackage + "' AND " + ThothDB.col_T_A_DAYID + "=" + appDayId + " AND " + ThothDB.col_T_A_BOOTID + "=" + appBootId, null);
				if (null != cR && cR.moveToFirst())
				{
					UL_CORRECTION_PREV = cR.getLong(0);
					DL_CORRECTION_PREV = cR.getLong(1);
					cR.close();
				}
				appUpload = (UL_CORRECTION >= UL_CORRECTION_PREV) ? (UL_CORRECTION - UL_CORRECTION_PREV) : UL_CORRECTION;
				appDownload = (DL_CORRECTION >= DL_CORRECTION_PREV) ? (DL_CORRECTION - DL_CORRECTION_PREV) : DL_CORRECTION;
				if (appUpload < 1 && appDownload < 1)
					continue;
				newRow = new ContentValues();
				newRow.put(ThothDB.col_T_A_PKG, appPackage);
				newRow.put(ThothDB.col_T_A_NAME, appName);
				newRow.put(ThothDB.col_T_A_UID, appUID);
				newRow.put(ThothDB.col_T_A_UID_NAME, appUIDName);
				newRow.put(ThothDB.col_T_A_DAYID, appDayId);
				newRow.put(ThothDB.col_T_A_BOOTID, appBootId);
				newRow.put(ThothDB.col_T_A_UPLOAD, appUpload);
				newRow.put(ThothDB.col_T_A_DOWNLOAD, appDownload);
				try
				{
					db.insertOrThrow(ThothDB.table_TEMP_APPS, "", newRow);
				}
				catch (Exception e)
				{
					//ThothLog.e(e);
				}
			}
		}
		if (db.isOpen())
			db.close();
		return true;
	}
	
	public boolean regularParse()
	{
		final PackageManager pkgMgr = getPackageManager();
		//
		ThothDB dbA = new ThothDB(this);
		SQLiteDatabase db = dbA.getDb();
		//
		for (PackageInfo app : pkgMgr.getInstalledPackages(0))
		{
			if (!((app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0))
			{
				int appUID = app.applicationInfo.uid;
				String appName = app.applicationInfo.loadLabel(getPackageManager()).toString();
				String appPackage = app.packageName;
				String appUIDName = pkgMgr.getNameForUid(appUID);
				long appUpload = NetworkStatistics.getATBytes(appUID);
				long appDownload = NetworkStatistics.getARBytes(appUID);
				long appDayId = Thoth.Data.CURR_DAY_ID;
				long appBootId = Thoth.Data.DATA_RESET;
				ContentValues newRow = new ContentValues();
				newRow.put(ThothDB.col_H_A_PKEY, appPackage);
				try
				{
					db.insertOrThrow(ThothDB.table_HASH_APPS, "", newRow);
				}
				catch (SQLiteConstraintException e)
				{}
				catch (Exception e)
				{
					//ThothLog.e(e);
				}
				long UL_CORRECTION = appUpload, UL_CORRECTION_PREV = 0, DL_CORRECTION = appDownload, DL_CORRECTION_PREV = 0;
				Cursor cR = db.rawQuery("SELECT SUM(" + ThothDB.col_T_A_UPLOAD + "), SUM(" + ThothDB.col_T_A_DOWNLOAD + ") FROM " + ThothDB.table_TEMP_APPS + " WHERE " + ThothDB.col_T_A_PKG + "='" + appPackage + "' AND " + ThothDB.col_T_A_DAYID + "=" + appDayId + " AND " + ThothDB.col_T_A_BOOTID + "=" + appBootId + " GROUP BY " + ThothDB.col_T_A_DAYID, null);
				if (null != cR && cR.moveToFirst())
				{
					UL_CORRECTION_PREV = cR.getLong(0);
					DL_CORRECTION_PREV = cR.getLong(1);
					cR.close();
				}
				appUpload = (UL_CORRECTION >= UL_CORRECTION_PREV) ? (UL_CORRECTION - UL_CORRECTION_PREV) : UL_CORRECTION;
				appDownload = (DL_CORRECTION >= DL_CORRECTION_PREV) ? (DL_CORRECTION - DL_CORRECTION_PREV) : DL_CORRECTION;
				if (appUpload < 1 && appDownload < 1)
					continue;
				newRow = new ContentValues();
				newRow.put(ThothDB.col_T_A_PKG, appPackage);
				newRow.put(ThothDB.col_T_A_NAME, appName);
				newRow.put(ThothDB.col_T_A_UID, appUID);
				newRow.put(ThothDB.col_T_A_UID_NAME, appUIDName);
				newRow.put(ThothDB.col_T_A_DAYID, appDayId);
				newRow.put(ThothDB.col_T_A_BOOTID, appBootId);
				newRow.put(ThothDB.col_T_A_UPLOAD, appUpload);
				newRow.put(ThothDB.col_T_A_DOWNLOAD, appDownload);
				try
				{
					db.insertOrThrow(ThothDB.table_TEMP_APPS, "", newRow);
				}
				catch (SQLiteConstraintException e)
				{}
				catch (Exception e)
				{
					// ThothLog.e(e);
				}
			}
		}
		if (db.isOpen())
			db.close();
		return true;
	}
}