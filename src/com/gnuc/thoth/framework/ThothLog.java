package com.gnuc.thoth.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class ThothLog
{
	final static String	TAG	= "Thoth-Log";
	
	public static void i(String msg)
	{
		Log.i(TAG, msg);
	}
	
	public static void d(String msg)
	{
		Log.d(TAG, msg);
	}
	
	public static void dL(String msg)
	{
		Log.d(TAG, msg);
		logDebugToFile(msg);
	}
	
	public static void w(String msg)
	{
		Log.w(TAG, msg);
	}
	
	public static void e(Throwable tr)
	{
		Log.e(TAG, tr.getMessage(), tr);
		logExceptionToFile(tr);
	}
	
	public static void logDebugToFile(String msg)
	{
		try
		{
			msg += "\n";
			FileOutputStream f = Thoth.cx.openFileOutput(TAG + ".log", Context.MODE_APPEND);
			f.write(msg.getBytes());
			f.close();
		}
		catch (IOException e)
		{
			e(e);
		}
	}
	
	public static void clear()
	{
		try
		{
			Thoth.cx.deleteFile(TAG + ".log");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void logExceptionToFile(Throwable tr)
	{
		try
		{
			StringWriter sw = new StringWriter();
			sw.append("\n");
			PrintWriter pw = new PrintWriter(sw);
			tr.printStackTrace(pw);
			FileOutputStream f = Thoth.cx.openFileOutput(TAG + ".log", Context.MODE_APPEND);
			f.write((new DateTime()).toString(DateTimeFormat.longDateTime()).getBytes());
			//
			f.write(sw.toString().getBytes());
			sw.close();
			pw.close();
			f.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void backupSend2(String sendStr, String sendCmd)
	{
		try
		{
			File sdCard = Environment.getExternalStorageDirectory();
			File thothDir = new File(sdCard.getAbsoluteFile(), "/Thoth");
			if (!thothDir.exists())
				thothDir.mkdir();
			File logFile = new File(thothDir, sendCmd + ".txt");
			if (!logFile.exists())
				logFile.createNewFile();
			FileOutputStream f = new FileOutputStream(logFile);
			f.write(sendStr.getBytes());
			f.close();
		}
		catch (IOException e)
		{
			e(e);
		}
	}
	
	public static void backupDb()
	{
		File sd = Environment.getExternalStorageDirectory();
		File data = Environment.getDataDirectory();
		if (sd.canWrite())
		{
			String currentDBPath = "/data/com.gnuc.thoth/databases/thoth.db";
			String backupDBDir = "/Thoth";
			String backupDBPath = "/Thoth/thoth.db";
			File currentDB = new File(data, currentDBPath);
			File backupDB = new File(sd, backupDBPath);
			File backupDBDirectory = new File(sd, backupDBDir);
			if (!backupDBDirectory.exists())
				backupDBDirectory.mkdirs();
			if (backupDB.exists())
				backupDB.delete();
			if (currentDB.exists())
			{
				FileInputStream from = null;
				FileOutputStream to = null;
				try
				{
					from = new FileInputStream(currentDB);
					to = new FileOutputStream(backupDB);
					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = from.read(buffer)) != -1)
						to.write(buffer, 0, bytesRead); // write
				}
				catch (IOException e)
				{
					e(e);
				}
				finally
				{
					if (from != null)
						try
						{
							from.close();
						}
						catch (IOException e)
						{
							;
						}
					if (to != null)
						try
						{
							to.close();
						}
						catch (IOException e)
						{
							;
						}
				}
			}
			d("Done DB copy.");
			try
			{
				FileOutputStream to = null;
				FileInputStream from = null;
				String backupLogPath = "/Thoth/thoth.log";
				File backupLog = new File(sd, backupLogPath);
				to = new FileOutputStream(backupLog);
				from = Thoth.cx.openFileInput("Thoth-Log.log");
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = from.read(buffer)) != -1)
					to.write(buffer, 0, bytesRead); // write
				from.close();
				to.close();
			}
			catch (Exception e)
			{
				ThothLog.e(e);
			}
			d("Done Log copy.");
		}
	}
}
