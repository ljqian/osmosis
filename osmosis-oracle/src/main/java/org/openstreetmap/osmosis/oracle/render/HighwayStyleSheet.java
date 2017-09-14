package org.openstreetmap.osmosis.oracle.render;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

}
