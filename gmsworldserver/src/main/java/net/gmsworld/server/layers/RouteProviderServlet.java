/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gmsworld.server.layers;

import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;

import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class RouteProviderServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(RouteProviderServlet.class.getName());

	
	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        GeocodeHelperFactory.setCacheProvider(new GoogleCacheProvider());
    }
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            if (HttpUtils.isEmptyAny(request, "lat_start", "lng_start", "lat_end", "lng_end", "type", "username")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String username = request.getParameter("username");
                String type = request.getParameter("type");
                double lat_start = NumberUtils.getDouble(request.getParameter("lat_start"), 0d);
                double lng_start = NumberUtils.getDouble(request.getParameter("lng_start"), 0d);
                double lat_end = NumberUtils.getDouble(request.getParameter("lat_end"), 0d);
                double lng_end = NumberUtils.getDouble(request.getParameter("lng_end"), 0d);

                JSONObject output = GeocodeHelperFactory.getMapQuestUtils().getRoute(lat_start, lng_start, lat_end, lng_end, type, username);

                if (output == null) {
                  	response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);                
                } else {
                    out.print(output.toString());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
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
        return "Routes provider";
    }// </editor-fold>
}
