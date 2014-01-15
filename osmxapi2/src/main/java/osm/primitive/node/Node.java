package osm.primitive.node;

import osm.primitive.Primitive;
import osm.primitive.PrimitiveTypeEnum;

/**
 * @author Ian Dees
 * 
 */
public class Node extends Primitive {

    private LatLon point;

    public Node(double lat, double lng) {
        this.point = new LatLon(lat, lng);
    }

    public double getLat() {
        return point.getLat();
    }

    public double getLon() {
        return point.getLon();
    }

    public PrimitiveTypeEnum getType() {
        return PrimitiveTypeEnum.node;
    }

    public LatLon getPoint() {
        return point;
    }
    
}
