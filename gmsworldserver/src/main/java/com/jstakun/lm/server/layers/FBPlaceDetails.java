/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.layers;

import com.restfb.Facebook;

/**
 *
 * @author jstakun
 */
public class FBPlaceDetails {

    //page_id, pic_small, website, phone, description

    @Facebook("page_id")
    String objectId;
    @Facebook("pic_small")
    String picSmall;
    @Facebook("website")
    String website;
    @Facebook("phone")
    String phone;
    @Facebook("description")
    String desc;
    
}
