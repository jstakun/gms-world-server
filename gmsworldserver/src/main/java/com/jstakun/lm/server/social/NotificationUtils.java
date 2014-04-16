package com.jstakun.lm.server.social;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

public class NotificationUtils {
	
	//private static final Logger logger = Logger.getLogger(NotificationUtils.class.getName());
	
	public static void createNotificationTask(Map<String, String> params) {
		Queue queue = QueueFactory.getQueue("notifications");
		TaskOptions options = withUrl("/tasks/notificationTask");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			options.param(entry.getKey(), entry.getValue());	
		}
		//logger.log(Level.INFO, "Creating new notification task {0}...", options.toString());
		queue.add(options);   		
	}

	public static Map<String, String> getNotificationParams(String key) {
		Map<String, String> params = new HashMap<String, String>();
		
		if (StringUtils.isNotEmpty(key)) {
			params.put("key", key);
			Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
			if (landmark != null) {
				params.put("url", UrlUtils.getShortUrl(UrlUtils.getLandmarkUrl(landmark)));
				if (landmark.getLayer().equals("Social")) {
					params.put("type", Integer.toString(Commons.BLOGEO));
					params.put("title", landmark.getName());
				} else if (landmark.getLayer().equals(Commons.MY_POS_CODE)) {
					params.put("type", Integer.toString(Commons.MY_POS));
					params.put("title", Commons.MY_POSITION_LAYER);
				} else {
					params.put("type", Integer.toString(Commons.LANDMARK));
					params.put("title", landmark.getName());
				}
			}
		} else {
			params.put("type", Integer.toString(Commons.LOGIN));
			params.put("url", ConfigurationManager.SERVER_URL);
			params.put("title", "Message from GMS World");
		}
		
		return params;
	}
}
