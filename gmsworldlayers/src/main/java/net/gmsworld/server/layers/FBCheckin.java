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
