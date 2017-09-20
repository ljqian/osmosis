package org.openstreetmap.osmosis.oracle.render;

import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.Collection;
import java.util.Map;

/*
 * @author lj.qian
 */
public interface StyleSheet {
    public boolean applicable(Collection<Tag> featureTags);
    public boolean applicable(Map<String, String> featureTags);

}
