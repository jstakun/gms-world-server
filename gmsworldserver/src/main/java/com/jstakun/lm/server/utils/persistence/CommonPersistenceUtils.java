package com.jstakun.lm.server.utils.persistence;

import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.UrlUtils;

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
                    return StringUtils.isNumeric(key);
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
