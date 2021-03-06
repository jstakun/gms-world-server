package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

import com.jstakun.lm.server.persistence.Layer;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils;
import com.jstakun.lm.server.utils.xml.XMLUtils;

/**
 *
 * @author jstakun
 */
public class ListLayersServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ListLayersServlet.class.getName());
    private static final String XML_KEY = "customXmlLayersList";
    private static final String JSON_LAYER_LIST = "jsonLayersList";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String format = StringUtil.getStringParam(request.getParameter("format"), "xml");
        if (format.equals("json")) {
            response.setContentType("text/json;charset=UTF-8");
        } else if (format.equals("xml")) {
            response.setContentType("text/xml;charset=UTF-8");
        }

        PrintWriter out = response.getWriter();

        Double latitudeMin = GeocodeUtils.getLatitude(request.getParameter("latitudeMin"));
        Double longitudeMin = GeocodeUtils.getLongitude(request.getParameter("longitudeMin"));
        Double latitudeMax = GeocodeUtils.getLatitude(request.getParameter("latitudeMax"));
        Double longitudeMax = GeocodeUtils.getLongitude(request.getParameter("longitudeMax"));
        int version = NumberUtils.getVersion(request.getParameter("version"), 1);
        int radius = NumberUtils.getRadius(request.getParameter("radius"), 3, 6371);

        if (format.equals("json") && latitudeMin != null && longitudeMin != null) {
            String key = JSON_LAYER_LIST + "_" + StringUtil.formatCoordE2(latitudeMin) + "_" + StringUtil.formatCoordE2(longitudeMin) + "_" + radius;

            if (version == 1 && latitudeMax != null && longitudeMax != null) {
                key += "_" + StringUtil.formatCoordE2(latitudeMax) + "_" + StringUtil.formatCoordE2(longitudeMax);
            }
            String json = CacheUtil.getString(key);
            if (json == null) {
                List<Layer> layerList = LayerPersistenceUtils.listAllLayers(2);
                if (layerList != null) {
                	try {
                    	if (version == 1) {
                        	json = LayerPersistenceUtils.createCustomJSonLayersList(layerList, latitudeMin, longitudeMin, latitudeMax, longitudeMax);
                    	} else {
                        	json = LayerPersistenceUtils.createCustomJSonLayersList(layerList, latitudeMin, longitudeMin, radius * 1000);
                    	}
                    	if (json != null) {
                        	CacheUtil.put(key, json, CacheType.NORMAL);
                        	logger.log(Level.INFO, "Adding layer landmark to cache with key {0}", key);
                    	}
                	} 	catch (Exception e) {
                    	logger.log(Level.SEVERE, e.getMessage(), e);
                	}
                }
            } else {
                logger.log(Level.INFO, "Reading layer list from cache with key {0}", key);
            }
            if (json != null) {
            	//logger.log(Level.INFO, json);
                out.print(json);
            } else {
                out.print("{ResultSet:[]}");
            }
        } else if (format.equals("xml")) {
            String xml = CacheUtil.getString(XML_KEY);
            if (xml == null) {
                try {
                    List<Layer> layerList = LayerPersistenceUtils.listAllLayers(1);
                    if (layerList != null && !layerList.isEmpty()) {
                    	xml = XMLUtils.createCustomXmlLayersList(layerList);
                    	CacheUtil.put(XML_KEY, xml, CacheType.NORMAL);
                    } else {
                    	xml = "<layers/>";
                    }               
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            out.print(xml);
        }
        out.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "List Layers Servlet";
    }
}
