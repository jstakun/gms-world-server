package net.gmsworld.server.utils.persistence;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import net.gmsworld.server.config.Commons;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class Landmark implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    
    private double latitude;
    
    private double longitude;
    
    private Double altitude;
    
    private String name;
    
    private String description;
    
    private String username;
    
    private Date creationDate;
   
    private Date validityDate;
    
    private String layer;
    
    private String hash;
   
    private String flex;
    
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
        this.email = email;
    }
    
    public Landmark() {
    	this.creationDate = new Date(System.currentTimeMillis());
    	this.email = "";
    	this.id = -1;
    	this.altitude = 0d;
    	this.description = "";
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

    public void setDescription(String desc) {
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
    
    public int getId() {
    	return id;
    }
    
    public void setId(int id){
    	this.id = id;
    }
    
    public void setFlex(String f) {
        this.flex = f;
    }
    
    public String getFlex() {
    	return flex;
    }
    
    public void setEmail(String e) {
    	this.email = e;
    }
    
    public String getEmail() {
    	return email;
    }
    
    public int getUseCount() {
    	return getIntFlex("useCount");
    }
    
    public int getAppId() {
    	return getIntFlex("appId");
    }
    
    public int getVersion() {
    	return getIntFlex("version");
    }
    
    public String getCountryCode() {
    	return getStringFlex("cc");
    }
    
    public String getCity() {
    	return getStringFlex("city");
    }
    
    public boolean compare(Landmark l) {
    	return StringUtils.equalsIgnoreCase(getName(), l.getName()) && 
    			Math.abs(getLatitude() - l.getLatitude()) < 0.02d &&
    			Math.abs(getLongitude() - l.getLongitude()) < 0.02d &&
    			StringUtils.equals(getLayer(), l.getLayer());
    }
    
    public boolean isSocial() {
    	return StringUtils.equals(getLayer(), Commons.SOCIAL);
    }
    
    private int getIntFlex(String name) {
    	try {
    		if (flex != null) {
    			JSONObject details = new JSONObject(flex);
    			return details.getInt(name);
    		} else {
    			return -1;
    		}
    	} catch (Exception e) {
    		return -1;
    	}
    }
    
    private String getStringFlex(String name) {
    	try {
    		if (flex != null) {
    			JSONObject details = new JSONObject(flex);
    			return details.optString(name);
    		} else {
    			return null;
    		}
    	} catch (Exception e) {
    		return null;
    	}
    }
    
}
