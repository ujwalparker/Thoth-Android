package com.gnuc.thoth.framework.network;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class NetworkStatistics
{
	private static final String	SYSCLASSNET	= "/sys/class/net/";  
	private static final String	CARRIER		= "/carrier";
	private static final String	RX_BYTES		= "/statistics/rx_bytes";
	private static final String	TX_BYTES		= "/statistics/tx_bytes";
	private static final String	PROUIDSTAT	= "/proc/uid_stat/";
	
	public static boolean isUp(String inter)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(SYSCLASSNET).append(inter).append(CARRIER);
		return new File(sb.toString()).canRead();
	}
	
	private static long getSizeVal(String prefix, String[] nodes, String suffix)
	{
		StringBuilder sb = new StringBuilder();
		RandomAccessFile raf = null;
		long foundSize = 0;
		for (String node : nodes)
		{
			if (!node.equals(""))
			{
				sb.append(prefix).append(node).append(suffix);
				try
				{
					raf = new RandomAccessFile(new File(sb.toString()), "r");
					foundSize = Long.valueOf(raf.readLine());
				}
				catch (Exception e)
				{}
				finally
				{
					if (raf != null)
					{
						try
						{
							raf.close();
						}
						catch (IOException e)
						{}
					}
					if (foundSize != 0)
						return foundSize;
				}
			}
		}
		return foundSize;
	}
	
	private static long getSizeValAll(String path)
	{
		long foundSize = 0;
		File dirSYSCLASSNET = new File(SYSCLASSNET);
		if (dirSYSCLASSNET != null && dirSYSCLASSNET.isDirectory())
		{
			File[] adapterList = dirSYSCLASSNET.listFiles();
			for (File adapter : adapterList)
			{
				if (adapter.isDirectory() && !adapter.getName().equals(".") && !adapter.getName().equals("lo"))
					foundSize += getSizeVal(SYSCLASSNET, new String[]{adapter.getName()}, path);
			}
		}
		return foundSize;
	}
	
	/**
	 * @return Returns the total bytes downloaded.
	 */
	public static long getTRBytes()
	{
		return getSizeValAll(RX_BYTES);
	}
	
	/**
	 * @return Returns the total bytes uploaded.
	 */
	public static long getTTBytes()
	{
		return getSizeValAll(TX_BYTES);
	}
	
	/**
	 * @return Returns the total bytes downloaded through WIFI internet link.
	 */
	public static long getWRBytes()
	{
		String[] interList = new String[]{"wlan0","eth0", "tiwlan0", "athwlan0", "eth1"};
		return getSizeVal(SYSCLASSNET, interList, RX_BYTES);
	}
	
	/**
	 * @return Returns the total bytes uploaded through WIFI internet link.
	 */
	public static long getWTBytes()
	{
		String[] interList = new String[]{"wlan0","eth0", "tiwlan0", "athwlan0", "eth1"};
		return getSizeVal(SYSCLASSNET, interList, TX_BYTES);
	}
	
	/**
	 * @return Returns the total bytes downloaded through MOBILE internet link.
	 */
	public static long getMRBytes()
	{
		String[] interList = new String[]{"rmnet0", "ppp0"};
		return getSizeVal(SYSCLASSNET, interList, RX_BYTES);
	}
	
	/**
	 * @return Returns the total bytes uploaded through MOBILE internet link.
	 */
	public static long getMTBytes()
	{
		String[] interList = new String[]{"rmnet0", "ppp0"};
		return getSizeVal(SYSCLASSNET, interList, TX_BYTES);
	}
	
	/**
	 * @return Returns the total bytes downloaded by the application defined by UID.
	 */
	public static long getARBytes(int UID)
	{
		String[] interList = new String[]{String.valueOf(UID)};
		return getSizeVal(PROUIDSTAT, interList, "/tcp_rcv");
	}
	
	/**
	 * @return Returns the total bytes uploaded by the application defined by UID.
	 */
	public static long getATBytes(int UID)
	{
		String[] interList = new String[]{String.valueOf(UID)};
		return getSizeVal(PROUIDSTAT, interList, "/tcp_snd");
	}
}
