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

import org.apache.commons.lang.StringUtils;
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
		final Class<LayerHelper> parentClass = LayerHelper.class;
		final String packageName = parentClass.getPackage().getName();
		logger.info("Starting LayerHelperFactory scanner in package " + packageName);
		
		Reflections reflections = new Reflections(new ConfigurationBuilder()
    		.setUrls(
    				ClasspathHelper.forClass(LayerHelper.class)
    		)
    		.setScanners(
    				new SubTypesScanner().filterResultsBy(
    						new FilterBuilder()
    							.includePackage(packageName)
    				)
    		)
    		.filterInputsBy(
    			new FilterBuilder()
                	.includePackage(packageName)
    		)
		);

		Set<Class<? extends LayerHelper>> matchingClasses = reflections.getSubTypesOf(parentClass);
		
		for (Class<? extends LayerHelper> matchingClass : matchingClasses) {
			if (!Modifier.isAbstract(matchingClass.getModifiers())) {
				try {
					LayerHelper layer = matchingClass.newInstance();
					logger.info("Found layer " + layer.getLayerName() + " class " + matchingClass.getName());
					if (layer.isEnabled()) {
						enabledLayers.add(layer.getLayerName());
					}
					allLayers.put(layer.getLayerName(), layer);
				} catch (Throwable e) {
					logger.log(Level.SEVERE, "Failed to create new instance for layer class " + matchingClass.getName(), e);
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
	
	public LayerHelper getByURI(String uri) {
		for (LayerHelper layer : allLayers.values()) {
			if (StringUtils.contains(uri, layer.getURI())) {
				return layer;
			}
		}
		return null;
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
