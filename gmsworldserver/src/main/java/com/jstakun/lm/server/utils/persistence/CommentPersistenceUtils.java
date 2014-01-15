/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.utils.persistence;

import com.jstakun.lm.server.persistence.Comment;
import com.jstakun.lm.server.persistence.PMF;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 *
 * @author jstakun
 */
public class CommentPersistenceUtils {

    private static final Logger logger = Logger.getLogger(CommentPersistenceUtils.class.getName());

    public static void persistComment(String username, String landmarkKey, String message) {
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            pm.makePersistent(new Comment(username, landmarkKey, message));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
    }

    public static List<Comment> selectCommentsByLandmark(String landmarkKey){
        PersistenceManager pm = PMF.get().getPersistenceManager();
        List<Comment> results = null;

        try {
            Query query = pm.newQuery(Comment.class);
            query.setOrdering("creationDate desc");
            query.setFilter("landmarkKey == lk");
            query.declareParameters("String lk");
            results = (List<Comment>) query.execute(landmarkKey);
            //results = (List<Comment>) pm.detachCopyAll(results);
         } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }
}
