/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.persistence;

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
public class Config {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key keycode;

  @Persistent
  private String key;

  @Persistent
  private String value;

  public String getKey()
  {
      return key;
  }

  public String getValue()
  {
      return value;
  }

  public Config(String key, String value)
  {
       this.key = key;
       this.value = value;
  }
  
  public Config() {
	  
  }

}
