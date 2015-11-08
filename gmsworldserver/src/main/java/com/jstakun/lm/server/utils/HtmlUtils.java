package com.jstakun.lm.server.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.ocpsoft.prettytime.PrettyTime;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils;
import com.openlapi.QualifiedCoordinates;

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
        "| Created in layer <a href=\"" + layerUrl + "\">" + LayerPersistenceUtils.getLayerFormattedName(landmark.getLayer()) + "</a> using <a href=\"" + ConfigurationManager.getAppUrl(landmark.getAppId()) + "\" target=\"_blank\">" +  ConfigurationManager.getAppName(landmark.getAppId()) + "</a>";
        String bookingUrl = "/bookingProvider/" + landmark.getId();
        if (StringUtils.isNotEmpty(landmark.getCity()) && StringUtils.isNotEmpty(landmark.getCountryCode())) {
        	bookingUrl += "/" + landmark.getCountryCode().toLowerCase(Locale.US) + "/" + landmark.getCity();
        }
		desc += "<br/><b><a href=\"" + bookingUrl + "\" target=\"_blank\">Book hotel room nearby!</a></b>" 
			 + HtmlUtils.getStatusImage(landmark.getUseCount());
		
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
	
	private static final Map<String, ExtendedLandmark> default_locations = new HashMap<String, ExtendedLandmark>();
    
	static {
		//protected ExtendedLandmark(String name, String desc, QualifiedCoordinates qc, String layer, long creationDate)
		default_locations.put("es_US", LandmarkFactory.getLandmark("United States, Los Angeles", "", new QualifiedCoordinates(34.052234, -118.243685, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //United States, Los Angeles 34.052234,-118.243685
		default_locations.put("en_US", LandmarkFactory.getLandmark("United States, New York", "", new QualifiedCoordinates(40.69847, -73.951442, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //United States, New York 40.69847, -73.951442
		default_locations.put("fr", LandmarkFactory.getLandmark("France, Paris", "", new QualifiedCoordinates(48.856918, 2.34121, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //France, Paris 48.856918, 2.34121 
		default_locations.put("fr_FR", LandmarkFactory.getLandmark("France, Paris", "", new QualifiedCoordinates(48.856918, 2.34121, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //France, Paris 48.856918, 2.34121
		default_locations.put("de", LandmarkFactory.getLandmark("Germany, Berlin", "", new QualifiedCoordinates(52.516071, 13.37698, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //Germany, Berlin 52.516071, 13.37698 
		default_locations.put("de_DE", LandmarkFactory.getLandmark("Germany, Berlin", "", new QualifiedCoordinates(52.516071, 13.37698, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //Germany, Berlin 52.516071, 13.37698 
		default_locations.put("en_GB", LandmarkFactory.getLandmark("United Kingdom, London", "", new QualifiedCoordinates(51.506321, -0.12714, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //United Kingdom, London, 51.506321,-0.12714  
		default_locations.put("it", LandmarkFactory.getLandmark("Italy, Rome", "", new QualifiedCoordinates(41.901514, 12.460774, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //Italy, Rome 41.901514, 12.460774
		default_locations.put("es", LandmarkFactory.getLandmark("Spain, Madrid", "", new QualifiedCoordinates(40.4203, -3.70577, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //Spain, Madrid 40.4203,-3.70577, 
		default_locations.put("es_ES", LandmarkFactory.getLandmark("Spain, Madrid", "", new QualifiedCoordinates(40.4203, -3.70577, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //Spain, Madrid 40.4203,-3.70577, 
		default_locations.put("ja", LandmarkFactory.getLandmark("Japan, Tokyo", "", new QualifiedCoordinates(35.689488, 139.691706, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //Japan, Tokyo, 35.689488,139.691706 
		default_locations.put("hi", LandmarkFactory.getLandmark("India, Mumbai", "", new QualifiedCoordinates(19.076191, 72.875877, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //India, Mumbai, 19.076191,72.875877 
		default_locations.put("zh", LandmarkFactory.getLandmark("China, Beijing", "", new QualifiedCoordinates(39.90403, 116.407526, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //China, Beijing 39.90403, 116.407526
		default_locations.put("pl", LandmarkFactory.getLandmark("Poland, Warsaw", "", new QualifiedCoordinates(52.235352, 21.00939, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //Poland, Warsaw, 52.235352,21.00939
		default_locations.put("en_CA", LandmarkFactory.getLandmark("Canada, Toronto", "", new QualifiedCoordinates(43.64856, -79.38533, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //Canada, Toronto, 43.64856,-79.38533
		default_locations.put("pt_BR", LandmarkFactory.getLandmark("Brazil, Sao Paolo", "", new QualifiedCoordinates(-23.548943, -46.638818, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //Brazil, Sao Paolo -23.548943,-46.638818,     
		default_locations.put("in", LandmarkFactory.getLandmark("Indonesia, Jakarta", "", new QualifiedCoordinates(-6.17144, 106.82782, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //IDN Indonesia, Jakarta -6.17144, 106.82782
		default_locations.put("th", LandmarkFactory.getLandmark("Thailand, Bangkok", "", new QualifiedCoordinates(13.75333, 100.504822, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //THA Thailand, Bangkok 13.75333, 100.504822
		default_locations.put("ru", LandmarkFactory.getLandmark("Russia, Moscow", "", new QualifiedCoordinates(55.755786, 37.617633, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //RUS Russia, Moscow 55.755786, 37.617633
		default_locations.put("es_MX", LandmarkFactory.getLandmark("Mexico, Mexico City", "", new QualifiedCoordinates(19.432608, -99.133208, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //MEX Mexico, Mexico City 19.432608, -99.133208
		default_locations.put("ms", LandmarkFactory.getLandmark("Malaysia, Kuala Lumpu", "", new QualifiedCoordinates(3.15248, 101.71727, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //MYS Malaysia, Kuala Lumpur 3.15248, 101.71727
		default_locations.put("tr", LandmarkFactory.getLandmark("Turkey, Istanbul", "", new QualifiedCoordinates(41.00527, 28.97696, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //TUR Turkey, Istanbul 41.00527, 28.97696
		default_locations.put("en_PH", LandmarkFactory.getLandmark("Philippines, Manilia", "", new QualifiedCoordinates(14.5995124, 120.9842195, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //PHL Philippines, Manilia 14.5995124, 120.9842195
		default_locations.put("nl", LandmarkFactory.getLandmark("Netherlands, Amsterdam", "", new QualifiedCoordinates(52.373119, 4.89319, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //NLD Netherlands, Amsterdam 52.373119, 4.89319
		default_locations.put("ar_SA", LandmarkFactory.getLandmark("Saudi Arabia, Riyadh", "", new QualifiedCoordinates(24.64732, 46.714581, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //SAU Saudi Arabia, Riyadh 24.64732, 46.714581
		default_locations.put("pt_PT", LandmarkFactory.getLandmark("Portugal, Lisbon", "", new QualifiedCoordinates(38.7252993, -9.1500364, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //PRT Portugal, Lisbon 38.7252993, 9.1500364
		default_locations.put("ur", LandmarkFactory.getLandmark("Pakistan, Islamabad", "", new QualifiedCoordinates(33.718151, 73.060547, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //PAK Pakistan, Islamabad 33.718151, 73.060547
		default_locations.put("sv", LandmarkFactory.getLandmark("Sweden, Stockholm", "", new QualifiedCoordinates(59.32893, 18.06491, 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); //SWE Sweden, Stockholm 59.32893, 18.06491 	       	
	} 
    
    public static String getLocaleCoords(Locale locale) {
    	ExtendedLandmark landmark = default_locations.get("en_GB");
    	if (locale != null && default_locations.containsKey(locale.toString())) {
    		landmark = default_locations.get(locale.toString());
    	}
    	return landmark.getQualifiedCoordinates().getLatitude() + "," + landmark.getQualifiedCoordinates().getLongitude();
    }
    
    public static String getTopLocations() {
    	String resp = "";
    	List<String> names = new ArrayList<String>();
    	for (ExtendedLandmark landmark : default_locations.values()) {
    		String name = landmark.getName();
    		if (!names.contains(name)) {
    			resp += "{\"name\": \"" + name + "\", \"lat\": " + landmark.getQualifiedCoordinates().getLatitude() + ", \"lng\": " + landmark.getQualifiedCoordinates().getLongitude() + "},\n";
    		    names.add(name);
    		}
    	}
    	return resp;
    }
    
    public static String getArchivesUrls() {
    	String resp = "";
    	for (int i=0;i<12;i++)
    	{
    		String[] date = DateUtils.getShortMonthYearString(i).split("-");
    	    resp += "<li><a href=\"/archive/" + date[1] + "/" + date[0] + "\">" + DateUtils.getLongMonthYearString(i) + "</a></li>\n";
    	}
    	resp += "<li><a href=\"/archive/2014\">January 2014</a></li>\n";
    	return resp;
    }
}

