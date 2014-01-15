/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils;
import java.io.IOException;
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
public class CreateLayerAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException,
                                                                      ServletException {
        DynaActionForm layerForm = (DynaActionForm) form;

        String name = (String)layerForm.get("name");
        String desc = (String)layerForm.get("desc");
        String formatted = (String)layerForm.get("formatted");


        //create layer
        LayerPersistenceUtils.persistLayer(name, desc, true, false, true, formatted);

        return mapping.findForward( "success");
    }

}
