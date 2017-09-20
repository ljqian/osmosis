package org.openstreetmap.osmosis.oracle.render;

import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
/*
 * @author lj.qian
 */
public class HighwayStyleSheet implements StyleSheet{
    private List<StylingRule> rules;

    public boolean loadFromJson(File f){
        return true;
    }

    public boolean loadFromJson(String jsonStyleSheetDoc){
        return true;
    }

    public void addRule(StylingRule rule){
        if(rules==null)
            rules = new ArrayList<>();

        rules.add(rule);
    }

    public void addRule(StylingRule rule, int index){
        if(rules==null)
            rules = new ArrayList<>();

        rules.add(index, rule);
    }

    public Color getColor(Collection<Tag> tags){
        //TODO: actually check the feature tags and apply styling rule accordingly.
        return Color.black;
    }

    public float getStrokeWidth(Collection<Tag> tags){
        //TODO: actually check the feature tags and apply styling rule accordingly.
        return 1.0f;
    }

    private static String[] _tags = new String[]{
            "motorway", "trunk", "primary", "secondary", "tertiary",
            "motorway_link", "trunk_link", "primary_link", "secondary_link", "tertiary_link",
            "unclassified", "service", "footway",
            "living_street", "pedestrian", "road"};

    private static Set<String> highwayTagSet = new HashSet<>(Arrays.asList(_tags));

    @Override
    public boolean applicable(Collection<Tag> featureTags) {
        for(Tag tag : featureTags){
            String key = tag.getKey();
            String value = tag.getValue();
            if(key.equals("highway")){
                //only these types of highways and streets are to be rendered
                return highwayTagSet.contains(value);
            }
        }
        return false;
    }

    @Override
    public boolean applicable(Map<String, String> featureTags) {
        String value = featureTags.get("highway");
        return value!=null && highwayTagSet.contains(value);
    }


}
