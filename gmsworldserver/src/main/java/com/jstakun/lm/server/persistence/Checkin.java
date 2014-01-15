/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.persistence;

import com.google.appengine.api.datastore.Key;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 *
 * @author jstakun
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class Checkin {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private Date creationDate;

  @Persistent
  private String landmarkKey;

  @Persistent
  private String pin;

  @Persistent
  private String username;

  @Persistent
  private Date pinDate;

  @Persistent
  private String status;

  @Persistent
  private Integer type; //0 qr, 1 web

  public Checkin(String username, String landmarkKey, Integer type)
  {
	  this();
      this.username = username;
      this.landmarkKey = landmarkKey;
      this.type = type;
  }
  
  public Checkin() {
	  this.creationDate = new Date(System.currentTimeMillis());
      this.status = "closed";
  }

  public Date getCreationDate()
  {
      return creationDate;
  }

  public String getUsername()
  {
      return username;
  }
}
