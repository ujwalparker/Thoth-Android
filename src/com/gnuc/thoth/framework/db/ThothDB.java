package com.gnuc.thoth.framework.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.gnuc.thoth.framework.ThothLog;

public class ThothDB
{
	/**
	 * Thoth Database
	 * 
	 * Database name & Database version
	 */
	private static final String	DATABASE_NAME						= "thoth.db";
	private static final int		DATABASE_VERSION					= 2;
	/**
	 ****************************** table_HASH_CALL ******************************
	 */
	public static final String		table_HASH_CALL					= "h_Call";
	/* COLUMNS */
	public static final String		col_H_C_PKEY						= "_id";
	/**
	 ****************************** table_HASH_MSGS ******************************
	 */
	public static final String		table_HASH_MSGS					= "h_Msgs";
	/* COLUMNS */
	public static final String		col_H_M_PKEY						= "_id";
	/**
	 * Used to store the package name hash table_HASH_APPS ******************************
	 */
	public static final String		table_HASH_APPS					= "h_Apps";
	/* COLUMNS */
	public static final String		col_H_A_PKEY						= "_id";
	/**
	 * ****************************** table_TEMP_CALL ******************************
	 */
	public static final String		table_TEMP_CALL					= "t_Call";
	/* COLUMNS */
	public static final String		col_T_C_PKEY						= "_id";
	public static final String		col_T_C_FKEY						= "hash";
	public static final String		col_T_C_NUMBER						= "number";
	public static final String		col_T_C_CONTACT					= "contact";
	public static final String		col_T_C_CONTACTNAME				= "contact_name";
	public static final String		col_T_C_DATETIME					= "time";
	public static final String		col_T_C_DURATION					= "duration";
	// 0-INBOUND, 1-OUTBOUND, 2-REJECTED, 3-MISSED, 4-NOANSWER
	public static final String		col_T_C_TYPE						= "type";
	/**
	 * ****************************** table_TEMP_MSGS ******************************
	 **/
	/* TABLE */
	public static final String		table_TEMP_MSGS					= "t_Msgs";
	/* COLUMNS */
	public static final String		col_T_M_PKEY						= "_id";
	public static final String		col_T_M_FKEY						= "hash";
	public static final String		col_T_M_NUMBER						= "number";
	public static final String		col_T_M_CONTACT					= "contact";
	public static final String		col_T_M_CONTACTNAME				= "contact_name";
	public static final String		col_T_M_DATETIME					= "time";
	public static final String		col_T_M_MSG							= "message";
	// 0-IN SMS, 1-OUT SMS, 2-IN MMS, 3-OUT MMS
	public static final String		col_T_M_TYPE						= "type";
	public static final String		col_T_M_BLOB_TEXT					= "blob_text";
	public static final String		col_T_M_BLOB_DATA					= "blob_data";
	public static final String		col_T_M_BLOB_DATA_MIME			= "blob_data_mime";
	/**
	 * ****************************** table_TEMP_APPS ******************************
	 */
	public static final String		table_TEMP_APPS					= "t_Apps";
	/* COLUMNS */
	public static final String		col_T_A_PKEY						= "_id";
	public static final String		col_T_A_PKG							= "pkg";																																																																																																																																																								// col_H_A_PKEY
	public static final String		col_T_A_NAME						= "name";
	public static final String		col_T_A_UID							= "uid";
	public static final String		col_T_A_UID_NAME					= "uid_name";
	public static final String		col_T_A_UPLOAD						= "upload";
	public static final String		col_T_A_DOWNLOAD					= "download";
	public static final String		col_T_A_DAYID						= "day_id";
	public static final String		col_T_A_BOOTID						= "boot_id";
	/**
	 * Used to store the package name hash table_TEMP_DATA_MOBL ******************************
	 */
	public static final String		table_TEMP_DATA_MOBL				= "t_Data_Mobile";
	/* COLUMNS */
	public static final String		col_T_D_M_PKEY						= "_id";
	public static final String		col_T_D_M_DAYID					= "day_id";
	public static final String		col_T_D_M_BOOTID					= "boot_id";
	public static final String		col_T_D_M_UPLOAD					= "upload";
	public static final String		col_T_D_M_DOWNLOAD				= "download";
	public static final String		col_T_D_M_TIME						= "time";
	/**
	 * Used to store the package name hash table_TEMP_DATA_WIFI ******************************
	 */
	public static final String		table_TEMP_DATA_WIFI				= "t_Data_Wifi";
	/* COLUMNS */
	public static final String		col_T_D_W_PKEY						= "_id";
	public static final String		col_T_D_W_DAYID					= "day_id";
	public static final String		col_T_D_W_BOOTID					= "boot_id";
	public static final String		col_T_D_W_UPLOAD					= "upload";
	public static final String		col_T_D_W_DOWNLOAD				= "download";
	public static final String		col_T_D_W_TIME						= "time";
	/**
	 * Table create scripts
	 */
	private static final String	CREATE_table_HASH_CALL			= "CREATE TABLE " + table_HASH_CALL + " (" + col_H_C_PKEY + " TEXT NOT NULL PRIMARY KEY);";
	private static final String	CREATE_table_HASH_MSGS			= "CREATE TABLE " + table_HASH_MSGS + " (" + col_H_M_PKEY + " TEXT NOT NULL PRIMARY KEY);";
	private static final String	CREATE_table_HASH_APPS			= "CREATE TABLE " + table_HASH_APPS + " (" + col_H_A_PKEY + " TEXT NOT NULL PRIMARY KEY);";
	private static final String	CREATE_table_TEMP_CALL			= "CREATE TABLE " + table_TEMP_CALL + " (" + col_T_C_PKEY + " INTEGER PRIMARY KEY AUTOINCREMENT," + col_T_C_FKEY + " TEXT UNIQUE REFERENCES " + table_HASH_CALL + "(" + col_H_C_PKEY + ")," + col_T_C_NUMBER + " TEXT," + col_T_C_CONTACT + " TEXT," + col_T_C_CONTACTNAME + " TEXT," + col_T_C_DATETIME + " INTEGER," + col_T_C_DURATION + " INTEGER," + col_T_C_TYPE + " INTEGER);";
	private static final String	CREATE_table_TEMP_MSGS			= "CREATE TABLE " + table_TEMP_MSGS + " (" + col_T_M_PKEY + " INTEGER PRIMARY KEY AUTOINCREMENT," + col_T_M_FKEY + " TEXT UNIQUE REFERENCES " + table_HASH_MSGS + "(" + col_H_M_PKEY + ")," + col_T_M_NUMBER + " TEXT," + col_T_M_CONTACT + " TEXT," + col_T_M_CONTACTNAME + " TEXT," + col_T_M_DATETIME + " INTEGER," + col_T_M_MSG + " TEXT," + col_T_M_TYPE + " INTEGER," + col_T_M_BLOB_TEXT + " BLOB," + col_T_M_BLOB_DATA + " BLOB," + col_T_M_BLOB_DATA_MIME + " TEXT);";
	private static final String	CREATE_table_TEMP_APPS			= "CREATE TABLE " + table_TEMP_APPS + " (" + col_T_A_PKEY + " INTEGER PRIMARY KEY," + col_T_A_PKG + " TEXT," + col_T_A_NAME + " TEXT," + col_T_A_UID + " INTEGER," + col_T_A_UID_NAME + " TEXT," + col_T_A_UPLOAD + " INTEGER," + col_T_A_DOWNLOAD + " INTEGER," + col_T_A_DAYID + " INTEGER," + col_T_A_BOOTID + " INTEGER);";
	private static final String	CREATE_table_TEMP_DATA_MOBL	= "CREATE TABLE " + table_TEMP_DATA_MOBL + " (" + col_T_D_M_PKEY + " INTEGER PRIMARY KEY," + col_T_D_M_DAYID + " INTEGER," + col_T_D_M_BOOTID + " INTEGER," + col_T_D_M_UPLOAD + " INTEGER," + col_T_D_M_DOWNLOAD + " INTEGER," + col_T_D_M_TIME + " INTEGER);";
	private static final String	CREATE_table_TEMP_DATA_WIFI	= "CREATE TABLE " + table_TEMP_DATA_WIFI + " (" + col_T_D_W_PKEY + " INTEGER PRIMARY KEY," + col_T_D_W_DAYID + " INTEGER," + col_T_D_W_BOOTID + " INTEGER," + col_T_D_W_UPLOAD + " INTEGER," + col_T_D_W_DOWNLOAD + " INTEGER," + col_T_D_W_TIME + " INTEGER);";
	/**
	 * Table drop scripts
	 */
	private static final String	DROP_table_HASH_CALL				= "DROP TABLE IF EXISTS " + table_HASH_CALL + ";";
	private static final String	DROP_table_HASH_MSGS				= "DROP TABLE IF EXISTS " + table_HASH_MSGS + ";";
	private static final String	DROP_table_HASH_APPS				= "DROP TABLE IF EXISTS " + table_HASH_APPS + ";";
	private static final String	DROP_table_TEMP_CALL				= "DROP TABLE IF EXISTS " + table_TEMP_CALL + ";";
	private static final String	DROP_table_TEMP_MSGS				= "DROP TABLE IF EXISTS " + table_TEMP_MSGS + ";";
	private static final String	DROP_table_TEMP_APPS				= "DROP TABLE IF EXISTS " + table_TEMP_APPS + ";";
	private static final String	DROP_table_TEMP_DATA_MOBL		= "DROP TABLE IF EXISTS " + table_TEMP_DATA_MOBL + ";";
	private static final String	DROP_table_TEMP_DATA_WIFI		= "DROP TABLE IF EXISTS " + table_TEMP_DATA_WIFI + ";";
	/**
	 * Table clear scripts
	 */
	public static final String		CLEAR_table_HASH_CALL			= "DELETE FROM " + table_HASH_CALL + ";";
	public static final String		CLEAR_table_HASH_MSGS			= "DELETE FROM " + table_HASH_MSGS + ";";
	public static final String		CLEAR_table_HASH_APPS			= "DELETE FROM " + table_HASH_APPS + ";";
	public static final String		CLEAR_table_TEMP_CALL			= "DELETE FROM " + table_TEMP_CALL + ";";
	public static final String		CLEAR_table_TEMP_MSGS			= "DELETE FROM " + table_TEMP_MSGS + ";";
	public static final String		CLEAR_table_TEMP_APPS			= "DELETE FROM " + table_TEMP_APPS + ";";
	public static final String		CLEAR_table_TEMP_DATA_MOBL		= "DELETE FROM " + table_TEMP_DATA_MOBL + ";";
	public static final String		CLEAR_table_TEMP_DATA_WIFI		= "DELETE FROM " + table_TEMP_DATA_WIFI + ";";
	//
	private SQLiteDatabase			db;
	
