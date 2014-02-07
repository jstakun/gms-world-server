/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.persistence;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author jstakun
 */
public class GeocodeCache implements Serializable {

  private static final long serialVersionUID = 1L;

  private Date creationDate;

  private double latitude;

  private double longitude;

  private int id;

  private String message;

  private String location;
  
  public GeocodeCache(String location, int id, String message, double latitude, double longitude)
  {
	  this();
      this.location = location;
      this.id= id;
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

  public void setCreationDate(Date creationDate) {
      this.creationDate = creationDate;
  }
  
  public int getId() {
	  return id;
  }

}
