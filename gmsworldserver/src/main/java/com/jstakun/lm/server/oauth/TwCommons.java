/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.oauth;

import com.jstakun.lm.server.config.ConfigurationManager;

/**
 *
 * @author jstakun
 */
public final class TwCommons {
    protected static final String CALLBACK_URL = ConfigurationManager.SERVER_URL + "twauth";
    
    private TwCommons() {}
}
