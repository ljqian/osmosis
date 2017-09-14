package org.openstreetmap.osmosis.oracle.render;

import org.openstreetmap.osmosis.oracle.common.MapFeature;

import java.util.List;

public interface StylingRule {

    /**
     * Checks if this styleing rule applies to the given map feature.
     *
     * @param feature  A MapFeature instance
     * @return true if this styling rule applies, false otherwise.
     */
    public boolean applies(MapFeature feature, RenderingContext rc);

    /**
     * Returns the style value associated with the style key for the given feature
     * in the specified rendering context.
     * @param stylePropertyName  The name of a style property, such as "line-color" or "icon-rotate".
     * @param feature  The map feature for which the styling element applies.
     * @param rc The current rendering context
     * @return The most applicable value for the given style property.
     */
    public String getStyle(String stylePropertyName, MapFeature feature, RenderingContext rc);

    public String getDefaultStyle(String stylePropertyName);

    /**
     * Gets the directly nested set of styling rules, if any. If there is no nested styling rule then
     * an empty list is returned.
     * @return a list of directly nested styling rules, or an empty list. Never returns null.
     */
    public List<StylingRule> getNestedRules();
}
