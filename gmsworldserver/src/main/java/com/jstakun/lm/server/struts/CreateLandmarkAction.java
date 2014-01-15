/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

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
        
        String name = (String)landmarkForm.get("name");
        String description =  (String)landmarkForm.get("description");
        double latitude = ((Double)landmarkForm.get("latitude")).doubleValue();
        double longitude = ((Double)landmarkForm.get("longitude")).doubleValue();
        String username = (String)landmarkForm.get("createdBy");
        Date validityDate = new Date(((Timestamp)landmarkForm.get("validityDate")).getTime());
        String layer = (String)landmarkForm.get("layer");

        LandmarkPersistenceUtils.persistLandmark(name, description, latitude, longitude, 0.0, username, validityDate, layer, null);

        return mapping.findForward( "success");
    }

}
