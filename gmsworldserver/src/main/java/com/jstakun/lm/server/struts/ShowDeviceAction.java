package com.jstakun.lm.server.struts;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONObject;

import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;

import net.gmsworld.server.utils.persistence.Landmark;

public class ShowDeviceAction extends Action {

	private static final Logger logger = Logger.getLogger(ShowDeviceAction.class.getName());
	
	@Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,  final HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		final String imei =  request.getParameter("imei");
		
		if (StringUtils.isNotEmpty(imei)) {
			try {
				final String deviceJsonString = DevicePersistenceUtils.getDevice(imei);
				JSONObject root = new JSONObject(deviceJsonString);
				if (root.has("output")) {
					 JSONObject deviceJson = root.getJSONObject("output");
				     Landmark landmark = ShowUserDevicesAction.jsonToLandmark(deviceJson, request);
				     if (landmark != null) {
				    	 request.setAttribute("landmark", landmark);
				     }
				} else {
					logger.log(Level.WARNING, "Device not found");
				}	
				String status = (String)CacheUtil.remove("locatedladmindlt:" + imei + ":status");
				if (StringUtils.isNotEmpty(status)) {
					status = StringUtils.replaceEach(status, new String[] {imei, "locatedladmin"}, new String[] {"", ""});
					request.setAttribute("status", status);
				}
				request.setAttribute("imei", imei);
				request.setAttribute("type", "device");
			} catch (Exception e) {
				 logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		return mapping.findForward("success");
	}
}
