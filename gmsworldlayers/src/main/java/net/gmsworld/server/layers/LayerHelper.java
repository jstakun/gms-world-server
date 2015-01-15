package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.ThreadProvider;
import net.gmsworld.server.utils.memcache.CacheProvider;

/**
 *
 * @author jstakun
 */
public abstract class LayerHelper {

    protected static final Logger logger = Logger.getLogger(LayerHelper.class.getName());
    protected ThreadProvider threadProvider = null;
    protected CacheProvider cacheProvider = null;
	
	public void setThreadProvider(ThreadProvider threadProvider){
		this.threadProvider = threadProvider;
	}
	
	public void setCacheProvider(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}
    
    protected JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2) throws Exception {
    	return null;
    }

    protected abstract List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws Exception;
    
    public void serialize(List<ExtendedLandmark> landmarks, OutputStream out, int version) {
    	ObjectOutputStream outObj = null;
    	DeflaterOutputStream compressor = null;
    	try {
    		if (version >= 12) {
    			compressor = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_COMPRESSION, false));
    			outObj = new ObjectOutputStream(compressor);
    		} else {
    			outObj = new ObjectOutputStream(out);
    		}
    		
    		if (version >= 11) {
    			//Externalization
    			outObj.writeInt(landmarks.size());
    			if (!landmarks.isEmpty()) {
    				for (ExtendedLandmark landmark : landmarks) {
    					if (landmark != null) {
    						landmark.writeExternal(outObj);
    					}
    				}
    			}
    			outObj.flush();
    			
    		} else {
    			//Serialize
    			outObj.writeObject(landmarks);
    			//out.flush();
    		}
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    	} finally {	
    		if (outObj != null) {
    			try {
    				outObj.close();
    			} catch (IOException e) {
    				
    			}
    			try {
    				if (compressor != null) {
    					compressor.close();
    				}
    			} catch (IOException e) {
    				
    			}
    			try {
    				out.close();
    			} catch (IOException e) {
    				
    			}
    		}
    	}
    }
    
    protected String getCacheKey(Class<?> clazz, String methodName, double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2) throws UnsupportedEncodingException {
        List<String> params = new ArrayList<String>(12);

        params.add(clazz.getName());
        if (StringUtils.isNotEmpty(methodName)) {
            params.add(methodName);
        }

        params.add(StringUtil.formatCoordE2(lat));
        params.add(StringUtil.formatCoordE2(lng));

        if (StringUtils.isNotEmpty(query)) {
            params.add(URLEncoder.encode(query, "UTF-8"));
        }

        params.add(Integer.toString(radius));
        params.add(Integer.toString(version)); 
        params.add(Integer.toString(limit)); 
        params.add(Integer.toString(stringLimit)); 
        
        if (StringUtils.isNotEmpty(flexString)) {
            params.add(flexString);
        }
       
        if (StringUtils.isNotEmpty(flexString2)) {
            params.add(flexString2); 
        }

        return StringUtils.join(params, "_");
    }
}
