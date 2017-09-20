/*
 * @author lj.qian
 */
package org.openstreetmap.osmosis.oracle.common;

public class SimpleNode {
    private long id;

    private double latitude;
    private double longitude;

    public SimpleNode(long id, double lon, double lat){
        this.id = id;
        this.longitude = lon;
        this.latitude = lat;
    }


    public long getId(){
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }


}
