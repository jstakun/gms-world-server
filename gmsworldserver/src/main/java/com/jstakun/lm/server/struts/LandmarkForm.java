package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils;
import java.sql.Timestamp;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

public class LandmarkForm extends DynaActionForm {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LandmarkForm() {
        super();
    }
    
    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request)
    {
      super.reset(mapping, request);

      //System.out.println("Reseting landmarkForm with key " + request.getParameter("key"));

      if (request.getParameter("key") != null)
      {
            String k = (String)request.getParameter("key");
            Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(k);

            set("name",landmark.getName());
            set("description",landmark.getDescription());
            set("longitude",new Double(landmark.getLongitude()));
            set("latitude",new Double(landmark.getLatitude()));
            set("createdBy",landmark.getUsername());
            set("creationDate",new Timestamp(landmark.getCreationDate().getTime()));
            set("key",k);
            set("layer",landmark.getLayer());
            set("flex", landmark.getFlex());
           
      }
      
      set("layers",LayerPersistenceUtils.selectAllLayers());
      
    }

    @Override
   public void initialize(ActionMapping mapping)
   {
       super.initialize(mapping);

       //System.out.println("Initializing landmarkForm");
   }

}
