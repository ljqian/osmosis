package org.openstreetmap.osmosis.oracle.common;

/**
 * A map point in the longitude/latitude space.
 */
public class MapPoint {
    protected double x;
    protected double y;

    public MapPoint(){

    }

    public MapPoint(double x, double y){
        this.x = x;
        this.y = y;
    }

    /**
     * Always returns 4326. Suitable for representing Lat/Lon while also support sub-class that has non-geodetic
     * coordinate systems.
     *
     * @return The SRID of this point in space.
     */
    public int getSrid (){
        return 4326;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
