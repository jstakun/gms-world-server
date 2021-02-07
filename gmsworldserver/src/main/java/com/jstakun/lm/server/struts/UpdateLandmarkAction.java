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

public class UpdateLandmarkAction extends Action {
    /**This is the main action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException,
                                                                      ServletException {
        
      DynaActionForm landmarkForm = (DynaActionForm) form;
      
      Landmark landmark = new Landmark();
      landmark.setId((int)landmarkForm.get("key"));
      landmark.setName((String)landmarkForm.get("name"));
      landmark.setDescription((String)landmarkForm.get("description"));
      landmark.setLatitude((double)landmarkForm.get("latitude"));
      landmark.setLongitude((double)landmarkForm.get("longitude"));
      landmark.setCreationDate(new Date(((Timestamp)landmarkForm.get("creationDate")).getTime()));
      landmark.setUsername((String)landmarkForm.get("createdBy"));
      landmark.setValidityDate(new Date(((Timestamp)landmarkForm.get("validityDate")).getTime()));
      landmark.setLayer((String)landmarkForm.get("layer"));
      landmark.setFlex((String)landmarkForm.get("flex"));
      
      LandmarkPersistenceUtils.updateLandmark(landmark, GoogleCacheProvider.getInstance());
  
      return mapping.findForward( "success");
    }
}
