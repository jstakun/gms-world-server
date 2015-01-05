package com.jstakun.gms.android.landmarks;

import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class LandmarkFactory {
     public static ExtendedLandmark getLandmark(String name, String desc, QualifiedCoordinates qc, String layer, AddressInfo address, long creationDate, String searchTerm)
     {
          return new ExtendedLandmark(name, desc, qc, layer, address, creationDate, searchTerm);
     }
}
