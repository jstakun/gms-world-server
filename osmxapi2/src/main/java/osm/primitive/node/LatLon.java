package osm.primitive.node;

public class LatLon {

    private double x,y;

    public LatLon(double lat, double lon) {
        x = lat;
        y = lon;
    }
    
    public double getLat() {
        return x;
    }
    
    public double getLon() {
        return y;
    }
}
