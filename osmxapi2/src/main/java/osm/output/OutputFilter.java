package osm.output;

import osm.OSMFile;

public interface OutputFilter {

    /**
     * @param out
     */
    OSMFile apply(OSMFile out);

}
