package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.jstakun.lm.server.utils.TokenUtil;

/**
 * Servlet implementation class TokenServlet
 */
public class TokenServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AuthnServlet.class.getName());
	/**
     * @see HttpServlet#HttpServlet()
     */
    public TokenServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}
	
	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json");
        PrintWriter out = response.getWriter();
        JSONObject resp = new JSONObject();
    	try {
        	String scope = (String)request.getParameter("scope");
        	if (scope != null) {
        		String key = TokenUtil.generateToken(scope, null);
        		resp.put("gmsToken", key);   			
        	} else {
        		resp.put("message", "Missing scope parameter!");          	
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        	resp.put("message", e.getMessage());
        } finally { 
          	out.print(resp.toString());
            out.close();
        }
	}

}
