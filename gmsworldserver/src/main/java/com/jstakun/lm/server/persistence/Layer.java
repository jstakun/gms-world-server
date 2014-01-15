/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.persistence;

import java.io.Serializable;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 *
 * @author jstakun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Layer implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@PrimaryKey
    private String name;
    @Persistent
    private String desc;
    @Persistent
    private boolean enabled;
    @Persistent
    private boolean manageable;
    @Persistent
    private boolean checkinable;
    @Persistent
    private String formatted;
    @Persistent
    private int version;

    public Layer(String name, String desc, boolean enabled, boolean manageable, boolean checkinable, String formatted) {
        this();
    	this.name = name;
        this.enabled = enabled;
        this.manageable = manageable;
        this.checkinable = checkinable;
        this.formatted = formatted;
        this.desc = desc;
    }
    
    public Layer() {
    	this.version = 2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isManageable() {
        return manageable;
    }

    public boolean inCheckinable()
    {
        return checkinable;
    }

    public String getFormatted() {
        return formatted;
    }

    public String getDesc() {
        return desc;
    }
}
