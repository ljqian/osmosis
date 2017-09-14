package org.openstreetmap.osmosis.oracle.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapFeatureLineString extends AbstractMapFeature {
    private FeatureCommonData commonData = new FeatureCommonData();

    //The list of points that make up this linestring feature.
    private List<MapPoint> points;

    public MapFeatureLineString(){
        super();
    }

    public MapFeatureLineString(String id){
        super(id);
    }

    public MapFeatureLineString(long id){
        super(id);
    }

    public MapFeatureLineString(String id, List<MapPoint> points){
        super(id);
        this.points = points;
    }

    public MapFeatureLineString(long id, List<MapPoint> points){
        super(id);
        this.points = points;
    }

    public MapFeatureLineString(String id, MapPoint[] points){
        super(id);
        this.points = Arrays.asList(points);
    }

    public MapFeatureLineString(long id, MapPoint[] points){
        super(id);
        this.points = Arrays.asList(points);
    }

    public MapFeatureLineString(FeatureCommonData commonData){
        super(commonData);
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.LineString;
    }

    public List<MapPoint> getPoints(){
        return this.points;
    }

    public static void main(String[] args){
        MapFeatureLineString line = new MapFeatureLineString(12341);


    }
}
