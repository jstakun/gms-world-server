package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.ConfigurationManager;

/**
 * Servlet implementation class LetsEncryptAuthServlet
 */
public class LetsEncryptAuthServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(LetsEncryptAuthServlet.class.getName());
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LetsEncryptAuthServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
		String[] tokens = StringUtils.split(request.getRequestURI(), "/");
		logger.log(Level.INFO, "Found: " + tokens.length + " keys");
		if (tokens.length > 0) {
			 String key = tokens[tokens.length-1];
			 logger.log(Level.INFO, "Checking token: " + key);
			 if (StringUtils.isNotEmpty(key)) {
				 String value = ConfigurationManager.getParam("letsEncryptTokens", null);
				 if (StringUtils.isNotEmpty(value)) {
					 tokens = StringUtils.split(value, ",");
					 for (int i=0;i<tokens.length;i++) {
						 //String[] token = StringUtils.split(tokens[i], ":");
						 //if (token.length == 2 && StringUtils.equals(token[0],key)) {
						 //	 out.print(token[1]);
						 //}
						 //each token has structure <key>.<value> or naked-domain.<key>.<value>
						 String[] token = StringUtils.split(tokens[i], ".");
						 if (token.length >= 2 && StringUtils.equals(token[0],key)) {
							 out.print(token[token.length-2] + "." + token[token.length-1]);
						}
					 }
				 } else {
					 out.print("No value found for token " + key);
				 }
			 }
		}
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
