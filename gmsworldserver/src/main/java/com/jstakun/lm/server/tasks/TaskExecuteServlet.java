/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.tasks;

import com.jstakun.lm.server.utils.NumberUtils;
import org.apache.commons.lang.StringUtils;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

/**
 *
 * @author jstakun
 */
public class TaskExecuteServlet extends HttpServlet {

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
            String action = request.getParameter("action");
            String entity = request.getParameter("entity");
            String time = request.getParameter("time");

            if (StringUtils.isNotEmpty(action)) {
                Queue queue = QueueFactory.getDefaultQueue();
                if (action.equalsIgnoreCase("purge") && StringUtils.isNotEmpty(entity)) {
                    if (entity.equalsIgnoreCase("hotel")) {
                        queue.add(withUrl("/tasks/execute").param("entity", "hotel").param("action", "purge"));
                    } else if (entity.equalsIgnoreCase("log")) {
                        queue.add(withUrl("/tasks/execute").param("entity", "log").param("action", "purge"));
                    } else if (entity.equalsIgnoreCase("screenshot")) {
                        queue.add(withUrl("/tasks/execute").param("entity", "screenshot").param("action", "purge"));
                    }
                } else if (action.equalsIgnoreCase("geocells") && StringUtils.isNotEmpty(entity)) {
                    if (entity.equalsIgnoreCase("hotel")) {
                        int deadline = NumberUtils.getInt(time, 5);
                        queue.add(withUrl("/tasks/execute").param("action", "geocells").param("entity", "hotel").param("time", Integer.toString(deadline)));
                    } else if (entity.equalsIgnoreCase("landmark")) {
                        queue.add(withUrl("/tasks/execute").param("action", "geocells").param("entity", "landmark"));
                    }
                } else if (action.equalsIgnoreCase("emailing")) {
                    queue.add(withUrl("/tasks/emailingTask"));
                } else if (action.equalsIgnoreCase("personalize")) {
                	queue.add(withUrl("/tasks/personalizeTask").param("first", request.getParameter("first")).param("last", request.getParameter("last")));
                } else if (action.equalsIgnoreCase("filter")) {
                    String filterProperty = request.getParameter("filterProperty");
                    String pattern = request.getParameter("pattern");
                    String resultProperty = request.getParameter("resultProperty");
                    if (StringUtils.isEmpty(resultProperty)) {
                        resultProperty = filterProperty;
                    }
                    String forDays = request.getParameter("forDays");
                    if (StringUtils.isEmpty(forDays)) {
                        forDays = "0";
                    }
                    queue.add(withUrl("/tasks/landmarkFilterTask").param("filterProperty", filterProperty).param("pattern", pattern).param("resultProperty", resultProperty).param("forDays", forDays));
                }
            }
            out.println("Task executed.<br/><br/><a href=\"index.jsp\">Back</a>");
        } catch (Exception e) {
            out.println("Task execution exception: " + e.getMessage() + ".<br/><br/><a href=\"index.jsp\">Back</a>");
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
