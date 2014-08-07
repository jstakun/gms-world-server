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
public class FBPhoto {
    //object_id, caption, aid, owner, link, created, place_id

    @Facebook("object_id")
    Long objectId;
    @Facebook("caption")
    String caption;
    @Facebook("aid")
    String aid;
    @Facebook("owner")
    String owner;
    @Facebook("link")
    String link;
    @Facebook("created")
    Long created;
    @Facebook("place_id")
    String place_id;
    @Facebook("src_small")
    String src_small;

}
