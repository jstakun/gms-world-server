/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.struts;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.jstakun.lm.server.layers.CloudmadeUtils;
import com.jstakun.lm.server.persistence.Screenshot;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.persistence.ScreenshotPersistenceUtils;

import eu.bitwalker.useragentutils.OperatingSystem;

/**
 *
 * @author jstakun
 */
public class ShowImageAction extends org.apache.struts.action.Action {

    /* forward name="success" path="" */
    private static final Logger logger = Logger.getLogger(ShowImageAction.class.getName());

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
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        final String key = (String) request.getParameter("key");

        if (StringUtils.isNotEmpty(key)) {

            /*Screenshot s = null;
            if (CacheUtil.containsKey(key)) {
            	s = (Screenshot) CacheUtil.getObject(key);
            } else {
            	s = ScreenshotPersistenceUtils.selectScreenshot(key);
            	CacheUtil.put(key, s);
            }*/
        	
        	CacheAction screenshotCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
				@Override
				public Object executeAction() {
					return ScreenshotPersistenceUtils.selectScreenshot(key);
				}
			});
        	Screenshot s = (Screenshot) screenshotCacheAction.getObjectFromCache(key);
            
            if (s != null) {
                String address = CloudmadeUtils.getReverseGeocode(s.getLatitude(),s.getLongitude());
                if (StringUtils.isNotEmpty(address)) {
                    request.setAttribute("address", address);
                }
                try {
                	ImagesService imagesService = ImagesServiceFactory.getImagesService();
                    ServingUrlOptions sou = ServingUrlOptions.Builder.withBlobKey(s.getBlobKey());
                    String imageUrl = imagesService.getServingUrl(sou);
                	request.setAttribute("screenshot", s);
                	request.setAttribute("imageUrl", imageUrl);
                } catch (Exception e) {
                	logger.log(Level.SEVERE, "ShowImageAction.execute() exception:", e);
                }
            } 
        }

        OperatingSystem os = OperatingSystem.parseUserAgentString(request.getHeader("User-Agent"));
        if (os.isMobileDevice()) {
            return mapping.findForward("mobile");
        } else {
            return mapping.findForward("success");
        }
    }
}
