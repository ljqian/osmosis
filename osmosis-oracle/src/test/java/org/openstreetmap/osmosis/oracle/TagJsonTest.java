package org.openstreetmap.osmosis.oracle;

import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.oracle.common.TagJson;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by lqian on 9/20/17.
 */
public class TagJsonTest {
    @Test
    public void getJsonFromCollection(){
        Collection<Tag> tagCollection = new ArrayList<>();

        Tag tag = new Tag("abc", "v1");
        tagCollection.add(tag);

        tag = new Tag("a_\"cd\"", "v2\"");
        tagCollection.add(tag);

        tag = new Tag("abc", "你好");
        tagCollection.add(tag);

        String json = TagJson.getJson(tagCollection);
        System.out.println("json="+ json);
    }
}
