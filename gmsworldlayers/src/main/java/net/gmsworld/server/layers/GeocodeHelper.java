package net.gmsworld.server.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.openlapi.AddressInfo;

import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.memcache.CacheProvider;

public abstract class GeocodeHelper {
	
	protected static final Logger logger = Logger.getLogger(GeocodeHelper.class.getName());
	
	protected CacheProvider cacheProvider = null;
	
	protected abstract JSONObject processGeocode(String addressIn, String email, int appId, boolean persistAsLandmark) throws Exception;
	
	protected abstract AddressInfo processReverseGeocode(double lat, double lng) throws Exception;
	
	protected abstract JSONObject getRoute(String lat_start, String lng_start, String lat_end, String lng_end, String type, String username) throws Exception;

    protected static String getRouteKey(Class<?> clazz, String lat_start, String lng_start, String lat_end, String lng_end, String type, String username) {
    	List<String> params = new ArrayList<String>(7);

        params.add(clazz.getName());
        
        params.add(lat_start);
        params.add(lng_start);
        params.add(lat_end);
        params.add(lng_end);
        
        params.add(type);
        params.add(username);
        
        return StringUtils.join(params, "_");
    }
    
    public void setCacheProvider(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}
    
    public CacheProvider getCacheProvider() {
		return cacheProvider;
	}
}
