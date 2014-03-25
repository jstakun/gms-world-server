/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.persistence;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {

  private static final long serialVersionUID = 1L;
  private Date creationDate;
  private String landmarkKey;
  private String message;
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
  
  public void setCreationDate(Date creationDate) {
	  this.creationDate = creationDate;
  }
  
  public void setUsername(String username) {
	  this.username = username;
  }
  
  public void setMessage(String message) {
	  this.message = message;
  }
}
