/*
 * @author lj.qian
 */
package org.openstreetmap.osmosis.oracle.common;

import java.util.List;
import java.util.Map;

public interface TagStore {

    public String getTagValue(String tagKey);

    public boolean hasTags();

    public void setTags(Map<String, String> tagMap);

    public void copyTags(Map<String, String> tagMap);

    /**
     * Checks if the feature has the specified tag.
     * @param tagKey
     * @return true if the tagKey exists in this feature.
     */
    public boolean has(String tagKey);

    /**
     * Checks if the feature has a tag matching the supplied tag key and value. Value is case-insensitive.
     *
     * @param tagKey
     * @param tagValue
     * @return true if the feature has the specified key with the exact tag value (case-insensitive).
     */
    public boolean eq(String tagKey, String tagValue);

    /**
     * Checks if the feature has a tag whose value is not the same as the supplied one.
     * @param tagKey
     * @param tagValue
     * @return true if this feature has the tag key but not the same value as specified; false if there is no such
     *  key or if the key's value is not the same as the supplied one.
     */
    public boolean neq(String tagKey, String tagValue);

    /**
     * Checks if the feature has a tag with the specified key and whose value is in the supplied list of values
     * (case-insensitive).
     * @param tagKey
     * @param tagValues  The specified list of values to check; if this list is null then false is returned immediately
     * @return true if the specified tag exists and its value is among the supplied list of values.
     */
    public boolean in(String tagKey, List<String> tagValues);

}
