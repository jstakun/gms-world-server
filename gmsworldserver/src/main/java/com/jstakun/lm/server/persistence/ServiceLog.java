package com.jstakun.lm.server.persistence;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.datanucleus.api.jpa.annotations.Extension;

import com.google.appengine.api.datastore.Key;

/**
 *
 * @author jstakun
 */
@Entity
public class ServiceLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Key key;

  private Date creationDate;

  @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private String username;

  @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private String serviceUri;

  @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private boolean auth;

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
