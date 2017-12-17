package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jstakun.lm.server.utils.xml.KMLParser;
import com.jstakun.lm.server.utils.xml.ParserManager;

import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

/**
 *
 * @author jstakun
 */
public class PanoramioProviderServlet extends HttpServlet {

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
    private static final Logger logger = Logger.getLogger(PanoramioProviderServlet.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	PrintWriter out = response.getWriter();
        try {
        	response.setContentType("text/json;charset=UTF-8");
        	String bbox = request.getParameter("bbox");
            String zoom = request.getParameter("zoom");
            //String max = request.getParameter("max");
            String max = "30";

            String lang = StringUtil.getLanguage(request.getParameter("lang"),"en_US",5);
               
            String panoramioURL = "http://www.panoramio.com/panoramio.kml?LANG=" + lang + ".utf8&BBOX="
                        + bbox + "&zoom=" + zoom + "&max=" + max;

            int version = NumberUtils.getVersion(request.getParameter("version"), 1);

            KMLParser parser = new KMLParser(version);
            ParserManager pm = new ParserManager(parser);
            pm.parseUri(panoramioURL);
            String output = JSONUtils.getJsonArrayObject(parser.getJSonArray());

            out.print(output);        	
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
