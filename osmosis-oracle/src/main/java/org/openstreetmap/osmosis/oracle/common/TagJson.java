package org.openstreetmap.osmosis.oracle.common;

import org.apache.commons.text.StringEscapeUtils;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.Collection;

/**
 * Creates a JSON document containing all the tags.
 * Created by lqian on 9/20/17.
 *
 */
public class TagJson {

    public static String getJson(Collection<Tag> tags){
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        String sep = "";
        for(Tag tag: tags){
            String key = tag.getKey();
            String value = tag.getValue();

            key = StringEscapeUtils.escapeJson(key);
            value = StringEscapeUtils.escapeJson(value);

            sb.append(sep);
            sb.append("\""+key+"\":"+"\""+value+"\"");
            sep = ",";
        }

        sb.append("}");

        return sb.toString();
    }
}
