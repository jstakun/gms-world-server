/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.struts;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 *
 * @author jstakun
 */
public class LayerForm extends DynaActionForm {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public void reset(ActionMapping mapping, HttpServletRequest request)
    {
      super.reset(mapping, request);
    }

}
