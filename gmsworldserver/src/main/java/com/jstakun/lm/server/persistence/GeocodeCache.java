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

  private String location;
  
  public GeocodeCache(String location, int id, double latitude, double longitude)
  {
	  this();
      this.setLocation(location);
      this.setId(id);
      this.setLatitude(latitude);
      this.setLongitude(longitude);    
  }

  public GeocodeCache() {
	  this.setCreationDate(new Date(System.currentTimeMillis()));
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

  public void setLocation(String location) {
	this.location = location;
  }

  public void setId(int id) {
	this.id = id;
  }

  public void setLongitude(double longitude) {
	this.longitude = longitude;
  }

  public void setLatitude(double latitude) {
	this.latitude = latitude;
  }

}
