package com.jstakun.lm.server.layers;

import java.util.List;
import java.util.Locale;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

public class SearchUtils extends LayerHelper {

	@Override
	protected List<ExtendedLandmark> processBinaryRequest(double lat, double lng,
			String query, int radius, int version, int limit, int stringLimit,
			String flexString, String flexString2, Locale locale)
			throws Exception {
		throw new Exception("Not yet implemented");
	}

	
}
