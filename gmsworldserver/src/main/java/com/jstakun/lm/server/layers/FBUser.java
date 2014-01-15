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
public class FBUser {
        @Facebook("uid")
        Long uid;
        @Facebook("name")
        String name;
}
