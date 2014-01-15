/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.persistence;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.beoui.geocell.annotations.Geocells;
import com.beoui.geocell.annotations.Latitude;
import com.beoui.geocell.annotations.Longitude;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 *
 * @author jstakun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Landmark implements Serializable {
    private static final long serialVersionUID = 1L;
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    @Latitude
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private double latitude;
    @Persistent
    @Longitude
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private double longitude;
    @Persistent
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private Double altitude;
    @Persistent
    private String name;
    @Persistent
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private String description;
    @Persistent
    private String keyString;
    @Persistent
    private String username;
    @Persistent
    private Date creationDate;
    @Persistent
    private Date validityDate;
    @Persistent
    private String layer;
    @Persistent
    @Geocells
    private List<String> geoCells;
    @Persistent
    private String hash;
    @Persistent
    @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
    private String email;

    public Landmark(double latitude, double longitude, double altitude, String name, String description, String username, Date validityDate, String layer, List<String> geoCells, String email) {
        this();
    	this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = new Double(altitude);
        this.name = name;
        this.description = description;
        this.username = username;
        this.validityDate = validityDate;       
        this.layer = layer;
        this.geoCells = geoCells;
        this.email = email;
    }
    
    public Landmark() {
    	this.creationDate = new Date(System.currentTimeMillis());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDecription(String desc) {
        this.description = desc;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double l) {
        this.latitude = l;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double l) {
        this.longitude = l;
    }

    public double getAltitude() {
        return altitude.doubleValue();
    }

    public void setAltitude(double l) {
        this.altitude = new Double(l);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date d) {
        this.creationDate = d;
    }

    public Date getValidityDate() {
        return validityDate;
    }

    public void setValidityDate(Date d) {
        this.validityDate = d;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String l) {
        this.layer = l;
    }

    public List<String> getGeoCells() {
        return geoCells;
    }

    /**
     * @param Facilities the Facilities to set
     */
    public void setGeoCells(List<String> geoCells) {
        this.geoCells = geoCells;
    }

    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }
}
