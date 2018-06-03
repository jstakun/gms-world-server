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

import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;

import net.gmsworld.server.utils.HttpUtils;

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
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			if (HttpUtils.isEmptyAny(request, "imei", "pin")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				 String imei = request.getParameter("imei");
		         Integer pin = Integer.valueOf(request.getParameter("pin"));		
		         int status = DevicePersistenceUtils.isDeviceRegistered(imei, pin);
		         if (status == 1) {
		        	  out.print("{\"status\":\"verified\"}");
		         } else {
		        	  out.print("{\"status\":\"unverified\"}");
		         }
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
		PrintWriter out = response.getWriter();
		try {
			if (HttpUtils.isEmptyAny(request, "imei", "pin")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				 String imei = request.getParameter("imei");
		         Integer pin = Integer.valueOf(request.getParameter("pin"));		
		         String token = request.getParameter("token");
		         String username = request.getParameter("username");
		         String name = request.getParameter("name");
		         String command = request.getParameter("command");
		         String args = request.getParameter("args");
		         String oldPin = request.getParameter("oldPin");
		         String correlationId = request.getParameter("correlationId");
		         if (pin < 0 || (oldPin != null && !StringUtils.isNumeric(oldPin)) || StringUtils.equalsIgnoreCase(token, "BLACKLISTED")) {
		        	 logger.log(Level.SEVERE, "Imei: " + imei + ", pin: " + pin + ", oldPin: " + oldPin + ", token: " + token);
	        		 response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		         } else {
		        	 int status;
			         if (StringUtils.isNotEmpty(command)) {
		        		 status = DevicePersistenceUtils.sendCommand(imei, pin, name, username, command, args, correlationId);
		        	 } else {
		        		 //logger.log(Level.INFO, "Imei: " + imei + ", pin: " + pin + ", name: " + name + ", username: " + username + ", token: " + token);
		        		 status = DevicePersistenceUtils.setupDevice(imei, pin, name, username, token, oldPin);
		        	 }	 
		        	 if (status == 1) {
		        		 out.print("{\"status\":\"ok\"}");
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
