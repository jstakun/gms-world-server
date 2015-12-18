package com.jstakun.lm.server.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.layers.LayerHelper;
import net.gmsworld.server.layers.LayerHelperFactory;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils;
import com.openlapi.QualifiedCoordinates;

public class HtmlUtils {
	
	private static final Logger logger = Logger.getLogger(HtmlUtils.class.getName());
	
	private static Random random = new Random();
	
	private static final Map<String, ExtendedLandmark> default_locations = new HashMap<String, ExtendedLandmark>();
	
	public static String getRandomUrl(ServletContext context) {
		if (default_locations.isEmpty()) {
    		readTopCities(context);
    	}
		
		if (default_locations.isEmpty()) {
			return "/hotels";
		} else {
			int r = random.nextInt(default_locations.size());
			ExtendedLandmark landmark = default_locations.get( default_locations.keySet().toArray()[r]);
			if (landmark == null) {
				landmark = default_locations.get("en_GB");
			}
			return "/hotelLandmark/" + landmark.getQualifiedCoordinates().getLatitude() + "/" + landmark.getQualifiedCoordinates().getLongitude();
		}
	}
    
    public static String getLocaleCoords(Locale locale, ServletContext context) {
    	if (default_locations.isEmpty()) {
    		readTopCities(context);
    	}
    	
    	ExtendedLandmark landmark = default_locations.get("en_GB");
    	if (locale != null && default_locations.containsKey(locale.toString())) {
    		landmark = default_locations.get(locale.toString());
    	} else if (locale != null && default_locations.containsKey(locale.getLanguage())) {
    		landmark = default_locations.get(locale.getLanguage());
    	}
    	
    	if (landmark != null) {
    		return landmark.getQualifiedCoordinates().getLatitude() + "," + landmark.getQualifiedCoordinates().getLongitude();
    	} else {
    		return "51.506321,-0.12714"; //London
    	}
    }
    
    public static String getTopLocations(ServletContext context) {   	
    	if (default_locations.isEmpty()) {
    		readTopCities(context);
    	}
    	
    	JSONArray topLocations = new JSONArray();
    	List<String> names = new ArrayList<String>();
    	for (ExtendedLandmark landmark : default_locations.values()) {
    		String name = landmark.getName();
    		if (!names.contains(name)) {
    			JSONObject city = new JSONObject().put("name", name).put("lat", landmark.getQualifiedCoordinates().getLatitude()).put("lng", landmark.getQualifiedCoordinates().getLongitude());
    			topLocations.put(city);
    			names.add(name);
    		}
    	}
    	return topLocations.toString();
    }
	
	private static void readTopCities(ServletContext context) {
		String csvFile = "/WEB-INF/topcities.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		try {
			br = new BufferedReader(new InputStreamReader(context.getResourceAsStream(csvFile), "UTF-8"));
			while ((line = br.readLine()) != null) {
			    String[] location = line.split(cvsSplitBy);
				//logger.log(Level.INFO, location[0] + ": " + location[1] + " " + location[2] + " -> " + location[3] + "," + location[4]);
				String name = location[1];
				if (!StringUtils.isEmpty(location[2])) {
					name += ", " + location[2];
				}
				default_locations.put(location[0], LandmarkFactory.getLandmark(name, "", new QualifiedCoordinates(Double.valueOf(location[3]), Double.valueOf(location[4]), 0f, Float.NaN, Float.NaN), null, null, System.currentTimeMillis(), null)); 
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

	public static String buildLandmarkDescV2(Landmark landmark, Object address, Locale locale, boolean isMobile) {
	    int fontSize = 16;
	    if (isMobile) {
	    	fontSize = 24;
	    }
		String desc = "'<span style=\"font-family:Roboto,Arial,sans-serif;font-size:" + fontSize + "px;font-style:normal;font-weight:normal;text-decoration:none;text-transform:none;color:000000;background-color:ffffff;\">'+\n" +
	            "'<b>" + StringEscapeUtils.escapeJavaScript(landmark.getName()) + "</b><br/>'+\n" +
		        "'<img src=\"https://maps.googleapis.com/maps/api/streetview?size=200x150&location=" + landmark.getLatitude() + "," + landmark.getLongitude() + "\" style=\"margin: 4px 0px\" title=\"Location street view image\"/><br/>'+\n";
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
		ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource", locale);
		
		LayerHelper layerHelper = LayerHelperFactory.getByName(Commons.HOTELS_LAYER);
		String json = layerHelper.getGeoJson(landmark.getLatitude(), landmark.getLongitude(), Commons.HOTELS_LAYER, locale.getLanguage());	
		String hotelsText = rb.getString("hotels.discover.nearby");
		
		if (StringUtils.startsWith(json, "{")) {
			JSONObject layerJson = new JSONObject(json);
			int layerSize = layerJson.getJSONArray("features").length();
			if (layerSize > 0) {
				hotelsText = String.format(rb.getString("hotels.discover.nearby.2"), layerSize);
			}
		}
		
		String userUrl = null;		
		if (landmark.isSocial()) {
			userUrl = URLEncoder.encode("/blogeo/" + landmark.getUsername(), "UTF-8");
		} else {
			userUrl = URLEncoder.encode("/showUser/" + landmark.getUsername(), "UTF-8");
		}
		String layerUrl = "/showLayer/" + landmark.getLayer();
		String bookingUrl = "/showLandmark/" + landmark.getId() + "?enabled=Hotels&fullScreenLandmarkMap=true";
		
		String desc = "";
		String description = landmark.getDescription();
		if (StringUtils.isNotEmpty(description)) {
			desc = description + "<br/>";
		}
		desc += "Posted " + prettyTime.format(landmark.getCreationDate()) + " on " + DateUtils.getFormattedDateTime(locale, landmark.getCreationDate()) + " by <a href=\"" + userUrl + "\">" + UrlUtils.createUsernameMask(landmark.getUsername()) + "</a>&nbsp;" + 
        "| Created in layer <a href=\"" + layerUrl + "\">" + LayerPersistenceUtils.getLayerFormattedName(landmark.getLayer()) + "</a> using <a href=\"" + ConfigurationManager.getAppUrl(landmark.getAppId()) + "\" target=\"_blank\">" +  ConfigurationManager.getAppName(landmark.getAppId()) + "</a>" +
		"<br/><b><a href=\"" + bookingUrl + "\" target=\"_blank\">" + hotelsText + "</a></b>" + HtmlUtils.getStatusImage(landmark.getUseCount());
		
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

