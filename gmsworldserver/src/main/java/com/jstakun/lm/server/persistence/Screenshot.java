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
public class Screenshot implements Serializable {

    private static final long serialVersionUID = 1L;
    private Date creationDate;
    private String username;
    private double latitude;
    private double longitude;
    private boolean auth;
    private String filename;
    private String url;

    public Screenshot(String username, boolean auth, double latitude, double longitude, Date creationDate) {
        if (creationDate == null) {
            this.setCreationDate(new Date(System.currentTimeMillis()));
        } else {
            this.setCreationDate(creationDate);
        }
        this.username = username;
        this.auth = auth;
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }
    
    public Screenshot() {
    	
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @return the auth
     */
    public boolean isAuth() {
        return auth;
    }

    public String getFilename() {
		return filename;
	}

	public void setFilename(String id) {
		this.filename = id;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
