/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gmsworld.server.layers;

import com.restfb.Facebook;
/**
 *
 * @author jstakun
 */
public class FBPlace {

    @Facebook("page_id")
    String pageId;
    @Facebook("name")
    String name;
    @Facebook("description")
    String description;
    @Facebook("latitude")
    Double latitude;
    @Facebook("longitude")
    Double longitude;
    @Facebook("display_subtext")
    String displaySubtext;
    @Facebook("checkin_count")
    Long checkinCount;
}
