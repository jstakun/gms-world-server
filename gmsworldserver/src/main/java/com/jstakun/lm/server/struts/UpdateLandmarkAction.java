package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

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

      Map<String,Object> update = new HashMap<String,Object>();

      update.put("name", landmarkForm.get("name"));
      update.put("description",landmarkForm.get("description"));
      update.put("latitude",landmarkForm.get("latitude"));
      update.put("longitude",landmarkForm.get("longitude"));
      update.put("creationDate",landmarkForm.get("creationDate"));
      update.put("createdBy",landmarkForm.get("createdBy"));
      update.put("validityDate",new Date(((Timestamp)landmarkForm.get("validityDate")).getTime()));
      update.put("layer",landmarkForm.get("layer"));
      
      //TODO LandmarkPersistenceUtils.updateLandmark((String)landmarkForm.get("key"), update);
  
      return mapping.findForward( "success");
    }
}
