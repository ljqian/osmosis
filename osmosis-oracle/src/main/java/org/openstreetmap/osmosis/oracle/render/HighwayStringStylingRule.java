package org.openstreetmap.osmosis.oracle.render;

import org.openstreetmap.osmosis.oracle.common.GeometryType;
import org.openstreetmap.osmosis.oracle.common.MapFeature;

import java.util.List;

/*
 * @author lj.qian
 */
public class HighwayStringStylingRule implements StylingRule{

    @Override
    public boolean applies(MapFeature feature, RenderingContext rc) {
        if(feature==null)
            return false;

        GeometryType geomType = feature.getGeometryType();

        if(geomType != GeometryType.LineString &&
            geomType != GeometryType.MultiLineString)
            return false;

        return feature.has("highway");
    }

    @Override
    public String getStyle(String stylePropertyName, MapFeature feature, RenderingContext rc) {
        if(stylePropertyName.equals("line-width"))
           return getLineWidth(feature, rc);
        else
            return null;
    }

    @Override
    public String getDefaultStyle(String stylePropertyName) {
        return null;
    }

    @Override
    public List<StylingRule> getNestedRules() {
        return null;
    }

    public String getLineWidth(MapFeature feature, RenderingContext rc){

        return "2";
    }
}
