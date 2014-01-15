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
import com.google.appengine.api.datastore.Text;

/**
 *
 * @author jstakun
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class User {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private String login;

  @Persistent
  private String password;

  @Persistent
  private String email;

  @Persistent
  private String firstname;

  @Persistent
  private String lastname;

  @Persistent
  private Date regDate;

  @Persistent
  private Boolean confirmed;

  @Persistent
  private Date confirmDate;

  @Persistent
  private String personalInfo;

  @Persistent
  private Text personalInfoLong;

  @Persistent
  private Date lastLogonDate;

  public User(String login, String password, String email, String firstname, String lastname)
  {
	  this();
      this.email = email;
      this.login = login;
      this.password = password;
      this.firstname = firstname;
      this.lastname = lastname;
      this.confirmed = false;
  }
  
  public User() {
	  this.regDate = new Date(System.currentTimeMillis());
  }

  public Key getKey() {
    return key;
  }

  public String getPassword()
  {
      return password;
  }

    /**
     * @return the confirmed
     */
    public Boolean getConfirmed() {
        return confirmed;
    }

    /**
     * @param confirmed the confirmed to set
     */
    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    /**
     * @param confirmDate the confirmDate to set
     */
    public void setConfirmDate(Date confirmDate) {
        this.confirmDate = confirmDate;
    }

    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param personalInfo the personalInfo to set
     */
    public void setPersonalInfo(String personalInfo) {
        this.personalInfo = personalInfo;
    }

    /**
     * @return the personalInfo
     */
    public String getPersonalInfo() {
        return personalInfo;
    }

    /**
     * @return the firstname
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * @return the lastname
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * @return the personalInfoLong
     */
    public Text getPersonalInfoLong() {
        return personalInfoLong;
    }

    /**
     * @param personalInfoLong the personalInfoLong to set
     */
    public void setPersonalInfoLong(Text personalInfoLong) {
        this.personalInfoLong = personalInfoLong;
    }

    /**
     * @param lastLogonDate the lastLogonDate to set
     */
    public void setLastLogonDate(Date lastLogonDate) {
        this.lastLogonDate = lastLogonDate;
    }
}
