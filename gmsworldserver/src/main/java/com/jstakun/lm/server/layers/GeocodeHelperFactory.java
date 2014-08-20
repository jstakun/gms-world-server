package com.jstakun.lm.server.layers;

import net.gmsworld.server.layers.MapQuestUtils;

public class GeocodeHelperFactory {

	private static final MapQuestUtils mapQuestUtils = new MapQuestUtils();
	
	private static final GoogleGeocodeUtils googleGeocodeUtils = new GoogleGeocodeUtils();
	
	public static MapQuestUtils getMapQuestUtils() {
		return mapQuestUtils;
	}
	
	protected static GoogleGeocodeUtils getGoogleGeocodeUtils() {
		return googleGeocodeUtils;
	}
}
