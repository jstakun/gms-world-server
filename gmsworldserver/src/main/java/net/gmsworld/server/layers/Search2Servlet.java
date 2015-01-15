package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.utils.GoogleThreadProvider;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;

/**
 *
 * @author jstakun
 */
public class Search2Servlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Search2Servlet.class.getName());
    private double latitude, longitude;
    private String query, ftoken;
    private int radius, limit, stringLimit, dealLimit;
    private List<ExtendedLandmark> foundLandmarks;
    private Locale locale;
    private JSONObject jsonResponse;
    
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LayerHelperFactory.setCacheProvider(new GoogleCacheProvider());
        LayerHelperFactory.setThreadProvider(new GoogleThreadProvider());
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
        String format = StringUtil.getStringParam(request.getParameter("format"), "json");
        PrintWriter out = null;
        
        if (format.equals("json")) {
        	response.setContentType("text/json;charset=UTF-8");
        	out = response.getWriter();
        } else if (format.equals("bin")) {
        	response.setContentType("deflate");        
        }
        
        try {
            if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius", "query")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                latitude = GeocodeUtils.getLatitude(request.getParameter("lat"));
                longitude = GeocodeUtils.getLongitude(request.getParameter("lng"));
                query = URLDecoder.decode(request.getParameter("query"), "utf-8");
                ftoken = request.getParameter("ftoken");
                radius = NumberUtils.getRadius(request.getParameter("radius"), 10, 6371);
                dealLimit = NumberUtils.getInt(request.getParameter("dealLimit"), 300);
                limit = NumberUtils.getInt(request.getParameter("limit"), 30);
                stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));
                int version = NumberUtils.getVersion(request.getParameter("version"), 1);
                String flexString = "0";
                if (StringUtils.isNotEmpty(request.getParameter("deals"))) {
                    flexString = "1";
                }
                flexString += "_" + NumberUtils.getInt(request.getParameter("geocode"),0);
                flexString += "_" + dealLimit;
                
                locale = request.getLocale();
                
                if (format.equals("json")) { 
                	jsonResponse = LayerHelperFactory.getSearchUtils().processRequest(latitude, longitude, query, radius, version, limit, stringLimit, flexString, ftoken, locale);
                } else {
                	foundLandmarks = LayerHelperFactory.getSearchUtils().processBinaryRequest(latitude, longitude, query, radius, version, limit, stringLimit, flexString, ftoken, locale, true);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (format.equals("json")) {
        		try {
        			out.print(jsonResponse);
        		} catch (Exception e) {
        			logger.log(Level.SEVERE, e.getMessage(), e);
        		}
        		out.close();
        	} else if (format.equals("bin")) {
            	LayerHelperFactory.getSearchUtils().serialize(foundLandmarks, response.getOutputStream(), 12);
        	}
        }
    }

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
        return "Search2 servlet";
    }
}
