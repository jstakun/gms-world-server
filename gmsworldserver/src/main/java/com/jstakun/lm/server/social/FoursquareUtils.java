package com.jstakun.lm.server.social;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.Commons.Property;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.CompleteTip;
import fi.foyt.foursquare.api.io.DefaultIOHandler;

public class FoursquareUtils {

	private static final Logger logger = Logger.getLogger(FoursquareUtils.class.getName());
	
	protected static int checkin(String accessToken, String venueId, String name) {
		try {
			FoursquareApi api = new FoursquareApi(Commons.getProperty(Property.FS_CLIENT_ID), Commons.getProperty(Property.FS_CLIENT_SECRET), null, accessToken, new DefaultIOHandler());
			ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
	        
			//shout must be maximum 200 character
			String shout = StringUtils.abbreviate(String.format(rb.getString("Social.checkin"), name), 200);
			Result<Checkin> result = api.checkinsAdd(venueId, null, shout, "public", null, null, null, null);
			
			int res = result.getMeta().getCode();
			if (res != 200) {
				logger.log(Level.SEVERE, result.getMeta().getErrorType() + ": " + result.getMeta().getErrorDetail());
			}
			return res;
		} catch (Exception ex) {
	        logger.log(Level.SEVERE, "FoursquareUtils.checkin exception:", ex);
	        return 500;
	    }
	}

	protected static int sendTip(String accessToken, String venueId, String text) {
		try {
			FoursquareApi api = new FoursquareApi(Commons.getProperty(Property.FS_CLIENT_ID), Commons.getProperty(Property.FS_CLIENT_SECRET), null, accessToken, new DefaultIOHandler());
			Result<CompleteTip> result = api.tipsAdd(venueId, text, null);
			int res = result.getMeta().getCode();
			if (res != 200) {
				logger.log(Level.SEVERE, result.getMeta().getErrorType() + ": " + result.getMeta().getErrorDetail());
			}
			return res;
		} catch (Exception ex) {
	        logger.log(Level.SEVERE, "FoursquareUtils.checkin exception:", ex);
	        return 500;
	    }
	}

}
