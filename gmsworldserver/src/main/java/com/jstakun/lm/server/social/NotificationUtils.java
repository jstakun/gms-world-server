package com.jstakun.lm.server.social;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class NotificationUtils {
	
	private static final Logger logger = Logger.getLogger(NotificationUtils.class.getName());
	
	public static void createNotificationTask(Map<String, String> params) {
		Queue queue = QueueFactory.getQueue("notifications");
		TaskOptions options = withUrl("/tasks/notificationTask");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			options.param(entry.getKey(), entry.getValue());	
		}
		logger.log(Level.INFO, "Creating new notification task: " + options.toString());
		queue.add(options);   		
	}

}