	public SQLiteDatabase getDb()
	{
		return db;
	}
	
	public void setDb(SQLiteDatabase db)
	{
		this.db = db;
	}
	
	private final Context	dbContext;
	private DBHelper			dbHelper	= null;
	
	public ThothDB(Context cx)
	{
		dbContext = cx;
		if (null == db)
		{
			dbHelper = new DBHelper(dbContext, DATABASE_NAME, null, DATABASE_VERSION);
			try
			{
				db = dbHelper.getWritableDatabase();
			}
			catch (SQLiteException ex)
			{
				db = dbHelper.getReadableDatabase();
			}
		}
	}
	
	public void close()
	{
		if (dbHelper != null)
		{
			dbHelper.close();
		}
	}
	
	public ThothDB refreshHashTables(SQLiteDatabase db)
	{
		// ThothLog.i("All HASH_XXXX data will be refreshed.");
		db.execSQL(CLEAR_table_HASH_CALL);
		db.execSQL(CLEAR_table_HASH_MSGS);
		db.execSQL(CLEAR_table_HASH_APPS);
		return this;
	}
	
	public ThothDB refreshTempTables(SQLiteDatabase db)
	{
		// ThothLog.i("All TEMP_XXXX data will be refreshed.");
		db.execSQL(CLEAR_table_TEMP_CALL);
		db.execSQL(CLEAR_table_TEMP_MSGS);
		db.execSQL(CLEAR_table_TEMP_APPS);
		db.execSQL(CLEAR_table_TEMP_DATA_WIFI);
		db.execSQL(CLEAR_table_TEMP_DATA_MOBL);
		return this;
	}
	
