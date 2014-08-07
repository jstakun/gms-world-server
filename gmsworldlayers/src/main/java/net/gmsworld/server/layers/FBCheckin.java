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
public class FBCheckin {

    @Facebook("author_uid")
    Long userId;
    @Facebook("target_id")
    String targetId;
    @Facebook("timestamp")
    Long timestamp;
}
