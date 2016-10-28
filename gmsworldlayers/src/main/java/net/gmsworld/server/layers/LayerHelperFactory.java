package net.gmsworld.server.layers;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.utils.memcache.CacheProvider;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 *
 * @author jstakun
 */
public class LayerHelperFactory {
	
	private static final Logger logger = Logger.getLogger(LayerHelperFactory.class.getName());
	
	private static List<String> enabledLayers = new ArrayList<String>();
	
	private static Map<String, LayerHelper> allLayers = new HashMap<String, LayerHelper>();
	
	private static final LayerHelperFactory instance = new LayerHelperFactory();
	
	private LayerHelperFactory() {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
    		.setUrls(
    				ClasspathHelper.forClass(LayerHelper.class)
    		)
    		.setScanners(
    				new SubTypesScanner().filterResultsBy(
    						new FilterBuilder()
    							.include(LayerHelper.class.getName())
    							.include(OverpassUtils.class.getName()) //TODO find way to search for indirect subclasses
    							.include(FoursquareUtils.class.getName())
    				)
    		)
    		.filterInputsBy(
    			new FilterBuilder()
                	.includePackage("net.gmsworld.server.layers")
    		)
		);

		Set<Class<? extends LayerHelper>> matchingClasses = reflections.getSubTypesOf(LayerHelper.class);
		
		for (Class<? extends LayerHelper> matchingClass : matchingClasses) {
			if (!Modifier.isAbstract(matchingClass.getModifiers())) {
				try {
					LayerHelper layer = matchingClass.newInstance();
					logger.info("Found layer " + layer.getLayerName() + " class " + matchingClass.getName());
					if (layer.isEnabled()) {
						enabledLayers.add(layer.getLayerName());
					}
					allLayers.put(layer.getLayerName(), layer);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Failed to create new instance for class " + matchingClass.getName(), e);
				}			
			}
		}		
	}
	
	public void setCacheProvider(CacheProvider cp) {
    	for (LayerHelper layer : allLayers.values()) {
    		layer.setCacheProvider(cp);
    	}
    }
    
    public void setThreadProvider(ThreadFactory tp) {
    	for (LayerHelper layer : allLayers.values()) {
    		layer.setThreadProvider(tp);
    	}
    }
    
    public static LayerHelperFactory getInstance() {
    	return instance;
    }
    
	public LayerHelper getByName(String name) {
    	return allLayers.get(name);
    }
    
    public List<String> getEnabledLayers() {
    	return enabledLayers;
    }
    
    public String getIcon(String name) {
    	LayerHelper layer = getByName(name);
    	if (layer != null) {
    		return layer.getIcon();
    	} else {
    		logger.log(Level.WARNING, "No icon found for layer " + name);
    		return null;
    	}
    }
    
    protected Map<String, LayerHelper> getAllLayers() {
    	return allLayers;
    }
}
