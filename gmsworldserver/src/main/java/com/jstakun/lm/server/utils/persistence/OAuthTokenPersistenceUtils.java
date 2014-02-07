/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.persistence;

import com.jstakun.lm.server.persistence.OAuthToken;
import com.jstakun.lm.server.persistence.PMF;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 *
 * @author jstakun
 */
public class OAuthTokenPersistenceUtils {

    private static final Logger logger = Logger.getLogger(OAuthTokenPersistenceUtils.class.getName());

    public static void persistOAuthToken(String service, String token, String username, String password, String userId) {
        OAuthToken oauth_token = new OAuthToken(service, token, username, password, userId);

        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            pm.makePersistent(oauth_token);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
    }

    public static long countOAuthTokenByUser(String username, String pwd) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        long result = 0;
        try {
            Query query = pm.newQuery(OAuthToken.class);
            query.setFilter("login == username && password == pwd");
            query.declareParameters("String username, String pwd");
            query.setResult("count(this)");
            result = ((Long) query.execute(username, pwd)).longValue();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return result;
    }

     public static List<OAuthToken> selectOAuthTokenByUser(String username) {
        //Logger.getLogger(OAuthTokenPersistenceUtils.class.getName()).log(Level.INFO, "selectOAuthTokenByUser " + username + " " + pwd);
        PersistenceManager pm = PMF.get().getPersistenceManager();
        List<OAuthToken> result = new ArrayList<OAuthToken>();
        try {
            Query query = pm.newQuery(OAuthToken.class);
            query.setFilter("login == username");
            query.declareParameters("String username");
            query.setOrdering("creationDate desc");
            result = (List<OAuthToken>) query.execute(username);
            //result = (List<OAuthToken>) pm.detachCopyAll(result);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return result;
    }

    public static OAuthToken selectOAuthTokenByService(String username, String pwd, String svc) {
        //Logger.getLogger(OAuthTokenPersistenceUtils.class.getName()).log(Level.INFO, "selectOAuthTokenByService " + username + " " + pwd + " " + svc);
        PersistenceManager pm = PMF.get().getPersistenceManager();
        OAuthToken token = null;
        try {
            Query query = pm.newQuery(OAuthToken.class);
            query.setFilter("service == svc && login == username && password == pwd");
            query.setOrdering("creationDate desc");
            query.declareParameters("String username, String pwd, String svc");
            List<OAuthToken> result = (List<OAuthToken>) query.execute(username, pwd, svc);
            if (!result.isEmpty()) {
                token = result.get(0);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return token;
    }
}
