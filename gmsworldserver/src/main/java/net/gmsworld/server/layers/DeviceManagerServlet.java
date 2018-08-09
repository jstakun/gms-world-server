package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;

import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;

/**
 * Servlet implementation class DeviceManagerServlet
 */
public final class DeviceManagerServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DeviceManagerServlet.class.getName());   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeviceManagerServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			if (!HttpUtils.isEmptyAny(request, "imei")) {
				 int status = DevicePersistenceUtils.isDeviceRegistered(request.getParameter("imei"));
		         if (status == 1) {
		        	  out.print("{\"status\":\"verified\"}");
		         } else {
		        	  out.print("{\"status\":\"unverified\"}");
		         }
			} else if (!HttpUtils.isEmptyAny(request, "username"))  {
				String devices = DevicePersistenceUtils.getUserDevices(request.getParameter("username"));
				out.print("{\"devices\":" + devices + "}");
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			out.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			if (HttpUtils.isEmptyAny(request, "imei")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				 String imei = request.getParameter("imei");
				 String token = request.getParameter("token");
		         String username = request.getParameter("username");
		         String name = request.getParameter("name");
		         String command = request.getParameter("command");
		         String args = request.getParameter("args");
		         String correlationId = request.getParameter("correlationId");
		         String action = request.getParameter("action"); 
		        		 
		         if (StringUtils.equalsIgnoreCase(token, "BLACKLISTED")) {
		        	 logger.log(Level.SEVERE, "Imei: " + imei + ", token: " + token);
	        		 response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		         } else {
		        	 int status;
		        	 int pin = NumberUtils.getInt(request.getParameter("pin"), -1);
			         if (StringUtils.isNotEmpty(command) && pin >= 0) {
			        	 String[] cid = StringUtils.split(correlationId, "+=+");
			        	 String commandKey = "";
			        	 if (cid != null && cid.length == 2) {
			        		 commandKey += cid[0].trim() + "_";
			        	 }
			        	 if (imei != null) {
			 				 commandKey +=  imei  + "_";
			 			 } else if (username != null && name != null) {
			 				 commandKey += username + "_" + name  + "_";
			 			 }
			        	 commandKey += command;
			        	 if (CacheUtil.containsKey(commandKey)) {
			        		  CacheUtil.increment(commandKey);
			        		  logger.log(Level.WARNING, "Command " + commandKey + " has been sent before " + CacheUtil.getString(commandKey) + " times");
			        	      //TODO status = -3;
			        	      status = DevicePersistenceUtils.sendCommand(imei, pin, name, username, command, args, correlationId);
			        	 } else {
			        		  CacheUtil.put(commandKey, "1", CacheType.FAST);
			        		  status = DevicePersistenceUtils.sendCommand(imei, pin, name, username, command, args, correlationId);
			        	 }		        		 
		        	 } else if (StringUtils.equalsIgnoreCase(action, "delete")) {
		        		 status = DevicePersistenceUtils.deleteDevice(imei);
		        	 } else { 
		        		 status = DevicePersistenceUtils.setupDevice(imei, name, username, token);
		        	 }	 
		        	 if (status == 1) {
		        		 out.print("{\"status\":\"ok\"}");
		        	 } else if (status == -2) {
		        		 response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		        	 } else if (status == -3) {
		        		 response.sendError(HttpServletResponse.SC_FORBIDDEN);
		        	 } else {
		        		 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		        	 }
		         }
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			out.close();
		}
	}
 
}
