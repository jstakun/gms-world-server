package net.gmsworld.server.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.layers.LayerHelperFactory;
import net.gmsworld.server.utils.memcache.CacheProvider;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;
import org.w3c.tidy.Tidy;

import com.jstakun.gms.android.deals.Deal;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.openlapi.AddressInfo;

/**
 *
 * @author jstakun
 */
public class JSONUtils {

    private static final Logger logger = Logger.getLogger(JSONUtils.class.getName());
    private static final Tidy tidy = new Tidy();
    //private static final Object exchangeRetesSemafor = new Object();

    static {
        tidy.setPrintBodyOnly(true);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        tidy.setInputEncoding("UTF-8");
        tidy.setOutputEncoding("UTF-8");
    };

    public static void putOptDate(Map<String, String> array, String name, String date, SimpleDateFormat formatter) {
        if (StringUtils.isNotEmpty(date)) {
            try {
                Date d = formatter.parse(date);
                String val = Long.toString(d.getTime());
                array.put(name, val);
            } catch (Exception ex) {
                logger.log(Level.WARNING, null, ex);
            }
        }
    }

    public static void putOptDate(Map<String, String> array, String name, JSONObject json, String property, SimpleDateFormat formatter) throws JSONException {
        try {
            if (json.has(property) && !json.isNull(property)) {
                String value = json.getString(property);
                putOptDate(array, name, value, formatter);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, null, ex);
        }
    }

