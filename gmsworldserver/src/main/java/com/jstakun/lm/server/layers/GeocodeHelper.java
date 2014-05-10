package com.jstakun.lm.server.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.utils.StringUtil;

public abstract class GeocodeHelper {
	
	protected static final Logger logger = Logger.getLogger(GeocodeHelper.class.getName());
	
	protected abstract JSONObject processGeocode(String addressIn, String email) throws Exception;
	
	protected abstract String processReverseGeocode(double lat, double lng) throws Exception;
	
	protected abstract JSONObject getRoute(double lat_start, double lng_start, double lat_end, double lng_end, String type, String username) throws Exception;

    protected static String getRouteKey(Class<?> clazz, double lat_start, double lng_start, double lat_end, double lng_end, String type, String username) {
    	List<String> params = new ArrayList<String>(7);

        params.add(clazz.getName());
        
        params.add(StringUtil.formatCoordE2(lat_start));
        params.add(StringUtil.formatCoordE2(lng_start));
        params.add(StringUtil.formatCoordE2(lat_end));
        params.add(StringUtil.formatCoordE2(lng_end));
        
        params.add(type);
        params.add(username);
        
        return StringUtils.join(params, "_");
    }
}
