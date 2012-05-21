package com.gnuc.thoth.framework.utils.date;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

public class ThothDate
{
	public static final DateTime		now			= new DateTime();
	public static final DateMidnight	todayS		= new DateMidnight();
	public static final DateTime		todayE		= now.millisOfDay().withMaximumValue().minusMillis(1);
	public static final DateMidnight	thisWeekS	= todayS.dayOfWeek().withMinimumValue();
	public static final DateTime		thisWeekE	= todayS.dayOfWeek().withMaximumValue().toDateTimeISO().millisOfDay().withMaximumValue().minusMillis(1);
	public static final DateMidnight	lastWeekS	= todayS.dayOfWeek().withMinimumValue().minusWeeks(1);
	public static final DateTime		lastWeekE	= todayS.dayOfWeek().withMaximumValue().minusWeeks(1).toDateTimeISO().millisOfDay().withMaximumValue().minusMillis(1);		;
	public static final DateMidnight	thisMonthS	= todayS.dayOfMonth().withMinimumValue();
	public static final DateTime		thisMonthE	= todayS.dayOfMonth().withMaximumValue().toDateTimeISO().millisOfDay().withMaximumValue().minusMillis(1);						;
	public static final DateMidnight	lastMonthS	= todayS.dayOfMonth().withMinimumValue().minusMonths(1);
	public static final DateTime		lastMonthE	= todayS.dayOfMonth().withMaximumValue().minusMonths(1).toDateTimeISO().millisOfDay().withMaximumValue().minusMillis(1);	;
	public static final DateMidnight	thisYearS	= todayS.dayOfYear().withMinimumValue();
	public static final DateTime		thisYearE	= todayS.dayOfYear().withMaximumValue().toDateTimeISO().millisOfDay().withMaximumValue().minusMillis(1);							;
	
	// 0 - TODAY, 1 - THIS_WEEK, 2 - LAST_WEEK, 3 - THIS_MONTH, 4 - LAST_MONTH, 5 - THIS_YEAR,
	// 6 -LAST_YEAR
	public static ThothDateMatch filter(DateTime ref)
	{
		ThothDateMatch tdm = new ThothDateMatch();
		if (ref.isAfter(todayS) && ref.isBefore(todayE))
			tdm.TODAY = true;
		if (ref.isAfter(thisWeekS) && ref.isBefore(thisWeekE))
			tdm.THIS_WEEK = true;
		if (ref.isAfter(lastWeekS) && ref.isBefore(lastWeekE))
			tdm.LAST_WEEK = true;
		if (ref.isAfter(thisMonthS) && ref.isBefore(thisMonthE))
			tdm.THIS_MONTH = true;
		if (ref.isAfter(lastMonthS) && ref.isBefore(lastMonthE))
			tdm.LAST_MONTH = true;
		if (ref.isAfter(thisYearS) && ref.isBefore(thisYearE))
			tdm.THIS_YEAR = true;
		return tdm;
	}
}