    public static void putOptValue(Map array, String name, String value, int stringLimit, boolean tidyHtml) throws JSONException {
        if (StringUtils.isNotEmpty(value)) {
            String val = value;
            if (stringLimit > 0 && value.length() > stringLimit) {
                int endOfWord = StringUtils.indexOf(value, ' ', stringLimit);
                if (endOfWord > 0) {
                    val = StringUtils.abbreviate(value, Math.max(endOfWord + 3, stringLimit));
                    if (tidyHtml) {
                        try {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            tidy.parse(new ByteArrayInputStream(val.getBytes("UTF-8")), bos);
                            val = bos.toString("UTF-8");
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            array.put(name, val);
        }
    }

    public static void putOptValue(Map<String, ?> array, String name, JSONObject json, String property, boolean unescapehtml, int stringLimit, boolean tidyHtml) {
        try {
            if (json.has(property) && !json.isNull(property)) {
                String value = json.optString(property);
                if (unescapehtml) {
                    value = StringEscapeUtils.unescapeHtml(value);
                    value = StringUtils.replace(value, "&apos;", "'");
                }
                putOptValue(array, name, value, stringLimit, tidyHtml);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, null, ex);
        }
    }

    public static String getJsonArrayObject(List<Map<String, Object>> jsonArray) throws JSONException {
        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json.toString();
    }

    public static JSONArray getJSonArray(String json, String arrayName) throws JSONException {
        if (StringUtils.startsWith(json, "{")) {
            JSONObject jsonRoot = new JSONObject(json);
            if (jsonRoot.has(arrayName)) {
                JSONArray result = jsonRoot.getJSONArray(arrayName);
                return result;
            }
        }

        return null;
    }

    public static int addJSONObjectToResultMap(Map<String, JSONObject> jsonMap, String label, JSONObject json, boolean addIfEmpty) {

        int size = 0;

        if (json != null) {
            JSONArray resultSet = json.optJSONArray("ResultSet");

            if (resultSet != null) {
                size = resultSet.length();
                if (size > 0 || addIfEmpty) {
                    jsonMap.put(label, json);
                }
            }
        }

        return size;
    }
    
    private static String getStars(int num) {
        String stars = "";
        
        for (int i = 0; i < num; i++) {
            //stars += "*";
            stars += "<img src=\"star_blue\" alt=\"*\"/> ";
        }
        return stars;
    }
    
    private static String getLink(String url, String name) {
        if (StringUtils.startsWith(url, "http")) {
            return "<a href=\"" + url + "\">" + name + "</a>";
        } else {
            return "<a href=\"http://" + url + "\">" + name + "</a>";
        }
    }
    
    private static void formatCurrency(Deal deal, String language, String country, String layer) {
    	
    	String fromcc = deal.getCurrencyCode();
    	String tocc = null;
    	if (StringUtils.isEmpty(country)) {
			country = language;
		}
		
    	if (StringUtils.equals(language, "en") && StringUtils.equals(country, "en")) {
    		country = "US";
    	}
    	
    	try {
    		Locale locale = new Locale(language, country);
    		Currency currency = Currency.getInstance(locale);
    		tocc = currency.getCurrencyCode();
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, "Error for: " + country + "," + language, e);
    	}
    	
    	if (tocc != null && fromcc != null && !StringUtils.equals(tocc, fromcc) && fromcc.length() == 3) {  		
    		Double toccrate = getExchangeRate(fromcc, tocc, layer);
			if (toccrate != null) {
				deal.setCurrencyCode(tocc);
				deal.setPrice(deal.getPrice() * toccrate);
			}
    	}  	
    }
    
    private static Double getExchangeRate(String fromcc, String tocc, String layer) {
    	CacheProvider cacheProvider = LayerHelperFactory.getInstance().getByName(layer).getCacheProvider();
		Map<String, Double> ratesMap = cacheProvider.getObject(HashMap.class, "CURRENCY_BASE_" + fromcc);
		if (ratesMap == null) {
			return null;
		} else {
			return ratesMap.get(tocc);
		}
    }
    
    public static String formatDeal(Deal deal, Locale locale, ResourceBundle rb) {
        String result = "";
        String currencyCode = deal.getCurrencyCode();
        NumberFormat pf = NumberFormat.getPercentInstance(locale);
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        nf.setMaximumFractionDigits(2);
        
        if (deal.getPrice() > 0 || (deal.getPrice() == 0 && deal.getDiscount() > 0)) {
            String dealFormatted = nf.format(deal.getPrice());
            if (currencyCode != null && (currencyCode.equals("$") || currencyCode.equals("USD"))) {
                dealFormatted = "<font color=\"green\">$" + dealFormatted + "</font>";
            } else if (currencyCode != null && currencyCode.equals("C$")) {
                dealFormatted = "<font color=\"green\">C$" + dealFormatted + "</font>";
            } else {
                dealFormatted = "<font color=\"green\">" + dealFormatted + " " + deal.getCurrencyCode() + "</font>";
            }
            result = String.format(rb.getString("Landmark.price"), dealFormatted);
        }
        
        if (deal.getDiscount() > 0) {
            String saveFormatted = nf.format(deal.getSave());
            if (currencyCode.equals("$") || currencyCode.equals("USD")) {
                saveFormatted = "<font color=\"red\">$" + saveFormatted + "</font>";
            } else if (currencyCode.equals("C$")) {
                saveFormatted = "<font color=\"red\">C$" + saveFormatted + "</font>";
            } else {
                saveFormatted = "<font color=\"red\">" + saveFormatted + " " + deal.getCurrencyCode() + "</font>";
            }
            
            String discountFormatted = "<font color=\"red\">" + pf.format(deal.getDiscount()) + "</font>";
            
            result += String.format(rb.getString("Landmark.discount"), discountFormatted, saveFormatted);
        }
        
        if (deal.isIsDealOfTheDay()) {
            if (result.length() > 0) {
                result += "<br/>";
            }
            result += rb.getString("Landmark.dealOfTheDay");
        } else if (StringUtils.isNotEmpty(deal.getDealType())) {
            if (result.length() > 0) {
                result += "<br/>";
            }
            result += String.format(rb.getString("Landmark.dealType"), deal.getDealType());
        }
        
        return result;
    }
    
    private static String formatOtherAddress(AddressInfo address) {
    	//Other
        //STREET
        //POSTAL_CODE CITY
        //STATE
        //COUNTRY
    	String result = "";
        
        if (StringUtils.isNotEmpty(address.getField(AddressInfo.STREET))) {
        	result += address.getField(AddressInfo.STREET);
        }
        
        String line = "";
        if (StringUtils.isNotEmpty(address.getField(AddressInfo.POSTAL_CODE))) {
        	line += address.getField(AddressInfo.POSTAL_CODE);
        }
        if (StringUtils.isNotEmpty(address.getField(AddressInfo.CITY))) {
            if (line.length() > 0) {
                line += " ";
            }
            line += address.getField(AddressInfo.CITY);
        }
        if (line.length() > 0) {
        	if (result.length() > 0) {
            	result += ", ";
            }
    		result += line;
        }
        
        if (StringUtils.isNotEmpty(address.getField(AddressInfo.STATE))) {
        	if (result.length() > 0) {
            	result += ", ";
            }
    		result += address.getField(AddressInfo.STATE);
        }
        
        if (StringUtils.isNotEmpty(address.getField(AddressInfo.COUNTRY))) {
        	if (result.length() > 0) {
            	result += ", ";
            }
    		result += address.getField(AddressInfo.COUNTRY);
        }
        return result;
    }
    
    private static String formatUSAddress(AddressInfo address) {
    	//US
        //STREET
        //CITY, STATE POSTAL_CODE
        //COUNTRY
    	String result = "";
        if (StringUtils.isNotEmpty(address.getField(AddressInfo.STREET))) {
            result += address.getField(AddressInfo.STREET);
        }
    	
        String line = "";
    	if (StringUtils.isNotEmpty(address.getField(AddressInfo.CITY))) {
            line += address.getField(AddressInfo.CITY);
        }
    	if (StringUtils.isNotEmpty(address.getField(AddressInfo.STATE))) {
            line += ", " + address.getField(AddressInfo.STATE);
            if (StringUtils.isNotEmpty(address.getField(AddressInfo.POSTAL_CODE))) {
                line += " " + address.getField(AddressInfo.POSTAL_CODE);
            }
    	}
    	if (line.length() > 0) {
            if (result.length() > 0) {
            	result += ", ";
            }
    		result += line;
        }
    	
    	if (StringUtils.isNotEmpty(address.getField(AddressInfo.COUNTRY))) {
    		if (result.length() > 0) {
            	result += ", ";
            }
    		result += address.getField(AddressInfo.COUNTRY);
        }
    	
    	return result;
    }
    
    public static String formatAddress(AddressInfo address) {
        if (StringUtils.equalsIgnoreCase(address.getField(AddressInfo.COUNTRY),"US")) {
        	return formatUSAddress(address);
        } else {
        	return formatOtherAddress(address);
        }       
    }
    
    private static void putOptValue(List<String> vector, String resource, String property, Map<String, String> tokens, java.util.Locale l, ResourceBundle rb) {
        String value = tokens.remove(property);
        if (StringUtils.isNotEmpty(value)) {
            value = String.format(rb.getString(resource), value);
            vector.add(StringUtils.trimToEmpty(value));
        }
    }
    
    public static String buildLandmarkDesc(ExtendedLandmark landmark, Map<String, String> tokens, Locale locale) {
        List<String> result = new ArrayList<String>();
        List<String> otherNamed = new ArrayList<String>();
        String start_date = null;
        ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource", locale);

        //long start = System.currentTimeMillis();

        putOptValue(result, "Landmark.category", "category", tokens, locale, rb);
        putOptValue(result, "Landmark.merchant", "merchant", tokens, locale, rb);
        putOptValue(result, "Landmark.artist", "artist", tokens, locale, rb);
        putOptValue(result, "Landmark.venue", "venue", tokens, locale, rb);
        putOptValue(result, "Landmark.description", "description", tokens, locale, rb);
        
        String val = tokens.remove("star_rating");
        if (val != null) {
            int star_rating = (int) NumberUtils.getDouble(val, 0.0);
            if (star_rating > 0) {
                result.add(getStars(star_rating));
            }
        }
        
        //System.out.println("P: " + deal.getPrice() + ", D: " + deal.getDiscount() + ", S: " + deal.getSave());
        if (landmark.containsDeal()) {
        	formatCurrency(landmark.getDeal(), locale.getLanguage(), locale.getCountry(),  landmark.getLayer());
            String priceFormatted = formatDeal(landmark.getDeal(), locale, rb);
            if (StringUtils.isNotEmpty(priceFormatted)) {
                result.add(priceFormatted);
            }
        }
        
        AddressInfo address = landmark.getAddressInfo();
        String locality = formatAddress(address);
        if (locality.isEmpty()) {
        	//Google places provides formatted address
        	locality = tokens.remove("address");
        }
        
        if (StringUtils.isNotEmpty(locality)) {
            result.add(String.format(rb.getString("Landmark.address"), locality));
        }  
        
        if (StringUtils.isNotEmpty(address.getField(AddressInfo.PHONE_NUMBER))) {
            otherNamed.add(String.format(rb.getString("Landmark.phone"), address.getField(AddressInfo.PHONE_NUMBER)));
        }

        //dates

        Calendar cal = Calendar.getInstance();
        PrettyTime prettyTime = new PrettyTime(locale);
        
        //val = tokens.remove("upload_date");
        //if (val != null) {
        //    otherNamed.add(String.format(rb.getString("Landmark.upload_date"), val));
        //}
        
        val = tokens.remove("start_date");
        if (StringUtils.isNumeric(val) && val.length() > 0) {
        	cal.setTimeInMillis(Long.valueOf(val).longValue());
        	start_date = String.format(rb.getString("Landmark.start_date"), prettyTime.format(cal));
        }
        
        String layer = landmark.getLayer();
        
        if (!StringUtils.equals(layer, Commons.LOCAL_LAYER) && landmark.getCreationDate() > 0 && landmark.getCreationDate() < System.currentTimeMillis()) {
        	cal.setTimeInMillis(landmark.getCreationDate());
        	otherNamed.add(String.format(rb.getString("Landmark.creation_date"), prettyTime.format(cal)));
        }
        
        val = tokens.remove("taken_date");
        if (StringUtils.isNumeric(val) && val.length() > 0) {
        	cal.setTimeInMillis(Long.valueOf(val).longValue());
            otherNamed.add(String.format(rb.getString("Landmark.taken_date"), prettyTime.format(cal)));
        }
        
        val = tokens.remove("end_date");
        if (StringUtils.isNumeric(val) && val.length() > 0) {
        	cal.setTimeInMillis(Long.valueOf(val).longValue());
            otherNamed.add(String.format(rb.getString("Landmark.end_date"), prettyTime.format(cal)));
        }
        
        val = tokens.remove("expiration_date");
        if (StringUtils.isNumeric(val) && val.length() > 0) {
        	cal.setTimeInMillis(Long.valueOf(val).longValue());
        	otherNamed.add(String.format(rb.getString("Landmark.expiration_date"), prettyTime.format(cal)));
        }

        //rating

        String ratingStr = "";
        int maxRating = NumberUtils.getInt(tokens.remove("maxRating"), 5);
        double r = landmark.getRating();
        if (r >= 0) {
        	if (layer.equals(Commons.YELP_LAYER) || layer.equals(Commons.COUPONS_LAYER)) {
                if (r >= maxRating * 0.9) {
                    ratingStr = "<img src=\"stars_5\" alt=\"*****\"/>";
                } else if (r >= maxRating * 0.8 && r < maxRating * 0.9) {
                    ratingStr = "<img src=\"stars_4_half\" alt=\"****-\"/>";
                } else if (r >= maxRating * 0.7 && r < maxRating * 0.8) {
                    ratingStr = "<img src=\"stars_4\" alt=\"****\"/>";
                } else if (r >= maxRating * 0.6 && r < maxRating * 0.7) {
                    ratingStr = "<img src=\"stars_3_half\" alt=\"***-\"/>";
                } else if (r >= maxRating * 0.5 && r < maxRating * 0.6) {
                    ratingStr = "<img src=\"stars_3\" alt=\"***\"/>";
                } else if (r >= maxRating * 0.4 && r < maxRating * 0.5) {
                    ratingStr = "<img src=\"stars_2_half\" alt=\"**-\"/>";
                } else if (r >= maxRating * 0.3 && r < maxRating * 0.4) {
                    ratingStr = "<img src=\"stars_2\" alt=\"**\"/>";
                } else if (r >= maxRating * 0.2 && r < maxRating * 0.3) {
                    ratingStr = "<img src=\"stars_1_half\" alt=\"*-\"/>";
                } else if (r >= maxRating * 0.1 && r < maxRating * 0.2) {
                    ratingStr = "<img src=\"stars_1\" alt=\"*\"/>";
                } else {
                    ratingStr = "<img src=\"stars_0\" alt=\"_\"/>";
                }
            } else {
                if (r >= maxRating * 0.9) {
                    ratingStr = "<img src=\"star_5\" alt=\"*****\"/>";
                } else if (r >= maxRating * 0.8 && r < maxRating * 0.9) {
                    ratingStr = "<img src=\"star_5\" alt=\"*****\"/>";
                } else if (r >= maxRating * 0.7 && r < maxRating * 0.8) {
                    ratingStr = "<img src=\"star_4\" alt=\"****\"/>";
                } else if (r >= maxRating * 0.6 && r < maxRating * 0.7) {
                    ratingStr = "<img src=\"star_4\" alt=\"****\"/>";
                } else if (r >= maxRating * 0.5 && r < maxRating * 0.6) {
                    ratingStr = "<img src=\"star_3\" alt=\"***\"/>";
                } else if (r >= maxRating * 0.4 && r < maxRating * 0.5) {
                    ratingStr = "<img src=\"star_3\" alt=\"***\"/>";
                } else if (r >= maxRating * 0.3 && r < maxRating * 0.4) {
                    ratingStr = "<img src=\"star_2\" alt=\"**\"/>";
                } else if (r >= maxRating * 0.2 && r < maxRating * 0.3) {
                    ratingStr = "<img src=\"star_2\" alt=\"**\"/>";
                } else if (r >= maxRating * 0.1 && r < maxRating * 0.2) {
                    ratingStr = "<img src=\"star_1\" alt=\"*\"/>";
                } else {
                    ratingStr = "<img src=\"star_0\" alt=\"_\"/>";
                }
            }
        	NumberFormat nf = NumberFormat.getNumberInstance(locale);
            nf.setMaximumFractionDigits(2);
            nf.format(r);
            
        	ratingStr = String.format(rb.getString("Landmark.userRating"), nf.format(r), ratingStr);
        }
        
        
        List<String> stats = new ArrayList<String>();
        
        String tmp = tokens.remove("Views");
        if (tmp != null) {
        	int views = NumberUtils.getInt(tmp, 0);
        	if (views > 0) {
        		MessageFormat fmt = new MessageFormat(String.format(rb.getString("Landmark.views"), views), locale);
            	stats.add(fmt.format(new Object[] {views}));
            }
        }
        
        int reviews = landmark.getNumberOfReviews();
        if (reviews > 0) {
        	MessageFormat fmt = new MessageFormat(String.format(rb.getString("Landmark.numberOfReviews"), reviews), locale);
        	stats.add(fmt.format(new Object[] {reviews}));
        }
        
        tmp = tokens.remove("Likes");
        if (tmp != null) {
        	int views = NumberUtils.getInt(tmp, 0);
        	if (views > 0) {
        		MessageFormat fmt = new MessageFormat(String.format(rb.getString("Landmark.likes"), views), locale);
            	stats.add(fmt.format(new Object[] {views}));
            }     	
        }
        
        tmp = tokens.remove("Dislikes");
        if (tmp != null) {
        	int views = NumberUtils.getInt(tmp, 0);
        	if (views > 0) {
        		MessageFormat fmt = new MessageFormat(String.format(rb.getString("Landmark.dislikes"), views), locale);
            	stats.add(fmt.format(new Object[] {views}));
            }  
        }
        
        tmp = tokens.remove("Comments");
        if (tmp != null) {
        	int views = NumberUtils.getInt(tmp, 0);
        	if (views > 0) {
        		MessageFormat fmt = new MessageFormat(String.format(rb.getString("Landmark.comments"), views), locale);
            	stats.add(fmt.format(new Object[] {views}));
            }  
        }
             
        if (!stats.isEmpty()) {
        	ratingStr += StringUtils.join(stats, ", ");
        }
        
        if (StringUtils.isNotEmpty(ratingStr)) {
            otherNamed.add(ratingStr);
        }

        //checkins

        String checkin_user = tokens.remove("checkin_user"); 
        if (checkin_user != null) {
            result.add("<font color=\"red\">" + String.format(rb.getString("Landmark.checkinUser"), checkin_user, prettyTime.format(new Date(landmark.getCreationDate()))) + "</font>");
        }
        String checkins = tokens.remove("checkins");
        if (checkins != null) {
        	result.add("<font color=\"red\">" + checkins + "</font>");
        }
        
        //tagged
        
        String tagged = tokens.remove("tagged");
        if (tagged != null) {
        	result.add("<font color=\"red\">" + String.format(rb.getString("Landmark.tagged"), prettyTime.format(new Date(landmark.getCreationDate())))  + "</font>");
        }

        //photo

        String photoUser = tokens.remove("photoUser");
        if (photoUser != null) {
            String caption = tokens.remove("caption");
            String link = tokens.remove("link");
            
            String message = null;
            cal.setTimeInMillis(landmark.getCreationDate());
            if (StringUtils.startsWith(link, "http")) {
            	message = String.format(rb.getString("Landmark.photo_url"), photoUser, prettyTime.format(cal), link, caption);
            } else if (StringUtils.startsWith(caption, "http")) {
            	message = String.format(rb.getString("Landmark.photo_url_noname"), photoUser, prettyTime.format(cal), caption);
            }
            
            if (message != null) {
                result.add(message);
            }
        }
        
        val = tokens.remove("isTrending");
        if (val != null) {
            result.add(String.format(rb.getString("Landmark.trending"), val));
        }
        //start_date

        if (StringUtils.isNotEmpty(start_date)) {
            result.add(start_date);
        }
       
        if (!otherNamed.isEmpty()) {
            result.add(StringUtils.join(otherNamed, ",<br/>"));
        }
        
        List<String> others = new ArrayList<String>();      
        
        //other
        
        for (Iterator<Map.Entry<String, String>> i = tokens.entrySet().iterator(); i.hasNext();) {
            Map.Entry<String, String> entry = i.next();
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (key.equals("homepage")) {
                String[] homepageTokens = StringUtils.split(value, ' ');
                if (homepageTokens.length > 0) {
                    others.add(getLink(homepageTokens[0], rb.getString("Landmark.homepage")));
                }
            } else if (key.equals("source")) {
                others.add(String.format(rb.getString("Landmark.source"), value));
            } else if (key.equals("twitter")) {
                others.add(String.format(rb.getString("Landmark.twitter"), getLink("http://mobile.twitter.com/" + value, "@" + value)));
            } else if (key.equals("facebook")) {
                others.add(getLink("http://touch.facebook.com/profile.php?id=" + value, "Facebook"));
            } else if (key.equals("menu")) {
                others.add(getLink(value, rb.getString("Landmark.menu")));
            } else if (key.equals("photo")) {
                others.add(getLink(value, rb.getString("Landmark.photo")));
            } else if (key.equals("no_rooms")) {
            	others.add(String.format(rb.getString("Landmark.no_rooms"), value)); 
            } else {
                others.add(key + ": " + value);
            }
            
        }
        
        if (!others.isEmpty()) {
            result.add(StringUtils.join(others, ", "));
        }
        
        return StringUtils.join(result, "<br/>");
    }
}
