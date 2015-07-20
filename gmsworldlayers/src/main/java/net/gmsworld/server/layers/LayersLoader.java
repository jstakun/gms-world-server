package net.gmsworld.server.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.utils.TaskExecutorUtils;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

public class LayersLoader {
	
	private List<String> layers = new ArrayList<String>();
	private ThreadFactory factory;
	private static final Logger logger = Logger.getLogger(LayersLoader.class.getName());
	
	public LayersLoader(ThreadFactory factory, List<String> layers) {
		this.layers = layers;
		this.factory = factory;
	}

	public List<List<ExtendedLandmark>> loadLayers(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flex, String flex2, Locale locale, boolean useCache) {
		int l = layers.size();
        List<List<ExtendedLandmark>> results = new ArrayList<List<ExtendedLandmark>>();
        TaskExecutorUtils<List<ExtendedLandmark>> executor = new TaskExecutorUtils<List<ExtendedLandmark>>(l, factory, results);
        
        for (String layerName : layers) {
        	executor.submit(new LayerLoaderTask(layerName, lat, lng, query, radius, version, limit, stringLimit, flex, flex2, locale, useCache));
        }
        
        executor.waitForResponses();
        
        return results;
	}
	
	private static class LayerLoaderTask implements Callable<List<ExtendedLandmark>> {

		private double latitude, longitude;
	    private String query, layer, flex, flex2;
	    private int radius, limit, stringLimit, version;
	    private Locale locale;
	    private boolean useCache;
	    
		public LayerLoaderTask(String layer, double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String flex, String flex2, Locale locale, boolean useCache) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.query = query;
			this.layer = layer;
			this.radius = radius;
			this.useCache = useCache;
			this.limit = limit;
			this.stringLimit = stringLimit;
			this.locale = locale;
			this.flex = flex;
			this.flex2 = flex2;
			this.version = version;
		}
		
		public List<ExtendedLandmark> call() throws Exception {
			logger.log(Level.INFO, "Loading landmarks in layer {0}", layer);
			List<ExtendedLandmark> landmarks = null;

			try {				
				LayerHelper layerHelper = LayerHelperFactory.getByName(layer);
				landmarks = layerHelper.processBinaryRequest(latitude, longitude, query, radius, version, limit, stringLimit, flex, flex2, locale, useCache);
				layerHelper.cacheGeoJson(landmarks, latitude, longitude, layer);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				if (landmarks != null) {
					logger.log(Level.INFO, "Loaded {0} landmarks in layer {1}", new Object[]{landmarks.size(), layer});	
				}
			}	
			return landmarks;
		}	
	}
}
