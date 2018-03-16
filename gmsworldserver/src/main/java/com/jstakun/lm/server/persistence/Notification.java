package com.jstakun.lm.server.persistence;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity

@NamedQueries({
	@NamedQuery(name = "Notification.findById", query = "select n from Notification n where n.id = :id"),
	@NamedQuery(name = "Notification.findAllWithStatus", query = "select n from Notification n where n.status = :status"),
})	

public class Notification {
	
	public enum Status {VERIFIED, UNVERIFIED};

	public static final String NOTIFICATION_FINDBYID= "Notification.findById";
	public static final String NOTIFICATION_FINDALLWITHSTATUS= "Notification.findAllWithStatus";
	
	@Id
	private String id;
	
	@Enumerated(EnumType.STRING)
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
}
