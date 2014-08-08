/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gmsworld.server.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.beanutils.converters.DateConverter;

/**
 *
 * @author jstakun
 */
public class DateUtils {

    private static DateFormat dayMonthYear = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private static DateFormat monthYearLong = new SimpleDateFormat("MMMMMM yyyy", Locale.getDefault());
    private static DateFormat monthYearShort = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
    private static DateFormat monthShort = new SimpleDateFormat("MMM", Locale.getDefault());
    
    private static final String rhcloudDatetimeFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String rhcloudTimeZone = "GMT-4:00";
    private static DateConverter dtConverter = null;
    
    
    public static String getMonthString(Date date) {
        return monthShort.format(date);
    }

    public static String getShortMonthYearString(int interval) {
        return getMonthYearString(interval, monthYearShort);
    }

    public static String getLongMonthYearString(int interval) {
        return getMonthYearString(interval, monthYearLong);
    }

    public static String getLongMonthYearString(Date date) {
        return monthYearLong.format(date);
    }

    private static String getMonthYearString(int interval, DateFormat formatter) {
        Calendar calendar = Calendar.getInstance();

        if (interval > 0) {
            calendar.add(Calendar.MONTH, -interval);
        }

        return formatter.format(calendar.getTime());
    }

    public static String getTimeString(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String hour = "" + calendar.get(Calendar.HOUR_OF_DAY);
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        String minute = "" + calendar.get(Calendar.MINUTE);
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        String second = "" + calendar.get(Calendar.SECOND);
        if (second.length() == 1) {
            second = "0" + second;
        }

        int indicator = calendar.get(Calendar.AM_PM);

        String time = hour + ":" + minute + ":" + second + " ";
        if (indicator == Calendar.AM) {
            time += "AM";
        } else {
            time += "PM";
        }

        return time;
    }

    public static String getDayOfMonthString(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String month = "" + calendar.get(Calendar.DAY_OF_MONTH);

        return month;
    }

    public static Date getLastDayOfMonth(String month) throws ParseException {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(getFirstDayOfMonth(month));

        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DATE, -1);

        return calendar.getTime();
    }

    public static Date getFirstDayOfNextMonth(String month) throws ParseException {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(getFirstDayOfMonth(month));

        calendar.add(Calendar.MONTH, 1);

        return calendar.getTime();
    }

    public static Date getFirstDayOfMonth(String month) throws ParseException {
        return dayMonthYear.parse("01-" + month);
    }

    public static Date getDay(String day) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dayMonthYear.parse(day));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static String getDay(Date day) throws ParseException {
        return dayMonthYear.format(day);
    }

    public static Date getNextDay(Date day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getPreviousDay() {
        return getDayInPast(1, true);
    }

    public static Date getDayInPast(int numOfDays, boolean fromZero) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -numOfDays);
        if (fromZero) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        return calendar.getTime();
    }

    public static String getFormattedDateTime(Locale currentLocale, Date date) {
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, currentLocale);
        if (formatter != null && date != null) {
        	return formatter.format(date);
        } else {
            return "unknown";	
        }       
    }
    
    public static DateConverter getRHCloudDateConverter() {
    	if (dtConverter == null) {
    		dtConverter = new DateConverter();
    		dtConverter.setPattern(rhcloudDatetimeFormat);
    		dtConverter.setTimeZone(TimeZone.getTimeZone(rhcloudTimeZone));
    	}
		return dtConverter; 
    }
}
