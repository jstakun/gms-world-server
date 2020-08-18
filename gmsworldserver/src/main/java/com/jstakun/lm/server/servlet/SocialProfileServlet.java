package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.google.gdata.util.common.util.Base64;

import net.gmsworld.server.utils.UrlUtils;

/**
 *
 * @author jstakun
 */
public class SocialProfileServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String uid = request.getParameter("uid");
            if (StringUtils.isEmpty(uid)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                final String profileUrl = UrlUtils.createUserProfileUrl(uid);
                if (StringUtils.startsWith(profileUrl, "http://") || StringUtils.startsWith(profileUrl, "https://")) {
                    response.sendRedirect(profileUrl);
                } else {         
                	if (uid.length() % 4 == 0) {
                		try {
                    		uid = new String(Base64.decode(uid));
                    	} catch (Exception e) {
                    		//from version 1086, 86 uid is Base64 encoded string
                    	}
                	}
                	//if (UserPersistenceUtils.userExists(uid)) {
                		response.sendRedirect("/showUser/" + uid);
                	/*} else {
                		out.println("<html>");
                		out.println("<head>");
                		out.println("<title>GMS World - user social profile</title>");
                		out.println("</head>");
                		out.println("<body>");
                		out.println("Sorry. We couldn't find user profile social address for uid: " + uid);
                		out.println("</body>");
                		out.println("</html>");
                	}*/	
                }
            }
        } finally {
            out.close();
        }
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);




    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);




    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";


    }// </editor-fold>
}
