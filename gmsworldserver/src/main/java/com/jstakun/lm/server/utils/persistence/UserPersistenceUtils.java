/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.persistence;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import javax.jdo.Query;
import com.jstakun.lm.server.persistence.PMF;
import com.jstakun.lm.server.persistence.User;
import com.jstakun.lm.server.personalization.RapleafUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class UserPersistenceUtils {

    private static final Logger logger = Logger.getLogger(UserPersistenceUtils.class.getName());

    public static String persistUser(String login, String password, String email, String firstname, String lastname) {
        User user = new User(login, password, email, firstname, lastname);
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            String personalInfo = RapleafUtil.readUserInfo(email, firstname, lastname);
            if (StringUtils.isNotEmpty(personalInfo)) {
                if (personalInfo.length() < 500) {
                    user.setPersonalInfo(personalInfo);
                } else {
                    user.setPersonalInfo("{}");
                    user.setPersonalInfoLong(new Text(personalInfo));
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        try {
            user = pm.makePersistent(user);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return KeyFactory.keyToString(user.getKey());
    }

    public static User selectUserByKey(String k) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        User user = null;

        try {
            Key key = KeyFactory.stringToKey(k);
            user = pm.getObjectById(User.class, key);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return user;
    }

    public static User selectUserByLogin(String username) {
        User user = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Query query = pm.newQuery(User.class, "login == username");
            query.setUnique(true);
            query.declareParameters("String username");
            user = (User) query.execute(username);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return user;
    }

    public static boolean confirmUserRegistration(String k, Boolean confirmation) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        User user = selectUserByKey(k);
        boolean result = false;

        try {
            if (user != null) {
                user.setConfirmed(confirmation);
                user.setConfirmDate(new Date());
                pm.makePersistent(user);
                result = true;
            } else {
                logger.log(Level.INFO, "User with key: {0} is null!. User wanted to confirm his account: {1}",
                        new Object[]{k, confirmation});
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return result;
    }

    public static void setLastLogonDate(User user) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            if (user != null) {
                user.setLastLogonDate(new Date());
                pm.makePersistent(user);
            } 
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
    }

    public static boolean userExists(String username) {
        int result = 0;
        try {
            DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query("User");
            query.setKeysOnly();
            Filter loginFilter =  new FilterPredicate("login", FilterOperator.EQUAL, username);
            query.setFilter(loginFilter);
            //query.addFilter("login", FilterOperator.EQUAL, username);
            PreparedQuery pq = ds.prepare(query);
            FetchOptions option = FetchOptions.Builder.withLimit(1);
            result = pq.countEntities(option);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return (result > 0);
    }

    //No need to migrate
    public static void setPersonalInfo(String k) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        User user = selectUserByKey(k);

        try {
            if (user != null) {
                String personalInfo = RapleafUtil.readUserInfo(user.getEmail(), user.getFirstname(), user.getLastname());
                if (StringUtils.isNotEmpty(personalInfo)) {
                    if (personalInfo.length() < 500) {
                        user.setPersonalInfo(personalInfo);
                    } else {
                        user.setPersonalInfo("{}");
                        user.setPersonalInfoLong(new Text(personalInfo));
                    }
                    pm.makePersistent(user);
                }
            } else {
                logger.log(Level.INFO, "User with key: {0} is null!.",
                        new Object[]{k});
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
    }

    //No need to migrate
    public static List<User> selectUsers(int first, int last) {
        List<User> results = new ArrayList<User>();
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Query query = pm.newQuery(User.class);
            query.setRange(first, last);
            query.setOrdering("regDate asc");

            results = (List<User>) query.execute();
            //results = (List<User>) pm.detachCopyAll(results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }
}
