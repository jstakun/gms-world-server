package net.gmsworld.server.layers;

import net.gmsworld.server.config.Commons;

public class OsmTaxiUtils extends OverpassUtils {

	@Override
	protected String getLayerName() {
		return Commons.OSM_TAXI_LAYER;
	}
}
