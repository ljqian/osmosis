/*
 * @author lj.qian
 */
package org.openstreetmap.osmosis.oracle.common;

import java.util.HashMap;

public class MapFeaturePoint extends AbstractMapFeature {

    private FeatureCommonData commonData = new FeatureCommonData();

    //The list of points that make up this linestring feature.
    private MapPoint point;

    public MapFeaturePoint(){
        super();
    }

    public MapFeaturePoint(long id){
        super(id);
    }

    public MapFeaturePoint(String id){
        super(id);
    }

    public MapFeaturePoint(long id, MapPoint pt){
        super(id);
        setPoint(pt);
    }

    public MapFeaturePoint(String id, MapPoint pt){
        super(id);
        setPoint(pt);
    }


    public MapFeaturePoint(FeatureCommonData commonData){
        super(commonData);
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.Point;
    }


    public MapPoint getPoint(){
        return point;
    }

    public void setPoint(MapPoint pt){
        point = pt;
    }

    public void setPoint(double x, double y){
        point = new MapPoint(x, y);
    }

    public void setPoint(double x, double y, int srid){
        point = new MapPointExt(x, y, srid);
    }
}
