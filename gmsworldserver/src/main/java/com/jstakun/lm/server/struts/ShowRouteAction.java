package com.jstakun.lm.server.struts;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.layers.GeocodeHelperFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;

import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;

/**
 *
 * @author jstakun
 */
public class ShowRouteAction extends org.apache.struts.action.Action {
	
	private static final Logger logger = Logger.getLogger(ShowRouteAction.class.getName());

	public ShowRouteAction() {
		super();
		GeocodeHelperFactory.setCacheProvider(new GoogleCacheProvider());
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

    	try {
			final double lat_start = Double.valueOf(request.getParameter("lat_start")).doubleValue();
			final double lng_start = Double.valueOf(request.getParameter("lng_start")).doubleValue();
			final double lat_end = Double.valueOf(request.getParameter("lat_end")).doubleValue();
			final double lng_end = Double.valueOf(request.getParameter("lng_end")).doubleValue();    	    
			request.setAttribute("routeQueryString", "lat_start=" + lat_start + "&lng_start=" + lng_start + "&lat_end=" + lat_end + "&lng_end=" + lng_end + "&thumbnail=false");
    	} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
		     
        OperatingSystem os = OperatingSystem.parseUserAgentString(request.getHeader("User-Agent"));
        if (os.getDeviceType().equals(DeviceType.MOBILE)) {
            return mapping.findForward("mobile");
        } else {
            return mapping.findForward("success");
        }
    }
}
