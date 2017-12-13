package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
				 Long imei = Long.valueOf(request.getParameter("imei"));
		         Integer pin = Integer.valueOf(request.getParameter("pin"));		
		         int status = DevicePersistenceUtils.isDeviceRegistered(imei, pin);
		         if (status == 1) {
		        	  out.print("{\"status\":\"verified\"}");
		         } else {
		        	  out.print("{\"status\":\"not-verified\"}");
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
				 Long imei = Long.valueOf(request.getParameter("imei"));
		         Integer pin = Integer.valueOf(request.getParameter("pin"));		
		         String token = request.getParameter("token");
		         String username = request.getParameter("username");
		         int status = DevicePersistenceUtils.setupDevice(imei, pin, username, token);
		         if (status == 1) {
		        	  out.print("{\"status\":\"ok\"}");
		         } else {
		        	  out.print("{\"status\":\"error\"}");
		        	  response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
