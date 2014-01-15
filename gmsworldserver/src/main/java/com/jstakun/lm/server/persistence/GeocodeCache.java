/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.persistence;

import com.google.appengine.api.datastore.Key;

import java.io.Serializable;
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
public class GeocodeCache implements Serializable {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private Date creationDate;

  @Persistent
  private double latitude;

  @Persistent
  private double longitude;

  @Persistent
  private int status;

  @Persistent
  private String message;

  @Persistent
  private String location;

  public GeocodeCache(String location, int status, String message, double latitude, double longitude)
  {
	  this();
      this.location = location;
      this.status = status;
      this.message = message;
      this.latitude = latitude;
      this.longitude = longitude;    
  }

  public GeocodeCache() {
	  this.creationDate = new Date(System.currentTimeMillis());
  }
  
  public double getLatitude()
  {
     return latitude;
  }

  public double getLongitude()
  {
     return longitude;
  }

  public Date getCreationDate()
  {
      return creationDate;
  }

  public String getLocation()
  {
      return location;
  }

  public Key getKey() {
    return key;
  }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

}
