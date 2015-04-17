package com.jstakun.lm.server.utils;

import java.util.Locale;

import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils;

public class HtmlUtils {

	public static String buildLandmarkDesc(Landmark landmark, Object address, Locale locale) {
	    String desc = "'<span style=\"font-family:Cursive;font-size:14px;font-style:normal;font-weight:normal;text-decoration:none;text-transform:none;color:000000;background-color:ffffff;\">'+\n" +
	            "'<img src=\"/images/flagblue.png\"/><br/>'+\n" +
	            "'Name: " + StringEscapeUtils.escapeJavaScript(landmark.getName()) + ",<br/>'+\n";
	    String landmarkDesc = landmark.getDescription();
	    if (StringUtils.isNotEmpty(landmarkDesc)) {
	           desc += "'Description: " + StringEscapeUtils.escapeJavaScript(landmarkDesc) + ",<br/>'+\n";
	    }       
	    if (address != null && StringUtils.isNotEmpty(address.toString())) {
	           desc += "'Geocode address: " + StringEscapeUtils.escapeJavaScript(address.toString()) + ",<br/>'+\n"; 
	    }
	    desc += "'Latitude: " + StringUtil.formatCoordE6(landmark.getLatitude()) + ", Longitude: " + StringUtil.formatCoordE6(landmark.getLongitude()) + ",<br/>'+\n" +
	            "'Posted on " + DateUtils.getFormattedDateTime(locale, landmark.getCreationDate()) + " by " + UrlUtils.createUsernameMask(landmark.getUsername()) + ",<br/>'+\n" +
	            "'Created in layer " + LayerPersistenceUtils.getLayerFormattedName(landmark.getLayer()) + ".</span>'";
	    return desc;
	}
	
	public static String getStatusImage(int useCount) {
		if (useCount > 0 && useCount < 10) {
			return "<br/><img src=\"/images/statusicon1.gif\"/>";
		} else if (useCount > 10 && useCount < 50) {
			return "<br/><img src=\"/images/statusicon2.gif\"/>";
		} else if (useCount > 50 && useCount < 100) {
			return "<br/><img src=\"/images/statusicon3.gif\"/>";
		} else if (useCount > 100 && useCount < 500) {
			return "<br/><img src=\"/images/statusicon4.gif\"/>";
		} else if (useCount > 500) {
			return "<br/><img src=\"/images/statusicon5.gif\"/>";
		} else {
			return "<br/><img src=\"/images/statusicon0.gif\"/>";
		}
	}

}
