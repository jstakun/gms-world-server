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

    private static final String dayMonthYear = "dd-MM-yyyy";
    private static final String monthYearLong = "MMMMMM yyyy";
    private static final String monthYearShort = "MM-yyyy";
    private static final String monthShort = "MMM";
    
    private static final String rhcloudDatetimeFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String rhcloudTimeZone = "GMT-4:00";
    private static final DateConverter dtConverter = new DateConverter();
    
    static {
    	dtConverter.setPattern(rhcloudDatetimeFormat);
		dtConverter.setTimeZone(TimeZone.getTimeZone(rhcloudTimeZone));
    }
    
    
    public static String getMonthString(Date date) {
        return formatDate(monthShort, date);
    }

    public static String getShortMonthYearString(int interval) {
        return getMonthYearString(interval, monthYearShort);
    }

    public static String getLongMonthYearString(int interval) {
        return getMonthYearString(interval, monthYearLong);
    }

    public static String getLongMonthYearString(Date date) {
        return formatDate(monthYearLong, date);
    }

    private static String getMonthYearString(int interval, String format) {
        Calendar calendar = Calendar.getInstance();

        if (interval > 0) {
            calendar.add(Calendar.MONTH, -interval);
        }

        return formatDate(format, calendar.getTime());
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
        return parseDate(dayMonthYear, "01-" + month);
    }

    public static Date getDay(String day) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseDate(dayMonthYear, day));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static String getDay(Date day) throws ParseException {
        return formatDate(dayMonthYear, day);
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

    public static String getFormattedGMTDateTime(Locale currentLocale, Date date) {
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, currentLocale);
        //formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (formatter != null && date != null) {
        	return formatter.format(date);
        } else {
            return "unknown";	
        }       
    }
    
    public static DateConverter getRHCloudDateConverter() {
    	return dtConverter; 
    }
    
    public static Date afterOneHundredYearsFromNow() {
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.YEAR, 100);
    	return cal.getTime();
    }
    
    public static String formatDate(String format, Date date) {
    	return getSimpleDateFormat(format).format(date);
    }
    
    public static Date parseDate(String format, String date) throws ParseException {
    	return getSimpleDateFormat(format).parse(date);
    }
    
    public static SimpleDateFormat getSimpleDateFormat(String format) {
    	return new SimpleDateFormat(format);
    }
}
