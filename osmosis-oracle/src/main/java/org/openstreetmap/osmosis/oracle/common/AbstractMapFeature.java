package org.openstreetmap.osmosis.oracle.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMapFeature implements MapFeature {
    private FeatureCommonData commonData = new FeatureCommonData();

    public AbstractMapFeature(){
    }

    public AbstractMapFeature(FeatureCommonData commonData){
        this.commonData = commonData;
    }

    public AbstractMapFeature(long id){
        commonData.setId(id);
    }

    public AbstractMapFeature(String id){
        commonData.setId(id);
    }

    @Override
    public String getId() {
        return commonData.getId();
    }

    @Override
    public String getTagValue(String tagKey) {
        return commonData.getTagValue(tagKey);
    }

    @Override
    public boolean hasTags() {
        return commonData.hasTags();
    }

    public void setTags(Map<String, String> tagMap){
        commonData.setTags(tagMap);
    }

    public void copyTags(Map<String, String> tagMap){
        commonData.copyTags(tagMap);
    }

    public Map<String, String> getTags() {
        return commonData.getTags();
    }

    /**
     * Checks if the feature has the specified tag.
     * @param tagKey
     * @return true if the tagKey exists in this feature.
     */
    public boolean has(String tagKey){
        return commonData.has(tagKey);
    }

    /**
     * Checks if the feature has a tag matching the supplied tag key and value. Value is case-insensitive.
     *
     * @param tagKey
     * @param tagValue
     * @return true if the feature has the specified key with the exact tag value (case-insensitive).
     */
    public boolean eq(String tagKey, String tagValue){
       return commonData.eq(tagKey, tagValue);
    }

    /**
     * Checks if the feature has a tag whose value is not the same as the supplied one.
     * @param tagKey
     * @param tagValue
     * @return true if this feature has the tag key but not the same value as specified; false if there is no such
     *  key or if the key's value is not the same as the supplied one.
     */
    public boolean neq(String tagKey, String tagValue){
        return commonData.neq(tagKey, tagValue);
    }

    /**
     * Checks if the feature has a tag with the specified key and whose value is in the supplied list of values
     * (case-insensitive).
     * @param tagKey
     * @param tagValues  The specified list of values to check; if this list is null then false is returned immediately
     * @return true if the specified tag exists and its value is among the supplied list of values.
     */
    public boolean in(String tagKey, List<String> tagValues){
        return commonData.in(tagKey, tagValues);
    }
}
