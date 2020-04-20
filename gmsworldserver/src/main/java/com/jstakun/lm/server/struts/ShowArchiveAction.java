package com.jstakun.lm.server.struts;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.utils.UserAgentUtils;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class ShowArchiveAction extends org.apache.struts.action.Action {

    private static final int INTERVAL = 10;
    private static final String df = "MM-yyyy";
    
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

        String month = request.getParameter("month");
        String year = request.getParameter("year");
        int first = NumberUtils.getInt(request.getParameter("first"), 0);
        int next = -1;
        int prev = -1;

        //mm-yyyy
        String m = null;
        try {
        	if (StringUtils.isNumeric(year) && StringUtils.isNumeric(month)) {
        		month = month +"-" + year;
        	}
            m = DateUtils.getLongMonthYearString(DateUtils.parseDate(df, month));
        } catch (Exception ex) {
            Date now = new Date();
            month = DateUtils.formatDate(df, now);
            m = DateUtils.getLongMonthYearString(now);
        }
        request.setAttribute("month", m);
        
        final String monthStr = month;
        CacheAction landmarkCountCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
			public Object executeAction() {
				if (UserAgentUtils.isBot(request.getHeader("User-Agent"))) {
	            	return 0;
	            } else {
	            	return LandmarkPersistenceUtils.countLandmarksByMonth(monthStr);
	            }
			}
        });
        int count = (Integer) landmarkCountCacheAction.getObjectFromCache(month + "-count", CacheType.NORMAL);

        if (count > 0) {
            if (count - first - INTERVAL > 0) {
            	next = first + INTERVAL;
            }
            if (first - INTERVAL >= 0) {
            	prev = first - INTERVAL;
            }

            request.setAttribute("next", new Integer(next));
            request.setAttribute("prev", new Integer(prev));
 
            CacheAction landmarkCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
    			public Object executeAction() {
    				return LandmarkPersistenceUtils.selectLandmarksByMonth(first, first+INTERVAL, monthStr);
    			}
            });
            List<Landmark> landmarkList = (List<Landmark>) landmarkCacheAction.getObjectFromCache(month + "-" + first + "-" + (first+INTERVAL) + "-list", CacheType.NORMAL);
            request.setAttribute("landmarkList", landmarkList);
        }

        return mapping.findForward("success");
    }
}
