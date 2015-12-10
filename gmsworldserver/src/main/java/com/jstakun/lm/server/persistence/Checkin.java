package com.jstakun.lm.server.persistence;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author jstakun
 */
public class Checkin implements Serializable {
  private static final long serialVersionUID = 1L;
  private Date creationDate;
  private String username;
  //private String landmarkKey;
  //private Integer type; //0 qr, 1 web

  public Checkin(String username, String landmarkKey, Integer type)
  {
	  this();
      this.username = username;
      //this.landmarkKey = landmarkKey;
      //this.type = type;
  }
  
  public Checkin() {
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
  
  public void setCreationDate(Date creationDate) {
	  this.creationDate = creationDate;
  }
  
  public void setUsername(String username) {
	  this.username = username;
  }
}
