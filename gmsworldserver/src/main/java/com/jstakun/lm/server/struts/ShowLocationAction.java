package com.jstakun.lm.server.struts;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.utils.HtmlUtils;
import com.jstakun.lm.server.utils.UserAgentUtils;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.openlapi.AddressInfo;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.layers.GeocodeHelperFactory;
import net.gmsworld.server.layers.HotelsBookingUtils;
import net.gmsworld.server.layers.LayerHelperFactory;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;

/**
 *
 * @author jstakun
 */
public class ShowLocationAction extends org.apache.struts.action.Action {

    private static final Logger logger = Logger.getLogger(ShowLocationAction.class.getName());
    private static final int HOTELS_LIMIT = 500;
	private static final int RADIUS = 50;

    public ShowLocationAction() {
    	super();
    	GeocodeHelperFactory.setCacheProvider(GoogleCacheProvider.getInstance());
    }
    
    /**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Double lat = null, lng = null;
        boolean isMobile = UserAgentUtils.isMobile(request.getHeader("User-Agent"));
        
        if (!HttpUtils.isEmptyAny(request, "lat", "lon") || !HttpUtils.isEmptyAny(request, "latitudeEnc", "longitudeEnc")) {
            try {
            	lat = HtmlUtils.decodeDouble(request.getParameter("latitudeEnc"));
            	if (lat == null) {
            	 	lat = Double.parseDouble(request.getParameter("lat"));
            	}           	 
            	lng = HtmlUtils.decodeDouble(request.getParameter("longitudeEnc"));
            	if (lng == null) {
            	 	lng = Double.parseDouble(request.getParameter("lon"));
            	}
                request.setAttribute("lat", StringUtil.formatCoordE6(lat));
                request.setAttribute("lng", StringUtil.formatCoordE6(lng));
               
                String zoom = request.getParameter("zoom");
                if (zoom != null) {
                	try {
                		request.setAttribute("zoom", Integer.parseInt(zoom));
                	} catch (Exception e) {
                		
                	}
                }
                
                String address = null;
                if (!StringUtils.equals(request.getParameter("enabled"), "Hotels")) {
                	AddressInfo addressInfo = GeocodeHelperFactory.processReverseGeocodeAddress(lat, lng);
                	address = addressInfo.getField(AddressInfo.EXTENSION);
                	if (StringUtils.isNotEmpty(address)) {
                		request.setAttribute("address", address);
                	}
                }
                
                String landmarkName = request.getParameter("landmarkName");
            	if (StringUtils.isBlank(landmarkName)) {
            		landmarkName = "Selected location";
            	} 
                request.setAttribute("landmarkName", "'" + landmarkName + "'");
                
                request.setAttribute("landmarkDesc", HtmlUtils.buildLocationDescV2(lat, lng, landmarkName, address, request.getLocale(), isMobile));
            	
            	String[] checkinOptions = StringUtils.split(request.getParameter("checkinOptions"),",");
            	if (checkinOptions != null && checkinOptions.length > 2) {
            		request.setAttribute("checkinAdults", Integer.parseInt(checkinOptions[0]));
            		int checkinChildren = Integer.parseInt(checkinOptions[1]);
            		request.setAttribute("checkinChildren", checkinChildren);
            		String checkinChildrenAges = "";
            		for (int i=0;i<checkinChildren;i++) {
            			checkinChildrenAges += checkinOptions[i+2] + ",";	
            		}
            		for (int i=checkinChildren;i<10;i++) {
            			checkinChildrenAges += "0,";
            		}
            		request.setAttribute("checkinChildrenAges", checkinChildrenAges);
            		request.setAttribute("checkinRooms", Integer.parseInt(checkinOptions[checkinOptions.length-1]));
            	} else {
            		request.setAttribute("checkinAdults", 1);
            		request.setAttribute("checkinChildren", 0);
            		request.setAttribute("checkinRooms", 1);
            	}
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        if (StringUtils.isNotEmpty(request.getParameter("fullScreen")) && lat != null && lng != null) {
        	//load hotels layer in asynchronous mode 
        	if (StringUtils.contains(request.getParameter("enabled"), "Hotels") && ! UserAgentUtils.isBot(request.getHeader("User-Agent"))) {
				((HotelsBookingUtils)LayerHelperFactory.getInstance().getByName(Commons.HOTELS_LAYER)).loadHotelsAsync(lat, lng, RADIUS, HOTELS_LIMIT, request.getParameter("sortType"), true); 
			}
			
        	if (isMobile) {
        		return mapping.findForward("landmarksMobile");
        	} else {
        		return mapping.findForward("landmarks");
        	}
        } else {
        	if (isMobile) {
        		return mapping.findForward("mobile");
        	} else {
        		return mapping.findForward("success");
        	}
        }
    }
}
