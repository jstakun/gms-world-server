/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.oauth;

import com.google.gdata.client.authn.oauth.OAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthUtil;
import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jstakun
 */
public class CommonUtils {

    public static String[] userPass(String authorization) {

        if (authorization != null) {
            String userPass = "";

            try {
                userPass = new String(Base64.decode(authorization));
            } catch (Base64DecoderException ex) {
                Logger.getLogger(CommonUtils.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }

            // The decoded string is in the form
            // "userID:password".

            int p = userPass.indexOf(":");
            if (p != -1) {
                String userId = userPass.substring(0, p);
                String password = userPass.substring(p + 1);
                return new String[]{userId, password};
            }
        }

        return null;
    }

    public static String buildAuthHeaderString(OAuthParameters params) {
        StringBuilder buffer = new StringBuilder();
        int cnt = 0;
        buffer.append("OAuth ");
        Map<String, String> paramMap = params.getBaseParameters();
        Object[] paramNames = paramMap.keySet().toArray();
        for (Object paramName : paramNames) {
            String value = paramMap.get((String) paramName);
            buffer.append(paramName).append("=\"").append(OAuthUtil.encode(value)).append("\"");
            cnt++;
            if (paramNames.length > cnt) {
                buffer.append(",");
            }

        }
        return buffer.toString();
    }
}
