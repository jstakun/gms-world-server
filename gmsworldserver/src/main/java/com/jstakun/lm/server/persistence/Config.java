package com.jstakun.lm.server.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.google.appengine.api.datastore.Key;

/**
 *
 * @author jstakun
 */
@Entity

@NamedQueries({
	@NamedQuery(name = "Config.findAll", query = "select c from Config c"),
	@NamedQuery(name = "Config.findByKey", query = "select c from Config c where c.key = :key"),
})	

public class Config {
	
	public static final String CONFIG_FINDALL = "Config.findAll";
	public static final String CONFIG_FINDBYKEY= "Config.findByKey";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key keycode;

	private String key;

	private String value;

	public String getKey()
	{
		return key;
	}

	public String getValue()
	{
		return value;
	}
	
	public void setValue(String v) {
		this.value = v;
	}

	public Config(String key, String value)
	{
		this.key = key;
		this.value = value;
	}
  
	public Config() {
	  
	}
}
