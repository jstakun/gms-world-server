package com.jstakun.lm.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.bitwalker.useragentutils.OperatingSystem;

/**
 * Servlet implementation class ErrorHandlerServlet
 */
public class ErrorHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ErrorHandlerServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
	 * @see HttpServlet#processRequest(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		
		String header = null, message = null;
		
		if (statusCode != null && statusCode == 403) { 
			header = "Access Forbidden";
			message = "Request rate too high.";
		} else if (statusCode != null && statusCode == 404) {
			header = "Page Not Found";
			message = "Sorry. Please try again or contact our <a href=\"mailto:support@gms-world.net?subject=Page Not Found\">support</a>.";
		} else {
			header = "Internal Server Error";
			message = "Oops! Something went wrong. Please try again or contact our <a href=\"mailto:support@gms-world.net?subject=Internal Server Error\">support</a>.";
		}
		
		request.setAttribute("header", header);
		request.setAttribute("message", message);
				
		OperatingSystem os = OperatingSystem.parseUserAgentString(request.getHeader("User-Agent"));
		if (os.isMobileDevice()) {
        	request.getRequestDispatcher("/m/error.jsp").forward(request, response); 
        } else {
        	request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
	}
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

}
