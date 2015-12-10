package com.jstakun.lm.server.persistence;

import java.io.Serializable;

public class Layer implements Serializable {

	private static final long serialVersionUID = 1L;
    private String name;
    private String desc;
    private boolean enabled;
    private boolean manageable;
    private boolean checkinable;
    private String formatted;
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

    public boolean isCheckinable()
    {
        return checkinable;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setManageable(boolean manageable) {
        this.manageable = manageable;
    }

    public void setCheckinable(boolean checkinable)
    {
        this.checkinable = checkinable;
    }

    public String getFormatted() {
        return formatted;
    }
    
    public void setFormatted(String formatted) {
        this.formatted = formatted;
    }

    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public void setVersion(int version) {
    	this.version = version;
    }
    
    public int geVersion() {
    	return version;
    }
}
