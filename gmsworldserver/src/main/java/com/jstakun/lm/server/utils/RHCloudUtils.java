package com.jstakun.lm.server.utils;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

public class RHCloudUtils {

	private static final Logger logger = Logger.getLogger(RHCloudUtils.class.getName());
	
	public static void rhcloudHealthCheck() {
		try {
			rhcloudHealthCheck("hotels", "http://openapi-hotels.b9ad.pro-us-east-1.openshiftapps.com/camel/v1/ping");
		} catch (Exception e) {
			 logger.log(Level.SEVERE, e.getMessage(), e);
		}
		try {
			rhcloudHealthCheck("landmarks", "https://openapi-landmarks.b9ad.pro-us-east-1.openshiftapps.com/");
		} catch (Exception e) {
			 logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
   	
	private static Integer rhcloudHealthCheck(String appname, String healthCheckUrl) throws IOException {
    	logger.log(Level.INFO, "Checking if {0} app is running...", appname);
    	URL rhcloudUrl = new URL(healthCheckUrl);
    	HttpUtils.processFileRequestWithBasicAuthn(rhcloudUrl, Commons.getProperty(Property.RH_GMS_USER), true);
    	Integer status = HttpUtils.getResponseCode(rhcloudUrl.toExternalForm()); 
    	if (status != null && status == 503) {
    		logger.log(Level.SEVERE, "Received Service Unavailable error response!");
    	} else {
    		logger.log(Level.INFO, "Received server response code " + status);
    	}
    	 if (status == null || status > 299) {
         	logger.log(Level.SEVERE, "Received landmarks status code " + status);
         	MailUtils.sendAdminMail("Received response code " + status + " from " + appname + ": " + healthCheckUrl + "\n" +
         	"Go to: https://console.pro-us-east-1.openshift.com/console/");
         }
    	return status;
    }
}
