package com.jstakun.lm.server.struts;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.bitwalker.useragentutils.OperatingSystem;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

public class WelcomeAction extends org.apache.struts.action.Action {

	@Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		List<Landmark> landmarkList = (List<Landmark>)CacheUtil.getObject("newestLandmarks");
        if (landmarkList == null) {
            landmarkList = LandmarkPersistenceUtils.selectNewestLandmarks();
            CacheUtil.putToFastCache("newestLandmarks", landmarkList);
        }
        request.setAttribute("newestLandmarkList", landmarkList);        
		
		OperatingSystem os = OperatingSystem.parseUserAgentString(request.getHeader("User-Agent"));
        if (os.isMobileDevice()) {
            return mapping.findForward("mobile");
        } else {
            return mapping.findForward("success");
        }
	}
}