	public ThothDB refreshAllTables(SQLiteDatabase db)
	{
		// ThothLog.i("All TABLE data will be refreshed.");
		db.execSQL(CLEAR_table_HASH_CALL);
		db.execSQL(CLEAR_table_HASH_MSGS);
		db.execSQL(CLEAR_table_HASH_APPS);
		db.execSQL(CLEAR_table_TEMP_CALL);
		db.execSQL(CLEAR_table_TEMP_MSGS);
		db.execSQL(CLEAR_table_TEMP_APPS);
		db.execSQL(CLEAR_table_TEMP_DATA_MOBL);
		db.execSQL(CLEAR_table_TEMP_DATA_WIFI);
		//
		return this;
	}
	
	private static class DBHelper extends SQLiteOpenHelper
	{
		public DBHelper(Context context, String name, CursorFactory factory, int version)
		{
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(CREATE_table_HASH_CALL);
			db.execSQL(CREATE_table_HASH_MSGS);
			db.execSQL(CREATE_table_HASH_APPS);
			db.execSQL(CREATE_table_TEMP_CALL);
			db.execSQL(CREATE_table_TEMP_MSGS);
			db.execSQL(CREATE_table_TEMP_APPS);
			db.execSQL(CREATE_table_TEMP_DATA_MOBL);
			db.execSQL(CREATE_table_TEMP_DATA_WIFI);
			//
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			ThothLog.w("Upgrading " + this.getClass().getName() + " from version " + oldVersion + " to " + newVersion + ". All TABLE data will be deleted.");
			//
			db.execSQL(DROP_table_HASH_CALL);
			db.execSQL(DROP_table_HASH_MSGS);
			db.execSQL(DROP_table_HASH_APPS);
			db.execSQL(DROP_table_TEMP_CALL);
			db.execSQL(DROP_table_TEMP_MSGS);
			db.execSQL(DROP_table_TEMP_APPS);
			db.execSQL(DROP_table_TEMP_DATA_MOBL);
			db.execSQL(DROP_table_TEMP_DATA_WIFI);
			//
			onCreate(db);
		}
	}
}
