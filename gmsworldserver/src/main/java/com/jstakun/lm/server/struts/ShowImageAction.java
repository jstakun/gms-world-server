package com.jstakun.lm.server.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import net.gmsworld.server.layers.GeocodeHelperFactory;

import com.jstakun.lm.server.persistence.Screenshot;
import com.jstakun.lm.server.utils.FileUtils;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.openlapi.AddressInfo;

import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;

/**
 *
 * @author jstakun
 */
public class ShowImageAction extends org.apache.struts.action.Action {

	public ShowImageAction() {
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

        final String key = (String) request.getParameter("key");

        Screenshot s = FileUtils.getScreenshot(key, false);
        
        if (s != null) {
        	AddressInfo ai = GeocodeHelperFactory.processReverseGeocode(s.getLatitude(),s.getLongitude());
        	if (ai != null) {
        		String address = ai.getField(AddressInfo.EXTENSION);
        		if (StringUtils.isNotEmpty(address)) {
        			request.setAttribute("address", address);
        		}
        	}
            request.setAttribute("screenshot", s);
        }
        
        OperatingSystem os = OperatingSystem.parseUserAgentString(request.getHeader("User-Agent"));
        if (os.getDeviceType().equals(DeviceType.MOBILE)) {
            return mapping.findForward("mobile");
        } else {
            return mapping.findForward("success");
        }
    }
}
