package net.gmsworld.server.utils;

import java.text.DecimalFormat;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 *
 * @author jstakun
 */
public class StringUtil {

    public static final int SMALL = 128;
    public static final int NORMAL = 256;
    public static final int LARGE = 512;
    public static final int XLARGE = 1024;
    private static final DecimalFormat coordsFormatE6 = new DecimalFormat("##.######");
    private static final DecimalFormat coordsFormatE2 = new DecimalFormat("##.##");
    private static final DecimalFormat coordsFormatE4 = new DecimalFormat("##.####");
    
    public static String getLanguage(String value, String defaultValue, int length) {
        String language = defaultValue;
        if (!StringUtils.isEmpty(value) && value.length() == length) {
            language = value;
        }
        return language;
    }

    public static boolean isSocialUser(String user) {
        final String[] suffixes = new String[]{"@tw", "@ln", "@fb", "@gl", "@fs", "@gw", "@gg"};
        if (StringUtils.endsWithAny(user, suffixes)) {
            if (!user.equals("anonymous@gl")) {
                return true;
            }
        }
        return false;
    }

    public static String getStringParam(String value, String defaultValue) {
        String param = defaultValue;
        if (StringUtils.isNotEmpty(value)) {
            param = value;
        }
        return param;
    }

    public static String capitalize(String src) {
        if (src != null) {
            return WordUtils.capitalize(src.toLowerCase());
        } else {
            return null;
        }
    }

    public static int getStringLengthLimit(String display) {
        int val = NORMAL;
        if (StringUtils.isNotEmpty(display)) {
            if (StringUtils.equalsIgnoreCase(display, "l")) {
                val = LARGE;
            } else if (StringUtils.equalsIgnoreCase(display, "xl")) {
                val = XLARGE;
            } else if (StringUtils.equalsIgnoreCase(display, "s")) {
                val = SMALL;
            }
        }
        return val;
    }

    public static String formatCoordE6(double coord) {
        return coordsFormatE6.format(coord);
    }

    public static String formatCoordE4(double coord) {
        return coordsFormatE4.format(coord);
    }
    
    public static String formatCoordE2(double coord) {
        return coordsFormatE2.format(coord);
    }
        
    public static String formatCoordE0(double coord) {
        return Integer.toString(Double.valueOf(coord).intValue());
    }
    
    public static String getUsername(Object attr, String header) {
    	String username = (String) attr;

        if ((StringUtils.equals(username, Commons.getProperty(Property.APP_USER)) || StringUtils.equals(username, Commons.getProperty(Property.MYPOS_USER)) || StringUtils.isEmpty(username)) && StringUtils.isNotEmpty(header)) {
            username = header;
        }
        
        return username;
    }
    
    public static String getFormattedUsername(String firstname, String lastname, String defaultValue) {
    	String name = ""; 
			if (StringUtils.isNotEmpty(firstname)) {
				name = firstname;
			}
			if (StringUtils.isNotEmpty(lastname)) {
				if (name.length() > 0) {
					name += " ";
				}
				name += lastname;
			}
			if (name.length() == 0) {
				name = defaultValue;
			}
		return name;	
    }
    
    public static boolean isAllLowerCaseAndDigit(String str) {
        if (str == null || StringUtils.isEmpty(str)) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
        	char c = str.charAt(i);
            if (Character.isDigit(c) == false && Character.isLowerCase(c) == false && c != '_' && c != '-') {
                return false;
            }
        }
        return true;
    }
}
