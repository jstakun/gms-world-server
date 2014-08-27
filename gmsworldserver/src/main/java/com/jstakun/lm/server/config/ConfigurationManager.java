package com.jstakun.lm.server.config;

import com.jstakun.lm.server.persistence.Config;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.persistence.ConfigPersistenceUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jstakun
 */
public final class ConfigurationManager {
    
    private static Map<String, String> configuration = new HashMap<String, String>();
    public static final String CONFIG = "config";
    
    public static void populateConfig()
    {
         List<Config> params = ConfigPersistenceUtils.selectAllConfigParams();
         for (Config param : params) {
             configuration.put(param.getKey(), param.getValue());
         }
         CacheUtil.put(CONFIG, configuration);
    }

    private static void refreshConfig()
    {
        Object o = CacheUtil.getObject(CONFIG);
        if (o != null && o instanceof HashMap) {
            configuration = (HashMap<String, String>) o;
        } else {
            populateConfig();
        }
    }

    public static String getParam(String key, String defaultValue)
    {
        refreshConfig();
        if (configuration.containsKey(key)) {
            return (String)configuration.get(key);
        } else {
            return defaultValue;
        }
    }    
    
    public static Map<String, String> getConfiguration() {
    	return Collections.unmodifiableMap(configuration);
    }
}
