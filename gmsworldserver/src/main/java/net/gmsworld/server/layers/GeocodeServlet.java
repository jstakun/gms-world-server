package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.CryptoTools;
import net.gmsworld.server.utils.NumberUtils;

import org.apache.commons.lang.StringUtils;

import com.google.gdata.util.common.util.Base64;


/**
 *
 * @author jstakun
 */
public class GeocodeServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(GeocodeServlet.class.getName());

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
        	String address = request.getParameter("address");
        	
            if (StringUtils.isNotEmpty(address)) {
                String email = request.getParameter("email");
                if (StringUtils.isNotEmpty(email)) {
                    try {
                        email = new String(CryptoTools.decrypt(Base64.decode(email.getBytes())));
                    } catch (Exception e) {
                        //logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
 
                int appId = NumberUtils.getInt(request.getHeader(Commons.APP_HEADER), -1);
                String jsonResp = GeocodeUtils.processRequest(address, email, request.getLocale(), appId, false);

                out.print(jsonResp);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
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
        return "Geocode Proxy Servlet";
    }// </editor-fold>
}
