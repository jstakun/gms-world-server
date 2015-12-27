package com.jstakun.lm.server.persistence;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.datanucleus.api.jpa.annotations.Extension;

import com.google.appengine.api.datastore.Key;

/**
 *
 * @author jstakun
 */
@Entity
public class Search {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private String uri;
    
    private Date creationDate;
    
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private String username;
    
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private double latitude;
    
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private double longitude;
    
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private boolean auth;
    
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private int radius;
    
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private String query;
    
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private String locale;
    
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private int count;


    public Search(String username, String uri, boolean auth, double latitude, double longitude, int radius, String query, String locale, int count) {
        this();
    	this.username = username;
        this.uri = uri;
        this.auth = auth;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.query = query;
        this.locale = locale;
        this.count = count;
    }
    
    public Search() {
    	this.creationDate = new Date(System.currentTimeMillis());
    }
    
    public Date getCreationDate() {
    	return creationDate;
    }
}

