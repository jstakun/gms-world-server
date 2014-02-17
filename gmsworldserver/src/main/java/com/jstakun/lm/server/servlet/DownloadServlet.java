/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

import com.google.appengine.api.datastore.KeyFactory;
import com.jstakun.lm.server.persistence.User;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jstakun
 */
public class DownloadServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int BUFSIZE = 1024;
    private static final String PATH = "./WEB-INF/download/";
    private static final Logger logger = Logger.getLogger(DownloadServlet.class.getName());
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

          //doDownload(request, response);
    	logger.log(Level.SEVERE, "Oops !!! Somebody called " + DownloadServlet.class.getName());
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
        return "Download Servlet";
    }// </editor-fold>

    /*private void doDownload(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        ServletOutputStream op = null;
        DataInputStream in = null;

        try {

            String key = req.getParameter("key");
            User user = null;

            try
            {
                user = UserPersistenceUtils.selectUser(key);
                if (user == null || !KeyFactory.keyToString(user.getKey()).equals(key))
                    throw new Exception("Authentication exception");
            }
            catch (Exception e)
            {
                throw new Exception("Authentication exception");
            }

              

            String file = req.getParameter("file");
            
            String filename = PATH + file;

            File f = new File(filename);

            if (!f.exists())
            {
                throw new Exception("File " + file + " doesn't exists.");
            }

            int length = 0;
            ServletContext context = getServletConfig().getServletContext();
            String mimetype = context.getMimeType(filename);

            byte[] bbuf = new byte[BUFSIZE];
            in = new DataInputStream(new FileInputStream(f));
            op = resp.getOutputStream();
            
            resp.setContentType((mimetype != null) ? mimetype : "application/octet-stream");
            resp.setContentLength((int) f.length());
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + file + "\"");

            while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                op.write(bbuf, 0, length);
            }

            op.flush();
        } catch (Exception e) {
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            try {
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Servlet DownloadServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h2>Download error</h2>" + e.getMessage());
                out.println("</body>");
                out.println("</html>");

            } finally {
                out.close();
            }
        } finally {
            if (in != null) {
                in.close();
            }

            if (op != null) {
                op.close();
            }
        }
    }*/
}
