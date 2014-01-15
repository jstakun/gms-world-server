package osm.output;

import osm.OSMFile;
import osm.primitive.node.Node;
import osm.primitive.relation.Relation;
import osm.primitive.way.Way;

public abstract class AbstractOutputter implements OSMOutputter {

    private OSMFile storage = new OSMFile();
    private int maxChanges;
    
    public void addNode(Node node) {
        checkAndWrite();
        
        storage.addNode(node);
    }

    private void checkAndWrite() {
        if(checkChanges()) {
            write(storage);
            storage = new OSMFile();
        }
    }

    /**
     * @return True if the file should be written.
     */
    private boolean checkChanges() {
        return storage.getChangeCount() >= maxChanges;
    }

    public void addRelation(Relation relation) {
        checkAndWrite();
        
        storage.addRelation(relation);
    }

    public void addWay(Way way) {
        checkAndWrite();
        
        storage.addWay(way);
    }

    public void finish() {
        write(storage);
        storage = new OSMFile();
    }

    public void setMaxElementsPerFile(int maxPerFile) {
        maxChanges = maxPerFile;
    }

    public void start() {

    }

    public abstract void write(OSMFile out);

}
