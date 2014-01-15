/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.persistence;

import com.google.appengine.api.datastore.KeyFactory;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.UrlUtils;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class CommonPersistenceUtils {

    private static final Logger logger = Logger.getLogger(CommonPersistenceUtils.class.getName());

    public static boolean isKeyValid(String key) {
        if (StringUtils.isNotEmpty(key)) {
            if (StringUtils.startsWith(key, UrlUtils.BITLY_URL) || StringUtils.startsWith(key, ConfigurationManager.SERVER_URL)) {
                return true;
            } else {
                try {
                    KeyFactory.stringToKey(key);
                    return true;
                } catch (IllegalArgumentException iae) {
                    logger.log(Level.SEVERE, "Wrong key format: {0}", key);
                    return false;
                }
            }
        } else {
            return false;
        }
    }
}
