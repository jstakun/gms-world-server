package net.gmsworld.server.layers;

import net.gmsworld.server.config.Commons;

public class OsmParkingsUtils extends OverpassUtils {

	public String getLayerName() {
		return Commons.OSM_PARKING_LAYER;
	}
	
	public String getIcon() {
		return "parking.png";
	}
}
