/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.layers;

import java.util.List;
import com.restfb.Facebook;

/**
 *
 * @author jstakun
 */
public class FBMultiQueryResults {
        @Facebook
        List<FBCheckin> checkins;
        @Facebook
        List<FBUser> users;
        @Facebook
        List<FBPlace> places;
        @Facebook
        List<FBPhoto> photos;
}