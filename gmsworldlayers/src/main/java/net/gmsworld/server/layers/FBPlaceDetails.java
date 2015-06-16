package net.gmsworld.server.layers;

import com.restfb.Facebook;

/**
 *
 * @author jstakun
 */
public class FBPlaceDetails {

    //page_id, pic, website, phone, description

    @Facebook("page_id")
    String objectId;
    @Facebook("pic")
    String pic;
    @Facebook("website")
    String website;
    @Facebook("phone")
    String phone;
    @Facebook("description")
    String desc;
    
}
