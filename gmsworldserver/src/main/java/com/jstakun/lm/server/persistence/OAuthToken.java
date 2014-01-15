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
public class OAuthToken {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private String service;

  @Persistent
  private String token;

  @Persistent
  private Date creationDate;

  @Persistent
  private String password;

  @Persistent
  private String login;

  @Persistent
  private String serviceUserId;

  public OAuthToken(String service, String token, String username, String password, String userId)
  {
	  this();
      this.token = token;
      this.service = service;
      this.password = password;
      this.login = username;
      this.serviceUserId = userId;
  }
  
  public OAuthToken() {
	  this.creationDate = new Date(System.currentTimeMillis());
  }

  public String getToken()
  {
      return token;
  }

  public String getUserId()
  {
      return serviceUserId;
  }

  public String getService()
  {
      return service;
  }

}
