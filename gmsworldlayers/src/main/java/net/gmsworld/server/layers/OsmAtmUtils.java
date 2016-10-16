package net.gmsworld.server.layers;

import net.gmsworld.server.config.Commons;

public class OsmAtmUtils extends OverpassUtils {

	public String getLayerName() {
		return Commons.OSM_ATM_LAYER;
	}
	
	public String getIcon() {
		return "credit_cards.png";
	}
}
