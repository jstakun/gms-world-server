package com.jstakun.lm.server.persistence;


/**
 *
 * @author jstakun
 */
public class User {
  
  private String login;

  private String password;

  private String email;

  private String firstname;

  private String lastname;

  //private Date regDate;

  private Boolean confirmed;

  //private Date confirmDate;

  private String personalInfo;

  //private Date lastLogonDate;

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
	  //this.regDate = new Date(System.currentTimeMillis());
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
    //public void setConfirmDate(Date confirmDate) {
    //    this.confirmDate = confirmDate;
    //}

    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
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
    
    public void setLogin(String login) {
    	this.login = login;
    }

    /**
     * @param lastLogonDate the lastLogonDate to set
     */
    //public void setLastLogonDate(Date lastLogonDate) {
    //    this.lastLogonDate = lastLogonDate;
    //}
}
