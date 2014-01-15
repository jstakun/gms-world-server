/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.persistence;

import java.util.Date;

import javax.jdo.annotations.Extension;
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
@PersistenceCapable(identityType=IdentityType.APPLICATION)
public class ServiceLog {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private Date creationDate;

  @Persistent
  @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private String username;

  @Persistent
  @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private String serviceUri;

  @Persistent
  @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private boolean auth;

  @Persistent
  private int appId;

  public ServiceLog(String username, String serviceUri, boolean auth, int appId)
  {
	  this();
      this.username = username;
      this.serviceUri = serviceUri;
      this.auth = auth;
      this.appId = appId;
  }

  public ServiceLog() {
	  this.creationDate = new Date(System.currentTimeMillis());
  }
}
