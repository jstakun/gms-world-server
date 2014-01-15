/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.personalization;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.utils.HttpUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class RapleafUtil {

    //https://personalize.rapleaf.com/v4/dr?first=John&last=Doe&email=personalize%40example.com&api_key=<key>&show_available
	
    private static final Logger logger = Logger.getLogger(RapleafUtil.class.getName());

    public static String readUserInfo(String email, String firstname, String lastname) throws UnsupportedEncodingException, IOException {
        if (StringUtils.isNotEmpty(email)) {
            StringBuilder sb = new StringBuilder("https://personalize.rapleaf.com/v4/dr?");
            if (StringUtils.isNotEmpty(firstname)) {
                sb.append("first=").append(URLEncoder.encode(firstname, "UTF-8")).append("&");
            }
            if (StringUtils.isNotEmpty(lastname)) {
                sb.append("last=").append(URLEncoder.encode(lastname, "UTF-8")).append("&");
            }
            sb.append("email=").append(URLEncoder.encode(email, "UTF-8")).append("&api_key=").append(Commons.RAPLEAF_API_KEY).append("&show_available");

            URL url = new URL(sb.toString());
            String resp = HttpUtils.processFileRequest(url);

            return resp;
        } else {
            logger.log(Level.WARNING, "Empty email address has been provider for user: {0} {1}", new Object[]{firstname, lastname});
            return null;
        }
    }
}
