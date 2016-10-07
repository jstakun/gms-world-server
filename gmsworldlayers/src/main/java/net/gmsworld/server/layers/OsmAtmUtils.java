package net.gmsworld.server.layers;

import net.gmsworld.server.config.Commons;

public class OsmAtmUtils extends OverpassUtils {

	@Override
	protected String getLayerName() {
		return Commons.OSM_ATM_LAYER;
	}
}
