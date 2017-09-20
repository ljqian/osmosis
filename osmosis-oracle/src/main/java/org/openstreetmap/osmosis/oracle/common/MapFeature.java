/*
 * @author lj.qian
 */
package org.openstreetmap.osmosis.oracle.common;

import java.util.HashMap;

/**
 * Represents a single map feature with a unique Id and an optional set of tags. Tags are key-value pairs where both
 * the key and value are texts.
 */
public interface MapFeature extends TagStore {

    public String getId();

    public GeometryType getGeometryType();

}
