package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;

import net.gmsworld.server.config.Commons;
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
				//TODO move to post
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
		         String flex = request.getParameter("flex"); 
		         
		         try {
		        	 int version = NumberUtils.getInt(request.getHeader("X-GMS-AppCode"), -1);
		        	 if (version >= 28) {
		        		 if (flex == null) {
		        			 flex = processHeadersV2(request);
		        		 } else {
		        			 flex += "," + processHeadersV2(request);
		        		 }
		        	 } else {
		        		 if (flex == null) {
		        			 flex = processHeaders(request);
		        		 } else {
		        			 flex += "," + processHeaders(request);
		        		 }
		        	 }
		         } catch (Exception e) {
		        	 logger.log(Level.SEVERE, e.getMessage(), e);
		         }
		         		        		 
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
			        	 Long count = CacheUtil.increment(commandKey);
			        	 if (count > 10) {
			        		 logger.log(Level.WARNING, "Command " + commandKey + " has been sent " + count + " times");
			        		 //TODO status = -3;
			        		 status = DevicePersistenceUtils.sendCommand(imei, pin, name, username, command, args, correlationId, flex);
			        	 } else {
			        		  logger.log(Level.INFO, "Command " + commandKey + " has been sent " + count + " times");
			        		  status = DevicePersistenceUtils.sendCommand(imei, pin, name, username, command, args, correlationId, flex);
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
 
	private String processHeaders(HttpServletRequest request) {
		Double latitude = null, longitude = null;
		String flex = null;
   	   	if (request.getHeader(Commons.LAT_HEADER) != null) {
   	   		latitude = GeocodeUtils.getLatitude(request.getHeader(Commons.LAT_HEADER));
   	   	}
   	   	if (request.getHeader(Commons.LNG_HEADER) != null) {
   	   		longitude = GeocodeUtils.getLongitude(request.getHeader(Commons.LNG_HEADER));
   	   	}
   	   	if (latitude != null && longitude != null) {
   	   		flex = "geo:" + latitude + "," + longitude;
   	   		if (StringUtils.isNotEmpty(request.getHeader("X-GMS-DeviceName"))) {
   				 flex += "," + request.getHeader("X-GMS-DeviceName");
   			 }
   			 if (StringUtils.isNotEmpty(request.getHeader("X-GMS-RouteId"))) {
   				 flex += ",rid:" + request.getHeader("X-GMS-RouteId");
   			 }
   	   	}
   	   	return flex;
	}
	
	private String processHeadersV2(HttpServletRequest request) {
		List<String> tokens = new ArrayList<>();
		Double latitude = null, longitude = null;
   	   	if (request.getHeader(Commons.LAT_HEADER) != null) {
   	   		latitude = GeocodeUtils.getLatitude(request.getHeader(Commons.LAT_HEADER));
   	   	}
   	   	if (request.getHeader(Commons.LNG_HEADER) != null) {
   	   		longitude = GeocodeUtils.getLongitude(request.getHeader(Commons.LNG_HEADER));
   	   	}
   	   	if (latitude != null && longitude != null) {
   	   		tokens.add("geo:" + latitude + "+" + longitude);
   	   	}	
   	   	if (StringUtils.isNotEmpty(request.getHeader("X-GMS-DeviceId"))) {
   	   		tokens.add("deviceId:" + request.getHeader("X-GMS-DeviceId"));
   	   	}
   	   	if (StringUtils.isNotEmpty(request.getHeader("X-GMS-DeviceName"))) {
   	   		tokens.add("deviceName:" + request.getHeader("X-GMS-DeviceName"));
   	   	}
   	   	if (StringUtils.isNotEmpty(request.getHeader("X-GMS-RouteId"))) {
   	   		tokens.add("routeId:" + request.getHeader("X-GMS-RouteId"));
   	   	} 
   	   	if (StringUtils.isNotEmpty(request.getParameter("replyToCommand"))) {
   	   		tokens.add("command:" + request.getParameter("replyToCommand"));
   	   	}
   	   	if (!tokens.isEmpty()) {
   	   		 return StringUtils.join(tokens, ",");
   	   	} else {
   	   		return null;
   	   	}
	}
	
}
