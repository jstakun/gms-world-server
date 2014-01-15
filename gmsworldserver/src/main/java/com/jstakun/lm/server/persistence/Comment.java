/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.persistence;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 *
 * @author jstakun
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class Comment {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private Date creationDate;

  @Persistent
  private String landmarkKey;

  @Persistent
  private String message;

  @Persistent
  private String username;

  public Comment(String username, String landmarkKey, String message) {
     this();
	 this.landmarkKey = landmarkKey;
     this.message = message;
     this.username = username;
  }
  
  public Comment() {
	  this.creationDate = new Date(System.currentTimeMillis()); 
  }

  public Date getCreationDate()
  {
      return creationDate;
  }

  public String getUsername()
  {
      return username;
  }

  public String getMessage() {
      return message;
  }
}
