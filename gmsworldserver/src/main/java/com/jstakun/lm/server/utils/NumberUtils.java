/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class NumberUtils {

    public static int getVersion(String versionString, int defaultValue) {
        int version = defaultValue;
        if (!StringUtils.isEmpty(versionString) && StringUtils.isNumeric(versionString)) {
            try {
                version = Integer.parseInt(versionString);
            } finally {
            }
        }
        return version;
    }

    public static int getRadius(String radiusString, int minValue, int maxValue) {
        int radius = minValue;

        if (!StringUtils.isEmpty(radiusString) && StringUtils.isNumeric(radiusString)) {
            try {
                radius = Integer.parseInt(radiusString);
            } finally {
            }
        }
        if (radius < minValue) {
            radius = minValue;
        } else if (radius > maxValue) {
            radius = maxValue;
        }

        return radius;
    }

    public static int normalizeNumber(int value, int minValue, int maxValue) {
        int res = value;
        
        if (value > maxValue) {
            res = maxValue;
        } else if (value < minValue) {
            res = minValue;
        }

        return res;
    }

    public static double getDouble(String doubleString, double defaultValue) {
        double result = defaultValue;
        if (!StringUtils.isEmpty(doubleString)) {
            try {
                result = Double.parseDouble(doubleString);
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static int getInt(String intString, int defaultValue) {
        int result = defaultValue;
        if (!StringUtils.isEmpty(intString)) {
            try {
                result = Integer.parseInt(intString);
            } catch (Exception e) {
            }
        }
        return result;
    }
    
    public static long getLong(String longString, long defaultValue) {
        long result = defaultValue;
        if (!StringUtils.isEmpty(longString)) {
            try {
                result = Long.parseLong(longString);
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static double distanceInKilometer(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double lat1rad = Math.toRadians(lat1);
        double lat2rad = Math.toRadians(lat2);
        double dist = Math.sin(lat1rad) * Math.sin(lat2rad) + Math.cos(lat1rad) * Math.cos(lat2rad) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist =  Math.toDegrees(dist);
        dist *= (69.09 * 1.609344); //kilometer

        return dist;
    }
}
