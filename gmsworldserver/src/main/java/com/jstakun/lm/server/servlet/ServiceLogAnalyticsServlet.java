/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

import com.jstakun.lm.server.utils.persistence.ServiceLogPersistenceUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.utils.DateUtils;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class ServiceLogAnalyticsServlet extends HttpServlet {

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

            out.println("<html>");
            out.println("<head>");
            out.println("<title>ServiceLog Analytics</title>");
            out.println("</head>");
            out.println("<body>");

            Date today = DateUtils.getToday();
            Date fromDay = null;

            if (StringUtils.isNumeric(request.getParameter("fromDay"))) {
                try {
                    fromDay = DateUtils.getDay(request.getParameter("fromDay"));
                } catch (ParseException ex) {
                    Logger.getLogger(ServiceLogAnalyticsServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (StringUtils.isNumeric(request.getParameter("forDays"))) {
                try {
                    String dayString = request.getParameter("forDays");
                    fromDay = DateUtils.getDayInPast(Integer.parseInt(dayString)-1, true);
                } catch (Exception ex) {
                    Logger.getLogger(ServiceLogAnalyticsServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {

                if (fromDay == null) {
                    fromDay = DateUtils.getDay(DateUtils.getDay(today));
                }

                out.println("<h2>Services call per day from " + DateUtils.getDay(fromDay) + "</h2>");

                ArrayList<String> report = new ArrayList<String>();
                long numOfCalls = 0;
                while (fromDay.compareTo(today) <= 0) {
                    numOfCalls = ServiceLogPersistenceUtils.countServiceLogByDay(fromDay);
                    report.add(DateUtils.getDay(fromDay) + ": " + numOfCalls);
                    fromDay = DateUtils.getNextDay(fromDay);
                }

                if (!report.isEmpty()) {
                    for (int i = report.size() - 1; i >= 0; i--) {
                        out.println(report.get(i) + "<br/>");
                    }
                }

            } catch (ParseException ex) {
                Logger.getLogger(ServiceLogAnalyticsServlet.class.getName()).log(Level.SEVERE, null, ex);
            }

            out.println("<br/>Task executed<br/><br/><a href=\"index.jsp\">Back</a>");
            out.println("</body>");
            out.println("</html>");

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
