package net.gmsworld.server.layers;

import net.gmsworld.server.config.Commons;

public class OsmParkingsUtils extends OverpassUtils {

	@Override
	protected String getLayerName() {
		return Commons.OSM_PARKING_LAYER;
	}
}
