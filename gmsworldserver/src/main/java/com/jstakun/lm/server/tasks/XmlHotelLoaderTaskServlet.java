/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.tasks;

import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.xml.HotelParser;
import com.jstakun.lm.server.utils.xml.ParserManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.InputSource;

/**
 *
 * @author jstakun
 */
public class XmlHotelLoaderTaskServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(XmlHotelLoaderTaskServlet.class.getName());

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
        InputStream is = null;
        ZipFile zipFile = null;

        try {
            ServletContext context = getServletContext();
            String file = request.getParameter("file");
            int modeVal = NumberUtils.getInt(request.getParameter("mode"), HotelParser.UPDATE);
            logger.log(Level.INFO, "Calling hotels loading task for file: {0}.", file);
            if (StringUtils.isNotEmpty(file)) {
                HotelParser parser = new HotelParser(modeVal);
                ParserManager pm = new ParserManager(parser);
                if (file.endsWith(".xml")) {
                    is = context.getResourceAsStream("/WEB-INF/hotels/" + file);
                } else if (file.endsWith(".zip")) {
                    zipFile = new ZipFile("WEB-INF/hotels/" + file);
                    String entry = file.replace("zip", "xml");
                    ZipEntry zipEntry = zipFile.getEntry(entry);
                    is = zipFile.getInputStream(zipEntry);
                }

                if (is != null) {
                    Reader reader = new InputStreamReader(new BOMInputStream(is), "UTF-8");
                    InputSource inputSource = new InputSource(reader);
                    inputSource.setEncoding("UTF-8");
                    pm.parseInputSource(inputSource);
                } else {
                    logger.log(Level.INFO, "File: {0} doesn't exists.", file);
                }

                int[] counters = parser.getCounters();
                logger.log(Level.INFO, "Loaded {0} hotels.", counters[HotelParser.TOTAL]);
                logger.log(Level.INFO, "{0} hotels created, {1} hotels updated, {2} hotels not changed.",
                        new Object[]{counters[HotelParser.CREATE], counters[HotelParser.UPDATE], counters[HotelParser.NONE]});
            } else {
                logger.log(Level.INFO, "No file specified.");
                out.println("No file specified.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (is != null) {
                is.close();
            }
            if (zipFile != null) {
            	zipFile.close();
            }
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
        return "Xml Hotel Loader Task Servlet";
    }// </editor-fold>
}
