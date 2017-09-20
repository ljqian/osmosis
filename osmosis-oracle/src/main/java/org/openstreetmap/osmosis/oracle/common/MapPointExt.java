/*
 * @author lj.qian
 */
package org.openstreetmap.osmosis.oracle.common;

/**
 * An extended map point where the SRID can be anything. If your point is not in the longitude/latitude space
 * then this is the point class to use.
 */
public class MapPointExt extends MapPoint {

    private int srid = 4326;

    public MapPointExt(double x, double y){
        super(x,y);
    }

    public MapPointExt(double x, double y, int srid){
        super(x,y);
        this.srid = srid;
    }

    public MapPointExt(){

    }


    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public void setPoint(double x, double y){
        this.x = x;
        this.y = y;
        this.srid = 4326;
    }

    public void setPoint(double x, double y, int srid){
        this.x = x;
        this.y = y;
        this.srid = srid;
    }

}
