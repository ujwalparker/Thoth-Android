package com.gnuc.thoth.framework.utils.date;

import java.util.HashMap;

public class ThothDateMatch
{
	public boolean	TODAY			= false; // 1
	public boolean	THIS_WEEK	= false; // 2
	public boolean	LAST_WEEK	= false; // 3
	public boolean	THIS_MONTH	= false; // 4
	public boolean	LAST_MONTH	= false; // 5
	public boolean	THIS_YEAR	= false; // 6
	
	public HashMap<Integer, Boolean> get()
	{
		HashMap<Integer, Boolean> result = new HashMap<Integer, Boolean>();
		result.put(1, TODAY);
		result.put(2, THIS_WEEK);
		result.put(3, LAST_WEEK);
		result.put(4, THIS_MONTH);
		result.put(5, LAST_MONTH);
		result.put(6, THIS_YEAR);
		return result;
	}
}
