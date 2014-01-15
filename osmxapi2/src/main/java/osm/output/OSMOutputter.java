package osm.output;

import osm.OSMFile;
import osm.primitive.node.Node;
import osm.primitive.relation.Relation;
import osm.primitive.way.Way;


public interface OSMOutputter {

    /**
     * @param maxPerFile The maximum number of changes per file to allow.
     */
    void setMaxElementsPerFile(int maxPerFile);

    /**
     * Called when the output OSM file should be created.
     */
    void start();

    /**
     * @param way The way to add.
     */
    void addWay(Way way);

    /**
     * @param relation The relation to add.
     */
    void addRelation(Relation relation);

    /**
     * @param node The node to add.
     */
    void addNode(Node node);

    /**
     * Called when the output OSM file should be completed.
     */
    void finish();

    /**
     * @param out
     */
    void write(OSMFile out);

}
