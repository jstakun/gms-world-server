package com.jstakun.lm.server.persistence;

import java.util.Date;

public class Notification {
	
	public enum Status {VERIFIED, UNVERIFIED};

	private String id;
	
	private Status status;
	
	private Date lastUpdateDate;
	
	private String secret;

	public String getId() {
		return id;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	
	public Notification(String id, Status status) {
		 this.id = id;
		 this.status = status;
		 this.lastUpdateDate = new Date();
	}
	
	public Notification() {
		this.lastUpdateDate = new Date();
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
