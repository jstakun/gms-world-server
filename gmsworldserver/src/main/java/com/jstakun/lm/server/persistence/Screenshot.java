/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.persistence;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 *
 * @author jstakun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Screenshot implements Serializable {

    private static final long serialVersionUID = 1L;
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private Date creationDate;
    @Persistent
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private String username;
    @Persistent
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private double latitude;
    @Persistent
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private double longitude;
    @Persistent
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private boolean auth;
    @Persistent
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private BlobKey blobKey;
    @Persistent
    private String keyString;

    public Screenshot(String username, boolean auth, double latitude, double longitude, BlobKey blobKey, Date creationDate) {
        if (creationDate == null) {
            this.creationDate = new Date(System.currentTimeMillis());
        } else {
            this.creationDate = creationDate;
        }
        this.username = username;
        this.auth = auth;
        this.latitude = latitude;
        this.longitude = longitude;
        this.blobKey = blobKey;
    }
    
    public Screenshot() {
    	
    }

    /**
     * @return the key
     */
    public Key getKey() {
        return key;
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

    /**
     * @return the blobKey
     */
    public BlobKey getBlobKey() {
        return blobKey;
    }
    
    public String getKeyString() {
        if (keyString == null) {
            return KeyFactory.keyToString(key);
        } else {
            return keyString;
        }
    }

    public void setKeyString(String keyString) {
        this.keyString = keyString;
    }
}
