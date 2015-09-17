package com.jstakun.lm.server.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.ocpsoft.prettytime.PrettyTime;

import com.jstakun.lm.server.config.ConfigurationManager;
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
	
	private static String getStatusImage(int useCount) {
		String htmlStr = "<br/>";
		int icon = 0;
		String title = null;
		if (useCount == 1) {
			title = useCount + " discovery";
		} else {
			title = useCount + " discoveries";
		}
		if (useCount > 0 && useCount < 10) {
			icon = 1;
		} else if (useCount > 10 && useCount < 50) {
			icon = 2;
		} else if (useCount > 50 && useCount < 100) {
			icon = 3;
		} else if (useCount > 100 && useCount < 500) {
			icon = 4;
		} else if (useCount > 500) {
			icon = 5;
		} 
		htmlStr += "<img src=\"/images/statusicon" + icon + ".gif\" title=\"" + title + "\"/>";
		return htmlStr;
	}
	
	public static String getLandmarkDesc(Landmark landmark, Locale locale) throws UnsupportedEncodingException {
		PrettyTime prettyTime = new PrettyTime(locale);
		String userUrl = null;		
		if (landmark.isSocial()) {
			userUrl = URLEncoder.encode("/blogeo/" + landmark.getUsername(), "UTF-8");
		} else {
			userUrl = URLEncoder.encode("/showUser/" + landmark.getUsername(), "UTF-8");
		}
		String layerUrl = "/showLayer/" + landmark.getLayer();
		
		String desc = "";
		String description = landmark.getDescription();
		if (StringUtils.isNotEmpty(description)) {
			desc = description + "<br/>";
		}
		desc += "Posted " + prettyTime.format(landmark.getCreationDate()) + " on " + DateUtils.getFormattedDateTime(locale, landmark.getCreationDate()) + " by <a href=\"" + userUrl + "\">" + UrlUtils.createUsernameMask(landmark.getUsername()) + "</a>&nbsp;" + 
        "| Created in layer <a href=\"" + layerUrl + "\">" + LayerPersistenceUtils.getLayerFormattedName(landmark.getLayer()) + "</a> using <a href=\"" + ConfigurationManager.LM_GOOGLE_PLAY_URL + "\" target=\"_blank\">" +  ConfigurationManager.getAppName(landmark.getAppId()) + "</a>";
        if (StringUtils.isNotEmpty(landmark.getCountryCode()) && StringUtils.isNotEmpty(landmark.getCity())) {
            String bookingUrl = String.format(ConfigurationManager.BOOKING_URL, landmark.getCountryCode().toLowerCase(Locale.US), landmark.getCity().toLowerCase(Locale.US));
        	desc += "<br/><b><a href=\"" + bookingUrl + "\">Book hotel room nearby!</a></b>";	
        }
        desc += HtmlUtils.getStatusImage(landmark.getUseCount());
		
		return desc;
	}
	
	public static String getLandmarkDescShort(Landmark landmark, Locale locale) throws UnsupportedEncodingException {
		PrettyTime prettyTime = new PrettyTime(locale);
		String layerUrl = "/showLayer/" + landmark.getLayer();
		String userUrl = null;
		if (landmark.isSocial()) {
			userUrl = URLEncoder.encode("/blogeo/" + landmark.getUsername(), "UTF-8");
		} else {
			userUrl = URLEncoder.encode("/showUser/" + landmark.getUsername(), "UTF-8");
		}
		
		String desc = "Created in layer <a href=\"" + layerUrl + "\">" + LayerPersistenceUtils.getLayerFormattedName(landmark.getLayer()) + "</a>" +
        "<div class=\"date\"><span>Posted " + prettyTime.format(landmark.getCreationDate()) + " on " + DateUtils.getFormattedDateTime(locale, landmark.getCreationDate()) + " by <a href=\"" + userUrl + "\">" + UrlUtils.createUsernameMask(landmark.getUsername()) + "</a></span></div>";
		
		return desc;
	}

}
