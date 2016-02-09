package com.jstakun.lm.server.struts;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;

/**
 *
 * @author jstakun
 */
public class CreateLandmarkAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException,
                                                                      ServletException {
        DynaActionForm landmarkForm = (DynaActionForm) form;
        
        Landmark l = new Landmark();
        
        l.setName((String)landmarkForm.get("name"));
        l.setDescription((String)landmarkForm.get("description"));
        l.setLatitude(((Double)landmarkForm.get("latitude")).doubleValue());
        l.setLongitude(((Double)landmarkForm.get("longitude")).doubleValue());
        l.setUsername((String)landmarkForm.get("createdBy"));
        l.setValidityDate(new Date(((Timestamp)landmarkForm.get("validityDate")).getTime()));
        l.setLayer((String)landmarkForm.get("layer"));

        LandmarkPersistenceUtils.persistLandmark(l, GoogleCacheProvider.getInstance());      
        
        return mapping.findForward( "success");
    }

}
