package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
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
    private List<ExtendedLandmark> foundLandmarks = new ArrayList<ExtendedLandmark>();
    private Locale locale;
    private JSONObject jsonResponse;
    
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LayerHelperFactory.getInstance().setCacheProvider(GoogleCacheProvider.getInstance());
        LayerHelperFactory.getInstance().setThreadProvider(new GoogleThreadProvider());
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
        int version = NumberUtils.getVersion(request.getParameter("version"), 1);
        PrintWriter out = null;
        
        if (format.equals("json")) {
        	response.setContentType("text/json;charset=UTF-8");
        	out = response.getWriter();
        } else if (format.equals("bin")) {
        	if (version >= 12) {
        		response.setContentType("deflate");
        	} else {
        		//version = 11; //this will use only serialization
        		response.setContentType("application/x-java-serialized-object"); 
        	}
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
                String flexString = "0";
                if (StringUtils.isNotEmpty(request.getParameter("deals"))) {
                    flexString = "1";
                }
                flexString += "_" + NumberUtils.getInt(request.getParameter("geocode"),0);
                flexString += "_" + dealLimit;
                
                locale = request.getLocale();
                
                logger.log(Level.INFO, "Searching for " + query);
                
                if (format.equals("json")) { 
                	jsonResponse = ((SearchUtils)LayerHelperFactory.getInstance().getByName(Commons.SEARCH_LAYER)).processRequest(latitude, longitude, query, radius, version, limit, stringLimit, flexString, ftoken, locale);
                } else {
                	foundLandmarks = LayerHelperFactory.getInstance().getByName(Commons.SEARCH_LAYER).processBinaryRequest(latitude, longitude, query, radius, version, limit, stringLimit, flexString, ftoken, locale, false);
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
        		LayerHelperFactory.getInstance().getByName(Commons.SEARCH_LAYER).serialize(foundLandmarks, response.getOutputStream(), version);
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
